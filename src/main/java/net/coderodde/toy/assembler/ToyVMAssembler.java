package net.coderodde.toy.assembler;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class is responsible for assembling a ToyVM source file.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Mar 10, 2016).
 */
public class ToyVMAssembler {
   
    private static final byte REG1 = 0x00;
    private static final byte REG2 = 0x01;
    private static final byte REG3 = 0x02;
    private static final byte REG4 = 0x03;
    
    private static final byte ADD = 0x01;
    private static final byte NEG = 0x02;
    private static final byte MUL = 0x03;
    private static final byte DIV = 0x04;
    private static final byte MOD = 0x05;
    
    private static final byte CMP = 0x10;
    private static final byte JA  = 0x11;
    private static final byte JE  = 0x12;
    private static final byte JB  = 0x13;
    private static final byte JMP = 0x14;
    
    private static final byte CALL = 0x20;
    private static final byte RET  = 0x21;
    
    private static final byte LOAD  = 0x30;
    private static final byte STORE = 0x31;
    private static final byte CONST = 0x32;
    
    private static final byte HALT = 0x40;
    private static final byte INT  = 0x41;
    private static final byte NOP  = 0x42;
    
    private static final byte PUSH     = 0x50;
    private static final byte PUSH_ALL = 0x51;
    private static final byte POP      = 0x52;
    private static final byte POP_ALL  = 0x53;
    private static final byte LSP      = 0x54;
    
    private static final String COMMENT_START_TOKEN = "//";
    
    private final List<String> sourceCodeLineList;
    private final List<Byte> machineCode = new ArrayList<>();
    private final Map<Integer, String> mapAddressToLabel = new HashMap<>();
    private final Map<String, Integer> mapLabelToAddress = new HashMap<>();
    private final String fileName;
    
    public ToyVMAssembler(String fileName, List<String> sourceCodeLineList) {
        Objects.requireNonNull(sourceCodeLineList,
                               "The input source code line list is null.");
        Objects.requireNonNull(fileName, "The input file name is null.");
        this.sourceCodeLineList  = sourceCodeLineList;
        this.fileName = fileName;
    }
    
    public byte[] assemble() {
        for (String sourceCodeLine : sourceCodeLineList) {
            assembleSourceCodeLine(sourceCodeLine);
        }
        
        return convertMachineCodeToByteArray();
    }
    
    private void assembleSourceCodeLine(String line) {
        // Prune the possible comment away.
        line = line.split(COMMENT_START_TOKEN)[0].trim();
        // Deal with the possible label.
        String[] parts = handleLabel(line);
        String actualLine;
        
        if (parts.length == 1) {
            actualLine = parts[0];
        } else {
            mapLabelToAddress.put(parts[0], machineCode.size());
            actualLine = parts[1];
        }
        
        if (actualLine.isEmpty()) {
            // The line contains only label.
            return;
        }
        
        // Switch to assembing the actual.
        if (actualLine.startsWith("add")) {
            assembleAdd(actualLine);
        } else if (actualLine.startsWith("neg")) {
            assembleNeg(actualLine);
        } else if (actualLine.startsWith("mul")) {
            assembleMul(actualLine);
        } else if (actualLine.startsWith("div")) {
            assembleDiv(actualLine);
        } else if (actualLine.startsWith("mod")) {
            assembleMod(actualLine);
        } else if (actualLine.startsWith("cmp")) {
            assembleCmp(actualLine);
        }
    }
    
    private void emitRegister(String registerToken) {
        switch (registerToken) {
            case "reg1":
                machineCode.add(REG1);
                return;
                
            case "reg2":
                machineCode.add(REG2);
                return;
                
            case "reg3":
                machineCode.add(REG3);
                return;
                
            case "reg4":
                machineCode.add(REG4);
                return;
                
            default:
                throw new RuntimeException(
                "Unknown register token: \"" + registerToken + "\".");
        }
    }
    
    private void emitAddress(int address) {
        machineCode.add((byte) (address & 0xff));
        machineCode.add((byte)((address >>>= 8) & 0xff));
        machineCode.add((byte)((address >>>= 8) & 0xff));
        machineCode.add((byte)((address >>>= 8) & 0xff));
    }
    
