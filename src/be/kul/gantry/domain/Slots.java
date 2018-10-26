package be.kul.gantry.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Slots {
    private Slot inputSlot;
    private Slot outputSlot;
    private ArrayList<ArrayList<Slot>> slotArrayYDimension;
    private Set<Slot>available;
    private List<Item> items;
    private List<Item>itemsInStorage;
    private int yDimension;
    private int xDimenseion;
    private int maxLevels;
    private boolean geschrankt;

    /**
     * De default constructor voor de klasse Slots
     * @param items Een lijst met alle items in de probleemstelling
     * @param maxX  Een integer waarde die de maximale X dimensie bevat
     * @param maxY  Een intiger waarde die de maximale Y dimensie bevat
     * @param maxLevels Een integet waarde die het maximaal aantal levels dat er gestapeld mag worden bevat.
     * @param geschrankt Een boolean waarde die erop wijst of er geschrankt gestapeld kan worden.
     */
    public Slots(List<Item> items,int maxX,int maxY,int maxLevels,boolean geschrankt) {

        this.items=items;
        this.yDimension=maxY/10;
        this.xDimenseion=(maxX-20)/10;
        this.geschrankt=geschrankt;
        this.maxLevels=maxLevels;
        this.slotArrayYDimension = new ArrayList<>();
        this.available=new HashSet<>();
        this.itemsInStorage=new ArrayList<>();
        generateYList();
    }

    /**
     * Void methode die ervoor zorgt dat in de Y dimensies de juiste rijen gedeclareerd worden.
     */
    private void generateYList() {
        for(int i=0;i<yDimension;i++){
            slotArrayYDimension.add(new ArrayList<Slot>());
        }
    }

    /**
     * Methode die de slots op een goede manier gaat aanmaken en alle parameters zet.
     * @param slots Een lijst van Slots die gegeven worden in de probleemstelling.
     */
    public void addSlots(List<Slot> slots){
        //werkt enkel voor niet geschrankt
        /*
            wanneer we te maken hebben met een opslag slot, moet deze op de juiste manier worden aangemaakt en ingpast
            worden.
            */
        for (Slot slot : slots)
            if (slot.isStorageSlot()) {
                int y = (slot.getCenterY() - 5) / 10;
                int x = ((slot.getCenterX() - 5) / 10) + slot.getZ() * xDimenseion;
                slotArrayYDimension.get(y).add(x, slot);

                /*
                De volgende logica is bedoeld om de juiste slots "beschikbaar" te maken.
                Zo zijn slots beschikbaar waneer ze geen item bevatten en er onder hun ofwel grond is,
                 ofwel een bezet slot.
                 */
                if (slot.getItem() == null) {
                    if (slot.getZ() == 0) {
                        available.add(slot);
                    } else if (findSlot(slot.getCenterX(), slot.getCenterY(), slot.getZ() - 1).getItem() == null) {
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

    public boolean isSlotAvailable(Slot slot){
        if(available.contains(slot)) return true;
        return false;
    }

    public Slot findBestSlot(int zone, int currentX, int currentY){
        //todo: implement method
        return null;
    }

    public Slot calculateDropOffSlot(List<Slot> forbiddenSlots){
        if(forbiddenSlots!=null){

        }
        return null;
    }

    public Slot findSlot(int x, int y, int z){

        y = (y - 5) / 10;
        x = ((x - 5) / 10) + z * xDimenseion;

        return slotArrayYDimension.get(y).get(x);
    }

    public Slot findSlotByItem(Item item){
        if(!itemsInStorage.contains(item)){
            return null;
        }else {
            int index = itemsInStorage.indexOf(item);
            return itemsInStorage.get(index).getSlotID();

        }
    }






}
