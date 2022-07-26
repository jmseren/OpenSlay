// OpenSlay Version: 1.0
// GNU General Public License v2.0
// Created By Jayden Serenari
// Based on the Turn Based Strategy Game "Slay" by Sean O'Connor

import processing.core.*;
import processing.sound.*;
import java.util.*;
import java.io.*;


public class OpenSlay extends PApplet {

    // Global Static Variables
    public static HashMap<String, PImage> textures = new HashMap<String, PImage>();
    public static int hexSize = 32;
    public static Color[] playerColors = {
        new Color(50, 168, 82),
        new Color(0, 127, 200),
        new Color(245, 152, 66),
        new Color(177, 77, 184),
    };
    public static Player[] players = new Player[playerColors.length];
    public static HexMap gameMap;


    // Global Variables
    public GameState gameState = GameState.INIT_GAME;

    public Hex selectedHex;
    public Pos playAreaSize;
    public Pos playAreaOffset = new Pos(100, 100);
    
    // Settings
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
        imageMode(CENTER);
        textures.put("background", loadImage("./textures/bg.png"));
        textures.put("pine", loadImage("./textures/pine.png"));
        textures.put("palm", loadImage("./textures/palm.png"));
        gameMap = loadMap("map.slay");

        // Create Players
        for(int i = 0; i < playerColors.length; i++){
            players[i] = new Player(playerColors[i]);
        }
    }


    public void draw(){
        background(0, 0, 255);
        drawBackground();
        switch(gameState){
            case INIT_GAME:
                initGame();
                break;
            case GAME:
                ingame();
                break;
        }
    }

    // In-Game Functions
    public void initGame(){
        // Randomize players territories
        for(int x = 0; x < gameMap.width; x++){
            for(int y = 0; y < gameMap.height; y++){
                Hex hex = gameMap.getHex(x, y);
                if(hex.filled){
                    hex.setOwner(players[(int)(Math.random() * players.length)]);
                }
            }
        }

        gameState = GameState.GAME;
    }
    public void ingame(){
        drawMap(gameMap);

    }

    public void mousePressed(){
        switch(gameState){
            case GAME:
                Territory t = new Territory(getClosestHex());
                for(Hex h : t.tiles){
                    h.color = new Color(0, 0, 0);
                }
                break;
        }
    }

    // Probably not the best way, but the quickest solution I could think of to get going
    public Hex getClosestHex(){
        Hex closestHex = gameMap.getHex(0,0);
        Pos p = closestHex.rawPos(playAreaOffset.x, playAreaOffset.y);
        float distance = dist(mouseX, mouseY, p.x, p.y);
        for(int x = 0; x < gameMap.width; x++){
            for(int y = 0; y < gameMap.height; y++){
                p = gameMap.getHex(x,y).rawPos(playAreaOffset.x, playAreaOffset.y);
                float d = dist(mouseX, mouseY, p.x, p.y);
                if(d < distance){
                    distance = d;
                    closestHex = gameMap.getHex(x, y);
                }
            }
        }
        return closestHex;
    }

    // File IO Functions

    // Function for loading a map. Maps use a .slay file format.
    // 0 = water
    // 1 = land
    // 2 = pine
    // 3 = palm

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
                    map.hexes[x][y] = new Hex(x, y, Integer.parseInt(s));
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
    public void drawBackground(){
        imageMode(CORNER);
        int size = 16;
        for(int x = 0; x < width; x += size){
            for(int y = 0; y < height; y += size){
                image(textures.get("background"), x, y);
            }
        }
        imageMode(CENTER);
    }
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

        switch(hex.code){
            case 2:
                image(textures.get("pine"), x + playAreaOffset.x, y + playAreaOffset.y);
                break;
            case 3:
                image(textures.get("palm"), x + playAreaOffset.x, y + playAreaOffset.y);
        }
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


    // ENUMS

    public enum GameState {
        MENU,
        INIT_GAME,
        GAME,
        PAUSE,
        END
    } 
}  