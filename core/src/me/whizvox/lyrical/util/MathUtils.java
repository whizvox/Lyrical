package me.whizvox.lyrical.util;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;

public class MathUtils {

  public static int gcd(int a, int b) {
    if (b == 0) {
      return a;
    }
    return gcd(b, a % b);
  }

  public static Vector2 alignRectangle(Rectangle outerBounds, Vector2 rectSize, int align) {
    float x = outerBounds.x;
    float y = outerBounds.y;
    if (Align.isCenterHorizontal(align)) {
      x += (outerBounds.width / 2) - (rectSize.x / 2);
    } else if (Align.isRight(align)) {
      x += outerBounds.width - rectSize.x;
    }
    if (Align.isCenterVertical(align)) {
      y += (outerBounds.height - rectSize.y) / 2.0f;
    } else if (Align.isTop(align)) {
      y += outerBounds.height - rectSize.y;
    }
    return new Vector2(x, y);
  }

}
