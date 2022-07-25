public class Color {
    /* 
        There is no actual color class in Processing, so we'll use one of our own to store color values easier
        than doing the bit manipulation ourselves.

        This is just a simple class to store color values, which I've borrowed from
        my past projects.
    */
    private int r, g, b;
    int a = 255;

    public Color(int r, int g, int b){
        this.r = r;
        this.b = b;
        this.g = g;
    }
    public Color(int r, int g, int b, int a){
        this.r = r;
        this.b = b;
        this.g = g;
        this.a = a;
    }
    public Color(int c){
        int mask = 0xFF;
        this.b = c & mask;
        this.g = (c >> 8) & mask;
        this.r = (c >> 16) & mask;
    }
    public Color(Color c){
        this.r = c.getRGB()[0];
        this.g = c.getRGB()[1];
        this.b = c.getRGB()[2];
    }
    public int[] getRGB(){
        int[] rgbArr = {r, g, b};
        return rgbArr;
    }
    public Color average(Color c){
        if(c == null) return this;
        int newR, newG, newB;
        newR = c.getRGB()[0];
        newG = c.getRGB()[1];
        newB = c.getRGB()[2];
        newR = (newR + this.r) / 2; 
        newG = (newG + this.g) / 2; 
        newB = (newB + this.b) / 2; 
        return new Color(newR, newG, newB);
    }
    public Color average(int pc){
        Color c = new Color(pc);
        int newR, newG, newB;
        newR = c.getRGB()[0];
        newG = c.getRGB()[1];
        newB = c.getRGB()[2];
        newR = (newR + this.r) / 2; 
        newG = (newG + this.g) / 2; 
        newB = (newB + this.b) / 2; 
        return new Color(newR, newG, newB);
    }

    public int toProcessingColor() {
        int result = b;
        result += (g << 8);
        result += (r << 16);
        result += (a << 24); 
        return result;
    }
    public String toString(){
        return "R: " + this.r + ", G: " + this.g + ", B: " + this.b;
    }
    
}
