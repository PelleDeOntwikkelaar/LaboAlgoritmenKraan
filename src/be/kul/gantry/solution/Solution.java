package be.kul.gantry.solution;

import be.kul.gantry.Extra.CSVFileWriter;
import be.kul.gantry.domain.*;

import java.util.*;

/**
 * @author pellereyniers & boriswauters
 */
public class Solution {

    private Problem problem;
    private CSVFileWriter csvFileWriter;

    private Slots slots;

    private Queue<Job> inputQueue;
    private Queue<Job> outputQueue;
    private LinkedList<Job> precedingJobs;

    private Job jobToSolve;

    private List<Gantry> gantries;

    private double time;
    private int jobNumber;

    private LinkedList<Job> currentJobs;
    private int globalTime;


    public Solution(Problem problem, CSVFileWriter csvFileWriter, boolean shifted) {
        this.csvFileWriter = csvFileWriter;
        this.problem = problem;
        initializeParameters(shifted);
    }

    private void initializeParameters(boolean shifted) {
        //Slots object is initialized with correct parameters.
        slots = new Slots(problem.getMaxX(), problem.getMaxY(), problem.getMaxLevels(), shifted);
        //queues are provided
        precedingJobs = new LinkedList<>();
        inputQueue = new LinkedList<>();
        outputQueue = new LinkedList<>();
        ((LinkedList<Job>) inputQueue).addAll(0, problem.getInputJobSequence());
        ((LinkedList<Job>) outputQueue).addAll(0, problem.getOutputJobSequence());
        jobToSolve = null;
        //Slots are being put into place, with correct parameters and dimensions
        slots.addSlots(problem.getSlots());
        gantries = problem.getGantries();
        time = 0;

        jobNumber = ((LinkedList<Job>) outputQueue).peekLast().getId() + 1000;
        globalTime = 0;
    }

