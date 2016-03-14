package net.coderodde.toy.assembler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import static net.coderodde.toy.assembler.ToyVMAssembler.*;

public class ToyVMAssemblerTest {

    private final List<String> source = new ArrayList<>();
    private ToyVMAssembler assembler;
    
    @Before
    public void before() {
        source.clear();
        assembler = new ToyVMAssembler("test", source);
    }
    
    @Test
    public void testAdd() {
        source.add("add reg1 reg2");
        source.add("add reg4 reg3");
        byte[] code = assembler.assemble();
        byte[] expected = new byte[]{ ADD, REG1, REG2, ADD, REG4, REG3 };
        assertTrue(Arrays.equals(expected, code));
    }
    
    @Test
    public void testNeg() {
        source.add("neg reg3");
        source.add("neg reg2");
        byte[] code = assembler.assemble();
        byte[] expected = new byte[]{ NEG, REG3, NEG, REG2 };
    }
    
    @Test
    public void testMul() {
        source.add("mul reg2 reg3");
        source.add("mul reg4 reg1");
        byte[] code = assembler.assemble();
        byte[] expected = new byte[]{ MUL, REG2, REG3, MUL, REG4, REG1 };
        assertTrue(Arrays.equals(expected, code));
    }
    @Test
    public void testDiv() {
        source.add("div reg1 reg2");
        source.add("div reg4 reg1");
        byte[] code = assembler.assemble();
        byte[] expected = new byte[]{ DIV, REG1, REG2, DIV, REG4, REG1 };
        assertTrue(Arrays.equals(expected, code));
    }
    
    @Test
    public void testMod() {
        source.add("mod reg2 reg2");
        source.add("mod reg1 reg4");
        byte[] code = assembler.assemble();
        byte[] expected = new byte[]{ MOD, REG2, REG2, MOD, REG1, REG4 };
        assertTrue(Arrays.equals(expected, code));
    }
    
    @Test
    public void testCmp() {
        source.add("cmp reg3 reg2");
        source.add("cmp reg1 reg2");
        byte[] code = assembler.assemble();
        byte[] expected = new byte[]{ CMP, REG3, REG2, CMP, REG1, REG2 };
        assertTrue(Arrays.equals(expected, code));
    }
    
    @Test
    public void testJa() {
        source.add("label1:");
        source.add("label2: nop");
        source.add("label3: ja label1");
        source.add("ja label2");
        source.add("ja 0xff");
        byte[] code = assembler.assemble();
        byte[] expected = new byte[]{ NOP, JA, 0, 0, 0, 0, JA, 0, 0, 0, 0,
                                      JA, (byte) 0xff, 0, 0, 0 };
        assertTrue(Arrays.equals(expected, code));
    }
    
    @Test
    public void testJe() {
        source.add("label1:");
        source.add("label2: nop");
        source.add("label3: je label1");
        source.add("je label2");
        source.add("je 0Xff");
        byte[] code = assembler.assemble();
        byte[] expected = new byte[]{ NOP, JE, 0, 0, 0, 0, JE, 0, 0, 0, 0,
                                      JE, (byte) 0xff, 0, 0, 0 };
        assertTrue(Arrays.equals(expected, code));
    }
    
    @Test
    public void testJb() {
        source.add("label1:");
        source.add("label2: nop");
        source.add("label3: jb label1");
        source.add("jb label2");
        source.add("jb 0xFf");
        byte[] code = assembler.assemble();
        byte[] expected = new byte[]{ NOP, JB, 0, 0, 0, 0, JB, 0, 0, 0, 0,
                                      JB, (byte) 0xff, 0, 0, 0 };
        assertTrue(Arrays.equals(expected, code));
    }
    
    @Test
    public void testJmp() {
        source.add("nop");
        source.add("int 2");
        source.add("label1:");
        source.add("label2: push reg3");
        source.add("jmp label1");
        source.add("jmp 0x12345678");
        source.add("jmp 0X12Fafb");
        byte[] code = assembler.assemble();
        byte[] expected = 
                new byte[]{ NOP, INT, 2, PUSH, REG3, JMP, 3, 0, 0, 0,
                            JMP, 0x78, 0x56, 0x34, 0x12, 
                            JMP, (byte) 0xfb, (byte) 0xFa, 0x12, 0 };
        assertTrue(Arrays.equals(expected, code));
    }
    
    @Test
    public void testCall() {
        source.add("nop");
        source.add("popa");
        source.add("call func");
        source.add("nop");
        source.add("func:");
        source.add("add reg1 reg4");
        byte[] code = assembler.assemble();
        byte[] expected = 
                new byte[]{ NOP, POP_ALL, CALL, 8, 0, 0, 0, 
                            NOP, ADD, REG1, REG4 };
        assertTrue(Arrays.equals(expected, code));
    }
    
    @Test
    public void testRet() {
        source.add("ret");
        source.add("neg reg2");
        source.add("ret");
        byte[] code = assembler.assemble();
        byte[] expected = new byte[]{ RET, NEG, REG2, RET };
        assertTrue(Arrays.equals(expected, code));
    }
    
    @Test
    public void testLoad() {
        source.add("load reg3 0X1234"); 
        source.add("word number 0x12341235");
        source.add("load reg2 number"); 
        source.add("load reg1 my_str"); 
        source.add("str my_str \"Funky Funk, hey yo!\"");
        byte[] code = assembler.assemble();
        byte[] expected = new byte[]{ LOAD, REG3, 0x34, 0x12, 0, 0, 
                                      LOAD, REG2, 18, 0, 0, 0,
                                      LOAD, REG1, 22, 0, 0, 0,
                                      0x35, 0x12, 0x34, 0x12,
                                      0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0 };
        writeString("Funky Funk, hey yo!", expected, 22);
        
        assertTrue(Arrays.equals(expected, code));
    }
    
    private void writeString(String string, byte[] code, int offset) {
        for (char c : string.toCharArray()) {
            code[offset++] = (byte) c;
        }
    }
}
