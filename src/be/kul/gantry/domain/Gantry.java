package be.kul.gantry.domain;

import be.kul.gantry.Extra.CSVFileWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wim on 27/04/2015.
 */
public class Gantry {

    private final int id;
    private final int xMin, xMax;
    private final int startX, startY;
    private final double xSpeed;
    private final double ySpeed;
    private final int safetyGap;

    private int currentX;
    private int currentY;

    private int moveToX;
    private int moveToY;

    private Job currentJob;
    private gantryMode mode;
    private int pickUpPlaceCountDown;
    private int pickUpPlaceDuration;

    private Slots slots;

    private CSVFileWriter csvFileWriter;

    private Boolean makeMoveTransition;


    public Gantry(int id,
                  int xMin, int xMax,
                  int startX, int startY,
                  double xSpeed, double ySpeed,
                  int safetyGap) {
        this.id = id;
        this.xMin = xMin;
        this.xMax = xMax;
        this.startX = startX;
        this.startY = startY;
        this.xSpeed = xSpeed;
        this.ySpeed = ySpeed;
        this.safetyGap = safetyGap;

        this.currentX = startX;
        this.currentY = startY;
        mode = gantryMode.IDLE;

        moveToX = currentX;
        moveToY = currentY;


        makeMoveTransition = true;
    }

    public void setSlots(Slots slots) {
        this.slots = slots;
    }

    public void setCsvFileWriter(CSVFileWriter csvFileWriter) {
        this.csvFileWriter = csvFileWriter;
    }

    public int getId() {
        return id;
    }

    public int getXMax() {
        return xMax;
    }

