package me.whizvox.lyrical.scene;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import me.whizvox.lyrical.Lyrical;
import me.whizvox.lyrical.TextEntryProcessor;
import me.whizvox.lyrical.graphics.GraphicsManager;
import me.whizvox.lyrical.graphics.TextAlign;
import me.whizvox.lyrical.graphics.TextBox;

public class TextEnterTestScene extends ApplicationAdapter {

  private GraphicsManager gm;
  private TextBox tb;
  private TextEntryProcessor ip;
  private Vector2 cursorPos;
  private float height;

  public TextEnterTestScene(GraphicsManager gm) {
    this.gm = gm;
  }

  private void updateTextBox() {
    tb = TextBox.create(gm.getFont(Lyrical.FONT_UI), ((TextEntryProcessor)Gdx.input.getInputProcessor()).getText().toString(),
        new Rectangle(5, 5, gm.getWidth() - 10, gm.getHeight() - 10), TextAlign.TOP_LEFT.value, Color.WHITE, true, null);
  }

  @Override
  public void create() {
    ip = new TextEntryProcessor();
    Gdx.input.setInputProcessor(ip);
    ip.setEnteringText(true);
    updateTextBox();

    cursorPos = new Vector2();
    height = gm.getFont(Lyrical.FONT_UI).getLineHeight();
  }

  @Override
  public void render() {
    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      Lyrical.getInstance().switchScene(Lyrical.SCENE_TITLE);
    }
    if (ip.hasTextChanged()) {
      updateTextBox();
    }

    if (ip.hasCursorPosChanged()) {
      cursorPos = ip.getOnScreenCursorPos(gm, tb);
    }

    gm.getBatch().begin();
    gm.drawTextBox(Lyrical.FONT_UI, tb);
    gm.getBatch().end();

    if (ip.drawCursorBar()) {
      gm.getShapeRenderer().begin(ShapeRenderer.ShapeType.Filled);
      gm.getShapeRenderer().setColor(Color.WHITE);
      gm.getShapeRenderer().rect(tb.textPosition.x + cursorPos.x, tb.outerBounds.y + tb.outerBounds.height + cursorPos.y, 2, height - 2);
      gm.getShapeRenderer().end();
    }
  }

}
