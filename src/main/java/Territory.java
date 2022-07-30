import java.util.*;

public class Territory {
    public Player owner;
    ArrayList<Hex> tiles = new ArrayList<Hex>();
    public Territory(Hex origin) {
        // Set the owner to the origins owner
        this.owner = origin.owner;
        
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
    public boolean isNeighbor(HexMap map, Hex h){
        for(Hex n : map.getNeighbors(h)){
            if(tiles.contains(n)) return true;
        }
        return false;
    }
    public void income(){
        if(this.size() < 2) return;
        int profit = 0;
        int wages = 0;
        for(Hex h : tiles){
            switch(h.code){
                case 1:
                    profit++;
                    break;
                case 4:
                    // Peasant
                    profit++;
                    wages += 2;
                    break;
                case 5:
                    // Spearman
                    profit++;
                    wages += 6;
                    break;
                case 6:
                    // Knight
                    profit++;
                    wages += 18;
                    break;
                case 7:
                    // Baron
                    profit++;
                    wages += 54;
                    break;
            }
        }
        this.getCapital().gold += profit - wages;
    }
}