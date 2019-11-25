package me.whizvox.lyrical;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class Reference {

  public static final String
      VERSION = "0.2-pre2";

  public static final class Files {
    public static final FileHandle
        SONGS_DIR = Gdx.files.local("songs"),
        IMPORT_DIR = Gdx.files.local("import"),
        CACHE_DIR = Gdx.files.local("cache"),
        SETTINGS = Gdx.files.local("lyrical.cfg");
  }

  public static final class Settings {
    public static final String
        RESOLUTION_WIDTH = "ResolutionWidth",
        RESOLUTION_HEIGHT = "ResolutionHeight",
        FULLSCREEN = "Fullscreen",
        WINDOW_WIDTH = "WindowWidth",
        WINDOW_HEIGHT = "WindowHeight",
        MUSIC_VOLUME = "MusicVolume";
  }

  public static final class Defaults {
    public static final int
        RESOLUTION_WIDTH = 800,
        RESOLUTION_HEIGHT = 450,
        WINDOW_WIDTH = RESOLUTION_WIDTH,
        WINDOW_HEIGHT = RESOLUTION_HEIGHT,
        MUSIC_VOLUME = 50;
    public static final boolean
        FULLSCREEN = false;
  }

}
