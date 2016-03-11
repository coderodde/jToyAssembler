package net.coderodde.toy.assembler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

/**
 * This class is responsible for loading the source code files into the list of
 * lines.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Mar 11, 2016)
 */
public class SourceFileReader {
   
    private final File file;
    
    public SourceFileReader(File file) {
        this.file = Objects.requireNonNull(file, "The input file is null.");
    }
    
    public List<String> toLineList() throws FileNotFoundException {
        List<String> lineList = new ArrayList<>();
        Scanner scanner = new Scanner(file);
        
        while (scanner.hasNextLine()) {
            lineList.add(scanner.nextLine());
        }
        
        return lineList;
    }
}
