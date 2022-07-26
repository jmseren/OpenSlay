public class HexMap {
    public int width;
    public int height;
    public Hex[][] hexes;
    public HexMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.hexes = new Hex[width][height];
    }
}