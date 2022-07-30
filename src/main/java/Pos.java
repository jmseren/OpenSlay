public class Pos {
    public int x;
    public int y;
    public Pos(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public Pos add(Pos p2) {
        return new Pos(x + p2.x, y + p2.y);
    }
}