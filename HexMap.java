public class HexMap {
    public int width;
    public int height;
    public Hex[] hexes;
    public HexMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.hexes = new Hex[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                this.hexes[x][y] = new Hex(x, y);
            }
        }
    }
}