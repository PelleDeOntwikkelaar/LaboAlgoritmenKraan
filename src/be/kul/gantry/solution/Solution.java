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

    private Queue<Job> inputQueue;
    private Queue<Job> outputQueue;

    private PriorityQueue<Job> precedingJobs;
    private Job jobToSolve;
    private Problem problem;

    private int currentX;
    private int currentY;

    public Solution(Problem problem) {
        this.problem = problem;
        initializeParameters();
    }

    private void initializeParameters() {
        slots = new Slots(problem.getItems(), problem.getMaxX(), problem.getMaxY(), problem.getMaxLevels(), false);
        precedingJobs = new PriorityQueue<>();
        inputQueue = new LinkedList<>();
        outputQueue = new LinkedList<>();
        ((LinkedList<Job>) inputQueue).addAll(0, problem.getInputJobSequence());
        ((LinkedList<Job>) outputQueue).addAll(0, problem.getOutputJobSequence());
        jobToSolve = null;
        slots.addSlots(problem.getSlots());

        // has to change if we want to include a secondary gantry
        currentX = problem.getGantries().get(0).getStartX();
        currentY = problem.getGantries().get(0).getStartY();
    }

    public void solveNextJob() {
        if (!precedingJobs.isEmpty()) {
            // we'll solve a preceding job
            jobToSolve = precedingJobs.poll();
            solveJob();
        } else if (!outputQueue.isEmpty() && !slots.containsItem(outputQueue.peek().getItem())) {
            // if the item we want to extract isn't in storage we'll do an input job
            if (!inputQueue.isEmpty()) {
                jobToSolve = inputQueue.poll();
                solveInputJob();
            }
        } else if (!outputQueue.isEmpty()) {
            // solve an output job
            jobToSolve = outputQueue.poll();
            solveOutputJob();
        }
    }

    private void solveInputJob(){
        //todo improve method
        if (jobToSolve.getPlace().getSlot() == null) {
            Slot bestFit = slots.findBestSlot(0, currentX, currentY);
            jobToSolve.getPlace().setSlot(bestFit);
            slots.addItemToSlot(jobToSolve.getItem(), bestFit);
        }
        System.out.println(jobToSolve);
        jobToSolve = null;
    }

    private void solveOutputJob(){
        if (jobToSolve.getPickup().getSlot() == null){
            Slot slot = slots.findSlotByItem(jobToSolve.getItem());
            jobToSolve.getPickup().setSlot(slot);

            if (!slots.getStackedItemSlots(slot).isEmpty()) {
                //todo make new preceding jobs and execute them first
            }
            slots.removeItemFromSlot(jobToSolve.getItem(), jobToSolve.getPickup().getSlot());
        }
        System.out.println(jobToSolve);
        jobToSolve = null;
    }

    private void solveJob() {
        //todo: implementatie van de job solver, deze bestaat uit de aard van de job uitzoek, ten minste de drop off locatie of pickup locatie berekenen.
        //na het uitvoeren van de job moet de jobToSolve varibele terug op null gezet worden.
        System.out.println(jobToSolve.toString());
        jobToSolve = null;

    }

    public void solve() {
        while (!inputQueue.isEmpty() && !outputQueue.isEmpty() && jobToSolve == null) {
            solveNextJob();
        }
    }


}
