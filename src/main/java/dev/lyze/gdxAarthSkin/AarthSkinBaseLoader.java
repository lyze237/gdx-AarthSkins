package dev.lyze.gdxAarthSkin;

import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.GridPoint2;
import lombok.var;

import java.util.StringTokenizer;

public abstract class AarthSkinBaseLoader<TFormat, TParameters extends AssetLoaderParameters<TFormat>> extends AsynchronousAssetLoader<TFormat, TParameters> {
    public AarthSkinBaseLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    protected static FileHandle getRelativeFileHandle(FileHandle file, String path) {
        var tokenizer = new StringTokenizer(path, "\\/");

        var result = file.parent();

        while (tokenizer.hasMoreElements()) {
            var token = tokenizer.nextToken();

            result = token.equals("..") ? result.parent() : result.child(token);
        }

        return result;
    }

    protected Pixmap convert(Texture texture, Texture map, Texture lookup) {
        if (!texture.getTextureData().isPrepared())
            texture.getTextureData().prepare();

        if (!map.getTextureData().isPrepared())
            map.getTextureData().prepare();

        if (!lookup.getTextureData().isPrepared())
            lookup.getTextureData().prepare();

        var sourcePixmap = texture.getTextureData().consumePixmap();
        var mapPixmap = map.getTextureData().consumePixmap();
        var lookupPixmap = lookup.getTextureData().consumePixmap();

        var overlayPixmap = convertToOverlay(sourcePixmap, mapPixmap);
        var finalPixmap = convertToResult(overlayPixmap, lookupPixmap);

        return finalPixmap;
    }

    protected Pixmap convertToOverlay(Pixmap source, Pixmap map) {
        var overlay = new Pixmap(source.getWidth(), source.getHeight(), source.getFormat());

        var tmpPoint = new GridPoint2();

        for (int x = 0; x < source.getWidth(); x++) {
            for (int y = 0; y < source.getHeight(); y++) {
                var color = source.getPixel(x, y);

                if (color == 0)
                    continue;

                var coordinate = findCoordinatesOfColor(map, color, tmpPoint);
                if (coordinate == null)
                    throw new IllegalArgumentException("Couldn't find color " + new Color(color) + " on intermediate map. (" + x + "/" + y + ")");

                overlay.setColor(coordinate.x / 255f, coordinate.y / 255f, 0, 1);
                overlay.drawPixel(x, y);
            }
        }

        return overlay;
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

    protected Pixmap convertToResult(Pixmap source, Pixmap lookup) {
        var result = new Pixmap(source.getWidth(), source.getHeight(), source.getFormat());

        for (int x = 0; x < source.getWidth(); x++) {
            for (int y = 0; y < source.getHeight(); y++) {
                var color = source.getPixel(x, y);

                if (color == 0)
                    continue;

                var xOnMap = (color & 0xff000000) >>> 24; // r
                var yOnMap = (color & 0x00ff0000) >>> 16; // g

                var mapColor = lookup.getPixel(xOnMap, yOnMap);

                var r = ((mapColor & 0xff000000) >>> 24) / 255f;
                var g = ((mapColor & 0x00ff0000) >>> 16) / 255f;
                var b = ((mapColor & 0x0000ff00) >>> 8) / 255f;
                var a = ((mapColor) & 0x000000ff) / 255f;

                result.setColor(r, g, b, a);
                result.drawPixel(x, y);
            }
        }

        return result;
    }
}
