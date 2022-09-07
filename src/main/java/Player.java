import java.util.ArrayList;

public class Player {
    Color color;
    boolean ai = false;
    boolean lost = false;
    int aiSteps = 5;
    float difficulty = 0.5f;
    public Player(Color color){
        this.color = color;
    }
    public void stepAI(ArrayList<Territory> territories){
        // AI will run in steps, so that it can appear to run at a slower speed

        // Going to use a simple AI for now, just to get it working
        // Future AI will consider enemy unit count, enemy territory income and size
        // Whether or not the AI can join two of its territories, etc.

        HexMap map = OpenSlay.gameMap;
        ArrayList<Territory> friendlyTerritories = new ArrayList<Territory>();
        ArrayList<Territory> enemyTerritories = new ArrayList<Territory>();

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

        ArrayList<Hex> unitHexes = new ArrayList<Hex>();
        for(Hex h : t.tiles){
            if(h.hasUnit() && h.unitCanMove) unitHexes.add(h);
        }

        if(!unitHexes.isEmpty() && (unitHexes.size() >= t.tiles.size() - 1 || t.getCapital().gold < 10 || Math.random() > 0.5)){
            // Not enough room to buy a new unit, so we will move a unit instead
            // Pick a random unit
            Hex unit = unitHexes.get((int)(Math.random() * unitHexes.size()));

            ArrayList<Hex> validTargets = new ArrayList<Hex>();
            
            for(Hex h : t.neighboringHexes()){
                // If the unit can attack this hex, add it to the list of valid targets
                if(unit.codeToUnitPower() > map.getRelativePower(h) && h.filled){
                    validTargets.add(h);
                }
            }


            if(!(validTargets.size() > 0)) return;

            // Pick a random target
            Hex target = validTargets.get((int)(Math.random() * validTargets.size()));
            target.setUnit(unit.getUnit());
            target.setOwner(this);
            target.unitCanMove = false;
            unit.code = 1;
        }else{
            // We will buy a new unit
            if((t.getIncome() >= 2 && t.getCapital().gold >= 10) || t.getCapital().gold >= 15){
                ArrayList<Hex> validHexes = new ArrayList<Hex>();
                
                for(Hex h : t.tiles){
                    if(h.isEmpty()) validHexes.add(h);
                }
                if(validHexes.isEmpty()) return;

                // Pick a random hex
                Hex hex = validHexes.get((int)(Math.random() * validHexes.size()));
                hex.setUnit(new Unit(1, t));
                t.getCapital().gold -= 10;
            }

        }
    }
}