package dev.lyze.gdxAarthSkin;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.TextureAtlasLoader;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import lombok.Data;
import lombok.var;

public class AarthSkinTextureAtlasAssetLoader extends AsynchronousAssetLoader<TextureAtlas, AarthSkinTextureAtlasAssetLoader.AarthSkinParameter> {
    private String textureFile, intermediateFile, mapFile;

    public AarthSkinTextureAtlasAssetLoader() {
        this(new InternalFileHandleResolver());
    }

    public AarthSkinTextureAtlasAssetLoader(FileHandleResolver resolver) {
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
    public TextureAtlas loadSync(AssetManager manager, String fileName, FileHandle file, AarthSkinParameter parameter) {
        var atlas = manager.get(textureFile, TextureAtlas.class);
        var map = manager.get(mapFile, Texture.class);
        var intermediate = manager.get(intermediateFile, Texture.class);

        if (!intermediate.getTextureData().isPrepared())
            intermediate.getTextureData().prepare();

        if (!map.getTextureData().isPrepared())
            map.getTextureData().prepare();

        var intermediatePixmap = intermediate.getTextureData().consumePixmap();
        var mapPixmap = map.getTextureData().consumePixmap();

        var textureMap = new ObjectMap<Texture, Texture>();

        for (var texture : atlas.getTextures()) {
            if (!texture.getTextureData().isPrepared())
                texture.getTextureData().prepare();

            var texturePixmap = texture.getTextureData().consumePixmap();
            var secondStepPixmap = convertToIntermediate(texturePixmap, intermediatePixmap);
            var finalPixmap = convertToFinal(mapPixmap, secondStepPixmap, parameter != null && parameter.keepAlphaFromSource);

            textureMap.put(texture, new Texture(finalPixmap));
        }

        for (var region : atlas.getRegions()) {
            var newTexture = textureMap.get(region.getTexture());
            region.setTexture(newTexture);
        }

        return atlas;
    }

    private Pixmap convertToIntermediate(Pixmap texturePixmap, Pixmap intermediatePixmap) {
        var pixmap = new Pixmap(texturePixmap.getWidth(), texturePixmap.getHeight(), texturePixmap.getFormat());

        var tmpPoint = new GridPoint2();

        for (int x = 0; x < texturePixmap.getWidth(); x++) {
            for (int y = 0; y < texturePixmap.getHeight(); y++) {
                var color = texturePixmap.getPixel(x, y);

                if (color == 0)
                    continue;

                var coordinate = findCoordinatesOfColor(intermediatePixmap, color, tmpPoint);
                if (coordinate == null)
                    throw new IllegalArgumentException("Couldn't find color " + new Color(color) + " on intermediate map. (" + x + " / " + y + ")");

                pixmap.setColor(coordinate.x / 255f, coordinate.y / 255f, 0, 1);
                pixmap.drawPixel(x, y);
            }
        }
        return pixmap;
    }

    private GridPoint2 findCoordinatesOfColor(Pixmap pixmap, int color, GridPoint2 tmp) {
        for (int x = 0; x < pixmap.getWidth(); x++) {
            for (int y = 0; y < pixmap.getHeight(); y++) {
                if (pixmap.getPixel(x, y) == color)
                    return tmp.set(x, y);
            }
        }

        return null;
    }

    private Pixmap convertToFinal(Pixmap mapPixmap, Pixmap texturePixmap, boolean keepAlphaFromSource) {
        var finalPixmap = new Pixmap(texturePixmap.getWidth(), texturePixmap.getHeight(), texturePixmap.getFormat());

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

                var a = ((keepAlphaFromSource ? color : mapColor) & 0x000000ff) / 255f;

                finalPixmap.setColor(r, g, b, a);
                finalPixmap.drawPixel(x, y);
            }
        }

        return finalPixmap;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, AarthSkinParameter parameter) {
        var descriptors = new Array<AssetDescriptor>();

        var content = file.readString().split("\n");
        if (content.length != 3)
            throw new GdxRuntimeException(new IllegalArgumentException("Aarth skin file should contain two lines: \nsource\nintermediate\nmap"));

        descriptors.add(new AssetDescriptor<>(resolve(content[0].trim()), TextureAtlas.class, parameter != null ? parameter.textureAtlasParameter : null));
        descriptors.add(new AssetDescriptor<>(resolve(content[1].trim()), Texture.class, parameter != null ? parameter.intermediateTextureParameter : null));
        descriptors.add(new AssetDescriptor<>(resolve(content[2].trim()), Texture.class, parameter != null ? parameter.mapTextureParameter : null));

        return descriptors;
    }

    @Data
    public static class AarthSkinParameter extends AssetLoaderParameters<TextureAtlas> {
        private boolean keepAlphaFromSource;

        private TextureLoader.TextureParameter intermediateTextureParameter, mapTextureParameter;
        private TextureAtlasLoader.TextureAtlasParameter textureAtlasParameter;
    }
}
