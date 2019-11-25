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
import me.whizvox.lyrical.Reference;
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

  private List<OrderedRepoEntry> orderedRepo;
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
    {
      Map<String, Song.Metadata> repoCopy = repo.getCopy();
      repoCopy.forEach((dir, metadata) -> {
        OrderedRepoEntry entry = new OrderedRepoEntry();
        entry.dir = dir;
        entry.metadata = metadata;
        entry.zeroLength = metadata.length == 0;
        entry.noAudio = !Gdx.files.local(dir).child(metadata.filePath).exists();
        orderedRepo.add(entry);
      });
    }
    // sort by title, then artist if there's a conflict
    orderedRepo.sort(Comparator.comparing(e -> e.metadata.title + e.metadata.artist));
    songsTbs = new TextBox[orderedRepo.size()];
    for (int i = 0; i < songsTbs.length; i++) {
      OrderedRepoEntry e = orderedRepo.get(i);
      Song.Metadata c = e.metadata;
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
          (e.noAudio || e.zeroLength) ? Color.SCARLET : Color.WHITE,
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
      OrderedRepoEntry e = orderedRepo.get(selectedSong);
      Song.Metadata m = e.metadata;
      String data =
          "Title: " + m.title + '\n' +
          "Artist: " + m.artist + '\n' +
          "Language: " + m.language + '\n' +
          (e.zeroLength ? "[SCARLET]" : "[WHITE]") + "Length: " + (m.length / 60) + ':' + String.format("%02d", m.length % 60) + "[]" + '\n' +
          "Charter: " + m.charter + "\n";
      if (e.noAudio) {
        data += "\n[SCARLET]- No audio[]";
      }
      if (e.zeroLength) {
        data += "\n[SCARLET]- No lines[]";
      }
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
      gm.drawTextBox(noSongsTb);
      batch.end();
    } else {
      if (playPreviewMusic < 0 && previewMusic == null) {
        Song.Metadata c = orderedRepo.get(selectedSong).metadata;
        FileHandle songFilePath = Gdx.files.internal(orderedRepo.get(selectedSong).dir).child(c.filePath);
        if (songFilePath.exists() && !songFilePath.isDirectory()) {
          try {
            previewMusic = Gdx.audio.newMusic(songFilePath);
            previewMusic.play();
            previewMusic.setVolume(Lyrical.getInstance().getSettings().getInt(Reference.Settings.MUSIC_VOLUME, Reference.Defaults.MUSIC_VOLUME) / 100f);
            // TODO Skipping in preview music causes a lot of lag. Maybe create a bunch of small .wavs for previews?
            //previewMusic.setPosition((float) c.previewTimestamp / 1000);
          } catch (Exception e) {
            System.err.println("Could not load and/or play the following file <" + songFilePath.path() + ">");
            e.printStackTrace();
          }
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
        Lyrical.getInstance().switchScene(Lyrical.SCENE_PLAYING, orderedRepo.get(selectedSong).dir);
        return;
      } else if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
        Lyrical.getInstance().switchScene(Lyrical.SCENE_EDITOR, new Pair<>(true, orderedRepo.get(selectedSong).dir));
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
          gm.drawTextBox(tb, 0, yoff);
        }
      }
      if (previewSongTb != null) {
        gm.drawTextBox(previewSongTb);
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

  private static class OrderedRepoEntry {
    String dir;
    Song.Metadata metadata;
    boolean zeroLength;
    boolean noAudio;
    OrderedRepoEntry(String dir, Song.Metadata metadata, boolean zeroLength, boolean noAudio) {
      this.dir = dir;
      this.metadata = metadata;
      this.zeroLength = zeroLength;
      this.noAudio = noAudio;
    }
    OrderedRepoEntry() {
    }
  }

}
