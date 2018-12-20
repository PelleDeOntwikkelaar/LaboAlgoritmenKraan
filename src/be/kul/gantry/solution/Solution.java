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

        if (problem.getGantries().size() == 1) {

            while (!(inputQueue.isEmpty() && outputQueue.isEmpty() && jobToSolve == null && precedingJobs.isEmpty())) {
                solveNextJobOne();
            }
            return;
        }


        Boolean continueLoop = true;
        while (continueLoop) {

            for (Gantry gantry : gantries) {
                Gantry otherGantry = null;
                for (Gantry gantry1 : gantries) {
                    if (gantry1 != gantry) {
                        otherGantry = gantry1;
                    }
                }
                //if gantry is idle -> assign next job
                if (gantry.getMode() == Gantry.gantryMode.IDLE) {
                    if (!precedingJobs.isEmpty()) {
                        // assign preceding job
                        Job nextJob = precedingJobs.peek();

                        if (nextJob.getPickup().getSlot().getCenterX() < (otherGantry.getCurrentX() + problem.getSafetyDistance())) {
                            nextJob = precedingJobs.poll();


                            if (nextJob.getPlace().getSlot() != null && gantry.getId() == 1) {
                                gantry.setCurrentJob(nextJob);
                            } else if (nextJob.getPlace().getSlot() == null) {
                                nextJob = solvePrecedingJob(nextJob, gantry.getId(), gantry.getCurrentX());
                                gantry.setCurrentJob(nextJob);
                            }
                            gantry.checkForIdleTransition(globalTime, otherGantry);
                        }
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
                                gantry.setMoveToY(5);
                                gantry.printStatus(globalTime);
                                gantry.setMode(Gantry.gantryMode.MOVE);
                                gantry.setMakeMoveTransition(false);
                            }

                        } else {
                            // assign output job
                            Job nextJob = outputQueue.peek();
                            if (nextJob != null) {
                                nextJob = solveOutputJob(nextJob);
                            }
                            if (nextJob == null && !precedingJobs.isEmpty()) {
                                nextJob = precedingJobs.poll();
                                nextJob = solvePrecedingJob(nextJob, gantry.getId(), gantry.getCurrentX());
                                gantry.setCurrentJob(nextJob);
                            } else if (nextJob != null) {
                                nextJob = outputQueue.poll();
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

            HashSet<Slot> slotHashSet = new HashSet<>();

            for (Gantry gantry : gantries) {
                if (gantry.getCurrentJob() == null) continue;
                if (gantry.getCurrentJob().getPlace().getSlot().isOutputSlot()) continue;

                slotHashSet.add(gantry.getCurrentJob().getPlace().getSlot());
            }
            for (Slot slot : problem.getSlots()) {
                if (!slotHashSet.contains(slot)) {
                    slot.setReserved(false);
                }
            }
        }
    }


    private boolean checkLoop() {
        if (inputQueue.isEmpty() && outputQueue.isEmpty() && precedingJobs.isEmpty()
                && gantries.get(0).getMode() == Gantry.gantryMode.IDLE
                && gantries.get(1).getMode() == Gantry.gantryMode.IDLE) {

            return false;
        }
        if (globalTime > 300000) return false;
        return true;
    }


    private void solveNextJobOne() {

        if ((!outputQueue.isEmpty() && !slots.containsItem(outputQueue.peek().getItem())) || outputQueue.isEmpty()) {
            // if the item we want to extract isn't in storage we'll do an input job
            if (!inputQueue.isEmpty()) {
                jobToSolve = inputQueue.poll();
                solveInputJobOne();
            }
        } else if (!outputQueue.isEmpty()) {
            // solve an output job
            jobToSolve = outputQueue.poll();
            solveOutputJobOne();
        }
    }

    private void solveInputJobOne() {

        // If there's only one gantry
        Gantry gantry = gantries.get(0);

        if (jobToSolve.getPlace().getSlot() == null) {

            //calculate drop off slot
            Slot bestFit = slots.findBestSlot(gantry.getCurrentX(), gantry.getCurrentY(), gantry.getXSpeed(), gantry.getYSpeed(), null);

            //update Job parameters
            jobToSolve.getPlace().setSlot(bestFit);
            slots.addItemToSlot(jobToSolve.getItem(), bestFit);
        }

        executeJobOne(jobToSolve, gantry);
        System.out.println(jobToSolve + "time: " + time);

        jobToSolve = null;
    }

    private void solveOutputJobOne() {

        // If there's only one gantry
        Gantry gantry = gantries.get(0);

        if (jobToSolve.getPickup().getSlot() == null) {
            Slot slot = slots.findSlotByItem(jobToSolve.getItem());
            jobToSolve.getPickup().setSlot(slot);
            List<Slot> stackedItems = slots.getStackedItemSlots(slot);
            if (!stackedItems.isEmpty()) {
                for (Slot slt : stackedItems) {
                    Job job = new Job(jobNumber++, slt.getItem(), slt, null);
                    job.getPickup().setSlot(slt);
                    precedingJobs.addFirst(job);
                }
                // execute all preceding jobs
                for (Job job : precedingJobs) {
                    solvePrecedingJobOne(job, gantry);
                }
                precedingJobs.clear();
            }

            slots.removeItemFromSlot(jobToSolve.getItem(), jobToSolve.getPickup().getSlot());
        }

        executeJobOne(jobToSolve, gantry);
        System.out.println(jobToSolve + "time: " + time);

        jobToSolve = null;
    }

    /**
     * Method that performs a preceding job.
     *
     * @param job    Type Job: Job to complete in this method.
     * @param gantry Type Gantry: Crane that will perform the given job.
     */
    private void solvePrecedingJobOne(Job job, Gantry gantry) {

        Slot pickupSlot = job.getPickup().getSlot();

        Set<Slot> forbiddenSlots = slots.findForbiddenSlots(jobToSolve.getPickup().getSlot());
        Slot bestFit = slots.findBestSlot(pickupSlot.getCenterX(), pickupSlot.getCenterY(), gantry.getXSpeed(), gantry.getYSpeed(), forbiddenSlots);
        job.getPlace().setSlot(bestFit);

        slots.removeItemFromSlot(job.getItem(), pickupSlot);
        slots.addItemToSlot(job.getItem(), bestFit);

        executeJobOne(job, gantry);
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
    public void executeJobOne(Job job, Gantry gantry) {

        //move gantry to pickup slot and perform time analysis
        job.performTaskOne(gantry, job.getPickup());
        time += job.getPickup().getTime();
        //print status after pickup task
        job.printStatusOne(gantry, csvFileWriter, time, Job.TaskType.PICKUP);
        time += 10;

        //move gantry to place slot and perform time analysis
        job.performTaskOne(gantry, job.getPlace());
        time += job.getPlace().getTime();
        //print status after place task
        job.printStatusOne(gantry, csvFileWriter, time, Job.TaskType.PLACE);
        time += 10;
    }


}
