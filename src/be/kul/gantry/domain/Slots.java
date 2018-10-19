package be.kul.gantry.domain;

import java.util.ArrayList;
import java.util.List;

public class Slots {
    private ArrayList<ArrayList> slotArrayYDimension;
    private List<Item> items;
    private int yDimension;
    private int xDimenseion;
    private boolean geschrankt;


    public Slots(List<Item> items,int yDimension,int xDimenseion,boolean geschrankt) {
        this.items=items;
        this.yDimension=yDimension;
        this.xDimenseion=xDimenseion;
        this.geschrankt=geschrankt;
        this.slotArrayYDimension = new ArrayList<>();

        for(int i=0;i<yDimension;i++){
            slotArrayYDimension.add(new ArrayList<Slot>());
        }
    }

    public void addSlots(){

    }

    public boolean isSlotAvailable(Slot slot){
        //todo: available slot methode schrijven.
        return false;
    }

    public Slot findBestSlot(int zone){
        //todo: implement method
        return null;
    }

    public int findSlotByItem(){
        //todo: implement method
        return -1;
    }






}
