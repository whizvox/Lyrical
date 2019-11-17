package me.whizvox.lyrical.scene;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import me.whizvox.lyrical.Lyrical;
import me.whizvox.lyrical.graphics.GraphicsManager;
import me.whizvox.lyrical.graphics.TextAlign;
import me.whizvox.lyrical.graphics.TextBox;

public class FallbackScene extends ApplicationAdapter {

  private GraphicsManager gm;
  private TextBox tb;

  public FallbackScene(GraphicsManager gm) {
    this.gm = gm;
  }

  @Override
  public void create() {
    tb = TextBox.create(gm.getFont(Lyrical.FONT_DISPLAY), "Uhh, you're not supposed to see this...", new Rectangle(0, 0, gm.getWidth(), gm.getHeight()), TextAlign.CENTER.value, Color.RED, false, null);
  }

  @Override
  public void render() {
    gm.getBatch().begin();
    gm.drawTextBox(Lyrical.FONT_DISPLAY, tb);
    gm.getBatch().end();
  }

}
