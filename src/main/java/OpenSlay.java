// OpenSlay Version: 1.0
// GNU General Public License v2.0
// Created By Jayden Serenari
// Based on the Turn Based Strategy Game "Slay" by Sean O'Connor

import processing.core.*;
import java.util.*;
import java.io.*;

public class OpenSlay extends PApplet {

    // Global Static Variables
    public static PFont font;
    public static HashMap<String, PImage> textures = new HashMap<String, PImage>();
    public static HashMap<String, GUI> guiElements;
    public static EventHandler eventHandler = new EventHandler();
    public static int hexSize = 32;
    public static int page = 0;
    public static int campaignMaps = 10;
    


    // For now, we will automatically assume we have as many players as there are colors
    public static Color[] playerColors = {
        // Green
        new Color(50, 168, 82),

        // Cyan
        new Color(0, 127, 200),

        // Red
        new Color(245, 152, 66),

        // Purple
        new Color(177, 77, 184),

        // Orange
        new Color(232, 40, 55),

        // Pink
        new Color(255, 0, 255),


    };
    public static Player[] players;
    public static Player currPlayer;

    public static HexMap gameMap;

    // Global Variables
    public GameState gameState;

    public ArrayList<Territory> currTerritories;

    public int turn = 0;
    public boolean refresh = true;

    public Hex selectedHex;
    public Territory selectedTerritory;
    public Unit selectedUnit;

    public Pos playAreaSize;
    public Pos playAreaOffset = new Pos(100, 100);
    
    public String mapFile = "";

    public int step = 0;
    // Settings
    public static void main(String[] args) {
        String[] appletArgs = new String[] { "OpenSlay" };
        PApplet.main(appletArgs);
    }
    public void settings(){
        // fullScreen();
        size(1280,720);
        noSmooth();
    }
    public void setup(){
        frameRate(60);
        imageMode(CENTER);
        textAlign(CENTER, CENTER);
        font = createFont("fonts/pixeloidsans.ttf", 32);
        textFont(font, 32);
        loadTextures();
        guiElements = new HashMap<String, GUI>();
        
        changeState(GameState.MENU);
    }


    public void draw(){
        background(0, 0, 255);
        drawBackground();
        drawGUI();
        switch(gameState){
            case NEXT_TURN:
                nextTurn();
                break;
            case GAME:
                ingame();
                break;
            default:
                break;
        }
        processEvents();
    }

    // In-Game Functions
    public void changeState(GameState state){
        gameState = state;
        initState(state);
    }
    public void initState(GameState state){
        HashMap<String, GUI> newGUI = new HashMap<String, GUI>();
        switch(state){
            case MENU:
                newGUI = new HashMap<String, GUI>();
                TextElement title = new TextElement("OpenSlay", width/2, height/2 - 100);
                TextButton startButton = new TextButton("Start", width/2, height/2, 200, 50, new Event(Events.CHANGE_STATE, GameState.MAP_SELECTION));
                newGUI.put("title", title);
                newGUI.put("startButton", startButton);
                break;
            case NEXT_TURN:
                break;
            case GAME:
                // Create Players
                players = new Player[((NumberElement)(guiElements.get("playerCount"))).value];
                for(int i = 0; i < players.length; i++){
                    players[i] = new Player(playerColors[i]);
                    if(!(i == 0)){
                        players[i].ai = true;
                    }
                }
                currPlayer = players[0];
                initGame(newGUI);
                break;
            case PLAYER_COUNT:
                title = new TextElement("Player Count", width/2, height/2 - 100);
                // Text element in the center will display the current player count
                NumberElement playerCount = new NumberElement(2, 2, 6, width/2, height/2);
                // Text buttons on the left and right will decrease and increase the player count
                TextButton decreaseButton = new TextButton("<", width/2 - 100, height/2, 50, 50, new Event(Events.DECREMENT, playerCount));
                TextButton increaseButton = new TextButton(">", width/2 + 100, height/2, 50, 50, new Event(Events.INCREMENT, playerCount));
                
                // Confirm button will start the game
                TextButton confirmButton = new TextButton("Confirm", width/2, height/2 + 100, 200, 50, new Event(Events.CHANGE_STATE, GameState.GAME));
                
                newGUI.put("title", title);
                newGUI.put("playerCount", playerCount);
                newGUI.put("decreaseButton", decreaseButton);
                newGUI.put("increaseButton", increaseButton);
                newGUI.put("confirmButton", confirmButton);
                break;
            case MAP_SELECTION:
                // Display a grid with list of maps in the campaign

                for(int i = 0; i < campaignMaps; i++){
                    int x = i % 3;
                    int y = i / 3;
                    int buttonSize = height / 15;
                    int buttonSpacing = height / 30;

                    TextButton mapButton = new TextButton("" + (i+1), width/2 - (buttonSize * 3 + buttonSpacing * 2)/2 + x * (buttonSize + buttonSpacing), height/2 - (buttonSize * 3 + buttonSpacing * 2)/2 + y * (buttonSize + buttonSpacing), buttonSize, buttonSize, new Event(Events.MAP_SELECTED, i+1));
                    newGUI.put("map" + i, mapButton);
                }
                break;
            default:
                break;
        }

        // This can probably be cleaned up, remnant of refactoring from a previous version
        guiElements = gameState == GameState.NEXT_TURN ? guiElements : newGUI;
    }

