package Ants;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OutputManager {

    AntSimulator antSim;

    OutputManager(AntSimulator antSim) {

        this.antSim = antSim;

    }

    OutputManager() {

    }

    public static void main(String[] args) {
        OutputManager man = new OutputManager();
        man.dataToCSV();
    }

    void dataToCSV() {

        String fileName = "src\\Ants\\country.csv";
        try (CSVReader reader = new CSVReader(new FileReader(fileName))) {
            List<String[]> r = reader.readAll();
            r.forEach(x -> System.out.println(Arrays.toString(x)));
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    private void setOutputValue() {


//        outValues.setName("mkyong");
//        outValues.setAge(38);
//        outValues.setPosition(new String[]{"Founder", "CTO", "Writer"});
//        Map<String, String> salary = new HashMap() {{
//            put("2010", new BigDecimal(10000));
//        }};
//        outValues.setSalary(salary);
//        outValues.setSkills(Arrays.asList("java", "python", "node", "kotlin"));

        HashMap <String, Double> somemap = new HashMap<>() {{
            put("Total Born", 100.0);
            put("Total Dead", 10.0);
        }};

    }
}

