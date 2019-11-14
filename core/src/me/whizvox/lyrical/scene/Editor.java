package me.whizvox.lyrical.scene;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import me.whizvox.lyrical.Lyrical;
import me.whizvox.lyrical.Reference;
import me.whizvox.lyrical.TextEntryProcessor;
import me.whizvox.lyrical.graphics.GraphicsManager;
import me.whizvox.lyrical.graphics.TextAlign;
import me.whizvox.lyrical.graphics.TextBox;
import me.whizvox.lyrical.song.Line;
import me.whizvox.lyrical.song.Song;
import me.whizvox.lyrical.util.InputUtils;
import me.whizvox.lyrical.util.Pair;
import me.whizvox.lyrical.util.StringUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

public class Editor extends ApplicationAdapter {

  private GraphicsManager gm;
  private boolean newSong;
  private Song song;

  private int timelinePos;
  private LinkedList<TextBox> linesTbs;

  private EditingState editingState;
  private int selectedLine;

  private TextEntryProcessor tep;
  private TextBox lineEditTb;
  private Rectangle lineEditCursor;

  private Line newInsertLine;

  private int selectedSongMeta;
  private boolean editingSongMeta;
  private TextBox[] songMetaTbs;
  private Rectangle songMetaCursorPos;
  private LineMoveMode lineMoveMode;
  private int lineMoveAmount;

  private String dirPath;

  private TextBox savedTb;
  private int savedTbLife;
  private TextBox previewSetTb;
  private int previewSetTbLife;
  private boolean forceDrawCursorBar;

  private boolean dirty;
  private boolean markForExit;
  private TextBox unsavedTb;

  private TextBox helpTb;
  private static final String HELP_TEXT =
      "[YELLOW]-- HELP MENU -- (Press [GRAY][[Escape][] to exit)\n" +
      "[]There are 2 different views available when editing a song.\n\n" +
      "* [GOLD]Timeline[]: Allows editing of the song's lines and lyrics\n" +
      "  * [GRAY][[P][]: Set the preview point to the timeline's current position.\n" +
      "  * Several different modes during this view:\n" +
      "    * [GREEN]Browsing[]: Can browse through the song. [GRAY][[Space][] to play/pause, [GRAY][[Left][] and [GRAY][[Right][] to skip by 5 seconds.\n" +
      "    * [GREEN]Selecting[]: Press [GRAY][[Enter][] to enter. Allows selecting of different lines with [GRAY][[Left][] and [GRAY][[Right][].\n" +
      "    * [GREEN]Editing[]: Press [GRAY][[Enter][] once in selecting line mode. Allows editing of the selected line's text. Press [GRAY][[Enter][] to confirm or [GRAY][[Escape][] to cancel.\n" +
      "    * [GREEN]Inserting[]: Press [GRAY][[I][] during browsing mode to enter. Hold [GRAY][[Enter][] to add a new line. Release to mark the endpoint.\n" +
      "    * [GREEN]Moving[]: Can enter when in Selecting mode. Allows manual shifting of the selected line's time points.\n" +
      "      * [GRAY][[Ctrl][]+[GRAY][[Left][]: Edit the begin point\n" +
      "      * [GRAY][[Ctrl][]+[GRAY][[Down / Up][]: Edit both the begin and end points, moving the entire line\n" +
      "      * [GRAY][[Ctrl][]+[GRAY][[Right][]: Edit the end point\n" +
      "      * Default shift amount is 10ms. Hold [GRAY][[Alt][] to shift by 1ms, or hold [GRAY][[Shift][] to shift by 100ms.\n" +
      "      * Once you're done, press [GRAY][[Enter][] to save your changes, or [GRAY][[Escape][] to revert back.\n" +
      "* [GOLD]Metadata[]: Allows editing of the song's metadata, like the title, artist, etc.\n" +
      "  * Use [GRAY][[Up][] and [GRAY][[Down][] to select the different options.\n" +
      "  * Press [GRAY][[Enter][] to edit a piece of selected metadata. Press [GRAY][[Enter][] to confirm or [GRAY][[Escape][] to cancel.\n\n" +
      "[GRAY][[Ctrl][]+[GRAY][[S][]: Save the song. This will update the folder name if need be.";

