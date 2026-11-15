package com.ironsmagiccircle.client.gui;

import com.ironsmagiccircle.client.CustomTextureManager;
import com.ironsmagiccircle.client.MagicCircleRenderHandler;
import com.ironsmagiccircle.client.SchoolVisualHelper;
import com.ironsmagiccircle.config.MagicCircleClientConfig;
import com.ironsmagiccircle.config.preset.MagicCirclePreset;
import com.ironsmagiccircle.config.preset.PresetManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.render.RenderHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import java.util.List;

/**
 * Interactive In-Game Configuration Screen for Iron's Spell Magic Circle.
 * Features:
 * 1. Live 3D rotating magic circle preview with real-time feedback for texture, scale, and speed adjustments.
 * 2. Per-layer custom texture overrides (supporting {school} placeholder) for all 5 layers.
 * 3. Per-layer scale and speed sliders (Clockwise / Counter-Clockwise).
 * 4. Per-phase dynamics (Emergence / Cast-In, Active Sustained, Fade-Out Dissolution).
 * 5. General and Gun Muzzle Flash compatibility settings.
 * 6. Dynamic School Detection: Automatically detects and renders any addon-registered school.
 * 7. Custom Preset Maker: Name, Save, Overwrite, Load, and Delete custom user presets (.json).
 * 8. Standard Presets: 5 built-in tuned presets ready to use or customize.
 */
public class MagicCircleConfigScreen extends Screen {

    private final Screen parentScreen;

    public enum MainTab {
        LAYERS,
        PHASES,
        GENERAL_AND_GUN,
        PRESETS
    }

    public enum PhaseSubTab {
        EMERGENCE,
        ACTIVE,
        FADE_OUT
    }

    public enum GeneralSubTab {
        GENERAL,
        GUN_MUZZLE
    }

    public enum PresetsSubTab {
        CUSTOM,
        STANDARD
    }

    // Active Navigation State
    private MainTab activeMainTab = MainTab.LAYERS;
    private int selectedLayerIndex = 0; // 0..4 (Layer 1..5)
    private PhaseSubTab activePhaseSubTab = PhaseSubTab.EMERGENCE;
    private GeneralSubTab activeGeneralSubTab = GeneralSubTab.GENERAL;
    private PresetsSubTab activePresetsSubTab = PresetsSubTab.CUSTOM;

    // Draft State - Layers 1..5
    private final String[] draftLayerTextures = new String[5];
    private final double[] draftLayerScales = new double[5];
    private final double[] draftLayerSpeeds = new double[5];

    // Draft State - Phase 1: Emergence
    private double draftEmergenceStartScale;
    private double draftEmergenceSpeedMult;
    private double draftLayerFadeInTicks;
    private double draftLayerStaggerTicks;
    private double draftLayerSlideDistance;

    // Draft State - Phase 2: Active
    private double draftActiveScale;
    private double draftActiveSpeedMult;
    private double draftPulseIntensity;

    // Draft State - Phase 3: Fade-Out
    private double draftFadeOutEndScale;
    private double draftFadeOutSpeedMult;
    private int draftFadeOutTicks;
    private double draftFadeOutDriftDistance;

    // Draft State - General
    private boolean draftEnableMagicCircles;
    private double draftCircleScale;
    private double draftForwardDistance;
    private double draftRotationSpeed;
    private double draftCircleOpacity;
    private boolean draftEnableShaderBloomGlow;
    private double draftGlowIntensity;

    // Draft State - Gun Muzzle
    private boolean draftEnableGunMagicCircles;
    private double draftGunCircleScale;
    private double draftGunBlastExpansion;
    private int draftGunCircleDurationTicks;
    private double draftGunCircleForwardOffset;
    private double draftGunCircleRotationSpeed;
    private double draftGunCircleOpacity;

    // Preview Window State (Dynamic School List from SchoolVisualHelper)
    private List<ResourceLocation> availableSchools;
    private int previewSchoolIndex = 0;
    private int previewLayerCount = 5; // 1..5
    private boolean simulateCastActive = false;
    private float simulateCastStartTime = 0.0f;

    // Widgets reference
    private EditBox textureEditBox;
    private EditBox newPresetNameBox;
    private String presetStatusMessage = "";
    private ChatFormatting presetStatusColor = ChatFormatting.WHITE;

    // Dynamic 3D Preview Frame Bounds
    private int previewBoxX = 0;
    private int previewBoxY = 0;
    private int previewBoxW = 0;
    private int previewBoxH = 0;

    // Preset Pagination for smaller screens
    private int customPresetPage = 0;
    private int standardPresetPage = 0;

    public MagicCircleConfigScreen(Screen parentScreen) {
        super(Component.literal("Magic Circle Configuration"));
        this.parentScreen = parentScreen;
        loadDraftFromConfig();
        this.availableSchools = SchoolVisualHelper.getAvailableSchools();
        for (int i = 0; i < availableSchools.size(); i++) {
            if ("holy".equals(availableSchools.get(i).getPath())) {
                previewSchoolIndex = i;
                break;
            }
        }
    }

    private void loadDraftFromConfig() {
        for (int i = 0; i < 5; i++) {
            draftLayerTextures[i] = MagicCircleClientConfig.getCustomLayerTexture(i);
            draftLayerScales[i] = MagicCircleClientConfig.getLayerScale(i);
            draftLayerSpeeds[i] = MagicCircleClientConfig.getLayerSpeed(i);
        }

        draftEmergenceStartScale = MagicCircleClientConfig.EMERGENCE_START_SCALE.get();
        draftEmergenceSpeedMult = MagicCircleClientConfig.EMERGENCE_SPEED_MULTIPLIER.get();
        draftLayerFadeInTicks = MagicCircleClientConfig.LAYER_FADE_IN_TICKS.get();
        draftLayerStaggerTicks = MagicCircleClientConfig.LAYER_STAGGER_TICKS.get();
        draftLayerSlideDistance = MagicCircleClientConfig.LAYER_SLIDE_DISTANCE.get();

        draftActiveScale = MagicCircleClientConfig.ACTIVE_SCALE.get();
        draftActiveSpeedMult = MagicCircleClientConfig.ACTIVE_SPEED_MULTIPLIER.get();
        draftPulseIntensity = MagicCircleClientConfig.PULSE_INTENSITY.get();

        draftFadeOutEndScale = MagicCircleClientConfig.FADE_OUT_END_SCALE.get();
        draftFadeOutSpeedMult = MagicCircleClientConfig.FADE_OUT_SPEED_MULTIPLIER.get();
        draftFadeOutTicks = MagicCircleClientConfig.FADE_OUT_TICKS.get();
        draftFadeOutDriftDistance = MagicCircleClientConfig.FADE_OUT_DRIFT_DISTANCE.get();

        draftEnableMagicCircles = MagicCircleClientConfig.ENABLE_MAGIC_CIRCLES.get();
        draftCircleScale = MagicCircleClientConfig.CIRCLE_SCALE.get();
        draftForwardDistance = MagicCircleClientConfig.FORWARD_DISTANCE.get();
        draftRotationSpeed = MagicCircleClientConfig.ROTATION_SPEED.get();
        draftCircleOpacity = MagicCircleClientConfig.CIRCLE_OPACITY.get();
        draftEnableShaderBloomGlow = MagicCircleClientConfig.ENABLE_SHADER_BLOOM_GLOW.get();
        draftGlowIntensity = MagicCircleClientConfig.GLOW_INTENSITY.get();

        draftEnableGunMagicCircles = MagicCircleClientConfig.ENABLE_GUN_MAGIC_CIRCLES.get();
        draftGunCircleScale = MagicCircleClientConfig.GUN_CIRCLE_SCALE.get();
        draftGunBlastExpansion = MagicCircleClientConfig.GUN_BLAST_EXPANSION.get();
        draftGunCircleDurationTicks = MagicCircleClientConfig.GUN_CIRCLE_DURATION_TICKS.get();
        draftGunCircleForwardOffset = MagicCircleClientConfig.GUN_CIRCLE_FORWARD_OFFSET.get();
        draftGunCircleRotationSpeed = MagicCircleClientConfig.GUN_CIRCLE_ROTATION_SPEED.get();
        draftGunCircleOpacity = MagicCircleClientConfig.GUN_CIRCLE_OPACITY.get();
    }