    public void initGame(HashMap<String, GUI> gui){
        // Load the map
        gameMap = loadMap(this.getClass().getResourceAsStream("/maps/" + mapFile + ".slay"));

        // Randomize players territories
        ShuffleBag<Player> playerBag = new ShuffleBag<Player>();
        for(Player p : players){
            playerBag.add(p, 2);
        }

        for(int x = 0; x < gameMap.width; x++){
            for(int y = 0; y < gameMap.height; y++){
                Hex hex = gameMap.getHex(x, y);
                if(hex.filled){
                    hex.setOwner(playerBag.next());
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
        
        // Initialize the GUI

        // Add the peasant button
        PImage peasantTexture = textures.get("peasant_disabled");
        ImageButton peasantButton = new ImageButton("peasant_button", peasantTexture, (int)(width-((width * 0.25)) + (width * 0.25 / 2)), height / 10 + (height /10) * 2, peasantTexture.width * 2, peasantTexture.height * 2, Events.PEASANT);
        gui.put(peasantButton.name, peasantButton);

        // Add the castle button
        PImage castleTexture = textures.get("castle_disabled");
        ImageButton castleButton = new ImageButton("castle_button", castleTexture, (int)(width-((width * 0.25)) + (width * 0.25 / 2)), height / 10 + (height /10) * 4, castleTexture.width * 2, castleTexture.height * 2, Events.CASTLE);
        gui.put(castleButton.name, castleButton);
        
        // Add the end turn button
        TextButton nextTurn = new TextButton("End Turn", (int)(width-((width * 0.25)) + (width * 0.25 / 2)), height - 100, 200, 50, new Event(Events.CHANGE_STATE, GameState.NEXT_TURN));
        gui.put("nextTurn", nextTurn);

    }

    public void nextTurn(){
        turn++;
        // Reset selected hex
        selectedHex = null;
        selectedTerritory = null;
        selectedUnit = null;
        // Set current player
        currPlayer = players[turn % players.length];
        // Refresh map
        refreshMap();
        // If it is the second round, calculate the income for the new player
        int round = turn / players.length;
        int tcount = 0;
        if(round >= 1){
            for(Territory t : currTerritories){
                if(t.owner == currPlayer){
                    t.income();
                    if(t.hasCapital()){
                        if(t.getCapital().gold < 0){
                            for(Hex h : t.tiles){
                                if(h.hasUnit()){
                                    h.code = 1;
                                }
                            }
                            t.getCapital().gold = 0;
                        }
                        tcount++;
                    }else{
                        for(Hex h : t.tiles){
                            if(h.hasUnit()){
                                h.code = 1;
                            }
                        }
                    }
                    
                }
            }
            if(tcount == 0) currPlayer.lost = true;
            // If it is a new round make all units moveable      
            if(turn % players.length == 0){
                for(Hex h : gameMap.allHexes()){
                    h.unitCanMove = true;
                }
            }

            // Find valid spots for trees in the players territories

            // Create a hash map to store the trees with their corresponding hexes
            HashMap<Hex, Integer> treesToGrow = new HashMap<Hex, Integer>();
            for(Territory t : currTerritories){
                if(t.owner == currPlayer){
                    for(Hex h : t.tiles){
                        if(h.isEmpty()){
                            // Hex is empty, valid candidate for a tree
                            int pines = 0;
                            for(Hex n : gameMap.getNeighbors(h)){
                                if(n.code == 3 && gameMap.onCoast(h)){
                                    // A palm tree should be grown
                                    treesToGrow.put(h, 3);
                                    break;
                                }else if(n.code == 2){
                                    // Add a tally to pine tree neighbors
                                    pines++;
                                }
    
                                if(pines >= 2){
                                    // A pine tree should be grown
                                    treesToGrow.put(h, 2);
                                    break;
                                }
                                
                            }
                        }
                    }
                }
                // Grow the trees
                for(Hex h : treesToGrow.keySet()){
                    h.code = treesToGrow.get(h);
                }
            }
        }
        // Set game state back
        gameState = GameState.GAME;

    }
    public void ingame(){
        if(refresh) refreshMap();
        if(!currPlayer.ai) return;

        // AI
        if(frameCount % 30 == 15 && step < currPlayer.aiSteps){
            step++;
            currPlayer.stepAI(currTerritories);
        }else if(step >= currPlayer.aiSteps){
            step = 0;
            changeState(GameState.NEXT_TURN);
        }



    }
    public void processEvents(){
        while(eventHandler.queueSize() > 0){
            Event e = eventHandler.nextEvent();
            switch(e.getEventType()){
                case INCREMENT:
                    // Increment a NumberElement
                    NumberElement n = (NumberElement) e.getEventData();
                    n.increment();
                    break;
                case DECREMENT:
                    // Decrement a NumberElement
                    n = (NumberElement) e.getEventData();
                    n.decrement();
                    break;
                case PEASANT:
                    // Player has attempted to purchase a peasant
                    if(selectedTerritory != null && selectedUnit == null && selectedTerritory.getCapital().gold >= 10){
                        selectedUnit = new Unit(1, selectedTerritory);
                        selectedTerritory.getCapital().gold -= 10;
                        selectedTerritory = null;
                        selectedHex = null;
                        guiElements.get("peasant_button").texture = textures.get("peasant_disabled");
                    }
                    break;
                case CASTLE:
                    // Player has attempted to purchase a castle
                    if(selectedTerritory != null && selectedUnit == null && selectedTerritory.getCapital().gold >= 20){
                        selectedUnit = new Unit(5, selectedTerritory);
                        selectedTerritory.getCapital().gold -= 15;
                        selectedTerritory = null;
                        selectedHex = null;
                        guiElements.get("castle_button").texture = textures.get("castle_disabled");
                    }
                    break;
                case CHANGE_STATE:
                    // Change the game state
                    changeState((GameState)(e.getEventData()));
                    break;
                case MAP_SELECTED:
                    mapFile = "map" + ((int) e.getEventData());
                    changeState(GameState.PLAYER_COUNT);
                    break;
                case NO_EVENT:
                default:
                    break;
            }
        }
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
        for(GUI g : guiElements.values()){
            if(g.click(mouseX, mouseY)) eventHandler.pushEvent(g.onClick());
        }
        // if(currPlayer.ai){
        //     // AI is playing, do nothing
        //     return;
        // }
        switch(gameState){
            case GAME:
                Hex h = getClosestHex();
                if(h != null && selectedUnit == null && (h.code >= 1 && h.code <= 3 || h.capital == true) ){
                    if(h.owner == currPlayer && h.territory.size() >= 2){
                        // The player has clicked on a territory, and it is a selectable tile and size
                        selectedTerritory = h.territory;
                        selectedHex = null;
                        selectedUnit = null;
                        break;
                    }
                }
                if(selectedUnit == null && h != null && h.code >= 4 && h.unitCanMove && h.owner == currPlayer){
                    // Player has selected a unit
                    selectedUnit = h.getUnit();
                    selectedTerritory = null;
                    selectedHex = null;
                    break;
                }
                if(selectedUnit != null && h != null && h.code >= 4){
                    // Player has attempted to combine units
                    if(h.combineUnit(selectedUnit)){
                        // Unit combination successful
                        selectedUnit = null;
                    }
                    selectedTerritory = null;
                    selectedHex = null;
                    break;
                }
                if(selectedUnit != null && h != null){
                    // Player has attempted to place a unit
                    if(h.isEmpty() && h.territory == selectedUnit.territory){
                        /// Valid territory for the unit
                        selectedHex = null;
                        selectedTerritory = selectedUnit.territory;
                        h.setUnit(selectedUnit);
                        selectedUnit = null;
                        break;

                    }
                    if((h.code == 2 || h.code == 3) && h.territory == selectedUnit.territory){
                        // Unit has destroyed a tree
                        h.setUnit(selectedUnit);
                        h.unitCanMove = false;
                        selectedUnit = null;
                        refresh = true;
                        break;

                    }
                    if(h.filled == true && selectedUnit.territory.isNeighbor(gameMap, h)){
                        // Neighboring hex of territory, unit can attack this square
                        if(gameMap.getRelativePower(h) < selectedUnit.power){
                            // Unit can attack this square

                            h.setOwner(currPlayer);
                            h.setUnit(selectedUnit);
                            h.unitCanMove = false;
                            selectedUnit = null;
                            refresh = true;
                        }
                        break;
                    }
                }
                
                
                break;
        }
    }
    public void keyPressed(){
        if(key == ' '){
            currPlayer.stepAI(currTerritories);
            refreshMap();
        }
    }
    
    // Event handling


    // User Input Functions

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
        if(distance > hexSize){
            return null;
        }
        return closestHex;
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


        if(selectedTerritory != null && !currPlayer.ai){
            int gold = selectedTerritory.getCapital().gold;
            text("Gold: " + gold, width-(width * .25f) + (width * 0.25f / 2), height / 10 + (height / 10) * 1);
            
            // Set the buttons to their proper colors depending on territory's gold
            if(gold >= 10){
                guiElements.get("peasant_button").texture = textures.get("peasant");
                if(gold >= 15){
                    guiElements.get("castle_button").texture = textures.get("castle");
                }else {
                    guiElements.get("castle_button").texture = textures.get("castle_disabled");
                }
            }else{
                guiElements.get("peasant_button").texture = textures.get("peasant_disabled");
            }
        }else if(currPlayer.ai){

        }
        

    }
    public void drawGUI(){
        if(gameState == GameState.GAME){
            drawMap(gameMap);
            drawToolBar();
            drawUnit();
        }
        for(GUI g : guiElements.values()){
            g.draw(this);
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
                break;
            case 4:
                image(textures.get("peasant"), x + playAreaOffset.x, y + playAreaOffset.y);
                break;
            case 5:
                image(textures.get("spearman"), x + playAreaOffset.x, y + playAreaOffset.y);
                break;
            case 6:
                image(textures.get("knight"), x + playAreaOffset.x, y + playAreaOffset.y);
                break;
            case 7:
                image(textures.get("baron"), x + playAreaOffset.x, y + playAreaOffset.y);
                break;
            
        }
        if(hex.capital) image(textures.get("capital"), x + playAreaOffset.x, y + playAreaOffset.y);
        if(hex.castle) image(textures.get("castle"), x + playAreaOffset.x, y + playAreaOffset.y);

        if(hex.owner == currPlayer){
            if(hex.hasUnit() && hex.unitCanMove){
                image(textures.get("icon_exclamation"), x + playAreaOffset.x, y + playAreaOffset.y - (int)(hexSize * 0.8));
            }else if(hex.capital && hex.territory.getCapital().gold >= 10){
                tint(50, 100, 200);
                image(textures.get("icon_flag"), x + playAreaOffset.x, y + playAreaOffset.y - (int)(hexSize * 0.8));
                noTint();
            }
        }
        
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
    public void drawUnit(){
        if(selectedUnit != null){
            image(selectedUnit.texture, mouseX, mouseY);
        }
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
    // 3 = Palm
    public HexMap loadMap(InputStream is){
        try{
            HexMap map = null;
            //File file = new File(mapFile);
            Scanner lineScanner = new Scanner(is);

            // Load default number of players
            while(lineScanner.hasNextLine()){
                String line = lineScanner.nextLine();
                String[] lineSplit = line.split(" ");
                if(line.charAt(0) == '[') continue; // This line is the header or a comment.
                if(line.charAt(0) == '#') continue; // This line is a comment.
                if(lineSplit.length != 1) throw new Exception("Invalid map file: player line is invalid.");
                //int playerCount = Integer.parseInt(lineSplit[0]);
                break;
            }

            // Load dimensions of map
            while(lineScanner.hasNextLine()){
                String line = lineScanner.nextLine();
                String[] lineSplit = line.split(" ");
                if(line.charAt(0) == '[') continue; // This line is the header or a comment.
                if(line.charAt(0) == '#') continue; // This line is a comment.
                if(lineSplit.length != 2) throw new Exception("Invalid map file: dimension line is invalid.");
                map = new HexMap(Integer.parseInt(lineSplit[0]), Integer.parseInt(lineSplit[1]));   
                break;
            }
            if(map == null) throw new Exception("Invalid map file: size line not found.");

            // Load hex size
            while(lineScanner.hasNextLine()){
                String line = lineScanner.nextLine();
                String[] lineSplit = line.split(" ");
                if(line.charAt(0) == '[') continue; // This line is the header or a comment.
                if(line.charAt(0) == '#') continue; // This line is a comment.
                if(lineSplit.length != 1) throw new Exception("Invalid map file: hex size line is invalid.");
                hexSize = Integer.parseInt(lineSplit[0]);
                break;
            }

            // Load offset values
            while(lineScanner.hasNextLine()){
                String line = lineScanner.nextLine();
                String[] lineSplit = line.split(" ");
                if(line.charAt(0) == '[') continue; // This line is the header or a comment.
                if(line.charAt(0) == '#') continue; // This line is a comment.
                if(lineSplit.length != 2) throw new Exception("Invalid map file: offset line is invalid.");
                playAreaOffset.x = Integer.parseInt(lineSplit[0]);
                playAreaOffset.y = Integer.parseInt(lineSplit[1]);
                break;
            }

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
                x = 0;
                y++;
            }
            lineScanner.close();
            loadTextures();
            return map;
        }catch(Exception e){
            System.out.println("Error loading map: " + e.getMessage());
            return null;
        }
    }
    public HexMap loadMap(String mapFile) throws FileNotFoundException{
        return loadMap(new FileInputStream(mapFile));
    }

    public void loadTextures(){
        textures.clear();
        importTexture("background", "textures/bg.png", 32);
        importTexture("pine", "textures/pine.png", (int)(hexSize * 0.85));
        importTexture("palm", "textures/palm.png", (int)(hexSize * 0.85));
        importTexture("capital", "textures/capital.png", (int)(hexSize * 0.85));
        importTexture("peasant", "textures/peasant.png", (int)(hexSize * 0.85));
        // Create disabled peasant texture
        PImage p = textures.get("peasant");
        PGraphics g = createGraphics(p.width, p.height);
        g.beginDraw();
        g.image(p, 0, 0, p.width, p.height);
        g.filter(GRAY);
        g.endDraw();
        textures.put("peasant_disabled", g);

        importTexture("spearman", "textures/spearman.png", (int)(hexSize * 0.85));
        importTexture("knight", "textures/knight.png", (int)(hexSize * 0.85));
        importTexture("baron", "textures/baron.png", (int)(hexSize * 0.85));

        importTexture("castle", "textures/fort.png", (int)(hexSize * 0.85));
        // Create disabled castle texture
        PImage f = textures.get("castle");
        g = createGraphics(f.width, f.height);
        g.beginDraw();
        g.image(f, 0, 0, f.width, f.height);
        g.filter(GRAY);
        g.endDraw();
        textures.put("castle_disabled", g);

        importTexture("icon_flag", "icons/flag.png", (int)(hexSize * 0.75));
        importTexture("icon_exclamation", "icons/exclamation.png", (int)(hexSize * 0.75));
    }

    // ENUMS
    public enum GameState {
        MENU,
        PLAYER_COUNT,
        LOAD,
        INIT_GAME,
        NEXT_TURN,
        MAP_SELECTION,
        GAME,
        PAUSE,
        END
    } 
}  