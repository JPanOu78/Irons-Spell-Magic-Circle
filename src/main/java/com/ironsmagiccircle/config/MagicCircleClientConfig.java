package com.ironsmagiccircle.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Client-only configuration for Iron's Spell Magic Circle.
 * Fully customizable textures, per-layer scaling/speed, and per-phase animation dynamics.
 */
public class MagicCircleClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    // ==========================================
    // 1. General Settings
    // ==========================================
    public static final ModConfigSpec.BooleanValue ENABLE_MAGIC_CIRCLES;
    public static final ModConfigSpec.DoubleValue CIRCLE_SCALE;
    public static final ModConfigSpec.DoubleValue FORWARD_DISTANCE;
    public static final ModConfigSpec.DoubleValue ROTATION_SPEED;
    public static final ModConfigSpec.DoubleValue CIRCLE_OPACITY;
    public static final ModConfigSpec.BooleanValue ENABLE_SHADER_BLOOM_GLOW;
    public static final ModConfigSpec.DoubleValue GLOW_INTENSITY;

    // ==========================================
    // 2. Custom Layer Textures (Layers 1..5)
    // ==========================================
    public static final ModConfigSpec.ConfigValue<String> LAYER_1_TEXTURE;
    public static final ModConfigSpec.ConfigValue<String> LAYER_2_TEXTURE;
    public static final ModConfigSpec.ConfigValue<String> LAYER_3_TEXTURE;
    public static final ModConfigSpec.ConfigValue<String> LAYER_4_TEXTURE;
    public static final ModConfigSpec.ConfigValue<String> LAYER_5_TEXTURE;

    // ==========================================
    // 3. Per-Layer Base Scale Factors (Layers 1..5)
    // ==========================================
    public static final ModConfigSpec.DoubleValue LAYER_1_SCALE;
    public static final ModConfigSpec.DoubleValue LAYER_2_SCALE;
    public static final ModConfigSpec.DoubleValue LAYER_3_SCALE;
    public static final ModConfigSpec.DoubleValue LAYER_4_SCALE;
    public static final ModConfigSpec.DoubleValue LAYER_5_SCALE;

    // ==========================================
    // 4. Per-Layer Rotation Speed Multipliers (Layers 1..5, +/- for CW/CCW)
    // ==========================================
    public static final ModConfigSpec.DoubleValue LAYER_1_SPEED;
    public static final ModConfigSpec.DoubleValue LAYER_2_SPEED;
    public static final ModConfigSpec.DoubleValue LAYER_3_SPEED;
    public static final ModConfigSpec.DoubleValue LAYER_4_SPEED;
    public static final ModConfigSpec.DoubleValue LAYER_5_SPEED;

    // ==========================================
    // 5. Display Phases & Stages (Emergence, Active, Fade-Out)
    // ==========================================
    // Phase 1: Emergence / Cast-In (ขั้นตอนเริ่มปรากฏ / สไลด์เข้า)
    public static final ModConfigSpec.DoubleValue EMERGENCE_START_SCALE;
    public static final ModConfigSpec.DoubleValue EMERGENCE_SPEED_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue LAYER_FADE_IN_TICKS;
    public static final ModConfigSpec.DoubleValue LAYER_STAGGER_TICKS;
    public static final ModConfigSpec.DoubleValue LAYER_SLIDE_DISTANCE;

    // Phase 2: Active / Sustained Casting (ขั้นตอนร่ายปกติ)
    public static final ModConfigSpec.DoubleValue ACTIVE_SCALE;
    public static final ModConfigSpec.DoubleValue ACTIVE_SPEED_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue PULSE_INTENSITY;

    // Phase 3: Fade-Out / Dissolution (ขั้นตอนสลายตัวหลังร่ายจบ)
    public static final ModConfigSpec.DoubleValue FADE_OUT_END_SCALE;
    public static final ModConfigSpec.DoubleValue FADE_OUT_SPEED_MULTIPLIER;
    public static final ModConfigSpec.IntValue FADE_OUT_TICKS;
    public static final ModConfigSpec.DoubleValue FADE_OUT_DRIFT_DISTANCE;

    // ==========================================
    // 6. Gun Muzzle Phase (Iron's Arms Spell Bullet)
    // ==========================================
    public static final ModConfigSpec.BooleanValue ENABLE_GUN_MAGIC_CIRCLES;
    public static final ModConfigSpec.DoubleValue GUN_CIRCLE_SCALE;
    public static final ModConfigSpec.DoubleValue GUN_BLAST_EXPANSION;
    public static final ModConfigSpec.IntValue GUN_CIRCLE_DURATION_TICKS;
    public static final ModConfigSpec.DoubleValue GUN_CIRCLE_FORWARD_OFFSET;
    public static final ModConfigSpec.DoubleValue GUN_CIRCLE_ROTATION_SPEED;
    public static final ModConfigSpec.DoubleValue GUN_CIRCLE_OPACITY;

    static {
        BUILDER.comment("General Magic Circle Settings").push("general");

        ENABLE_MAGIC_CIRCLES = BUILDER
                .comment("Enable rendering of arcane magic circles during spellcasting.")
                .define("enableMagicCircles", true);

        CIRCLE_SCALE = BUILDER
                .comment("Base scale multiplier for rendered magic circles (multiplied by Player Scale).")
                .defineInRange("circleScale", 1.0, 0.25, 4.0);

        FORWARD_DISTANCE = BUILDER
                .comment("Target distance in blocks in front of the caster where layers converge (multiplied by Player Scale).")
                .defineInRange("forwardDistance", 1.15, 0.3, 5.0);

        ROTATION_SPEED = BUILDER
                .comment("Base rotation speed multiplier for magic circle layers (degrees per tick).")
                .defineInRange("rotationSpeed", 2.8, 0.0, 20.0);

        CIRCLE_OPACITY = BUILDER
                .comment("Maximum opacity (alpha) of rendered magic circles (lower = easier to see through).")
                .defineInRange("circleOpacity", 0.48, 0.05, 1.0);

        ENABLE_SHADER_BLOOM_GLOW = BUILDER
                .comment("Enable additive HDR emissive bloom pass so magic circles glow with Iris/Shader packs.")
                .define("enableShaderBloomGlow", true);

        GLOW_INTENSITY = BUILDER
                .comment("Intensity of the additive shader glow pass (0.05 to 2.0).")
                .defineInRange("glowIntensity", 0.42, 0.05, 2.0);

        BUILDER.pop();

        // ------------------------------------------
        // Layer Textures
        // ------------------------------------------
        BUILDER.comment("Custom Texture Overrides for each of the 5 Magic Circle Layers.\n" +
                "Leave empty (\"\") to use the default school texture.\n" +
                "Supports ResourceLocations (e.g. \"irons_magic_circle:textures/magic_circle/fire/layer_1.png\").\n" +
                "Can use {school} placeholder to dynamically adapt to the spell's school (e.g. \"my_pack:textures/{school}/ring.png\")."
        ).push("layer_textures");

        LAYER_1_TEXTURE = BUILDER
                .comment("Custom texture path for Layer 1 (Core Geometry / Common ring).")
                .define("layer1Texture", "");

        LAYER_2_TEXTURE = BUILDER
                .comment("Custom texture path for Layer 2 (Inner Star Frame / Uncommon ring).")
                .define("layer2Texture", "");

        LAYER_3_TEXTURE = BUILDER
                .comment("Custom texture path for Layer 3 (Octagram Ring / Rare ring).")
                .define("layer3Texture", "");

        LAYER_4_TEXTURE = BUILDER
                .comment("Custom texture path for Layer 4 (Constellation Ring / Epic ring).")
                .define("layer4Texture", "");

        LAYER_5_TEXTURE = BUILDER
                .comment("Custom texture path for Layer 5 (Outer Crown / Legendary ring).")
                .define("layer5Texture", "");

        BUILDER.pop();

        // ------------------------------------------
        // Per-Layer Scale & Speed
        // ------------------------------------------
        BUILDER.comment("Per-Layer Scaling & Rotation Speeds (Layers 1..5).\n" +
                "Speeds can be positive (Clockwise) or negative (Counter-Clockwise)."
        ).push("layer_settings");

        LAYER_1_SCALE = BUILDER.comment("Scale factor for Layer 1").defineInRange("layer1Scale", 1.00, 0.1, 3.0);
        LAYER_2_SCALE = BUILDER.comment("Scale factor for Layer 2").defineInRange("layer2Scale", 1.02, 0.1, 3.0);
        LAYER_3_SCALE = BUILDER.comment("Scale factor for Layer 3").defineInRange("layer3Scale", 1.04, 0.1, 3.0);
        LAYER_4_SCALE = BUILDER.comment("Scale factor for Layer 4").defineInRange("layer4Scale", 1.06, 0.1, 3.0);
        LAYER_5_SCALE = BUILDER.comment("Scale factor for Layer 5").defineInRange("layer5Scale", 1.08, 0.1, 3.0);

        LAYER_1_SPEED = BUILDER.comment("Rotation speed for Layer 1 (Clockwise)").defineInRange("layer1Speed", 1.65, -10.0, 10.0);
        LAYER_2_SPEED = BUILDER.comment("Rotation speed for Layer 2 (Counter-Clockwise)").defineInRange("layer2Speed", -1.15, -10.0, 10.0);
        LAYER_3_SPEED = BUILDER.comment("Rotation speed for Layer 3 (Clockwise)").defineInRange("layer3Speed", 0.85, -10.0, 10.0);
        LAYER_4_SPEED = BUILDER.comment("Rotation speed for Layer 4 (Counter-Clockwise)").defineInRange("layer4Speed", -0.60, -10.0, 10.0);
        LAYER_5_SPEED = BUILDER.comment("Rotation speed for Layer 5 (Clockwise)").defineInRange("layer5Speed", 1.25, -10.0, 10.0);

        BUILDER.pop();

        // ------------------------------------------
        // Animation Phases & Stages
        // ------------------------------------------
        BUILDER.comment("Animation Dynamics across the 3 Spellcasting Display Phases:\n" +
                "1. Emergence / Cast-In (ตอนเริ่มปรากฏ / สไลด์เข้ามา)\n" +
                "2. Active / Sustained Casting (ตอนร่ายปกติ / วงเวทย์ทำงาน)\n" +
                "3. Fade-Out / Dissolution (ตอนสลายตัวหลังร่ายจบ)"
        ).push("phases");

        // Phase 1: Emergence
        BUILDER.push("emergence");
        EMERGENCE_START_SCALE = BUILDER
                .comment("Initial scale factor when each layer begins materializing before converging to activeScale.")
                .defineInRange("emergenceStartScale", 1.12, 0.1, 3.0);

        EMERGENCE_SPEED_MULTIPLIER = BUILDER
                .comment("Rotation speed multiplier during the emergence / slide-in phase.")
                .defineInRange("emergenceSpeedMultiplier", 1.0, 0.0, 5.0);

        LAYER_STAGGER_TICKS = BUILDER
                .comment("Delay in ticks between each stacked layer (1..5) starting its emergence.")
                .defineInRange("layerStaggerTicks", 2.5, 0.0, 15.0);

        LAYER_FADE_IN_TICKS = BUILDER
                .comment("Duration in ticks for each layer to fade in and slide inward.")
                .defineInRange("layerFadeInTicks", 7.0, 1.0, 40.0);

        LAYER_SLIDE_DISTANCE = BUILDER
                .comment("Extra distance in blocks in front of the caster where layers spawn before sliding into place.")
                .defineInRange("layerSlideDistance", 0.65, 0.0, 3.0);
        BUILDER.pop();

        // Phase 2: Active
        BUILDER.push("active");
        ACTIVE_SCALE = BUILDER
                .comment("Overall scale multiplier during the active sustained casting phase.")
                .defineInRange("activeScale", 1.0, 0.1, 3.0);

        ACTIVE_SPEED_MULTIPLIER = BUILDER
                .comment("Overall rotation speed multiplier during active sustained casting.")
                .defineInRange("activeSpeedMultiplier", 1.0, 0.0, 5.0);

        PULSE_INTENSITY = BUILDER
                .comment("Intensity of the idle breathing pulse effect during active casting (0.0 = no pulse).")
                .defineInRange("pulseIntensity", 0.08, 0.0, 0.5);
        BUILDER.pop();

        // Phase 3: Fade-Out
        BUILDER.push("fade_out");
        FADE_OUT_END_SCALE = BUILDER
                .comment("Target scale factor as the magic circle dissolves after casting ends (e.g. >1.0 expands, <1.0 shrinks).")
                .defineInRange("fadeOutEndScale", 1.05, 0.1, 3.0);

        FADE_OUT_SPEED_MULTIPLIER = BUILDER
                .comment("Rotation speed multiplier during the post-cast fade-out phase.")
                .defineInRange("fadeOutSpeedMultiplier", 0.75, 0.0, 5.0);

        FADE_OUT_TICKS = BUILDER
                .comment("Duration in ticks (20 ticks = 1 second) for the magic circle to slowly fade out after casting ends.")
                .defineInRange("fadeOutTicks", 32, 5, 100);

        FADE_OUT_DRIFT_DISTANCE = BUILDER
                .comment("Distance in blocks the circle drifts outward/forward during fade-out.")
                .defineInRange("fadeOutDriftDistance", 0.15, 0.0, 2.0);
        BUILDER.pop();

        BUILDER.pop(); // End phases

        // ------------------------------------------
        // Gun Muzzle Settings
        // ------------------------------------------
        BUILDER.comment("Gun Muzzle Magic Circle Compatibility (Iron's Arms Spell Bullet)").push("gun_muzzle");

        ENABLE_GUN_MAGIC_CIRCLES = BUILDER
                .comment("Enable rendering of arcane magic circles at the firearm muzzle when firing spell bullets.")
                .define("enableGunMagicCircles", true);

        GUN_CIRCLE_SCALE = BUILDER
                .comment("Base scale multiplier for gun muzzle magic circles relative to weapon size (0.1 to 3.0).")
                .defineInRange("gunCircleScale", 0.35, 0.1, 3.0);

        GUN_BLAST_EXPANSION = BUILDER
                .comment("Expansion scale multiplier for the energetic gunshot muzzle blastwave.")
                .defineInRange("gunBlastExpansion", 1.12, 1.0, 3.0);

        GUN_CIRCLE_DURATION_TICKS = BUILDER
                .comment("Duration in ticks (20 ticks = 1 second) for the muzzle magic circle to flash and fade out after firing.")
                .defineInRange("gunCircleDurationTicks", 12, 3, 60);

        GUN_CIRCLE_FORWARD_OFFSET = BUILDER
                .comment("Additional forward or backward positional offset in blocks along the firearm barrel.")
                .defineInRange("gunCircleForwardOffset", 0.0, -1.0, 2.0);

        GUN_CIRCLE_ROTATION_SPEED = BUILDER
                .comment("Rotation speed multiplier for the high-energy gun muzzle spin (degrees per tick).")
                .defineInRange("gunCircleRotationSpeed", 4.5, 0.0, 30.0);

        GUN_CIRCLE_OPACITY = BUILDER
                .comment("Maximum opacity (alpha) of rendered gun muzzle circles (0.05 to 1.0).")
                .defineInRange("gunCircleOpacity", 0.65, 0.05, 1.0);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static String getCustomLayerTexture(int layerIndex) {
        return switch (layerIndex) {
            case 0 -> LAYER_1_TEXTURE.get();
            case 1 -> LAYER_2_TEXTURE.get();
            case 2 -> LAYER_3_TEXTURE.get();
            case 3 -> LAYER_4_TEXTURE.get();
            case 4 -> LAYER_5_TEXTURE.get();
            default -> "";
        };
    }

    public static float getLayerScale(int layerIndex) {
        double val = switch (layerIndex) {
            case 0 -> LAYER_1_SCALE.get();
            case 1 -> LAYER_2_SCALE.get();
            case 2 -> LAYER_3_SCALE.get();
            case 3 -> LAYER_4_SCALE.get();
            case 4 -> LAYER_5_SCALE.get();
            default -> 1.0;
        };
        return (float) val;
    }

    public static float getLayerSpeed(int layerIndex) {
        double val = switch (layerIndex) {
            case 0 -> LAYER_1_SPEED.get();
            case 1 -> LAYER_2_SPEED.get();
            case 2 -> LAYER_3_SPEED.get();
            case 3 -> LAYER_4_SPEED.get();
            case 4 -> LAYER_5_SPEED.get();
            default -> 1.0;
        };
        return (float) val;
    }

    public static void saveConfig() {
        SPEC.save();
    }
}