  private static final int
      META_TITLE = 0,
      META_ARTIST = 1,
      META_LANGUAGE = 2,
      META_CHARTER = 3;

  public Editor(GraphicsManager gm) {
    this.gm = gm;
  }

  private void updateLineEditTextBox() {
    lineEditTb = TextBox.create(gm.getFont(Lyrical.FONT_UI), tep.getText().toString(), new Rectangle(5, Lyrical.HEIGHT / 2f + 5, Lyrical.WIDTH - 10, Lyrical.HEIGHT / 2f - 10), TextAlign.TOP_LEFT.value, Color.WHITE, true, null);
  }

  private void updateLineEditCursor() {
    Vector2 pos = tep.getOnScreenCursorPos(gm, lineEditTb);
    lineEditCursor = new Rectangle(lineEditTb.textPosition.x + pos.x, lineEditTb.textPosition.y + lineEditTb.glyphLayout.height + pos.y, 2, gm.getFont(Lyrical.FONT_UI).getLineHeight());
  }

  private TextBox createTextBoxForLine(Line line) {
    return TextBox.create(gm.getFont(Lyrical.FONT_UI), line.text, new Rectangle(
        Lyrical.WIDTH / 2f,
        25,
        ((line.end - line.begin) * Lyrical.WIDTH) / 10000f,
        Lyrical.HEIGHT / 2f - 50
    ), TextAlign.TOP_LEFT.value, Color.WHITE, true, null);
  }

  private void updateSongMetaTextBox(int i, boolean forEditing) {
    String s;
    if (forEditing) {
      s = tep.getText().toString();
    } else {
      switch (i) {
        case META_TITLE:
          s = song.metadata.title;
          break;
        case META_ARTIST:
          s = song.metadata.artist;
          break;
        case META_LANGUAGE:
          s = song.metadata.language;
          break;
        case META_CHARTER:
          s = song.metadata.charter;
          break;
        default:
          s = "(Unknown)";
      }
    }
    songMetaTbs[i * 2 + 1] = TextBox.create(gm.getFont(Lyrical.FONT_UI), s, new Rectangle(5, Lyrical.HEIGHT * (0.7f - i * 0.1f), Lyrical.WIDTH - 10, 25), TextAlign.TOP_LEFT.value, Color.WHITE, true, null);
  }

  private void updateSongMetaCursorPos() {
    TextBox tb = songMetaTbs[selectedSongMeta * 2 + 1];
    Vector2 pos = tep.getOnScreenCursorPos(gm, tb);
    songMetaCursorPos = new Rectangle(tb.textPosition.x + pos.x, tb.textPosition.y + tb.glyphLayout.height + pos.y, 2, gm.getFont(Lyrical.FONT_UI).getLineHeight());
  }

  private void moveSelectedLine(boolean left) {
    int amount = left ? -10 : 10; // default: 10ms
    if (InputUtils.isMetaKeyPressed(InputUtils.META_ALT)) {
      amount /= 10; // w/ alt: 1ms
    } else if (InputUtils.isMetaKeyPressed(InputUtils.META_SHIFT)) {
      amount *= 10; // w/ shift: 100ms
    }
    Line sLine = song.lines.get(selectedLine);
    switch (lineMoveMode) {
      case BEGIN:
        sLine.begin += amount;
        break;
      case WHOLE:
        sLine.begin += amount;
        sLine.end += amount;
        break;
      case END:
        sLine.end += amount;
        break;
    }
    lineMoveAmount += amount;
    linesTbs.set(selectedLine, createTextBoxForLine(sLine));
    markDirty();
  }

  private void saveSong() {
    String newFilePath = StringUtils.filterFileName(
        song.metadata.artist + " - " + song.metadata.title + " (" + song.metadata.charter + ")"
    );
    FileHandle newDir = Reference.Files.SONGS_DIR.child(newFilePath);
    if (!newDir.path().equals(dirPath)) {
      FileHandle oldDir = Gdx.files.local(dirPath);
      //oldDir.moveTo(Reference.Files.SONGS_DIR); // doesn't work, literally moves the directory itself
      try {
        Files.move(Paths.get(oldDir.path()), Paths.get(newDir.path()));
      } catch (IOException e) {
        System.err.println("Could not save song at <" + newDir.path() + ">");
        throw new RuntimeException(e);
      }
      dirPath = newDir.path();
    }
    try (FileWriter writer = new FileWriter(Gdx.files.local(dirPath).child("song.ini").file())) {
      song.save(writer);
      System.out.println("Song saved at <" + dirPath + ">");
      savedTbLife = 90;
      dirty = false;
    } catch (IOException e) {
      System.err.println("Could not save song");
      e.printStackTrace();
    }
  }

