package examples.java;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.infra.Blackhole;

public class TypesComparison {
  
  private static final int RANDOM_INT = new Random().nextInt();
  private static final Integer RANDOM_INTEGER = RANDOM_INT;

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void justReturnIncrementedInt(Blackhole bh) {
    bh.consume(incInt(RANDOM_INT));
  }

  public int incInt(int i) {
    return i;
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void primitiveInt(Blackhole bh) {
    bh.consume(calculateWithInt(RANDOM_INT));
  }
  
  public int calculateWithInt(int randomNumber) {
    int counter = 0;
    for (int i = 0; i < 10000; i++) {
      counter += randomNumber;
    }
    return counter;
  }
  
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void wrapperInteger(Blackhole bh) {
    bh.consume(calculateWithInteger(RANDOM_INTEGER));
  }
    

  public int calculateWithInteger(Integer randomNumber) {
    Integer counter = 0;
    for (int i = 0; i < 10000; i++) {
      counter += randomNumber;
    }
    return counter;
  }
  
}
