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
    private String sourceFile, mapFile, lookupFile;

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
            throw new GdxRuntimeException(new IllegalArgumentException("Aarth skin file should contain three lines: \nsource\nmap\nlookup"));

        sourceFile = getRelativeFileHandle(file, content[0].trim()).path();
        mapFile = getRelativeFileHandle(file, content[1].trim()).path();
        lookupFile = getRelativeFileHandle(file, content[2].trim()).path();
    }

    @Override
    public TextureAtlas loadSync(AssetManager manager, String fileName, FileHandle file, AarthSkinParameter parameter) {
        var source = manager.get(sourceFile, TextureAtlas.class);
        var map = manager.get(mapFile, Texture.class);
        var lookup = manager.get(lookupFile, Texture.class);


        return convert(source, map, lookup);
    }

    public TextureAtlas convert(TextureAtlas source, Texture map, Texture lookup) {
         var textures = new ObjectMap<Texture, Texture>();

        for (var texture : source.getTextures())
            textures.put(texture, new Texture(convert(texture, map, lookup)));

        for (var region : source.getRegions())
            region.setTexture(textures.get(region.getTexture()));

        return source;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, AarthSkinParameter parameter) {
        var descriptors = new Array<AssetDescriptor>();

        var content = file.readString().split("\n");
        if (content.length != 3)
            throw new GdxRuntimeException(new IllegalArgumentException("Aarth skin file should contain three lines: \nsource\nmap\nlookup"));

        descriptors.add(new AssetDescriptor<>(getRelativeFileHandle(file, content[0].trim()), TextureAtlas.class, parameter != null ? parameter.sourceTextureAtlasParameter : null));
        descriptors.add(new AssetDescriptor<>(getRelativeFileHandle(file, content[1].trim()), Texture.class, parameter != null ? parameter.mapTextureParameter : null));
        descriptors.add(new AssetDescriptor<>(getRelativeFileHandle(file, content[2].trim()), Texture.class, parameter != null ? parameter.lookupTextureParameter : null));

        return descriptors;
    }

    @Data
    public static class AarthSkinParameter extends AssetLoaderParameters<TextureAtlas> {
        private TextureAtlasLoader.TextureAtlasParameter sourceTextureAtlasParameter;
        private TextureLoader.TextureParameter mapTextureParameter, lookupTextureParameter;
    }
}
