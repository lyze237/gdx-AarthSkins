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

public class AarthSkinTextureAssetLoader extends AarthSkinBaseAssetLoader<Texture, AarthSkinTextureAssetLoader.AarthSkinParameter> {
    private String textureFile, intermediateFile, mapFile;

    public AarthSkinTextureAssetLoader() {
        this(new InternalFileHandleResolver());
    }

    public AarthSkinTextureAssetLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, AarthSkinParameter parameter) {
        var content = file.readString().split("\n");
        if (content.length != 3)
            throw new GdxRuntimeException(new IllegalArgumentException("Aarth skin file should contain two lines: \nsource\nintermediate\nmap"));

        textureFile = content[0].trim();
        intermediateFile = content[1].trim();
        mapFile = content[2].trim();
    }

    @Override
    public Texture loadSync(AssetManager manager, String fileName, FileHandle file, AarthSkinParameter parameter) {
        var texture = manager.get(textureFile, Texture.class);
        var map = manager.get(mapFile, Texture.class);
        var intermediate = manager.get(intermediateFile, Texture.class);

        var finalPixmap = convert(texture, intermediate, map, parameter != null && parameter.keepAlphaFromSource);

        return new Texture(finalPixmap);
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, AarthSkinParameter parameter) {
        var descriptors = new Array<AssetDescriptor>();

        var content = file.readString().split("\n");
        if (content.length != 3)
            throw new GdxRuntimeException(new IllegalArgumentException("Aarth skin file should contain two lines: \nsource\nintermediate\nmap"));

        descriptors.add(new AssetDescriptor<>(resolve(content[0].trim()), Texture.class, parameter != null ? parameter.textureParameter : null));
        descriptors.add(new AssetDescriptor<>(resolve(content[1].trim()), Texture.class, parameter != null ? parameter.intermediateTextureParameter : null));
        descriptors.add(new AssetDescriptor<>(resolve(content[2].trim()), Texture.class, parameter != null ? parameter.mapTextureParameter : null));

        return descriptors;
    }

    @Data
    public static class AarthSkinParameter extends AssetLoaderParameters<Texture> {
        private boolean keepAlphaFromSource;

        private TextureLoader.TextureParameter textureParameter, intermediateTextureParameter, mapTextureParameter;
    }
}
