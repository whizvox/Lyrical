package me.whizvox.lyrical.scene;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import me.whizvox.lyrical.Lyrical;
import me.whizvox.lyrical.graphics.GraphicsManager;
import me.whizvox.lyrical.graphics.TextAlign;
import me.whizvox.lyrical.graphics.TextBox;
import me.whizvox.lyrical.song.Song;
import me.whizvox.lyrical.song.SongsRepository;
import me.whizvox.lyrical.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class SongSelectionScene extends ApplicationAdapter {

  private GraphicsManager gm;
  private SpriteBatch batch;
  private ShapeRenderer srenderer;
  private SongsRepository repo;

  private List<Map.Entry<String, Song.Metadata>> orderedRepo;
  private TextBox[] songsTbs;
  private TextBox previewSongTb;
  private int selectedSong;
  private Music previewMusic;
  private int playPreviewMusic;

  private TextBox noSongsTb;
  
  private float textboxHeight;
  private float ePad;
  private final float listSize;

  public SongSelectionScene(GraphicsManager gm, SongsRepository repo) {
    this.gm = gm;
    batch = gm.getBatch();
    srenderer = gm.getShapeRenderer();
    this.repo = repo;
    orderedRepo = new ArrayList<>();
    previewMusic = null;
    playPreviewMusic = 0;
    selectedSong = 0;

    listSize = 0.7f;
  }

  private void refresh() {
    orderedRepo.clear();
    orderedRepo.addAll(repo.getCopy().entrySet());
    orderedRepo.sort(Comparator.comparing(o -> o.getValue().title));
    songsTbs = new TextBox[orderedRepo.size()];
    for (int i = 0; i < songsTbs.length; i++) {
      Song.Metadata c = orderedRepo.get(i).getValue();
      songsTbs[i] = TextBox.create(
          gm.getFont(Lyrical.FONT_DISPLAY),
          c.title,
          new Rectangle(
              ePad,
              gm.getHeight() - (i + 1) * textboxHeight,
              (float)gm.getWidth() * listSize - (ePad * 2),
              textboxHeight
          ),
          TextAlign.LEFT.value,
          c.length == 0 ? Color.RED : Color.WHITE,
          false,
          "..."
      );
    }
    updatePreviewTextBox();
  }

  private void updatePreviewTextBox() {
    if (songsTbs.length == 0) {
      previewSongTb = null;
    } else {
      Song.Metadata c = orderedRepo.get(selectedSong).getValue();
      String data =
          "Title: " + c.title + '\n' +
          "Artist: " + c.artist + '\n' +
          "Language: " + c.language + '\n' +
          (c.length == 0 ? "[RED]" : "[WHITE]") + "Length: " + (c.length / 60) + ':' + String.format("%02d", c.length % 60) + "[]" + '\n' +
          "Charter: " + c.charter;
      previewSongTb = TextBox.create(
          gm.getFont(Lyrical.FONT_UI),
          data,
          new Rectangle((float)gm.getWidth() * listSize + ePad, ePad, (float)gm.getWidth() * (1 - listSize) - (ePad * 2), gm.getHeight() - (ePad * 2)),
          TextAlign.TOP_LEFT.value,
          Color.WHITE,
          true,
          null
      );
      if (previewMusic != null && previewMusic.isPlaying()) {
        previewMusic.stop();
      }
      // adds a 15 tick cooldown until the preview song is played
      playPreviewMusic = 15;
      previewMusic = null;
    }
  }

  @Override
  public void create() {
    textboxHeight = gm.getHeight() / 15f;
    ePad = gm.getWidth() / 120f;
    refresh();
    noSongsTb = TextBox.create(gm.getFont(Lyrical.FONT_UI), "No songs found!\nCheck the [WHITE]/songs[] folder and press [GRAY][[F5][] to refresh.", new Rectangle(
        0, 0, gm.getWidth(), gm.getHeight()
    ), TextAlign.CENTER.value, Color.CORAL, false, null);
  }

  @Override
  public void render() {
    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      Lyrical.getInstance().switchScene(Lyrical.SCENE_TITLE);
      return;
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
      try {
        repo.refresh();
        refresh();
      } catch (IOException e) {
        System.err.println("Could not refresh songs list");
        e.printStackTrace();
      }
    }

    if (orderedRepo.isEmpty()) {
      batch.begin();
      gm.drawTextBox(Lyrical.FONT_UI, noSongsTb);
      batch.end();
    } else {
      if (playPreviewMusic < 0 && previewMusic == null) {
        Song.Metadata c = orderedRepo.get(selectedSong).getValue();
        FileHandle songFilePath = Gdx.files.internal(orderedRepo.get(selectedSong).getKey()).child(c.filePath);
        try {
          previewMusic = Gdx.audio.newMusic(songFilePath);
          previewMusic.play();
          // TODO: Skipping in preview music causes a lot of lag
          //previewMusic.setPosition((float) c.previewTimestamp / 1000);
        } catch (Exception e) {
          System.err.println("Could not load and/or play the following file <" + songFilePath.path() + ">");
          e.printStackTrace();
        }
      } else {
        playPreviewMusic--;
      }

      if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
        if (++selectedSong >= orderedRepo.size()) {
          selectedSong = 0;
        }
        updatePreviewTextBox();
      } else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
        if (--selectedSong < 0) {
          selectedSong = orderedRepo.size() - 1;
        }
        updatePreviewTextBox();
      } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
        if (orderedRepo.get(selectedSong).getValue().length > 0) {
          Lyrical.getInstance().switchScene(Lyrical.SCENE_PLAYING, orderedRepo.get(selectedSong).getKey());
          return;
        }
      } else if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
        Lyrical.getInstance().switchScene(Lyrical.SCENE_EDITOR, new Pair<>(true, orderedRepo.get(selectedSong).getKey()));
        return;
      }

      float yoff = selectedSong * textboxHeight - gm.getHeight() / 2f;
      batch.begin();
      for (int i = 0; i < songsTbs.length; i++) {
        TextBox tb = songsTbs[i];
        if (tb.textPosition.y + yoff <= gm.getHeight() + textboxHeight && tb.textPosition.y + yoff >= -textboxHeight) {
          if (i == selectedSong) {
            batch.end();
            srenderer.setColor(Color.BLUE);
            srenderer.begin(ShapeRenderer.ShapeType.Filled);
            srenderer.rect(
                tb.textPosition.x - ePad,
                tb.textPosition.y - ePad + yoff,
                tb.glyphLayout.width + (ePad * 2),
                tb.glyphLayout.height + (ePad * 2)
            );
            srenderer.end();
            batch.begin();
          }
          gm.drawTextBox(Lyrical.FONT_DISPLAY, tb, 0, yoff);
        }
      }
      if (previewSongTb != null) {
        gm.drawTextBox(Lyrical.FONT_UI, previewSongTb);
      }
      batch.end();
    }
  }

  @Override
  public void dispose() {
    if (previewMusic != null) {
      if (previewMusic.isPlaying()) {
        previewMusic.stop();
      }
      previewMusic.dispose();
    }
  }

}
