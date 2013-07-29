package fr.umlv.indycov;

import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;

public class Agent {

  public static void premain(String agentArgs, Instrumentation instrumentation) {
    instrumentation.addTransformer(new ClassFileTransformer() {
      @Override
      public byte[] transform(ClassLoader loader, String className,
          Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
          byte[] classfileBuffer) throws IllegalClassFormatException {

        // don't try to analyze coverage of classes in bootstrap classpath
        if (loader == null)
          return null;

        try {
          ClassReader reader = new ClassReader(classfileBuffer);
          ClassWriter writer = new ClassWriter(reader, 0);
          reader.accept(new CoverageClassVisitor(writer), 0);
          byte[] byteArray = writer.toByteArray();
          
          //new ClassReader(byteArray).accept(new TraceClassVisitor(new PrintWriter(System.out)), 0);
          //CheckClassAdapter.verify(new ClassReader(byteArray), loader, true, new PrintWriter(System.out));

          return byteArray;

        } catch(Throwable t) {
          System.err.println("Coverage Agent Error:");
          t.printStackTrace();
          throw (IllegalClassFormatException)new IllegalClassFormatException().initCause(t);
        }
      }
    }, true);  
    
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      @Override
      public void run() {
        RT.dump();
      }
    }));
  }
}
