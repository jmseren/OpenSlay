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
        
        // If the territory size is greater than 1, check if it has a capital, or more than 1 capital
        if(tiles.size() >= 2){
            int capitals = this.numCapitals();
            if(capitals == 0){
                for(Hex tile : tiles){
                    if(tile.code == 1){
                        setCapital(tile);
                        break;
                    }
                }
            }else if(capitals > 1){
                // If there os more than one capital choose the capital with more money
                // Add the money from the second capital to the first capital
                Hex c = this.getCapital();
                for(Hex h : tiles){
                    if(h == c) continue;
                    if(h.capital){
                        c.gold += h.gold;
                        h.gold = 0;
                        h.capital = false;
                    }
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
    public boolean hasCapital() {
        if(this.size() < 2) return false;
        return !(this.getCapital() == null);
    }
    public void moveCapital() {
        Hex c = this.getCapital();
        if(this.size() == 2){
            c.gold = 0;
            c.capital = false;
            return;
        }
        for(Hex h : tiles) {
            if(h == c) continue;
            h.capital = true;
            h.gold = c.gold;
            c.gold = 0;
            c.capital = false;
            return;
        }

    }
    public int numCapitals() {
        int count = 0;
        for(Hex h : tiles) {
            if(h.capital) count++;
        }
        return count;
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
    public ArrayList<Hex> neighboringHexes(){
        ArrayList<Hex> neighbors = new ArrayList<Hex>();
        for(Hex h : tiles){
            for(Hex n : OpenSlay.gameMap.getNeighbors(h)){
                if(!neighbors.contains(n) && !tiles.contains(n)) neighbors.add(n);
            }
        }
        return neighbors;
    }
    public int getIncome(){ // Returns the net income for a territory
        if(this.size() < 2) return 0;
        int profit = 0;
        int wages = 0;
        for(Hex h : tiles){
            switch(h.code){
                case 1:
                    profit++;
                    break;
                // 2 and 3 are skipped as they contain trees and do not produce gold
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
        return profit - wages;
    }
    public void income(){
        if(this.size() < 2) return;
        this.getCapital().gold += this.getIncome();
    }
}