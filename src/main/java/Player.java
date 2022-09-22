import java.util.ArrayList;

public class Player {
    Color color;
    boolean ai = false;
    boolean lost = false;
    int aiSteps;
    int difficulty = 5;
    
    public Player(Color color){
        this.color = color;
        this.aiSteps = difficulty;
    }
    public void setDifficulty(int difficulty){
        this.difficulty = difficulty;
        this.aiSteps = difficulty;
    }
    public void stepAI(ArrayList<Territory> territories){
        // AI will run in steps, so that it can appear to run at a slower speed
        // Number of steps will be determined by the difficulty

        // Future AI Considerations:
        // AI should prioritize severing territories to make them weaker
        // AI should defend hexes with adjacent enemy units
        // AI should prioritize killing enemy units

        HexMap map = OpenSlay.gameMap;
        ArrayList<Territory> friendlyTerritories = new ArrayList<Territory>();
        ArrayList<Territory> enemyTerritories = new ArrayList<Territory>();
        ArrayList<Hex> unitHexes = new ArrayList<Hex>();
        ArrayList<Hex> treeHexes = new ArrayList<Hex>();

        for(Territory t : territories){
            if(t.owner == this && t.hasCapital()){
                friendlyTerritories.add(t);
            }else{
                enemyTerritories.add(t);
            }
        }
        if(friendlyTerritories.isEmpty()) return;

        // Pick a random territory
        Territory t = friendlyTerritories.get((int)(Math.random() * friendlyTerritories.size()));

        for(Hex h : t.tiles){
            if(h.hasUnit() && h.unitCanMove) unitHexes.add(h);
            else if(h.hasTree()) treeHexes.add(h);
        }


        if(!unitHexes.isEmpty() && (unitHexes.size() >= t.tiles.size() - 1 || t.getCapital().gold < 10 || Math.random() > 0.4)){
            // Moving a unit

            // Pick a random unit
            Hex unit = unitHexes.get((int)(Math.random() * unitHexes.size()));

            ArrayList<Hex> validTargets = new ArrayList<Hex>();
            ArrayList<Hex> priorityTargets = new ArrayList<Hex>();
            

            for(Hex h : t.neighboringHexes()){
                // If the unit can attack this hex, add it to the list of valid targets
                if(unit.codeToUnitPower() > map.getRelativePower(h) && h.filled){
                    validTargets.add(h);
                    if(h.capital || h.castle) priorityTargets.add(h);
                }
            }

            for(Hex h : unitHexes){
                if(h == unit) continue;
                if(t.getIncome() >= Unit.wage(h.codeToUnitPower()) + 2) validTargets.add(h);
            }

            if(treeHexes.size() > 1) validTargets.addAll(treeHexes);

            if(validTargets.isEmpty()) return;

            // Pick a random target
            Hex target;
            if(priorityTargets.isEmpty()) target = validTargets.get((int)(Math.random() * validTargets.size()));
            else target = priorityTargets.get((int)(Math.random() * priorityTargets.size()));

            if(target.owner == this){
                // Combine units
                target.combineUnit(unit.getUnit());
            }else{
                target.setUnit(unit.getUnit());
                target.setOwner(this);
                target.unitCanMove = false;
            }
            
            unit.code = 1;
        }else{
            // We will buy a new unit
            if((t.getIncome() >= 2 && t.getCapital().gold >= 10) || t.getCapital().gold >= 15){
                ArrayList<Hex> validHexes = new ArrayList<Hex>();
                ArrayList<Hex> combinableHexes = new ArrayList<Hex>();
                
                for(Hex h : t.tiles){
                    if(h.isEmpty()) validHexes.add(h);
                    else if(h.hasUnit()) combinableHexes.add(h);
                }

                // Add combinable units to the list of valid hexes if 
                // the territory can afford it
                for(Hex h : combinableHexes){
                    int combinedPower = h.codeToUnitPower() + 1;
                    if(Unit.wage(combinedPower) >= t.getIncome()) continue;
                    else if(combinedPower >= 5) continue;
                    else validHexes.add(h);
                }

                if(validHexes.isEmpty()) return;

                // Pick a random hex
                Hex hex = validHexes.get((int)(Math.random() * validHexes.size()));
                hex.combineUnit(new Unit(1, t));
                t.getCapital().gold -= 10;
            }

        }
    }
}