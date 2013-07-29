package fr.umlv.indycov;

import java.dyn.CallSite;
import java.dyn.ClassValue;
import java.dyn.ConstantCallSite;
import java.dyn.MethodHandles;
import java.dyn.MethodHandles.Lookup;
import java.dyn.MethodType;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class RT {
  static final ConcurrentHashMap<String, ClassData> classDataMap =
    new ConcurrentHashMap<String, ClassData>();
  
  private static final ClassValue<ClassCoverage> classValue = 
    new ClassValue<ClassCoverage>() {
      @Override
      protected ClassCoverage computeValue(Class<?> type) {
        return classDataMap.get(type.getName()).getCoverage();
      }
    };
    
  public static void addClassData(String className, ClassData classData) {
    assert !className.contains("/");
    
    classData.seal();
    classDataMap.put(className, classData);
  }
    
  public static void dump() {
    for(Entry<String, ClassData> entry: classDataMap.entrySet()) {
      entry.getValue().dump(entry.getKey());
    }
  }
  
  public static CallSite bsm(Lookup lookup, String name, MethodType type, Object index) {
    classValue.get(lookup.lookupClass()).cover((Integer)index);
    return new ConstantCallSite(MethodHandles.identity(void.class));
  }
}
