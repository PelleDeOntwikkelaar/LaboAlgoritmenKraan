package be.kul.gantry.domain;

import java.util.ArrayList;

/**
 * Created by Wim on 27/04/2015.
 */
public class Gantry {

    private final int id;
    private final int xMin, xMax;
    private final int startX, startY;
    private final double xSpeed;
    private final double ySpeed;

    private int currentX;
    private int currentY;

    private int moveToX;
    private int moveToY;

    private Job currentJob;
    private gantryMode mode;
    private int pickUpPlaceCountDown;
    private int pickUpPlaceDuration;



    public Gantry(int id,
                  int xMin, int xMax,
                  int startX, int startY,
                  double xSpeed, double ySpeed) {
        this.id = id;
        this.xMin = xMin;
        this.xMax = xMax;
        this.startX = startX;
        this.startY = startY;
        this.xSpeed = xSpeed;
        this.ySpeed = ySpeed;

        this.currentX = startX;
        this.currentY = startY;
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
    public void moveCraneToNewPosition(double currentTime){
        int xInt=moveToX-currentX;
        int yInt=moveToY-currentY;

        posUpdate(0,xInt,moveToX,xSpeed);
        posUpdate(1,yInt,moveToY,ySpeed);

        checkForMoveTransition(currentTime);

    }

    private void checkForMoveTransition(double currentTime) {

        if(currentJob.getPickup().getSlot().getCenterX()==currentX&&currentJob.getPickup().getSlot().getCenterY()==currentY){
            mode=gantryMode.PICKUP;
            printStatus(currentTime);
            pickUpPlaceCountDown=pickUpPlaceDuration;
        }else if(currentJob.getPlace().getSlot().getCenterX()==currentX&&currentJob.getPlace().getSlot().getCenterY()==currentY){
            mode=gantryMode.PLACE;
            printStatus(currentTime);
            pickUpPlaceCountDown=pickUpPlaceDuration;
        }
    }

    public void checkForPickUpTransition(double currentTime){
        if (pickUpPlaceCountDown==0){
            moveToX=currentJob.getPlace().getSlot().getCenterX();
            moveToY=currentJob.getPlace().getSlot().getCenterY();
            mode=gantryMode.MOVE;
            //todo: item moet verwijderd worden uit slot
            printStatus(currentTime);
        }
    }

    public void checkForPlaceTransition(double currentTime){
        if (pickUpPlaceCountDown==0){
            mode=gantryMode.IDLE;
            //todo: item moet verwijderd worden uit slot
            printStatus(currentTime);
        }
    }

    public void checkForIdleTransition(double currentTime){
        if(currentX!=moveToX &&currentY!=moveToY){
            mode=gantryMode.MOVE;
            printStatus(currentTime);
        }
    }

    public void decreasePickupPlaceCountDown(){
        pickUpPlaceCountDown--;
    }

    private void posUpdate(int currentIndex, int interval, int moveTo, double speed){
        ArrayList<Integer> current=new ArrayList<>();
        current.add(currentX);
        current.add(currentY);
        if(interval>speed){
            if(currentIndex==0)currentX+=speed;
            else currentY+=speed;
        }else if(interval>0){
            if(currentIndex==0)currentX=moveTo;
            else currentY=moveTo;
        }else if(interval<(-1)*speed){
            if(currentIndex==0)currentX-=speed;
            else currentY-=speed;
        }else if(interval<0){
            if(currentIndex==0)currentX=moveTo;
            else currentY=moveTo;
        }
    }

    public String printStatus(double totalTime) {
        StringBuilder stb = new StringBuilder();

        stb.append(id);
        stb.append(";");

        stb.append(totalTime);
        stb.append(";");

        stb.append(currentX);
        stb.append(";");

        stb.append(currentY);
        stb.append(";");

        // dit moet volgens mij nog aangepast worden
        if (currentJob.getItem() == null) stb.append("null");
        else stb.append(currentJob.getItem().getId());
        stb.append(";");

        stb.append("\n");

        return stb.toString();

    }

    public Job getCurrentJob() {
        return currentJob;
    }

    public void setCurrentJob(Job currentJob) {
        this.currentJob = currentJob;
    }

    public enum gantryMode {
        PICKUP,
        PLACE,
        WAIT,
        MOVE,
        IDLE
    }
}
