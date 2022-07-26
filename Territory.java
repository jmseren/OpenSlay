import java.util.*;

public class Territory {
    ArrayList<Hex> tiles = new ArrayList<Hex>();
    public Territory(Hex origin) {
        // Create an array list to store the unchecked tiles in
        ArrayList<Hex> toCheck = new ArrayList<Hex>();

        // Start with the origin
        toCheck.add(origin);

        while(toCheck.size() > 0) {
            // Anything in toCheck is part of the territory
            tiles.add(toCheck.get(0));

            // Check the neighbors to see if they should be added to toCheck
            Hex[] neighbors = OpenSlay.gameMap.getNeighbors(toCheck.get(0));
            for(Hex neighbor : neighbors) {
                if(!tiles.contains(neighbor) && neighbor.filled && neighbor.owner == origin.owner) {
                    toCheck.add(neighbor);
                }
            }
            toCheck.remove(0);
        }
        
        
    }
}