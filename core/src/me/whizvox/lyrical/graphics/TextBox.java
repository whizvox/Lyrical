package me.whizvox.lyrical.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;

public class TextBox {

  public GlyphLayout glyphLayout;
  public Rectangle outerBounds;
  public Vector2 textPosition;

  public TextBox(GlyphLayout glyphLayout, Rectangle outerBounds, Vector2 textPosition) {
    this.glyphLayout = glyphLayout;
    this.outerBounds = outerBounds;
    this.textPosition = textPosition;
  }

  public static TextBox create(GlyphLayout glyphLayout, Rectangle outerBounds, int align) {
    float x = outerBounds.x;
    float y = outerBounds.y;
    if (Align.isCenterHorizontal(align)) {
      x += (outerBounds.width / 2) - (glyphLayout.width / 2);
    } else if (Align.isRight(align)) {
      x += outerBounds.width - glyphLayout.width;
    }
    if (Align.isCenterVertical(align)) {
      y += (outerBounds.height - glyphLayout.height) / 2.0f;
    } else if (Align.isTop(align)) {
      y += outerBounds.height - glyphLayout.height;
    }
    return new TextBox(glyphLayout, outerBounds, new Vector2(x, y));
  }

  public static TextBox create(BitmapFont font, String text, Rectangle outerBounds, int align, Color color, boolean wrap, String truncate) {
    return create(new GlyphLayout(font, text, 0, text.length(), color, outerBounds.width, align, wrap, truncate), outerBounds, align);
  }

}
