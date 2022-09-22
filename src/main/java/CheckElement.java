import processing.core.PConstants;

public class CheckElement extends GUI {
    boolean checked = false;
    int size;
    int borderRadius = 10;
    Events e;
    public CheckElement(int x, int y, int size, Events e, boolean checked) {
        super("check", null, x, y, size, size);
        this.checked = checked;
        this.size = size;
        this.e = e;
    }
    public CheckElement(int x, int y, int size, Events e) {
        this(x, y, size, e, false);
    }
    public Event onClick(){
        this.checked = !this.checked;
        return new Event(e, this.checked);
    }
    public void draw(OpenSlay os){
        os.fill(255);
        os.rectMode(PConstants.CENTER);
        os.rect(this.x, this.y, this.size, this.size, this.borderRadius);
        os.fill(0);
        if(this.checked){
            // Place a smaller black square in the middle
            os.rect(this.x, this.y, this.size * 0.6f, this.size * 0.6f, this.borderRadius);
        }
        os.rectMode(PConstants.CORNER);

    }
        
    
}
