package com.ironsmagiccircle.client;

import com.ironsmagiccircle.IronsMagicCircleMod;
import com.ironsmagiccircle.config.MagicCircleClientConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.render.RenderHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Client-side 3D world render handler for arcane magic circles.
 *
 * Features:
 * 1. See-through, non-obstructive opacity + clean open-center geometric layers.
 * 2. Persistent Casting Session Tracker:
 *    - Keeps circles alive for 1.5–2 seconds (configurable fadeOutTicks) after casting ends,
 *      so even Instant spells smoothly animate in and fade out instead of blinking.
 * 3. Staggered Layer Emergence & Slide-In Convergence:
 *    - Layers 1..N do not appear all at once; each layer i starts with a slight stagger delay,
 *      fading in while sliding inward from further away in front of the caster until converging
 *      at the target casting plane.
 * 4. Alternating Layer Rotation & Dynamic Player Scale (player.getScale()).
 * 5. Dual-Pass Shader HDR Emissive Bloom.
 */
@EventBusSubscriber(modid = IronsMagicCircleMod.MODID, value = Dist.CLIENT)
public class MagicCircleRenderHandler {

    private static final int MAX_LAYERS = 5;

    /**
     * Supported Iron's Spells schools with dedicated 5-layer texture sets.
     */
    private static final Set<String> SUPPORTED_SCHOOLS = Set.of(
            "fire", "ice", "lightning", "holy", "ender",
            "blood", "evocation", "nature", "eldritch"
    );

    private static final String DEFAULT_SCHOOL = "evocation";

    /**
     * Alternating rotation speed multipliers per layer (Layer 1..5).
     */
    private static final float[] LAYER_ROTATION_MULTIPLIERS = {
            1.65f,  // Layer 1 (Common): Core geometry - fast clockwise
            -1.15f, // Layer 2 (Uncommon): Inner star frame - medium counter-clockwise
            0.85f,  // Layer 3 (Rare): Octagram ring - steady clockwise
            -0.60f, // Layer 4 (Epic): Constellation ring - slow counter-clockwise
            1.25f   // Layer 5 (Legendary): Outer crown - brisk clockwise
    };

    /**
     * Slight radial scale expansion per stacked layer.
     */
    private static final float[] LAYER_SCALE_FACTORS = {
            1.00f,  // Layer 1
            1.02f,  // Layer 2
            1.04f,  // Layer 3
            1.06f,  // Layer 4
            1.08f   // Layer 5
    };

    /**
     * Cached ResourceLocations for textures/magic_circle/<school>/layer_<1..5>.png
     */
    private static final Map<String, ResourceLocation[]> TEXTURE_CACHE = new HashMap<>();

    /**
     * Active/fading magic circle sessions per player UUID.
     * Allows instant spells and finished casts to smoothly animate and fade out over 1–2 seconds.
     */
    private static final Map<UUID, MagicCircleSession> ACTIVE_SESSIONS = new HashMap<>();

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        if (!MagicCircleClientConfig.ENABLE_MAGIC_CIRCLES.get()) {
            ACTIVE_SESSIONS.clear();
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) {
            ACTIVE_SESSIONS.clear();
            return;
        }

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        long gameTime = level.getGameTime();
        float currentTime = (float) gameTime + partialTick;

        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        int fadeOutTicks = MagicCircleClientConfig.FADE_OUT_TICKS.get();
        float staggerTicks = MagicCircleClientConfig.LAYER_STAGGER_TICKS.get().floatValue();
        float fadeInTicks = MagicCircleClientConfig.LAYER_FADE_IN_TICKS.get().floatValue();

        // 1. Update sessions from currently casting players in the level
        for (AbstractClientPlayer player : level.players()) {
            if (player.isSpectator() || !player.isAlive()) {
                ACTIVE_SESSIONS.remove(player.getUUID());
                continue;
            }

            UUID uuid = player.getUUID();
            CastingState liveState = resolvePlayerCastingState(player, player == mc.player);

            if (liveState != null) {
                MagicCircleSession session = ACTIVE_SESSIONS.get(uuid);
                boolean isNewCast = (session == null)
                        || !session.isCurrentlyCasting
                        || !session.spellId.equals(liveState.spellId);

                if (isNewCast) {
                    session = new MagicCircleSession(
                            liveState.spellId,
                            liveState.schoolName,
                            liveState.layerCount,
                            currentTime
                    );
                    ACTIVE_SESSIONS.put(uuid, session);
                } else {
                    session.schoolName = liveState.schoolName;
                    session.layerCount = liveState.layerCount;
                }
                session.isCurrentlyCasting = true;
                session.lastActiveTime = currentTime;
            } else {
                MagicCircleSession session = ACTIVE_SESSIONS.get(uuid);
                if (session != null && session.isCurrentlyCasting) {
                    session.isCurrentlyCasting = false;
                    // Ensure all layers have enough time to finish their staggered slide-in
                    // before the fade-out completes, especially for Instant / short casts!
                    float minFullDisplayTime = session.castStartTime
                            + ((session.layerCount - 1) * staggerTicks)
                            + fadeInTicks
                            + 4.0f;
                    session.castEndTime = Math.max(currentTime, minFullDisplayTime);
                }
            }
        }

