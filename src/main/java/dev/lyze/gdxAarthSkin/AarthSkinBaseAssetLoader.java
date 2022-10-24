package dev.lyze.gdxAarthSkin;

import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.GridPoint2;
import lombok.var;

public abstract class AarthSkinBaseAssetLoader<TFormat, TParameters extends AssetLoaderParameters<TFormat>> extends AsynchronousAssetLoader<TFormat, TParameters> {
    public AarthSkinBaseAssetLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    protected Pixmap convert(Texture texture, Texture intermediate, Texture map, boolean keepAlphaFromSource) {
        if (!texture.getTextureData().isPrepared())
            texture.getTextureData().prepare();

        if (!intermediate.getTextureData().isPrepared())
            intermediate.getTextureData().prepare();

        if (!map.getTextureData().isPrepared())
            map.getTextureData().prepare();

        var texturePixmap = texture.getTextureData().consumePixmap();
        var intermediatePixmap = intermediate.getTextureData().consumePixmap();
        var mapPixmap = map.getTextureData().consumePixmap();
        var secondStepPixmap = convertToIntermediate(texturePixmap, intermediatePixmap);

        return convertToFinal(secondStepPixmap, mapPixmap, keepAlphaFromSource);
    }

    protected Pixmap convertToIntermediate(Pixmap texturePixmap, Pixmap intermediatePixmap) {
        var pixmap = new Pixmap(texturePixmap.getWidth(), texturePixmap.getHeight(), texturePixmap.getFormat());

        var tmpPoint = new GridPoint2();

        for (int x = 0; x < texturePixmap.getWidth(); x++) {
            for (int y = 0; y < texturePixmap.getHeight(); y++) {
                var color = texturePixmap.getPixel(x, y);

                if (color == 0)
                    continue;

                var coordinate = findCoordinatesOfColor(intermediatePixmap, color, tmpPoint);
                if (coordinate == null)
                    throw new IllegalArgumentException("Couldn't find color " + new Color(color) + " on intermediate map. (" + x + "/" + y + ")");

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

    protected Pixmap convertToFinal(Pixmap texturePixmap, Pixmap mapPixmap, boolean keepAlphaFromSource) {
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
}
