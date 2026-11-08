package com.ironsmagiccircle.mixin;

import com.ironsmagiccircle.client.compat.gun.GunMagicCircleCompat;
import io.redspace.irons_artifice.network.packets.ClientboundMuzzleFlashPacket;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientboundMuzzleFlashPacket.class, remap = false)
public class ClientboundMuzzleFlashPacketMixin {

    @Inject(method = "handle", at = @At("HEAD"), remap = false)
    private static void ironsmagiccircle$onMuzzleFlashPacket(ClientboundMuzzleFlashPacket packet, IPayloadContext context, CallbackInfo ci) {
        GunMagicCircleCompat.onClientboundMuzzleFlash(packet);
    }
}

