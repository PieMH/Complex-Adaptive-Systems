package Ants;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class OutputManager {

    private final String outputFileName;

    private final String logFileName;

    private FileWriter outputFileWriter;

    private FileWriter logFileWriter;

    private FileReader outputFileReader;

    OutputManager() {
        String cwd = System.getProperty("user.dir");  // Getting absolute path
        outputFileName = cwd + "\\src\\Ants\\output.csv";
        logFileName = cwd + "\\src\\Ants\\output.log";

        try {
            Files.deleteIfExists(Path.of(outputFileName));
            Files.deleteIfExists(Path.of(logFileName));
        }
        catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    void writeCSV(List<String[]> data) {
        // default all fields are enclosed in double quotes
        // default separator is a comma
        try (CSVWriter writer = new CSVWriter(new FileWriter(outputFileName, true))) {
            writer.writeAll(data);
        }
        catch (java.io.IOException e) {
            e.printStackTrace();
        }

    }
/*
    void writeLog() {
        try (CSVReader reader = new CSVReader(new FileReader(outputFileName));
             FileWriter logWriter = new FileWriter(logFileName, true);
             BufferedWriter b = new BufferedWriter(logWriter);
             PrintWriter p = new PrintWriter(b);
             ) {

            for (String[] line : reader) {
                if (Arrays.equals(line, new String[]{""})) p.println("\n");
                else p.println(Arrays.toString(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    void writeLog(List<String[]> data) {
        try ( FileWriter logWriter = new FileWriter(logFileName, true);
            BufferedWriter b = new BufferedWriter(logWriter);
            PrintWriter p = new PrintWriter(b);
             ) {

            for (String[] line : data) {
                if (Arrays.equals(line, new String[]{""})) p.print("\n");
                else {
                    String stringLine = Arrays.toString(line).replace(']', '\n');
                    p.print(stringLine.substring(1));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

