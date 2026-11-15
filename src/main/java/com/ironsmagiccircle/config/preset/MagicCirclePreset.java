package com.ironsmagiccircle.config.preset;

import com.ironsmagiccircle.config.MagicCircleClientConfig;

/**
 * Data container representing a complete Magic Circle configuration preset.
 * Can be serialized to/from JSON for user sharing and custom loadouts.
 */
public class MagicCirclePreset {
    public String id;
    public String displayName;
    public String description;
    public boolean isBuiltin;

    // Per-Layer (1..5)
    public double[] layerScales = new double[]{1.00, 1.02, 1.04, 1.06, 1.08};
    public double[] layerSpeeds = new double[]{1.65, -1.15, 0.85, -0.60, 1.25};
    public String[] layerTextures = new String[]{"", "", "", "", ""};

    // Phase 1: Emergence / Cast-In
    public double emergenceStartScale = 1.12;
    public double emergenceSpeedMultiplier = 1.0;
    public double layerFadeInTicks = 7.0;
    public double layerStaggerTicks = 2.5;
    public double layerSlideDistance = 0.65;

    // Phase 2: Active / Sustained Casting
    public double activeScale = 1.0;
    public double activeSpeedMultiplier = 1.0;
    public double pulseIntensity = 0.08;

    // Phase 3: Fade-Out / Dissolution
    public double fadeOutEndScale = 1.05;
    public double fadeOutSpeedMultiplier = 0.75;
    public int fadeOutTicks = 32;
    public double fadeOutDriftDistance = 0.15;

    // General
    public boolean enableMagicCircles = true;
    public double circleScale = 1.0;
    public double forwardDistance = 1.15;
    public double rotationSpeed = 2.8;
    public double circleOpacity = 0.48;
    public boolean enableShaderBloomGlow = true;
    public double glowIntensity = 0.42;

    // Gun Muzzle
    public boolean enableGunMagicCircles = true;
    public double gunCircleScale = 0.35;
    public double gunBlastExpansion = 1.12;
    public int gunCircleDurationTicks = 12;
    public double gunCircleForwardOffset = 0.0;
    public double gunCircleRotationSpeed = 4.5;
    public double gunCircleOpacity = 0.65;

    public MagicCirclePreset() {
    }