    public int getXMin() {
        return xMin;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public double getXSpeed() {
        return xSpeed;
    }

    public double getYSpeed() {
        return ySpeed;
    }

    public int getCurrentX() {
        return currentX;
    }

    public void setCurrentX(int currentX) {
        this.currentX = currentX;
    }

    public int getCurrentY() {
        return currentY;
    }

    public void setCurrentY(int currentY) {
        this.currentY = currentY;
    }

    public int getMoveToX() {
        return moveToX;
    }

    public void setMoveToX(int moveToX) {
        this.moveToX = moveToX;
    }

    public int getMoveToY() {
        return moveToY;
    }

    public void setMoveToY(int moveToY) {
        this.moveToY = moveToY;
    }

    public gantryMode getMode() {
        return mode;
    }

    public void setMode(gantryMode mode) {
        this.mode = mode;
    }

    public int getPickUpPlaceDuration() {
        return pickUpPlaceDuration;
    }

    public void setPickUpPlaceDuration(int pickUpPlaceDuration) {
        this.pickUpPlaceDuration = pickUpPlaceDuration;
    }

    public boolean overlapsGantryArea(Gantry g) {
        return g.xMin < xMax && xMin < g.xMax;
    }

    public int[] getOverlapArea(Gantry g) {

        int maxmin = Math.max(xMin, g.xMin);
        int minmax = Math.min(xMax, g.xMax);

        if (minmax < maxmin)
            return null;
        else
            return new int[]{maxmin, minmax};
    }

    public boolean canReachSlot(Slot s) {
        return xMin <= s.getCenterX() && s.getCenterX() <= xMax;
    }

    public void moveCrane(int centerX, int centerY) {
        currentX = centerX;
        currentY = centerY;
    }

    public void moveCraneToNewPosition(double currentTime, Gantry otherGantry) {
        int xInt = moveToX - currentX;
        int yInt = moveToY - currentY;
        posUpdate(0, xInt, moveToX, xSpeed);
        posUpdate(1, yInt, moveToY, ySpeed);

        /*
        if (id == 1 && !calculateOtherGantryBoundary(true, otherGantry)) {

            if (!otherGantry.calculateOtherGantryBoundary(false, this)){

                moveToX = otherGantry.moveToX + safetyGap;

                mode = gantryMode.MOVE;
                makeMoveTransition = false;
                printStatus(currentTime);

            } else {
                mode = gantryMode.WAIT;
                printStatus(currentTime);
            }



        } else {
            if (makeMoveTransition) {
                posUpdate(0, xInt, moveToX, xSpeed);
                posUpdate(1, yInt, moveToY, ySpeed);
                System.out.println("posupdate: id " + id);

                checkForMoveTransition(currentTime, otherGantry);
            } else {
                makeMoveTransition = true;
            }
        }
        */

    }

    public void checkForMoveTransition(double currentTime, Gantry otherGantry) {
        if (currentJob == null) {
            if (currentX == moveToX && currentY == moveToY) {
                mode = gantryMode.IDLE;
                printStatus(currentTime);
            }
        } else {
            if (currentX == currentJob.getPickup().getSlot().getCenterX() && currentY == currentJob.getPickup().getSlot().getCenterY() && !currentJob.isPickedUp()) {
                pickUpPlaceCountDown = pickUpPlaceDuration + 1;
                mode = gantryMode.PICKUP;
                printStatus(currentTime);
            } else if(currentX == currentJob.getPlace().getSlot().getCenterX() && currentY == currentJob.getPlace().getSlot().getCenterY() && currentJob.isPickedUp()){
                pickUpPlaceCountDown = pickUpPlaceDuration + 1;
                mode = gantryMode.PLACE;
                printStatus(currentTime);
            }else{
                if (currentX == moveToX && currentY == moveToY) {
                    mode = gantryMode.WAIT;
                    printStatus(currentTime);
                }else{
                    boolean noProblem=isChangeOfStatusNotAProblem(currentTime,otherGantry);
                    if(!noProblem){
                        //probleem!!!
                        boolean noProblemCurPos=isCurrentPositionAProblem(currentTime,otherGantry);
                        if(!noProblemCurPos){
                            if(id==0){
                                moveToX=getMinimalX(otherGantry)-safetyGap;
                            }else {
                                moveToX=getMaximalX(otherGantry)+safetyGap;
                            }
                            mode = gantryMode.MOVE;
                            printStatus(currentTime);
                        }else{
                            printStatus(currentTime);
                            moveToX=currentX;
                            moveToY=currentY;
                            mode=gantryMode.WAIT;
                        }
                    }else{

                        mode = gantryMode.MOVE;
                        makeMoveTransition = false;

                    }
                }

            }

        }

    }

    private void checkForWaitOrMove(double currentTime, Gantry otherGantry) {
        if (otherGantry.moveToX + safetyGap <= currentX) {
            mode = gantryMode.WAIT;
        } else {
            moveToX = otherGantry.moveToX + safetyGap;
            moveToY = currentY;
            mode = gantryMode.MOVE;
        }
    }

    public void checkForPickUpTransition(double currentTime, Gantry otherGantry) {
        if (pickUpPlaceCountDown == 0) {
            System.out.println("pickupDone: id " + id + "   item: " + currentJob.getItem().getId());
            moveToX = currentJob.getPlace().getSlot().getCenterX();
            moveToY = currentJob.getPlace().getSlot().getCenterY();
            currentJob.pickedUp();
            slots.removeItemFromSlot(currentJob.getItem(), currentJob.getPickup().getSlot());
            printStatus(currentTime);
            boolean noProblem=isChangeOfStatusNotAProblem(currentTime,otherGantry);
            if(!noProblem){
                //probleem!!!
                boolean noProblemCurPos=isCurrentPositionAProblem(currentTime,otherGantry);
                if(!noProblemCurPos){
                    if(id==0){
                        moveToX=getMinimalX(otherGantry)-safetyGap;
                    }else {
                        moveToX=getMaximalX(otherGantry)+safetyGap;
                    }
                    mode = gantryMode.MOVE;
                    makeMoveTransition = false;
                    printStatus(currentTime);
                }else{
                    moveToX=currentX;
                    moveToY=currentY;
                    mode=gantryMode.WAIT;
                }
            }else{
                mode = gantryMode.MOVE;
                makeMoveTransition = false;
                printStatus(currentTime);
            }


        }
    }

    public void checkForPlaceTransition(double currentTime, Gantry otherGantry) {
        if (pickUpPlaceCountDown == 0) {
            System.out.println("placeDone: id " + id + "   item: " + currentJob.getItem().getId());
            slots.addItemToSlot(currentJob.getItem(), currentJob.getPlace().getSlot());
            currentJob.placed();
            printStatus(currentTime);
            currentJob = null;
            mode = gantryMode.IDLE;

            moveToX = currentX;
            moveToY = currentY;

        }
    }

    public void checkForIdleTransition(double currentTime, Gantry otherGantry) {
        //check eerste voorwaarde: geen job=Idle blijven
        if (mode == gantryMode.IDLE && currentJob != null) {

            System.out.println("idle: id " + id);
            boolean noProblem=isChangeOfStatusNotAProblem(currentTime,otherGantry);
            if(!noProblem){
                //probleem!!!
                boolean noProblemCurPos=isCurrentPositionAProblem(currentTime,otherGantry);
                if(!noProblemCurPos){
                    if(id==0){
                        moveToX=getMinimalX(otherGantry)-safetyGap;
                    }else {
                        moveToX=getMaximalX(otherGantry)+safetyGap;
                    }
                    mode = gantryMode.MOVE;
                    printStatus(currentTime);
                }else{
                    printStatus(currentTime);
                    moveToX=currentX;
                    moveToY=currentY;
                    mode=gantryMode.WAIT;

                }
            }else{
                if (currentX == currentJob.getPickup().getSlot().getCenterX() && currentY == currentJob.getPickup().getSlot().getCenterY()) {
                    pickUpPlaceCountDown = pickUpPlaceDuration + 1;
                    mode = gantryMode.PICKUP;
                    printStatus(currentTime);
                } else {
                    mode = gantryMode.MOVE;
                    makeMoveTransition = false;
                    printStatus(currentTime);
                }
            }

        }

    }

    private boolean isCurrentPositionAProblem(double currentTime, Gantry otherGantry) {
        if(id==0){
            return calculateOtherGantryBoundaryOnCurrentPosition(false,otherGantry);
        } else if(id==1){
            return calculateOtherGantryBoundaryOnCurrentPosition(true,otherGantry);
        }
        return false;
    }

    private boolean isChangeOfStatusNotAProblem(double currentTime, Gantry otherGantry) {
        if(id==0){
            return calculateOtherGantryBoundary(false,otherGantry);
        } else if(id==1){
            return calculateOtherGantryBoundary(true,otherGantry);
        }
        return false;
    }

    public void checkForWaitTransition(double currentTime, Gantry otherGantry) {

        if (currentJob == null) {
            mode = gantryMode.IDLE;
            moveToX = currentX;
            moveToY = currentY;


        }else {
            if(!currentJob.isPickedUp()){
                moveToX=currentJob.getPickup().getSlot().getCenterX();
                moveToY=currentJob.getPickup().getSlot().getCenterY();
            }else{
                moveToX=currentJob.getPlace().getSlot().getCenterX();
                moveToY=currentJob.getPlace().getSlot().getCenterY();
            }


            boolean noProblem=isChangeOfStatusNotAProblem(currentTime,otherGantry);
            if(!noProblem){
                //probleem!!!
                boolean noProblemCurPos=isCurrentPositionAProblem(currentTime,otherGantry);
                if(!noProblemCurPos){
                    if(id==0){
                        moveToX=getMinimalX(otherGantry)-safetyGap;
                    }else {
                        moveToX=getMaximalX(otherGantry)+safetyGap;
                    }
                    mode = gantryMode.MOVE;
                    makeMoveTransition = false;
                    printStatus(currentTime);
                }else{
                    moveToX=currentX;
                    moveToY=currentY;
                    mode=gantryMode.WAIT;
                }
            }else{
                mode = gantryMode.MOVE;
                makeMoveTransition = false;
                printStatus(currentTime);
            }

        }
    }

    private void posUpdate(int currentIndex, int interval, int moveTo, double speed) {
        ArrayList<Integer> current = new ArrayList<>();
        current.add(currentX);
        current.add(currentY);
        if (interval > speed) {
            if (currentIndex == 0) currentX += speed;
            else currentY += speed;
        } else if (interval > 0) {
            if (currentIndex == 0) currentX = moveTo;
            else currentY = moveTo;
        } else if (interval < (-1) * speed) {
            if (currentIndex == 0) currentX -= speed;
            else currentY -= speed;
        } else if (interval < 0) {
            if (currentIndex == 0) currentX = moveTo;
            else currentY = moveTo;
        }
    }

    public void printStatus(double totalTime) {
        StringBuilder stb = new StringBuilder();

        stb.append(id);
        stb.append(";");

        stb.append(totalTime);
        stb.append(";");

        stb.append(currentX);
        stb.append(";");

        stb.append(currentY);
        stb.append(";");

        if (currentJob == null || !currentJob.isPickedUp()) stb.append("null");
        else stb.append(currentJob.getItem().getId());
        stb.append(";");

        stb.append("\n");

        csvFileWriter.add(stb);

    }

    public Job getCurrentJob() {
        return currentJob;
    }

    public void setCurrentJob(Job currentJob) {
        this.currentJob = currentJob;
        moveToX = this.currentJob.getPickup().getSlot().getCenterX();
        moveToY = this.currentJob.getPickup().getSlot().getCenterY();
    }

    public void performTimeStep(double time, Gantry otherGantry) {
        if(mode == gantryMode.MOVE){
            moveCraneToNewPosition(time,otherGantry);
        }else if (mode == gantryMode.PICKUP) {
            pickUpPlaceCountDown--;
            if (pickUpPlaceCountDown == 0) {
                checkForPickUpTransition(time, otherGantry);
            }
        } else if (mode == gantryMode.PLACE) {
            pickUpPlaceCountDown--;
            if (pickUpPlaceCountDown == 0) {
                checkForPlaceTransition(time, otherGantry);
            }
        } else if (mode == gantryMode.IDLE) {
            checkForIdleTransition(time, otherGantry);
        }
    }

    private boolean calculateOtherGantryBoundary(boolean lessGreater, Gantry otherGantry) {
        int minX = getMinimalX(otherGantry);
        int maxX = getMaximalX(otherGantry);
        if (lessGreater) {
            if (moveToX >= maxX + safetyGap) return true;
        } else {
            if (moveToX <= minX - safetyGap) return true;
        }
        return false;
    }

    private boolean calculateOtherGantryBoundaryOnCurrentPosition(boolean lessGreater, Gantry otherGantry) {
        int minX = getMinimalX(otherGantry);
        int maxX = getMaximalX(otherGantry);
        if (lessGreater) {
            if (currentX >= maxX + safetyGap) return true;
        } else {
            if (currentX <= minX - safetyGap) return true;
        }
        return false;
    }



    private int getMinimalX(Gantry otherGantry) {
        return Math.min(otherGantry.currentX, otherGantry.moveToX);
    }

    private int getMaximalX(Gantry otherGantry) {
        return Math.max(otherGantry.currentX, otherGantry.moveToX);
    }

    public void forceUnlock( int globalTime, Gantry otherGantry) {
        if(id==0 && currentJob==null){
            moveToX=-15;
            moveToY=5;
            printStatus(globalTime);
        }else if(id==0){
            if(!currentJob.isPickedUp()){
                moveToX=currentJob.getPickup().getSlot().getCenterX();
                moveToY=currentJob.getPickup().getSlot().getCenterY();
            }else{
                moveToX=currentJob.getPlace().getSlot().getCenterX();
                moveToY=currentJob.getPlace().getSlot().getCenterY();
            }
            printStatus(globalTime);
            mode=gantryMode.MOVE;
            otherGantry.moveToX=moveToX+safetyGap;
            otherGantry.printStatus(globalTime);
            otherGantry.mode=gantryMode.MOVE;
        }
    }

    public enum gantryMode {
        PICKUP,
        PLACE,
        WAIT,
        MOVE,
        IDLE
    }

    public void setMakeMoveTransition(Boolean makeMoveTransition) {
        this.makeMoveTransition = makeMoveTransition;
    }

    public String printStatusOne(double totalTime){
        StringBuilder stb = new StringBuilder();

        stb.append(id);
        stb.append(";");

        stb.append(totalTime);
        stb.append(";");

        stb.append(currentX);
        stb.append(";");

        stb.append(currentY);
        stb.append(";");

        return stb.toString();

    }
}
