package net.coderodde.toy.assembler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements a command-line utility that compiles ToyVM source code
 * files to executable images.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Mar 8, 2016)
 */
public class App {
   
    private static final String SOURCE_CODE_FILE_EXTENSION  = ".toy";
    private static final String BINARY_IMAGE_FILE_EXTENSION = ".brick";
    
    public static void main(String[] args) {
        for (File file : getFileList(args)) {
            try {
                List<String> sourceCode = new SourceFileReader(file)
                                             .toLineList();
                byte[] machineCode = new ToyVMAssembler(file.getAbsolutePath(),
                                                        sourceCode).assemble();
                String outputFileName = computeOutputFileName(file.getName());
                FileUtilities.writeFile(new File(outputFileName), machineCode);
            } catch (FileNotFoundException ex) {
                System.err.println(
                        "ERROR: File \"" + file.getAbsolutePath() + "\" " +
                        "is not found.");
            } catch (AssemblyException | IOException ex) {
                System.err.println("ERROR: " + ex.getMessage());
            }
        }
    }
    
    private static String computeOutputFileName(String inputFileName) {
        if (!inputFileName.endsWith(SOURCE_CODE_FILE_EXTENSION)) {
            return inputFileName + BINARY_IMAGE_FILE_EXTENSION;
        }
        
        int index = inputFileName.lastIndexOf(SOURCE_CODE_FILE_EXTENSION);
        return inputFileName.substring(0, index) + BINARY_IMAGE_FILE_EXTENSION;
    }
    
    private static List<File> getFileList(String[] fileNameArray) {
        List<File> fileList = new ArrayList<>(fileNameArray.length);
        
        for (String fileName : fileNameArray) {
            fileList.add(new File(fileName));
        }
        
        return fileList;
    }
}