    private void saveDraftToConfig() {
        MagicCircleClientConfig.LAYER_1_TEXTURE.set(draftLayerTextures[0]);
        MagicCircleClientConfig.LAYER_2_TEXTURE.set(draftLayerTextures[1]);
        MagicCircleClientConfig.LAYER_3_TEXTURE.set(draftLayerTextures[2]);
        MagicCircleClientConfig.LAYER_4_TEXTURE.set(draftLayerTextures[3]);
        MagicCircleClientConfig.LAYER_5_TEXTURE.set(draftLayerTextures[4]);

        MagicCircleClientConfig.LAYER_1_SCALE.set(draftLayerScales[0]);
        MagicCircleClientConfig.LAYER_2_SCALE.set(draftLayerScales[1]);
        MagicCircleClientConfig.LAYER_3_SCALE.set(draftLayerScales[2]);
        MagicCircleClientConfig.LAYER_4_SCALE.set(draftLayerScales[3]);
        MagicCircleClientConfig.LAYER_5_SCALE.set(draftLayerScales[4]);

        MagicCircleClientConfig.LAYER_1_SPEED.set(draftLayerSpeeds[0]);
        MagicCircleClientConfig.LAYER_2_SPEED.set(draftLayerSpeeds[1]);
        MagicCircleClientConfig.LAYER_3_SPEED.set(draftLayerSpeeds[2]);
        MagicCircleClientConfig.LAYER_4_SPEED.set(draftLayerSpeeds[3]);
        MagicCircleClientConfig.LAYER_5_SPEED.set(draftLayerSpeeds[4]);

        MagicCircleClientConfig.EMERGENCE_START_SCALE.set(draftEmergenceStartScale);
        MagicCircleClientConfig.EMERGENCE_SPEED_MULTIPLIER.set(draftEmergenceSpeedMult);
        MagicCircleClientConfig.LAYER_FADE_IN_TICKS.set(draftLayerFadeInTicks);
        MagicCircleClientConfig.LAYER_STAGGER_TICKS.set(draftLayerStaggerTicks);
        MagicCircleClientConfig.LAYER_SLIDE_DISTANCE.set(draftLayerSlideDistance);

        MagicCircleClientConfig.ACTIVE_SCALE.set(draftActiveScale);
        MagicCircleClientConfig.ACTIVE_SPEED_MULTIPLIER.set(draftActiveSpeedMult);
        MagicCircleClientConfig.PULSE_INTENSITY.set(draftPulseIntensity);

        MagicCircleClientConfig.FADE_OUT_END_SCALE.set(draftFadeOutEndScale);
        MagicCircleClientConfig.FADE_OUT_SPEED_MULTIPLIER.set(draftFadeOutSpeedMult);
        MagicCircleClientConfig.FADE_OUT_TICKS.set(draftFadeOutTicks);
        MagicCircleClientConfig.FADE_OUT_DRIFT_DISTANCE.set(draftFadeOutDriftDistance);

        MagicCircleClientConfig.ENABLE_MAGIC_CIRCLES.set(draftEnableMagicCircles);
        MagicCircleClientConfig.CIRCLE_SCALE.set(draftCircleScale);
        MagicCircleClientConfig.FORWARD_DISTANCE.set(draftForwardDistance);
        MagicCircleClientConfig.ROTATION_SPEED.set(draftRotationSpeed);
        MagicCircleClientConfig.CIRCLE_OPACITY.set(draftCircleOpacity);
        MagicCircleClientConfig.ENABLE_SHADER_BLOOM_GLOW.set(draftEnableShaderBloomGlow);
        MagicCircleClientConfig.GLOW_INTENSITY.set(draftGlowIntensity);

        MagicCircleClientConfig.ENABLE_GUN_MAGIC_CIRCLES.set(draftEnableGunMagicCircles);
        MagicCircleClientConfig.GUN_CIRCLE_SCALE.set(draftGunCircleScale);
        MagicCircleClientConfig.GUN_BLAST_EXPANSION.set(draftGunBlastExpansion);
        MagicCircleClientConfig.GUN_CIRCLE_DURATION_TICKS.set(draftGunCircleDurationTicks);
        MagicCircleClientConfig.GUN_CIRCLE_FORWARD_OFFSET.set(draftGunCircleForwardOffset);
        MagicCircleClientConfig.GUN_CIRCLE_ROTATION_SPEED.set(draftGunCircleRotationSpeed);
        MagicCircleClientConfig.GUN_CIRCLE_OPACITY.set(draftGunCircleOpacity);

        MagicCircleClientConfig.saveConfig();
    }

    public void applyPresetToDraft(MagicCirclePreset preset) {
        if (preset == null) return;
        for (int i = 0; i < 5; i++) {
            draftLayerTextures[i] = preset.layerTextures[i];
            draftLayerScales[i] = preset.layerScales[i];
            draftLayerSpeeds[i] = preset.layerSpeeds[i];
        }

        draftEmergenceStartScale = preset.emergenceStartScale;
        draftEmergenceSpeedMult = preset.emergenceSpeedMultiplier;
        draftLayerFadeInTicks = preset.layerFadeInTicks;
        draftLayerStaggerTicks = preset.layerStaggerTicks;
        draftLayerSlideDistance = preset.layerSlideDistance;

        draftActiveScale = preset.activeScale;
        draftActiveSpeedMult = preset.activeSpeedMultiplier;
        draftPulseIntensity = preset.pulseIntensity;

        draftFadeOutEndScale = preset.fadeOutEndScale;
        draftFadeOutSpeedMult = preset.fadeOutSpeedMultiplier;
        draftFadeOutTicks = preset.fadeOutTicks;
        draftFadeOutDriftDistance = preset.fadeOutDriftDistance;

        draftEnableMagicCircles = preset.enableMagicCircles;
        draftCircleScale = preset.circleScale;
        draftForwardDistance = preset.forwardDistance;
        draftRotationSpeed = preset.rotationSpeed;
        draftCircleOpacity = preset.circleOpacity;
        draftEnableShaderBloomGlow = preset.enableShaderBloomGlow;
        draftGlowIntensity = preset.glowIntensity;

        draftEnableGunMagicCircles = preset.enableGunMagicCircles;
        draftGunCircleScale = preset.gunCircleScale;
        draftGunBlastExpansion = preset.gunBlastExpansion;
        draftGunCircleDurationTicks = preset.gunCircleDurationTicks;
        draftGunCircleForwardOffset = preset.gunCircleForwardOffset;
        draftGunCircleRotationSpeed = preset.gunCircleRotationSpeed;
        draftGunCircleOpacity = preset.gunCircleOpacity;

        presetStatusMessage = "Loaded preset: " + preset.displayName;
        presetStatusColor = ChatFormatting.GREEN;

        refreshWidgets();
    }

    private MagicCirclePreset createPresetFromDraft(String name) {
        MagicCirclePreset p = new MagicCirclePreset("custom", name, "User custom preset", false);
        for (int i = 0; i < 5; i++) {
            p.layerTextures[i] = draftLayerTextures[i];
            p.layerScales[i] = draftLayerScales[i];
            p.layerSpeeds[i] = draftLayerSpeeds[i];
        }

        p.emergenceStartScale = draftEmergenceStartScale;
        p.emergenceSpeedMultiplier = draftEmergenceSpeedMult;
        p.layerFadeInTicks = draftLayerFadeInTicks;
        p.layerStaggerTicks = draftLayerStaggerTicks;
        p.layerSlideDistance = draftLayerSlideDistance;

        p.activeScale = draftActiveScale;
        p.activeSpeedMultiplier = draftActiveSpeedMult;
        p.pulseIntensity = draftPulseIntensity;

        p.fadeOutEndScale = draftFadeOutEndScale;
        p.fadeOutSpeedMultiplier = draftFadeOutSpeedMult;
        p.fadeOutTicks = draftFadeOutTicks;
        p.fadeOutDriftDistance = draftFadeOutDriftDistance;

        p.enableMagicCircles = draftEnableMagicCircles;
        p.circleScale = draftCircleScale;
        p.forwardDistance = draftForwardDistance;
        p.rotationSpeed = draftRotationSpeed;
        p.circleOpacity = draftCircleOpacity;
        p.enableShaderBloomGlow = draftEnableShaderBloomGlow;
        p.glowIntensity = draftGlowIntensity;

        p.enableGunMagicCircles = draftEnableGunMagicCircles;
        p.gunCircleScale = draftGunCircleScale;
        p.gunBlastExpansion = draftGunBlastExpansion;
        p.gunCircleDurationTicks = draftGunCircleDurationTicks;
        p.gunCircleForwardOffset = draftGunCircleForwardOffset;
        p.gunCircleRotationSpeed = draftGunCircleRotationSpeed;
        p.gunCircleOpacity = draftGunCircleOpacity;

        return p;
    }

