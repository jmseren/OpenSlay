public abstract class GUI {
    public String name;
    public int x;
    public int y;
    public int width;
    public int height;

    public GUI(String name, int x, int y, int width, int height) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    public abstract void onClick();

    public boolean click(int x, int y){
        if( x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.height){
            onClick();
            return true;
        }
        return false;
    }
}