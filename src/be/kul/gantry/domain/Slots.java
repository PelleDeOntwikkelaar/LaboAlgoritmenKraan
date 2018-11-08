package be.kul.gantry.domain;

import java.util.*;

public class Slots {
    //variables with the purpose of storing slots
    private Slot inputSlot;
    private Slot outputSlot;
    private ArrayList<ArrayList<Slot>> slotArrayYDimension;

    //Set of available Slots divided in zones.
    private ArrayList<Set<Slot>> available;

    //Set of items
    private List<Item> items;

    //List of items currently in storage
    private ArrayList<Item> itemsInStorage;

    //properties of the problem
    private int yDimension;
    private int xDimension;
    private int maxLevels;
    private boolean shifted;

    /**
     * De default constructor voor de klasse Slots
     *
     * @param items     Een lijst met alle items in de probleemstelling
     * @param maxX      Een integer waarde die de maximale X dimensie bevat
     * @param maxY      Een intiger waarde die de maximale Y dimensie bevat
     * @param maxLevels Een integet waarde die het maximaal aantal levels dat er gestapeld mag worden bevat.
     * @param shifted   Een boolean waarde die erop wijst of er shifted gestapeld kan worden.
     */
    public Slots(List<Item> items, int maxX, int maxY, int maxLevels, boolean shifted) {

        this.items = items;
        this.yDimension = maxY / 10;
        this.xDimension = (maxX - 20) / 10;
        this.shifted = shifted;
        this.maxLevels = maxLevels;
        this.slotArrayYDimension = new ArrayList<>();
        this.available = new ArrayList<Set<Slot>>();
        for (int i = 0; i < 4; i++) {
            available.add(new HashSet<Slot>());
        }
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
        //werkt enkel voor niet shifted
        /*
            wanneer we te maken hebben met een opslag slot, moet deze op de juiste manier worden aangemaakt en ingpast
            worden.
            */
        for (Slot slot : slots) {
            if (slot.isStorageSlot()) {
                int y = (slot.getCenterY() - 5) / 10;
                int x = ((slot.getCenterX() - 5) / 10) + slot.getZ() * xDimension;
                slotArrayYDimension.get(y).add(x, slot);

                /*
                De volgende logica is bedoeld om de juiste slots "beschikbaar" te maken.
                Zo zijn slots beschikbaar waneer ze geen item bevatten en er onder hun ofwel grond is,
                 ofwel een bezet slot.
                 */
                if (slot.getItem() == null) {
                    if (slot.getZ() == 0 ||
                            findSlot(slot.getCenterX(), slot.getCenterY(), slot.getZ() - 1).getItem() != null) {
                        addToAvailable(slot);
                    }
                } else {
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

    /**
     * Find slot to store item, with logic.
     *
     * @param zone
     * @param currentX
     * @param currentY
     * @param xSpeed
     * @param ySpeed
     * @return
     */
    public Slot findBestSlot(int zone, int currentX, int currentY, double xSpeed, double ySpeed, int forbiddenX, int forbiddenY) {
        //while loop is necessary, Y dimension can be full.
        int yArray = (currentY-5)/10;
        while(true){
            for (Slot slot : slotArrayYDimension.get(yArray)) {
                //when an item is moved out of relocation purposes, the slot above may never be the destination slot
                if (slot.getItem() == null && slot.getCenterX() != forbiddenX && slot.getCenterY() != forbiddenY){
                    return slot;
                }
            }
            if(yArray == yDimension-1) yArray = 0;
            else yArray++;
        }


        /*Slot destinationSlot = null;
        double timeNeeded = Math.max(xDimension * xSpeed, yDimension * ySpeed);

        for (Slot slot : available.get(zone)) {
            int x = Math.abs(currentX - slot.getCenterX());
            int y = Math.abs(currentY - slot.getCenterY());
            double time = Math.max(x * xSpeed, y * ySpeed);
            if (time < timeNeeded) {
                timeNeeded = time;
                destinationSlot = slot;
            }

        }
        return destinationSlot;*/
    }

    /**
     * Method to calculate the perfect drop off zone for a given item.
     *
     * @param item
     * @param outputQueue
     * @return
     */
    public int calculateZone(Item item, Queue<Job> outputQueue) {
        int i = 0;
        int zone = 0;
        for (Job job : outputQueue) {
            if (job.getItem().getId() == item.getId()) {
                if (i < 10) zone = 3;
                else if (i < 20) zone = 2;
                else if (i < 30) zone = 1;
                else zone = 0;
            }
            i++;
        }
        return checkIfZoneIsPossible(zone);
    }

    /**
     * This method checks if the chosen zone is a viable option.
     * Meaning if there are available slots in the given zone.
     *
     * @param zone ZoneNumber of type integer
     * @return Zone number of type integer.
     */
    private int checkIfZoneIsPossible(int zone) {
        if (available.get(zone).isEmpty()) {
            if (zone == 3) {
                zone = 0;
            } else {
                zone++;
            }
            zone = checkIfZoneIsPossible(zone);
        }
        return zone;
    }

    public Slot calculateDropOffSlot(List<Slot> forbiddenSlots) {
        //todo: implement method
        if (forbiddenSlots != null) {

        }
        return null;
    }

    /**
     * find slot at given location
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public Slot findSlot(int x, int y, int z) {

        y = (y - 5) / 10;
        x = ((x - 5) / 10) + z * xDimension;

        return slotArrayYDimension.get(y).get(x);
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
        //remove slot from available for input list
        removeFromAvailable(slot);
        //if possible add the slot above to available for input list
        int xIndex = ((slot.getCenterX() - 5) / 10) + (slot.getZ() + 1) * xDimension;
        if (xDimension * maxLevels > xIndex) {
            int yIndex = (slot.getCenterY() - 5) / 10;
            addToAvailable(slotArrayYDimension.get(yIndex).get(xIndex));
        }
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
        addToAvailable(slot);
        //if possible remove the slot above from available for input list
        int Xindex = ((slot.getCenterX() - 5) / 10) + (slot.getZ() - 1) * xDimension;
        if (0 <= Xindex) {
            int Yindex = (slot.getCenterY() - 5) / 10;
            removeFromAvailable(slotArrayYDimension.get(Yindex).get(Xindex));
        }
    }

    /**
     * Method to add a slot to a correct Available Set.
     *
     * @param slot
     */
    private void addToAvailable(Slot slot) {
        int x = ((slot.getCenterX() - 5) / 10) + slot.getZ() * xDimension;
        if (x < xDimension / 4) available.get(0).add(slot);
        else if (x < xDimension / 2) available.get(1).add(slot);
        else if (x < xDimension * 3 / 4) available.get(2).add(slot);
        else available.get(3).add(slot);
    }

    /**
     * Method to remove a given slot from a correct Available Set.
     *
     * @param slot
     */
    private void removeFromAvailable(Slot slot) {
        int x = ((slot.getCenterX() - 5) / 10) + slot.getZ() * xDimension;
        if (x < xDimension / 4) available.get(0).remove(slot);
        else if (x < xDimension / 2) available.get(1).remove(slot);
        else if (x < xDimension * 3 / 4) available.get(2).remove(slot);
        else available.get(3).remove(slot);

    }

    /**
     * Checks all slots above the current slot for items
     *
     * @param slot
     * @return A list containing all slots where an items needs to be moved first
     */
    public List<Slot> getStackedItemSlots(Slot slot) {
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
                if (slt.getItem() != null) list.add(slt);
                else return list;
            }
            return list;

        } else {
            //todo shifted rows
            return null;
        }

    }

}
