package me.whizvox.lyrical.scene;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import me.whizvox.lyrical.Lyrical;
import me.whizvox.lyrical.Reference;
import me.whizvox.lyrical.graphics.GraphicsManager;
import me.whizvox.lyrical.graphics.TextAlign;
import me.whizvox.lyrical.graphics.TextBox;
import me.whizvox.lyrical.song.Song;
import me.whizvox.lyrical.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class ImportScene extends ApplicationAdapter {

  private GraphicsManager gm;
  private List<String> imports;

  private List<TextBox> importsTbs;
  private TextBox noEntriesTb;
  private int selected;

  private float ePad;
  private float lHeight;

  public ImportScene(GraphicsManager gm) {
    this.gm = gm;
    imports = new ArrayList<>();
    importsTbs = new ArrayList<>();
  }

  public void refresh() {
    imports.clear();
    importsTbs.clear();
    FileHandle[] importHandles = Reference.Files.IMPORT_DIR.list();
    for (FileHandle importHandle : importHandles) {
      final String name = importHandle.name();
      importsTbs.add(TextBox.create(gm.getFont(Lyrical.FONT_UI), name, new Rectangle(
          ePad, gm.getHeight() - imports.size() * (lHeight + ePad) - lHeight, gm.getWidth() - (ePad * 2), lHeight
      ), TextAlign.LEFT.value, Color.WHITE, false, "..."));
      imports.add(name);
    }
    noEntriesTb = TextBox.create(
        gm.getFont(Lyrical.FONT_UI),
        "Nothing to import!\nPut your music in the [WHITE]/import[] folder and press [GRAY][[F5][] to refresh.",
        new Rectangle(
            0, 0, gm.getWidth(), gm.getHeight()
        ), TextAlign.CENTER.value, Color.CORAL, true, null);
    selected = 0;
  }

  @Override
  public void create() {
    ePad = gm.getWidth() / 120f;
    lHeight = gm.getFont(Lyrical.FONT_UI).getLineHeight();
    refresh();
  }

  @Override
  public void render() {
    if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
      refresh();
      selected = 0;
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      Lyrical.getInstance().switchScene(Lyrical.SCENE_TITLE);
      return;
    }

    SpriteBatch sb = gm.getBatch();
    if (imports.isEmpty()) {
      sb.begin();
      gm.drawTextBox(Lyrical.FONT_UI, noEntriesTb);
      sb.end();
    } else {
      if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
        if (++selected >= imports.size()) {
          selected = 0;
        }
      } else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
        if (--selected < 0) {
          selected = imports.size() - 1;
        }
      } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
        Lyrical.getInstance().switchScene(Lyrical.SCENE_EDITOR, new Pair<>(false, imports.get(selected)));
        return;
      }
      sb.begin();
      TextBox tb;
      for (int i = 0; i < importsTbs.size(); i++) {
        tb = importsTbs.get(i);
        if (i == selected) {
          sb.end();
          ShapeRenderer sr = gm.getShapeRenderer();
          sr.setColor(Color.BLUE);
          sr.begin(ShapeRenderer.ShapeType.Filled);
          sr.rect(
              tb.textPosition.x - ePad,
              tb.textPosition.y - ePad,
              tb.glyphLayout.width + (ePad * 2),
              tb.glyphLayout.height + (ePad * 2)
          );
          sr.end();
          sb.begin();
        }
        gm.drawTextBox(Lyrical.FONT_UI, tb);
      }
      sb.end();
    }
  }

}
