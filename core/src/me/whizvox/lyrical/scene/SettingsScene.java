package me.whizvox.lyrical.scene;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import me.whizvox.lyrical.Lyrical;
import me.whizvox.lyrical.Reference;
import me.whizvox.lyrical.Settings;
import me.whizvox.lyrical.TextEntryProcessor;
import me.whizvox.lyrical.graphics.GraphicsManager;
import me.whizvox.lyrical.graphics.TextAlign;
import me.whizvox.lyrical.graphics.TextBox;
import me.whizvox.lyrical.util.InputUtils;
import me.whizvox.lyrical.util.MathUtils;

import java.io.IOException;
import java.io.Writer;

public class SettingsScene extends ApplicationAdapter {

  private GraphicsManager gm;
  private TextEntryProcessor tep;

  private Option[] options;
  private int[][] resOptions = {
      {800, 450},
      {800, 600},
      {1200, 675},
      {1200, 900},
      {1600, 900},
      {1600, 1200},
      {1920, 1080},
      {1920, 1440},
      {2560, 1440}
  };

  private boolean active;
  private int selected;
  private boolean shouldApply;
  private boolean shouldExit;

  private Settings settings;

  private int prevWidth, prevHeight;

  private float ePad;

  public SettingsScene(GraphicsManager gm) {
    this.gm = gm;
    prevWidth = -1;
    prevHeight = -1;
  }

  @Override
  public void create() {
    ePad = gm.getWidth() / 120f;

    settings = Lyrical.getInstance().getSettings();
    tep = new TextEntryProcessor();
    Gdx.input.setInputProcessor(tep);

    options = new Option[3];

    // resolution
    int resWidth = settings.getInt(Reference.Settings.RESOLUTION_WIDTH, RESOLUTION_WIDTH_DEFAULT);
    int resHeight = settings.getInt(Reference.Settings.RESOLUTION_HEIGHT, RESOLUTION_HEIGHT_DEFAULT);
    int defaultIndex = -1;
    for (int i = 0; i < resOptions.length; i++) {
      if (resOptions[i][0] == resWidth && resOptions[i][1] == resHeight) {
        defaultIndex = i;
        break;
      }
    }
    String[] strResOptions = new String[resOptions.length + (defaultIndex != -1 ? 0 : 1)];
    for (int i = 0; i < resOptions.length; i++) {
      int w = resOptions[i][0];
      int h = resOptions[i][1];
      int gcd = MathUtils.gcd(w, h);
      strResOptions[i] = w + "x" + h + " (" + (w / gcd) + ":" + (h / gcd) + ")";
    }
    if (defaultIndex == -1) {
      int gcd = MathUtils.gcd(resWidth, resHeight);
      strResOptions[strResOptions.length - 1] = resWidth + "x" + resHeight + " (" + (resWidth / gcd) + ":" + (resHeight / gcd) + ") [Custom]";
    }
    options[0] = new ChoiceOption("Internal resolution", strResOptions, defaultIndex == -1 ? strResOptions.length - 1 : defaultIndex);

    // fullscreen
    boolean fs = settings.getBool(Reference.Settings.FULLSCREEN, false);
    options[1] = new ChoiceOption("Fullscreen", new String[] {"No", "Yes"}, fs ? 1 : 0);

    // apply
    options[2] = new ApplyOption();

    float yoff = gm.getHeight() - ePad;
    for (Option option : options) {
      option.create(yoff);
      yoff -= (option.getHeight() + (ePad * 2));
    }

    selected = 0;
    setInactive();

    shouldApply = false;
    shouldExit = false;
  }

