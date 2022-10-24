package gdxAarthSkins.lwjgl.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import dev.lyze.gdxAarthSkin.AarthSkinTextureAssetLoader;
import gdxAarthSkins.lwjgl.LibgdxLwjglUnitTest;
import lombok.var;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class TestPlayground extends LibgdxLwjglUnitTest {
    private SpriteBatch batch;
    private FitViewport viewport;

    private Texture texture;

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
            assMan.setLoader(Texture.class, "aarth", new AarthSkinTextureAssetLoader());
            assMan.load("test.aarth", Texture.class);
            assMan.finishLoading();
            texture = assMan.get("test.aarth", Texture.class);
        });
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.TEAL);

        if (texture == null)
            return;

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        batch.draw(texture, 0, 0);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }
}
