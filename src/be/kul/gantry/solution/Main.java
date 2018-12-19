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


        boolean shifted =isShifted(args[0]);
        File inputFile = new File(args[0]);


        Problem problem;
        Solution solution;
        CSVFileWriter csvFileWriter = new CSVFileWriter(args[1]);
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

    public static boolean isShifted(String inputName){
        int count=0;
        int startIndex=0;
        boolean set=false;
        for (int i=0;i<inputName.toCharArray().length;i++){
            if(inputName.toCharArray()[i]=='_') count ++;
            if(count==4 && !set) {
                startIndex=i+1;
                set=true;
            }
        }
        int stopIndex=startIndex+4;
        String boolStr=inputName.substring(startIndex,stopIndex);
        if(boolStr.equals("TRUE"))return true;
        return false;
    }
}

