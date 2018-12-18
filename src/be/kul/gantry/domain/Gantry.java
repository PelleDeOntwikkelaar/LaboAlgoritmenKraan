package be.kul.gantry.domain;

/**
 * Created by Wim on 27/04/2015.
 */
public class Gantry {

    private final int id;
    private final int xMin, xMax;
    private final int startX, startY;
    private final double xSpeed, ySpeed;

    private int currentX;
    private int currentY;

    private Job currentJob;

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
}
