package net.coderodde.toy.assembler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Mar 8, 2016)
 */
public class App {
    
    private App() {}
    
    public void assemble(List<File> fileList) {
        for (File file : fileList) {
            try {
                assemble(file);
            } catch (Exception ex) {
                System.err.println("[ERROR] " + ex.getMessage());
            }
        }
    }
    
    private void assemble(File file) throws FileNotFoundException {
        if (file == null || !file.isFile()) {
            return;
        }
        
        Scanner scanner = new Scanner(file);
        List<Byte> machineCode = new ArrayList<>();
        Map<String, Integer> labelMap = new HashMap<>();
        
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            assembleLine(line, machineCode, labelMap);
        }
    }
    
    private void assembleLine(String line,
                              List<Byte> machineCode,
                              Map<String, Integer> labelMap) {
        line = line.trim();
        int colonIndex = line.indexOf(':');
        
        if (colonIndex != -1) {
            if (line.indexOf(':', colonIndex + 1) != -1) {
                throw new RuntimeException(
                        "Only at most one colon allowed per line.");
            }
            
            line = line.substring(colonIndex + 1);
        }
        
        if (line.startsWith("add ")) {
            
        }
    }
    
    public static void main(String[] args) {
        App app = new App();
        List<File> fileList = new ArrayList<>(args.length);
        
        for (String fileName : args) {
            fileList.add(new File(fileName));
        }
        
        app.assemble(fileList);
    }
}
