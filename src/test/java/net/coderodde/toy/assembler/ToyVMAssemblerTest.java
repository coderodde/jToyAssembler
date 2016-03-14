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
    
    @Test
    public void testStore() {
        source.add("store reg1 30");
        source.add("word number 0x12341235");
        source.add("store reg2 number");
        source.add("store reg4 my_str");
        source.add("str my_str \"Hello\"");
        byte[] code = assembler.assemble();
        byte[] expected = new byte[]{ STORE, REG1, 30, 0, 0, 0,
                                      STORE, REG2, 18, 0, 0, 0,
                                      STORE, REG4, 22, 0, 0, 0,
                                      0x35, 0x12, 0x34, 0x12,
                                      0, 0, 0, 0, 0, 0 };
        writeString("Hello", expected, 22);
        assertTrue(Arrays.equals(expected, code));
    }
    
    @Test
    public void testConst() {
        source.add("const reg1 0x12345678");
        source.add("const reg4 0x9abcdef0");
        byte[] code = assembler.assemble();
        byte[] expected = new byte[]{ 
            CONST, REG1, 0x78, 0x56, 0x34, 0x12,
            CONST, REG4, (byte) 0xf0, (byte)0xde, (byte)0xbc, (byte) 0x9a };
        assertTrue(Arrays.equals(expected, code));
    }
    
    @Test
    public void testHalt() {
        source.add("halt");
        source.add("halt");
        byte[] code = assembler.assemble();
        byte[] expected = new byte[]{ HALT, HALT };
        assertTrue(Arrays.equals(expected, code));
    }
    
    @Test
    public void testInt() {
        source.add("int 1");
        source.add("int 2");
        byte[] code = assembler.assemble();
        byte[] expected = new byte[]{ INT, 1, INT, 2 };
        assertTrue(Arrays.equals(expected, code));
    }
    
    @Test
    public void testNop() {
        source.add("nop");
        source.add("nop");
        byte[] code = assembler.assemble();
        byte[] expected = new byte[]{ NOP, NOP };
        assertTrue(Arrays.equals(expected, code));
    }
    
    @Test
    public void testPush() {
        source.add("push reg2");
        source.add("push reg1");
        byte[] code = assembler.assemble();
        byte[] expected = new byte[]{ PUSH, REG2, PUSH, REG1 };
        assertTrue(Arrays.equals(expected, code));
    }
    
    @Test
    public void testPop() {
        source.add("pop reg2");
        source.add("pop reg4");
        byte[] code = assembler.assemble();
        byte[] expected = new byte[]{ POP, REG2, POP, REG4 };
        assertTrue(Arrays.equals(expected, code));
    }
    
    @Test
    public void testPushAll() {
        source.add("pusha");
        source.add("pusha");
        byte[] code = assembler.assemble();
        byte[] expected = new byte[]{ PUSH_ALL, PUSH_ALL };
        assertTrue(Arrays.equals(expected, code));
    }
    
    @Test
    public void testPopAll() {
        source.add("popa");
        source.add("popa");
        byte[] code = assembler.assemble();
        byte[] expected = new byte[]{ POP_ALL, POP_ALL };
        assertTrue(Arrays.equals(expected, code));
    }
    
    @Test
    public void testLsp() {
        source.add("lsp reg3");
        source.add("lsp reg2");
        byte[] code = assembler.assemble();
        byte[] expected = new byte[]{ LSP, REG3, LSP, REG2 };
        assertTrue(Arrays.equals(expected, code));
    }
    
    @Test
    public void testEmptyCodeOnNoSource() {
        assertEquals(0, assembler.assemble().length);
    }
    
    @Test
    public void testEmptyLinesAreOmitted() {
        source.add("");
        source.add("neg reg1");
        source.add("");
        source.add("pusha");
        source.add("popa");
        source.add("");
        byte[] code = assembler.assemble();
        byte[] expected = new byte[]{ NEG, REG1, PUSH_ALL, POP_ALL };
        assertTrue(Arrays.equals(expected, code));
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooMuchTokensInAddThrowsAssemblyException() {
        source.add("add reg1 fun reg3");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooMuchTokensInNegThrowsAssemblyException() {
        source.add("neg fun nuf");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooMuchTokensInMulThrowsAssemblyException() {
        source.add("mul one reg2 three");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooMuchTokensInDivThrowsAssemblyException() {
        source.add("div reg1 reg2 reg3");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooMuchTokensInModThrowsAssemblyException() {
        source.add("mod r r rrr");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooMuchTokensInCmpThrowsAssemblyException() {
        source.add("cmp reg1 reg2 reg3");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooMuchTokensInJaThrowsAssemblyException() {
        source.add("ja 3 reg2");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooMuchTokensInJeThrowsAssemblyException() {
        source.add("je 4 reg1");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooMuchTokensInJbThrowsAssemblyException() {
        source.add("jb 5 reg3");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooMuchTokensInJumpThrowsAssemblyException() {
        source.add("jump 3 reg4");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooMuchTokensInCallThrowsAssemblyException() {
        source.add("call 3 reg4");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooMuchTokensInRetThrowsAssemblyException() {
        source.add("ret 3");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooMuchTokensInLoadThrowsAssemblyException() {
        source.add("load 3 reg1 reg4");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooMuchTokensInStoreThrowsAssemblyException() {
        source.add("store reg3 reg1 fun");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooMuchTokensInConstThrowsAssemblyException() {
        source.add("const reg3 3 4");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooMuchTokensInHaltThrowsAssemblyException() {
        source.add("halt 4");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooMuchTokensInIntThrowsAssemblyException() {
        source.add("int 1 2");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooMuchTokensInNopThrowsAssemblyException() {
        source.add("nop reg3");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooMuchTokensInPopThrowsAssemblyException() {
        source.add("pop reg3 reg1");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooMuchTokensInPopAllThrowsAssemblyException() {
        source.add("popa all");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooMuchTokensInPushThrowsAssemblyException() {
        source.add("push reg1 reg3");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooMuchTokensInPushAllThrowsAssemblyException() {
        source.add("pusha all");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooMuchTokensInLspThrowsAssemblyException() {
        source.add("lsp reg3 1");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooLittleTokensInAddThrowsAssemblyException() {
        source.add("add reg1");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooLittleTokensInNegThrowsAssemblyException() {
        source.add("neg");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooLittleTokensInMulThrowsAssemblyException() {
        source.add("mul reg2");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooLittleTokensInDivThrowsAssemblyException() {
        source.add("div reg3");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooLittleTokensInModThrowsAssemblyException() {
        source.add("mod reg4");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooLittleTokensInCmpThrowsAssemblyException() {
        source.add("cmp reg1");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooLittleTokensInJaThrowsAssemblyException() {
        source.add("ja");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooLittleTokensInJbThrowsAssemblyException() {
        source.add("jb");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooLittleTokensInJeThrowsAssemblyException() {
        source.add("je");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooLittleTokensInCallThrowsAssemblyException() {
        source.add("call");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooLittleTokensInLoadThrowsAssemblyException() {
        source.add("load reg1");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooLittleTokensInStoreThrowsAssemblyException() {
        source.add("store reg2");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooLittleTokensInConstThrowsAssemblyException() {
        source.add("const reg3");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooLittleTokensInIntThrowsAssemblyException() {
        source.add("int");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooLittleTokensInPushThrowsAssemblyException() {
        source.add("push");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooLittleTokensInPopThrowsAssemblyException() {
        source.add("pop");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testTooLittleTokensInLspThrowsAssemblyException() {
        source.add("lsp");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class)
    public void testInvalidOpcodeThrowsAssemblyException() {
        source.add("bad");
        assembler.assemble();
    }
     
    @Test
    public void testGoodRegisterDoesNotThrowAssemblyException() {
        source.add("neg reg1");
        source.add("neg reg2");
        source.add("neg reg3");
        source.add("neg reg4");
        assembler.assemble();
    }
    
    @Test(expected = AssemblyException.class) 
    public void testBadRegisterThrowsAssemblyException() {
        source.add("neg reg5");
        assembler.assemble();
    }
    
    private void writeString(String string, byte[] code, int offset) {
        for (char c : string.toCharArray()) {
            code[offset++] = (byte) c;
        }
    }
}
