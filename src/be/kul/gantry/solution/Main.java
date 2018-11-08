package be.kul.gantry.solution;

import be.kul.gantry.Extra.CSVFileWriter;
import be.kul.gantry.domain.Problem;

import java.io.File;

/**
 * @author Pelle Reyniers & Boris Wauters
 */
public class Main {

    public static void main(String[] args) {

        File inputFile = new File("1_10_100_4_FALSE_65_50_50.json");
        Problem problem;
        Solution solution;
        CSVFileWriter csvFileWriter = new CSVFileWriter();
        try {
            problem = Problem.fromJson(inputFile);
            solution = new Solution(problem,csvFileWriter);
            solution.solve();
        } catch (Exception e) {
            e.printStackTrace();
        }
        csvFileWriter.flush();

        /*dat commando:
        * java -jar validator-v7.jar -debug 1_10_100_4_FALSE_65_50_50.json output.csv
        * */



    }
}

