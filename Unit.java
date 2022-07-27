import processing.core.*;

// The unit class is only for when the unit is being dragged and dropped.

public class Unit {
    public int strength;
    PImage texture;

    public Unit(int strength) {
        this.strength = strength;
        switch(strength){
            case 1:
                texture = OpenSlay.textures.get("peasant");
                break;
            case 2:
                texture = OpenSlay.textures.get("spearman");
                break;
            case 3:
                texture = OpenSlay.textures.get("knight");
                break;
            case 4:
                texture = OpenSlay.textures.get("baron");
                break;
        }
    }
}