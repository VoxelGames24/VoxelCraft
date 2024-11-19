package com.diamantino.voxelcraft.client.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.PixmapPackerIO;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.diamantino.voxelcraft.common.Constants;
import com.diamantino.voxelcraft.common.utils.FileUtils;
import com.diamantino.voxelcraft.common.utils.MathUtils;
import com.diamantino.voxelcraft.launchers.VoxelCraftClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ClientLoadingUtils {
    public static void loadResourcesFile(Map<String, JSONObject> modResources, String modId) {
        JSONObject resourcesFile = new JSONObject(Gdx.files.internal("assets/" + modId + "/resources.json").readString());

        modResources.put(modId, resourcesFile);
    }

    public static void loadTextures(VoxelCraftClient clientInstance, String modId, JSONObject modResources) {
        JSONArray textureLocationsVDO = modResources.getJSONArray("textureLocations");

        for (int i = 0; i < textureLocationsVDO.length(); i++) {
            String textureLoc = "assets" + File.separator + modId + File.separator + textureLocationsVDO.getString(i);

            List<FileHandle> textures = FileUtils.getAllFilesInFolderInternal(Gdx.files.internal(textureLoc), "png");
            List<FileHandle> vcMetas = FileUtils.getAllFilesInFolderInternal(Gdx.files.internal(textureLoc), "vcmeta");
            List<String> vcMetasNames = vcMetas.stream().map(FileHandle::pathWithoutExtension).toList();

            textures.forEach(texture -> {
                String textureName = texture.pathWithoutExtension();

                if (vcMetasNames.contains(textureName)) {
                    String vcMetaPath = textureName + ".vcmeta";

                    JSONObject metaVDO = new JSONObject(Gdx.files.internal(vcMetaPath).readString());

                    clientInstance.assetManager.load(vcMetaPath, JSONObject.class);

                    String typeVDO = metaVDO.getString("type");

                    if (typeVDO.equals("nine-patch")) {
                        JSONObject ninePatchVDO = metaVDO.getJSONObject("ninePatch");

                        int cutTop = ninePatchVDO.getInt("cutTop");
                        int cutBottom = ninePatchVDO.getInt("cutBottom");
                        int cutLeft = ninePatchVDO.getInt("cutLeft");
                        int cutRight = ninePatchVDO.getInt("cutRight");

                        NinePatch ninePatch = new NinePatch(new Texture(texture), cutLeft, cutRight, cutTop, cutBottom);

                        clientInstance.assetManager.load(texture.nameWithoutExtension(), ninePatch);
                    }
                } else {
                    clientInstance.assetManager.load(texture.path(), Texture.class);
                }
            });
        }
    }

    /**
     * Get the size of the width and height of the atlas in pixels.
     * @return Size in pixels.
     */
    private static int getAtlasSize(int textureSize, int files) {
        int sideTex = MathUtils.getNearestPO2((int) Math.round(Math.sqrt(files)));

        return sideTex * textureSize;
    }

    public static void saveAtlases(VoxelCraftClient clientInstance, Map<String, PixmapPacker> atlasPackers, String modId, JSONObject modResources) {
        JSONArray atlasesVDO = modResources.getJSONArray("atlases");

        for (int i = 0; i < atlasesVDO.length(); i++) {
            JSONObject atlasJson = atlasesVDO.getJSONObject(i);

            String textureLoc = "assets" + File.separator + modId + File.separator + atlasJson.getString("location");

            List<FileHandle> textures = FileUtils.getAllFilesInFolderInternal(Gdx.files.internal(textureLoc), "png");
            List<FileHandle> vcMetas = FileUtils.getAllFilesInFolderInternal(Gdx.files.internal(textureLoc), "vcmeta");

            if (textures.isEmpty()) {
                Gdx.app.error(Constants.errorLogTag, "No textures found in the location: " + textureLoc);

                return;
            }

            PixmapPacker packer = atlasPackers.computeIfAbsent(atlasJson.getString("name"), (_) -> {
                int additionalSpace = 0;

                for (FileHandle metaFile : vcMetas) {
                    clientInstance.assetManager.load(metaFile.path(), JSONObject.class);

                    JSONObject metaJson = new JSONObject(Gdx.files.internal(metaFile.path()).readString());

                    if (metaJson.has("frames")) {
                        additionalSpace += metaJson.getInt("frames");
                    }
                }

                int size = atlasJson.getInt("size");

                if (size == -1) {
                    size = getAtlasSize(new Texture(textures.getFirst()).getWidth(), textures.size() + additionalSpace);
                }

                return new PixmapPacker(size, size, Pixmap.Format.RGBA8888, 0, false);
            });

            for (FileHandle file : textures) {
                Pixmap pixmap = new Pixmap(file);

                packer.pack(file.name().replace(".png", ""), pixmap);

                pixmap.dispose();
            }
        }
    }

    public static void loadAtlases(VoxelCraftClient clientInstance, Map<String, PixmapPacker> atlasPackers) {
        atlasPackers.forEach((atlasName, packer) -> {
            try {
                String atlasLoc = FileUtils.getVoxelCraftFolder() + "/cache/atlases/" + atlasName + ".atlas";

                PixmapPackerIO pixmapPackerIO = new PixmapPackerIO();

                pixmapPackerIO.save(Gdx.files.absolute(atlasLoc), packer);

                clientInstance.assetManager.load(atlasLoc, TextureAtlas.class);

                packer.dispose();
            } catch (Exception e) {
                Gdx.app.error(Constants.errorLogTag, "Error while saving atlas: " + atlasName + ".atlas: " + e.getMessage());
            }
        });

        atlasPackers.clear();
    }

    /**
     * Get the texture index in the atlas.
     * @param name Name of the texture.
     * @return Index of the texture in the atlas.
     */
    public static int getBlockTextureIndex(VoxelCraftClient clientInstance, String atlasName, String name) {
        TextureAtlas atlas = clientInstance.assetManager.get(FileUtils.getVoxelCraftFolder() + "/cache/atlases/" + atlasName + ".atlas", TextureAtlas.class);

        for (int i = 0, n = atlas.getRegions().size; i < n; i++) {
            if (atlas.getRegions().get(i).name.equals(name)) {
                return i;
            }
        }

        return getBlockTextureIndex(clientInstance, atlasName, "not_found");
    }
}
