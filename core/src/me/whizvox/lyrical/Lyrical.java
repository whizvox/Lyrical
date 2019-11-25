package me.whizvox.lyrical;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import me.whizvox.lyrical.graphics.GraphicsManager;
import me.whizvox.lyrical.scene.*;
import me.whizvox.lyrical.song.SongsRepository;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class Lyrical extends ApplicationAdapter {
  GraphicsManager gm;
  SpriteBatch batch;
  ShapeRenderer srenderer;

  SongsRepository repo;

  private Settings settings;

  private Map<Integer, ApplicationAdapter> scenes;
  private ApplicationAdapter currentScene;
  private ApplicationAdapter queuedScene;
  private Object transitionData;

  @Override
  public void create() {
    settings = new Settings();
    if (!Reference.Files.SETTINGS.exists()) {
      Reference.Files.SETTINGS.writeBytes(new byte[0], false);
    }
    try (Reader reader = Reference.Files.SETTINGS.reader()) {
      settings.load(reader);
      System.out.println("Settings loaded");
    } catch (IOException e) {
      System.err.println("Could not load settings from <" + Reference.Files.SETTINGS.path() + ">");
      e.printStackTrace();
      Gdx.app.exit();
    }

    boolean moreSettingsAdded = false;
    Field[] settingsFields = Reference.Settings.class.getFields();
    for (Field field : settingsFields) {
      final int mod = field.getModifiers();
      if (Modifier.isStatic(mod) && field.getType().isAssignableFrom(String.class)) {
        try {
          String key = (String) field.get(null);
          Field defaultsField = Reference.Defaults.class.getField(field.getName());
          if (!settings.containsKey(key)) {
            settings.set(key, defaultsField.get(null));
            moreSettingsAdded = true;
          }
        } catch (IllegalAccessException | NoSuchFieldException e) {
          e.printStackTrace();
        }
      }
    }
    if (moreSettingsAdded) {
      try (Writer writer = Reference.Files.SETTINGS.writer(false, "UTF-8")) {
        settings.save(writer);
        System.out.println("Saved settings at " + Reference.Files.SETTINGS.path());
      } catch (IOException e) {
        System.err.println("Could not save settings at " + Reference.Files.SETTINGS.path());
        e.printStackTrace();
      }
    }
    if (settings.getBool(Reference.Settings.FULLSCREEN, Reference.Defaults.FULLSCREEN)) {
      Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
    }
    // TODO Looks bad unless we can get and manipulate window position
    /* else {
      int wWidth = settings.getInt(Reference.Settings.WINDOW_WIDTH, Reference.Defaults.WINDOW_WIDTH);
      int wHeight = settings.getInt(Reference.Settings.WINDOW_HEIGHT, Reference.Defaults.WINDOW_HEIGHT);
      Gdx.graphics.setWindowedMode(wWidth, wHeight);
    }*/

    gm = new GraphicsManager();
    gm.create();
    batch = gm.getBatch();
    srenderer = gm.getShapeRenderer();
    Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
    Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);

    if (!Reference.Files.SONGS_DIR.exists()) {
      Reference.Files.SONGS_DIR.mkdirs();
    }
    if (!Reference.Files.IMPORT_DIR.exists()) {
      Reference.Files.IMPORT_DIR.mkdirs();
    }
    if (!Reference.Files.CACHE_DIR.exists()) {
      Reference.Files.CACHE_DIR.mkdirs();
    }

    repo = new SongsRepository("songs");
    try {
      repo.refresh();
    } catch (IOException e) {
      System.err.println("Could not refresh songs directory");
      e.printStackTrace();
    }

    scenes = new HashMap<>();
    scenes.put(SCENE_TITLE, new TitleScene(gm));
    scenes.put(SCENE_SONG_SELECTION, new SongSelectionScene(gm, repo));
    scenes.put(SCENE_PLAYING, new SongPlayingScene(gm));
    scenes.put(SCENE_EDITOR, new Editor(gm));
    scenes.put(SCENE_DEBUG, new TextEnterTestScene(gm));
    scenes.put(SCENE_IMPORTS, new ImportScene(gm));
    scenes.put(SCENE_SETTINGS, new SettingsScene(gm));
    currentScene = scenes.get(SCENE_TITLE);
    currentScene.create();
    queuedScene = null;
    transitionData = null;
  }

  @Override
  public void resize(int width, int height) {
    settings.set(Reference.Settings.WINDOW_WIDTH, Gdx.graphics.getWidth());
    settings.set(Reference.Settings.WINDOW_HEIGHT, Gdx.graphics.getHeight());
    gm.resize(width, height);
    currentScene.resize(width, height);
  }

  @Override
  public void render() {
    if (queuedScene != null) {
      currentScene.dispose();
      currentScene = queuedScene;
      queuedScene = null;
      currentScene.create();
    }

    Gdx.gl.glClearColor(0, 0, 0, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    currentScene.render();
    gm.render();
  }

  @Override
  public void dispose() {
    try (Writer writer = Reference.Files.SETTINGS.writer(false, "UTF-8")) {
      settings.save(writer);
    } catch (IOException e) {
      System.err.println("Could not save settings");
      e.printStackTrace();
    }

    gm.dispose();
    currentScene.dispose();
  }

  public Settings getSettings() {
    return settings;
  }

  public Object getTransitionData() {
    return transitionData;
  }

  public SongsRepository getSongsRepo() {
    return repo;
  }

  public void switchScene(int sceneId, Object transitionData) {
    if (queuedScene != null) {
      System.err.println("Two or more scenes attempted to queue at once");
    } else {
      queuedScene = scenes.get(sceneId);
      if (queuedScene == null) {
        System.err.println("Invalid scene id: " + sceneId);
      } else {
        this.transitionData = transitionData;
      }
    }
  }

  public void switchScene(int sceneId) {
    switchScene(sceneId, null);
  }

  public static Lyrical getInstance() {
    return (Lyrical) Gdx.app.getApplicationListener();
  }

  public static final int
      FONT_DISPLAY = 0,
      FONT_UI = 1,
      SCENE_TITLE = 0,
      SCENE_SONG_SELECTION = 1,
      SCENE_PLAYING = 2,
      SCENE_EDITOR = 3,
      SCENE_DEBUG = 4,
      SCENE_IMPORTS = 5,
      SCENE_SETTINGS = 6;

}
