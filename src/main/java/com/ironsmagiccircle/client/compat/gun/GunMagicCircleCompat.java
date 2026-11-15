package com.ironsmagiccircle.client.compat.gun;

import com.ironsmagiccircle.client.MagicCircleRenderHandler;
import com.ironsmagiccircle.config.MagicCircleClientConfig;
import com.ironsspellbullet.component.ModDataComponents;
import com.ironsspellbullet.component.SpellChamberData;
import com.ironsspellbullet.event.GunplayEvents;
import io.redspace.irons_artifice.api.GunAboutToShootEvent;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.network.packets.ClientboundMuzzleFlashPacket;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Event-driven bridge connecting Iron's Arms Spell Bullet + Iron's Artifice with
 * Iron's Spell Magic Circle.
 *
 * Isolated in this class so all dependencies on irons_artifice and irons_spell_bullet
 * remain soft and do not load unless both mods are present in the environment.
 */
public class GunMagicCircleCompat implements IGunCompatBridge {

    private static long lastLocalShotTimeMs = 0L;

    public static void init() {
        GunMagicCircleCompat instance = new GunMagicCircleCompat();
        MagicCircleRenderHandler.registerGunBridge(instance);
        NeoForge.EVENT_BUS.register(instance);
    }

    @Override
    public boolean isScoping(Player player) {
        return GunItem.isScoping(player);
    }

    /**
     * Triggered on client tick (synchronous zero-latency prediction) when local player or
     * integrated server entity fires a firearm with a spell bullet loaded.
     */
    @SubscribeEvent
    public void onGunAboutToShoot(GunAboutToShootEvent event) {
        if (!MagicCircleClientConfig.ENABLE_GUN_MAGIC_CIRCLES.get()) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ShotProfile profile = event.getShotProfile();
        if (profile == null) {
            return;
        }

        SpellChamberData.SpellSlotEntry entry = profile.components().getOrDefault(GunplayEvents.SHOT_SPELL);
        if (entry != null && entry.hasSpell()) {
            AbstractSpell spell = entry.getSpell();
            if (spell != null && spell != SpellRegistry.none()) {
                double muzzleOffset = 0.0;
                if (profile.components().has(ShotComponents.MUZZLE_OFFSET)) {
                    muzzleOffset = profile.value(ShotComponents.MUZZLE_OFFSET);
                }

                int spellLevel = Math.max(1, entry.spellLevel());
                triggerForPlayer(player, spell, spellLevel, (float) muzzleOffset);

                if (player == Minecraft.getInstance().player) {
                    lastLocalShotTimeMs = System.currentTimeMillis();
                }
            }
        }
    }

    /**
     * Triggered when ClientboundMuzzleFlashPacket arrives from server, allowing remote players
     * in multiplayer to display their gun magic circle bursts.
     */
    public static void onClientboundMuzzleFlash(ClientboundMuzzleFlashPacket packet) {
        if (!MagicCircleClientConfig.ENABLE_GUN_MAGIC_CIRCLES.get()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        int entityId = packet.entityId();
        if (mc.player != null && mc.player.getId() == entityId) {
            // Already handled on client tick by onGunAboutToShoot with zero latency
            if (System.currentTimeMillis() - lastLocalShotTimeMs < 150L) {
                return;
            }
        }

        Entity entity = mc.level.getEntity(entityId);
        if (entity instanceof Player player) {
            ItemStack gunStack = player.getMainHandItem();
            if (gunStack.getItem() instanceof GunItem) {
                SpellChamberData chamber = gunStack.get(ModDataComponents.LOADED_SPELLS.get());
                if (chamber != null && !chamber.isEmpty()) {
                    SpellChamberData.SpellSlotEntry entry = chamber.peekNext();
                    if (entry != null && entry.hasSpell()) {
                        AbstractSpell spell = entry.getSpell();
                        if (spell != null && spell != SpellRegistry.none()) {
                            triggerForPlayer(player, spell, entry.spellLevel(), packet.extraForwardOffset());
                        }
                    }
                }
            }
        }
    }

    private static void triggerForPlayer(Player player, AbstractSpell spell, int spellLevel, float extraForwardOffset) {
        SchoolType schoolType = spell.getSchoolType();
        net.minecraft.resources.ResourceLocation schoolId = (schoolType != null && schoolType.getId() != null)
                ? schoolType.getId()
                : io.redspace.ironsspellbooks.api.registry.SchoolRegistry.EVOCATION_RESOURCE;

        SpellRarity rarity = spell.getRarity(spellLevel);
        int rarityIndex = (rarity != null) ? rarity.getValue() : 0;
        int layerCount = Mth.clamp(rarityIndex + 2, 2, 5); // Minimum 2 layers for dynamic counter-rotating visual impact

        MagicCircleRenderHandler.triggerGunMagicCircle(
                player.getUUID(),
                spell.getSpellId(),
                schoolId,
                schoolType,
                layerCount,
                extraForwardOffset
        );
    }
}

