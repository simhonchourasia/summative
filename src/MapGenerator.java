import java.util.Random;

public class MapGenerator {

    static Random rand;
    MapComponent[][][] map;
    Tile spawnTile, rockTile;
    int h, w;

    public MapGenerator(int seed) {
        rand = new Random(seed);
    }

    public void generate(int height, int width) {
        map = new MapComponent[2][height][width];
        h = height;
        w = width;

        //Fill the thing with empty crap so it doesn't bother you and say huRr huRr NullPointerException
        for (int l = 0; l < 2; l++) {
            for (int r = 0; r < h; r++) {
                for (int c = 0; c < w; c++) {
                    map[l][r][c] = new MapComponent(MapComponent.NULL);
                }
            }
        }

        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                map[Map.GROUND_LAYER][r][c] = new MapComponent(MapComponent.GRASS);
            }
        }

        //Initial lake point generation
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                if (chance(0.001)) {
                    map[Map.GROUND_LAYER][r][c] = new MapComponent(MapComponent.WATER); //randomly spawning water
                    map[Map.ITEM_LAYER][r][c] = new MapComponent(MapComponent.WATER);
                }
            }
        }

        //Lake generation based on initial points
        for(int i = 0; i < 70; i++) {
            //Temp
            MapComponent[][][] temp = new MapComponent[2][h][w];
            copyMap(map, temp); //copy everything from map to temp
            //Check map, write to temp
            for (int r = 0; r < h; r++) {
                for (int c = 0; c < w; c++) {
                    {
                        int touching = countTouching(map, Map.GROUND_LAYER, r, c, MapComponent.WATER);
                        if (chance(touching / 10.0)){
                            temp[Map.GROUND_LAYER][r][c] = new MapComponent(MapComponent.WATER);
                            temp[Map.ITEM_LAYER][r][c] = new MapComponent(MapComponent.WATER);
                        }
                    }
                }
            }
            copyMap(temp, map); //copy everything from temp to map
        }

        //Smoothen water generation
        for(int i = 0; i < 10; i++) {
            //Temp
            MapComponent[][][] temp = new MapComponent[2][h][w];
            copyMap(map, temp);
            for (int r = 0; r < h; r++) {
                for (int c = 0; c < w; c++) {
                    if(map[Map.GROUND_LAYER][r][c].getMapComponentID() == MapComponent.WATER &&
                            countTouching(map, Map.GROUND_LAYER, r, c, MapComponent.GRASS) >= 3 &&
                            chance(0.2)) {
                        temp[Map.GROUND_LAYER][r][c] = new MapComponent(MapComponent.GRASS);
                        temp[Map.ITEM_LAYER][r][c] = new MapComponent(MapComponent.NULL);
                    }
                    else if(map[Map.GROUND_LAYER][r][c].getMapComponentID() == MapComponent.GRASS &&
                            countTouching(map, Map.GROUND_LAYER, r, c, MapComponent.WATER) >= 3 &&
                            chance(0.8)) {
                        temp[Map.GROUND_LAYER][r][c] = new MapComponent(MapComponent.WATER);
                        temp[Map.ITEM_LAYER][r][c] = new MapComponent(MapComponent.WATER);
                    }
                }
            }
            copyMap(temp, map);
        }

        //Random dirt spots
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                if (chance(0.1) && map[Map.GROUND_LAYER][r][c].getMapComponentID() == MapComponent.GRASS) { //if grass and chance(0.1)
                    map[Map.GROUND_LAYER][r][c] = new MapComponent(MapComponent.SOIL);
                }
            }
        }

        //Trees and bushes
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                if (chance(0.08) && map[Map.GROUND_LAYER][r][c].getMapComponentID() != MapComponent.WATER) { //if grass and chance(0.1)
                    if(chance(0.5)) map[Map.ITEM_LAYER][r][c] = new MapComponent(MapComponent.SMALL_TREE);
                    else map[Map.ITEM_LAYER][r][c] = new MapComponent(MapComponent.SMALL_BUSH);
                }
            }
        }

        //Rocks
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                if (chance(0.01) && map[Map.GROUND_LAYER][r][c].getMapComponentID() != MapComponent.WATER) { //if grass and chance(0.1)
                    map[Map.ITEM_LAYER][r][c] = new MapComponent(MapComponent.ROCKS);
                }
            }
        }

        //Generate spawn tile
        boolean spawnTileFound = false;
        while(!spawnTileFound) {
            spawnTileFound = true; //True until proven otherwise
            int row = rand.nextInt(h / 2) + h / 4; //Somewhere in the middle
            int column = rand.nextInt(w / 2) + w / 4;
            spawnTile = new Tile(row, column);
            for(int r = spawnTile.getRow(); r < spawnTile.getRow() + 4; r++) { //Find a 4x3 area where there is no water on the base layer
                for(int c = spawnTile.getColumn(); c < spawnTile.getColumn() + 3; c++) {
                    if(map[Map.GROUND_LAYER][r][c].getMapComponentID() == MapComponent.WATER) spawnTileFound = false;
                }
            }
        }

        //Plane
        for(int r = spawnTile.getRow() + 1; r < spawnTile.getRow() + 2; r++) { //4 by 2 region of plane
            for(int c = spawnTile.getColumn(); c < spawnTile.getColumn() + 4; c++) {
                map[Map.ITEM_LAYER][r][c] = new MapComponent(MapComponent.FILLED_NULL);
            }
        }
        map[Map.ITEM_LAYER][spawnTile.getRow() + 1][spawnTile.getColumn()] = new MapComponent(MapComponent.PLANE); //top left corner of plane

        // Wise rock
        while(true) {
            int row = spawnTile.getRow() + rand.nextInt(11) - 5; // up to 5 away
            int column = spawnTile.getColumn() + rand.nextInt(11) - 5;
            if(map[Map.GROUND_LAYER][row][column].getMapComponentID() != MapComponent.WATER && //if ground not water and what is above it empty
                    map[Map.ITEM_LAYER][row][column].getMapComponentID() == MapComponent.NULL) {
                map[Map.ITEM_LAYER][row][column] = new MapComponent(MapComponent.WISE_ROCK);
                rockTile = new Tile(row, column);

                break; //exit the loop
            }
        }

        //Radio parts
        while(true) {
            int row = spawnTile.getRow() + rand.nextInt(31) - 15; // up to 15 away
            int column = spawnTile.getColumn() + rand.nextInt(31) - 15;
            if(map[Map.GROUND_LAYER][row][column].getMapComponentID() != MapComponent.WATER && //if ground not water and what is above it empty
                    map[Map.ITEM_LAYER][row][column].getMapComponentID() == MapComponent.NULL) {
                map[Map.ITEM_LAYER][row][column] = new MapComponent(MapComponent.ANTENNA);
                System.out.println("Antenna: " + "(" + column + ", " + row + ")");
                break; //exit the loop
            }
        }
        while(true) {
            int row = spawnTile.getRow() + rand.nextInt(31) - 15; // up to 15 away
            int column = spawnTile.getColumn() + rand.nextInt(31) - 15;
            if(map[Map.GROUND_LAYER][row][column].getMapComponentID() != MapComponent.WATER && //if ground not water and what is above it empty
                    map[Map.ITEM_LAYER][row][column].getMapComponentID() == MapComponent.NULL) {
                map[Map.ITEM_LAYER][row][column] = new MapComponent(MapComponent.TRANSMITTER);
                System.out.println("Transmitter: " + "(" + column + ", " + row + ")");
                break; //exit the loop
            }
        }
        while(true) {
            int row = spawnTile.getRow() + rand.nextInt(31) - 15; // up to 15 away
            int column = spawnTile.getColumn() + rand.nextInt(31) - 15;
            if(map[Map.GROUND_LAYER][row][column].getMapComponentID() != MapComponent.WATER && //if ground not water and what is above it empty
                    map[Map.ITEM_LAYER][row][column].getMapComponentID() == MapComponent.NULL) {
                map[Map.ITEM_LAYER][row][column] = new MapComponent(MapComponent.CIRCUIT_BOARD);
                System.out.println("Circuit board: " + "(" + column + ", " + row + ")");
                break; //exit the loop
            }
        }

        //Monsters
        for(int r = 0; r < height; r++) {
            for(int c = 0; c < width; c++) {
                if(chance(0.003) && // 1/33 chance of spawning
                        map[Map.GROUND_LAYER][r][c].getWalkable() && //Spawn on walkable land
                        map[Map.ITEM_LAYER][r][c].getWalkable() && //Do not spawn inside an item
                        (Math.abs(r - spawnTile.getRow()) > 20 || Math.abs(c - spawnTile.getColumn()) > 20)) { //At least one coordinate has to be >20 blocks away
                    map[Map.ITEM_LAYER][r][c] = new MapComponent(MapComponent.MONSTER, 100, 1, 1); //You're a monster bro
                    Map.totalMonsters++;
                }
            }
        }

        // Boss Monster
        map[Map.ITEM_LAYER][8][8] = new MapComponent(MapComponent.BOSS_MONSTER, 1000, 14, 1);
        map[Map.ITEM_LAYER][height - 8][8] = new MapComponent(MapComponent.BOSS_MONSTER2, 5000, 30, 1);
        map[Map.ITEM_LAYER][height - 8][width - 8] = new MapComponent(MapComponent.BOSS_MONSTER3, 60, 30, 3);
        map[Map.ITEM_LAYER][8][width - 8] = new MapComponent(MapComponent.BOSS_MONSTER4, 200, 1, 5);


        // Rabbits
        for(int r = 0; r < height; r++) {
            for(int c = 0; c < width; c++) {
                if(chance(0.003) && // 1/33 chance of spawning
                        map[Map.GROUND_LAYER][r][c].getWalkable() && //Spawn on walkable land
                        map[Map.ITEM_LAYER][r][c].getWalkable() && //Do not spawn inside an item
                        (Math.abs(r - spawnTile.getRow()) > 20 || Math.abs(c - spawnTile.getColumn()) > 20)) { //At least one coordinate has to be >20 blocks away
                    map[Map.ITEM_LAYER][r][c] = new MapComponent(MapComponent.RABBIT, 50, 0, 0);
                }
            }
        }

        // Birds
        for(int r = 0; r < height; r++) {
            for(int c = 0; c < width; c++) {
                if(chance(0.003) && // 1/33 chance of spawning
                        map[Map.GROUND_LAYER][r][c].getWalkable() && //Spawn on walkable land
                        map[Map.ITEM_LAYER][r][c].getWalkable() && //Do not spawn inside an item
                        (Math.abs(r - spawnTile.getRow()) > 20 || Math.abs(c - spawnTile.getColumn()) > 20)) { //At least one coordinate has to be >20 blocks away
                    map[Map.ITEM_LAYER][r][c] = new MapComponent(MapComponent.BIRD, 50, 0, 0);
                }
            }
        }



        // String
        for(int r = 0; r < height; r++) {
            for(int c = 0; c < width; c++) {
                if(chance(0.01) && // 1/100 chance of spawning
                        map[Map.GROUND_LAYER][r][c].getWalkable() && //Spawn on walkable land
                        map[Map.ITEM_LAYER][r][c].getWalkable() && //Do not spawn inside an item
                        (Math.abs(r - spawnTile.getRow()) > 10 || Math.abs(c - spawnTile.getColumn()) > 10)) { //At least one coordinate has to be >10 blocks away
                    map[Map.ITEM_LAYER][r][c] = new MapComponent(MapComponent.STRING_GROUNDED, 30, 0, 0);
                }
            }
        }


    }

    private boolean chance(double probability) {
        if(rand.nextInt(10000) < probability * 10000) return true; //Limited accuracy but it works for this purpose
        return false;
    }

    //maybe we can use this later???
    private int surrounding(int layer, int row, int column, int MapComponentID, int radius) {
        int count = 0;
        for(int r = row - radius; r <= row + radius; r++) {
            for(int c = column - radius; c <= column + radius; c++) {
                try {
                    if(map[layer][r][c].getMapComponentID() == MapComponentID) count++;
                } catch(ArrayIndexOutOfBoundsException ex) {}
            }
        }
        return count;
    }

    private int countTouching(MapComponent[][][] m, int layer, int row, int column, int MapComponentID) {
        int count = 0;
        try {
            if (m[layer][row + 1][column].getMapComponentID() == MapComponentID) count++;
        } catch(ArrayIndexOutOfBoundsException ex) {}
        try {
            if(m[layer][row - 1][column].getMapComponentID() == MapComponentID) count++;
        } catch(ArrayIndexOutOfBoundsException ex) {}
        try {
            if(m[layer][row][column + 1].getMapComponentID() == MapComponentID) count++;
        } catch(ArrayIndexOutOfBoundsException ex) {}
        try {
            if(m[layer][row][column - 1].getMapComponentID() == MapComponentID) count++;
        } catch(ArrayIndexOutOfBoundsException ex) {}
        return count;
    }

    public void copyMap(MapComponent[][][] original, MapComponent[][][] copy) {
        for(int l = 0; l < 2; l++) {
            for (int r = 0; r < h; r++) {
                for (int c = 0; c < w; c++) {
                    copy[l][r][c] = new MapComponent(original[l][r][c].getMapComponentID());
                }
            }
        }
    }

    public MapComponent[][][] getMap() {
        return map;
    }
    public Tile getSpawnTile() {
        return spawnTile;
    }

    public Tile getRockTile() {
        return rockTile;
    }

}
