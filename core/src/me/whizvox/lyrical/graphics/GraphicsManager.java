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
import me.whizvox.lyrical.Reference;
import me.whizvox.lyrical.Settings;

import java.util.HashMap;
import java.util.Map;

public class GraphicsManager extends ApplicationAdapter {

  private Map<Integer, BitmapFont> fonts;
  private Viewport viewport;
  private SpriteBatch batch;
  private ShapeRenderer shapeRenderer;

  private int width, height;

  public GraphicsManager() {
    fonts = new HashMap<>();
    batch = new SpriteBatch();
    shapeRenderer = new ShapeRenderer();
    viewport = new FitViewport(Reference.Defaults.RESOLUTION_WIDTH, Reference.Defaults.RESOLUTION_HEIGHT);
  }

  public void setResolution(int width, int height) {
    this.width = width;
    this.height = height;
    viewport.setWorldSize(width, height);
    viewport.getCamera().position.set(width / 2f, height / 2f, 0);
    viewport.getCamera().update();
    resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

    int displayFontSize = (int) (width / (100f / 3));
    int displayBorderWidth = (int) (width / 400f);
    int uiFontSize = (int) (width / (400f / 7));

    fonts.forEach((id, font) -> {
      font.dispose();
    });
    fonts.clear();
    FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/notosans/NotoSans-Regular.ttf"));
    FreeTypeFontGenerator.FreeTypeFontParameter par = new FreeTypeFontGenerator.FreeTypeFontParameter();
    par.size = displayFontSize;
    par.borderColor = Color.BLACK;
    par.borderWidth = displayBorderWidth;
    BitmapFont font = gen.generateFont(par);
    font.getData().markupEnabled = true;
    registerFont(Lyrical.FONT_DISPLAY, font);
    par.size = uiFontSize;
    par.borderWidth = 0;
    BitmapFont uiFont = gen.generateFont(par);
    uiFont.getData().markupEnabled = true;
    registerFont(Lyrical.FONT_UI, uiFont);
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
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
    Settings settings = Lyrical.getInstance().getSettings();

    int width = settings.getInt(Reference.Settings.RESOLUTION_WIDTH, Reference.Defaults.RESOLUTION_WIDTH);
    int height = settings.getInt(Reference.Settings.RESOLUTION_HEIGHT, Reference.Defaults.RESOLUTION_HEIGHT);

    setResolution(width, height);
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

  public void drawTextBox(TextBox tb, float dx, float dy) {
    tb.font.draw(batch, tb.glyphLayout, tb.outerBounds.x + dx, tb.textPosition.y + tb.glyphLayout.height + dy);
  }

  public void drawTextBox(TextBox tb) {
    tb.font.draw(batch, tb.glyphLayout, tb.outerBounds.x, tb.textPosition.y + tb.glyphLayout.height);
  }

}
