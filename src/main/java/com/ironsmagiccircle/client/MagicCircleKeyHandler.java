package com.ironsmagiccircle.client;

import com.ironsmagiccircle.IronsMagicCircleMod;
import com.ironsmagiccircle.client.gui.MagicCircleConfigScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

/**
 * Keybind handler allowing players to press 'O' (or custom key) to configure magic circle textures, scales, and speeds in-game.
 */
public class MagicCircleKeyHandler {
    public static final String KEY_CATEGORY = "key.categories.irons_magic_circle";
    public static final String KEY_OPEN_CONFIG = "key.irons_magic_circle.open_config";

    public static final KeyMapping OPEN_CONFIG_KEY = new KeyMapping(
            KEY_OPEN_CONFIG,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            KEY_CATEGORY
    );

    @EventBusSubscriber(modid = IronsMagicCircleMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(OPEN_CONFIG_KEY);
        }
    }

    @EventBusSubscriber(modid = IronsMagicCircleMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static class GameBusEvents {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            while (OPEN_CONFIG_KEY.consumeClick()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.screen == null) {
                    mc.setScreen(new MagicCircleConfigScreen(null));
                }
            }
        }
    }
}

