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
            toCheck.get(0).territory = this;
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
        
        // If the territory doesn't have a capital, set it to the first tile
        if(tiles.size() >= 2 && this.getCapital() == null){
            for(Hex tile : tiles){
                if(tile.code == 1){
                    setCapital(tile);
                    break;
                }
            }
        }
        
        
    }
    public Hex getCapital() {
        for(Hex h : tiles) {
            if(h.capital) return h;
        }
        return null;
    }
    public void setCapital(Hex c){
        c.capital = true;
        c.gold = 0;
    }
    public int size(){
        return tiles.size();
    }
}