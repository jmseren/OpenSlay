import processing.core.*;
import processing.sound.*;

public class OpenSlay extends PApplet {
    public static void main(String[] args) {
        String[] appletArgs = new String[] { "OpenSlay" };
        PApplet.main(appletArgs);
    }
    public void settings(){
        size(1280,720);
        noSmooth();
    }
    public void setup(){
        frameRate(60);
    }
    public void draw(){
        background(0, 0, 255);
    }
}  