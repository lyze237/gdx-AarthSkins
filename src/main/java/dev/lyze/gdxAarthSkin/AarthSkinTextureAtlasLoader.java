package dev.lyze.gdxAarthSkin;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.TextureAtlasLoader;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import lombok.Data;
import lombok.var;

public class AarthSkinTextureAtlasLoader extends AarthSkinBaseLoader<TextureAtlas, AarthSkinTextureAtlasLoader.AarthSkinParameter> {
    private String textureFile, intermediateFile, mapFile;

    public AarthSkinTextureAtlasLoader() {
        this(new InternalFileHandleResolver());
    }

    public AarthSkinTextureAtlasLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, AarthSkinParameter parameter) {
        var content = file.readString().split("\n");
        if (content.length != 3)
            throw new GdxRuntimeException(new IllegalArgumentException("Aarth skin file should contain two lines: \nsource\nintermediate\nmap"));

        textureFile = getRelativeFileHandle(file, content[0].trim()).path();
        intermediateFile = getRelativeFileHandle(file, content[1].trim()).path();
        mapFile = getRelativeFileHandle(file, content[2].trim()).path();
    }

    @Override
    public TextureAtlas loadSync(AssetManager manager, String fileName, FileHandle file, AarthSkinParameter parameter) {
        var atlas = manager.get(textureFile, TextureAtlas.class);
        var map = manager.get(mapFile, Texture.class);
        var intermediate = manager.get(intermediateFile, Texture.class);

        var textureMap = new ObjectMap<Texture, Texture>();

        for (var texture : atlas.getTextures()) {
            var finalPixmap = convert(texture, intermediate, map);

            textureMap.put(texture, new Texture(finalPixmap));
        }

        for (var region : atlas.getRegions()) {
            var newTexture = textureMap.get(region.getTexture());

            region.setTexture(newTexture);
        }

        return atlas;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, AarthSkinParameter parameter) {
        var descriptors = new Array<AssetDescriptor>();

        var content = file.readString().split("\n");
        if (content.length != 3)
            throw new GdxRuntimeException(new IllegalArgumentException("Aarth skin file should contain two lines: \nsource\nintermediate\nmap"));

        descriptors.add(new AssetDescriptor<>(getRelativeFileHandle(file, content[0].trim()), TextureAtlas.class, parameter != null ? parameter.textureAtlasParameter : null));
        descriptors.add(new AssetDescriptor<>(getRelativeFileHandle(file, content[1].trim()), Texture.class, parameter != null ? parameter.intermediateTextureParameter : null));
        descriptors.add(new AssetDescriptor<>(getRelativeFileHandle(file, content[2].trim()), Texture.class, parameter != null ? parameter.mapTextureParameter : null));

        return descriptors;
    }

    @Data
    public static class AarthSkinParameter extends AssetLoaderParameters<TextureAtlas> {
        private TextureLoader.TextureParameter intermediateTextureParameter, mapTextureParameter;
        private TextureAtlasLoader.TextureAtlasParameter textureAtlasParameter;
    }
}
