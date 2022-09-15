import processing.core.*;

// The unit class is only for when the unit is being dragged and dropped.

public class Unit {
    public int power;
    PImage texture;
    Territory territory;

    public static int wage(int power){
        return 2 * 3^(power - 1);
    }

    public Unit(int power, Territory territory) {
        this.power = power;
        this.territory = territory;
        switch(power){
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
            case 5:
                texture = OpenSlay.textures.get("castle");
                break;
        }
    }
}