    public void solveNextJob() {

        if ((!outputQueue.isEmpty() && !slots.containsItem(outputQueue.peek().getItem())) || outputQueue.isEmpty()) {
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
        Gantry gantry = gantries.get(0);
        Slot bestFit = null;
        if (jobToSolve.getPlace().getSlot() == null) {

            if (gantries.size() == 1) {
                //calculate drop off slot
                bestFit = slots.findBestSlot(gantry.getCurrentX(), gantry.getCurrentY(), gantry.getXSpeed(), gantry.getYSpeed(), null);
            } else {
                //calculate drop off slot when working whit multiple gantries.
                bestFit = slots.findBestSlotFirstGantry(jobToSolve, gantries, null, false, 0);
            }
            //update Job parameters
            jobToSolve.getPlace().setSlot(bestFit);
        }

        slots.addItemToSlot(jobToSolve.getItem(), bestFit);
        executeJob(jobToSolve, gantry);
        System.out.println(jobToSolve + "time: " + time);
        jobToSolve = null;
    }

    private void solveOutputJob() {
        //pre solving work: finding item, computing stacked slots,..
        Slot slot = slots.findSlotByItem(jobToSolve.getItem());
        if (jobToSolve.getPickup().getSlot() == null) {
            jobToSolve.getPickup().setSlot(slot);
        }
        int refX = slot.getCenterX();
        int refY = slot.getCenterY();

        //finding stacked items
        List<Slot> stackedItems = slots.getStackedItemSlots(slot);

        //solving stacked items
        if (!stackedItems.isEmpty()) {
            for (Slot slt : stackedItems) {
                Job job = new Job(jobNumber++, slt.getItem(), slt, null);
                job.getPickup().setSlot(slt);
                precedingJobs.addFirst(job);
            }
            for (Job job : precedingJobs) {
                int gantryIndex = slots.findSuitableGantry(job, gantries, refX, refY);
                solvePrecedingJob(job, gantryIndex, refX);
            }
            precedingJobs.clear();
        }

        //get last crane: last crane always has to perform output jobs.
        Gantry gantry = gantries.get(gantries.size() - 1);
        slots.removeItemFromSlot(jobToSolve.getItem(), jobToSolve.getPickup().getSlot());
        //execute output job.
        executeJob(jobToSolve, gantry);
        System.out.println(jobToSolve + "time: " + time);
    }

    /**
     * Method that performs a preceding job.
     *
     * @param job         Type Job: Job to complete in this method.
     * @param gantryIndex Type Integer: Crane that will perform the given job.
     */
    private void solvePrecedingJob(Job job, int gantryIndex, int refX) {
        Slot pickupSlot = job.getPickup().getSlot();
        Set<Slot> forbiddenSlots = slots.findForbiddenSlots(jobToSolve.getPickup().getSlot());
        Slot bestFit;
        if (gantries.size() == 1) {
            bestFit = slots.findBestSlot(pickupSlot.getCenterX(), pickupSlot.getCenterY(), gantries.get(gantryIndex).getXSpeed(), gantries.get(gantryIndex).getYSpeed(), forbiddenSlots);
        } else {
            boolean leftRight;
            if (gantryIndex == 0) bestFit = slots.findBestSlotFirstGantry(job, gantries, forbiddenSlots, true, refX);
            else bestFit = slots.findBestSlotSecondGantry(job, gantries, forbiddenSlots, true, refX);

        }

        slots.removeItemFromSlot(job.getItem(), pickupSlot);
        slots.addItemToSlot(job.getItem(), bestFit);

        executeJob(job, gantries.get(gantryIndex));
        System.out.println(job.toString() + "time: " + time);
    }

    /**
     * Method that performs a job excecution in two different steps.
     * 1) Time calculation for crane movement and print crane movement.
     * 2) Time calculation of pickup/delivery and dor it and print specifics
     *
     * @param job    Type Job: Job to complete in this method.
     * @param gantry Type Gantry: Crane that will perform the given job.
     */
    public void executeJob(Job job, Gantry gantry) {

        //move gantry to pickup slot and perform time analysis
        job.performTask(gantry, job.getPickup());
        time += job.getPickup().getTime();
        //print status after pickup task
        job.printStatus(gantry, csvFileWriter, time, Job.TaskType.PICKUP);
        time += 10;

        //move gantry to place slot and perform time analysis
        job.performTask(gantry, job.getPlace());
        time += job.getPlace().getTime();
        //print status after place task
        job.printStatus(gantry, csvFileWriter, time, Job.TaskType.PLACE);
        time += 10;
    }

    /**
     * Method with a while loop that keeps on solving jobs until there are none left.
     */
    public void solve() {
        /*while (!(inputQueue.isEmpty() && outputQueue.isEmpty() && jobToSolve == null && precedingJobs.isEmpty())) {
            solveNextJob();
        }*/

        Boolean continueLoop = true;
        while (continueLoop) {

            /*check currentJobs
             * on time print part of job
             * if job is done => remove job from list, set gantry to idle
             * assign new job to idle gantry
             * todo: for inputGantry prioritize digging over input jobs
             * todo: extra*/
            for (Job job : currentJobs) {

                if (globalTime == job.getStartingTimePlace()) {
                    // todo: print place
                } else if (globalTime == job.getStartingTimePickup()) {
                    // todo: print pickup
                } else if (globalTime > job.getStartingTimePlace() + job.getPlace().getTime()) {
                    // todo: remove job, set gantry to idle
                }


            }

            // todo: assign new job to idle gantry

            continueLoop = checkLoop();
            //globalTime += setToNextSafeTime();
            globalTime++;
        }

    }

    private int setToNextSafeTime() {
        int shortestTime = Integer.MAX_VALUE;
        //todo: check all gantries, when one is idle, check pos, if save, move tim to that position
        for (Gantry gantry : gantries) {
            if (gantry.getCurrentJob() != null) {
                if (gantry.getCurrentJob().getRemainingTime() < shortestTime) {
                    shortestTime = gantry.getCurrentJob().getRemainingTime();
                }
            }
        }

        int safeTime = findMaxSafeTime(shortestTime);

    }

    private boolean checkLoop() {
        if (inputQueue.isEmpty() && outputQueue.isEmpty() && precedingJobs.isEmpty() && currentJobs.isEmpty()) {
            return false;
        }
        return true;
    }

    private int findMaxSafeTime(int time) {

    }

}
