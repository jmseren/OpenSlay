import processing.core.*;

// Spritesheets are supported only in the horizontal direction

public class AnimatedImage extends GUI implements Animation {
    PImage spriteSheet;
    int spriteWidth;
    int spriteHeight;
    int delayCount = 0;
    int frames;
    int speed;
    boolean playing = true;
    int frame = 0;

    public AnimatedImage(PImage spriteSheet, int speed, int width, int height, int x, int y) {
        super("AnimatedImage", spriteSheet, x, y, width, height);
        this.spriteSheet = spriteSheet;
        this.frames = spriteSheet.width / width;
        this.spriteWidth = width;
        this.spriteHeight = height;
    }

    public void draw(OpenSlay os) {
        // Create a graphics object and move it to place the correct frame in the graphic

        PGraphics still = os.createGraphics(spriteWidth, spriteHeight);
        still.beginDraw();
        still.image(spriteSheet, frame * -spriteWidth, 0);
        still.endDraw();

        os.image(still, x, y);
    }

    public void toggle(){
        playing = !playing;
    }

    public void animate() {
        if(playing && delayCount <= 0){
            delayCount = speed;
            frame++;
        }else if(playing){
            delayCount--;
        }
    }

    public void delay(int frames) {
        delayCount += frames;                
    }

    public boolean isAnimating() {
        return playing;
    }

    public Event onClick() {
        return new Event(Events.NO_EVENT);
    }

    
}
