package be.kul.gantry.domain;

/**
 * Created by Wim on 12/05/2015.
 */
public class Item {

    private final int id;

    private int slotID;

    public Item(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getSlotID() {
        return slotID;
    }

    public void setSlotID(int slotID) {
        this.slotID = slotID;
    }

}
