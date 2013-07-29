package fr.umlv.indycov;

import java.dyn.CallSite;
import java.dyn.MethodHandles.Lookup;
import java.dyn.MethodType;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodHandle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class CoverageClassVisitor extends ClassAdapter {
  static final MethodHandle BSM = new MethodHandle(Opcodes.MH_INVOKESTATIC,
      RT.class.getName().replace('.', '/'),
      "bsm",
      MethodType.methodType(CallSite.class, Lookup.class, String.class, MethodType.class, Object.class).toMethodDescriptorString());
  
  final ClassData classData = new ClassData();
  private String className;
  
  public CoverageClassVisitor(ClassVisitor cv) {
    super(cv);
  }
  
  @Override
  public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
    super.visit(version, access, name, signature, superName, interfaces);
    
    className = name.replace('/', '.');
  }
  
  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
    return new MethodAdapter(mv) {
      private int line = -1;
      private int blockIndex = -1;
      
      @Override
      public void visitLineNumber(int line, Label start) {
        super.visitLineNumber(line, start);
        
        if (this.line == -1) {
          blockIndex = classData.addBlock(line);
          insertProbe(blockIndex);
        }
        this.line = line;
      }
      
      @Override
      public void visitJumpInsn(int opcode, Label label) {
        // end a block
        classData.endBlock(blockIndex, line);
        blockIndex = -1;
        
        super.visitJumpInsn(opcode, label);
        
        if (opcode == Opcodes.GOTO)
          return;
        
        // start a new block
        blockIndex = classData.addBlock(line);
        insertProbe(blockIndex);
      }
      
      @Override
      public void visitInsn(int opcode) {
        super.visitInsn(opcode);
        if (opcode == Opcodes.ATHROW || (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)) {
          // end a block
          classData.endBlock(blockIndex, line);
          blockIndex = -1;
        }
      }
      
      @Override
      public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
        if (blockIndex != -1) {
          // end a block
          classData.endBlock(blockIndex, line);
        }
        
        super.visitFrame(type, nLocal, local, nStack, stack);
        
        // start a new block
        blockIndex = classData.addBlock(line);
        insertProbe(blockIndex);
      }
      
      private void insertProbe(int index) {
        super.visitInvokeDynamicInsn("probe", "()V", BSM, new Object[]{index});
      }
    };
  }
  
  @Override
  public void visitEnd() {
    super.visitEnd();
    RT.addClassData(className, classData);
  }
}
