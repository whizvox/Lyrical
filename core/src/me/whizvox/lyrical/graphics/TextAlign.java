package me.whizvox.lyrical.graphics;

import static com.badlogic.gdx.utils.Align.*;

public enum TextAlign {

  BOTTOM_LEFT(bottomLeft),
  LEFT(left),
  TOP_LEFT(topLeft),
  BOTTOM_CENTER(bottom | center),
  CENTER(center),
  TOP_CENTER(top | center),
  BOTTOM_RIGHT(bottomRight),
  RIGHT(right),
  TOP_RIGHT(topRight);

  public final int value;

  TextAlign(int value) {
    this.value = value;
  }

}
