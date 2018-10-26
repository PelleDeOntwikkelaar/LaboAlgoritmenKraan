package be.kul.gantry.solution;

import be.kul.gantry.domain.Problem;
import be.kul.gantry.domain.Slot;

import java.io.File;

/**
 * @author Boris Wauters
 */
public class Main {

    public static void main(String[] args){

        File inputFile = new File("1_10_100_4_FALSE_65_50_50.json");
        Problem problem;
        Solution solution;
        try {
            problem = Problem.fromJson(inputFile);
            solution = new Solution(problem);
            System.out.println("Hello World");
        } catch (Exception e) {
            System.err.println(e);
        }


        System.out.println("hello");


    }
}

