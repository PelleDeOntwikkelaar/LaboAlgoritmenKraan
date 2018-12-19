package be.kul.gantry.domain;

import java.util.*;

public class Slots {
    //variables with the purpose of storing slots
    private Slot inputSlot;
    private Slot outputSlot;
    private ArrayList<ArrayList<Slot>> slotArrayYDimension;

    //List of items currently in storage
    private ArrayList<Item> itemsInStorage;

    //properties of the problem
    private int yDimension;
    private int xDimension;
    private int maxX;
    private int maxLevels;
    private boolean shifted;

    /**
     * De default constructor voor de klasse Slots
     *
     * @param maxX      Een integer waarde die de maximale X dimensie bevat
     * @param maxY      Een intiger waarde die de maximale Y dimensie bevat
     * @param maxLevels Een integet waarde die het maximaal aantal levels dat er gestapeld mag worden bevat.
     * @param shifted   Een boolean waarde die erop wijst of er shifted gestapeld kan worden.
     */
    public Slots(int maxX, int maxY, int maxLevels, boolean shifted) {

        this.maxX = maxX;
        this.yDimension = maxY / 10;
        this.xDimension = (maxX - 20) / 10;
        this.shifted = shifted;
        this.maxLevels = maxLevels;
        this.slotArrayYDimension = new ArrayList<>();
        this.itemsInStorage = new ArrayList<>();
        generateYList();
    }

    /**
     * Void methode die ervoor zorgt dat in de Y dimensies de juiste rijen gedeclareerd worden.
     */
    private void generateYList() {
        for (int i = 0; i < yDimension; i++) {
            slotArrayYDimension.add(new ArrayList<Slot>());
        }
    }

    /**
     * Methode die de slots op een goede manier gaat aanmaken en alle parameters zet.
     *
     * @param slots Een lijst van Slots die gegeven worden in de probleemstelling.
     */
    public void addSlots(List<Slot> slots) {
        for (Slot slot : slots) {
            if (slot.isStorageSlot()) {
                int y = (slot.getCenterY() - 5) / 10;
                int x;

                if (!shifted) x = ((slot.getCenterX() - 5) / 10) + slot.getZ() * xDimension;
                else x = findShiftedX(slot);

                slotArrayYDimension.get(y).add(x, slot);


                if (slot.getItem() != null) {

                    slot.getItem().setSlotID(slot);
                    itemsInStorage.add(slot.getItem());
                }

            } else if (slot.isInputSlot()) {
                inputSlot = slot;
            } else {
                outputSlot = slot;
            }
        }
    }

    private int findShiftedX(Slot slot) {
        int z = slot.getZ();
        int offset = 0;
        for (int i = 1; i <= z; i++) {
            offset += xDimension - (i - 1);
        }
        int base = (slot.getCenterX() - 5 * (z + 1)) / 10;

        return base + offset;
    }

    /**
     * Find slot to store item, with logic.
     *
     * @param currentX Current X position of the gantry that will perform the job, type: Integer.
     * @param currentY Current Y position of the gantry that will perform the job, type: Integer.
     * @param xSpeed
     * @param ySpeed
     * @return
     */
    public Slot findBestSlot(int currentX, int currentY, double xSpeed, double ySpeed, Set<Slot> forbiddenSlots) {
        //while loop is necessary, Y dimension can be full.
        int yArray = (currentY - 5) / 10;
        while (true) {
            for (Slot slot : slotArrayYDimension.get(yArray)) {
                //when an item is moved out of relocation purposes, the slot above may never be the destination slot
                if (slot.getItem() == null) {
                    if ((forbiddenSlots == null) || (forbiddenSlots != null && !forbiddenSlots.contains(slot))) {
                        return slot;
                    }

                }
            }
            if (yArray == yDimension - 1) yArray = 0;
            else yArray++;
        }
    }

    /**
     * Method to find best placement slot for first Gantry when working with multiple gantries.
     *
     * @param job
     * @param gantries
     * @param forbiddenSlots
     * @return
     */
    public Slot findBestSlotFirstGantry(Job job, List<Gantry> gantries, Set<Slot> forbiddenSlots, boolean precedingJob, int refX) {
        int minX;
        if (precedingJob) minX = refX;
        else minX = findMinX(gantries.get(1));
        Slot bestSlot = tryFindBestSlotLeftRightToX(minX - 20, gantries.get(0), forbiddenSlots, true);
        if (bestSlot == null) {
            bestSlot = tryFindBestSlotLeftRightToX(outputSlot.getCenterX(), gantries.get(0), forbiddenSlots, true);
        }
        return bestSlot;
    }

    /**
     * Method to find best placement slot for second Gantry when working with multiple gantries.
     *
     * @param job
     * @param gantries
     * @param forbiddenSlots
     * @return
     */
    public Slot findBestSlotSecondGantry(Job job, List<Gantry> gantries, Set<Slot> forbiddenSlots, boolean precedingJob, int refX) {
        int maxX;
        if (precedingJob) maxX = refX;
        else maxX = findMaxX(gantries.get(0));
        Slot bestSlot = tryFindBestSlotLeftRightToX(maxX + 20, gantries.get(0), forbiddenSlots, false);
        if (bestSlot == null) {
            bestSlot = tryFindBestSlotLeftRightToX(outputSlot.getCenterX(), gantries.get(0), forbiddenSlots, true);
        }
        return bestSlot;
    }