        // 2. Render all active and fading-out sessions
        Iterator<Map.Entry<UUID, MagicCircleSession>> it = ACTIVE_SESSIONS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, MagicCircleSession> entry = it.next();
            UUID uuid = entry.getKey();
            MagicCircleSession session = entry.getValue();

            Player player = level.getPlayerByUUID(uuid);
            if (player == null || !player.isAlive() || player.isSpectator()) {
                it.remove();
                continue;
            }

            // Compute post-cast fade-out factor (1.0 while casting, smoothly eases to 0.0 over fadeOutTicks)
            float fadeOutFactor = 1.0f;
            if (!session.isCurrentlyCasting) {
                float elapsedAfterEnd = currentTime - session.castEndTime;
                if (elapsedAfterEnd >= fadeOutTicks) {
                    it.remove();
                    continue;
                }
                if (elapsedAfterEnd > 0.0f) {
                    float t = Mth.clamp(elapsedAfterEnd / (float) fadeOutTicks, 0.0f, 1.0f);
                    // Smooth cosine/cubic ease-out so it lingers gently then dissolves
                    fadeOutFactor = (1.0f - t) * (1.0f - t * 0.5f);
                }
            }

            renderPlayerMagicCircle(
                    player,
                    session,
                    currentTime,
                    fadeOutFactor,
                    partialTick,
                    camPos,
                    poseStack,
                    bufferSource
            );
        }

        bufferSource.endBatch();
    }

    /**
     * Resolves the active spellcasting state for either the local client player or a remote player.
     */
    private static CastingState resolvePlayerCastingState(Player player, boolean isLocalPlayer) {
        boolean isCasting;
        String spellId;
        int spellLevel;

        if (isLocalPlayer) {
            isCasting = ClientMagicData.isCasting();
            if (!isCasting) {
                return null;
            }
            spellId = ClientMagicData.getCastingSpellId();
            spellLevel = ClientMagicData.getCastingSpellLevel();
        } else {
            SyncedSpellData syncedData = ClientMagicData.getSyncedSpellData(player);
            if (syncedData == null || !syncedData.isCasting()) {
                return null;
            }
            spellId = syncedData.getCastingSpellId();
            spellLevel = syncedData.getCastingSpellLevel();
        }

        if (spellId == null || spellId.isEmpty() || "none".equals(spellId)) {
            return null;
        }

        AbstractSpell spell = SpellRegistry.getSpell(spellId);
        if (spell == null || spell == SpellRegistry.none()) {
            return null;
        }

        // Determine school name for texture folder lookup
        String schoolName = DEFAULT_SCHOOL;
        SchoolType schoolType = spell.getSchoolType();
        if (schoolType != null && schoolType.getId() != null) {
            String path = schoolType.getId().getPath();
            if (SUPPORTED_SCHOOLS.contains(path)) {
                schoolName = path;
            }
        }

        // Determine number of stacked layers (1..5) from SpellRarity (COMMON=0 .. LEGENDARY=4)
        int safeLevel = Math.max(1, spellLevel);
        SpellRarity rarity = spell.getRarity(safeLevel);
        int rarityIndex = (rarity != null) ? rarity.getValue() : 0;
        int layerCount = Mth.clamp(rarityIndex + 1, 1, MAX_LAYERS);

        return new CastingState(spellId, schoolName, layerCount);
    }

    /**
     * Renders the stacked 1..5 layer magic circle in front of the casting player,
     * with staggered per-layer fade-in, inward slide convergence, post-cast fade-out,
     * and dynamic player scale calculation.
     */
    private static void renderPlayerMagicCircle(
            Player player,
            MagicCircleSession session,
            float currentTime,
            float fadeOutFactor,
            float partialTick,
            Vec3 camPos,
            PoseStack poseStack,
            MultiBufferSource bufferSource
    ) {
        // 1. Player Scale Calculation (Minecraft 1.21.1 Entity.getScale() reads Attributes.SCALE)
        float playerScale = Math.max(0.05f, player.getScale());
        float configScale = MagicCircleClientConfig.CIRCLE_SCALE.get().floatValue();
        float configForwardDist = MagicCircleClientConfig.FORWARD_DISTANCE.get().floatValue();
        float baseRotationSpeed = MagicCircleClientConfig.ROTATION_SPEED.get().floatValue();
        float maxOpacity = MagicCircleClientConfig.CIRCLE_OPACITY.get().floatValue();
        boolean enableBloomGlow = MagicCircleClientConfig.ENABLE_SHADER_BLOOM_GLOW.get();
        float glowIntensity = MagicCircleClientConfig.GLOW_INTENSITY.get().floatValue();

        float staggerTicks = MagicCircleClientConfig.LAYER_STAGGER_TICKS.get().floatValue();
        float fadeInTicks = Math.max(0.5f, MagicCircleClientConfig.LAYER_FADE_IN_TICKS.get().floatValue());
        float slideDistance = MagicCircleClientConfig.LAYER_SLIDE_DISTANCE.get().floatValue() * playerScale;

        // 2. Interpolated Caster Position & View Direction
        double interpX = Mth.lerp(partialTick, player.xo, player.getX());
        double interpY = Mth.lerp(partialTick, player.yo, player.getY());
        double interpZ = Mth.lerp(partialTick, player.zo, player.getZ());

        // Position origin slightly below eye level so the open center aligns comfortably with the view
        double originY = interpY + player.getEyeHeight() - (0.18D * playerScale);

        Vec3 lookVec = player.getViewVector(partialTick);
        double forwardDist = configForwardDist * playerScale;

        double circleCenterX = interpX + lookVec.x * forwardDist;
        double circleCenterY = originY + lookVec.y * forwardDist;
        double circleCenterZ = interpZ + lookVec.z * forwardDist;

        // 3. Base Radius calculated from Player Scale + slight growth for higher-rarity circles
        float raritySizeBonus = 1.0f + (session.layerCount - 1) * 0.07f;
        float baseRadius = 0.70f * playerScale * configScale * raritySizeBonus;

        // 4. Interpolated View Angles to align the circle plane perpendicular to the caster's aim
        float yaw = player.getViewYRot(partialTick);
        float pitch = player.getViewXRot(partialTick);

        // Elapsed ticks since this cast session started
        float sessionAge = Math.max(0.0f, currentTime - session.castStartTime);
        float pulse = 0.92f + 0.08f * Mth.sin(currentTime * 0.22f);

        ResourceLocation[] schoolLayers = getSchoolLayerTextures(session.schoolName);

        // 5. Render each stacked layer (layer_1 .. layer_N) with staggered fade-in & inward slide
        for (int i = 0; i < session.layerCount; i++) {
            // Each layer i starts appearing after (i * staggerTicks)
            float layerAge = sessionAge - (i * staggerTicks);
            if (layerAge <= 0.0f) {
                continue; // This layer hasn't started materializing yet
            }

            // Progress from 0.0 (just spawned further away) to 1.0 (fully converged at target plane)
            float spawnProgress = Mth.clamp(layerAge / fadeInTicks, 0.0f, 1.0f);
            // Smooth cubic ease-out: 1 - (1 - t)^3
            float inv = 1.0f - spawnProgress;
            float easedProgress = 1.0f - (inv * inv * inv);

            // Combined opacity for this layer (staggered fade-in * post-cast fade-out * config opacity)
            float layerOpacity = maxOpacity * easedProgress * fadeOutFactor;
            int baseAlpha = Mth.clamp((int) (layerOpacity * 255.0f), 0, 255);
            if (baseAlpha <= 2) {
                continue;
            }

            ResourceLocation layerTexture = schoolLayers[i];
            // Slight scale-in from 1.12x down to 1.0x as the layer slides inward and locks into place
            float slideScale = 1.0f + (1.0f - easedProgress) * 0.12f;
            float layerRadius = baseRadius * LAYER_SCALE_FACTORS[i] * slideScale;

            // Slide-in along lookVec:
            // Starts further away from the caster (+layerExtraStartDist along lookVec)
            // and slides inward toward the caster until converging at the target plane (0).
            float layerExtraStartDist = slideDistance * (1.0f + i * 0.20f);
            float currentSlideDist = (1.0f - easedProgress) * layerExtraStartDist;

            // Tiny converged depth offset so stacked layers never Z-fight once locked together
            float convergedDepthOffset = (i - (session.layerCount - 1) * 0.5f) * 0.005f * playerScale;
            // Subtle outward drift during the final fade-out
            float fadeOutDriftDist = (1.0f - fadeOutFactor) * 0.15f * playerScale;

            double totalForwardShift = convergedDepthOffset + currentSlideDist + fadeOutDriftDist;
            double layerWorldX = circleCenterX + lookVec.x * totalForwardShift;
            double layerWorldY = circleCenterY + lookVec.y * totalForwardShift;
            double layerWorldZ = circleCenterZ + lookVec.z * totalForwardShift;

            // Continuous rotation angle for this layer
            float rotationAngleDeg = 0.0f;
            if (baseRotationSpeed > 0.0f) {
                rotationAngleDeg = (layerAge * baseRotationSpeed * LAYER_ROTATION_MULTIPLIERS[i]) % 360.0f;
            }

            poseStack.pushPose();
            poseStack.translate(
                    layerWorldX - camPos.x,
                    layerWorldY - camPos.y,
                    layerWorldZ - camPos.z
            );
            poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
            if (rotationAngleDeg != 0.0f) {
                poseStack.mulPose(Axis.ZP.rotationDegrees(rotationAngleDeg));
            }

            Matrix4f poseMatrix = poseStack.last().pose();

            // PASS 1: Base Translucent Emissive Pass (See-through, crisp color)
            VertexConsumer baseConsumer = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(layerTexture));
            drawDoubleSidedQuad(baseConsumer, poseMatrix, layerRadius, 255, 255, 255, baseAlpha);

            // PASS 2: Shader HDR Additive Bloom Pass (scaled by easedProgress and fadeOutFactor)
            if (enableBloomGlow) {
                float effectiveGlow = glowIntensity * pulse * easedProgress * fadeOutFactor;
                int glowChannel = Mth.clamp((int) (255.0f * Mth.clamp(effectiveGlow, 0.0f, 1.0f)), 0, 255);
                if (glowChannel > 2) {
                    VertexConsumer glowConsumer = bufferSource.getBuffer(RenderHelper.CustomerRenderType.magicNoCull(layerTexture));
                    drawDoubleSidedQuad(glowConsumer, poseMatrix, layerRadius, glowChannel, glowChannel, glowChannel, glowChannel);
                }
            }

            poseStack.popPose();
        }
    }

    /**
     * Draws a double-sided square quad on the local XY plane centered at (0, 0, 0) with half-size {@code r}.
     */
    private static void drawDoubleSidedQuad(
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

        // Front face (+Z normal)
        consumer.addVertex(matrix, -r, -r, 0.0f)
                .setColor(red, green, blue, alpha)
                .setUv(0.0f, 1.0f)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(0.0f, 0.0f, 1.0f);
        consumer.addVertex(matrix, r, -r, 0.0f)
                .setColor(red, green, blue, alpha)
                .setUv(1.0f, 1.0f)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(0.0f, 0.0f, 1.0f);
        consumer.addVertex(matrix, r, r, 0.0f)
                .setColor(red, green, blue, alpha)
                .setUv(1.0f, 0.0f)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(0.0f, 0.0f, 1.0f);
        consumer.addVertex(matrix, -r, r, 0.0f)
                .setColor(red, green, blue, alpha)
                .setUv(0.0f, 0.0f)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(0.0f, 0.0f, 1.0f);

        // Back face (-Z normal, reversed winding order)
        consumer.addVertex(matrix, -r, r, 0.0f)
                .setColor(red, green, blue, alpha)
                .setUv(0.0f, 0.0f)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(0.0f, 0.0f, -1.0f);
        consumer.addVertex(matrix, r, r, 0.0f)
                .setColor(red, green, blue, alpha)
                .setUv(1.0f, 0.0f)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(0.0f, 0.0f, -1.0f);
        consumer.addVertex(matrix, r, -r, 0.0f)
                .setColor(red, green, blue, alpha)
                .setUv(1.0f, 1.0f)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(0.0f, 0.0f, -1.0f);
        consumer.addVertex(matrix, -r, -r, 0.0f)
                .setColor(red, green, blue, alpha)
                .setUv(0.0f, 1.0f)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(0.0f, 0.0f, -1.0f);
    }

    /**
     * Returns the cached array of 5 layer ResourceLocations for the given school.
     */
    private static ResourceLocation[] getSchoolLayerTextures(String school) {
        return TEXTURE_CACHE.computeIfAbsent(school, key -> {
            ResourceLocation[] layers = new ResourceLocation[MAX_LAYERS];
            for (int i = 0; i < MAX_LAYERS; i++) {
                layers[i] = ResourceLocation.fromNamespaceAndPath(
                        IronsMagicCircleMod.MODID,
                        "textures/magic_circle/" + key + "/layer_" + (i + 1) + ".png"
                );
            }
            return layers;
        });
    }

    private record CastingState(String spellId, String schoolName, int layerCount) {
    }

    /**
     * Tracks an active or fading-out magic circle animation for a specific player.
     */
    private static class MagicCircleSession {
        final String spellId;
        String schoolName;
        int layerCount;
        final float castStartTime;
        float lastActiveTime;
        float castEndTime;
        boolean isCurrentlyCasting;

        MagicCircleSession(String spellId, String schoolName, int layerCount, float castStartTime) {
            this.spellId = spellId;
            this.schoolName = schoolName;
            this.layerCount = layerCount;
            this.castStartTime = castStartTime;
            this.lastActiveTime = castStartTime;
            this.castEndTime = castStartTime;
            this.isCurrentlyCasting = true;
        }
    }
}
