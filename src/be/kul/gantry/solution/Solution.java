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
        for (Gantry gantry : gantries) {
            gantry.setPickUpPlaceDuration(problem.getPickupPlaceDuration());
            gantry.setSlots(slots);
            gantry.setCsvFileWriter(csvFileWriter);
        }
        time = 0;

        jobNumber = ((LinkedList<Job>) outputQueue).peekLast().getId() + 1000;
        globalTime = 0;
    }

    private Job solveInputJob(Job inputJobToSolve) {
        Gantry gantry = gantries.get(0);
        Slot bestFit = null;
        if (inputJobToSolve.getPlace().getSlot() == null) {

            if (gantries.size() == 1) {
                //calculate drop off slot
                bestFit = slots.findBestSlot(gantry.getCurrentX(), gantry.getCurrentY(), gantry.getXSpeed(), gantry.getYSpeed(), null);
            } else {
                //calculate drop off slot when working whit multiple gantries.
                bestFit = slots.findBestSlotFirstGantry(inputJobToSolve, gantries, null, false, 0);
            }
            //update Job parameters
            inputJobToSolve.getPlace().setSlot(bestFit);
        }

        return inputJobToSolve;
    }

    private Job solveOutputJob(Job nexOutputJobToSolve) {
        //pre solving work: finding item, computing stacked slots,..
        Slot slot = slots.findSlotByItem(nexOutputJobToSolve.getItem());
        if (slot == null) {
            return null;
        }

        if (nexOutputJobToSolve.getPickup().getSlot() == null) {
            nexOutputJobToSolve.getPickup().setSlot(slot);
        }

        //finding stacked items
        List<Slot> stackedItems = slots.getStackedItemSlots(slot);

        //solving stacked items
        if (!stackedItems.isEmpty()) {
            for (Slot slt : stackedItems) {
                Job job = new Job(jobNumber++, slt.getItem(), slt, null);
                job.getPickup().setSlot(slt);
                job.setForbiddenSlots(slots.findForbiddenSlots(nexOutputJobToSolve.getPickup().getSlot()));
                precedingJobs.addFirst(job);
            }
            precedingJobs.addLast(nexOutputJobToSolve);
            return null;
        } else {
            return nexOutputJobToSolve;
        }

    }

    /**
     * Method that performs a preceding job.
     *
     * @param job         Type Job: Job to complete in this method.
     * @param gantryIndex Type Integer: Crane that will perform the given job.
     */
    private Job solvePrecedingJob(Job job, int gantryIndex, int refX) {
        Slot pickupSlot = job.getPickup().getSlot();
        Set<Slot> forbiddenSlots = job.getForbiddenSlots();
        Slot bestFit;
        if (job.getPlace().getSlot() == null) {
            if (gantries.size() == 1) {
                bestFit = slots.findBestSlot(pickupSlot.getCenterX(), pickupSlot.getCenterY(), gantries.get(gantryIndex).getXSpeed(), gantries.get(gantryIndex).getYSpeed(), forbiddenSlots);
            } else {
                if (gantryIndex == 0)
                    bestFit = slots.findBestSlotFirstGantry(job, gantries, forbiddenSlots, true, refX);
                else bestFit = slots.findBestSlotSecondGantry(job, gantries, forbiddenSlots, true, refX);

            }

            job.getPlace().setSlot(bestFit);
        }

        return job;
    }


    /**
     * Method with a while loop that keeps on solving jobs until there are none left.
     */
    public void solve() {

        Boolean continueLoop = true;
        while (continueLoop) {

            //todo: wait times & safetygap.

            for (Gantry gantry: gantries) {
                Gantry otherGantry = null;
                for (Gantry gantry1 : gantries) {
                    if (gantry1 != gantry) {
                        otherGantry = gantry;
                    }
                }
                //if gantry is idle -> assign next job
                if (gantry.getMode() == Gantry.gantryMode.IDLE) {
                    if (!precedingJobs.isEmpty()) {
                        // assign preceding job
                        Job nextJob = precedingJobs.poll();

                        if (nextJob.getPlace().getSlot() != null && gantry.getId() == 1) {
                            gantry.setCurrentJob(nextJob);
                        } else if (nextJob.getPlace().getSlot() == null) {
                            nextJob = solvePrecedingJob(nextJob, gantry.getId(), gantry.getCurrentX());
                            gantry.setCurrentJob(nextJob);
                        }
                        gantry.checkForIdleTransition(globalTime,otherGantry);
                    }

                    if (gantry.getMode() == Gantry.gantryMode.IDLE) {
                        //if gantry == input gantry -> assign input job, else -> output job.
                        if (gantry.getId() == 0) {
                            // assign input job
                            Job nextJob = inputQueue.poll();
                            if (nextJob != null) {
                                gantry.setCurrentJob(solveInputJob(nextJob));
                            } else {
                                gantry.setMoveToX(-15);
                                gantry.setMoveToX(5);
                                gantry.printStatus(globalTime);
                                gantry.setMode(Gantry.gantryMode.MOVE);
                            }

                        } else {
                            // assign ouput job
                            Job nextJob = outputQueue.poll();
                            if (nextJob != null) {
                                nextJob = solveOutputJob(nextJob);
                            }
                            if (nextJob == null && !precedingJobs.isEmpty()) {
                                nextJob = precedingJobs.poll();
                                nextJob = solvePrecedingJob(nextJob, gantry.getId(), gantry.getCurrentX());
                                gantry.setCurrentJob(nextJob);
                            }

                        }
                        gantry.checkForIdleTransition(globalTime, otherGantry);
                    }


                }

                //job is assigned, now perform action on time step.

                gantry.performTimeStep(globalTime, gantries);

            }
            System.out.println(globalTime);
            continueLoop = checkLoop();
            globalTime++;
        }
    }


    private boolean checkLoop() {
        if (inputQueue.isEmpty() && outputQueue.isEmpty() && precedingJobs.isEmpty()
                && gantries.get(0).getMode()== Gantry.gantryMode.IDLE
                && gantries.get(1).getMode()== Gantry.gantryMode.IDLE) {

            return false;
        }
        if(globalTime>300000)return false;
        return true;
    }


}
