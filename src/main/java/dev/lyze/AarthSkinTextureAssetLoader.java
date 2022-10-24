package dev.lyze;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import lombok.Data;
import lombok.var;

public class AarthSkinTextureAssetLoader extends AsynchronousAssetLoader<Texture, AarthSkinTextureAssetLoader.AarthSkinParameter> {
    private String mapFile, textureFile;

    public AarthSkinTextureAssetLoader() {
        this(new InternalFileHandleResolver());
    }

    public AarthSkinTextureAssetLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, AarthSkinParameter parameter) {
        var content = file.readString().split("\n");
        if (content.length != 2)
            throw new GdxRuntimeException(new IllegalArgumentException("Aarth skin file should contain two lines: \nmap\ntexture"));

        mapFile = content[0].trim();
        textureFile = content[1].trim();
    }

    @Override
    public Texture loadSync(AssetManager manager, String fileName, FileHandle file, AarthSkinParameter parameter) {
        var map = manager.get(mapFile, Texture.class);
        var texture = manager.get(textureFile, Texture.class);

        if (!map.getTextureData().isPrepared())
            map.getTextureData().prepare();

        if (!texture.getTextureData().isPrepared())
            texture.getTextureData().prepare();

        var mapPixmap = map.getTextureData().consumePixmap();
        var texturePixmap = texture.getTextureData().consumePixmap();

        for (int x = 0; x < texturePixmap.getWidth(); x++) {
            for (int y = 0; y < texturePixmap.getHeight(); y++) {
                var color = texturePixmap.getPixel(x, y);

                if (color == 0)
                    continue;

                var xOnMap = (color & 0xff000000) >>> 24; // r
                var yOnMap = (color & 0x00ff0000) >>> 16; // g

                var mapColor = mapPixmap.getPixel(xOnMap, yOnMap);

                var r = ((mapColor & 0xff000000) >>> 24) / 255f;
                var g = ((mapColor & 0x00ff0000) >>> 16) / 255f;
                var b = ((mapColor & 0x0000ff00) >>> 8) / 255f;

                var a = ((parameter != null && parameter.keepAlphaFromSource ? color : mapColor) & 0x000000ff) / 255f;

                texturePixmap.setColor(r, g, b, a);
                texturePixmap.drawPixel(x, y);
            }
        }

        texturePixmap.setColor(Color.RED);
        texturePixmap.drawPixel(1, 6);

        return new Texture(texturePixmap);
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, AarthSkinParameter parameter) {
        var descriptors = new Array<AssetDescriptor>();

        var content = file.readString().split("\n");
        if (content.length != 2)
            throw new GdxRuntimeException(new IllegalArgumentException("Aarth skin file should contain two lines: \nmap\ntexture"));

        descriptors.add(new AssetDescriptor<>(resolve(content[0].trim()), Texture.class));
        descriptors.add(new AssetDescriptor<>(resolve(content[1].trim()), Texture.class));

        return descriptors;
    }

    @Data
    public static class AarthSkinParameter extends AssetLoaderParameters<Texture> {
        private boolean keepAlphaFromSource;

        private TextureLoader.TextureParameter textureParameter;
    }
}
