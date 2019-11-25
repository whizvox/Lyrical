package me.whizvox.lyrical.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

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
    FORBIDDEN_FILENAME_CHARS = new char[] {
        '/', '\\', '<', '>', ':', '"', '|', '?', '*'
    };
    Arrays.sort(FORBIDDEN_FILENAME_CHARS);
  }

  public static boolean parseBoolean(String s) {
    if (s != null) {
      if ("true".equalsIgnoreCase(s) || "1".equals(s)) {
        return true;
      } else if ("false".equalsIgnoreCase(s) || "0".equals(s)) {
        return false;
      }
    }
    throw new IllegalArgumentException("Not a valid boolean: " + s);
  }

  public static String filterFileName(String name) {
    StringBuilder sb = new StringBuilder(name.length());
    char c;
    for (int i = 0; i < name.length(); i++) {
      c = name.charAt(i);
      // character cannot be
      // 1) one of the 31 control characters
      // 2) a character in the FORBIDDEN_FILENAME_CHARS array
      // 3) a period (.) at the end of the string
      // linux actually supports all of this (except slash [/]), but windows doesn't
      if (c <= 31 || Arrays.binarySearch(FORBIDDEN_FILENAME_CHARS, c) >= 0 || i == name.length() - 1 && c == '.') {
        sb.append('_');
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  private static final Pattern FILE_EXTENSION = Pattern.compile("[a-zA-Z0-9]{1,16}");

  public static String getFileNameWithoutExtension(String fileName) {
    int indexOf = fileName.lastIndexOf('.');
    if (indexOf == -1) {
      return fileName;
    }
    String extension = fileName.substring(0, indexOf);
    if (FILE_EXTENSION.matcher(extension).matches()) {
      return extension;
    }
    return fileName;
  }

  public static String getFileNameExtension(String fileName) {
    int indexOf = fileName.lastIndexOf('.');
    if (indexOf == -1) {
      return "";
    }
    String extension = fileName.substring(indexOf + 1);
    if (FILE_EXTENSION.matcher(extension).matches()) {
      return extension;
    }
    return "";
  }

  public static String resolveConflictingFilePath(String root, String destFileName) {
    Path rootPath = Paths.get(root);
    Path destPath = rootPath.resolve(destFileName);
    if (!Files.exists(destPath)) {
      return destFileName;
    }
    // keeps making copies of the destFile, with the format of "file (#).ext"
    String name = getFileNameWithoutExtension(destFileName);
    String extension = getFileNameExtension(destFileName);
    Path copyPath;
    int copyIndex = 1;
    do {
      copyPath = rootPath.resolve(name + " (" + copyIndex + ")" + (extension.isEmpty() ? "" : "." + extension));
      copyIndex++;
    } while (Files.exists(copyPath));
    return copyPath.getFileName().toString();
  }

}
