package examples.storm.test;

import backtype.storm.Config;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Fields;
import storm.trident.operation.TridentCollector;
import storm.trident.spout.IBatchSpout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by ipogudin on 11/15/15.
 */
public class BatchGeneratorSpout implements IBatchSpout {
  private Fields fields;
  private Supplier<List<Object>> generator;
  private int maxBatchSize;
  private HashMap<Long, List<List<Object>>> batches = new HashMap<Long, List<List<Object>>>();

  public BatchGeneratorSpout(Fields fields, int maxBatchSize, Supplier<List<Object>> generator) {
    this.fields = fields;
    this.generator = generator;
    this.maxBatchSize = maxBatchSize;
  }

  @Override
  public void open(Map conf, TopologyContext context) {

  }

  @Override
  public void emitBatch(long batchId, TridentCollector collector) {
    List<List<Object>> batch = this.batches.get(batchId);
    if(batch == null){
      batch = new ArrayList<List<Object>>();
      for(int i=0; i < maxBatchSize; i++) {
        batch.add(generator.get());
      }
      this.batches.put(batchId, batch);
    }
    for(List<Object> list : batch){
      collector.emit(list);
    }
  }

  @Override
  public void ack(long batchId) {
    this.batches.remove(batchId);
  }

  @Override
  public void close() {
  }

  @Override
  public Map getComponentConfiguration() {
    Config conf = new Config();
    conf.setMaxTaskParallelism(1);
    return conf;
  }

  @Override
  public Fields getOutputFields() {
    return fields;
  }
}
