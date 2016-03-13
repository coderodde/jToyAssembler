package net.coderodde.toy.assembler;

/**
 * This class implements an exception thrown whenever the assembly language 
 * syntax is violated.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Mar 13, 2016)
 */
public class AssemblyException extends RuntimeException {
    
    public AssemblyException(String message) {
        super(message);
    }
}
