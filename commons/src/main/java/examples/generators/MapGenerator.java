package examples.generators;

import static examples.commons.Utils.check;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class MapGenerator {
  
  private static final int MAX_ELEMENTS = 5;
  private static final int MIN_ELEMENTS = 1;
  private static final int MAX_LEVEL = 5;
  private static final int MIN_LEVEL = 1;

  /**
   * This method generate a map with random numbers
   * @param elements
   * @param levels
   * @return
   */
  public Map<Long, Object> generate(int elements, int levels) {
    check(elements, MIN_ELEMENTS, MAX_ELEMENTS, 
        "elements=%d is not between (%d:%d)", elements, MIN_ELEMENTS, MAX_ELEMENTS);
    check(levels, MIN_LEVEL, MAX_LEVEL, 
        "levels=%d is not between (%d:%d)", elements, MIN_LEVEL, MAX_LEVEL);
    
    Map<Long, Object> map = new HashMap<>();
    
    for (int i = 0; i < elements; i++) {
      Object value;
      if (levels <= MIN_LEVEL) {
        value = ThreadLocalRandom.current().nextLong();
      }
      else {
        value = generate(elements, levels - 1);
      }
      
      map.put(
          ThreadLocalRandom.current().nextLong(), 
          value);
    }
    
    return map;
  }

}
