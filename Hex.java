public class Hex {
    public int x;
    public int y;
    public Color color;
    boolean tree;
    boolean filled;
    public Hex(int x, int y) {
        this.x = x;
        this.y = y;
        this.color = new Color(0, 0, 0);
        this.tree = false;
        this.filled = false;
    }
}