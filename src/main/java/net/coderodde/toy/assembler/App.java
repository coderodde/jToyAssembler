//package net.coderodde.toy.assembler;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Scanner;
//
///**
// *
// * @author Rodion "rodde" Efremov
// * @version 1.6 (Mar 8, 2016)
// */
//public class App {
//   
//    
//    private App() {}
//    
//    public void assemble(List<File> fileList) {
//        for (File file : fileList) {
//            try {
//                assemble(file);
//            } catch (Exception ex) {
//                System.err.println("[ERROR] " + ex.getMessage());
//            }
//        }
//    }
//    
//    private void assemble(File file) throws FileNotFoundException {
//        if (file == null || !file.isFile()) {
//            return;
//        }
//        
//        Scanner scanner = new Scanner(file);
//        List<Byte> machineCode = new ArrayList<>();
//        Map<String, Integer> labelMap = new HashMap<>();
//        Map<Integer, String> addressToLabelMap = new HashMap<>();
//        
//        while (scanner.hasNextLine()) {
//            String line = scanner.nextLine();
//            assembleLine(line, machineCode, labelMap);
//        }
//        
//        fixLabelLinkage(addressToLabelMap, labelMap);
//    }
//    
//    private void fixLabelLinkage(Map<Integer, String> addressToLabelMap,
//                                 Map<String, Integer> labelMap) {
//        for (Map.Entry<Integer, String> entry : addressToLabelMap.entrySet()) {
//            
//        }
//    }
//    
//    private void assembleLine(String line,
//                              List<Byte> machineCode,
//                              Map<String, Integer> labelMap) {
//        line = line.trim();
//        int colonIndex = line.indexOf(':');
//        
//        if (colonIndex != -1) {
//            if (line.indexOf(':', colonIndex + 1) != -1) {
//                throw new RuntimeException(
//                        "Only at most one colon allowed per line.");
//            }
//            
//            labelMap.put(line.substring(0, colonIndex), machineCode.size());
//            line = line.substring(colonIndex + 1);
//        }
//        
//        if (line.startsWith("add ")) {
//            assembleAdd(line, machineCode);
//        } else if (line.startsWith("neg ")) {
//            assembleNeg(line, machineCode);
//        }
//    }
//    
//    private void emitRegister(String registerToken, List<Byte> machineCode) {
//        switch (registerToken) {
//            case "reg1":
//                machineCode.add(REG1);
//                break;
//                
//            case "reg2":
//                machineCode.add(REG2);
//                break;
//                
//            case "reg3":
//                machineCode.add(REG3);
//                break;
//                
//            case "reg4":
//                machineCode.add(REG4);
//                break;
//                
//            default:
//                throw new RuntimeException(
//                "Unknown register: \"" + registerToken + "\".");
//        }
//    }
//    
//    private void setAddress(List<Byte> machineCode, int index, int address) {
//        machineCode.set(index, (byte)(address & 0xff));
//        machineCode.set(index + 1, (byte)((address >>>= 8) & 0xff));
//        machineCode.set(index + 2, (byte)((address >>>= 8) & 0xff));
//        machineCode.set(index + 3, (byte)((address >>>= 8) & 0xff));
//    }
//    
//    private void emitAddress(int address, List<Byte> machineCode) {
//        machineCode.add((byte) (address         & 0xff));
//        machineCode.add((byte)((address >>> 8 ) & 0xff));
//        machineCode.add((byte)((address >>> 16) & 0xff));
//        machineCode.add((byte)((address >>> 24) & 0xff));
//    }
//    
//    private void assembleAdd(String line, List<Byte> machineCode) {
//        String[] tokens = toTokens(line);
//        
//        if (tokens.length < 3) {
//            throw new RuntimeException(
//                    "The ADD instruction must consist of exactly 3 tokens: " +
//                    "\"add regi regj\"");
//        }
//        
//        // Emit the opcode for ADD.
//        machineCode.add(ADD);
//        emitRegister(tokens[1], machineCode);
//        emitRegister(tokens[2], machineCode);
//    }
//    
//    private void assembleNeg(String line, List<Byte> machineCode) {
//        String[] tokens = toTokens(line);
//        
//        if (tokens.length < 2) {
//            throw new RuntimeException(
//                    "The NEG instruction must consist of exactly 2 tokens: " +
//                    "\"neg regi\"");
//        }
//        
//        machineCode.add(NEG);
//        emitRegister(tokens[1], machineCode);
//    }
//    
//    private void assembleMul(String line, List<Byte> machineCode) {
//        String[] tokens = toTokens(line);
//        
//        if (tokens.length < 3) {
//            throw new RuntimeException(
//                    "The MUL instruction must consist of exactly 3 tokens: " +
//                    "\"mul regi regj\"");
//        }
//        
//        // Emit the opcode for ADD.
//        machineCode.add(MUL);
//        emitRegister(tokens[1], machineCode);
//        emitRegister(tokens[2], machineCode);
//    }
//    
//    private void assembleDiv(String line, List<Byte> machineCode) {
//        String[] tokens = toTokens(line);
//        
//        if (tokens.length < 3) {
//            throw new RuntimeException(
//                    "The DIV instruction must consist of exactly 3 tokens: " +
//                    "\"div regi regj\"");
//        }
//        
//        // Emit the opcode for ADD.
//        machineCode.add(DIV);
//        emitRegister(tokens[1], machineCode);
//        emitRegister(tokens[2], machineCode);
//    }
//    
//    private void assembleMod(String line, List<Byte> machineCode) {
//        String[] tokens = toTokens(line);
//        
//        if (tokens.length < 3) {
//            throw new RuntimeException(
//                    "The MOD instruction must consist of exactly 3 tokens: " +
//                    "\"mod regi regj\"");
//        }
//        
//        // Emit the opcode for ADD.
//        machineCode.add(MOD);
//        emitRegister(tokens[1], machineCode);
//        emitRegister(tokens[2], machineCode);
//    }
//    
//    private void assembleCmp(String line, List<Byte> machineCode) {
//        String[] tokens = toTokens(line);
//        
//        if (tokens.length < 3) {
//            throw new RuntimeException(
//                    "The CMP instruction must consist of exactly 3 tokens: " +
//                    "\"cmp regi regj\"");
//        }
//        
//        // Emit the opcode for ADD.
//        machineCode.add(CMP);
//        emitRegister(tokens[1], machineCode);
//        emitRegister(tokens[2], machineCode);
//    }
//    
//    private int parseInteger(String s) {
//        if (s.length() >= 3 && (s.startsWith("0x") || s.startsWith("0X"))) {
//            try {
//                return Integer.parseInt(s.substring(2), 16);
//            } catch (NumberFormatException ex) {
//                
//            }
//        }
//        
//        try {
//            return Integer.parseInt(s);
//        } catch (NumberFormatException ex) {
//            return Integer.MIN_VALUE;
//        }
//    }
//    
//    // Assemble JA (jump if above) instruction.
//    private void assembleJa(String line, 
//                            List<Byte> machineCode) {
//        String[] tokens = toTokens(line);
//        
//        if (tokens.length < 2) {
//            throw new RuntimeException(
//                    "The JA instruction must consist of exactly 2 tokens: " +
//                    "\"ja label\"");
//        }
//        
//        machineCode.add(JA);
//        int address = parseInteger(tokens[1]);
//        
//        if (address != Integer.MIN_VALUE) {
//            // The address argument is hard-codes as a decimal or hexadecimal
//            // integer.
//            emitAddress(address, machineCode);
//        } else {
//            // The address argument is a label.
//        }
//    }
//    
//    private String[] toTokens(String line) {
//        return line.split("\\s+");
//    }
//    
//    public static void main(String[] args) {
//        App app = new App();
//        List<File> fileList = new ArrayList<>(args.length);
//        
//        for (String fileName : args) {
//            fileList.add(new File(fileName));
//        }
//        
//        app.assemble(fileList);
//    }
//}