  private void markDirty() {
    dirty = true;
  }

  @Override
  public void create() {
    Object transData = Lyrical.getInstance().getTransitionData();
    newSong = false;
    if (transData instanceof Pair) {
      Pair<Boolean, String> data = (Pair<Boolean, String>) transData;
      if (data.first) { // true = editing a currently-existing song
        dirty = false;
        dirPath = data.second;
        Path songPath = Paths.get(data.second, "song.ini");
        try {
          song = Song.readSong(songPath);
          Song.swapWithWav(data.second, song);
        } catch (IOException e) {
          System.err.println("Could not read from <" + songPath.toString() + ">");
          e.printStackTrace();
          Lyrical.getInstance().switchScene(Lyrical.SCENE_SONG_SELECTION);
        }
      } else { // false = importing a music file and creating a new song
        markDirty();
        Pair<String, Song> pair = Song.createSong(data.second);
        if (pair == null) {
          Lyrical.getInstance().switchScene(Lyrical.SCENE_TITLE);
          song = new Song();
          return;
        }
        song = pair.second;
        Song.swapWithWav(pair.first, song);
        dirPath = pair.first;
      }
    } else {
      throw new RuntimeException("Transition data must be a pair (boolean, String): " + transData);
    }

    timelinePos = 0;
    linesTbs = new LinkedList<>();
    song.lines.forEach(line -> linesTbs.add(createTextBoxForLine(line)));

    editingState = EditingState.BROWSING;
    selectedLine = -1;

    tep = new TextEntryProcessor();
    Gdx.input.setInputProcessor(tep);
    updateLineEditTextBox();
    updateLineEditCursor();

    newInsertLine = null;

    selectedSongMeta = 0;
    editingSongMeta = false;
    songMetaTbs = new TextBox[8];

    for (int i = 0; i < 4; i++) {
      String s;
      switch (i) {
        case META_TITLE:
          s = "Title";
          break;
        case META_ARTIST:
          s = "Artist";
          break;
        case META_LANGUAGE:
          s = "Language";
          break;
        case META_CHARTER:
          s = "Charter";
          break;
        default:
          s = "(Unknown)";
      }
      songMetaTbs[i * 2] = TextBox.create(gm.getFont(Lyrical.FONT_UI), s + ":", new Rectangle(
          5, Lyrical.HEIGHT * (0.75f - i * 0.1f), Lyrical.WIDTH - 10, 25
      ), TextAlign.TOP_LEFT.value, Color.YELLOW, false, null);
      updateSongMetaTextBox(i, false);
    }
    songMetaCursorPos = new Rectangle();
    lineMoveMode = LineMoveMode.WHOLE;
    lineMoveAmount = 0;

    savedTb = TextBox.create(gm.getFont(Lyrical.FONT_DISPLAY), "Saved!", new Rectangle(
        5, 5, Lyrical.WIDTH - 10, Lyrical.HEIGHT - 10
    ), TextAlign.BOTTOM_RIGHT.value, Color.GREEN, false, null);
    savedTbLife = 0;
    previewSetTb = TextBox.create(gm.getFont(Lyrical.FONT_DISPLAY), "Preview point set!", new Rectangle(
        5, 5, Lyrical.WIDTH - 10, Lyrical.HEIGHT - 10
    ), TextAlign.BOTTOM_RIGHT.value, Color.GREEN, false, null);
    previewSetTbLife = 0;
    markForExit = false;

    forceDrawCursorBar = false;
    unsavedTb = TextBox.create(gm.getFont(Lyrical.FONT_UI),
        "[YELLOW]There are unsaved changes![] Are you sure you want to exit?\n" +
        "[GRAY][[Esc][] Cancel | [GRAY][[Enter][] Confirm and save | [GRAY][[Ctrl][]+[GRAY][[Enter][] [CORAL]Confirm without saving[]",
        new Rectangle(0, 0, Lyrical.WIDTH, Lyrical.HEIGHT), TextAlign.CENTER.value, Color.WHITE, false, null);

    helpTb = TextBox.create(gm.getFont(Lyrical.FONT_UI), HELP_TEXT, new Rectangle(5, 5, Lyrical.WIDTH - 10, Lyrical.HEIGHT - 10), TextAlign.TOP_LEFT.value, Color.WHITE, true, null);
  }

