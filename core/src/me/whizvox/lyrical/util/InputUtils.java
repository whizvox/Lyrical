package me.whizvox.lyrical.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class InputUtils {

  public static final int
      META_NONE = 0,
      META_CTRL = 1,
      META_ALT = 1 << 1,
      META_SHIFT = 1 << 2;

  // doesn't account for order in which keys are pressed
  public static boolean isMetaKeyPressed(int meta) {
    int sMeta = 0;
    if ((meta & META_CTRL) != 0 &&
        (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT))) {
      sMeta |= META_CTRL;
    }
    if ((meta & META_ALT) != 0 &&
        (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT))) {
      sMeta |= META_ALT;
    }
    if ((meta & META_SHIFT) != 0 &&
        (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT))) {
      sMeta |= META_SHIFT;
    }
    return sMeta == (meta & (META_CTRL | META_ALT | META_SHIFT));
  }

  public static boolean isKeyPressed(int key, int meta) {
    return Gdx.input.isKeyJustPressed(key) && isMetaKeyPressed(meta);
  }

}
