import processing.core.*;

public class ImageElement extends GUI {
    int borderRadius = 10;

    public Color tint;

    public ImageElement(String name, PImage texture, int x, int y, int width, int height) {
        super(name, texture, x, y, width, height);
        tint = new Color(255, 255, 255);   
    }
    public Event onClick() {
        return null;
    }
    public void draw(OpenSlay os) {
        os.rectMode(PConstants.CENTER);
        os.fill(127, 127, 127);
        os.rect(x, y, width, height, borderRadius);
        os.fill(0,0,0);
        os.tint(tint.toProcessingColor());
        os.image(texture, x, y, width, height);
        os.noTint();
        os.rectMode(PConstants.CORNER);
    }
}
