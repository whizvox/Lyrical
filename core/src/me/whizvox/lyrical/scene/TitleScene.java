package me.whizvox.lyrical.scene;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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

  private float ePad;
  private float logoSize;
  private Rectangle logoBounds;

  public TitleScene(GraphicsManager gm) {
    this.gm = gm;
  }

  @Override
  public void create() {
    ePad = gm.getWidth() / 120f;

    selected = 0;
    BitmapFont font = gm.getFont(Lyrical.FONT_DISPLAY);
    float eHeight = font.getLineHeight();
    menuTbs = new TextBox[4];
    menuTbs[0] = TextBox.create(font, "Select", new Rectangle(
        0, gm.getHeight() / 2f - ePad * 6, gm.getWidth(), eHeight
    ), TextAlign.CENTER.value, Color.WHITE, false, null);
    menuTbs[1] = TextBox.create(font, "Import", new Rectangle(
        0, menuTbs[0].textPosition.y - (eHeight + ePad), gm.getWidth(), eHeight
    ), TextAlign.CENTER.value, Color.WHITE, false, null);
    menuTbs[2] = TextBox.create(font, "Settings", new Rectangle(
        0, menuTbs[1].textPosition.y - (eHeight + ePad), gm.getWidth(), eHeight
    ), TextAlign.CENTER.value, Color.WHITE, false, null);
    menuTbs[3] = TextBox.create(font, "Quit", new Rectangle(
        0, menuTbs[2].textPosition.y - (eHeight + ePad), gm.getWidth(), eHeight
    ), TextAlign.CENTER.value, Color.WHITE, false, null);

    versionTb = TextBox.create(gm.getFont(Lyrical.FONT_UI), "Version: [YELLOW]" + Reference.VERSION,
        new Rectangle(ePad, ePad, gm.getWidth() - (ePad * 2), gm.getHeight() - (ePad * 2)),
        TextAlign.BOTTOM_CENTER.value, Color.WHITE, false, null);

    logoTex = new Texture(Gdx.files.internal("logo.png"));
    float ar = (float)logoTex.getWidth() / logoTex.getHeight();
    logoSize = gm.getWidth() * 0.4f; // logo's width takes up 40% of the screen
    logoBounds = new Rectangle(
        gm.getWidth() / 2f - logoSize / 2f, gm.getHeight() * 0.7f - (logoSize / ar) / 2f, logoSize, logoSize / ar
    );
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
          Lyrical.getInstance().switchScene(Lyrical.SCENE_SETTINGS);
          break;
        case 3:
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
    sr.rect(
        stb.textPosition.x - ePad,
        stb.textPosition.y - ePad,
        stb.glyphLayout.width + (ePad * 2),
        stb.glyphLayout.height + (ePad * 2)
    );
    sr.end();
    SpriteBatch sb = gm.getBatch();
    sb.begin();
    sb.draw(logoTex, logoBounds.x, logoBounds.y, logoBounds.width, logoBounds.height);
    for (TextBox tb : menuTbs) {
      gm.drawTextBox(tb);
    }
    gm.drawTextBox(versionTb);
    sb.end();
  }

}
