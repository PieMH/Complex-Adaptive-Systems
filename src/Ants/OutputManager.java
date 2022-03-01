package Ants;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import java.io.*;
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
        logFileName = cwd + "\\src\\Ants\\log.log";
    }

    void writeCSV(List<String[]> data) {
        // default all fields are enclosed in double quotes
        // default separator is a comma
        try (CSVWriter writer = new CSVWriter(new FileWriter(outputFileName))) {
            writer.writeAll(data);
        }
        catch (java.io.IOException e) {
            e.printStackTrace();
        }

    }

    void writeLog() {
        try (CSVReader reader = new CSVReader(new FileReader(outputFileName));
             FileWriter logWriter = new FileWriter(logFileName, true);
             BufferedWriter b = new BufferedWriter(logWriter);
             PrintWriter p = new PrintWriter(b);
             ) {

            for (String[] line : reader) {
                p.println(Arrays.toString(line));
            }

            List<String[]> r = reader.readAll();
            r.forEach(x -> System.out.println(Arrays.toString(x)));

        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }
}

