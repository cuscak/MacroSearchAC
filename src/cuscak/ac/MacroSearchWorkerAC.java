package cuscak.ac;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

import static java.nio.file.FileVisitResult.CONTINUE;

public class MacroSearchWorkerAC extends SimpleFileVisitor<Path> {
    private PathMatcher matcher;
    private int macro;
    private String value;
    private ArrayList<String[]> outputToWrite = new ArrayList();

    public MacroSearchWorkerAC (String ext, int macroToCompare, String valueToSearch) {
        macro = macroToCompare;
        value = valueToSearch;

        matcher = FileSystems.getDefault().getPathMatcher("glob:*." + ext);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException {

        Path name = file.getFileName();
        if (name.toString().toUpperCase() != null && matcher.matches(name)) {
            System.out.println("Processing file: " + file.toString());
            processLine(file);
        }
        return CONTINUE;
    }

    // If there is some error accessing the file, let the user know.
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        System.err.println(exc);
        return CONTINUE;
    }

    private void processLine(Path file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(file)));){
            String line;
            while ((line = reader.readLine()) != null){
                String[] temp = line.split("\",\"");
                if(temp.length > 1){
                    if(temp[macro].trim().contains(value)){
                        outputToWrite.add(temp);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Something wrong with writing records from file: " + file.toString());
            e.printStackTrace();
        }
    }

    public ArrayList<String[]> getOutputToWrite() {
        return outputToWrite;
    }

}