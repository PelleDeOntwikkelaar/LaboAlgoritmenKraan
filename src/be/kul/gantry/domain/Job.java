package be.kul.gantry.domain;

import be.kul.gantry.Extra.CSVFileWriter;

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

    /*
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
    */

    @Override
    public String toString() {
        return String.format("J%d move %d from %s to %s in %f",id,item.getId(),pickup.slot,place.slot,time);
    }

    public void performTask(Gantry gantry, Task task) {
        task.calculateTime(gantry);
        gantry.moveCrane(task.slot.getCenterX(), task.slot.getCenterY());
    }


    public void printStatus(Gantry gantry, CSVFileWriter csvFileWriter, double totalTime,TaskType type) {
        StringBuilder stb = new StringBuilder();
        stb.append(gantry.printStatus(totalTime));
        if (type == TaskType.PICKUP) stb.append(item.getId());
        else stb.append("null");
        stb.append(";");
        stb.append("\n");
        csvFileWriter.add(stb);

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
        /*
        public StringBuilder getOutput(double totalTime) {
            StringBuilder stb = new StringBuilder();

            if (type == TaskType.PICKUP) stb.append(parentJob.getItem().getId());
            else stb.append("null");
            stb.append(";");

            return stb;
        }
        */

        public void calculateTime(Gantry gantry) {

            int xDistance;
            int yDistance;

            if (type == TaskType.PICKUP){
                xDistance = Math.abs(slot.getCenterX() - gantry.getCurrentX());
                yDistance = Math.abs(slot.getCenterY() - gantry.getCurrentY());
            } else {
                xDistance = Math.abs(parentJob.pickup.slot.getCenterX() - gantry.getCurrentX());
                yDistance = Math.abs(parentJob.pickup.slot.getCenterY() - gantry.getCurrentY());
            }


            double xTime = xDistance/gantry.getXSpeed();
            double yTime = yDistance/gantry.getYSpeed();

            time= Math.max(xTime,yTime);

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
