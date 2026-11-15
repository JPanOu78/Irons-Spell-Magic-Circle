package com.ironsmagiccircle;

import com.ironsmagiccircle.config.MagicCircleClientConfig;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;

/**
 * Purely client-side visual addon for Iron's Spells 'n Spellbooks.
 * Renders arcane magic circles during spellcasting without requiring server installation.
 */
@Mod(value = IronsMagicCircleMod.MODID, dist = Dist.CLIENT)
public class IronsMagicCircleMod {
    public static final String MODID = "irons_magic_circle";
    public static final Logger LOGGER = LogUtils.getLogger();

    public IronsMagicCircleMod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, MagicCircleClientConfig.SPEC);
        modContainer.registerExtensionPoint(
                net.neoforged.neoforge.client.gui.IConfigScreenFactory.class,
                (container, parentScreen) -> new com.ironsmagiccircle.client.gui.MagicCircleConfigScreen(parentScreen)
        );
        modEventBus.addListener(this::onClientSetup);
        LOGGER.info("Iron's Spell Magic Circle (Client-Only) initialized.");
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Iron's Spell Magic Circle client renderer ready.");
        if (net.neoforged.fml.ModList.get().isLoaded("irons_artifice") && net.neoforged.fml.ModList.get().isLoaded("irons_spell_bullet")) {
            try {
                com.ironsmagiccircle.client.compat.gun.GunMagicCircleCompat.init();
                LOGGER.info("Iron's Spell Magic Circle: Gun compatibility initialized successfully.");
            } catch (Throwable t) {
                LOGGER.error("Failed to initialize gun compatibility", t);
            }
        }
    }
}

