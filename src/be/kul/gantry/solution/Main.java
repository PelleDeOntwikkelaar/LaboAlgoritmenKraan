package be.kul.gantry.solution;

import be.kul.gantry.Extra.CSVFileWriter;
import be.kul.gantry.domain.Problem;

import java.io.File;
import java.util.Scanner;

/**
 * @author Pelle Reyniers & Boris Wauters
 */
public class Main {

    public static void main(String[] args) {
        String outputFileName ="output.csv";
        String inputFileName1 = "1_10_100_4_FALSE_65_50_50.json";
        String inputFileName2 = "1_10_100_4_TRUE_65_50_50.json";


        Scanner sc = new Scanner(System.in);
        int choice;
        boolean shifted;

        System.out.println("Choose your input file:");
        System.out.println("    1. One gantry, stacked rows");
        System.out.println("    2. One gantry, shifted rows");

        choice = sc.nextInt();

        File inputFile;
        File oneGantryFalseFile = new File(inputFileName1);
        File oneGantryTrueFile = new File(inputFileName2);
        // File twoGantryTrueFile = new File("1_10_100_4_FALSE_65_50_50.json");

        if (choice == 1) {
            inputFile = oneGantryFalseFile;
            shifted = false;
        } else if (choice == 2) {
            inputFile = oneGantryTrueFile;
            shifted = true;
        }
        else return;

        Problem problem;
        Solution solution;
        CSVFileWriter csvFileWriter = new CSVFileWriter(outputFileName);
        try {
            problem = Problem.fromJson(inputFile);
            solution = new Solution(problem,csvFileWriter, shifted);
            solution.solve();
        } catch (Exception e) {
            e.printStackTrace();
        }
        csvFileWriter.flush();

        /*dat commando:
        * java -jar validator-v7.jar -debug 1_10_100_4_FALSE_65_50_50.json output.csv
        * java -jar validator-v7.jar -debug 1_10_100_4_TRUE_65_50_50.json output.csv
        * */



    }
}

