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

        if (id == 1 && calculateOtherGantryBoundary(true, otherGantry)) {
            mode = gantryMode.WAIT;
            printStatus(currentTime);
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

    }

    private void checkForMoveTransition(double currentTime, Gantry otherGantry) {
        if (currentJob == null) {
            if (currentX == moveToX && currentY == moveToY) {
                mode = gantryMode.IDLE;
                printStatus(currentTime);
            }
        } else {
            if (currentJob.getPickup().getSlot().getCenterX() == currentX && currentJob.getPickup().getSlot().getCenterY() == currentY) {
                mode = gantryMode.PICKUP;
                printStatus(currentTime);
                pickUpPlaceCountDown = pickUpPlaceDuration;
            } else if (currentJob.getPlace().getSlot().getCenterX() == currentX && currentJob.getPlace().getSlot().getCenterY() == currentY) {
                mode = gantryMode.PLACE;
                printStatus(currentTime);
                pickUpPlaceCountDown = pickUpPlaceDuration;
            } else {
                if (!currentJob.isPickedUp()) {
                    moveToX = currentJob.getPickup().getSlot().getCenterX();
                    moveToY = currentJob.getPickup().getSlot().getCenterY();
                } else {
                    moveToX = currentJob.getPlace().getSlot().getCenterX();
                    moveToY = currentJob.getPlace().getSlot().getCenterY();
                }
                printStatus(currentTime);
                if ((id == 1 && calculateOtherGantryBoundary(true, otherGantry)) || id == 0) {
                    mode = gantryMode.MOVE;
                    makeMoveTransition = false;
                    printStatus(currentTime);
                } else {
                    checkForWaitOrMove(currentTime, otherGantry);
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
            System.out.println("pickupDone: id " + id);
            moveToX = currentJob.getPlace().getSlot().getCenterX();
            moveToY = currentJob.getPlace().getSlot().getCenterY();
            currentJob.pickedUp();
            slots.removeItemFromSlot(currentJob.getItem(), currentJob.getPickup().getSlot());
            printStatus(currentTime);
            if ((id == 1 && calculateOtherGantryBoundary(true, otherGantry)) || id == 0) {
                mode = gantryMode.MOVE;
                makeMoveTransition = false;
                printStatus(currentTime);
            } else {
                checkForWaitOrMove(currentTime,otherGantry);
                //mode = gantryMode.WAIT;
            }


        }
    }

    public void checkForPlaceTransition(double currentTime, Gantry otherGantry) {
        if (pickUpPlaceCountDown == 0) {
            System.out.println("placeDone: id " + id);
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
        if (mode == gantryMode.IDLE && currentJob != null) {
            System.out.println("idle: id " + id);
            if (currentX == moveToX && currentY == moveToY) {
                pickUpPlaceCountDown = pickUpPlaceDuration;
                mode = gantryMode.PICKUP;
                printStatus(currentTime);
            } else {
                if ((id == 1 && calculateOtherGantryBoundary(true, otherGantry)) || id == 0) {
                    mode = gantryMode.MOVE;
                    makeMoveTransition = false;
                    printStatus(currentTime);
                }
            }
        }

    }

    private void checkForWaitTransition(double currentTime, Gantry otherGantry) {
        if (currentJob == null) {
            mode = gantryMode.IDLE;

            moveToX = currentX;
            moveToY = currentY;


        } else if (id == 1 && calculateOtherGantryBoundary(true, otherGantry)) {
            mode = gantryMode.MOVE;
            makeMoveTransition = false;
            printStatus(currentTime);
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

    public void performTimeStep(double time, List<Gantry> gantries) {
        Gantry otherGantry = null;
        for (Gantry gantry : gantries) {
            if (gantry != this) {
                otherGantry = gantry;
            }
        }
        if (mode == gantryMode.WAIT) {
            checkForWaitTransition(time, otherGantry);
        } else if (mode == gantryMode.MOVE) {
            moveCraneToNewPosition(time, otherGantry);
        } else if (mode == gantryMode.PICKUP) {
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

    private int getMinimalX(Gantry otherGantry) {
        return Math.min(otherGantry.currentX, otherGantry.moveToX);
    }

    private int getMaximalX(Gantry otherGantry) {
        return Math.max(otherGantry.currentX, otherGantry.moveToX);
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
}