    public MagicCirclePreset(String id, String displayName, String description, boolean isBuiltin) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.isBuiltin = isBuiltin;
    }

    public static MagicCirclePreset fromCurrentConfig() {
        MagicCirclePreset p = new MagicCirclePreset("custom", "Custom Preset", "Current in-game configuration", false);
        for (int i = 0; i < 5; i++) {
            p.layerScales[i] = MagicCircleClientConfig.getLayerScale(i);
            p.layerSpeeds[i] = MagicCircleClientConfig.getLayerSpeed(i);
            p.layerTextures[i] = MagicCircleClientConfig.getCustomLayerTexture(i);
        }

        p.emergenceStartScale = MagicCircleClientConfig.EMERGENCE_START_SCALE.get();
        p.emergenceSpeedMultiplier = MagicCircleClientConfig.EMERGENCE_SPEED_MULTIPLIER.get();
        p.layerFadeInTicks = MagicCircleClientConfig.LAYER_FADE_IN_TICKS.get();
        p.layerStaggerTicks = MagicCircleClientConfig.LAYER_STAGGER_TICKS.get();
        p.layerSlideDistance = MagicCircleClientConfig.LAYER_SLIDE_DISTANCE.get();

        p.activeScale = MagicCircleClientConfig.ACTIVE_SCALE.get();
        p.activeSpeedMultiplier = MagicCircleClientConfig.ACTIVE_SPEED_MULTIPLIER.get();
        p.pulseIntensity = MagicCircleClientConfig.PULSE_INTENSITY.get();

        p.fadeOutEndScale = MagicCircleClientConfig.FADE_OUT_END_SCALE.get();
        p.fadeOutSpeedMultiplier = MagicCircleClientConfig.FADE_OUT_SPEED_MULTIPLIER.get();
        p.fadeOutTicks = MagicCircleClientConfig.FADE_OUT_TICKS.get();
        p.fadeOutDriftDistance = MagicCircleClientConfig.FADE_OUT_DRIFT_DISTANCE.get();

        p.enableMagicCircles = MagicCircleClientConfig.ENABLE_MAGIC_CIRCLES.get();
        p.circleScale = MagicCircleClientConfig.CIRCLE_SCALE.get();
        p.forwardDistance = MagicCircleClientConfig.FORWARD_DISTANCE.get();
        p.rotationSpeed = MagicCircleClientConfig.ROTATION_SPEED.get();
        p.circleOpacity = MagicCircleClientConfig.CIRCLE_OPACITY.get();
        p.enableShaderBloomGlow = MagicCircleClientConfig.ENABLE_SHADER_BLOOM_GLOW.get();
        p.glowIntensity = MagicCircleClientConfig.GLOW_INTENSITY.get();

        p.enableGunMagicCircles = MagicCircleClientConfig.ENABLE_GUN_MAGIC_CIRCLES.get();
        p.gunCircleScale = MagicCircleClientConfig.GUN_CIRCLE_SCALE.get();
        p.gunBlastExpansion = MagicCircleClientConfig.GUN_BLAST_EXPANSION.get();
        p.gunCircleDurationTicks = MagicCircleClientConfig.GUN_CIRCLE_DURATION_TICKS.get();
        p.gunCircleForwardOffset = MagicCircleClientConfig.GUN_CIRCLE_FORWARD_OFFSET.get();
        p.gunCircleRotationSpeed = MagicCircleClientConfig.GUN_CIRCLE_ROTATION_SPEED.get();
        p.gunCircleOpacity = MagicCircleClientConfig.GUN_CIRCLE_OPACITY.get();

        return p;
    }

    public void applyToConfig() {
        MagicCircleClientConfig.LAYER_1_TEXTURE.set(layerTextures[0]);
        MagicCircleClientConfig.LAYER_2_TEXTURE.set(layerTextures[1]);
        MagicCircleClientConfig.LAYER_3_TEXTURE.set(layerTextures[2]);
        MagicCircleClientConfig.LAYER_4_TEXTURE.set(layerTextures[3]);
        MagicCircleClientConfig.LAYER_5_TEXTURE.set(layerTextures[4]);

        MagicCircleClientConfig.LAYER_1_SCALE.set(layerScales[0]);
        MagicCircleClientConfig.LAYER_2_SCALE.set(layerScales[1]);
        MagicCircleClientConfig.LAYER_3_SCALE.set(layerScales[2]);
        MagicCircleClientConfig.LAYER_4_SCALE.set(layerScales[3]);
        MagicCircleClientConfig.LAYER_5_SCALE.set(layerScales[4]);

        MagicCircleClientConfig.LAYER_1_SPEED.set(layerSpeeds[0]);
        MagicCircleClientConfig.LAYER_2_SPEED.set(layerSpeeds[1]);
        MagicCircleClientConfig.LAYER_3_SPEED.set(layerSpeeds[2]);
        MagicCircleClientConfig.LAYER_4_SPEED.set(layerSpeeds[3]);
        MagicCircleClientConfig.LAYER_5_SPEED.set(layerSpeeds[4]);

        MagicCircleClientConfig.EMERGENCE_START_SCALE.set(emergenceStartScale);
        MagicCircleClientConfig.EMERGENCE_SPEED_MULTIPLIER.set(emergenceSpeedMultiplier);
        MagicCircleClientConfig.LAYER_FADE_IN_TICKS.set(layerFadeInTicks);
        MagicCircleClientConfig.LAYER_STAGGER_TICKS.set(layerStaggerTicks);
        MagicCircleClientConfig.LAYER_SLIDE_DISTANCE.set(layerSlideDistance);

        MagicCircleClientConfig.ACTIVE_SCALE.set(activeScale);
        MagicCircleClientConfig.ACTIVE_SPEED_MULTIPLIER.set(activeSpeedMultiplier);
        MagicCircleClientConfig.PULSE_INTENSITY.set(pulseIntensity);

        MagicCircleClientConfig.FADE_OUT_END_SCALE.set(fadeOutEndScale);
        MagicCircleClientConfig.FADE_OUT_SPEED_MULTIPLIER.set(fadeOutSpeedMultiplier);
        MagicCircleClientConfig.FADE_OUT_TICKS.set(fadeOutTicks);
        MagicCircleClientConfig.FADE_OUT_DRIFT_DISTANCE.set(fadeOutDriftDistance);

        MagicCircleClientConfig.ENABLE_MAGIC_CIRCLES.set(enableMagicCircles);
        MagicCircleClientConfig.CIRCLE_SCALE.set(circleScale);
        MagicCircleClientConfig.FORWARD_DISTANCE.set(forwardDistance);
        MagicCircleClientConfig.ROTATION_SPEED.set(rotationSpeed);
        MagicCircleClientConfig.CIRCLE_OPACITY.set(circleOpacity);
        MagicCircleClientConfig.ENABLE_SHADER_BLOOM_GLOW.set(enableShaderBloomGlow);
        MagicCircleClientConfig.GLOW_INTENSITY.set(glowIntensity);

        MagicCircleClientConfig.ENABLE_GUN_MAGIC_CIRCLES.set(enableGunMagicCircles);
        MagicCircleClientConfig.GUN_CIRCLE_SCALE.set(gunCircleScale);
        MagicCircleClientConfig.GUN_BLAST_EXPANSION.set(gunBlastExpansion);
        MagicCircleClientConfig.GUN_CIRCLE_DURATION_TICKS.set(gunCircleDurationTicks);
        MagicCircleClientConfig.GUN_CIRCLE_FORWARD_OFFSET.set(gunCircleForwardOffset);
        MagicCircleClientConfig.GUN_CIRCLE_ROTATION_SPEED.set(gunCircleRotationSpeed);
        MagicCircleClientConfig.GUN_CIRCLE_OPACITY.set(gunCircleOpacity);

        MagicCircleClientConfig.saveConfig();
    }
}

