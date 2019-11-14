package me.whizvox.lyrical;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import me.whizvox.lyrical.util.Pair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LyricalInputProcessor implements InputProcessor {

  private Vector2 lastMousePos;
  private Vector2 mousePos;
  private List<Integer> pressedKeys;
  private List<Integer> releasedKeys;
  private List<Pair<Long, Integer>> heldKeys;
  private List<Integer> pressedButtons;
  private List<Integer> releasedButtons;
  private List<Pair<Long, Integer>> heldButtons;
  private StringBuilder textEntry;
  private boolean enteringText;

  private List<KeyPressListener> keyPressListeners;
  private List<KeyDownListener> keyDownListeners;
  private List<KeyUpListener> keyUpListeners;
  private List<MouseClickListener> mouseClickListeners;
  private List<MouseHeldListener> mouseHeldListeners;
  private List<MouseReleaseListener> mouseReleaseListeners;
  private List<MouseMovedListener> mouseMovedListeners;
  private List<MouseDraggedListener> mouseDraggedListeners;
  private List<MouseScrollListener> mouseScrollListeners;
  private List<Pair<int[], KeyComboPressListener>> keyComboPressListeners;

  public LyricalInputProcessor() {
    lastMousePos = new Vector2();
    mousePos = new Vector2();
    pressedKeys = new ArrayList<>();
    releasedKeys = new ArrayList<>();
    heldKeys = new LinkedList<>();
    pressedButtons = new ArrayList<>();
    releasedButtons = new ArrayList<>();
    heldButtons = new LinkedList<>();
    textEntry = new StringBuilder();
    enteringText = false;

    keyPressListeners = new ArrayList<>();
    keyDownListeners = new ArrayList<>();
    keyUpListeners = new ArrayList<>();
    mouseClickListeners = new ArrayList<>();
    mouseHeldListeners = new ArrayList<>();
    mouseReleaseListeners = new ArrayList<>();
    mouseMovedListeners = new ArrayList<>();
    mouseDraggedListeners = new ArrayList<>();
    mouseScrollListeners = new LinkedList<>();
    keyComboPressListeners = new ArrayList<>();
  }

  public void addListener(Object listener) {
    if (listener instanceof KeyPressListener) {
      keyPressListeners.add((KeyPressListener) listener);
    } else if (listener instanceof KeyDownListener) {
      keyDownListeners.add((KeyDownListener) listener);
    } else if (listener instanceof KeyUpListener) {
      keyUpListeners.add((KeyUpListener) listener);
    } else if (listener instanceof MouseClickListener) {
      mouseClickListeners.add((MouseClickListener) listener);
    } else if (listener instanceof MouseHeldListener) {
      mouseHeldListeners.add((MouseHeldListener) listener);
    } else if (listener instanceof MouseReleaseListener) {
      mouseReleaseListeners.add((MouseReleaseListener) listener);
    } else if (listener instanceof MouseMovedListener) {
      mouseMovedListeners.add((MouseMovedListener) listener);
    } else if (listener instanceof MouseDraggedListener) {
      mouseDraggedListeners.add((MouseDraggedListener) listener);
    } else if (listener instanceof MouseScrollListener) {
      mouseScrollListeners.add((MouseScrollListener) listener);
    }
  }

  public void addKeyComboListener(int keycode, int metacode, KeyComboPressListener listener) {
    keyComboPressListeners.add(new Pair<>(new int[] {keycode, metacode}, listener));
  }

  public void tick() {
    heldKeys.forEach((pair) -> {

    });
  }

  @Override
  public boolean keyDown(int keycode) {
    heldKeys.add(new Pair<>(System.currentTimeMillis(), keycode));
    return true;
  }

  @Override
  public boolean keyUp(int keycode) {
    heldKeys.removeIf((pair) -> pair.second == keycode);
    return true;
  }

  @Override
  public boolean keyTyped(char character) {
    if (enteringText) {
      textEntry.append(character);
    }
    return true;
  }

  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    pressedButtons.add(button);
    heldButtons.add(new Pair<>(System.currentTimeMillis(), button));
    mouseClickListeners.forEach(listener -> listener.onAction(button));
    return true;
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    releasedButtons.add(button);
    long duration = 0;
    for (Pair<Long, Integer> pair : heldButtons) {
      if (pair.second == button) {
        duration = System.currentTimeMillis() - pair.first;
      }
    }
    heldButtons.removeIf((pair) -> pair.second == button);
    long finalDuration = duration;
    mouseReleaseListeners.forEach(listener -> listener.onAction(button, (int)finalDuration));
    return true;
  }

  @Override
  public boolean touchDragged(int screenX, int screenY, int pointer) {
    mouseDraggedListeners.forEach((listener) -> listener.onAction(mousePos, lastMousePos));
    return true;
  }

  @Override
  public boolean mouseMoved(int screenX, int screenY) {
    lastMousePos.set(mousePos);
    mousePos.set(screenX, screenY);
    return true;
  }

  @Override
  public boolean scrolled(int amount) {
    mouseScrollListeners.forEach(listener -> listener.onAction(amount));
    return true;
  }

  public void enableTextEntry(boolean enable) {
    this.enteringText = enable;
  }

  public boolean isKeyPressed(int keycode) {
    //return pressedKeys.contains(keycode);
    return Gdx.input.isKeyJustPressed(keycode);
  }

  public boolean isKeyReleased(int keycode) {
    return releasedKeys.contains(keycode);
  }

  public boolean isKeyHeld(int keycode) {
    return Gdx.input.isKeyPressed(keycode);
  }

  public long getKeyHeld(int keycode) {
    for (Pair<Long, Integer> pair : heldKeys) {
      if (pair.second == keycode) {
        return System.currentTimeMillis() - pair.first;
      }
    }
    return 0;
  }

  public boolean isButtonPressed(int button) {
    return pressedButtons.contains(button);
  }

  public boolean isButtonReleased(int button) {
    return releasedButtons.contains(button);
  }

  public boolean isButtonHeld(int button) {
    return Gdx.input.isButtonPressed(button);
  }

  public long getButtonHeld(int button) {
    for (Pair<Long, Integer> pair : heldButtons) {
      if (pair.second == button) {
        return System.currentTimeMillis() - pair.first;
      }
    }
    return 0;
  }

  private static final int
      SHIFT = 1 << 0,
      CTRL = 1 << 1,
      ALT = 1 << 2,
      SUPER = 1 << 3;

  public static int metacode(boolean shift, boolean ctrl, boolean alt, boolean _super) {
    int code = 0;
    if (shift) {
      code |= SHIFT;
    }
    if (ctrl) {
      code |= CTRL;
    }
    if (alt) {
      code |= ALT;
    }
    if (_super) {
      code |= SUPER;
    }
    return code;
  }

  public static boolean hasShift(int metacode) {
    return (metacode & SHIFT) != 0;
  }

  public static boolean hasCtrl(int metacode) {
    return (metacode & CTRL) != 0;
  }

  public static boolean hasAlt(int metacode) {
    return (metacode & ALT) != 0;
  }

  public static boolean hasSuper(int metacode) {
    return (metacode & SUPER) != 0;
  }

  public interface KeyPressListener {
    void onAction(int keycode);
  }

  public interface KeyDownListener {
    void onAction(int keycode, int duration);
  }

  public interface KeyUpListener {
    void onAction(int keycode, int duration);
  }

  public interface MouseClickListener {
    void onAction(int button);
  }

  public interface MouseHeldListener {
    void onAction(int button, int duration);
  }

  public interface MouseReleaseListener {
    void onAction(int button, int duration);
  }

  public interface MouseMovedListener {
    void onAction(Vector2 pos, Vector2 delta);
  }

  public interface MouseDraggedListener {
    void onAction(Vector2 pos, Vector2 delta);
  }

  public interface MouseScrollListener {
    void onAction(int amount);
  }

  public interface KeyComboPressListener {
    void onAction();
  }

}
