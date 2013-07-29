package fr.umlv.indycov;

import java.util.concurrent.atomic.AtomicIntegerArray;

public class ClassCoverage {
  private final AtomicIntegerArray coverageSet;
  
  public ClassCoverage(int size) {
    coverageSet = new AtomicIntegerArray(1 + size / 32);
  }
  
  public void cover(int index) {
    int arrayIndex = index / 32;
    int bit = index % 32;
    
    AtomicIntegerArray coverageSet = this.coverageSet;
    for(;;) {
      int expect = coverageSet.get(arrayIndex);
      int update = expect | 1 << bit;
      if (coverageSet.compareAndSet(arrayIndex, expect, update)) {
        return;
      }
    }
  }

  public boolean isCovered(int index) {
    int arrayIndex = index / 32;
    int bit = index % 32;
    
    return (coverageSet.get(arrayIndex) & (1 << bit)) != 0;
  }
}
