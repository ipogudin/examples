package examples.commons;

public class Utils {

  public static void check(
      int value, int min, int max, String message, Object ... args
      ) throws IllegalArgumentException {
    if (value > max || value < min) {
      throw new IllegalArgumentException(String.format(message, args));
    }
  }
}
