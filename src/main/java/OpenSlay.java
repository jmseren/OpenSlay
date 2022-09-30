// OpenSlay Version: 1.0
// GNU General Public License v2.0
// Created By Jayden Serenari
// Based on the Turn Based Strategy Game "Slay" by Sean O'Connor

import processing.core.*;
import processing.data.JSONObject;
import processing.event.*;

import java.util.*;
import java.io.*;


public class OpenSlay extends PApplet {

    // Global Static Variables
    public static PFont font;
    boolean fullScreen;
    public static HashMap<String, PImage> textures = new HashMap<String, PImage>();
    public static HashMap<String, GUI> guiElements;
    public static EventHandler eventHandler = new EventHandler();
    public static int hexSize = 32;
    public static int page = 0;
    public static int campaignMaps;

    

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
        new Color(255, 30, 255),


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

    private JSONObject settingsJSON;

    // Settings
    public static void main(String[] args) {
        String[] appletArgs = new String[] { "OpenSlay" };
        PApplet.main(appletArgs);
    }
    public void settings(){
        File f = new File("./data/settings.json");
        if(!f.exists()){
            settingsJSON = loadJSONObject("settings.json");
            saveJSONObject(settingsJSON, "./data/settings.json");
        }else{
            settingsJSON = loadJSONObject("./data/settings.json");
        }
        JSONObject window = settingsJSON.getJSONObject("window");
        JSONObject maps = settingsJSON.getJSONObject("maps");
        boolean fs = window.getBoolean("fullscreen");
        if(fs) fullScreen();    
        else size(window.getInt("width"), window.getInt("height"));
        campaignMaps = maps.getInt("campaignLength");

    }
    public void setup(){
        frameRate(60);
        imageMode(CENTER);
        textAlign(CENTER, CENTER);
        strokeWeight(2);
        font = createFont("fonts/pixeloidsans.ttf", 64);
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
                TextElement title = new TextElement("OpenSlay", width/2, height/2 - 200);
                TextButton startButton = new TextButton("Campaign", width/2, height/2 - 100, 200, 50, new Event(Events.CHANGE_STATE, GameState.MAP_SELECTION));
                TextButton skirmishButton = new TextButton("Skirmish", width/2, height/2 + (50*2) - 100, 200, 50, new Event(Events.CHANGE_STATE, GameState.SKIRMISH));
                TextButton settingsButton = new TextButton("Settings", width/2, height/2 + (50*4) - 100, 200, 50, new Event(Events.CHANGE_STATE, GameState.SETTINGS_MENU));
                newGUI.put("title", title);
                newGUI.put("startButton", startButton);
                newGUI.put("skirmishButton", skirmishButton);
                newGUI.put("settingsButton", settingsButton);
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
            case GAME_OVER:
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
                // Display a row of 4 maps at a time, with arrows to scroll through them
                page = 0;

                for(int i = 0; i < 4; i++){
                    int mapNum = i+1;
                    newGUI.put(""+i, new TextButton("" + mapNum, width/2 - (2*110) + (150*i), height/2, 100, 100, new Event(Events.MAP_SELECTED, mapNum)));
                    
                }
                newGUI.put("leftArrow", new TextButton("<", width/2 - 350, height/2, 50, 50, new Event(Events.PAGE, 0)));
                newGUI.put("rightArrow", new TextButton(">", width/2 + 350, height/2, 50, 50, new Event(Events.PAGE, 1)));
                newGUI.put("backButton", new TextButton("Back", width/2, height/2 + 200, 200, 50, new Event(Events.CHANGE_STATE, GameState.MENU)));
                break;
            case SETTINGS_MENU:
                TextElement settingsTitle = new TextElement("Settings", width/2, height/2 - 200);
                TextElement fullScreenLabel = new TextElement("Full Screen (Requires Restart)", width/2, height/2 - 100);
                CheckElement fullScreenCheck = new CheckElement(width/2, height/2 - 50, 50, Events.SET_FULLSCREEN, settingsJSON.getJSONObject("window").getBoolean("fullscreen"));
                TextButton backButton = new TextButton("Confirm", width/2, height/2 + 100, 200, 50, new Event(Events.CHANGE_STATE, GameState.MENU));
                newGUI.put("title", settingsTitle);
                newGUI.put("fullScreenLabel", fullScreenLabel);
                newGUI.put("fullScreenCheck", fullScreenCheck);
                newGUI.put("backButton", backButton);
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
            playerBag.add(p, 1);
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
        // ImageButton peasantButton = new ImageButton("peasant_button", peasantTexture, (int)(width-((width * 0.25)) + (width * 0.25 / 2)), height / 10 + (height /10) * 2, peasantTexture.width, peasantTexture.height, Events.PEASANT);
        // gui.put(peasantButton.name, peasantButton);

        // Add the castle button
        PImage castleTexture = textures.get("castle_disabled");
        // ImageButton castleButton = new ImageButton("castle_button", castleTexture, (int)(width-((width * 0.25)) + (width * 0.25 / 2)), height / 10 + (height /10) * 4, castleTexture.width, castleTexture.height, Events.CASTLE);
        // gui.put(castleButton.name, castleButton);
        
        // Add the end turn button
        TextButton nextTurn = new TextButton("End Turn", (int)(width-((width * 0.25)) + (width * 0.25 / 2)), height - 100, 200, 50, new Event(Events.CHANGE_STATE, GameState.NEXT_TURN));
        gui.put("nextTurn", nextTurn);

        // Add the player indicator
        ImageElement playerIndicator = new ImageElement("player_indicator", textures.get("icon_player"), (width / 24)/2 + 10, (width / 24)/2 + 10, width / 24, width / 24);
        gui.put("player_indicator", playerIndicator);

        // Add the peasant button
        ImageElement peasantButton = new ImageElement("peasant_button", peasantTexture, (width/24)/2 + 10, height - ((width/24)/2 + 10), width/24, width/24);
        peasantButton.e = new Event(Events.PEASANT);
        peasantButton.resize = false;
        gui.put("peasant_button", peasantButton);

        // Add the castle button
        ImageElement castleButton = new ImageElement("castle_button", castleTexture, (width/24) + 10 + width/30, height - ((width/24)/2 + 10), width/24, width/24);
        castleButton.e = new Event(Events.CASTLE);
        castleButton.resize = false;
        gui.put("castle_button", castleButton);

        // Display the list of players and ai/computer status
        int yoff = 0;
        for(Player p : players){
            float x = width-(width * .25f) + (width * 0.25f / 2);
            float y = (height / 10) * 2 + yoff;
            ImageElement playerIcon = new ImageElement("", textures.get(p.ai ? "icon_computer" : "icon_player"), (int)x, (int)y, width / 24, width / 24);     
            playerIcon.tint = p.color;
            gui.put("player_icon_" + ((yoff / 100)), playerIcon);
            yoff += 100;
        }

        // Create a gold indicator
        TextButton goldIndicator = new TextButton("Gold: 0", width/2, height - width/24 + 12 , 200, 50, null);
        goldIndicator.visible = false;
        gui.put("gold_indicator", goldIndicator);



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
                                    h.code = 2;
                                    h.grave = true;
                                }
                            }
                            t.getCapital().gold = 0;
                        }
                        tcount++;
                    }else{
                        for(Hex h : t.tiles){
                            if(h.hasUnit()){
                                h.code = 2;
                                h.grave = true;
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
                                }else if(n.code == 2 && !h.grave){
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
        Player winner = null;

        for(Player p : players){
            if(p.lost) continue;
            if(winner == null){
                winner = p;
            }else{
                winner = null;
                break;
            }
        }
        if(winner != null){
            gameState = GameState.GAME_OVER;
            return;
        }
        // Set game state back
        gameState = GameState.GAME;

    }
    public void ingame(){
        if(mouseX > width - 3){
            // Scroll right
            playAreaOffset.x -= 5;
        }
        if(mouseX < 3){
            // Scroll left
            playAreaOffset.x += 5;
        }
        if(mouseY > height - 3){
            // Scroll down
            playAreaOffset.y -= 5;
        }
        if(mouseY < 3){
            // Scroll up
            playAreaOffset.y += 5;
        }
        if(refresh) refreshMap();
        ((ImageElement)(guiElements.get("player_indicator"))).tint = currPlayer.color;
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
            if(e == null) continue;
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
                    if(selectedTerritory != null && selectedUnit == null && selectedTerritory.getCapital().gold >= 15){
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
                case SET_FULLSCREEN:
                    settingsJSON.getJSONObject("window").setBoolean("fullscreen", (boolean) e.getEventData());
                    saveSettings();
                    break;
                case PAGE:
                    // Change the page of the menu
                    if((int) e.getEventData() == 0){
                        // Scroll Left
                        if(page > 0){
                            page--;
                        }
                    }else{
                        // Scroll Right
                        if(page < campaignMaps - 4){
                            page++;
                        }
                    }
                    ((TextButton)(guiElements.get("0"))).text = "" + (page + 1);
                    ((TextButton)(guiElements.get("0"))).e.setEventData(page + 1);
                    ((TextButton)(guiElements.get("1"))).text = "" + (page + 2);
                    ((TextButton)(guiElements.get("1"))).e.setEventData(page + 2);
                    ((TextButton)(guiElements.get("2"))).text = "" + (page + 3);
                    ((TextButton)(guiElements.get("2"))).e.setEventData(page + 3);
                    ((TextButton)(guiElements.get("3"))).text = "" + (page + 4);
                    ((TextButton)(guiElements.get("3"))).e.setEventData(page + 4);
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
            if(t.tiles.size() < 2 && t.getCapital() != null){
                t.getCapital().gold = 0;
                t.getCapital().capital = false;
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
                if(selectedUnit != null && h != null && h.code >= 4 && h.owner == currPlayer){
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
        // Unused
    }

    public void mouseWheel(MouseEvent event) {
        float e = event.getCount();
        switch(gameState){
            case GAME:
                if(e > 0){
                    // Zoom in
                    if(hexSize < 256) hexSize++;
                }
                if(e < 0){
                    // Zoom out
                    if(hexSize > 8) hexSize--;
                }
                break;
            default:
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
            guiElements.get("gold_indicator").visible = true;
            ((TextButton)(guiElements.get("gold_indicator"))).text = "Gold: " + gold;
            
            // Set the buttons to visible if the player has enough gold
            if(gold >= 10){
                guiElements.get("peasant_button").visible = true;
                if(gold >= 15){
                    guiElements.get("castle_button").texture = textures.get("castle_button");
                    guiElements.get("castle_button").visible = true;
                }else {
                }
            }else{
                guiElements.get("peasant_button").visible = false;
                guiElements.get("castle_button").visible = false;
            }
        }else if(selectedTerritory == null){
            guiElements.get("gold_indicator").visible = false;
            guiElements.get("peasant_button").visible = false;
            guiElements.get("castle_button").visible = false;
        }else{ 
            guiElements.get("gold_indicator").visible = true;
        }

        

        
        

    }
    public void drawGUI(){
        if(gameState == GameState.GAME){
            drawMap(gameMap);
            drawToolBar();
            drawUnit();
        }
        for(GUI g : guiElements.values()){
            if(g.visible) g.draw(this);
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
        int imageSize = (int)(hexSize * 0.85);

        fill(hex.color.toProcessingColor());
        polygon(x + playAreaOffset.x, y + playAreaOffset.y, hexSize, 6, selected);
        switch(hex.code){
            case 2:
                if(!hex.grave) image(textures.get("pine"), x + playAreaOffset.x, y + playAreaOffset.y, imageSize, imageSize);
                else image(textures.get("grave"), x + playAreaOffset.x, y + playAreaOffset.y, imageSize, imageSize);
                break;
            case 3:
                image(textures.get("palm"), x + playAreaOffset.x, y + playAreaOffset.y, imageSize, imageSize);
                break;
            case 4:
                image(textures.get("peasant"), x + playAreaOffset.x, y + playAreaOffset.y, imageSize, imageSize);
                break;
            case 5:
                image(textures.get("spearman"), x + playAreaOffset.x, y + playAreaOffset.y, imageSize, imageSize);
                break;
            case 6:
                image(textures.get("knight"), x + playAreaOffset.x, y + playAreaOffset.y, imageSize, imageSize);
                break;
            case 7:
                image(textures.get("baron"), x + playAreaOffset.x, y + playAreaOffset.y, imageSize, imageSize);
                break;
            
        }
        if(hex.capital) image(textures.get("capital"), x + playAreaOffset.x, y + playAreaOffset.y, imageSize, imageSize);
        if(hex.castle) image(textures.get("castle"), x + playAreaOffset.x, y + playAreaOffset.y, imageSize, imageSize);

        if(hex.owner == currPlayer){
            imageSize = (int)(hexSize * 0.75);
            if(hex.hasUnit() && hex.unitCanMove){
                image(textures.get("icon_exclamation"), x + playAreaOffset.x, y + playAreaOffset.y - imageSize, imageSize, imageSize);
            }else if(hex.capital && hex.territory.getCapital().gold >= 10){
                tint(50, 100, 200);
                image(textures.get("icon_flag"), x + playAreaOffset.x, y + playAreaOffset.y - imageSize, imageSize, imageSize);
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

    // Save the settings in the classpath
    public boolean saveSettings() {
        try{
            File f = new File("./data/settings.json");
            FileWriter fw = new FileWriter(f);
            fw.write(settingsJSON.toString());
            fw.close();
        }catch(Exception e){
            System.out.println("Error saving settings: " + e);
            return false;
        }
        return true;
    }

    // Asset loading
    public void importTexture(String name, String path, int size){
        PImage img = loadImage(path);
        if(size != 0) img.resize(size, size);
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
        importTexture("pine", "textures/pine.png", 0);
        importTexture("palm", "textures/palm.png", 0);
        importTexture("capital", "textures/capital.png", 0);
        importTexture("capital", "textures/capital.png", 0);
        importTexture("grave", "textures/grave.png", 128);
        importTexture("peasant_button", "textures/peasant.png", (width/40));
        importTexture("castle_button", "textures/fort.png", (width/40));
        // Create disabled peasant texture
        PImage p = textures.get("peasant_button");
        PGraphics g = createGraphics(p.width, p.height);
        g.beginDraw();
        g.image(p, 0, 0, p.width, p.height);
        g.filter(GRAY);
        g.endDraw();
        textures.put("peasant_disabled", g);

        importTexture("spearman", "textures/spearman.png", 0);
        importTexture("knight", "textures/knight.png", 0);
        importTexture("baron", "textures/baron.png", 0);

        // importTexture("castle", "textures/fort.png", (int)(hexSize * 0.85));
        // Create disabled castle texture
        PImage f = textures.get("castle_button");
        g = createGraphics(f.width, f.height);
        g.beginDraw();
        g.image(f, 0, 0, f.width, f.height);
        g.filter(GRAY);
        g.endDraw();
        textures.put("castle_disabled", g);

        importTexture("icon_flag", "icons/flag.png", 0);
        importTexture("icon_exclamation", "icons/exclamation.png", 0);
        importTexture("icon_computer", "icons/cpu.png", width / 20);
        importTexture("icon_player", "icons/player.png", width / 25);

        PImage i = textures.get("peasant_button").get();
        i.resize((int)(hexSize * 0.85), (int)(hexSize * 0.85));
        textures.put("peasant", i);
        i = textures.get("castle_button").get();
        i.resize((int)(hexSize * 0.85), (int)(hexSize * 0.85));
        textures.put("castle", i);
    }  

    // ENUMS
    public enum GameState {
        MENU,
        SKIRMISH,
        SETTINGS_MENU,
        PLAYER_COUNT,
        LOAD,
        INIT_GAME,
        NEXT_TURN,
        MAP_SELECTION,
        GAME,
        GAME_OVER,
        PAUSE,
        END
    } 
}  