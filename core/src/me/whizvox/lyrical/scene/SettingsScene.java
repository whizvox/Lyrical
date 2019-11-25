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
import me.whizvox.lyrical.graphics.DynamicTextBox;
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

    options = new Option[4];

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

    // volume
    options[2] = new SliderOption(
        "Music volume", settings.getInt(Reference.Settings.MUSIC_VOLUME, Reference.Defaults.MUSIC_VOLUME), 0, 100
    );

    // apply
    options[3] = new ApplyOption();

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
        settings.set(Reference.Settings.MUSIC_VOLUME, ((SliderOption) options[2]).getValue());
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
      applyTb = TextBox.create(font, "[YELLOW]Apply", new Rectangle(
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
          sr.setColor(Color.DARK_GRAY);
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
      gm.drawTextBox(applyTb);
      if (active) {
        gm.drawTextBox(confirmTb);
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
    private DynamicTextBox choiceTb;
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
        text = "[YELLOW]" + choices[selected];
      }
      choiceTb.updateText(text);
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
      BitmapFont uiFont = gm.getFont(Lyrical.FONT_UI);

      labelTb = TextBox.create(
          uiFont, label + ": ",
          new Rectangle(ePad, yoff - elementHeight, gm.getWidth() - (ePad * 2), elementHeight),
          TextAlign.LEFT.value, Color.WHITE, false, null
      );
      choiceTb = DynamicTextBox.create(
          uiFont, "",
          new Rectangle(
            labelTb.outerBounds.x + labelTb.glyphLayout.width + (ePad * 2),
            labelTb.outerBounds.y,
            gm.getWidth() - (labelTb.outerBounds.x + labelTb.glyphLayout.width),
            elementHeight
          ), TextAlign.LEFT.value, Color.WHITE, false, null);
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
            selected = choices.length - 1;
          }
          updateChoiceTextBox();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
          if (++selected >= choices.length) {
            selected = 0;
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
        sr.setColor(Color.DARK_GRAY);
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
      gm.drawTextBox(labelTb);
      gm.drawTextBox(choiceTb);
      sb.end();
    }

    @Override
    public void dispose() {}
  }

  public class SliderOption implements Option {

    public final int min, max;

    private int value;
    private int prevValue;
    private String label;

    private TextBox labelTb;
    private DynamicTextBox valueTb;

    private float elementHeight;
    private float barThickness;

    private Rectangle barBounds;
    private Rectangle cursorBounds;
    private float xoff;

    private boolean active;

    private long lastLeft, lastRight;

    public SliderOption(String label, int initialValue, int min, int max) {
      this.label = label;
      this.value = initialValue;
      this.min = min;
      this.max = max;
      active = false;
    }

    private void updateXOffset() {
      xoff = ((float) value / (max - min)) * barBounds.width - barThickness / 2f;
    }

    private void _setInactive() {
      active = false;
      valueTb.updateColor(Color.YELLOW);
      setInactive();
    }

    public int getValue() {
      return value;
    }

    @Override
    public float getHeight() {
      return elementHeight;
    }

    @Override
    public void create(float yoff) {
      lastLeft = 0;
      lastRight = 0;

      BitmapFont uiFont = gm.getFont(Lyrical.FONT_UI);
      elementHeight = uiFont.getLineHeight();
      barThickness = gm.getWidth() / 400f;

      labelTb = DynamicTextBox.create(
          uiFont, label + ": ",
          new Rectangle(ePad, yoff - elementHeight, gm.getWidth() - (ePad * 2), elementHeight),
          TextAlign.LEFT.value, Color.WHITE, false, null
      );

      barBounds = new Rectangle(
          labelTb.textPosition.x + labelTb.glyphLayout.width + (ePad * 2),
          labelTb.textPosition.y + (labelTb.glyphLayout.height / 2f) - (barThickness / 2f),
          gm.getWidth() * 0.25f,
          barThickness
      );
      cursorBounds = new Rectangle(
          barBounds.x, barBounds.y - barThickness * 3, barThickness, barThickness * 7
      );
      updateXOffset();

      valueTb = DynamicTextBox.create(
          uiFont, "",
          new Rectangle(
              barBounds.x + barBounds.width + (ePad * 2),
              labelTb.outerBounds.y,
              gm.getWidth() - (labelTb.outerBounds.x + labelTb.glyphLayout.width - barBounds.width),
              elementHeight
          ),
          TextAlign.LEFT.value, Color.YELLOW, false, null
      );
    }

    @Override
    public void onActive() {
      active = true;
      valueTb.updateColor(Color.WHITE);
      prevValue = value;
    }

    @Override
    public void render(boolean selected) {
      if (active) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
          value = prevValue;
          updateXOffset();
          _setInactive();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
          _setInactive();
        }
        final long t = System.currentTimeMillis();
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
          lastLeft = t;
          if (--value < min) {
            value = min;
          }
          updateXOffset();
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
          if (t - lastLeft >= 500 && --value < min) {
            value = min;
          }
          updateXOffset();
        } else if (lastLeft != 0) {
          lastLeft = 0;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
          lastRight = t;
          if (++value > max) {
            value = max;
          }
          updateXOffset();
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
          if (lastRight == 0) {
            lastRight = t;
          }
          if (t - lastRight >= 500 && ++value > max) {
            value = max;
          }
          updateXOffset();
        } else if (lastRight != 0) {
          lastRight = 0;
        }
      }

      ShapeRenderer sr = gm.getShapeRenderer();
      SpriteBatch sb = gm.getBatch();

      sr.setColor(Color.WHITE);
      sr.begin(ShapeRenderer.ShapeType.Filled);
      sr.rect(barBounds.x, barBounds.y, barBounds.width, barBounds.height);
      if (active) {
        sr.setColor(Color.PURPLE);
      }
      sr.rect(cursorBounds.x + xoff, cursorBounds.y, cursorBounds.width, cursorBounds.height);
      if (selected || active) {
        if (!active) {
          sr.setColor(Color.DARK_GRAY);
        }
        sr.rect(valueTb.textPosition.x - ePad,
            valueTb.textPosition.y - ePad,
            valueTb.glyphLayout.width + (ePad * 2),
            valueTb.glyphLayout.height + (ePad * 2)
        );
      }
      sr.end();

      valueTb.updateText("[YELLOW]" + value);

      sb.begin();
      gm.drawTextBox(labelTb);
      gm.drawTextBox(valueTb);
      sb.end();
    }

    @Override
    public void dispose() {}
  }

}
