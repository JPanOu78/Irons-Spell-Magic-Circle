package com.ironsmagiccircle.client;

import com.ironsmagiccircle.IronsMagicCircleMod;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages custom external texture files for Magic Circle layers.
 * Allows players to open the textures folder, choose PNG files via native file dialogs,
 * and dynamically uploads them into Minecraft's TextureManager.
 */
public class CustomTextureManager {

    private static Path texturesDir = null;
    private static final Map<String, ResourceLocation> LOADED_TEXTURES = new ConcurrentHashMap<>();

    public static Path getTexturesDirectory() {
        if (texturesDir == null) {
            texturesDir = Minecraft.getInstance().gameDirectory.toPath()
                    .resolve("config")
                    .resolve(IronsMagicCircleMod.MODID)
                    .resolve("textures");
        }
        try {
            if (!Files.exists(texturesDir)) {
                Files.createDirectories(texturesDir);
            }
        } catch (Exception e) {
            IronsMagicCircleMod.LOGGER.error("Failed to create textures directory: " + texturesDir, e);
        }
        return texturesDir;
    }

    /**
     * Opens the .minecraft/config/irons_magic_circle/textures folder in the OS file explorer.
     */
    public static void openTexturesFolder() {
        Path dir = getTexturesDirectory();
        Util.getPlatform().openFile(dir.toFile());
    }

    /**
     * Opens the native OS file picker to select a PNG file.
     * Copies the selected file into the mod's textures folder if external,
     * registers it as a DynamicTexture, and returns a descriptor (e.g. "file:my_ring.png").
     */
    public static String openFileDialogAndImport() {
        try {
            Path defaultDir = getTexturesDirectory();
            String defaultPath = defaultDir.toAbsolutePath().toString();

            String selectedFilePath;
            try (MemoryStack stack = MemoryStack.stackPush()) {
                PointerBuffer filters = stack.mallocPointer(1);
                filters.put(stack.UTF8("*.png"));
                filters.flip();

                selectedFilePath = TinyFileDialogs.tinyfd_openFileDialog(
                        "Select Magic Circle PNG Texture",
                        defaultPath,
                        filters,
                        "PNG Images (*.png)",
                        false
                );
            }

            if (selectedFilePath == null || selectedFilePath.trim().isEmpty()) {
                return null;
            }

            File chosenFile = new File(selectedFilePath);
            if (!chosenFile.exists() || !chosenFile.isFile()) {
                return null;
            }

            // Copy to textures folder if not already inside it
            Path destPath = defaultDir.resolve(chosenFile.getName());
            if (!chosenFile.toPath().equals(destPath)) {
                Files.copy(chosenFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
            }

            // Pre-load dynamic texture into TextureManager
            loadDynamicTexture(destPath.toFile());

            return "file:" + chosenFile.getName();
        } catch (Throwable t) {
            IronsMagicCircleMod.LOGGER.error("Failed to select/import texture file", t);
            return null;
        }
    }

    /**
     * Lists all PNG file names currently stored in the mod's textures folder.
     */
    public static List<String> getAvailableTextureFiles() {
        Path dir = getTexturesDirectory();
        File folder = dir.toFile();
        if (!folder.exists() || !folder.isDirectory()) {
            return Collections.emptyList();
        }

        File[] files = folder.listFiles((d, name) -> name.toLowerCase().endsWith(".png"));
        if (files == null || files.length == 0) {
            return Collections.emptyList();
        }

        List<String> list = new ArrayList<>();
        for (File f : files) {
            list.add(f.getName());
        }
        return list;
    }

    /**
     * Resolves a texture string (either a "file:<filename>" or a Minecraft ResourceLocation string)
     * to a GPU-uploaded ResourceLocation.
     */
    public static ResourceLocation resolveCustomTexture(String texturePath) {
        if (texturePath == null || texturePath.trim().isEmpty()) {
            return null;
        }

        String trimmed = texturePath.trim();

        // 1. If it's a file descriptor "file:<filename>" or an absolute path
        if (trimmed.startsWith("file:")) {
            String fileName = trimmed.substring(5).trim();
            File file = getTexturesDirectory().resolve(fileName).toFile();
            if (file.exists() && file.isFile()) {
                return loadDynamicTexture(file);
            }
            return null;
        }

        // 2. If it's a direct file path on disk (e.g. C:/.../image.png)
        File directFile = new File(trimmed);
        if (directFile.exists() && directFile.isFile() && trimmed.toLowerCase().endsWith(".png")) {
            return loadDynamicTexture(directFile);
        }

        // 3. Otherwise try to parse as Minecraft ResourceLocation
        return ResourceLocation.tryParse(trimmed);
    }

    /**
     * Reads a PNG image from disk, creates a DynamicTexture, and registers it with Minecraft's TextureManager.
     */
    public static ResourceLocation loadDynamicTexture(File file) {
        String key = file.getAbsolutePath();
        if (LOADED_TEXTURES.containsKey(key)) {
            return LOADED_TEXTURES.get(key);
        }

        try (InputStream stream = new FileInputStream(file)) {
            NativeImage nativeImage = NativeImage.read(stream);
            DynamicTexture dynamicTexture = new DynamicTexture(nativeImage);

            String safeName = file.getName().replaceAll("[^a-zA-Z0-9_\\-]", "_").toLowerCase();
            ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(
                    IronsMagicCircleMod.MODID,
                    "dynamic_layer_" + safeName + "_" + Math.abs(key.hashCode())
            );

            Minecraft.getInstance().getTextureManager().register(loc, dynamicTexture);
            LOADED_TEXTURES.put(key, loc);
            IronsMagicCircleMod.LOGGER.info("Registered dynamic magic circle texture: " + loc + " from " + key);
            return loc;
        } catch (Throwable t) {
            IronsMagicCircleMod.LOGGER.error("Failed to load dynamic texture from file: " + file, t);
            return null;
        }
    }
}

