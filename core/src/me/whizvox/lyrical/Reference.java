package me.whizvox.lyrical;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class Reference {

  public static final String
      VERSION = "0.2-pre1";

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
        WIDTH = "SizeWidth",
        HEIGHT = "SizeHeight",
        POS_X = "PosX",
        POS_Y = "PosY";
  }

  public static final class Defaults {
    public static final int
        RESOLUTION_WIDTH = 800,
        RESOLUTION_HEIGHT = 450,
        SIZE_WIDTH = RESOLUTION_WIDTH,
        SIZE_HEIGHT = RESOLUTION_HEIGHT;
    public static final boolean
        FULLSCREEN = false;
  }

}
