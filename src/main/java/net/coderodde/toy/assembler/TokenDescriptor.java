package net.coderodde.toy.assembler;

/**
 * This enumeration class enumerates all possible token types.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Mar 17, 2016)
 */
public enum TokenDescriptor {
    REGISTER  (1),
    ADDRESS   (4),
    WORD_DATA (4),
    BYTE_DATA (1);
    
    private final int bytes;
    
    public int getLengthInBytes() {
        return bytes;
    }
    
    private TokenDescriptor(int bytes) {
        if (bytes < 1) {
            throw new IllegalArgumentException(
                    "A token cannot occupy less than one (1) byte.");
        }
        
        this.bytes = bytes;
    }
}
