package gdxAarthSkins.lwjgl.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import dev.lyze.gdxAarthSkin.AarthSkinTextureAtlasAssetLoader;
import gdxAarthSkins.lwjgl.LibgdxLwjglUnitTest;
import lombok.var;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class TextureAtlasExampleTest extends LibgdxLwjglUnitTest {
    private SpriteBatch batch;
    private FitViewport viewport;

    private Animation<TextureAtlas.AtlasRegion> animation;
    private float animationTime;

    @Override
    public void create() {
        super.create();

        viewport = new FitViewport(100, 100);
        batch = new SpriteBatch();
    }

    @Test
    @Tag("lwjgl")
    public void test() {
        Gdx.app.postRunnable(() -> {
            var assMan = new AssetManager();
            assMan.setLoader(TextureAtlas.class, "aarth", new AarthSkinTextureAtlasAssetLoader());
            assMan.load("Sprite.aarth", TextureAtlas.class);
            assMan.finishLoading();

            var atlas = assMan.get("Sprite.aarth", TextureAtlas.class);
            animation = new Animation<>(0.1f, atlas.findRegions("Sprite"), Animation.PlayMode.LOOP);
            animationTime = 0;
        });
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.TEAL);

        if (animation == null)
            return;

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        batch.draw(animation.getKeyFrame(animationTime += Gdx.graphics.getDeltaTime()), 0, 0);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }
}
