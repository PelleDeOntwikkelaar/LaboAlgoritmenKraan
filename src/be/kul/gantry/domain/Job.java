package be.kul.gantry.domain;

/**
 * Created by Wim on 12/05/2015.
 */
public class Job {

    private final int id;

    private final Task pickup;
    private final Task place;

    private final Item item;

    private double time;


    public Job(int id, Item c, Slot from, Slot to) {
        this.id = id;
        this.item = c;
        this.pickup = new Task(id*2,TaskType.PICKUP);
        this.place = new Task(id*2+1,TaskType.PLACE);
        this.pickup.slot = from;
        this.place.slot = to;
    }

    public int getId() {
        return id;
    }

    public Task getPickup() {
        return pickup;
    }

    public Task getPlace() {
        return place;
    }

    public Item getItem() {
        return item;
    }

    public double getTime() {
        return time;
    }

    public void calculateTime(Gantry gantry){
        pickup.calculateTime(gantry);
        place.calculateTime(gantry);
        time = pickup.getTime() + place.getTime();

        System.out.println("pickup: " + pickup.getTime() + ", place: " + place.getTime() + ", time: " + time);
    }

    public StringBuilder getOutput(Gantry gantry, double totalTime) {
        StringBuilder stb = new StringBuilder();

        stb.append(pickup.getOutput(gantry, totalTime));
        stb.append("\n");
        stb.append(place.getOutput(gantry, totalTime + pickup.getTime()));
        stb.append("\n");

        return stb;
    }

    @Override
    public String toString() {
        return String.format("J%d move %d from %s to %s in %f",id,item.getId(),pickup.slot,place.slot,time);
    }

    public class Task {
        private final int id;
        private Slot slot;
        private Job parentJob;
        private TaskType type;
        private double time;

        public Task(int id, TaskType taskType) {
            this.id = id;
            this.type = taskType;
            this.parentJob = Job.this;
        }

        public int getId() {
            return id;
        }

        public Slot getSlot() {
            return slot;
        }

        public void setSlot(Slot slot) {
            this.slot = slot;
        }

        public Job getParentJob() {
            return parentJob;
        }

        public TaskType getType() {
            return type;
        }

        public double getTime() {
            return time;
        }

        public StringBuilder getOutput(Gantry gantry, double totalTime) {
            StringBuilder stb = new StringBuilder();

            stb.append(gantry.getId());
            stb.append(";");

            stb.append(time + totalTime);
            stb.append(";");

            gantry.setCurrentX(slot.getCenterX());
            stb.append(slot.getCenterX());
            stb.append(";");

            gantry.setCurrentY(slot.getCenterY());
            stb.append(slot.getCenterY());
            stb.append(";");

            if (type == TaskType.PICKUP) stb.append(parentJob.getItem().getId());
            else stb.append("null");
            stb.append(";");

            return stb;
        }

        public void calculateTime(Gantry gantry) {
            int xDistance = Math.abs(slot.getCenterX() - gantry.getCurrentX());
            double xTime = xDistance/gantry.getXSpeed();
            int yDistance = Math.abs(slot.getCenterY() - gantry.getCurrentY());
            double yTime = yDistance/gantry.getYSpeed();

            if (xTime >= yTime) time = xTime;
            else time = yTime;

            System.out.println("x: " + xTime + ", y: " + yTime + ", time: " + time);
        }

        @Override
        public String toString() {
            if(type == TaskType.PICKUP) {
                return String.format("Pickup %d from %s",Job.this.item.getId(),slot);
            } else {
                return String.format("Place %d at %s",Job.this.item.getId(),slot);
            }
        }
    }

    public static enum TaskType {
        PICKUP,
        PLACE
    }

}
