package com.ironsmagiccircle.client.compat.gun;

import net.minecraft.world.entity.player.Player;

/**
 * Clean decoupled bridge between core magic circle renderer and optional firearm mods.
 * Contains no external mod imports to guarantee safe class verification.
 */
public interface IGunCompatBridge {
    /**
     * @return true if the player is currently scoping / aiming down sights with a firearm.
     */
    boolean isScoping(Player player);
}

