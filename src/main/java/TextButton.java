import processing.core.*;
public class TextButton extends GUI {
    public String text;
    public int borderRadius = 10;
    Event e;


    public TextButton(String text, int x, int y, int width, int height, Event e){
        super(text, null, x, y, width, height);
        this.text = text;
        this.e = e;
    }
    public Event onClick() {
        return e;
    }
    public void draw(OpenSlay os) {
        os.rectMode(PConstants.CENTER);
        os.fill(127, 127, 127);
        os.rect(x, y, width, height, borderRadius);
        os.fill(0,0,0);
        os.text(text, x, y);
        os.rectMode(PConstants.CORNER);
    }
}
