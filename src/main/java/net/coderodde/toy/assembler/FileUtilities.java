package net.coderodde.toy.assembler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * This class contains utility methods for dealing with files.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Mar 13, 2016)
 */
public final class FileUtilities {
    
    /**
     * Discards the prior content of the file {@code file} and writes the data
     * from {@code data} to it.
     * 
     * @param file the file to write to.
     * @param data the data to write.
     * 
     * @throws IOException thrown if I/O fails.
     * 
     * @throws FileNotFoundException thrown if {@code file} does not represent
     *                               an existing file.
     */
    public static void writeFile(File file, byte[] data)
    throws IOException, FileNotFoundException {
        Objects.requireNonNull(file, "The input file is null.");
        Objects.requireNonNull(data, "The input data to write is null.");

        try (BufferedOutputStream stream = new BufferedOutputStream(
                                           new FileOutputStream(file))) {
            stream.write(data);
        }
    }
}