    @Override
    protected void init() {
        super.init();
        this.availableSchools = SchoolVisualHelper.getAvailableSchools();
        refreshWidgets();
    }

    private void refreshWidgets() {
        this.clearWidgets();

        // 0. Responsive Dimensions & Scaling Tiers
        int screenW = this.width;
        int screenH = this.height;

        int marginX = Math.max(6, Math.min(14, (int) (screenW * 0.025)));
        int marginY = Math.max(3, Math.min(8, (int) (screenH * 0.02)));

        int titleY = marginY;
        int topTabY = titleY + 12;
        int topTabH = Math.clamp((int) (screenH * 0.075), 14, 18);

        int bottomBarH = Math.clamp((int) (screenH * 0.075), 14, 18);
        int bottomBarY = screenH - bottomBarH - marginY;

        int contentStartY = topTabY + topTabH + Math.max(2, (int) (screenH * 0.01));
        int contentBottomY = bottomBarY - Math.max(2, (int) (screenH * 0.01));
        int availableContentH = Math.max(50, contentBottomY - contentStartY);

        int previewPanelW = Math.clamp((int) (screenW * 0.35), 110, 210);
        int panelGap = Math.max(4, Math.min(8, (int) (screenW * 0.015)));

        int previewX = screenW - previewPanelW - marginX;
        int controlsStartX = marginX;
        int controlsW = previewX - panelGap - controlsStartX;

        // Dynamic row height & gaps so controls NEVER exceed availableContentH
        int neededWidgetRows = 6;
        int neededTextLines = 0;
        if (activeMainTab == MainTab.LAYERS) {
            neededWidgetRows = 6;
            neededTextLines = 2; // Role description + Custom Texture header
        } else if (activeMainTab == MainTab.PHASES) {
            neededWidgetRows = activePhaseSubTab == PhaseSubTab.EMERGENCE ? 6 : (activePhaseSubTab == PhaseSubTab.ACTIVE ? 4 : 5);
            neededTextLines = 0;
        } else if (activeMainTab == MainTab.GENERAL_AND_GUN) {
            neededWidgetRows = 7;
            neededTextLines = 0;
        } else if (activeMainTab == MainTab.PRESETS) {
            neededWidgetRows = 6;
            neededTextLines = activePresetsSubTab == PresetsSubTab.CUSTOM ? 2 : 1;
        }

        int textSpace = neededTextLines * 10;
        int usableH = Math.max(30, availableContentH - textSpace);
        int rowH = Math.clamp(usableH / (neededWidgetRows + 1), 12, 18);
        int totalGaps = neededWidgetRows + neededTextLines;
        int remainingSpace = availableContentH - (neededWidgetRows * rowH) - textSpace;
        int rowGap = Math.clamp(remainingSpace / Math.max(1, totalGaps), 1, 4);
        int labelGap = Math.max(1, rowGap);

        // 1. Top Navigation Bar (Main Tabs: 4 tabs)
        int tabBtnWidth = (controlsW - (3 * 3)) / 4;
        String[] tabNames = tabBtnWidth < 46
                ? new String[]{"Lyr", "Phs", "Gen", "Prst"}
                : new String[]{"Layers", "Phases", "General", "Presets"};

        addRenderableWidget(Button.builder(
                getTabComponent(tabNames[0], activeMainTab == MainTab.LAYERS, tabBtnWidth),
                b -> {
                    activeMainTab = MainTab.LAYERS;
                    refreshWidgets();
                }
        ).bounds(controlsStartX, topTabY, tabBtnWidth, topTabH).build());

        addRenderableWidget(Button.builder(
                getTabComponent(tabNames[1], activeMainTab == MainTab.PHASES, tabBtnWidth),
                b -> {
                    activeMainTab = MainTab.PHASES;
                    refreshWidgets();
                }
        ).bounds(controlsStartX + tabBtnWidth + 3, topTabY, tabBtnWidth, topTabH).build());

        addRenderableWidget(Button.builder(
                getTabComponent(tabNames[2], activeMainTab == MainTab.GENERAL_AND_GUN, tabBtnWidth),
                b -> {
                    activeMainTab = MainTab.GENERAL_AND_GUN;
                    refreshWidgets();
                }
        ).bounds(controlsStartX + (tabBtnWidth + 3) * 2, topTabY, tabBtnWidth, topTabH).build());

        addRenderableWidget(Button.builder(
                getTabComponent(tabNames[3], activeMainTab == MainTab.PRESETS, tabBtnWidth),
                b -> {
                    activeMainTab = MainTab.PRESETS;
                    refreshWidgets();
                }
        ).bounds(controlsStartX + (tabBtnWidth + 3) * 3, topTabY, tabBtnWidth, topTabH).build());

        // 2. Active Tab Sub-Controls
        switch (activeMainTab) {
            case LAYERS -> buildLayersTabControls(controlsStartX, contentStartY, controlsW, rowH, rowGap, labelGap, contentBottomY);
            case PHASES -> buildPhasesTabControls(controlsStartX, contentStartY, controlsW, rowH, rowGap, contentBottomY);
            case GENERAL_AND_GUN -> buildGeneralGunTabControls(controlsStartX, contentStartY, controlsW, rowH, rowGap, contentBottomY);
            case PRESETS -> buildPresetsTabControls(controlsStartX, contentStartY, controlsW, rowH, rowGap, labelGap, contentBottomY);
        }

        // 3. Right-side Live 3D Preview Panel Controls
        buildPreviewControls(previewX, topTabY, previewPanelW, rowH, rowGap, bottomBarY, bottomBarH);

        // 4. Bottom Action Bar (Responsive action buttons aligned strictly with controlsW)
        int btnGap = 4;
        int actionBtnW = (controlsW - (btnGap * 3)) / 4;
        actionBtnW = Math.clamp(actionBtnW, 40, 95);

        boolean compactLabels = actionBtnW < 60;
        Component resetText = compactLabels ? Component.literal("Reset").withStyle(ChatFormatting.RED)
                                            : Component.literal("Reset Defaults").withStyle(ChatFormatting.RED);
        Component presetText = compactLabels ? Component.literal("💾 Preset").withStyle(ChatFormatting.AQUA)
                                             : Component.literal("💾 Save Preset...").withStyle(ChatFormatting.AQUA);
        Component cancelText = CommonComponents.GUI_CANCEL;
        Component applyText = compactLabels ? Component.literal("Apply").withStyle(ChatFormatting.GREEN)
                                            : Component.literal("Save & Apply").withStyle(ChatFormatting.GREEN);

        addRenderableWidget(Button.builder(
                resetText,
                b -> resetAllToDefaults()
        ).bounds(controlsStartX, bottomBarY, actionBtnW, bottomBarH).build());

        addRenderableWidget(Button.builder(
                presetText,
                b -> {
                    activeMainTab = MainTab.PRESETS;
                    activePresetsSubTab = PresetsSubTab.CUSTOM;
                    presetStatusMessage = "Enter a name for your custom preset and click Save!";
                    presetStatusColor = ChatFormatting.YELLOW;
                    refreshWidgets();
                    if (newPresetNameBox != null) {
                        setFocused(newPresetNameBox);
                    }
                }
        ).bounds(controlsStartX + actionBtnW + btnGap, bottomBarY, actionBtnW, bottomBarH).tooltip(Tooltip.create(Component.literal("Name and save your current configuration\nas a reusable custom preset!"))).build());

        addRenderableWidget(Button.builder(
                cancelText,
                b -> onClose()
        ).bounds(controlsStartX + (actionBtnW + btnGap) * 2, bottomBarY, actionBtnW, bottomBarH).build());

        addRenderableWidget(Button.builder(
                applyText,
                b -> {
                    saveDraftToConfig();
                    onClose();
                }
        ).bounds(controlsStartX + controlsW - actionBtnW, bottomBarY, actionBtnW, bottomBarH).build());
    }

