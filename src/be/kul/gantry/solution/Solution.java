package be.kul.gantry.solution;

import be.kul.gantry.Extra.CSVFileWriter;
import be.kul.gantry.domain.*;

import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class Solution {
    private Slots slots;

    private Queue<Job> inputQueue;
    private Queue<Job> outputQueue;

    private LinkedList<Job> precedingJobs;
    private Job jobToSolve;
    private Problem problem;

    private List<Gantry> gantries;

    private CSVFileWriter csvFileWriter;

    public Solution(Problem problem, CSVFileWriter csvFileWriter) {
        this.csvFileWriter = csvFileWriter;
        this.problem = problem;
        initializeParameters();
    }

    private void initializeParameters() {
        slots = new Slots(problem.getItems(), problem.getMaxX(), problem.getMaxY(), problem.getMaxLevels(), false);
        precedingJobs = new LinkedList<>();
        inputQueue = new LinkedList<>();
        outputQueue = new LinkedList<>();
        ((LinkedList<Job>) inputQueue).addAll(0, problem.getInputJobSequence());
        ((LinkedList<Job>) outputQueue).addAll(0, problem.getOutputJobSequence());
        jobToSolve = null;
        slots.addSlots(problem.getSlots());
        gantries = problem.getGantries();
    }

    public void solveNextJob() {
        if (!outputQueue.isEmpty() && !slots.containsItem(outputQueue.peek().getItem())) {
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

    private void solveInputJob() {
        //todo improve method

        // If there's only one gantry
        Gantry gantry = gantries.get(0);

        if (jobToSolve.getPlace().getSlot() == null) {
            Slot bestFit = slots.findBestSlot(0, gantry.getCurrentX(), gantry.getCurrentY());
            jobToSolve.getPlace().setSlot(bestFit);
            slots.addItemToSlot(jobToSolve.getItem(), bestFit);
        }
        System.out.println(jobToSolve);

        csvFileWriter.add(jobToSolve.getOutput(gantry));
        //csvFileWriter.addLine(jobToSolve.toString());

        jobToSolve = null;
    }

    private void solveOutputJob() {

        // If there's only one gantry
        Gantry gantry = gantries.get(0);

        if (jobToSolve.getPickup().getSlot() == null) {
            Slot slot = slots.findSlotByItem(jobToSolve.getItem());
            jobToSolve.getPickup().setSlot(slot);

            if (!slots.getStackedItemSlots(slot).isEmpty()) {
                //todo: make new preceding jobs and execute them first
                for (Slot slt : slots.getStackedItemSlots(slot)) {
                    Job job = new Job(1000, slt.getItem(), slt, null);
                    job.getPickup().setSlot(slt);
                    precedingJobs.addFirst(job);
                }
                // execute all preceding jobs
                for (Job job : precedingJobs) {
                    solvePrecedingJob(job, gantry);
                }
                precedingJobs.clear();
            }
            slots.removeItemFromSlot(jobToSolve.getItem(), jobToSolve.getPickup().getSlot());
        }
        System.out.println(jobToSolve);

        csvFileWriter.add(jobToSolve.getOutput(gantry));
        //csvFileWriter.addLine(jobToSolve.toString());
        jobToSolve = null;
    }

    private void solvePrecedingJob(Job job, Gantry gantry) {
        Slot bestFit = slots.findBestSlot(0, gantry.getCurrentX(), gantry.getCurrentY());
        //todo: maybe a different method for best fit when it comes to preceding jobs
        job.getPlace().setSlot(bestFit);

        slots.removeItemFromSlot(job.getItem(), job.getPickup().getSlot());
        slots.addItemToSlot(job.getItem(), bestFit);

        System.out.println(job.toString());

        csvFileWriter.add(job.getOutput(gantry));
        //csvFileWriter.addLine(jobToSolve.toString());
    }

    public void solve() {
        while (!inputQueue.isEmpty() && !outputQueue.isEmpty() && jobToSolve == null) {
            solveNextJob();
        }
    }


}
