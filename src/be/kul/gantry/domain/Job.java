package be.kul.gantry.domain;

import java.util.Set;

/**
 * Created by Wim on 12/05/2015.
 */
public class Job {

    private final int id;

    private final Task pickup;
    private final Task place;
    private boolean pickedUp;

    private final Item item;


    private Set<Slot> forbiddenSlots;

    public Job(int id, Item c, Slot from, Slot to) {
        this.id = id;
        this.item = c;
        this.pickup = new Task(id * 2, TaskType.PICKUP);
        this.place = new Task(id * 2 + 1, TaskType.PLACE);
        this.pickup.slot = from;
        this.place.slot = to;
        pickedUp = false;
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

    public boolean isPickedUp() {
        return pickedUp;
    }

    public void pickedUp() {
        pickedUp = true;
    }

    public void placed() {
        pickedUp = false;
    }

    public Set<Slot> getForbiddenSlots() {
        return forbiddenSlots;
    }

    public void setForbiddenSlots(Set<Slot> forbiddenSlots) {
        this.forbiddenSlots = forbiddenSlots;
    }

    @Override
    public String toString() {
        return String.format("J%d move %d from %s to %s in %f", id, item.getId(), pickup.slot, place.slot);
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

        public void calculateTime(Gantry gantry) {

            int xDistance;
            int yDistance;

            if (type == TaskType.PICKUP) {
                xDistance = Math.abs(slot.getCenterX() - gantry.getCurrentX());
                yDistance = Math.abs(slot.getCenterY() - gantry.getCurrentY());
            } else {
                xDistance = Math.abs(parentJob.place.slot.getCenterX() - gantry.getCurrentX());
                yDistance = Math.abs(parentJob.place.slot.getCenterY() - gantry.getCurrentY());
            }


            double xTime = xDistance / gantry.getXSpeed();
            double yTime = yDistance / gantry.getYSpeed();

            time = Math.max(xTime, yTime);

            // System.out.println("x: " + xTime + ", y: " + yTime + ", startingTime: " + startingTime);
        }

        @Override
        public String toString() {
            if (type == TaskType.PICKUP) {
                return String.format("Pickup %d from %s", Job.this.item.getId(), slot);
            } else {
                return String.format("Place %d at %s", Job.this.item.getId(), slot);
            }
        }
    }

    public enum TaskType {
        PICKUP,
        PLACE
    }

}
