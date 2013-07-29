package fr.umlv.indycov;

import java.util.Arrays;

public class ClassData {
  private int[] blocks;
  private int size;
  private volatile ClassCoverage coverage;
  
  public ClassData() {
    blocks = new int[64];
  }
  
  public int addBlock(int start) {
    int size = this.size;
    int[] blocks = this.blocks;
    if (size == blocks.length) {
      this.blocks = blocks = Arrays.copyOf(blocks, size << 1);
    }
    blocks[size] = start;
    //blocks[size + 1] = -1;
    this.size = size + 2;
    return size >> 1;
  }
  
  public void endBlock(int index, int end) {
    blocks[(index  << 1) + 1] = end;
  }
  
  //DEBUG
  int startBlock(int index) {
    return blocks[index << 1];
  }
  
  public ClassCoverage getCoverage() {
    return coverage;
  }
  
  public void seal() {
    coverage = new ClassCoverage(size >> 1);
  }
  
  public void dump(String className) {
    for(int i=0; i<size; i+=2) {
      if (coverage.isCovered(i >> 1)) {
        continue;
      }
      System.out.println(className + ": no coverage for line(s) "+blocks[i]+" to "+blocks[i + 1]);
    }
  }
}
