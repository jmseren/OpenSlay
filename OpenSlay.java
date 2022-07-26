// OpenSlay Version: 1.0
// GNU General Public License v2.0
// Created By Jayden Serenari
// Based on the Turn Based Strategy Game "Slay" by Sean O'Connor

import processing.core.*;
import processing.sound.*;
import java.util.*;
import java.io.*;


public class OpenSlay extends PApplet {
    public GameState gameState = GameState.GAME;
    public HexMap gameMap;

    public int hexSize = 32;
    public Pos playAreaSize;
    public Pos playAreaOffset = new Pos(100, 100);
    

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

        gameMap = loadMap("map.slay");


    }


    public void draw(){
        background(0, 0, 255);
        switch(gameState){
            case GAME:
                ingame();
                break;
        }
    }


    public void ingame(){
        drawMap(gameMap);

    }

    // Function for loading a map. Maps use a .slay file format.
    // 0 = water
    // 1 = land
    public HexMap loadMap(String mapFile){
        try{
            HexMap map = null;
            File file = new File(mapFile);
            Scanner lineScanner = new Scanner(file);

            // Load size of map
            while(lineScanner.hasNextLine()){
                String line = lineScanner.nextLine();
                String[] lineSplit = line.split(" ");
                if(line.charAt(0) == '[') continue; // This line is the header or a comment.
                if(line.charAt(0) == '#') continue; // This line is a comment.
                if(lineSplit.length != 2) throw new Exception("Invalid map file: size line is invalid.");
                map = new HexMap(Integer.parseInt(lineSplit[0]), Integer.parseInt(lineSplit[1]));   
                break;
            }
            if(map == null) throw new Exception("Invalid map file: size line not found.");

            // Load tile data from map file
            int x = 0;
            int y = 0;
            while(lineScanner.hasNextLine()){
                String line = lineScanner.nextLine();
                String[] lineSplit = line.split(" ");
                if(line.charAt(0) == '[') continue; // This line is the header or a comment.
                if(line.charAt(0) == '#') continue; // This line is a comment.
                if(lineSplit.length != map.width) throw new Exception("Invalid map file: line length does not equal map width.");
                for(String s : lineSplit){
                    map.hexes[x][y] = new Hex(x, y, Integer.parseInt(s) > 0);
                    x++;
                }
                x=0;
                y++;
            }
            return map;
        }catch(Exception e){
            System.out.println("Error loading map: " + e.getMessage());
            return null;
        }
    }




    // Drawing Functions

    public void drawMap(HexMap map){
        for(int x = 0; x < map.width; x++){
            for(int y = 0; y < map.height; y++){
                Hex hex = map.hexes[x][y];
                if(hex.filled) drawHex(hex);
            }
        }
    }
    public void drawHex(Hex hex){
        float h = (float)(Math.sqrt(3) * hexSize);
        int x = (int)(hex.x * (hexSize*2 * 0.75));
        int y = (int)((float)hex.y * h);
        if(hex.x % 2 == 1){
            y += h / 2.0;
        }
        fill(hex.color.toProcessingColor());
        polygon(x + playAreaOffset.x, y + playAreaOffset.y, hexSize, 6);
    }


    // This method is adapted from the Processing Documentation
    // https://processing.org/examples/regularpolygon.html
    public void polygon(int x, int y, int radius, int npoints){ 
        float angle = TWO_PI / npoints;
        beginShape();
        for (float a = 0; a < TWO_PI; a += angle) {
            float sx = x + cos(a) * radius;
            float sy = y + sin(a) * radius;
            vertex(sx, sy);
        }
        endShape(CLOSE);
    }

    public enum GameState {
        MENU,
        GAME,
        PAUSE,
        END
    } 
}  