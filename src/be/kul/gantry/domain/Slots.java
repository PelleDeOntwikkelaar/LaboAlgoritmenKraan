package be.kul.gantry.domain;

import java.util.*;

public class Slots {
    private Slot inputSlot;
    private Slot outputSlot;
    private ArrayList<ArrayList<Slot>> slotArrayYDimension;
    private Set<Slot> available;
    private List<Item> items;
    private ArrayList<Item> itemsInStorage;
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
        this.available = new HashSet<>();
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
        for (Slot slot : slots)
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
                            findSlot(slot.getCenterX(), slot.getCenterY(), slot.getZ() - 1).getItem() == null) {
                        available.add(slot);
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

    public boolean isSlotAvailable(Slot slot) {
        if (available.contains(slot)) return true;
        return false;
    }

    public Slot findBestSlot(int zone, int currentX, int currentY) {
        //todo think of an exact strategy to chose the best slot
        for (Slot slot : slotArrayYDimension.get((currentY-5)/10)) {
            if (slot.getItem() == null) return slot;
        }
        return null;
    }

    public Slot calculateDropOffSlot(List<Slot> forbiddenSlots) {
        //todo: implement method
        if (forbiddenSlots != null) {

        }
        return null;
    }

    public Slot findSlot(int x, int y, int z) {

        y = (y - 5) / 10;
        x = ((x - 5) / 10) + z * xDimension;

        return slotArrayYDimension.get(y).get(x);
    }

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
        slot.setItem(item);
        item.setSlotID(slot);
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
    public LinkedList<Slot> getStackedItemSlots(Slot slot) {
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
