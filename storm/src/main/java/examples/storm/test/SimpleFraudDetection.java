package examples.storm.test;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.LocalDRPC;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import storm.trident.TridentTopology;
import storm.trident.operation.BaseFilter;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;

import java.util.concurrent.TimeUnit;

/**
 * Created by ipogudin on 11/15/15.
 */
public class SimpleFraudDetection {

  static final String IMPRESSION = "i";
  static final String CLICK = "c";

  static final int ID = 0;
  static final String ID_FIELD = "id";
  static final int TYPE = 1;
  static final String TYPE_FIELD = "type";
  static final int USER_ID = 2;
  static final String USER_ID_FIELD = "user_id";
  static final int TIMESTAMP = 3;
  static final String TIMESTAMP_FIELD = "timestamp";

  static final int IMP_USER_ID = 4;
  static final String IMP_USER_ID_FIELD = "imp_user_id";
  static final int IMP_TIMESTAMP = 5;
  static final String IMP_TIMESTAMP_FIELD = "imp_timestamp";
  static final int TIME_FRAUD = 6;
  static final String TIME_FRAUD_FIELD = "time_fraud";
  static final int USER_FRAUD = 7;
  static final String USER_FRAUD_FIELD = "user_fraud";

  public static class Throttler extends BaseFilter {

    private final int timeout;

    public Throttler(int timeout) {
      this.timeout = timeout;
    }

    public boolean isKeep(TridentTuple tuple) {
      try {
        Thread.sleep(timeout);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      return true;
    }
  }

  public static class Parser extends BaseFunction {

    public void execute(TridentTuple tuple, TridentCollector collector) {
      String raw = tuple.getString(0);
      String[] parsed = raw.split(";");
      collector.emit(new Values(parsed[ID], parsed[TYPE], parsed[USER_ID], parsed[TIMESTAMP]));
    }
  }

  public static class Printer extends BaseFilter {
    public boolean isKeep(TridentTuple tuple) {
      System.out.println(tuple);
      return true;
    }
  }

  public static class Matcher extends BaseFunction {

    private final Cache<String, TridentTuple> matchingCache;

    public Matcher() {
      matchingCache = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.SECONDS).build();
    }

    public void execute(TridentTuple tuple, final TridentCollector collector) {
      String type = tuple.getString(TYPE);
      if (IMPRESSION.equalsIgnoreCase(type)) {
        matchingCache.put(tuple.getString(ID), tuple);
      }
      else if (CLICK.equalsIgnoreCase(type)) {
        TridentTuple impression = matchingCache.getIfPresent(tuple.getString(ID));
        if (impression == null) {
          collector.emit(new Values("", ""));
        }
        else {
          collector.emit(new Values(impression.get(USER_ID), impression.get(TIMESTAMP)));
        }
      }
    }
  }

  public static class TimeFraud extends BaseFunction {
    @Override
    public void execute(TridentTuple tuple, TridentCollector collector) {
      long impressionTimestamp = 0;
      try {
        impressionTimestamp = Long.valueOf(String.valueOf(tuple.get(IMP_TIMESTAMP)));
      } catch (NumberFormatException e) {}
      long clickTimestamp = 0;
      try {
        clickTimestamp = Long.valueOf(String.valueOf(tuple.get(TIMESTAMP)));
      } catch (NumberFormatException e) {}
      long period = clickTimestamp - impressionTimestamp;
      if (period < 1000 || period > 60000) {
        collector.emit(new Values(true));
      }
      else {
        collector.emit(new Values(false));
      }
    }
  }

  public static class UserFraud extends BaseFunction {
    @Override
    public void execute(TridentTuple tuple, TridentCollector collector) {
      String imprpessionUserId = String.valueOf(tuple.get(IMP_USER_ID));
      String clickUserId = String.valueOf(tuple.get(USER_ID));
      if (imprpessionUserId!= null && !imprpessionUserId.equalsIgnoreCase(clickUserId)) {
        collector.emit(new Values(true));
      }
      else {
        collector.emit(new Values(false));
      }
    }
  }

  public static StormTopology buildTopology(LocalDRPC drpc) {
    BatchGeneratorSpout spout = new BatchGeneratorSpout(new Fields("raw"), 2, new LogGenerator());

    TridentTopology topology = new TridentTopology();
      topology
            .newStream("raw-source", spout)
            .each(new Fields("raw"), new Throttler(300))
            .each(new Fields("raw"), new Parser(), new Fields(ID_FIELD, TYPE_FIELD, USER_ID_FIELD, TIMESTAMP_FIELD))
            .each(new Fields(ID_FIELD, TYPE_FIELD, USER_ID_FIELD, TIMESTAMP_FIELD),
                    new Matcher(), new Fields(IMP_USER_ID_FIELD, IMP_TIMESTAMP_FIELD))
            .each(new Fields(ID_FIELD, TYPE_FIELD, USER_ID_FIELD, TIMESTAMP_FIELD, IMP_USER_ID_FIELD, IMP_TIMESTAMP_FIELD),
                    new TimeFraud(), new Fields(TIME_FRAUD_FIELD))
            .each(new Fields(ID_FIELD, TYPE_FIELD, USER_ID_FIELD, TIMESTAMP_FIELD, IMP_USER_ID_FIELD, IMP_TIMESTAMP_FIELD),
                    new UserFraud(), new Fields(USER_FRAUD_FIELD))
            .each(
                    new Fields(ID_FIELD, TYPE_FIELD, USER_ID_FIELD, TIMESTAMP_FIELD, IMP_USER_ID_FIELD, IMP_TIMESTAMP_FIELD, TIME_FRAUD_FIELD, USER_FRAUD_FIELD),
                    new Printer());

    return topology.build();
  }

  public static void main(String[] args) throws Exception {
    Config conf = new Config();
    conf.setMaxSpoutPending(20);
    if (args.length == 0) {
      LocalDRPC drpc = new LocalDRPC();
      LocalCluster cluster = new LocalCluster();
      cluster.submitTopology("fraudDetection", conf, buildTopology(drpc));
      /*for (int i = 0; i < 100; i++) {
        System.out.println("DRPC RESULT: " + drpc.execute("words", "cat the dog jumped"));
        Thread.sleep(1000);
      }*/
    }
    else {
      conf.setNumWorkers(3);
      StormSubmitter.submitTopologyWithProgressBar(args[0], conf, buildTopology(null));
    }
  }

}
