package net.coderodde.toy.assembler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * This class describes an instruction.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Mar 17, 2016)
 */
public class Instruction {
   
    private final String opcodeMnemonic;
    private final byte opcode;
    private final List<TokenDescriptor> argumentTokenDescriptorList;
    private final int instructionLength;
    
    public Instruction(String opcodeMnemonic,
                       byte opcode, 
                       TokenDescriptor... argumentTokenDescriptors) {
        this.opcodeMnemonic = 
                Objects.requireNonNull(opcodeMnemonic,
                                       "Opcode mnemonic is null.");
        
        if (opcodeMnemonic.isEmpty()) {
            throw new IllegalArgumentException(
                    "The opcode mnemonic is empty.");
        }
        
        checkArgumentTokenDescriptors(argumentTokenDescriptors);
        
        this.opcode = opcode;
        this.argumentTokenDescriptorList =
                Arrays.asList(argumentTokenDescriptors);
        this.instructionLength = computeInstructionLength();
    }
    
    public List<Byte> assemble(String[] tokens) {
        checkNumberOfTokens(tokens.length);
        List<Byte> code = new ArrayList<>(instructionLength);
        code.add(opcode);
        
        int tokenIndex = 1;
        
        for (TokenDescriptor tokenDescriptor : argumentTokenDescriptorList) {
            process(tokenDescriptor, tokens[tokenIndex++], code);
        }
        
        return code;
    }
    
    private void process(TokenDescriptor tokenDescriptor, 
                         String token,
                         List<Byte> code) {
        switch (tokenDescriptor) {
            case REGISTER:
                emitRegister(token, code);
                break;
//               
//            case ADDRESS:
//                emitAddress(token, code);
//                break;
//                
//            case WORD_DATA:
//                emitWordData(token, code);
//                break;
//                
//            case BYTE_DATA:
//                emitByteData(token, code);
//                break;
        }
    }
    
    private void emitRegister(String token, List<Byte> code) {
        Byte registerIndex = Configuration.mapRegisterNameToCode.get(token);
        
        if (registerIndex == null) {
            throw new AssemblyException(
                    "Unknown register name: \"" + token + ".\"");
        }
        
        code.add(registerIndex);
    }
    
    private void checkNumberOfTokens(int numberOfTokens) {
        int expectedNumberOfTokens = 1 + argumentTokenDescriptorList.size();
        
        if (numberOfTokens != expectedNumberOfTokens) {
            throw new AssemblyException(
            "Instruction '" + opcodeMnemonic + "' requires " + 
            expectedNumberOfTokens + ", " + numberOfTokens + " received.");
        }
    }
    
    private void checkArgumentTokenDescriptors(
            TokenDescriptor[] argumentTokenDescriptors) {
        for (TokenDescriptor tokenDescriptor : argumentTokenDescriptors) {
            switch (tokenDescriptor) {
                case REGISTER:
                case ADDRESS:
                case WORD_DATA:
                case BYTE_DATA:
                    break;
                    
                default:
                    throw new IllegalStateException("");
            }
        }
    }
    
    private int computeInstructionLength() {
        int bytes = 1; // Count the opcode; occupies always one byte.
        
        bytes = argumentTokenDescriptorList
                .stream()
                .map((tokenDescriptor) -> tokenDescriptor.getLengthInBytes())
                .reduce(bytes, Integer::sum);
        
        return bytes;
    }
}