  @Override
  public void render() {
    if (shouldExit) {
      if (shouldApply) {
        ChoiceOption resOption = (ChoiceOption) options[0];
        final int resI = resOption.getSelectedIndex();
        int resW, resH;
        if (resI != resOptions.length) {
          resW = resOptions[resI][0];
          resH = resOptions[resI][1];
          settings.set(Reference.Settings.RESOLUTION_WIDTH, resW);
          settings.set(Reference.Settings.RESOLUTION_HEIGHT, resH);
        } else {
          resW = settings.getInt(Reference.Settings.RESOLUTION_WIDTH, Reference.Defaults.RESOLUTION_WIDTH);
          resH = settings.getInt(Reference.Settings.RESOLUTION_HEIGHT, Reference.Defaults.RESOLUTION_HEIGHT);
        }
        final boolean fs = ((ChoiceOption) options[1]).getSelectedIndex() != 0;
        settings.set(Reference.Settings.FULLSCREEN, fs);
        try (Writer writer = Reference.Files.SETTINGS.writer(false, "UTF-8")) {
          settings.save(writer);
          System.out.println("Settings saved at " + Reference.Files.SETTINGS.path());
        } catch (IOException e) {
          System.err.println("Could not save settings at " + Reference.Files.SETTINGS.path());
          e.printStackTrace();
        }
        if (fs) {
          if (!Gdx.graphics.isFullscreen()) {
            prevWidth = Gdx.graphics.getWidth();
            prevHeight = Gdx.graphics.getHeight();
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
          }
        } else {
          if (Gdx.graphics.isFullscreen()) {
            if (prevWidth == -1 || prevHeight == -1) {
              prevWidth = Gdx.graphics.getWidth();
              prevHeight = Gdx.graphics.getHeight();
            }
            Gdx.graphics.setWindowedMode(prevWidth, prevHeight);
          }
        }
        gm.setResolution(resW, resH);
      }
      Lyrical.getInstance().switchScene(Lyrical.SCENE_TITLE);
      return;
    }

    boolean setActive = false;

    if (!active) {
      if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
        if (--selected < 0) {
          selected = options.length - 1;
        }
      } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
        if (++selected >= options.length) {
          selected = 0;
        }
      } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
        Lyrical.getInstance().switchScene(Lyrical.SCENE_TITLE);
        return;
      } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
        active = true;
        setActive = true;
      }
    }

    for (int i = 0; i < options.length; i++) {
      options[i].render(i == selected);
    }

    if (setActive) {
      options[selected].onActive();
    }
  }

  public void setInactive() {
    active = false;
  }

  public void setShouldApply() {
    shouldApply = true;
  }

  public void setShouldExit() {
    shouldExit = true;
  }

  public static final int
      RESOLUTION_WIDTH_DEFAULT = 800,
      RESOLUTION_HEIGHT_DEFAULT = 450;

  public interface Option extends Disposable {
    float getHeight();
    void create(float yoff);
    void onActive();
    void render(boolean selected);
  }

  public class ApplyOption implements Option {

    private float elementHeight;
    private TextBox applyTb;
    private TextBox confirmTb;
    private boolean active;

    @Override
    public float getHeight() {
      return elementHeight;
    }

    @Override
    public void create(float yoff) {
      BitmapFont font = gm.getFont(Lyrical.FONT_UI);
      elementHeight = font.getLineHeight();
      applyTb = TextBox.create(font, "Apply", new Rectangle(
          ePad, yoff - elementHeight, gm.getWidth() - (ePad * 2), elementHeight
      ), TextAlign.LEFT.value, Color.WHITE, false, null);
      confirmTb = TextBox.create(gm.getFont(Lyrical.FONT_UI),
          "[YELLOW]Are you sure you want to apply these settings?[]\n" +
          "[GRAY][[Esc][] Cancel | " +
          "[GRAY][[Enter][] Confirm | " +
          "[GRAY][[Ctrl][]+[GRAY][[Enter][] [CORAL]Confirm without saving[]",
          new Rectangle(0, 0, gm.getWidth(), gm.getHeight()), TextAlign.CENTER.value, Color.WHITE, false, null);
    }

    @Override
    public void onActive() {
      active = true;
    }

    @Override
    public void render(boolean selected) {
      ShapeRenderer sr = gm.getShapeRenderer();
      SpriteBatch sb = gm.getBatch();

      if (active) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
          if (InputUtils.isMetaKeyPressed(InputUtils.META_CTRL)) {
            setShouldExit();
            setInactive();
            active = false;
            return;
          } else {
            setShouldExit();
            setShouldApply();
            setInactive();
            active = false;
            return;
          }
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
          setInactive();
          active = false;
        }
      }

      if (active || selected) {
        if (active) {
          sr.setColor(Color.PURPLE);
        } else {
          sr.setColor(Color.GRAY);
        }
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.rect(
            applyTb.textPosition.x - ePad,
            applyTb.textPosition.y - ePad,
            applyTb.glyphLayout.width + (ePad * 2),
            applyTb.glyphLayout.height + (ePad * 2)
        );
        sr.end();
      }

      sb.begin();
      gm.drawTextBox(Lyrical.FONT_UI, applyTb);
      if (active) {
        gm.drawTextBox(Lyrical.FONT_UI, confirmTb);
      }
      sb.end();
    }

    @Override
    public void dispose() {

    }
  }

  public class ChoiceOption implements Option {

    private String label;
    private Object[] choices;

    private TextBox labelTb;
    private TextBox choiceTb;
    private int selected;
    private int prevSelected;

    private boolean active;
    private float elementHeight;

    public ChoiceOption(String label, Object[] choices, int initialChoice) {
      this.label = label;
      this.choices = choices;
      selected = initialChoice;
    }

    private void updateChoiceTextBox() {
      String text;
      if (choices.length == 0) {
        text = "[RED]INVALID[]";
      } else {
        if (selected < 0 || selected >= choices.length) {
          selected = 0;
        }
        text = String.valueOf(choices[selected]);
      }
      choiceTb = TextBox.create(gm.getFont(Lyrical.FONT_UI), text, new Rectangle(
          labelTb.outerBounds.x + labelTb.glyphLayout.width + (ePad * 2),
          labelTb.outerBounds.y,
          gm.getWidth() - labelTb.outerBounds.width,
          elementHeight
      ), TextAlign.LEFT.value, Color.WHITE, false, null);
    }

    public int getSelectedIndex() {
      return selected;
    }

    public Object getSelected() {
      return choices[selected];
    }

    @Override
    public float getHeight() {
      return elementHeight;
    }

    @Override
    public void create(float yoff) {
      elementHeight = gm.getFont(Lyrical.FONT_UI).getLineHeight();
      labelTb = TextBox.create(gm.getFont(Lyrical.FONT_UI), label + ": ",
          new Rectangle(
            ePad,
            yoff - elementHeight,
            gm.getWidth() - (ePad * 2),
            elementHeight
          ),
          TextAlign.LEFT.value,
          Color.WHITE,
          false,
          null
      );
      updateChoiceTextBox();
      active = false;
    }

    @Override
    public void onActive() {
      if (choices.length == 0) {
        setInactive();
      } else {
        prevSelected = selected;
        active = true;
      }
    }

    @Override
    public void render(boolean isSelected) {
      if (active) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
          if (--selected < 0) {
            selected = 0;
          }
          updateChoiceTextBox();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
          if (++selected >= choices.length) {
            selected = choices.length - 1;
          }
          updateChoiceTextBox();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
          setInactive();
          active = false;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
          selected = prevSelected;
          updateChoiceTextBox();
          setInactive();
          active = false;
        }
      }

      ShapeRenderer sr = gm.getShapeRenderer();
      SpriteBatch sb = gm.getBatch();
      if (active) {
        sr.setColor(Color.PURPLE);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.rect(
            choiceTb.textPosition.x - ePad,
            choiceTb.textPosition.y - ePad,
            choiceTb.glyphLayout.width + (ePad * 2),
            choiceTb.glyphLayout.height + (ePad * 2)
        );
        sr.end();
      } else if (isSelected) {
        sr.setColor(Color.GRAY);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.rect(
            choiceTb.textPosition.x - ePad,
            choiceTb.textPosition.y - ePad,
            choiceTb.glyphLayout.width + (ePad * 2),
            choiceTb.glyphLayout.height + (ePad * 2)
        );
        sr.end();
      }
      sb.begin();
      gm.drawTextBox(Lyrical.FONT_UI, labelTb);
      gm.drawTextBox(Lyrical.FONT_UI, choiceTb);
      sb.end();
    }

    @Override
    public void dispose() {}
  }

}
