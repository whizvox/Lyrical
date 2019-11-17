package me.whizvox.lyrical;

import me.whizvox.lyrical.util.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Settings {

  private Map<String, String> settings;

  public Settings() {
    settings = new HashMap<>();
  }

  public boolean containsKey(String key) {
    return settings.containsKey(key);
  }

  public <T> T get(String key, T def, Function<String, T> decoder, Function<T, String> encoder) {
    if (!settings.containsKey(key)) {
      settings.put(key, encoder.apply(def));
      return def;
    }
    final String valueStr = settings.get(key);
    try {
      return decoder.apply(valueStr);
    } catch (Exception e) {
      System.err.println("Invalid setting for " + key + ": " + valueStr);
      System.err.println("\t" + e.getMessage());
      settings.put(key, encoder.apply(def));
    }
    return def;
  }

  public String getString(String key, String def) {
    return get(key, def, s -> s, s -> s);
  }

  public int getInt(String key, int def) {
    return get(key, def, Integer::parseInt, String::valueOf);
  }

  public boolean getBool(String key, boolean def) {
    return get(key, def, StringUtils::parseBoolean, b -> Boolean.toString(b));
  }

  public <T> void set(String key, T value, Function<T, String> encoder) {
    settings.put(key, encoder.apply(value));
  }

  public void set(String key, Object value) {
    set(key, value, String::valueOf);
  }

  public void save(Writer writer) throws IOException {
    BufferedWriter w = new BufferedWriter(writer);
    List<String> orderedKeys = new ArrayList<>(settings.keySet());
    orderedKeys.sort(String::compareTo);
    for (String key : orderedKeys) {
      w.write(key);
      w.write('=');
      w.write(settings.get(key));
      w.newLine();
    }
    w.flush();
  }

  public void load(Reader reader) throws IOException {
    BufferedReader r = new BufferedReader(reader);
    settings.clear();
    String line;
    List<String> tokens = new ArrayList<>(2);
    while ((line = r.readLine()) != null) {
      StringUtils.tokenize(line, '=', 2, tokens);
      if (tokens.size() != 2) {
        System.err.println("Invalid line while reading settings: \"" + line + "\"");
      } else {
        settings.put(tokens.get(0), tokens.get(1));
      }
    }
  }

}
