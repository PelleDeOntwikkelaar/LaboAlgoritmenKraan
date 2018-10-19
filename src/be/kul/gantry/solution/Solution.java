package be.kul.gantry.solution;

import be.kul.gantry.domain.Job;
import be.kul.gantry.domain.Problem;
import be.kul.gantry.domain.Slot;
import be.kul.gantry.domain.Slots;

import java.util.PriorityQueue;
import java.util.Queue;

public class Solution {
    private Slots slots;
    private Queue<Job>inputQueue;
    private Queue<Job>outputQueue;

    private PriorityQueue<Job>tussenkomendeJobs;
    private Job jobToSolve;
    private Problem problem;

    public Solution(Problem problem) {
        this.problem=problem;
        initializeParameters();
    }

    private void initializeParameters() {
        slots=new Slots(problem.getItems(),problem.getMaxX(),problem.getMaxY(),problem.getMaxLevels(),false);
        tussenkomendeJobs=new PriorityQueue<>();
        jobToSolve=null;
    }

    public void solveNextJob(){

    }


}