    private Component getTabComponent(String title, boolean active) {
        return getTabComponent(title, active, 100);
    }

    private Component getTabComponent(String title, boolean active, int tabWidth) {
        if (active) {
            String prefix = tabWidth >= 62 ? "▶ " : "";
            return Component.literal(prefix + title).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        }
        return Component.literal(title).withStyle(ChatFormatting.GRAY);
    }

    private void resetAllToDefaults() {
        MagicCirclePreset standard = PresetManager.getStandardPresets().get(0);
        applyPresetToDraft(standard);
        presetStatusMessage = "Reset all settings to default!";
        presetStatusColor = ChatFormatting.YELLOW;
    }

    // ==========================================
    // TAB 1: LAYERS
    // ==========================================
    private void buildLayersTabControls(int x, int curY, int width, int rowH, int rowGap, int labelGap, int contentBottomY) {
        int subBtnWidth = (width - 16) / 5;
        for (int i = 0; i < 5; i++) {
            final int layerIdx = i;
            boolean selected = (layerIdx == selectedLayerIndex);
            Component name = Component.literal("L" + (i + 1) + (selected ? " *" : ""));
            if (selected) {
                name = name.copy().withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD);
            }
            addRenderableWidget(Button.builder(name, b -> {
                selectedLayerIndex = layerIdx;
                refreshWidgets();
            }).bounds(x + i * (subBtnWidth + 4), curY, subBtnWidth, rowH).build());
        }
        curY += rowH + rowGap;

        // Subtitle Role text as a real StringWidget (Zero overlap)
        String[] layerRoles = {
                "Layer 1: Core Geometry (Common Rarity)",
                "Layer 2: Inner Star Frame (Uncommon Rarity)",
                "Layer 3: Octagram Ring (Rare Rarity)",
                "Layer 4: Constellation Ring (Epic Rarity)",
                "Layer 5: Outer Crown (Legendary Rarity)"
        };
        StringWidget roleWidget = new StringWidget(x, curY, width, 10, Component.literal(layerRoles[selectedLayerIndex]).withStyle(ChatFormatting.AQUA), this.font);
        roleWidget.alignLeft();
        addRenderableWidget(roleWidget);
        curY += 10 + labelGap;

        // Scale slider
        addRenderableWidget(new DoubleSlider(
                x, curY, width, rowH,
                0.20, 2.50, draftLayerScales[selectedLayerIndex],
                val -> Component.literal(String.format("Scale: %.2fx", val)),
                val -> draftLayerScales[selectedLayerIndex] = val
        ));
        curY += rowH + rowGap;

        // Speed slider
        addRenderableWidget(new DoubleSlider(
                x, curY, width, rowH,
                -5.00, 5.00, draftLayerSpeeds[selectedLayerIndex],
                val -> {
                    String dir = val > 0.01 ? "CW" : (val < -0.01 ? "CCW" : "Static");
                    return Component.literal(String.format("Speed: %+.2f (%s)", val, dir));
                },
                val -> draftLayerSpeeds[selectedLayerIndex] = val
        ));
        curY += rowH + rowGap;

