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
            .comment("Base scale multiplier for rendered magic circles.")
            .defineInRange("circleScale", 1.0, 0.25, 4.0);

    public static final ModConfigSpec.DoubleValue ROTATION_SPEED = BUILDER
            .comment("Rotation speed multiplier for magic circle rings (degrees per tick).")
            .defineInRange("rotationSpeed", 2.5, 0.0, 20.0);

    public static final ModConfigSpec.DoubleValue CIRCLE_OPACITY = BUILDER
            .comment("Opacity (alpha) of rendered magic circles.")
            .defineInRange("circleOpacity", 0.85, 0.1, 1.0);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
