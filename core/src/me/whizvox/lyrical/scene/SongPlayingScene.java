package me.whizvox.lyrical.scene;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import me.whizvox.lyrical.Lyrical;
import me.whizvox.lyrical.graphics.GraphicsManager;
import me.whizvox.lyrical.graphics.TextAlign;
import me.whizvox.lyrical.graphics.TextBox;
import me.whizvox.lyrical.song.Line;
import me.whizvox.lyrical.song.Song;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SongPlayingScene extends ApplicationAdapter {

  private GraphicsManager gm;
  private Song song;
  private TextBox[] lineTbs;
  private TextBox finishedTb;
  private Texture background;
  private Rectangle bgRegion;

  private final float nextLineDeltaDefault = 0.05f;
  private float nextLineProgress;
  private float nextLineDelta;
  private int lastIndex;

  public SongPlayingScene(GraphicsManager gm) {
    this.gm = gm;
  }

  @Override
  public void create() {
    Path songPath = Paths.get((String)Lyrical.getInstance().getTransitionData(), "song.ini");

    finishedTb = TextBox.create(gm.getFont(Lyrical.FONT_UI), "Playback has finished. Press [GRAY][[ESC][] to go back.", new Rectangle(0, 10, Lyrical.WIDTH, Lyrical.HEIGHT - 20), TextAlign.BOTTOM_CENTER.value, Color.YELLOW, false, null);

    try {
      song = Song.readSong(songPath);
      if (song.lines.isEmpty()) {
        Lyrical.getInstance().switchScene(Lyrical.SCENE_SONG_SELECTION);
        song = null;
        return;
      }
      lineTbs = new TextBox[song.lines.size()];
      for (int i = 0; i < lineTbs.length; i++) {
        lineTbs[i] = TextBox.create(gm.getFont(Lyrical.FONT_DISPLAY), song.lines.get(i).text, new Rectangle(
            0, 0, Lyrical.WIDTH, Lyrical.HEIGHT
        ), TextAlign.CENTER.value, Color.WHITE, true, null);
      }
      if (!song.metadata.background.isEmpty()) {
        background = new Texture(Gdx.files.internal(songPath.getParent().resolve(song.metadata.background).toString()));
      } else {
        background = null;
      }
    } catch (IOException e) {
      System.err.println("Could not read song at <" + songPath.toString() + ">");
      e.printStackTrace();
      Lyrical.getInstance().switchScene(Lyrical.SCENE_SONG_SELECTION);
    }
    song.play();

    if (background != null) {
      float ar = (float) background.getWidth() / background.getHeight();
      float uAr = (float) Lyrical.WIDTH / Lyrical.HEIGHT;
      bgRegion = new Rectangle(0, 0, Lyrical.WIDTH, Lyrical.HEIGHT);
      if (ar > uAr) {
        bgRegion.setY((Lyrical.HEIGHT - (uAr / ar) * Lyrical.HEIGHT) * 0.5f);
        bgRegion.setHeight((uAr / ar) * Lyrical.HEIGHT);
      } else {
        bgRegion.setX((Lyrical.WIDTH - (ar / uAr) * Lyrical.WIDTH) * 0.5f);
        bgRegion.setWidth((ar / uAr) * Lyrical.WIDTH);
      }
    }

    nextLineProgress = 0;
    nextLineDelta = nextLineDeltaDefault;
    lastIndex = -1;
  }

  @Override
  public void render() {
    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      Lyrical.getInstance().switchScene(Lyrical.SCENE_SONG_SELECTION);
    }

    if (song != null) {
      if (background != null) {
        gm.getBatch().begin();
        gm.getBatch().draw(background, bgRegion.x, bgRegion.y, bgRegion.width, bgRegion.height);
        gm.getBatch().end();
      }
      if (!song.hasFinished()) {
        song.tick();
      } else {
        gm.getBatch().begin();
        gm.drawTextBox(Lyrical.FONT_UI, finishedTb);
        gm.getBatch().end();
      }
      Line line = song.getCurrentLine();
      if (line != null) {
        final int I = song.getCurrentLineIndex();
        if (lastIndex != -1 && I != lastIndex) {
          nextLineProgress = 1.0f;
          nextLineDelta = 1000f / (song.lines.get(I).begin - song.lines.get(lastIndex).end) / 60f;
          if (nextLineDelta < nextLineDeltaDefault) {
            nextLineDelta = nextLineDeltaDefault;
          }
        }
        TextBox tb = lineTbs[I];
        ShapeRenderer sr = gm.getShapeRenderer();
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.2f, 0.2f, 0.2f, 0.5f);
        sr.rect(5, tb.textPosition.y + 20, Lyrical.WIDTH - 10, tb.glyphLayout.height + 10);
        sr.end();
        if (song.getProgress() != 0) {
          sr.begin(ShapeRenderer.ShapeType.Filled);
          sr.setColor(Color.PURPLE);
          sr.rect(tb.textPosition.x - 5, tb.textPosition.y + 20, tb.glyphLayout.width * song.getProgress() + 10, tb.glyphLayout.height + 10);
          sr.end();
        }
        gm.getBatch().begin();
        if (nextLineProgress <= 0) {
          for (int i = -1; i <= 2; i++) {
            if (I + i >= 0 && I + i < song.lines.size()) {
              gm.drawTextBox(Lyrical.FONT_DISPLAY, lineTbs[I + i], 0, -i * 50 + 25);
            }
          }
        } else {
          for (int i = -2; i <= 2; i++) {
            float p = nextLineProgress;
            if (I + i >= 0 && I + i < song.lines.size()) {
              if (i == -2) {
                p = -5 * (1 - nextLineProgress);
              } else if (i == 2) {
                p = 5 * nextLineProgress;
              }/* else if (i == 0 && line.begin < song.getTimestamp()) {
                nextLineProgress = 0;
              }*/
              gm.drawTextBox(Lyrical.FONT_DISPLAY, lineTbs[I + i], 0, -i * 50 + 25 - p * 50);
            }
          }
          //nextLineProgress -= 0.05f;
          nextLineProgress -= nextLineDelta;
        }
        gm.getBatch().end();
        lastIndex = I;
      }
    }
  }

  @Override
  public void dispose() {
    if (song != null) {
      song.dispose();
    }
  }

}
