import processing.core.*;

public class ImageElement extends GUI {
    int borderRadius = 10;

    Event e = null;

    public Color tint;

    public boolean resize = true;

    public Color backgroundColor;

    public ImageElement(String name, PImage texture, int x, int y, int width, int height) {
        super(name, texture, x, y, width, height);
        tint = new Color(255, 255, 255);
        backgroundColor = new Color(127, 127, 127);
    }
    public Event onClick() {
        return e;
    }
    public void draw(OpenSlay os) {
        os.rectMode(PConstants.CENTER);
        os.fill(backgroundColor.toProcessingColor());
        os.rect(x, y, width, height, borderRadius);
        os.fill(0,0,0);
        os.tint(tint.toProcessingColor());
        if(resize) os.image(texture, x, y, width, height);
        else os.image(texture, x, y);
        os.noTint();
        os.rectMode(PConstants.CORNER);
    }
}
