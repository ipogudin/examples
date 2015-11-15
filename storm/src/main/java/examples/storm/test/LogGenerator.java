package examples.storm.test;

import backtype.storm.tuple.Values;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Created by ipogudin on 11/15/15.
 */
public class LogGenerator implements Supplier<List<Object>>, Serializable {

  static final String IMPRESSION = "i";
  static final String CLICK = "c";

  private final Cache<String, String> impressionsToUserId =
          CacheBuilder.newBuilder().expireAfterWrite(120000, TimeUnit.MILLISECONDS).build();

  @Override
  public List<Object> get() {
    final ThreadLocalRandom rand = ThreadLocalRandom.current();
    String id = String.valueOf(rand.nextLong(0, 1000000000));
    String type = IMPRESSION;
    String userId = String.valueOf(rand.nextInt(0, 10000));
    long ts = System.currentTimeMillis() - rand.nextInt(0, 120000);

    if (rand.nextDouble(0, 1) > 0.6) {
      impressionsToUserId.put(String.valueOf(id), userId);
    }
    else {
      int i = (int) impressionsToUserId.size();
      if (i > 0) {
        String impressionId = String.valueOf(impressionsToUserId.asMap().keySet().toArray()[rand.nextInt(0, i)]);
        userId = (rand.nextDouble(0, 1) < 0.5) ? impressionsToUserId.getIfPresent(impressionId) : String.valueOf(rand.nextInt(0, 10000));
        impressionsToUserId.invalidate(impressionId);
        id = impressionId;
      }
      type = CLICK;
    }
    return new Values(String.format("%s;%s;u%s;%d", id, type, userId, ts));
  }

}
