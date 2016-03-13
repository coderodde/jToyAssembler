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
    private final Map<String, InstructionAssembler> mapOpcodeToAssembler 
            = new HashMap<>();
    
    private final Map<String, Integer> mapWordNameToWordValue = new HashMap<>();
    private final Map<String, String> mapStringNameToStringValue 
            = new HashMap<>();
    
    private final String fileName;
    private int lineNumber = 1;
    
    @FunctionalInterface
    private interface InstructionAssembler {
        void assemble(String line);
    }
    
    public ToyVMAssembler(String fileName, List<String> sourceCodeLineList) {
        Objects.requireNonNull(sourceCodeLineList,
                               "The input source code line list is null.");
        Objects.requireNonNull(fileName, "The input file name is null.");
        this.sourceCodeLineList  = sourceCodeLineList;
        this.fileName = fileName;
        
        buildOpcodeMap();
    }
    
    private void buildOpcodeMap() {
        mapOpcodeToAssembler.put("add",   this::assembleAdd    );
        mapOpcodeToAssembler.put("neg",   this::assembleNeg    );
        mapOpcodeToAssembler.put("mul",   this::assembleMul    );
        mapOpcodeToAssembler.put("div",   this::assembleDiv    );
        mapOpcodeToAssembler.put("mod",   this::assembleMod    );
        mapOpcodeToAssembler.put("cmp",   this::assembleCmp    );
        mapOpcodeToAssembler.put("ja",    this::assembleJa     );
        mapOpcodeToAssembler.put("je",    this::assembleJe     );
        mapOpcodeToAssembler.put("jb",    this::assembleJb     );
        mapOpcodeToAssembler.put("jmp",   this::assembleJmp    );
        mapOpcodeToAssembler.put("call",  this::assembleCall   );
        mapOpcodeToAssembler.put("ret",   this::assembleRet    );
        mapOpcodeToAssembler.put("load",  this::assembleLoad   );
        mapOpcodeToAssembler.put("store", this::assembleStore  );
        mapOpcodeToAssembler.put("const", this::assembleConst  );
        mapOpcodeToAssembler.put("halt",  this::assembleHalt   );
        mapOpcodeToAssembler.put("int",   this::assembleInt    );
        mapOpcodeToAssembler.put("nop",   this::assembleNop    );
        mapOpcodeToAssembler.put("push",  this::assemblePush   );
        mapOpcodeToAssembler.put("pusha", this::assemblePushAll);
        mapOpcodeToAssembler.put("pop",   this::assemblePop    );
        mapOpcodeToAssembler.put("popa",  this::assemblePopAll );
        mapOpcodeToAssembler.put("lsp",   this::assembleLsp    );
        mapOpcodeToAssembler.put("word",  this::assembleWord   );
        mapOpcodeToAssembler.put("str",   this::assembleString );
    }
    
    public byte[] assemble() {
        for (String sourceCodeLine : sourceCodeLineList) {
            assembleSourceCodeLine(sourceCodeLine);
            lineNumber++;
        }
        
        resolveLabels();
        return convertMachineCodeToByteArray();
    }
    
    // Resolves all symbolical references (labels).
    private void resolveLabels() {
        for (Map.Entry<Integer, String> entry : mapAddressToLabel.entrySet()) {
            String label = entry.getValue();
            
            if (!mapLabelToAddress.containsKey(label)) {
                throw new RuntimeException(
                        "Label \"" + label + "\" is not defined.");
            }
            
            Integer address = mapLabelToAddress.get(label);
            setAddress(entry.getKey(), address);
        }
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
        InstructionAssembler instructionAssembler = 
                mapOpcodeToAssembler.get(toTokens(actualLine)[0]);
        
        if (instructionAssembler == null) {
            throw new RuntimeException(
                    errorHeader() +
                    "Unknown instruction in line \"" + line + "\".");
        }
        
        instructionAssembler.assemble(actualLine);
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
            throw new AssemblyException(
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
            throw new AssemblyException(
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
            throw new AssemblyException(
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
            throw new AssemblyException(
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
            throw new AssemblyException(
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
            throw new AssemblyException(
                    errorHeader() +
                    "The 'cmp' instruction requires exactly three tokens: " +
                    "\"cmp regi regj\"");
        }
        
        machineCode.add(CMP);
        emitRegister(tokens[1]);
        emitRegister(tokens[2]);
    }
    
    private void assembleJa(String line) {
        String[] tokens = toTokens(line);
        
        if (tokens.length != 2) {
            throw new AssemblyException(
                    errorHeader() + 
                    "The 'ja' instruction requires exactly two tokens: " +
                    "\"ja label\" or \"ja address\"");
        }
        
        machineCode.add(JA);
        
        if (isHexInteger(tokens[1])) {
            emitAddress(hexStringToInteger(tokens[1]));
        } else if (isInteger(tokens[1])) {
            emitAddress(toInteger(tokens[1]));
        } else {
            mapAddressToLabel.put(machineCode.size(), tokens[1]);
            emitAddress(0);
        }
    }
    
    private void assembleJe(String line) {
        String[] tokens = toTokens(line);
        
        if (tokens.length != 2) {
            throw new AssemblyException(
                    errorHeader() + 
                    "The 'je' instruction requires exactly two tokens: " +
                    "\"je label\" or \"je address\"");
        }
        
        machineCode.add(JE);
        
        if (isHexInteger(tokens[1])) {
            emitAddress(hexStringToInteger(tokens[1]));
        } else if (isInteger(tokens[1])) {
            emitAddress(toInteger(tokens[1]));
        } else {
            mapAddressToLabel.put(machineCode.size(), tokens[1]);
            emitAddress(0);
        }
    }
    
    private void assembleJb(String line) {
        String[] tokens = toTokens(line);
        
        if (tokens.length != 2) {
            throw new AssemblyException(
                    errorHeader() + 
                    "The 'jb' instruction requires exactly two tokens: " +
                    "\"jb label\" or \"jb address\"");
        }
        
        machineCode.add(JB);
        
        if (isHexInteger(tokens[1])) {
            emitAddress(hexStringToInteger(tokens[1]));
        } else if (isInteger(tokens[1])) {
            emitAddress(toInteger(tokens[1]));
        } else {
            mapAddressToLabel.put(machineCode.size(), tokens[1]);
            emitAddress(0);
        }
    }
    
    private void assembleJmp(String line) {
        String[] tokens = toTokens(line);
        
        if (tokens.length != 2) {
            throw new AssemblyException(
                    errorHeader() +
                    "The 'jmp' instructoin requires exactly two tokens: " +
                    "\"jmp label\" or \"jmp address\"");
        }
        
        machineCode.add(JMP);
        
        if (isHexInteger(tokens[1])) {
            emitAddress(hexStringToInteger(tokens[1]));
        } else if (isInteger(tokens[1])) {
            emitAddress(toInteger(tokens[1]));
        } else {
            mapAddressToLabel.put(machineCode.size(), tokens[1]);
            emitAddress(0);
        }
    }
    
    private void assembleCall(String line) {
        String[] tokens = toTokens(line);
        
        if (tokens.length != 2) {
            throw new AssemblyException(
                    errorHeader() + 
                    "The 'call' instruction requires exactly two tokens: " +
                    "\"call label\" or \"call address\"");
        }
        
        machineCode.add(CALL);
        
        if (isHexInteger(tokens[1])) {
            emitAddress(hexStringToInteger(tokens[1]));
        } else if (isInteger(tokens[1])) {
            emitAddress(toInteger(tokens[1]));
        } else {
            mapAddressToLabel.put(machineCode.size(), tokens[1]);
            emitAddress(0);
        }
    }
    
    private void assembleRet(String line) {
        String[] tokens = toTokens(line);
        
        if (tokens.length != 1) {
            throw new AssemblyException(
                    errorHeader() + 
                    "The 'ret' instruction must not have any arguments.");
        }
        
        machineCode.add(RET);
    }
    
    private void assembleLoad(String line) {
        String[] tokens = toTokens(line);
        
        if (tokens.length != 3) {
            throw new AssemblyException(
                    errorHeader() +
                    "The 'load' instruction requires exactly three tokens: " +
                    "\"load regi address\" or \"load regi label\"");
        }
        
        machineCode.add(LOAD);
        emitRegister(tokens[1]);
        
        if (isHexInteger(tokens[2])) {
            emitAddress(hexStringToInteger(tokens[2]));
        } else if (isInteger(tokens[2])) {
            emitAddress(toInteger(tokens[2]));
        } else {
            mapAddressToLabel.put(machineCode.size(), tokens[2]);
            emitAddress(0);
        }
    }
    
    private void assembleStore(String line) {
        String[] tokens = toTokens(line);
        
        if (tokens.length != 3) {
            throw new AssemblyException(
                    errorHeader() +
                    "The 'store' instruction requires exactly three tokens: " +
                    "\"store regi address\" or \"store regi label\"");
        }
        
        machineCode.add(STORE);
        emitRegister(tokens[1]);
        
        if (isHexInteger(tokens[2])) {
            emitAddress(hexStringToInteger(tokens[2]));
        } else if (isInteger(tokens[2])) {
            emitAddress(toInteger(tokens[2]));
        } else {
            mapAddressToLabel.put(machineCode.size(), tokens[2]);
            emitAddress(0);
        }
    }
    
    private void assembleConst(String line) {
        String[] tokens = toTokens(line);
        
        if (tokens.length != 3) {
            throw new AssemblyException(
                    errorHeader() +
                    "The 'const' instruction requires exactly three tokens: " +
                    "\"cosnt regi constant\"");
        }
        
        machineCode.add(CONST);
        emitRegister(tokens[1]);
        
        if (isHexInteger(tokens[2])) {
            emitAddress(hexStringToInteger(tokens[2]));
        } else if (isInteger(tokens[2])) {
            emitAddress(toInteger(tokens[2]));
        } else {
            mapAddressToLabel.put(machineCode.size(), tokens[2]);
            emitAddress(0);
        }
    }
    
    private void assembleHalt(String line) {
        String[] tokens = toTokens(line);
        
        if (tokens.length != 1) {
            throw new AssemblyException(
                    errorHeader() +
                    "The 'halt' instruction must not have any arguments.");
        }
        
        machineCode.add(HALT);
    }
    
    private void assembleInt(String line) {
        String[] tokens = toTokens(line);
        
        if (tokens.length != 2) {
            throw new AssemblyException(
                    errorHeader() +
                    "The 'int' instruction requires exactly two tokens: " +
                    "\"int interrupt_number\"");
        }
        
        machineCode.add(INT);
        
        if (isHexInteger(tokens[1])) {
            machineCode.add((byte) hexStringToInteger(tokens[1]));
        } else if (isInteger(tokens[1])) {
            machineCode.add((byte) toInteger(tokens[1]));
        } else {
            throw new AssemblyException(
                    "The interrupt number is not a valid decimal or " +
                    "hexadecimal integer: \"" + tokens[1] + "\".");
        }
    }
    
    private void assembleNop(String line) {
        String[] tokens = toTokens(line);
        
        if (tokens.length != 1) {
            throw new AssemblyException(
                    errorHeader() + 
                    "The 'nop' instruction must not have arguments.");
        }
        
        machineCode.add(NOP);
    }
    
    private void assemblePush(String line) {
        String[] tokens = toTokens(line);
        
        if (tokens.length != 2) {
            throw new AssemblyException(
                    errorHeader() +
                    "The 'push' instruction requires exactly two tokens: " + 
                    "\"push regi\"");
        }
        
        machineCode.add(PUSH);
        emitRegister(tokens[1]);
    }
    
    private void assemblePushAll(String line) {
        String[] tokens = toTokens(line);
        
        if (tokens.length != 1) {
            throw new AssemblyException(
                    errorHeader() +
                    "The 'pusha' instruction must not have arguments.");
        }
        
        machineCode.add(PUSH_ALL);
    }
    
    private void assemblePop(String line) {
        String[] tokens = toTokens(line);
        
        if (tokens.length != 2) {
            throw new AssemblyException(
                    errorHeader() +
                    "The 'pop' instruction requires exactly two tokens: " + 
                    "\"pop regi\"");
        }
        
        machineCode.add(POP);
        emitRegister(tokens[1]);
    }
    
    private void assemblePopAll(String line) {
        String[] tokens = toTokens(line);
        
        if (tokens.length != 1) {
            throw new AssemblyException(
                    errorHeader() +
                    "The 'popa' instruction must not have arguments.");
        }
        
        machineCode.add(POP_ALL);
    }
    
    private void assembleLsp(String line) {
        String[] tokens = toTokens(line);
        
        if (tokens.length != 1) {
            throw new AssemblyException(
                    errorHeader() +
                    "The 'lsp' instruction must not have arguments.");
        }
        
        machineCode.add(LSP);
    }
    
    private void assembleWord(String line) {
        String[] tokens = toTokens(line);
        
        if (tokens.length != 3) {
            throw new AssemblyException(
                    errorHeader() + 
                    "The 'word' instruction requireis exactly three tokens: " +
                    "\"word name value\"");
        }
        
        int datum;
        
        if (isHexInteger(tokens[2])) {
            datum = hexStringToInteger(tokens[2]);
        } else if (isInteger(tokens[2])) {
            datum = toInteger(tokens[2]);
        } else {
            throw new AssemblyException(
                    "Cannot parse \"" + tokens[2] + "\" as a decimal or " + 
                    "hexadecimal integer.");
        }
        
        mapWordNameToWordValue.put(tokens[1], datum);
    }
    
    private void assembleString(String line) {
        int firstQuoteIndex = line.indexOf("\"");
        
        if (firstQuoteIndex == -1) {
            throw new AssemblyException(
                    errorHeader() +
                    "The string must be enclosed in quotation marks: " +
                    "str name \"string content\"");
        }
        
        int lastQuoteIndex  = line.lastIndexOf("\"");
        
        String str = line.substring(firstQuoteIndex + 1, lastQuoteIndex);
        String[] tokens = toTokens(line);
        
        if (tokens.length < 3) {
            throw new AssemblyException(
                    errorHeader() + 
                    "The 'str' instruction requireis exactly three tokens: " +
                    "\"str name value\"");
        }
        
        mapStringNameToStringValue.put(tokens[1], str);
    }
    
    private boolean isInteger(String token) {
        try {
            Integer.parseInt(token);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
    
    private boolean isHexInteger(String token) {
        if (token.length() < 3 
                || (!token.startsWith("0x") || !token.startsWith("0x"))) {
            return false;
        }
        
        String body = token.substring(2);
        
        try {
            Integer.parseInt(body, 16);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
    
    private int hexStringToInteger(String token) {
        if (!isHexInteger(token)) {
            throw new IllegalArgumentException(
                    "The input token is not a hexadecimal number.");
        }
        
        return Integer.parseInt(token, 16); 
    }
    
    private int toInteger(String token) {
        return Integer.parseInt(token);
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
            throw new AssemblyException(
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
        return "ERROR in file \"" + fileName + 
               "\" at line " + lineNumber + ": ";
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
