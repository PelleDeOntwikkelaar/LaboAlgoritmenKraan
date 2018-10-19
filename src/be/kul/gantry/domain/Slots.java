package be.kul.gantry.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Slots {
    private Slot inputSlot;
    private Slot outputSlot;
    private ArrayList<ArrayList> slotArrayYDimension;
    private Set<Slot>available;
    private List<Item> items;
    private List<Item>itemsInStorage;
    private int yDimension;
    private int xDimenseion;
    private int maxLevels;
    private boolean geschrankt;


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

    private void generateYList() {
        for(int i=0;i<yDimension;i++){
            slotArrayYDimension.add(new ArrayList<Slot>());
        }
    }

    public void addSlots(List<Slot> slots){
        //werkt enkel voor niet geschrankt
        for (Slot slot : slots){
            if(slot.isStorageSlot()){
                int y= (slot.getCenterY()-5)/10;
                int x= ((slot.getCenterX()-5)/10)+ slot.getZ()*xDimenseion;
                slotArrayYDimension.get(y).add(x,slot);
                if(slot.getItem()==null){
                    available.add(slot);
                }else{
                    slot.getItem().setSlotID(slot);
                    itemsInStorage.add(slot.getItem());
                }
            }else if(slot.isInputSlot()){
                inputSlot=slot;
            }else{
                outputSlot=slot;
            }
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

    public Slot findSlotByItem(Item item){
        if(!itemsInStorage.contains(item)){
            return null;
        }else {
            int index = itemsInStorage.indexOf(item);
            return itemsInStorage.get(index).getSlotID();

        }
    }






}
