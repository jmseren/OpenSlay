import processing.core.PImage;

public class ImageButton extends GUI {
    public Events eventType;
    public ImageButton(String name, PImage texture, int x, int y, int width, int height, Events eventType) {
        this(name, texture, x, y, 0, width, height, eventType);
    }
    public ImageButton(String name, PImage texture, int x, int y, int z, int width, int height, Events eventType) {
        super(name, texture, x, y, width, height);
        this.eventType = eventType;
    }
    public Event onClick() {
        return new Event(eventType);
    }
    
}
