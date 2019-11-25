package me.whizvox.lyrical.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import me.whizvox.lyrical.util.MathUtils;

public class DynamicTextBox extends TextBox {

  public String text;
  public int align;
  public Color color;
  public boolean wrap;
  public String truncate;

  public DynamicTextBox(BitmapFont font, GlyphLayout glyphLayout, Rectangle outerBounds, Vector2 textPosition,
                        String text, int align, Color color, boolean wrap, String truncate) {
    super(font, glyphLayout, outerBounds, textPosition);
    this.text = text;
    this.align = align;
    this.color = color;
    this.wrap = wrap;
    this.truncate = truncate;
  }

  public void reloadGlyphLayout() {
    glyphLayout = new GlyphLayout(font, text, 0, text.length(), color, outerBounds.width, align, wrap, truncate);
  }

  public void reloadTextPosition() {
    textPosition = MathUtils.alignRectangle(outerBounds, new Vector2(glyphLayout.width, glyphLayout.height), align);
  }

  public void updateFont(BitmapFont newFont) {
    this.font = newFont;
    reloadGlyphLayout();
    reloadTextPosition();
  }

  public void updateOuterBounds(Rectangle newOuterBounds) {
    outerBounds = newOuterBounds;
    reloadTextPosition();
  }

  public void updateAlignment(int newAlign) {
    align = newAlign;
    reloadTextPosition();
  }

  public void updateText(String newText) {
    text = newText;
    reloadGlyphLayout();
    reloadTextPosition();
  }

  public void updateColor(Color newColor) {
    color = newColor;
    reloadGlyphLayout();
  }

  public static DynamicTextBox create(BitmapFont font, String text, Rectangle outerBounds, int align, Color color,
                                      boolean wrap, String truncate) {
    TextBox tb = TextBox.create(font, text, outerBounds, align, color, wrap, truncate);
    return new DynamicTextBox(
        tb.font, tb.glyphLayout, tb.outerBounds, tb.textPosition, text, align, color, wrap, truncate
    );
  }

}
