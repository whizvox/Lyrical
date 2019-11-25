package me.whizvox.lyrical.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import me.whizvox.lyrical.util.MathUtils;

public class TextBox {

  public BitmapFont font;
  public GlyphLayout glyphLayout;
  public Rectangle outerBounds;
  public Vector2 textPosition;

  public TextBox(BitmapFont font, GlyphLayout glyphLayout, Rectangle outerBounds, Vector2 textPosition) {
    this.font = font;
    this.glyphLayout = glyphLayout;
    this.outerBounds = outerBounds;
    this.textPosition = textPosition;
  }

  public static TextBox create(BitmapFont font, GlyphLayout glyphLayout, Rectangle outerBounds, int align) {
    return new TextBox(font, glyphLayout, outerBounds,
        MathUtils.alignRectangle(outerBounds, new Vector2(glyphLayout.width, glyphLayout.height), align)
    );
  }

  public static TextBox create(BitmapFont font, String text, Rectangle outerBounds, int align, Color color, boolean wrap, String truncate) {
    return create(font, new GlyphLayout(font, text, 0, text.length(), color, outerBounds.width, align, wrap, truncate), outerBounds, align);
  }

}
