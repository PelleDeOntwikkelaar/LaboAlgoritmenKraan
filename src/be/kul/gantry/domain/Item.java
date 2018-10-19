package be.kul.gantry.domain;

/**
 * Created by Wim on 12/05/2015.
 */
public class Item {

    private final int id;

    private Slot slot;

    public Item(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Slot getSlotID() {
        return slot;
    }

    public void setSlotID(Slot slot) {
        this.slot = slot;
    }

}
