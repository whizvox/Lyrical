package me.whizvox.lyrical.graphics;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import me.whizvox.lyrical.Lyrical;

import java.util.HashMap;
import java.util.Map;

public class GraphicsManager extends ApplicationAdapter {

  private Map<Integer, BitmapFont> fonts;
  private Viewport viewport;
  private SpriteBatch batch;
  private ShapeRenderer shapeRenderer;

  public GraphicsManager() {
    fonts = new HashMap<>();
    batch = new SpriteBatch();
    shapeRenderer = new ShapeRenderer();
    viewport = new FitViewport(Lyrical.WIDTH, Lyrical.HEIGHT);
    viewport.getCamera().translate((float)Lyrical.WIDTH / 2, (float)Lyrical.HEIGHT / 2, 0);
    viewport.getCamera().update();
  }

  public SpriteBatch getBatch() {
    return batch;
  }

  public ShapeRenderer getShapeRenderer() {
    return shapeRenderer;
  }

  public void registerFont(int id, BitmapFont font) {
    fonts.computeIfPresent(id, (_id, _font) -> {
      _font.dispose();
      return font;
    });
    fonts.put(id, font);
  }

  public BitmapFont getFont(int fontId) {
    return fonts.get(fontId);
  }

  @Override
  public void resize(int width, int height) {
    viewport.update(width, height);
    batch.setProjectionMatrix(viewport.getCamera().combined);
    shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
  }

  @Override
  public void create() {
    FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/notosans/NotoSans-Regular.ttf"));
    FreeTypeFontGenerator.FreeTypeFontParameter par = new FreeTypeFontGenerator.FreeTypeFontParameter();
    par.size = 24;
    par.borderColor = Color.BLACK;
    par.borderWidth = 2;
    BitmapFont font = gen.generateFont(par);
    font.getData().markupEnabled = true;
    registerFont(Lyrical.FONT_DISPLAY, font);
    par.size = 14;
    par.borderWidth = 0;
    BitmapFont uiFont = gen.generateFont(par);
    uiFont.getData().markupEnabled = true;
    registerFont(Lyrical.FONT_UI, uiFont);
  }

  @Override
  public void render() {
    if (Gdx.input.justTouched()) {
      Vector2 mousePos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
      viewport.unproject(mousePos);
    }
  }

  @Override
  public void dispose() {
    fonts.forEach((id, font) -> font.dispose());
    batch.dispose();
    shapeRenderer.dispose();
  }

  public void drawTextBox(int fontId, TextBox tb, float disHorz, float disVert) {
    fonts.get(fontId).draw(batch, tb.glyphLayout, tb.outerBounds.x + disHorz, tb.textPosition.y + tb.glyphLayout.height + disVert);
  }

  public void drawTextBox(int fontId, TextBox tb) {
    fonts.get(fontId).draw(batch, tb.glyphLayout, tb.outerBounds.x, tb.textPosition.y + tb.glyphLayout.height);
  }

}