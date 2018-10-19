package be.kul.gantry.solution;

import be.kul.gantry.domain.Problem;

import java.io.File;

/**
 * @author Boris Wauters
 */
public class Main {

    public static void main(String[] args){

        File inputFile = new File("1_10_100_4_FALSE_65_50_50.json");

        try {
            Problem problem = Problem.fromJson(inputFile);
            System.out.println(problem.getMaxX());
        } catch (Exception e) {
            System.err.println(e);
        }




    }
}

