package examples.java;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

public class TypesComparison {
  
  private static final int RANDOM_INT = new Random().nextInt();
  private static final Integer RANDOM_INTEGER = RANDOM_INT;

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public void primitiveInt() {
    int result = calculateWithInt(RANDOM_INT);
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
  public void wrapperInteger() {
    int result = calculateWithInteger(RANDOM_INTEGER);
  }
    

  public int calculateWithInteger(Integer randomNumber) {
    Integer counter = 0;
    for (int i = 0; i < 10000; i++) {
      counter += randomNumber;
    }
    return counter;
  }
  
}
