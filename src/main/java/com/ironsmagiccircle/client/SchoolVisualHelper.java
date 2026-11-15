package com.ironsmagiccircle.client;

import com.ironsmagiccircle.IronsMagicCircleMod;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dynamically resolves magic circle textures and visual color tints for ANY school,
 * including addon-registered schools and third-party content.
 */
public class SchoolVisualHelper {

    private static final int MAX_LAYERS = 5;
    private static final String FALLBACK_BASE_SCHOOL = "evocation";

    public static class SchoolVisual {
        public final ResourceLocation schoolId;
        public final ResourceLocation[] layers;
        public final boolean isDedicatedTexture;
        public final int red;
        public final int green;
        public final int blue;

        public SchoolVisual(ResourceLocation schoolId, ResourceLocation[] layers, boolean isDedicatedTexture, int red, int green, int blue) {
            this.schoolId = schoolId;
            this.layers = layers;
            this.isDedicatedTexture = isDedicatedTexture;
            this.red = red;
            this.green = green;
            this.blue = blue;
        }
    }

    private static final Map<ResourceLocation, SchoolVisual> VISUAL_CACHE = new ConcurrentHashMap<>();

    /**
     * Resolves the visual appearance for a given SchoolType and its ResourceLocation ID.
     */
    public static SchoolVisual getSchoolVisual(ResourceLocation schoolId, SchoolType schoolType) {
        if (schoolId == null) {
            schoolId = SchoolRegistry.EVOCATION_RESOURCE;
        }
        return VISUAL_CACHE.computeIfAbsent(schoolId, key -> resolveVisualInternal(key, schoolType));
    }

    private static SchoolVisual resolveVisualInternal(ResourceLocation schoolId, SchoolType schoolType) {
        if (schoolType == null) {
            schoolType = SchoolRegistry.getSchool(schoolId);
        }

        Minecraft mc = Minecraft.getInstance();
        ResourceManager rm = mc.getResourceManager();
        String path = schoolId.getPath();

        // 1. Check if dedicated textures exist in irons_magic_circle mod or resourcepack
        ResourceLocation testModLoc = ResourceLocation.fromNamespaceAndPath(
                IronsMagicCircleMod.MODID,
                "textures/magic_circle/" + path + "/layer_1.png"
        );

        boolean existsInMod = (rm != null) && rm.getResource(testModLoc).isPresent();

        if (existsInMod) {
            ResourceLocation[] layers = new ResourceLocation[MAX_LAYERS];
            for (int i = 0; i < MAX_LAYERS; i++) {
                layers[i] = ResourceLocation.fromNamespaceAndPath(
                        IronsMagicCircleMod.MODID,
                        "textures/magic_circle/" + path + "/layer_" + (i + 1) + ".png"
                );
            }
            return new SchoolVisual(schoolId, layers, true, 255, 255, 255);
        }

        // 2. Check if dedicated textures exist in the addon mod's own namespace
        ResourceLocation testAddonLoc = ResourceLocation.fromNamespaceAndPath(
                schoolId.getNamespace(),
                "textures/magic_circle/" + path + "/layer_1.png"
        );
        boolean existsInAddon = (rm != null) && rm.getResource(testAddonLoc).isPresent();

        if (existsInAddon) {
            ResourceLocation[] layers = new ResourceLocation[MAX_LAYERS];
            for (int i = 0; i < MAX_LAYERS; i++) {
                layers[i] = ResourceLocation.fromNamespaceAndPath(
                        schoolId.getNamespace(),
                        "textures/magic_circle/" + path + "/layer_" + (i + 1) + ".png"
                );
            }
            return new SchoolVisual(schoolId, layers, true, 255, 255, 255);
        }

        // 3. FALLBACK: Addon school has no PNG textures.
        // Use clean base geometric rings from 'evocation' and dynamically tint them with the addon's targeting color!
        ResourceLocation[] fallbackLayers = new ResourceLocation[MAX_LAYERS];
        for (int i = 0; i < MAX_LAYERS; i++) {
            fallbackLayers[i] = ResourceLocation.fromNamespaceAndPath(
                    IronsMagicCircleMod.MODID,
                    "textures/magic_circle/" + FALLBACK_BASE_SCHOOL + "/layer_" + (i + 1) + ".png"
            );
        }

        int r = 240, g = 240, b = 255;
        if (schoolType != null) {
            Vector3f targetColor = schoolType.getTargetingColor();
            if (targetColor != null) {
                float cx = targetColor.x();
                float cy = targetColor.y();
                float cz = targetColor.z();
                r = Mth.clamp(cx <= 1.0f ? (int) (cx * 255.0f) : (int) cx, 20, 255);
                g = Mth.clamp(cy <= 1.0f ? (int) (cy * 255.0f) : (int) cy, 20, 255);
                b = Mth.clamp(cz <= 1.0f ? (int) (cz * 255.0f) : (int) cz, 20, 255);
            } else if (schoolType.getDisplayName() != null && schoolType.getDisplayName().getStyle().getColor() != null) {
                int rgb = schoolType.getDisplayName().getStyle().getColor().getValue();
                r = (rgb >> 16) & 0xFF;
                g = (rgb >> 8) & 0xFF;
                b = rgb & 0xFF;
            }
        }

        return new SchoolVisual(schoolId, fallbackLayers, false, r, g, b);
    }

    /**
     * Dynamically collects all registered schools in the game (Iron's Spells + all installed Addons).
     */
    public static List<ResourceLocation> getAvailableSchools() {
        List<ResourceLocation> list = new ArrayList<>();
        if (SchoolRegistry.REGISTRY != null) {
            for (ResourceLocation id : SchoolRegistry.REGISTRY.keySet()) {
                if (id != null && !list.contains(id)) {
                    list.add(id);
                }
            }
        }
        if (list.isEmpty()) {
            list.add(SchoolRegistry.FIRE_RESOURCE);
            list.add(SchoolRegistry.ICE_RESOURCE);
            list.add(SchoolRegistry.LIGHTNING_RESOURCE);
            list.add(SchoolRegistry.HOLY_RESOURCE);
            list.add(SchoolRegistry.ENDER_RESOURCE);
            list.add(SchoolRegistry.BLOOD_RESOURCE);
            list.add(SchoolRegistry.EVOCATION_RESOURCE);
            list.add(SchoolRegistry.NATURE_RESOURCE);
            list.add(SchoolRegistry.ELDRITCH_RESOURCE);
        }
        return list;
    }

    /**
     * Clears the cache when resource packs or configs reload.
     */
    public static void clearCache() {
        VISUAL_CACHE.clear();
    }
}

