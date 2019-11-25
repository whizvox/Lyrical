package me.whizvox.lyrical.song;

import me.whizvox.lyrical.Reference;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class SongsRepository {

  private Map<String, Song.Metadata> songs;
  private Path rootDir;

  public SongsRepository(String root) {
    rootDir = Paths.get(root);
    songs = new HashMap<>();
  }

  public void refresh() throws IOException {
    songs.clear();
    long t1 = System.currentTimeMillis();
    Files.walk(rootDir, 2).filter(path -> path.getFileName().toString().equals("song.ini")).forEach(path -> {
      try {
        Song.Metadata metadata = Song.readMetadata(path);
        songs.put(path.getParent().toString(), metadata);
      } catch (IOException e) {
        System.err.println("Could not read from file at <" + path.toString() + ">");
        e.printStackTrace();
      }
    });
    long t2 = System.currentTimeMillis();
    System.out.println("Read " + songs.size() + " song(s) in " + (t2 - t1) + "ms");
  }

  public void refresh(String dir) {
    Path dirPath = Paths.get(Reference.Files.SONGS_DIR.child(dir).path());
    songs.remove(dirPath.toString());
    try {
      Song.Metadata metadata = Song.readMetadata(dirPath.resolve("song.ini"));
      songs.put(dirPath.toString(), metadata);
    } catch (IOException e) {
      System.err.println("Could not do a single song refresh");
      e.printStackTrace();
    }
  }

  public Map<String, Song.Metadata> getCopy() {
    return new HashMap<>(songs);
  }

  public void forEach(BiConsumer<String, Song.Metadata> action) {
    songs.forEach(action);
  }

}