  @Override
  public void render() {
    if (markForExit) {
      if (dirty) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
          markForExit = false;
        } else if (Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
          if (!InputUtils.isMetaKeyPressed(InputUtils.META_CTRL)) {
            saveSong();
          }
          dirty = false;
        }

        gm.getBatch().begin();
        gm.drawTextBox(Lyrical.FONT_UI, unsavedTb);
        gm.getBatch().end();
      } else {
        Lyrical.getInstance().switchScene(Lyrical.SCENE_SONG_SELECTION);
      }
      return;
    }

    if (song.isPlaying() && !song.isPaused()) {
      song.tick();
    }
    timelinePos = song.getTimestamp();

    if (InputUtils.isKeyPressed(Input.Keys.S, InputUtils.META_CTRL)) {
      saveSong();
    }

    if (editingState == EditingState.HELP_MENU) {
      if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
        editingState = EditingState.BROWSING;
      }
      SpriteBatch sb = gm.getBatch();
      sb.begin();
      gm.drawTextBox(Lyrical.FONT_UI, helpTb);
      sb.end();
    } else if (editingState != EditingState.EDIT_SONG_INFO) {
      if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && editingState == EditingState.BROWSING) {
        if (song.isPlaying()) {
          if (song.isPaused()) {
            song.resume();
          } else {
            song.pause();
          }
        } else {
          song.play();
        }
      } else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
        if (editingState == EditingState.SELECT_LINE && InputUtils.isMetaKeyPressed(InputUtils.META_CTRL)) {
          editingState = EditingState.MOVE_LINE;
          lineMoveMode = LineMoveMode.BEGIN;
        } else if (editingState == EditingState.MOVE_LINE) {
          moveSelectedLine(true);
        } else {
          if (editingState == EditingState.BROWSING) {
            song.skip(song.getTimestamp() - 5000);
          } else if (editingState == EditingState.SELECT_LINE && selectedLine > 0) {
            selectedLine--;
            song.skip(song.lines.get(selectedLine).begin);
          }
        }
      } else if (editingState == EditingState.SELECT_LINE && (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.DOWN))) {
        editingState = EditingState.MOVE_LINE;
        lineMoveMode = LineMoveMode.WHOLE;
      } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
        if (editingState == EditingState.SELECT_LINE && InputUtils.isMetaKeyPressed(InputUtils.META_CTRL)) {
          editingState = EditingState.MOVE_LINE;
          lineMoveMode = LineMoveMode.END;
        } else if (editingState == EditingState.MOVE_LINE) {
          moveSelectedLine(false);
        } else {
          if (editingState == EditingState.BROWSING) {
            song.skip(song.getTimestamp() + 5000);
          } else if (editingState == EditingState.SELECT_LINE && selectedLine < song.lines.size() - 1) {
            selectedLine++;
            song.skip(song.lines.get(selectedLine).begin);
          }
        }
      } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
        switch (editingState) {
          case BROWSING:
            markForExit = true;
            return;
          case SELECT_LINE:
            editingState = EditingState.BROWSING;
            selectedLine = -1;
            break;
          case EDIT_LINE:
            editingState = EditingState.SELECT_LINE;
            tep.setEnteringText(false);
            break;
          case INSERT_LINE:
            editingState = EditingState.BROWSING;
            newInsertLine = null;
            break;
          case MOVE_LINE:
            Line sLine = song.lines.get(selectedLine);
            switch (lineMoveMode) {
              case BEGIN:
                sLine.begin -= lineMoveAmount;
                break;
              case WHOLE:
                sLine.begin -= lineMoveAmount;
                sLine.end -= lineMoveAmount;
                break;
              case END:
                sLine.end -= lineMoveAmount;
                break;
            }
            lineMoveAmount = 0;
            linesTbs.set(selectedLine, createTextBoxForLine(sLine));
            editingState = EditingState.SELECT_LINE;
            markDirty();
            break;
        }
      } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
        switch (editingState) {
          case EDIT_LINE:
            editingState = EditingState.SELECT_LINE;
            tep.setEnteringText(false);
            String text = tep.getText().toString().replaceAll("\n", "");
            song.lines.get(selectedLine).text = text;
            linesTbs.set(selectedLine, createTextBoxForLine(song.lines.get(selectedLine)));
            markDirty();
            break;
          case SELECT_LINE:
            editingState = EditingState.EDIT_LINE;
            tep.setEnteringText(true);
            tep.getText().setLength(0);
            tep.getText().append(song.lines.get(selectedLine).text);
            tep.setCursorPos(-1);
            break;
          case BROWSING:
            if (!song.lines.isEmpty()) {
              editingState = EditingState.SELECT_LINE;
              int s = 0;
              int smallestDelta = Integer.MAX_VALUE;
              for (int i = 0; i < song.lines.size(); i++) {
                final int db = Math.abs(song.lines.get(i).begin - timelinePos);
                final int de = Math.abs(song.lines.get(i).end - timelinePos);
                if (db < smallestDelta || de < smallestDelta) {
                  s = i;
                  smallestDelta = Math.min(db, de);
                } else {
                  break;
                }
              }
              selectedLine = s;
              song.skip(song.lines.get(selectedLine).begin);
              if (song.isPlaying()) {
                song.pause();
              }
            }
            break;
          case MOVE_LINE:
            editingState = EditingState.SELECT_LINE;
            lineMoveAmount = 0;
            break;
        }
      } else if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
        if (editingState == EditingState.BROWSING) {
          editingState = EditingState.INSERT_LINE;
        }
      } else if (Gdx.input.isKeyJustPressed(Input.Keys.DEL)) {
        if (editingState == EditingState.SELECT_LINE) {
          if (selectedLine >= 0 && selectedLine < song.lines.size()) {
            song.lines.remove(selectedLine);
            linesTbs.remove(selectedLine);
            song.reset();
            selectedLine = -1;
            editingState = EditingState.BROWSING;
            markDirty();
          }
        }
      } else if (Gdx.input.isKeyJustPressed(Input.Keys.S) && !InputUtils.isMetaKeyPressed(InputUtils.META_CTRL)) {
        if (editingState == EditingState.BROWSING) {
          editingState = EditingState.EDIT_SONG_INFO;
          if (song.isPlaying()) {
            song.pause();
          }
        }
      } else if (Gdx.input.isKeyJustPressed(Input.Keys.P) && editingState != EditingState.EDIT_LINE) {
        song.metadata.previewTimestamp = timelinePos;
        previewSetTbLife = 90;
        markDirty();
      } else if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
        if (editingState == EditingState.BROWSING) {
          editingState = EditingState.HELP_MENU;
        }
      }

      if (editingState == EditingState.INSERT_LINE) {
        if (Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
          if (newInsertLine == null) {
            newInsertLine = new Line();
            newInsertLine.text = "";
            newInsertLine.begin = timelinePos;
            newInsertLine.end = timelinePos;
          } else {
            newInsertLine.end = timelinePos;
          }
        } else {
          if (newInsertLine != null) {
            int i = 0;
            for (Line line : song.lines) {
              if (newInsertLine.begin > line.begin) {
                i++;
              } else {
                break;
              }
            }
            song.lines.add(i, newInsertLine);
            linesTbs.add(i, createTextBoxForLine(newInsertLine));
            song.reset();
            newInsertLine = null;
            markDirty();
          }
        }
      }

      ShapeRenderer sr = gm.getShapeRenderer();
      SpriteBatch sb = gm.getBatch();

      if (editingState == EditingState.EDIT_LINE) {
        if (tep.hasTextChanged()) {
          updateLineEditTextBox();
        }
        if (tep.hasCursorPosChanged()) {
          updateLineEditCursor();
          forceDrawCursorBar = true;
        }
        sb.begin();
        gm.drawTextBox(Lyrical.FONT_UI, lineEditTb);
        sb.end();
        if (tep.drawCursorBar() || forceDrawCursorBar) {
          forceDrawCursorBar = false;
          sr.setColor(Color.WHITE);
          sr.begin(ShapeRenderer.ShapeType.Filled);
          sr.rect(lineEditCursor.x, lineEditCursor.y, lineEditCursor.width, lineEditCursor.height);
          sr.end();
        }
      }

      sr.setColor(Color.WHITE);
      sr.begin(ShapeRenderer.ShapeType.Filled);
      if (timelinePos > 10000) {
        sr.rect(0, Lyrical.HEIGHT / 2f - 3, Lyrical.WIDTH, 6);
      } else {
        float w = (timelinePos / 10000f) * Lyrical.WIDTH / 2f;
        sr.rect(Lyrical.WIDTH / 2f - w, Lyrical.HEIGHT / 2f - 3, Lyrical.WIDTH - (Lyrical.WIDTH / 2f - w), 6);
      }
      sr.rect(Lyrical.WIDTH / 2f - 3, Lyrical.HEIGHT / 2f, 6, 20);

      if (newInsertLine != null) {
        final float x = ((newInsertLine.begin - timelinePos) * Lyrical.WIDTH) / 10000f + Lyrical.WIDTH / 2f;
        sr.rect(x, Lyrical.HEIGHT / 2f - 66, Lyrical.WIDTH / 2f - x, 50);
      }
      sr.end();

      sb.begin();
      Line line;
      int i = 0;
      for (TextBox tb : linesTbs) {
        line = song.lines.get(i);
        final float x = ((line.begin - timelinePos) * Lyrical.WIDTH) / 10000f + Lyrical.WIDTH / 2f;
        //final float w = ((line.end - line.begin - timelinePos) * Lyrical.WIDTH) / 10000f + Lyrical.WIDTH / 2f;
        final float w = tb.outerBounds.width;
        if (x + w >= 0 && x < Lyrical.WIDTH) {
          sb.end();
          sr.begin(ShapeRenderer.ShapeType.Filled);
          if (i == selectedLine) {
            sr.setColor(Color.PURPLE);
          } else {
            sr.setColor(Color.DARK_GRAY);
          }
          sr.rect(tb.outerBounds.x + x - tb.textPosition.x, Lyrical.HEIGHT / 2f - tb.glyphLayout.height - 53, tb.outerBounds.width, tb.glyphLayout.height + 50);
          if (i == selectedLine && editingState == EditingState.MOVE_LINE) {
            sr.setColor(Color.YELLOW);
            switch (lineMoveMode) {
              case BEGIN:
                sr.rect(tb.outerBounds.x + x - tb.textPosition.x, Lyrical.HEIGHT / 2f - tb.glyphLayout.height - 53, 2, tb.glyphLayout.height + 50);
                break;
              case WHOLE:
                sr.rect(tb.outerBounds.x + x - tb.textPosition.x, Lyrical.HEIGHT / 2f - tb.glyphLayout.height - 53, tb.outerBounds.width, 2);
                break;
              case END:
                sr.rect(tb.outerBounds.x + x - tb.textPosition.x + w - 2, Lyrical.HEIGHT / 2f - tb.glyphLayout.height - 53, 2, tb.glyphLayout.height + 50);
                break;
            }
          }
          sr.end();
          sb.begin();
          gm.drawTextBox(Lyrical.FONT_UI, tb, x - tb.textPosition.x, 0);
        }
        i++;
      }
      String editModeStr;
      switch (editingState) {
        case BROWSING:
          editModeStr = "Browsing";
          break;
        case EDIT_SONG_INFO:
          editModeStr = "Editing song info";
          break;
        case SELECT_LINE:
          editModeStr = "Selecting line";
          break;
        case EDIT_LINE:
          editModeStr = "Editing line";
          break;
        case INSERT_LINE:
          editModeStr = "Inserting";
          break;
        case MOVE_LINE:
          editModeStr = "Moving line";
          break;
        default:
          editModeStr = "(Unknown)";
      }
      gm.getFont(Lyrical.FONT_UI).draw(sb, "Mode: [GREEN]" + editModeStr, 200, 50);
      gm.getFont(Lyrical.FONT_UI).draw(sb, "Position: [YELLOW]" + String.format("%.03f", (song.getTimestamp() / 1000f)) + "[] sec", 50, 50);
      sb.end();
    } else {
      if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
        if (editingSongMeta) {
          editingSongMeta = false;
          updateSongMetaTextBox(selectedSongMeta, false);
        } else {
          editingState = EditingState.BROWSING;
        }
        tep.setEnteringText(false);
      } else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
        if (!editingSongMeta && --selectedSongMeta < 0) {
          selectedSongMeta = 3;
        }
      } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
        if (!editingSongMeta && ++selectedSongMeta > 3) {
          selectedSongMeta = 0;
        }
      } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
        if (!editingSongMeta) {
          editingSongMeta = true;
          tep.setEnteringText(true);
          tep.getText().setLength(0);
          String s;
          switch (selectedSongMeta) {
            case META_TITLE:
              s = song.metadata.title;
              break;
            case META_ARTIST:
              s = song.metadata.artist;
              break;
            case META_LANGUAGE:
              s = song.metadata.language;
              break;
            case META_CHARTER:
              s = song.metadata.charter;
              break;
            default:
              s = "(Unknown)";
          }
          tep.getText().append(s);
          tep.setCursorPos(-1);
          updateSongMetaCursorPos();
        } else {
          String s = tep.getText().toString().replaceAll("\n", "");
          switch (selectedSongMeta) {
            case META_TITLE:
              song.metadata.title = s;
              break;
            case META_ARTIST:
              song.metadata.artist = s;
              break;
            case META_LANGUAGE:
              song.metadata.language = s;
              break;
            case META_CHARTER:
              song.metadata.charter = s;
              break;
          }
          updateSongMetaTextBox(selectedSongMeta, false);
          editingSongMeta = false;
          tep.setEnteringText(false);
          markDirty();
        }
      } else if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
        if (!editingSongMeta) {
          editingState = EditingState.HELP_MENU;
        }
      }

      if (tep.isEnteringText()) {
        if (tep.hasTextChanged()) {
          updateSongMetaTextBox(selectedSongMeta, true);
        }
        if (tep.hasCursorPosChanged()) {
          updateSongMetaCursorPos();
          forceDrawCursorBar = true;
        }
      }

      SpriteBatch sb = gm.getBatch();
      ShapeRenderer sr = gm.getShapeRenderer();
      sb.begin();
      /*for (TextBox tb : songMetaTbs) {
        gm.drawTextBox(Lyrical.FONT_UI, tb);
      }*/
      for (int i = 0; i < songMetaTbs.length; i++) {
        TextBox tb = songMetaTbs[i];
        if (i / 2 == selectedSongMeta && i % 2 == 1) {
          sb.end();
          if (editingSongMeta) {
            sr.setColor(Color.PURPLE);
          } else {
            sr.setColor(Color.DARK_GRAY);
          }
          sr.begin(ShapeRenderer.ShapeType.Filled);
          sr.rect(tb.textPosition.x - 5, tb.textPosition.y - 5, tb.glyphLayout.width + 10, tb.glyphLayout.height + 10);
          if (tep.isEnteringText() && (tep.drawCursorBar() || forceDrawCursorBar)) {
            forceDrawCursorBar = false;
            sr.setColor(Color.WHITE);
            sr.rect(songMetaCursorPos.x, songMetaCursorPos.y, songMetaCursorPos.width, songMetaCursorPos.height);
          }
          sr.end();
          sb.begin();
        }
        gm.drawTextBox(Lyrical.FONT_UI, tb);
      }
      sb.end();
    }

    if (savedTbLife > 0) {
      gm.getBatch().begin();
      gm.drawTextBox(Lyrical.FONT_DISPLAY, savedTb);
      gm.getBatch().end();
      savedTbLife--;
    }
    if (previewSetTbLife > 0) {
      gm.getBatch().begin();
      gm.drawTextBox(Lyrical.FONT_DISPLAY, previewSetTb);
      gm.getBatch().end();
      previewSetTbLife--;
    }

  }

  @Override
  public void dispose() {
    song.dispose();
  }

  private enum EditingState {
    BROWSING,
    EDIT_SONG_INFO,
    SELECT_LINE,
    EDIT_LINE,
    INSERT_LINE,
    MOVE_LINE,
    HELP_MENU
  }

  private enum LineMoveMode {
    BEGIN, WHOLE, END
  }

}
