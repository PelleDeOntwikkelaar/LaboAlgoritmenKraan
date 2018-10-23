package be.kul.gantry.solution;

import be.kul.gantry.domain.Job;
import be.kul.gantry.domain.Problem;
import be.kul.gantry.domain.Slot;
import be.kul.gantry.domain.Slots;

import java.util.LinkedList;
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
        inputQueue=new LinkedList<>();
        outputQueue= new LinkedList<>();
        ((LinkedList<Job>) inputQueue).addAll(0,problem.getInputJobSequence());
        ((LinkedList<Job>) outputQueue).addAll(0,problem.getOutputJobSequence());
        jobToSolve=null;
    }

    public void solveNextJob(){
        if(!tussenkomendeJobs.isEmpty()) {
            jobToSolve = tussenkomendeJobs.poll();
            solveJob();
        } else if(!outputQueue.isEmpty()&&slots.findSlotByItem(outputQueue.peek().getItem())!=null){
            if(!inputQueue.isEmpty()){
                jobToSolve = inputQueue.poll();
                solveJob();
            }
        }else if(!outputQueue.isEmpty()){
            jobToSolve=outputQueue.poll();
            solveJob();
        }
    }

    private void solveJob(){
        //todo: implementatie van de job solver, deze bestaat uit de aard van de job uitzoek, ten minste de drop off locatie of pickup locatie berekenen.
        //na het uitvoeren van de job moet de jobToSolve varibele terug op null gezet worden.

        jobToSolve=null;

    }

    public void solve(){
        while(!inputQueue.isEmpty()&&!outputQueue.isEmpty()&&jobToSolve==null){
            solveNextJob();
        }
    }


}
