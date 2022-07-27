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
    public static PFont font;
    public static HashMap<String, PImage> textures = new HashMap<String, PImage>();
    public static int hexSize = 32;
    public static Color[] playerColors = {
        new Color(50, 168, 82),
        new Color(0, 127, 200),
        new Color(245, 152, 66),
        new Color(177, 77, 184),
    };
    public static Player[] players = new Player[playerColors.length];
    public static Player currPlayer;
    public static HexMap gameMap;


    // Global Variables
    public GameState gameState = GameState.INIT_GAME;

    public ArrayList<Territory> currTerritories;

    public int turn = 0;
    public boolean refresh = true;

    public Hex selectedHex;
    public Territory selectedTerritory;
    public Unit selectedUnit;

    public Pos playAreaSize;
    public Pos playAreaOffset = new Pos(100, 100);
    
    // Settings
    public static void main(String[] args) {
        String[] appletArgs = new String[] { "OpenSlay" };
        PApplet.main(appletArgs);
    }
    public void settings(){
        //fullScreen();
        size(1280,720);
        noSmooth();
    }
    public void setup(){
        frameRate(60);
        imageMode(CENTER);
        textAlign(CENTER, CENTER);
        font = createFont("./fonts/pixeloidsans.ttf", 32);
        textFont(font, 32);
        importTexture("background", "./textures/bg.png", 32);
        importTexture("pine", "./textures/pine.png", (int)(hexSize * 0.75));
        importTexture("palm", "./textures/palm.png", (int)(hexSize * 0.75));
        importTexture("capital", "./textures/capital.png", (int)(hexSize * 0.75));
        gameMap = loadMap("map.slay");

        // Create Players
        for(int i = 0; i < playerColors.length; i++){
            players[i] = new Player(playerColors[i]);
        }
        currPlayer = players[0];
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
        // Set initial territories
        refreshMap();

        // Give every capital a starting balance of 10 gold
        for(Territory t : currTerritories){
            Hex capital = t.getCapital();
            if(capital != null){
                capital.gold = 10;
            }
        }

        gameState = GameState.GAME;
    }
    public void ingame(){
        if(refresh) refreshMap();
        drawToolBar();
        drawMap(gameMap);

    }
    public void refreshMap(){
        ArrayList<Hex> hexes = gameMap.allHexes();
        currTerritories = new ArrayList<Territory>();
        while(hexes.size() > 0){
            Territory t = new Territory(hexes.get(0));
            for(Hex h2 : t.tiles){
                hexes.remove(h2);
            }
            currTerritories.add(t);
        }
        refresh = false;
    }


    public void mousePressed(){
        switch(gameState){
            case GAME:
                Hex h = getClosestHex();
                if(h.owner == currPlayer && (h.code == 1 || h.capital == true) && h.territory.size() >= 2){
                    selectedTerritory = h.territory;
                    selectedHex = null;
                    selectedUnit = null;
                }
                break;
        }
    }

    // Definitely not the best way, but the quickest solution I could think of to get going
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

    // Asset loading
    public void importTexture(String name, String path, int size){
        PImage img = loadImage(path);
        img.resize(size, size);
        textures.put(name, img);
    }

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
        int size = textures.get("background").width;
        for(int x = 0; x < width; x += size){
            for(int y = 0; y < height; y += size){
                image(textures.get("background"), x, y);
            }
        }
        imageMode(CENTER);
    }
    public void drawToolBar(){
        fill(161, 161, 161);
        rect(width-(width * .25f), 0, width * 0.25f, height);
        fill(0, 0, 0);
        text("OpenSlay", width-(width * .25f) + (width * 0.25f / 2), height / 10);
        if(selectedTerritory != null){
            text("Gold: " + selectedTerritory.getCapital().gold, width-(width * .25f) + (width * 0.25f / 2), height / 10 + (height / 10) * 1);
        }
    }
    public void drawMap(HexMap map){
        // ArrayList to store hexes that should be drawn last
        ArrayList<Hex> differed = new ArrayList<Hex>();

        for(int x = 0; x < map.width; x++){
            for(int y = 0; y < map.height; y++){
                Hex hex = map.hexes[x][y];

                if(hex.filled && selectedTerritory != null && hex.territory == selectedTerritory){
                    differed.add(hex);
                    continue;
                }
                if(hex.filled) drawHex(hex);


            }
        }
        for(Hex h : differed){
            drawHex(h);
        }
    }
    public void drawHex(Hex hex){
        float h = (float)(Math.sqrt(3) * hexSize);
        int x = (int)(hex.x * (hexSize*2 * 0.75));
        int y = (int)((float)hex.y * h);
        if(hex.x % 2 == 1){
            y += h / 2.0;
        }

        // Make sure selected hexes get a border
        boolean selected = false;
        if(selectedTerritory != null && selectedTerritory == hex.territory){
            selected = true;
        }
        

        fill(hex.color.toProcessingColor());
        polygon(x + playAreaOffset.x, y + playAreaOffset.y, hexSize, 6, selected);

        switch(hex.code){
            case 2:
                image(textures.get("pine"), x + playAreaOffset.x, y + playAreaOffset.y);
                break;
            case 3:
                image(textures.get("palm"), x + playAreaOffset.x, y + playAreaOffset.y);
        }
        if(hex.capital) image(textures.get("capital"), x + playAreaOffset.x, y + playAreaOffset.y);
    }


    // This method is adapted from the Processing Documentation
    // https://processing.org/examples/regularpolygon.html
    public void polygon(int x, int y, int radius, int npoints, boolean highlight){ 
        float angle = TWO_PI / npoints;
        

        PShape s = createShape();
        if(highlight) s.setStroke(255);        
        
        s.beginShape();
        if(highlight) s.strokeWeight(3);
        for (float a = 0; a < TWO_PI; a += angle) {
            float sx = x + cos(a) * radius;
            float sy = y + sin(a) * radius;
            s.vertex(sx, sy);
        }
        s.endShape(CLOSE);
        shape(s, 0,0);
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