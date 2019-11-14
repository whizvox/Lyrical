package me.whizvox.lyrical.song;

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
    Files.walk(rootDir, 2).filter(path -> path.getFileName().toString().equals("song.ini")).forEach(path -> {
      try {
        Song.Metadata metadata = Song.readMetadata(path);
        songs.put(path.getParent().toString(), metadata);
        System.out.println("Imported song from <" + path.getParent().toString() + ">");
      } catch (IOException e) {
        System.err.println("Could not read from file at <" + path.toString() + ">");
        e.printStackTrace();
      }
    });
  }

  public Map<String, Song.Metadata> getCopy() {
    return new HashMap<>(songs);
  }

  public void forEach(BiConsumer<String, Song.Metadata> action) {
    songs.forEach(action);
  }

}