    private void setAddress(int index, int address) {
        machineCode.set(index, (byte)(address & 0xff));
        machineCode.set(index + 1, (byte)((address >>>= 8) & 0xff));
        machineCode.set(index + 2, (byte)((address >>>= 8) & 0xff));
        machineCode.set(index + 3, (byte)((address >>>= 8) & 0xff));
    }
    
    private void assembleAdd(String line) {
        String[] tokens = toTokens(line);
        
        if (tokens.length != 3) {
            throw new RuntimeException(
                    errorHeader() +
                    "The 'add' instruction requires exactly three tokens: " +
                    "\"add regi regj\"");
        }
        
        machineCode.add(ADD);
        emitRegister(tokens[1]);
        emitRegister(tokens[2]);
    }
    
    private void assembleNeg(String line) {
        String[] tokens = toTokens(line);
        
        if (tokens.length != 2) {
            throw new RuntimeException(
                    errorHeader() +
                    "The 'neg' instruction requires exactly two tokens: " +
                    "\"neg regi\"");
        }
        
        machineCode.add(NEG);
        emitRegister(tokens[1]);
    }
    
    private void assembleMul(String line) {
        String[] tokens = toTokens(line);
        
        if (tokens.length != 3) {
            throw new RuntimeException(
                    errorHeader() +
                    "The 'mul' instruction requires exactly three tokens: " +
                    "\"mul regi regj\"");
        }
        
        machineCode.add(MUL);
        emitRegister(tokens[1]);
        emitRegister(tokens[2]);
    }
    
    private void assembleDiv(String line) {
        String[] tokens = toTokens(line);
        
        if (tokens.length != 3) {
            throw new RuntimeException(
                    errorHeader() +
                    "The 'div' instruction requires exactly three tokens: " +
                    "\"div regi regj\"");
        }
        
        machineCode.add(DIV);
        emitRegister(tokens[1]);
        emitRegister(tokens[2]);
    }
    
    private void assembleMod(String line) {
        String[] tokens = toTokens(line);
        
        if (tokens.length != 3) {
            throw new RuntimeException(
                    errorHeader() +
                    "The 'mod' instruction requires exactly three tokens: " +
                    "\"mod regi regj\"");
        }
        
        machineCode.add(MOD);
        emitRegister(tokens[1]);
        emitRegister(tokens[2]);
    }
    
    private void assembleCmp(String line) {
        String[] tokens = toTokens(line);
        
        if (tokens.length != 3) {
            throw new RuntimeException(
                    errorHeader() +
                    "The 'cmp' instruction requires exactly three tokens: " +
                    "\"cmp regi regj\"");
        }
        
        machineCode.add(CMP);
        emitRegister(tokens[1]);
        emitRegister(tokens[2]);
    }
    
    private void checkComment(String comment) {
        if (comment.length() < 2 || !comment.startsWith("//")) {
            throw new RuntimeException(
                    "Bad comment token: \"" + comment + "\"");
        }
    }
    
    private String[] toTokens(String line) {
        return line.split("\\s+");
    }
    
    private String[] handleLabel(String line) {
        int colonIndex = line.indexOf(":");
        
        if (colonIndex == -1) {
            return new String[]{ line };
        }
        
        if (line.indexOf(":", colonIndex + 1) != -1) {
            throw new RuntimeException(
                    "Only one label allowed per line. The input line is \"" +
                    line + "\".");
        }
        
        return new String[] { 
            line.substring(0, colonIndex).trim(),
            line.substring(colonIndex + 1, line.length()).trim() 
        };
    }
    
    private byte[] convertMachineCodeToByteArray() {
        byte[] code = new byte[machineCode.size()];
        
        for (int i = 0; i < code.length; ++i) {
            code[i] = machineCode.get(i);
        }
        
        return code;
    }
    
    private String errorHeader() {
        return "ERROR in file \"" + fileName + "\": ";
    }
    
    public static void main(String[] args) throws FileNotFoundException {
        String line1 = "add reg1 reg2 // yeah";
        String line2 = "label: add reg3 reg4// commnet // yeah";
        
        List<String> list = new ArrayList<>();
        list.add(line1);
        list.add(line2);
        
        ToyVMAssembler assembler = new ToyVMAssembler("file", list);
        byte[] data = assembler.assemble();
        System.out.println("Done.");
    }
}
