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
}
