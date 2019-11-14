package me.whizvox.lyrical.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StringUtils {

  public static void tokenize(String str, char c, int max, List<String> output) {
    output.clear();
    int lastIndex = 0;
    for (int i = 0; i < str.length() && output.size() < max - 1; i++) {
      if (str.charAt(i) == c) {
        output.add(str.substring(lastIndex, i));
        lastIndex = i + 1;
      }
    }
    output.add(str.substring(lastIndex));
  }

  private static final char[] FORBIDDEN_FILENAME_CHARS;
  static {
    // first 31 control characters
    List<Character> forbiddenChars = new ArrayList<>();
    for (int i = 0; i < 31; i++) {
      forbiddenChars.add((char)i);
    }
    Collections.addAll(forbiddenChars,
        '/', '\\', '<', '>', ':', '"', '|', '?', '*'
    );
    Collections.sort(forbiddenChars);
    FORBIDDEN_FILENAME_CHARS = new char[forbiddenChars.size()];
    for (int i = 0; i < forbiddenChars.size(); i++) {
      FORBIDDEN_FILENAME_CHARS[i] = forbiddenChars.get(i);
    }
  }

  public static String filterFileName(String name) {
    StringBuilder sb = new StringBuilder(name.length());
    char c;
    for (int i = 0; i < name.length(); i++) {
      c = name.charAt(i);
      if (Arrays.binarySearch(FORBIDDEN_FILENAME_CHARS, c) >= 0 || (i == name.length() - 1 && c == '.')) {
        sb.append('_');
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

}
