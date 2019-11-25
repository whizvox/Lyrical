package me.whizvox.lyrical.song;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;
import me.whizvox.lyrical.Lyrical;
import me.whizvox.lyrical.Reference;
import me.whizvox.lyrical.util.Pair;
import me.whizvox.lyrical.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Song implements Disposable {

  public final Metadata metadata;
  public final List<Line> lines;

  private int timestamp;
  private int currentLine;
  private float progress;
  private Music music;

  private long start;
  private long paused;

  private Song(Metadata metadata, List<Line> lines) {
    this.metadata = metadata;
    this.lines = lines;

    timestamp = 0;
    currentLine = 0;
    progress = 0;
    start = -1;
    paused = 0;
  }

  public Song() {
    this(new Metadata(), new ArrayList<>());
  }

  public void play() {
    music.play();
    music.setVolume(Lyrical.getInstance().getSettings().getInt(Reference.Settings.MUSIC_VOLUME, Reference.Defaults.MUSIC_VOLUME) / 100f);
    start = System.currentTimeMillis();
  }

  public boolean isPlaying() {
    return start != -1;
  }

  public boolean hasFinished() {
    if (currentLine != -1) {
      return timestamp > getCurrentLine().end;
    }
    return true;
  }

  public void pause() {
    music.pause();
    paused = System.currentTimeMillis();
  }

  public void resume() {
    music.play();
    music.setVolume(Lyrical.getInstance().getSettings().getInt(Reference.Settings.MUSIC_VOLUME, Reference.Defaults.MUSIC_VOLUME) / 100f);
    start += System.currentTimeMillis() - paused;
    paused = 0;
  }

  public boolean isPaused() {
    return paused != 0;
  }

  public void reset() {
    currentLine = 0;
  }

  public void tick() {
    timestamp = (int)(System.currentTimeMillis() - start - metadata.offset);
    if (!lines.isEmpty()) {
      Line c = getCurrentLine();
      while (timestamp > c.end && currentLine < lines.size() - 1) {
        c = lines.get(++currentLine);
        progress = 0;
      }
      if (timestamp > c.end) {
        progress = 0;
      } else if (timestamp >= c.begin) {
        progress = 1 - (float) (c.end - timestamp) / (c.end - c.begin);
      }
    }
  }

  public float getProgress() {
    return progress;
  }

  public Line getCurrentLine() {
    if (isPlaying()) {
      return lines.get(currentLine);
    }
    return null;
  }

  public int getCurrentLineIndex() {
    return currentLine;
  }

  public void skip(int pos) {
    if (pos < 0) {
      pos = 0;
    }
    if (!music.isPlaying()) {
      if (isPaused()) {
        resume();
      } else {
        play();
      }
      music.setPosition(pos / 1000f);
      pause();
    } else {
      music.setPosition(pos / 1000f);
    }
    start += timestamp - pos;
    tick();
  }

  public int getTimestamp() {
    return timestamp;
  }

  public Music getMusic() {
    return music;
  }

  public void save(Writer writer) throws IOException {
    metadata.length = 0;
    if (!lines.isEmpty()) {
      metadata.length = lines.get(lines.size() - 1).end / 1000;
    }
    writer.write("[Version=1]\n");
    writer.write("[Metadata]\n");
    writer.write("Title=" + metadata.title + "\n");
    writer.write("Artist=" + metadata.artist + "\n");
    writer.write("Language=" + metadata.language + "\n");
    writer.write("Length=" + metadata.length + "\n");
    writer.write("Charter=" + metadata.charter + "\n");
    writer.write("File=" + metadata.filePath + "\n");
    writer.write("Offset=" + metadata.offset + "\n");
    writer.write("Preview=" + metadata.previewTimestamp + "\n");
    writer.write("Background=" + metadata.background + "\n");
    writer.write("[Lines]\n");
    for (Line line : lines) {
      writer.write(line.begin + "," + line.end + "," + line.text + "\n");
    }
  }

  @Override
  public void dispose() {
    if (music != null) {
      music.dispose();
    }
  }

  public static void swapWithWav(String filePath, Song song) {
    FileHandle[] list = Reference.Files.CACHE_DIR.list(".wav");
    for (FileHandle fh : list) {
      System.out.println("Deleting " + fh.path());
      fh.delete();
    }
    FileHandle inFile = Gdx.files.local(filePath).child(song.metadata.filePath);
    FileHandle outFile = Reference.Files.CACHE_DIR.child(UUID.randomUUID().toString() + ".wav");
    try {
      String[] cmd = {
          "ffmpeg",
          "-v",
          "warning",
          "-i",
          inFile.path(), // getting the absolute path doesn't work??
          outFile.file().getAbsolutePath()
      };
      System.out.println("Executing command: " + Arrays.toString(cmd));
      Process exec = Runtime.getRuntime().exec(cmd);
      System.out.println("\tExit value: " + exec.waitFor());
      boolean anyErrorOut = false;
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getErrorStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          if (!anyErrorOut) {
            System.out.println("\t-- ERROR OUTPUT --");
            anyErrorOut = true;
          }
          System.out.println("\t" + line);
        }
      }
      song.music.dispose();
      song.music = Gdx.audio.newMusic(Gdx.files.absolute(outFile.file().getAbsolutePath()));
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  public static class Metadata {
    public String title;
    public String artist;
    public String language;
    public int length;
    public String charter;
    public String filePath;
    public int offset;
    public int previewTimestamp;
    public String background;

    public Metadata() {
      title = "(Unknown title)";
      artist = "(Unknown artist)";
      language = "(Unknown language)";
      length = 0;
      charter = "(Unknown charter)";
      filePath = "";
      offset = 0;
      previewTimestamp = 0;
      background = "";
    }
  }

  private static Object readObject(Path filePath, boolean readMetadataOnly) throws IOException {
    try (BufferedReader reader = Files.newBufferedReader(filePath)) {
      String line;
      boolean readMetadata = false;
      boolean readLines = false;
      List<String> tokens = new ArrayList<>();
      Song song = new Song(new Metadata(), new ArrayList<>());
      while ((line = reader.readLine()) != null) {
        if (!line.isEmpty()) {
          if ("[Metadata]".equals(line)) {
            readMetadata = true;
            readLines = false;
          } else if ("[Lines]".equals(line)) {
            if (readMetadataOnly) {
              break;
            }
            readLines = true;
            readMetadata = false;
          } else if (readMetadata) {
            StringUtils.tokenize(line, '=', 2, tokens);
            if (tokens.size() == 2) {
              final String key = tokens.get(0);
              final String value = tokens.get(1);
              switch (key) {
                case "Title":
                  song.metadata.title = value;
                  break;
                case "Artist":
                  song.metadata.artist = value;
                  break;
                case "Language":
                  song.metadata.language = value;
                  break;
                case "Length":
                  song.metadata.length = Integer.parseInt(value);
                  break;
                case "File":
                  song.metadata.filePath = value;
                  break;
                case "Charter":
                  song.metadata.charter = value;
                  break;
                case "Offset":
                  song.metadata.offset = Integer.parseInt(value);
                  break;
                case "Preview":
                  song.metadata.previewTimestamp = Integer.parseInt(value);
                  break;
                case "Background":
                  song.metadata.background = value;
                  break;
                default:
                  System.err.println("Invalid metadata key: " + key);
              }
            } else {
              System.err.println("Invalid line: " + line + " (not enough tokens)");
            }
          } else if (readLines) {
            StringUtils.tokenize(line, ',', 3, tokens);
            if (tokens.size() == 3) {
              Line l = new Line();
              l.begin = Integer.parseInt(tokens.get(0));
              l.end = Integer.parseInt(tokens.get(1));
              l.text = tokens.get(2);
              song.lines.add(l);
            } else {
              System.err.println("Invalid lines line (not enough tokens): " + line);
            }
          }
        }
      }
      if (readMetadataOnly) {
        return song.metadata;
      }
      FileHandle audioPath = Gdx.files.local(filePath.getParent().resolve(song.metadata.filePath).toString());
      if (audioPath.exists()) {
        song.music = Gdx.audio.newMusic(audioPath);
      } else {
        System.err.println("Could not load audio, as it does not exist: " + audioPath.path());
        song.music = Gdx.audio.newMusic(Gdx.files.internal("silence.wav"));
      }
      return song;
    }
  }

  public static Metadata readMetadata(Path filePath) throws IOException {
    return (Metadata)readObject(filePath, true);
  }

  public static Song readSong(Path filePath) throws IOException {
    return (Song)readObject(filePath, false);
  }

  public static Pair<String, Song> createSong(String importPath) {
    FileHandle importHandle = Reference.Files.IMPORT_DIR.child(importPath);
    if (!importHandle.exists()) {
      return null;
    }
    FileHandle resDir = Reference.Files.SONGS_DIR.child(importHandle.nameWithoutExtension());
    resDir.mkdirs();
    FileHandle resMusicFile = resDir.child(importHandle.name());
    importHandle.moveTo(resMusicFile);
    Song song = new Song();
    song.metadata.filePath = resMusicFile.name();
    song.music = Gdx.audio.newMusic(resMusicFile);
    FileHandle songIni = resDir.child("song.ini");
    try (Writer writer = songIni.writer(true)) {
      song.save(writer);
    } catch (IOException e) {
      System.err.println("Could not save song to " + songIni.path());
      throw new RuntimeException(e);
    }
    return new Pair<>(resDir.path(), song);
  }

}
