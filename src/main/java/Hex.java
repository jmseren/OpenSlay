public class Hex {
    public int x;
    public int y;
    public Color color;
    public Player owner;
    public boolean unitCanMove = true;
    Territory territory;

    boolean filled;
    int code;


    // Capital hex variables
    public boolean capital = false;
    public int gold = 0;

    public Hex(int x, int y, int mapCode){
        this.x = x;
        this.y = y;
        this.filled = mapCode > 0;
        this.code = mapCode;
        this.color = new Color(255, 255, 255);
    }

    public void setOwner(Player player){
        this.owner = player;
        this.color = player.color;
    }
    public Unit getUnit(){
        Unit u = new Unit(codeToUnitPower(code), this.territory);
        this.code = 1;
        return u;
    }
    public void setUnit(Unit unit){
        this.code = unitPowerToCode(unit.power);
    }
    public Player getOwner(){
        return this.owner;
    }

    public Pos getPos(){
        return new Pos(this.x, this.y);
    }

    public int codeToUnitPower(int code){
        return Math.max(code - 3, this.capital ? 1 : 0);
    }
    public int codeToUnitPower(){
        return codeToUnitPower(this.code);
    }

    public int unitPowerToCode(int power){
        return power + 3;
    }
    public boolean combineUnit(Unit u){
        int newCode = this.code + u.power;
        
        // Check if the new power would be higher than the max unit
        if(codeToUnitPower(newCode) > 7) return false;

        this.code = newCode;
        return true;
    }
    public boolean isEmpty(){
        // Returns whether or not the hex has a unit or tree on it
        return this.code == 1 && !this.capital;
    }

    // The pixel position of the hex on the window
    public Pos rawPos(int xOff, int yOff){
        float h = (float)(Math.sqrt(3) * OpenSlay.hexSize);
        int x = (int)(this.x * (OpenSlay.hexSize * 2 * 0.75));
        int y = (int)((float)this.y * h);
        if(this.x % 2 == 1){
            y += h / 2.0;
        }
        return new Pos(xOff + x, yOff + y);
    }

}