        // Custom Texture Label as a real StringWidget (ZERO OVERLAP WITH BUTTONS)
        StringWidget texLabelWidget = new StringWidget(x, curY, width, 10, Component.literal("Custom Layer Texture:").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD), this.font);
        texLabelWidget.alignLeft();
        addRenderableWidget(texLabelWidget);
        curY += 10 + labelGap;

        int btn1W = (width - 4) / 3;
        int btn2W = (width - 4) / 3;
        int btn3W = width - btn1W - btn2W - 4;

        Component selectBtnText = width < 220 ? Component.literal("📂 Pick PNG").withStyle(ChatFormatting.GREEN)
                                              : Component.literal("📂 Select File...").withStyle(ChatFormatting.GREEN);
        Component folderBtnText = width < 220 ? Component.literal("📁 Folder").withStyle(ChatFormatting.YELLOW)
                                              : Component.literal("📁 Open Folder").withStyle(ChatFormatting.YELLOW);

        // Button 1: Open OS File Picker
        addRenderableWidget(Button.builder(
                selectBtnText,
                b -> {
                    String imported = CustomTextureManager.openFileDialogAndImport();
                    if (imported != null) {
                        draftLayerTextures[selectedLayerIndex] = imported;
                        if (textureEditBox != null) textureEditBox.setValue(imported);
                        refreshWidgets();
                    }
                }
        ).bounds(x, curY, btn1W, rowH).tooltip(Tooltip.create(Component.literal("Browse and choose any PNG image file\nfrom your computer directly!"))).build());

        // Button 2: Open Textures Folder
        addRenderableWidget(Button.builder(
                folderBtnText,
                b -> CustomTextureManager.openTexturesFolder()
        ).bounds(x + btn1W + 2, curY, btn2W, rowH).tooltip(Tooltip.create(Component.literal("Open .minecraft/config/irons_magic_circle/textures\nto drag & drop custom PNG images!"))).build());

        // Button 3: Revert to Default
        addRenderableWidget(Button.builder(
                Component.literal("Default"),
                b -> {
                    draftLayerTextures[selectedLayerIndex] = "";
                    if (textureEditBox != null) textureEditBox.setValue("");
                    refreshWidgets();
                }
        ).bounds(x + btn1W + btn2W + 4, curY, btn3W, rowH).tooltip(Tooltip.create(Component.literal("Reset this layer to use the school's default texture."))).build());
        curY += rowH + rowGap;

        // Texture Path Box
        textureEditBox = new EditBox(this.font, x, curY, width, rowH, Component.literal("Texture Override"));
        textureEditBox.setMaxLength(256);
        textureEditBox.setValue(draftLayerTextures[selectedLayerIndex]);
        textureEditBox.setResponder(text -> draftLayerTextures[selectedLayerIndex] = text);
        textureEditBox.setHint(Component.literal("Selected file or custom ResourceLocation..."));
        textureEditBox.setTooltip(Tooltip.create(Component.literal("Shows the active file or ResourceLocation path.\nSupports {school} placeholder.")));
        addRenderableWidget(textureEditBox);
        curY += rowH + rowGap;

        // Reset Layer Button
        addRenderableWidget(Button.builder(
                Component.literal("Reset Layer " + (selectedLayerIndex + 1) + " to Defaults"),
                b -> {
                    draftLayerTextures[selectedLayerIndex] = "";
                    draftLayerScales[selectedLayerIndex] = 1.0 + selectedLayerIndex * 0.02;
                    float[] defaultSpeeds = {1.65f, -1.15f, 0.85f, -0.60f, 1.25f};
                    draftLayerSpeeds[selectedLayerIndex] = defaultSpeeds[selectedLayerIndex];
                    refreshWidgets();
                }
        ).bounds(x, curY, width, rowH).build());
    }

    // ==========================================
    // TAB 2: PHASES
    // ==========================================
    private void buildPhasesTabControls(int x, int curY, int width, int rowH, int rowGap, int contentBottomY) {
        int subBtnWidth = (width - 8) / 3;

        addRenderableWidget(Button.builder(
                getTabComponent("1. Emergence", activePhaseSubTab == PhaseSubTab.EMERGENCE, subBtnWidth),
                b -> {
                    activePhaseSubTab = PhaseSubTab.EMERGENCE;
                    refreshWidgets();
                }
        ).bounds(x, curY, subBtnWidth, rowH).build());

        addRenderableWidget(Button.builder(
                getTabComponent("2. Active", activePhaseSubTab == PhaseSubTab.ACTIVE, subBtnWidth),
                b -> {
                    activePhaseSubTab = PhaseSubTab.ACTIVE;
                    refreshWidgets();
                }
        ).bounds(x + subBtnWidth + 4, curY, subBtnWidth, rowH).build());

        addRenderableWidget(Button.builder(
                getTabComponent("3. Fade-Out", activePhaseSubTab == PhaseSubTab.FADE_OUT, subBtnWidth),
                b -> {
                    activePhaseSubTab = PhaseSubTab.FADE_OUT;
                    refreshWidgets();
                }
        ).bounds(x + (subBtnWidth + 4) * 2, curY, subBtnWidth, rowH).build());

        curY += rowH + rowGap + 2;
        switch (activePhaseSubTab) {
            case EMERGENCE -> {
                addRenderableWidget(new DoubleSlider(
                        x, curY, width, rowH,
                        0.20, 2.50, draftEmergenceStartScale,
                        val -> Component.literal(String.format("Start Scale: %.2fx", val)),
                        val -> draftEmergenceStartScale = val
                ));
                curY += rowH + rowGap;
                addRenderableWidget(new DoubleSlider(
                        x, curY, width, rowH,
                        0.00, 4.00, draftEmergenceSpeedMult,
                        val -> Component.literal(String.format("Speed Multiplier: %.2fx", val)),
                        val -> draftEmergenceSpeedMult = val
                ));
                curY += rowH + rowGap;
                addRenderableWidget(new DoubleSlider(
                        x, curY, width, rowH,
                        1.0, 30.0, draftLayerFadeInTicks,
                        val -> Component.literal(String.format("Fade-In Duration: %.1f ticks", val)),
                        val -> draftLayerFadeInTicks = val
                ));
                curY += rowH + rowGap;
                addRenderableWidget(new DoubleSlider(
                        x, curY, width, rowH,
                        0.0, 10.0, draftLayerStaggerTicks,
                        val -> Component.literal(String.format("Stagger Delay: %.1f ticks", val)),
                        val -> draftLayerStaggerTicks = val
                ));
                curY += rowH + rowGap;
                addRenderableWidget(new DoubleSlider(
                        x, curY, width, rowH,
                        0.00, 2.00, draftLayerSlideDistance,
                        val -> Component.literal(String.format("Slide Distance: %.2f blocks", val)),
                        val -> draftLayerSlideDistance = val
                ));
            }
            case ACTIVE -> {
                addRenderableWidget(new DoubleSlider(
                        x, curY, width, rowH,
                        0.20, 2.50, draftActiveScale,
                        val -> Component.literal(String.format("Active Scale: %.2fx", val)),
                        val -> draftActiveScale = val
                ));
                curY += rowH + rowGap;
                addRenderableWidget(new DoubleSlider(
                        x, curY, width, rowH,
                        0.00, 4.00, draftActiveSpeedMult,
                        val -> Component.literal(String.format("Speed Multiplier: %.2fx", val)),
                        val -> draftActiveSpeedMult = val
                ));
                curY += rowH + rowGap;
                addRenderableWidget(new DoubleSlider(
                        x, curY, width, rowH,
                        0.00, 0.40, draftPulseIntensity,
                        val -> Component.literal(String.format("Pulse Breathing: %.2f", val)),
                        val -> draftPulseIntensity = val
                ));
            }
            case FADE_OUT -> {
                addRenderableWidget(new DoubleSlider(
                        x, curY, width, rowH,
                        0.20, 2.50, draftFadeOutEndScale,
                        val -> Component.literal(String.format("Fade End Scale: %.2fx", val)),
                        val -> draftFadeOutEndScale = val
                ));
                curY += rowH + rowGap;
                addRenderableWidget(new DoubleSlider(
                        x, curY, width, rowH,
                        0.00, 4.00, draftFadeOutSpeedMult,
                        val -> Component.literal(String.format("Speed Multiplier: %.2fx", val)),
                        val -> draftFadeOutSpeedMult = val
                ));
                curY += rowH + rowGap;
                addRenderableWidget(new DoubleSlider(
                        x, curY, width, rowH,
                        5.0, 80.0, (double) draftFadeOutTicks,
                        val -> Component.literal(String.format("Fade Duration: %d ticks", val.intValue())),
                        val -> draftFadeOutTicks = val.intValue()
                ));
                curY += rowH + rowGap;
                addRenderableWidget(new DoubleSlider(
                        x, curY, width, rowH,
                        0.00, 1.50, draftFadeOutDriftDistance,
                        val -> Component.literal(String.format("Drift Distance: %.2f blocks", val)),
                        val -> draftFadeOutDriftDistance = val
                ));
            }
        }
    }

    // ==========================================
    // TAB 3: GENERAL & GUN
    // ==========================================
    private void buildGeneralGunTabControls(int x, int curY, int width, int rowH, int rowGap, int contentBottomY) {
        int subBtnWidth = (width - 4) / 2;

        addRenderableWidget(Button.builder(
                getTabComponent("General Settings", activeGeneralSubTab == GeneralSubTab.GENERAL, subBtnWidth),
                b -> {
                    activeGeneralSubTab = GeneralSubTab.GENERAL;
                    refreshWidgets();
                }
        ).bounds(x, curY, subBtnWidth, rowH).build());

        addRenderableWidget(Button.builder(
                getTabComponent("Gun Muzzle", activeGeneralSubTab == GeneralSubTab.GUN_MUZZLE, subBtnWidth),
                b -> {
                    activeGeneralSubTab = GeneralSubTab.GUN_MUZZLE;
                    refreshWidgets();
                }
        ).bounds(x + subBtnWidth + 4, curY, subBtnWidth, rowH).build());

        curY += rowH + rowGap + 2;
        if (activeGeneralSubTab == GeneralSubTab.GENERAL) {
            addRenderableWidget(Button.builder(
                    Component.literal("Magic Circles: " + (draftEnableMagicCircles ? "ON" : "OFF"))
                            .withStyle(draftEnableMagicCircles ? ChatFormatting.GREEN : ChatFormatting.RED),
                    b -> {
                        draftEnableMagicCircles = !draftEnableMagicCircles;
                        refreshWidgets();
                    }
            ).bounds(x, curY, width, rowH).build());

            curY += rowH + rowGap;
            addRenderableWidget(new DoubleSlider(
                    x, curY, width, rowH,
                    0.25, 3.00, draftCircleScale,
                    val -> Component.literal(String.format("Overall Circle Scale: %.2fx", val)),
                    val -> draftCircleScale = val
            ));

            curY += rowH + rowGap;
            addRenderableWidget(new DoubleSlider(
                    x, curY, width, rowH,
                    0.30, 3.00, draftForwardDistance,
                    val -> Component.literal(String.format("Forward Distance: %.2f blocks", val)),
                    val -> draftForwardDistance = val
            ));

            curY += rowH + rowGap;
            addRenderableWidget(new DoubleSlider(
                    x, curY, width, rowH,
                    0.00, 10.00, draftRotationSpeed,
                    val -> Component.literal(String.format("Master Rotation Speed: %.2f", val)),
                    val -> draftRotationSpeed = val
            ));

            curY += rowH + rowGap;
            addRenderableWidget(new DoubleSlider(
                    x, curY, width, rowH,
                    0.05, 1.00, draftCircleOpacity,
                    val -> Component.literal(String.format("Circle Opacity: %.2f", val)),
                    val -> draftCircleOpacity = val
            ));

            curY += rowH + rowGap;
            int halfW = (width - 4) / 2;
            addRenderableWidget(Button.builder(
                    Component.literal("Bloom: " + (draftEnableShaderBloomGlow ? "ON" : "OFF"))
                            .withStyle(draftEnableShaderBloomGlow ? ChatFormatting.AQUA : ChatFormatting.GRAY),
                    b -> {
                        draftEnableShaderBloomGlow = !draftEnableShaderBloomGlow;
                        refreshWidgets();
                    }
            ).bounds(x, curY, halfW, rowH).build());

            addRenderableWidget(new DoubleSlider(
                    x + halfW + 4, curY, width - halfW - 4, rowH,
                    0.05, 2.00, draftGlowIntensity,
                    val -> Component.literal(String.format("Glow: %.2f", val)),
                    val -> draftGlowIntensity = val
            ));

        } else {
            addRenderableWidget(Button.builder(
                    Component.literal("Gun Magic Circles: " + (draftEnableGunMagicCircles ? "ON" : "OFF"))
                            .withStyle(draftEnableGunMagicCircles ? ChatFormatting.GREEN : ChatFormatting.RED),
                    b -> {
                        draftEnableGunMagicCircles = !draftEnableGunMagicCircles;
                        refreshWidgets();
                    }
            ).bounds(x, curY, width, rowH).build());

            curY += rowH + rowGap;
            addRenderableWidget(new DoubleSlider(
                    x, curY, width, rowH,
                    0.10, 2.00, draftGunCircleScale,
                    val -> Component.literal(String.format("Gun Muzzle Scale: %.2fx", val)),
                    val -> draftGunCircleScale = val
            ));

            curY += rowH + rowGap;
            addRenderableWidget(new DoubleSlider(
                    x, curY, width, rowH,
                    1.00, 2.50, draftGunBlastExpansion,
                    val -> Component.literal(String.format("Blastwave Expansion: %.2fx", val)),
                    val -> draftGunBlastExpansion = val
            ));

            curY += rowH + rowGap;
            addRenderableWidget(new DoubleSlider(
                    x, curY, width, rowH,
                    3.0, 40.0, (double) draftGunCircleDurationTicks,
                    val -> Component.literal(String.format("Flash Duration: %d ticks", val.intValue())),
                    val -> draftGunCircleDurationTicks = val.intValue()
            ));

            curY += rowH + rowGap;
            addRenderableWidget(new DoubleSlider(
                    x, curY, width, rowH,
                    -0.50, 1.50, draftGunCircleForwardOffset,
                    val -> Component.literal(String.format("Forward Offset: %+.2f blocks", val)),
                    val -> draftGunCircleForwardOffset = val
            ));

            curY += rowH + rowGap;
            addRenderableWidget(new DoubleSlider(
                    x, curY, width, rowH,
                    0.00, 15.00, draftGunCircleRotationSpeed,
                    val -> Component.literal(String.format("Gun Spin Speed: %.2f", val)),
                    val -> draftGunCircleRotationSpeed = val
            ));
        }
    }

    // ==========================================
    // TAB 4: PRESETS (Custom Preset Maker & Standard Presets)
    // ==========================================
    private void buildPresetsTabControls(int x, int curY, int width, int rowH, int rowGap, int labelGap, int contentBottomY) {
        int subBtnWidth = (width - 4) / 2;

        addRenderableWidget(Button.builder(
                getTabComponent("Custom Presets", activePresetsSubTab == PresetsSubTab.CUSTOM, subBtnWidth),
                b -> {
                    activePresetsSubTab = PresetsSubTab.CUSTOM;
                    refreshWidgets();
                }
        ).bounds(x, curY, subBtnWidth, rowH).build());

        addRenderableWidget(Button.builder(
                getTabComponent("Standard (5)", activePresetsSubTab == PresetsSubTab.STANDARD, subBtnWidth),
                b -> {
                    activePresetsSubTab = PresetsSubTab.STANDARD;
                    refreshWidgets();
                }
        ).bounds(x + subBtnWidth + 4, curY, subBtnWidth, rowH).build());

        curY += rowH + rowGap + 2;

        if (activePresetsSubTab == PresetsSubTab.CUSTOM) {
            // Header 1: Create & Name Custom Preset (StringWidget)
            StringWidget createHeader = new StringWidget(x, curY, width, 10, Component.literal("Create & Name Custom Preset:").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD), this.font);
            createHeader.alignLeft();
            addRenderableWidget(createHeader);
            curY += 10 + labelGap;

            int saveBtnW = width < 220 ? 46 : 58;
            newPresetNameBox = new EditBox(this.font, x, curY, width - saveBtnW - 4, rowH, Component.literal("Preset Name"));
            newPresetNameBox.setMaxLength(32);
            newPresetNameBox.setHint(Component.literal("Enter preset name..."));
            addRenderableWidget(newPresetNameBox);

            addRenderableWidget(Button.builder(
                    Component.literal("💾 Save").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD),
                    b -> {
                        String name = (newPresetNameBox != null) ? newPresetNameBox.getValue().trim() : "";
                        if (name.isEmpty()) {
                            presetStatusMessage = "Please enter a preset name!";
                            presetStatusColor = ChatFormatting.RED;
                            refreshWidgets();
                            return;
                        }
                        MagicCirclePreset custom = createPresetFromDraft(name);
                        boolean ok = PresetManager.savePreset(custom);
                        if (ok) {
                            presetStatusMessage = "✓ Preset '" + name + "' saved!";
                            presetStatusColor = ChatFormatting.GREEN;
                            if (newPresetNameBox != null) newPresetNameBox.setValue("");
                            refreshWidgets();
                        } else {
                            presetStatusMessage = "Failed to save preset.";
                            presetStatusColor = ChatFormatting.RED;
                            refreshWidgets();
                        }
                    }
            ).bounds(x + width - saveBtnW, curY, saveBtnW, rowH).tooltip(Tooltip.create(Component.literal("Save all current custom sliders and textures\nas a reusable named preset!"))).build());

            curY += rowH + rowGap + 2;

            // Header 2: Saved Custom Presets (StringWidget)
            StringWidget savedHeader = new StringWidget(x, curY, width, 10, Component.literal("Saved Custom Presets:").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD), this.font);
            savedHeader.alignLeft();
            addRenderableWidget(savedHeader);
            curY += 10 + labelGap;

            List<MagicCirclePreset> userPresets = PresetManager.loadUserPresets();
            if (userPresets.isEmpty()) {
                StringWidget emptyWidget = new StringWidget(x, curY, width, 10, Component.literal("No custom presets saved yet.").withStyle(ChatFormatting.DARK_GRAY), this.font);
                emptyWidget.alignLeft();
                addRenderableWidget(emptyWidget);
                curY += 10 + rowGap;
            } else {
                int reservedBottom = rowH + rowGap + (presetStatusMessage.isEmpty() ? 0 : 12) + 2;
                int remainingH = contentBottomY - curY - reservedBottom;
                int maxSlots = Math.max(1, remainingH / (rowH + rowGap));

                boolean needsPaging = userPresets.size() > maxSlots;
                int pageSize = needsPaging ? Math.max(1, maxSlots - 1) : userPresets.size();
                int totalPages = Math.max(1, (int) Math.ceil((double) userPresets.size() / pageSize));
                customPresetPage = Mth.clamp(customPresetPage, 0, totalPages - 1);

                int startIdx = customPresetPage * pageSize;
                int endIdx = Math.min(startIdx + pageSize, userPresets.size());

                for (int i = startIdx; i < endIdx; i++) {
                    final MagicCirclePreset up = userPresets.get(i);
                    int updateW = width < 220 ? 44 : 52;
                    int delW = 18;
                    int loadW = width - updateW - delW - 4;

                    addRenderableWidget(Button.builder(
                            Component.literal("▶ " + up.displayName).withStyle(ChatFormatting.YELLOW),
                            b -> applyPresetToDraft(up)
                    ).bounds(x, curY, loadW, rowH).tooltip(Tooltip.create(Component.literal("Click to load and preview this preset"))).build());

                    addRenderableWidget(Button.builder(
                            Component.literal("✎"),
                            b -> {
                                MagicCirclePreset updated = createPresetFromDraft(up.displayName);
                                updated.id = up.id;
                                PresetManager.savePreset(updated);
                                presetStatusMessage = "Updated preset '" + up.displayName + "'!";
                                presetStatusColor = ChatFormatting.AQUA;
                                refreshWidgets();
                            }
                    ).bounds(x + loadW + 2, curY, updateW, rowH).tooltip(Tooltip.create(Component.literal("Overwrite this preset with your current settings"))).build());

                    addRenderableWidget(Button.builder(
                            Component.literal("✕").withStyle(ChatFormatting.RED),
                            b -> {
                                PresetManager.deletePreset(up.id);
                                presetStatusMessage = "Deleted preset: " + up.displayName;
                                presetStatusColor = ChatFormatting.YELLOW;
                                refreshWidgets();
                            }
                    ).bounds(x + loadW + updateW + 4, curY, delW, rowH).tooltip(Tooltip.create(Component.literal("Delete this preset file"))).build());

                    curY += rowH + rowGap;
                }

                if (needsPaging) {
                    int pageBtnW = (width - 50) / 2;
                    addRenderableWidget(Button.builder(
                            Component.literal("◀ Prev"),
                            b -> {
                                customPresetPage = Math.max(0, customPresetPage - 1);
                                refreshWidgets();
                            }
                    ).bounds(x, curY, pageBtnW, rowH).build());

                    addRenderableWidget(Button.builder(
                            Component.literal((customPresetPage + 1) + "/" + totalPages),
                            b -> {}
                    ).bounds(x + pageBtnW + 2, curY, 46, rowH).build());

                    addRenderableWidget(Button.builder(
                            Component.literal("Next ▶"),
                            b -> {
                                customPresetPage = Math.min(totalPages - 1, customPresetPage + 1);
                                refreshWidgets();
                            }
                    ).bounds(x + width - pageBtnW, curY, pageBtnW, rowH).build());

                    curY += rowH + rowGap;
                }
            }

            // Open Folder Button
            addRenderableWidget(Button.builder(
                    Component.literal("📁 Open Presets Folder...").withStyle(ChatFormatting.GRAY),
                    b -> PresetManager.openPresetsFolder()
            ).bounds(x, curY, width, rowH).tooltip(Tooltip.create(Component.literal("Opens .minecraft/config/irons_magic_circle/presets\nin Windows Explorer to copy or share preset files."))).build());
            curY += rowH + rowGap;

            if (!presetStatusMessage.isEmpty()) {
                StringWidget statusWidget = new StringWidget(x, curY, width, 10, Component.literal(presetStatusMessage).withStyle(presetStatusColor), this.font);
                statusWidget.alignLeft();
                addRenderableWidget(statusWidget);
            }

        } else {
            // Header: Built-In Standard Presets (StringWidget)
            StringWidget stdHeader = new StringWidget(x, curY, width, 10, Component.literal("Built-In Standard Presets:").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD), this.font);
            stdHeader.alignLeft();
            addRenderableWidget(stdHeader);
            curY += 10 + labelGap;

            List<MagicCirclePreset> standards = PresetManager.getStandardPresets();
            int reservedBottom = rowH + rowGap + (presetStatusMessage.isEmpty() ? 0 : 12) + 2;
            int remainingH = contentBottomY - curY - reservedBottom;
            int maxSlots = Math.max(1, remainingH / (rowH + rowGap));

            boolean needsPaging = standards.size() > maxSlots;
            int pageSize = needsPaging ? Math.max(1, maxSlots - 1) : standards.size();
            int totalPages = Math.max(1, (int) Math.ceil((double) standards.size() / pageSize));
            standardPresetPage = Mth.clamp(standardPresetPage, 0, totalPages - 1);

            int startIdx = standardPresetPage * pageSize;
            int endIdx = Math.min(startIdx + pageSize, standards.size());

            for (int i = startIdx; i < endIdx; i++) {
                final MagicCirclePreset preset = standards.get(i);
                int custW = width < 220 ? 52 : 68;
                int loadW = width - custW - 4;

                addRenderableWidget(Button.builder(
                        Component.literal("★ " + preset.displayName).withStyle(ChatFormatting.GOLD),
                        b -> applyPresetToDraft(preset)
                ).bounds(x, curY, loadW, rowH).tooltip(Tooltip.create(Component.literal(preset.description))).build());

                addRenderableWidget(Button.builder(
                        Component.literal("Customize"),
                        b -> {
                            applyPresetToDraft(preset);
                            activePresetsSubTab = PresetsSubTab.CUSTOM;
                            presetStatusMessage = "Loaded '" + preset.displayName + "'. Name and save as custom preset!";
                            presetStatusColor = ChatFormatting.AQUA;
                            refreshWidgets();
                        }
                ).bounds(x + loadW + 4, curY, custW, rowH).tooltip(Tooltip.create(Component.literal("Load this preset into custom maker to tweak and rename it!"))).build());

                curY += rowH + rowGap;
            }

            if (needsPaging) {
                int pageBtnW = (width - 50) / 2;
                addRenderableWidget(Button.builder(
                        Component.literal("◀ Prev"),
                        b -> {
                            standardPresetPage = Math.max(0, standardPresetPage - 1);
                            refreshWidgets();
                        }
                ).bounds(x, curY, pageBtnW, rowH).build());

                addRenderableWidget(Button.builder(
                        Component.literal((standardPresetPage + 1) + "/" + totalPages),
                        b -> {}
                ).bounds(x + pageBtnW + 2, curY, 46, rowH).build());

                addRenderableWidget(Button.builder(
                        Component.literal("Next ▶"),
                        b -> {
                            standardPresetPage = Math.min(totalPages - 1, standardPresetPage + 1);
                            refreshWidgets();
                        }
                ).bounds(x + width - pageBtnW, curY, pageBtnW, rowH).build());

                curY += rowH + rowGap;
            }

            // Open Folder Button
            addRenderableWidget(Button.builder(
                    Component.literal("📁 Open Presets Folder...").withStyle(ChatFormatting.GRAY),
                    b -> PresetManager.openPresetsFolder()
            ).bounds(x, curY, width, rowH).build());
            curY += rowH + rowGap;

            if (!presetStatusMessage.isEmpty()) {
                StringWidget statusWidget = new StringWidget(x, curY, width, 10, Component.literal(presetStatusMessage).withStyle(presetStatusColor), this.font);
                statusWidget.alignLeft();
                addRenderableWidget(statusWidget);
            }
        }
    }

    // ==========================================
    // 3D PREVIEW PANEL (With Dynamic School Detection & Clamped Scaling)
    // ==========================================
    private void buildPreviewControls(int x, int startY, int width, int rowH, int rowGap, int bottomY, int bottomH) {
        if (availableSchools == null || availableSchools.isEmpty()) {
            availableSchools = SchoolVisualHelper.getAvailableSchools();
        }

        ResourceLocation schoolId = availableSchools.get(previewSchoolIndex % availableSchools.size());
        SchoolType schoolType = SchoolRegistry.getSchool(schoolId);
        Component schoolName = (schoolType != null && schoolType.getDisplayName() != null)
                ? schoolType.getDisplayName()
                : Component.literal(schoolId.getPath().toUpperCase());

        int schoolBtnY = startY;
        int layersBtnY = schoolBtnY + rowH + rowGap;
        int simulateBtnY = bottomY;

        previewBoxX = x;
        previewBoxY = layersBtnY + rowH + Math.max(2, rowGap);
        previewBoxW = width;
        previewBoxH = simulateBtnY - previewBoxY - Math.max(2, rowGap);

        // Dynamic School Switcher button
        addRenderableWidget(Button.builder(
                Component.literal("School: ").append(schoolName),
                b -> {
                    previewSchoolIndex = (previewSchoolIndex + 1) % availableSchools.size();
                    refreshWidgets();
                }
        ).bounds(x, schoolBtnY, width, rowH).tooltip(Tooltip.create(Component.literal("Cycles through all registered schools\n(including installed Addon schools)."))).build());

        // Layer count switcher
        addRenderableWidget(Button.builder(
                Component.literal("Layers: " + previewLayerCount + " / 5"),
                b -> {
                    previewLayerCount = (previewLayerCount % 5) + 1;
                    refreshWidgets();
                }
        ).bounds(x, layersBtnY, width, rowH).build());

        // Test Cast Animation button - perfectly aligned with the bottom action bar
        addRenderableWidget(Button.builder(
                Component.literal(simulateCastActive ? "Re-Cast" : "Simulate Cast"),
                b -> {
                    simulateCastActive = true;
                    simulateCastStartTime = (float) (System.currentTimeMillis() % 1000000L) / 50.0f;
                }
        ).bounds(x, simulateBtnY, width, bottomH).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int marginX = Math.max(6, Math.min(14, (int) (this.width * 0.025)));
        int titleY = Math.max(3, Math.min(8, (int) (this.height * 0.02)));

        // Screen Header Title
        guiGraphics.drawString(
                this.font,
                Component.literal("Iron's Spell Magic Circle Configuration").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                marginX, titleY, 0xFFFFFFFF, true
        );

        // Draw Preview Frame Background & Live 3D Magic Circle
        if (previewBoxH > 20 && previewBoxW > 20) {
            guiGraphics.fill(previewBoxX, previewBoxY, previewBoxX + previewBoxW, previewBoxY + previewBoxH, 0xDD0C1017);
            guiGraphics.renderOutline(previewBoxX, previewBoxY, previewBoxW, previewBoxH, 0xFF4A6B8A);
            renderMagicCirclePreview(guiGraphics, previewBoxX, previewBoxY, previewBoxW, previewBoxH);
        }
    }

    private void renderMagicCirclePreview(GuiGraphics guiGraphics, int boxX, int boxY, int boxWidth, int boxHeight) {
        guiGraphics.enableScissor(boxX, boxY, boxX + boxWidth, boxY + boxHeight);
        try {
            float centerX = boxX + boxWidth * 0.5f;
            float centerY = boxY + boxHeight * 0.5f;

            float currentTime = (float) (System.currentTimeMillis() % 1000000L) / 50.0f;

            if (availableSchools == null || availableSchools.isEmpty()) {
                availableSchools = SchoolVisualHelper.getAvailableSchools();
            }

            ResourceLocation currentSchoolId = availableSchools.get(previewSchoolIndex % availableSchools.size());
            SchoolType currentSchoolType = SchoolRegistry.getSchool(currentSchoolId);
            SchoolVisualHelper.SchoolVisual visual = SchoolVisualHelper.getSchoolVisual(currentSchoolId, currentSchoolType);

            // Calculate phase simulation progress
            float simAge = simulateCastActive ? Math.max(0.0f, currentTime - simulateCastStartTime) : 999.0f;
            float totalCastDuration = (float) (draftLayerStaggerTicks * (previewLayerCount - 1) + draftLayerFadeInTicks + 20.0f);
            float totalFadeDuration = totalCastDuration + (float) draftFadeOutTicks;

            boolean isSimulatingEmergence = simulateCastActive && (simAge < totalCastDuration);
            boolean isSimulatingFade = simulateCastActive && (simAge >= totalCastDuration) && (simAge < totalFadeDuration);
            if (simulateCastActive && simAge >= totalFadeDuration) {
                simulateCastActive = false;
            }

            float maxSafeRadius = Math.min(boxWidth, boxHeight) * 0.40f;
            float basePreviewRadius = Math.min(maxSafeRadius, Math.min(boxWidth, boxHeight) * 0.32f * (float) draftCircleScale);
            float pulse = (float) (1.0 + draftPulseIntensity * Mth.sin(currentTime * 0.22f));

            PoseStack poseStack = guiGraphics.pose();

            for (int i = 0; i < previewLayerCount; i++) {
                float layerAge = isSimulatingEmergence ? (simAge - (float) (i * draftLayerStaggerTicks)) : 50.0f;
                if (layerAge < 0.0f) continue;

                float spawnProgress = Mth.clamp(layerAge / (float) Math.max(0.5, draftLayerFadeInTicks), 0.0f, 1.0f);
                float inv = 1.0f - spawnProgress;
                float easedProgress = 1.0f - (inv * inv * inv);

                float phaseScale;
                float phaseSpeedMult;
                float fadeFactor = 1.0f;

                if (isSimulatingEmergence) {
                    if (spawnProgress < 1.0f) {
                        phaseScale = (float) Mth.lerp(easedProgress, draftEmergenceStartScale, draftActiveScale);
                        phaseSpeedMult = (float) Mth.lerp(easedProgress, draftEmergenceSpeedMult, draftActiveSpeedMult);
                    } else {
                        phaseScale = (float) draftActiveScale * pulse;
                        phaseSpeedMult = (float) draftActiveSpeedMult;
                    }
                } else if (isSimulatingFade) {
                    float fadeAge = simAge - totalCastDuration;
                    float t = Mth.clamp(fadeAge / (float) draftFadeOutTicks, 0.0f, 1.0f);
                    fadeFactor = (1.0f - t) * (1.0f - t * 0.5f);
                    phaseScale = (float) Mth.lerp(1.0f - fadeFactor, draftActiveScale, draftFadeOutEndScale);
                    phaseSpeedMult = (float) Mth.lerp(1.0f - fadeFactor, draftActiveSpeedMult, draftFadeOutSpeedMult);
                } else {
                    phaseScale = (float) draftActiveScale * pulse;
                    phaseSpeedMult = (float) draftActiveSpeedMult;
                }

                float layerRadius = basePreviewRadius * (float) draftLayerScales[i] * phaseScale;
                layerRadius = Math.min(layerRadius, Math.min(boxWidth, boxHeight) * 0.45f);

                float speed = (float) (draftRotationSpeed * draftLayerSpeeds[i] * phaseSpeedMult);
                float rotAngle = (currentTime * speed * 1.5f) % 360.0f;

                int alpha = Mth.clamp((int) (draftCircleOpacity * 255.0 * easedProgress * fadeFactor), 0, 255);
                if (alpha <= 2) continue;

                ResourceLocation textureLoc = resolveLayerTexture(currentSchoolId, currentSchoolType, i);

                poseStack.pushPose();
                poseStack.translate(centerX, centerY, 150.0f + (i * 0.05f));
                poseStack.mulPose(Axis.XP.rotationDegrees(20.0f));
                poseStack.mulPose(Axis.YP.rotationDegrees(-12.0f));
                poseStack.mulPose(Axis.ZP.rotationDegrees(rotAngle));

                Matrix4f matrix = poseStack.last().pose();

                // PASS 1: Base Translucent Emissive Pass with native or dynamically tinted school colors
                VertexConsumer consumer = guiGraphics.bufferSource().getBuffer(RenderType.entityTranslucentEmissive(textureLoc));
                drawPreviewQuad(consumer, matrix, layerRadius, visual.red, visual.green, visual.blue, alpha);

                // PASS 2: Additive Bloom Glow Pass
                if (draftEnableShaderBloomGlow) {
                    float effectiveGlow = (float) draftGlowIntensity * pulse * easedProgress * fadeFactor;
                    int glowAlpha = Mth.clamp((int) (255.0f * Mth.clamp(effectiveGlow, 0.0f, 1.0f)), 0, 255);
                    if (glowAlpha > 2) {
                        int glowR = Mth.clamp((int) (visual.red * effectiveGlow), 0, 255);
                        int glowG = Mth.clamp((int) (visual.green * effectiveGlow), 0, 255);
                        int glowB = Mth.clamp((int) (visual.blue * effectiveGlow), 0, 255);
                        VertexConsumer glowConsumer = guiGraphics.bufferSource().getBuffer(RenderHelper.CustomerRenderType.magicNoCull(textureLoc));
                        drawPreviewQuad(glowConsumer, matrix, layerRadius, glowR, glowG, glowB, glowAlpha);
                    }
                }

                poseStack.popPose();
            }

            guiGraphics.bufferSource().endBatch();
        } finally {
            guiGraphics.disableScissor();
        }
    }

    private ResourceLocation resolveLayerTexture(ResourceLocation schoolId, SchoolType schoolType, int layerIndex) {
        String custom = draftLayerTextures[layerIndex];
        if (custom != null && !custom.trim().isEmpty()) {
            String resolved = custom.replace("{school}", schoolId.getPath());
            ResourceLocation loc = CustomTextureManager.resolveCustomTexture(resolved);
            if (loc != null) {
                return loc;
            }
        }
        return SchoolVisualHelper.getSchoolVisual(schoolId, schoolType).layers[layerIndex];
    }

    private void drawPreviewQuad(
            VertexConsumer consumer,
            Matrix4f matrix,
            float r,
            int red,
            int green,
            int blue,
            int alpha
    ) {
        int light = LightTexture.FULL_BRIGHT;
        int overlay = OverlayTexture.NO_OVERLAY;

        // Front Face
        consumer.addVertex(matrix, -r, -r, 0.0f).setColor(red, green, blue, alpha).setUv(0.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0.0f, 0.0f, 1.0f);
        consumer.addVertex(matrix, r, -r, 0.0f).setColor(red, green, blue, alpha).setUv(1.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0.0f, 0.0f, 1.0f);
        consumer.addVertex(matrix, r, r, 0.0f).setColor(red, green, blue, alpha).setUv(1.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0.0f, 0.0f, 1.0f);
        consumer.addVertex(matrix, -r, r, 0.0f).setColor(red, green, blue, alpha).setUv(0.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0.0f, 0.0f, 1.0f);

        // Back Face
        consumer.addVertex(matrix, -r, r, 0.0f).setColor(red, green, blue, alpha).setUv(0.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0.0f, 0.0f, -1.0f);
        consumer.addVertex(matrix, r, r, 0.0f).setColor(red, green, blue, alpha).setUv(1.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0.0f, 0.0f, -1.0f);
        consumer.addVertex(matrix, r, -r, 0.0f).setColor(red, green, blue, alpha).setUv(1.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0.0f, 0.0f, -1.0f);
        consumer.addVertex(matrix, -r, -r, 0.0f).setColor(red, green, blue, alpha).setUv(0.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0.0f, 0.0f, -1.0f);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parentScreen);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
