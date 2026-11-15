package com.ironsmagiccircle.config.preset;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ironsmagiccircle.IronsMagicCircleMod;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages loading, saving, and cataloging of Magic Circle configuration presets.
 */
public class PresetManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path presetsDir = null;

    public static Path getPresetsDirectory() {
        if (presetsDir == null) {
            presetsDir = Minecraft.getInstance().gameDirectory.toPath()
                    .resolve("config")
                    .resolve(IronsMagicCircleMod.MODID)
                    .resolve("presets");
        }
        try {
            if (!Files.exists(presetsDir)) {
                Files.createDirectories(presetsDir);
            }
        } catch (Exception e) {
            IronsMagicCircleMod.LOGGER.error("Failed to create presets directory: " + presetsDir, e);
        }
        return presetsDir;
    }

    /**
     * Returns the 5 standard built-in presets.
     */
    public static List<MagicCirclePreset> getStandardPresets() {
        List<MagicCirclePreset> list = new ArrayList<>();

        // 1. Standard Balanced Arcane
        MagicCirclePreset standard = new MagicCirclePreset(
                "standard_arcane",
                "Standard Arcane",
                "Default balanced rotation speeds and organic staggered emergence.",
                true
        );
        standard.layerScales = new double[]{1.00, 1.02, 1.04, 1.06, 1.08};
        standard.layerSpeeds = new double[]{1.65, -1.15, 0.85, -0.60, 1.25};
        standard.emergenceStartScale = 1.12;
        standard.emergenceSpeedMultiplier = 1.0;
        standard.layerFadeInTicks = 7.0;
        standard.layerStaggerTicks = 2.5;
        standard.layerSlideDistance = 0.65;
        standard.activeScale = 1.0;
        standard.activeSpeedMultiplier = 1.0;
        standard.pulseIntensity = 0.08;
        standard.fadeOutEndScale = 1.05;
        standard.fadeOutSpeedMultiplier = 0.75;
        standard.fadeOutTicks = 32;
        standard.fadeOutDriftDistance = 0.15;
        standard.circleScale = 1.0;
        standard.forwardDistance = 1.15;
        standard.rotationSpeed = 2.8;
        standard.circleOpacity = 0.48;
        standard.enableShaderBloomGlow = true;
        standard.glowIntensity = 0.42;
        standard.gunCircleScale = 0.35;
        standard.gunBlastExpansion = 1.12;
        standard.gunCircleDurationTicks = 12;
        list.add(standard);

        // 2. High-Speed Turbine / Overdrive
        MagicCirclePreset turbine = new MagicCirclePreset(
                "turbine_overdrive",
                "Turbine Overdrive",
                "High-speed rapid alternating spinning layers with snappy explosive emergence.",
                true
        );
        turbine.layerScales = new double[]{0.95, 1.02, 1.08, 1.15, 1.22};
        turbine.layerSpeeds = new double[]{4.20, -3.20, 2.50, -1.90, 3.50};
        turbine.emergenceStartScale = 0.50;
        turbine.emergenceSpeedMultiplier = 2.5;
        turbine.layerFadeInTicks = 4.0;
        turbine.layerStaggerTicks = 1.2;
        turbine.layerSlideDistance = 0.35;
        turbine.activeScale = 1.05;
        turbine.activeSpeedMultiplier = 1.5;
        turbine.pulseIntensity = 0.18;
        turbine.fadeOutEndScale = 1.25;
        turbine.fadeOutSpeedMultiplier = 2.0;
        turbine.fadeOutTicks = 20;
        turbine.fadeOutDriftDistance = 0.25;
        turbine.circleScale = 1.0;
        turbine.forwardDistance = 1.20;
        turbine.rotationSpeed = 3.6;
        turbine.circleOpacity = 0.62;
        turbine.enableShaderBloomGlow = true;
        turbine.glowIntensity = 0.65;
        turbine.gunCircleScale = 0.42;
        turbine.gunBlastExpansion = 1.30;
        turbine.gunCircleDurationTicks = 15;
        turbine.gunCircleRotationSpeed = 6.5;
        list.add(turbine);

        // 3. Slow Mystical / Eldritch Ritual
        MagicCirclePreset ritual = new MagicCirclePreset(
                "slow_ritual",
                "Eldritch Ritual",
                "Slow, majestic ceremonial rotation with grand scale and extended dissolving drift.",
                true
        );
        ritual.layerScales = new double[]{1.05, 1.10, 1.16, 1.22, 1.28};
        ritual.layerSpeeds = new double[]{0.60, -0.42, 0.32, -0.22, 0.48};
        ritual.emergenceStartScale = 1.35;
        ritual.emergenceSpeedMultiplier = 0.6;
        ritual.layerFadeInTicks = 14.0;
        ritual.layerStaggerTicks = 3.5;
        ritual.layerSlideDistance = 1.10;
        ritual.activeScale = 1.10;
        ritual.activeSpeedMultiplier = 0.7;
        ritual.pulseIntensity = 0.05;
        ritual.fadeOutEndScale = 0.90;
        ritual.fadeOutSpeedMultiplier = 0.4;
        ritual.fadeOutTicks = 48;
        ritual.fadeOutDriftDistance = 0.35;
        ritual.circleScale = 1.15;
        ritual.forwardDistance = 1.35;
        ritual.rotationSpeed = 2.0;
        ritual.circleOpacity = 0.42;
        ritual.enableShaderBloomGlow = true;
        ritual.glowIntensity = 0.55;
        ritual.gunCircleScale = 0.32;
        ritual.gunBlastExpansion = 1.05;
        ritual.gunCircleDurationTicks = 18;
        list.add(ritual);

        // 4. Compact Tactical / Fast-Cast
        MagicCirclePreset tactical = new MagicCirclePreset(
                "compact_tactical",
                "Compact Tactical",
                "Scaled down and subtle with low opacity. Ideal for clear vision in PvP and fast dungeons.",
                true
        );
        tactical.layerScales = new double[]{0.90, 0.93, 0.96, 0.99, 1.02};
        tactical.layerSpeeds = new double[]{2.00, -1.50, 1.10, -0.80, 1.60};
        tactical.emergenceStartScale = 0.90;
        tactical.emergenceSpeedMultiplier = 1.2;
        tactical.layerFadeInTicks = 3.0;
        tactical.layerStaggerTicks = 0.8;
        tactical.layerSlideDistance = 0.15;
        tactical.activeScale = 0.95;
        tactical.activeSpeedMultiplier = 1.0;
        tactical.pulseIntensity = 0.02;
        tactical.fadeOutEndScale = 1.00;
        tactical.fadeOutSpeedMultiplier = 0.8;
        tactical.fadeOutTicks = 16;
        tactical.fadeOutDriftDistance = 0.05;
        tactical.circleScale = 0.72;
        tactical.forwardDistance = 0.90;
        tactical.rotationSpeed = 2.5;
        tactical.circleOpacity = 0.30;
        tactical.enableShaderBloomGlow = true;
        tactical.glowIntensity = 0.25;
        tactical.gunCircleScale = 0.25;
        tactical.gunBlastExpansion = 1.05;
        tactical.gunCircleDurationTicks = 8;
        list.add(tactical);

        // 5. Supernova Burst / Grand Ritual
        MagicCirclePreset supernova = new MagicCirclePreset(
                "supernova_burst",
                "Supernova Burst",
                "Massive radiant magic circle bursting outward from a core point with intense bloom.",
                true
        );
        supernova.layerScales = new double[]{1.00, 1.08, 1.18, 1.28, 1.40};
        supernova.layerSpeeds = new double[]{2.80, -2.10, 1.60, -1.20, 2.40};
        supernova.emergenceStartScale = 0.20;
        supernova.emergenceSpeedMultiplier = 3.0;
        supernova.layerFadeInTicks = 8.0;
        supernova.layerStaggerTicks = 2.0;
        supernova.layerSlideDistance = 0.85;
        supernova.activeScale = 1.15;
        supernova.activeSpeedMultiplier = 1.2;
        supernova.pulseIntensity = 0.22;
        supernova.fadeOutEndScale = 1.50;
        supernova.fadeOutSpeedMultiplier = 1.5;
        supernova.fadeOutTicks = 40;
        supernova.fadeOutDriftDistance = 0.60;
        supernova.circleScale = 1.35;
        supernova.forwardDistance = 1.40;
        supernova.rotationSpeed = 3.2;
        supernova.circleOpacity = 0.75;
        supernova.enableShaderBloomGlow = true;
        supernova.glowIntensity = 0.95;
        supernova.gunCircleScale = 0.48;
        supernova.gunBlastExpansion = 1.60;
        supernova.gunCircleDurationTicks = 20;
        supernova.gunCircleRotationSpeed = 6.0;
        list.add(supernova);

        return list;
    }

    /**
     * Loads all user-saved presets from disk (.json files).
     */
    public static List<MagicCirclePreset> loadUserPresets() {
        Path dir = getPresetsDirectory();
        File folder = dir.toFile();
        if (!folder.exists() || !folder.isDirectory()) {
            return Collections.emptyList();
        }

        File[] files = folder.listFiles((d, name) -> name.toLowerCase().endsWith(".json"));
        if (files == null || files.length == 0) {
            return Collections.emptyList();
        }

        List<MagicCirclePreset> result = new ArrayList<>();
        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                MagicCirclePreset preset = GSON.fromJson(reader, MagicCirclePreset.class);
                if (preset != null) {
                    if (preset.id == null || preset.id.isEmpty()) {
                        preset.id = file.getName().replace(".json", "");
                    }
                    if (preset.displayName == null || preset.displayName.isEmpty()) {
                        preset.displayName = preset.id;
                    }
                    preset.isBuiltin = false;
                    result.add(preset);
                }
            } catch (Exception e) {
                IronsMagicCircleMod.LOGGER.error("Failed to load preset file: " + file.getName(), e);
            }
        }
        return result;
    }

    /**
     * Saves a preset to disk as a JSON file.
     */
    public static boolean savePreset(MagicCirclePreset preset) {
        if (preset == null || preset.displayName == null || preset.displayName.trim().isEmpty()) {
            return false;
        }

        String safeFileName = preset.displayName.trim()
                .replaceAll("[^a-zA-Z0-9_\\- ]", "")
                .replace(" ", "_")
                .toLowerCase();

        if (safeFileName.isEmpty()) {
            safeFileName = "preset_" + System.currentTimeMillis();
        }

        preset.id = safeFileName;
        preset.isBuiltin = false;

        Path filePath = getPresetsDirectory().resolve(safeFileName + ".json");
        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            GSON.toJson(preset, writer);
            IronsMagicCircleMod.LOGGER.info("Successfully saved preset: " + filePath);
            return true;
        } catch (Exception e) {
            IronsMagicCircleMod.LOGGER.error("Failed to save preset to file: " + filePath, e);
            return false;
        }
    }

    /**
     * Deletes a user preset from disk.
     */
    public static boolean deletePreset(String presetId) {
        if (presetId == null || presetId.isEmpty()) return false;
        Path filePath = getPresetsDirectory().resolve(presetId + ".json");
        try {
            return Files.deleteIfExists(filePath);
        } catch (Exception e) {
            IronsMagicCircleMod.LOGGER.error("Failed to delete preset: " + filePath, e);
            return false;
        }
    }

    /**
     * Opens the presets folder in the OS file explorer.
     */
    public static void openPresetsFolder() {
        Path dir = getPresetsDirectory();
        Util.getPlatform().openFile(dir.toFile());
    }
}

