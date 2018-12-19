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
        }
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

    private Job solveInputJob() {
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

        return jobToSolve;
    }

    private Job solveOutputJob() {
        //pre solving work: finding item, computing stacked slots,..
        Slot slot = slots.findSlotByItem(jobToSolve.getItem());
        if (jobToSolve.getPickup().getSlot() == null) {
            jobToSolve.getPickup().setSlot(slot);
        }

        //finding stacked items
        List<Slot> stackedItems = slots.getStackedItemSlots(slot);

        //solving stacked items
        if (!stackedItems.isEmpty()) {
            for (Slot slt : stackedItems) {
                Job job = new Job(jobNumber++, slt.getItem(), slt, null);
                job.getPickup().setSlot(slt);
                precedingJobs.addFirst(job);
            }
            precedingJobs.addLast(jobToSolve);
            return null;
        } else {
            return jobToSolve;
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
        Set<Slot> forbiddenSlots = slots.findForbiddenSlots(jobToSolve.getPickup().getSlot());
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

            for (Gantry gantry : gantries) {
                Job job = gantry.getCurrentJob();
                if (job == null) {
                    if (!precedingJobs.isEmpty()) {
                        // assign preceding job
                        gantry.setCurrentJob(precedingJobs.pollFirst());

                        gantry.getCurrentJob().getPickup().calculateTime(gantry);
                        gantry.getCurrentJob().getPlace().calculateTime(gantry);

                        solvePrecedingJob(gantry.getCurrentJob(), gantry.getId(), gantry.getCurrentX());
                    } else {
                        if (gantry.getId() == 0) {
                            if (!inputQueue.isEmpty()) {
                                // assign input job
                                gantry.setCurrentJob(inputQueue.poll());
                                jobToSolve = gantry.getCurrentJob();
                                solveInputJob();
                            } else {
                                // empty input queue
                                continue;
                            }
                        } else {
                            // assign output job
                            jobToSolve = outputQueue.peek();
                            Item item = jobToSolve.getItem();

                            if (slots.containsItem(item)) {
                                jobToSolve = outputQueue.poll();
                                if (solveOutputJob() == null)
                                    gantry.setCurrentJob(precedingJobs.pollFirst());
                                else gantry.setCurrentJob(jobToSolve);
                            } else continue;
                        }
                    }
                    job = gantry.getCurrentJob();

                    job.getPickup().calculateTime(gantry);
                    job.getPlace().calculateTime(gantry);

                    job.setStartingTimePickup(globalTime);
                    job.setStartingTimePlace(globalTime + job.getPickup().getTime() + problem.getPickupPlaceDuration());
                }

                if (globalTime == job.getStartingTimePickup()) {
                    // start of a pickup task
                    gantry.printStatus(globalTime);
                } else if (globalTime == job.getStartingTimePickup() + job.getPickup().getTime()) {
                    // end of movement of a pickup task
                    gantry.moveCrane(job.getPickup().getSlot().getCenterX(), job.getPickup().getSlot().getCenterY());
                    gantry.printStatus(globalTime);
                }else if (globalTime == job.getStartingTimePickup() + job.getPickup().getTime() + problem.getPickupPlaceDuration()) {
                    // end of a pickup task
                    Item item = gantry.getCurrentJob().getItem();
                    Slot slot = gantry.getCurrentJob().getPickup().getSlot();
                    if (!slot.isInputSlot()) slots.removeItemFromSlot(item, slot);
                    gantry.printStatus(globalTime);
                }

                if (globalTime == job.getStartingTimePlace()) {
                    // start of a place task
                    gantry.printStatus(globalTime);
                } else if (globalTime == job.getStartingTimePlace() + job.getPlace().getTime()) {
                    // end of movement of a place task
                    gantry.moveCrane(job.getPlace().getSlot().getCenterX(), job.getPlace().getSlot().getCenterY());
                    gantry.printStatus(globalTime);
                } else if (globalTime == job.getStartingTimePlace() + job.getPlace().getTime() + problem.getPickupPlaceDuration()) {
                    // end of a place task
                    Item item = gantry.getCurrentJob().getItem();
                    Slot slot = gantry.getCurrentJob().getPlace().getSlot();
                    if (!slot.isOutputSlot())
                        slots.addItemToSlot(item, slot);
                    gantry.printStatus(globalTime);
                }


                if (globalTime >= job.getStartingTimePlace() + job.getPlace().getTime() + problem.getPickupPlaceDuration()) {
                    gantry.setCurrentJob(null);
                }
            }

            continueLoop = checkLoop();
            globalTime++;
            System.out.println("iq: " + inputQueue.size() + "   oq: " + outputQueue.size());
            if (inputQueue.isEmpty()) {
                for (Job job : outputQueue) {
                    System.out.println(job.getItem().getId() + " : " + slots.containsItem(job.getItem()));
                }
            }
        }

    }

    /*
    private int setToNextSafeTime(){
        int shortestTime=Integer.MAX_VALUE;
        //todo: check all gantries, when one is idle, check pos, if save, move tim to that position
        for(Gantry gantry: gantries){
            if(gantry.getCurrentJob()!=null){
                if (gantry.getCurrentJob().getRemainingTime() < shortestTime) {
                    shortestTime = gantry.getCurrentJob().getRemainingTime();
                }
            }
        }

        int safeTime= findMaxSafeTime(shortestTime);

    }
    */

    private boolean checkLoop() {
        if (inputQueue.isEmpty() && outputQueue.isEmpty() && precedingJobs.isEmpty() && currentJobs.isEmpty()) {
            return false;
        }
        return true;
    }


}
