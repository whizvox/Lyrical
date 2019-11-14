package me.whizvox.lyrical;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;
import me.whizvox.lyrical.graphics.GraphicsManager;
import me.whizvox.lyrical.graphics.TextBox;

public class TextEntryProcessor extends InputAdapter {

  private boolean enteringText;
  private StringBuilder text;
  private StringBuilder prevText;
  private int cursorPos;
  private int prevCursorPos;

  private int cursorBarRenderCooldown;
  private int cursorBarCounter;

  private long leftDownTime, rightDownTime;

  public TextEntryProcessor() {
    cursorPos = 0;
    prevCursorPos = 0;
    text = new StringBuilder();
    prevText = new StringBuilder();
    enteringText = false;

    cursorBarRenderCooldown = 30;
    cursorBarCounter = 0;

    leftDownTime = 0;
    rightDownTime = 0;
  }

  public int getCursorPos() {
    return cursorPos;
  }

  public StringBuilder getText() {
    return text;
  }

  public boolean isEnteringText() {
    return enteringText;
  }

  public void setEnteringText(boolean enteringText) {
    this.enteringText = enteringText;
  }

  public boolean hasCursorPosChanged() {
    final long t = System.currentTimeMillis();
    if (leftDownTime != 0 && Gdx.input.isKeyPressed(Input.Keys.LEFT) && t - leftDownTime > 500) {
      if (cursorPos > 0) {
        cursorPos--;
      }
    } else if (rightDownTime != 0 && Gdx.input.isKeyPressed(Input.Keys.RIGHT) && t - rightDownTime > 500) {
      if (cursorPos < text.length()) {
        cursorPos++;
      }
    }
    if (prevCursorPos != cursorPos) {
      prevCursorPos = cursorPos;
      return true;
    }
    return false;
  }

  public void setCursorPos(int newCursorPos) {
    if (newCursorPos < 0 || newCursorPos > text.length()) {
      cursorPos = text.length();
    } else {
      cursorPos = newCursorPos;
    }
  }

  private boolean stringBuildersEquals(StringBuilder a, StringBuilder b) {
    if (a.length() != b.length()) {
      return false;
    }
    for (int i = 0; i < a.length(); i++) {
      if (a.charAt(i) != b.charAt(i)) {
        return false;
      }
    }
    return true;
  }

  public boolean hasTextChanged() {
    if (!stringBuildersEquals(text, prevText)) {
      prevText = new StringBuilder(text);
      return true;
    }
    return false;
  }

  public boolean drawCursorBar() {
    if (cursorBarCounter++ > cursorBarRenderCooldown * 2) {
      cursorBarCounter = 0;
      return true;
    }
    return cursorBarCounter <= cursorBarRenderCooldown;
  }

  @Override
  public boolean keyDown(int keycode) {
    switch (keycode) {
      case Input.Keys.LEFT:
        if (cursorPos > 0) {
          cursorPos--;
        }
        leftDownTime = System.currentTimeMillis();
        break;
      case Input.Keys.RIGHT:
        if (cursorPos < text.length()) {
          cursorPos++;
        }
        rightDownTime = System.currentTimeMillis();
        break;
    }
    return true;
  }

  @Override
  public boolean keyUp(int keycode) {
    switch (keycode) {
      case Input.Keys.LEFT:
        leftDownTime = 0;
        break;
      case Input.Keys.RIGHT:
        rightDownTime = 0;
        break;
    }
    return true;
  }

  @Override
  public boolean keyTyped(char c) {
    if (enteringText) {
      switch (c) {
        case '\b':
          if (text.length() > 0 && cursorPos > 0) {
            text.replace(cursorPos - 1, cursorPos, "");
            cursorPos--;
          }
          break;
        case '\t':
          text.insert(cursorPos, "  ");
          cursorPos += 2;
          break;
        default:
          text.insert(cursorPos, c);
          cursorPos++;
      }
    }
    return true;
  }

  // NOTE: Only works with TOP_LEFT alignment and if there aren't any empty lines with a non-ending cursor position
  public Vector2 getOnScreenCursorPos(GraphicsManager gm, TextBox tb) {
    int p = getCursorPos();
    float xoff = 0;
    float yoff = 0;
    float width = 0;
    float height = gm.getFont(Lyrical.FONT_UI).getLineHeight();
    for (int i = 0; i < tb.glyphLayout.runs.size && p > 0; i++) {
      xoff = tb.glyphLayout.runs.get(i).x;
      for (int j = 0; j < tb.glyphLayout.runs.get(i).xAdvances.size - 1 && p > 0; j++, p--) {
        xoff += tb.glyphLayout.runs.get(i).xAdvances.get(j);
        width = tb.glyphLayout.runs.get(i).glyphs.get(j).width;
      }
      p--; // account for newlines taking up an extra character in the stringbuilder, but not in the glyphs array
    }
    if (getCursorPos() == 0 || (getCursorPos() > 0 && getText().charAt(getCursorPos() - 1) == '\n')) {
      xoff = 0;
      width = 0;
    }
    yoff = 0;
    for (p = 0; p < getCursorPos(); p++) {
      if (getText().charAt(p) == '\n') {
        yoff -= height;
      }
    }
    return new Vector2(xoff + width, yoff - height * 0.75f);
  }

  // supposed to be a better alternative to the method above
  private static Vector2 getOnScreenCursorPos(int cursorPos, GlyphLayout layout, float lineHeight) {
    float xoff = 0f;
    float yoff = 0f;
    float lastWidth = 0f;
    int newlineCount = 0;
    int p = 0;
    int x = 0, y = 0;
    GlyphLayout.GlyphRun run;
    for (int i = 0; i < layout.runs.size; i++) {
      run = layout.runs.get(i);
      System.out.println(run.x + ", " + run.y);
    }
    return new Vector2();
  }

}
