package me.whizvox.lyrical;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class Reference {

  public static final String
      VERSION = "0.1";

  public static final class Files {
    public static final FileHandle
        SONGS_DIR = Gdx.files.local("songs"),
        IMPORT_DIR = Gdx.files.local("import"),
        CACHE_DIR = Gdx.files.local("cache");
  }

}
