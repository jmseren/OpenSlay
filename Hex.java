public class Hex {
    public int x;
    public int y;
    public Color color;
    public Player owner;

    boolean filled;
    int code;

    public Hex(int x, int y, int mapCode){
        this.x = x;
        this.y = y;
        this.filled = mapCode > 0;
        this.code = code;
        this.color = new Color(255, 255, 255);
    }

    public void setOwner(Player player){
        this.owner = player;
        this.color = player.color;
    }
    public Player getOwner(){
        return this.owner;
    }

    public Pos getPos(){
        return new Pos(this.x, this.y);
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