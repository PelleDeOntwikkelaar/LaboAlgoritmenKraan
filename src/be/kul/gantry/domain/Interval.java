package be.kul.gantry.domain;

public class Interval {
    int minx,maxX;

    public Interval(int minx, int maxX) {
        this.minx = minx;
        this.maxX = maxX;
    }

    public int getMinx() {
        return minx;
    }

    public void setMinx(int minx) {
        this.minx = minx;
    }

    public int getMaxX() {
        return maxX;
    }

    public void setMaxX(int maxX) {
        this.maxX = maxX;
    }
}
