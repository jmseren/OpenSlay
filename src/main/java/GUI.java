import processing.core.*;


public abstract class GUI {
    public String name;
    public int x;
    public int y;
    public int width;
    public int height;
    public PImage texture;

    public GUI(String name, PImage texture, int x, int y, int z, int width, int height) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.texture = texture;
    }
    public GUI(String name, PImage texture, int x, int y, int width, int height) {
        this(name, texture, x, y, 0, width, height);
    }
    
    public boolean click(int x, int y){
        if( x >= this.x - this.width/2 && x <= this.x + this.width/2 && y >= this.y - this.height/2 && y <= this.y + this.height/2){
            return true;
        }
        return false;
    }
    public abstract Event onClick();
    public void draw(OpenSlay os){
        os.image(texture, x, y, width, height);
    }
}