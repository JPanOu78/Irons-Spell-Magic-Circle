package com.ironsmagiccircle.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Client-only configuration for Iron's Spell Magic Circle.
 */
public class MagicCircleClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_MAGIC_CIRCLES = BUILDER
            .comment("Enable rendering of arcane magic circles during spellcasting.")
            .define("enableMagicCircles", true);

    public static final ModConfigSpec.DoubleValue CIRCLE_SCALE = BUILDER
            .comment("Base scale multiplier for rendered magic circles (multiplied by Player Scale).")
            .defineInRange("circleScale", 1.0, 0.25, 4.0);

    public static final ModConfigSpec.DoubleValue FORWARD_DISTANCE = BUILDER
            .comment("Target distance in blocks in front of the caster where layers converge (multiplied by Player Scale).")
            .defineInRange("forwardDistance", 1.15, 0.3, 5.0);

    public static final ModConfigSpec.DoubleValue ROTATION_SPEED = BUILDER
            .comment("Base rotation speed multiplier for magic circle layers (degrees per tick).")
            .defineInRange("rotationSpeed", 2.8, 0.0, 20.0);

    public static final ModConfigSpec.DoubleValue CIRCLE_OPACITY = BUILDER
            .comment("Maximum opacity (alpha) of rendered magic circles (lower = easier to see through).")
            .defineInRange("circleOpacity", 0.48, 0.05, 1.0);

    public static final ModConfigSpec.BooleanValue ENABLE_SHADER_BLOOM_GLOW = BUILDER
            .comment("Enable additive HDR emissive bloom pass so magic circles glow with Iris/Shader packs.")
            .define("enableShaderBloomGlow", true);

    public static final ModConfigSpec.DoubleValue GLOW_INTENSITY = BUILDER
            .comment("Intensity of the additive shader glow pass (0.05 to 2.0).")
            .defineInRange("glowIntensity", 0.42, 0.05, 2.0);

    public static final ModConfigSpec.IntValue FADE_OUT_TICKS = BUILDER
            .comment("Duration in ticks (20 ticks = 1 second) for the magic circle to slowly fade out after casting ends.")
            .defineInRange("fadeOutTicks", 32, 5, 100);

    public static final ModConfigSpec.DoubleValue LAYER_STAGGER_TICKS = BUILDER
            .comment("Delay in ticks between each stacked layer (1..5) starting its fade-in and slide-in animation.")
            .defineInRange("layerStaggerTicks", 2.5, 0.0, 15.0);

    public static final ModConfigSpec.DoubleValue LAYER_FADE_IN_TICKS = BUILDER
            .comment("Duration in ticks for each layer to fade in and slide inward from further in front of the caster.")
            .defineInRange("layerFadeInTicks", 7.0, 1.0, 40.0);

    public static final ModConfigSpec.DoubleValue LAYER_SLIDE_DISTANCE = BUILDER
            .comment("Extra distance in blocks further away from the caster that each layer starts at before sliding into place.")
            .defineInRange("layerSlideDistance", 0.65, 0.0, 3.0);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
