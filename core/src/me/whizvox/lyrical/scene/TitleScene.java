package me.whizvox.lyrical.scene;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import me.whizvox.lyrical.Lyrical;
import me.whizvox.lyrical.Reference;
import me.whizvox.lyrical.graphics.GraphicsManager;
import me.whizvox.lyrical.graphics.TextAlign;
import me.whizvox.lyrical.graphics.TextBox;

public class TitleScene extends ApplicationAdapter {

  private GraphicsManager gm;

  private TextBox[] menuTbs;
  private int selected;
  private Texture logoTex;
  private TextBox versionTb;

  public TitleScene(GraphicsManager gm) {
    this.gm = gm;
  }

  @Override
  public void create() {
    selected = 0;
    menuTbs = new TextBox[3];
    menuTbs[0] = TextBox.create(gm.getFont(Lyrical.FONT_DISPLAY), "Select", new Rectangle(
        0, (float)Lyrical.HEIGHT / 2 - 30, Lyrical.WIDTH, 25
    ), TextAlign.CENTER.value, Color.WHITE, false, null);
    menuTbs[1] = TextBox.create(gm.getFont(Lyrical.FONT_DISPLAY), "Import", new Rectangle(
        0, (float)Lyrical.HEIGHT / 2 - 65, Lyrical.WIDTH, 25
    ), TextAlign.CENTER.value, Color.WHITE, false, null);
    menuTbs[2] = TextBox.create(gm.getFont(Lyrical.FONT_DISPLAY), "Quit", new Rectangle(
        0, (float)Lyrical.HEIGHT / 2 - 100, Lyrical.WIDTH, 25
    ), TextAlign.CENTER.value, Color.WHITE, false, null);
    logoTex = new Texture(Gdx.files.internal("logo.png"));

    versionTb = TextBox.create(gm.getFont(Lyrical.FONT_UI), "Version: [YELLOW]" + Reference.VERSION,
        new Rectangle(5, 5, Lyrical.WIDTH - 10, Lyrical.HEIGHT - 10),
        TextAlign.BOTTOM_CENTER.value, Color.WHITE, false, null);
  }

  @Override
  public void render() {
    if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
      if (++selected > menuTbs.length - 1) {
        selected = 0;
      }
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
      if (--selected < 0) {
        selected = menuTbs.length - 1;
      }
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
      switch (selected) {
        case 0:
          Lyrical.getInstance().switchScene(Lyrical.SCENE_SONG_SELECTION);
          break;
        case 1:
          Lyrical.getInstance().switchScene(Lyrical.SCENE_IMPORTS);
          break;
        case 2:
          Gdx.app.exit();
          break;
      }
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
      Lyrical.getInstance().switchScene(Lyrical.SCENE_DEBUG);
    }

    ShapeRenderer sr = gm.getShapeRenderer();
    sr.setColor(Color.ORANGE);
    sr.begin(ShapeRenderer.ShapeType.Filled);
    TextBox stb = menuTbs[selected];
    sr.rect(stb.textPosition.x - 5, stb.textPosition.y - 5, stb.glyphLayout.width + 10, stb.glyphLayout.height + 10);
    sr.end();
    SpriteBatch sb = gm.getBatch();
    sb.begin();
    sb.draw(logoTex, Lyrical.WIDTH / 2.0f - logoTex.getWidth() / 2.0f, Lyrical.HEIGHT / 2.0f + 10 / 2.0f);
    for (TextBox tb : menuTbs) {
      gm.drawTextBox(Lyrical.FONT_DISPLAY, tb);
    }
    gm.drawTextBox(Lyrical.FONT_UI, versionTb);
    sb.end();
  }

}
