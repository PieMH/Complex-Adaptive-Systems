package Ants;

import com.opencsv.CSVWriter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * This class is responsible for creating two files, a .csv and a .log
 * Where the data and the stats of the simulation will be printed
 * @see AntSimulator
 */
public class OutputManager {

    private final String outputFileName;

    private final String logFileName;

    OutputManager() {
        String cwd = System.getProperty("user.dir");  // Getting absolute path of cwd
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

    /**
     * prints the data to the csv file,
     * @param data the layout of the data to be printed as a list of arrays' string
     */
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

    /**
     * prints the data to the log file,
     * @param data the layout of the data to be printed as a list of arrays' string
     */
    void writeLog(List<String[]> data) {
        try ( FileWriter logWriter = new FileWriter(logFileName, true);
            BufferedWriter b = new BufferedWriter(logWriter);
            PrintWriter p = new PrintWriter(b)
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