    public Slot tryFindBestSlotLeftRightToX(int x, Gantry gantry, Set<Slot> forbiddenSlots, boolean leftRight) {
        int yArray = (gantry.getCurrentY() - 5) / 10;
        int arrayCounter = 0;
        while (arrayCounter != yDimension) {
            for (Slot slot : slotArrayYDimension.get(yArray)) {
                //when an item is moved out of relocation purposes, the slot above may never be the destination slot
                if (slot.getItem() == null) {
                    //forbidden slots== null -> never a problem
                    //forbiddenslots!=null -> check if it doesn't contain slot.
                    if ((forbiddenSlots == null) || (forbiddenSlots != null && !forbiddenSlots.contains(slot))) {
                        if (leftRight && slot.getCenterX() < x) return slot;
                        else if (!leftRight && slot.getCenterX() > x) return slot;
                    }

                }
            }
            if (yArray == yDimension - 1) yArray = 0;
            else yArray++;
            arrayCounter++;
        }

        return null;
    }

    private int findMinX(Gantry gantry) {
        if (gantry.getCurrentJob() != null && gantry.getCurrentJob().getPlace() != null)
            return Math.min(gantry.getCurrentX(), gantry.getCurrentJob().getPlace().getSlot().getCenterX());
        else return gantry.getCurrentX();
    }

    private int findMaxX(Gantry gantry) {
        if (gantry.getCurrentJob() != null && gantry.getCurrentJob().getPlace() != null)
            return Math.max(gantry.getCurrentX(), gantry.getCurrentJob().getPlace().getSlot().getCenterX());
        else return gantry.getCurrentX();
    }

    public int findSuitableGantry(Job job, List<Gantry> gantries, int refX, int refY) {
        if (gantries.size() == 1 || job.getPickup().getSlot().getCenterX() < refX) return 0;
        if (job.getPickup().getSlot().getCenterX() > refX) return 1;
        if (job.getPickup().getSlot().getCenterX() == refX) {
            if (refX <= maxX / 2) return 0;
            else return 1;
        }
        //default
        return 0;
    }

    /**
     * Checks if item is in storage.
     *
     * @param item to check.
     * @return True or false whether item is in storage.
     */
    public Boolean containsItem(Item item) {
        if (!itemsInStorage.contains(item)) return false;
        return true;
    }

    public Slot findSlotByItem(Item item) {
        if (!itemsInStorage.contains(item)) {
            return null;
        } else {
            int index = itemsInStorage.indexOf(item);
            return itemsInStorage.get(index).getSlotID();
        }
    }

    /**
     * Fully set an item for a slot
     *
     * @param item
     * @param slot
     */
    public void addItemToSlot(Item item, Slot slot) {
        //add item from slot
        slot.setItem(item);
        //add slot to item
        item.setSlotID(slot);
        //add item to storage collection
        itemsInStorage.add(item);
    }


    /**
     * Fully removes an item from a slot
     *
     * @param item
     * @param slot
     */
    public void removeItemFromSlot(Item item, Slot slot) {
        slot.setItem(null);
        item.setSlotID(null);
        itemsInStorage.remove(item);

    }

    public Set<Slot> findForbiddenSlots(Slot slotToSolvePickUp) {
        Set<Slot> forbiddenSlots;

        List<Slot> slots = getStackedSlots(slotToSolvePickUp);
        forbiddenSlots = new HashSet<>();
        forbiddenSlots.addAll(slots);


        return forbiddenSlots;
    }

    /**
     * Checks all slots above the current slot for items
     *
     * @param slot
     * @return A list containing all slots where an items needs to be moved first
     */
    public List<Slot> getStackedItemSlots(Slot slot) {

        LinkedList<Slot> list = (LinkedList<Slot>) getStackedSlots(slot);

        HashSet<Slot> toRemove = new HashSet<>();

        for (Slot slt : list) {
            if (slt.getItem() == null) toRemove.add(slt);
        }

        for (Slot slt : toRemove) {
            list.remove(slt);
        }

        return list;
    }

    /**
     * @param slot
     * @return List containing all slots with an item stacked above the given slot
     */
    public List<Slot> getStackedSlots(Slot slot) {
        // the list we'll return
        LinkedList<Slot> list = new LinkedList<>();

        // amount of containers on the lowest level on a row
        int baseSize;
        // row of the slot
        int row = slot.getYMin() / 10;

        if (!shifted) {
            baseSize = slotArrayYDimension.get(0).size() / maxLevels;

            for (int i = slot.getZ() + 1; i < maxLevels; i++) {
                // checking all slots above current slot for items
                Slot slt = slotArrayYDimension.get(row).get((slot.getXMin() / 10) + (i * baseSize));
                list.add(slt);
            }
            return list;

        } else {

            int sizeCorrection = 0;
            for (int i = 0; i < maxLevels; i++) {
                sizeCorrection += i;
            }

            baseSize = (slotArrayYDimension.get(0).size() + sizeCorrection) / maxLevels;

            int z = slot.getZ() + 1;

            int minus = 0;

            for (int i = z; i < maxLevels; i++) {
                // checking all slots above current slot for items
                System.out.println("checking i: " + i);

                minus += baseSize - i;

                for (int j = 0; j < i - z + 2; j++) {

                    int index = findShiftedX(slot);

                    // prevents indexOutOfBoundsExceptions
                    if (index + minus + j >= slotArrayYDimension.get(row).size()) break;

                    Slot slt = slotArrayYDimension.get(row).get(index + minus + j);

                    if (checkShiftedLevel(findShiftedX(slt), baseSize) == i) {
                        list.add(slt);
                        System.out.println(index + minus + j);
                    }
                }
            }
            return list;
        }
    }

    /**
     * Checks the z of a certain index
     *
     * @param index
     * @param baseLevel
     * @return level of the slot with given index
     */
    public int checkShiftedLevel(int index, int baseLevel) {
        int level = 0;

        while (index > 0) {
            index -= baseLevel;
            if (index >= 0) {
                baseLevel--;
                level++;
            }
        }
        return level;
    }

}
