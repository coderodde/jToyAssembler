package net.coderodde.toy.assembler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class defines the constants involved in assembling the machine code.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Mar 17, 2016)
 */
public class Configuration {
    
    private static final Map<String, 
                             Byte> mapRegisterNameToCodeImpl = new HashMap<>();
    
    public static final Map<String, 
                            Byte> mapRegisterNameToCode = 
            Collections.<String, 
                         Byte>unmodifiableMap(mapRegisterNameToCodeImpl);
    
    static {
        mapRegisterNameToCode.put("reg1", (byte) 0x00);
        mapRegisterNameToCode.put("reg2", (byte) 0x01);
        mapRegisterNameToCode.put("reg3", (byte) 0x02);
        mapRegisterNameToCode.put("reg4", (byte) 0x03);
        
    }
}
