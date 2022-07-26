public class Hex {
    public int x;
    public int y;
    public Color color;
    boolean tree;
    boolean filled;
    public Hex(int x, int y) {
        this(x, y, true);
    }
    public Hex(int x, int y, boolean filled){
        this.x = x;
        this.y = y;
        this.filled = filled;
        this.color = new Color(255, 255, 255);
        this.tree = false;
    }
}