package com.ironsmagiccircle.client;

import com.ironsmagiccircle.IronsMagicCircleMod;
import com.ironsmagiccircle.config.MagicCircleClientConfig;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * Client-side world render handler for arcane magic circles.
 */
@EventBusSubscriber(modid = IronsMagicCircleMod.MODID, value = Dist.CLIENT)
public class MagicCircleRenderHandler {

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        if (!MagicCircleClientConfig.ENABLE_MAGIC_CIRCLES.get()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }

        // Ready for custom magic circle rendering per school / casting state
    }
}
