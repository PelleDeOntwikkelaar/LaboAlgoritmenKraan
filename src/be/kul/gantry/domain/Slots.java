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

                if(!shifted) x = ((slot.getCenterX() - 5) / 10) + slot.getZ() * xDimension;
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

    private int findShiftedX(Slot slot){
        int z=slot.getZ();
        int offset=0;
        for(int i=1;i<=z;i++){
            offset+=xDimension-(i-1);
        }
        int base=(slot.getCenterX() - 5*(z+1)) / 10;

        return base+offset;
    }

    /**
     * Find slot to store item, with logic.
     *
     * @param currentX
     * @param currentY
     * @param xSpeed
     * @param ySpeed
     * @return
     */
    public Slot findBestSlot(int currentX, int currentY, double xSpeed, double ySpeed, int forbiddenX, int forbiddenY) {
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
