package dev.lyze.gdxAarthSkin;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import lombok.Data;
import lombok.var;

public class AarthSkinTextureLoader extends AarthSkinBaseLoader<Texture, AarthSkinTextureLoader.AarthSkinParameter> {
    private String textureFile, mapFile, lookupFile;

    public AarthSkinTextureLoader() {
        this(new InternalFileHandleResolver());
    }

    public AarthSkinTextureLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, AarthSkinParameter parameter) {
        var content = file.readString().split("\n");
        if (content.length != 3)
            throw new GdxRuntimeException(new IllegalArgumentException("Aarth skin file should contain three lines: \nsource\nmap\nlookup"));

        textureFile = getRelativeFileHandle(file, content[0].trim()).path();
        mapFile = getRelativeFileHandle(file, content[1].trim()).path();
        lookupFile = getRelativeFileHandle(file, content[2].trim()).path();
    }

    @Override
    public Texture loadSync(AssetManager manager, String fileName, FileHandle file, AarthSkinParameter parameter) {
        var texture = manager.get(textureFile, Texture.class);
        var map = manager.get(mapFile, Texture.class);
        var lookup = manager.get(lookupFile, Texture.class);

        var result = convert(texture, map, lookup);
        return new Texture(result);
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, AarthSkinParameter parameter) {
        var descriptors = new Array<AssetDescriptor>();

        var content = file.readString().split("\n");
        if (content.length != 3)
            throw new GdxRuntimeException(new IllegalArgumentException("Aarth skin file should contain three lines: \nsource\nmap\nlookup"));

        descriptors.add(new AssetDescriptor<>(getRelativeFileHandle(file, content[0].trim()), Texture.class, parameter != null ? parameter.sourceTextureParameter : null));
        descriptors.add(new AssetDescriptor<>(getRelativeFileHandle(file, content[1].trim()), Texture.class, parameter != null ? parameter.mapTextureParameter : null));
        descriptors.add(new AssetDescriptor<>(getRelativeFileHandle(file, content[2].trim()), Texture.class, parameter != null ? parameter.lookupTextureParameter : null));

        return descriptors;
    }

    @Data
    public static class AarthSkinParameter extends AssetLoaderParameters<Texture> {
        private TextureLoader.TextureParameter sourceTextureParameter, mapTextureParameter, lookupTextureParameter;
    }
}
