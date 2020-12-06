package examples.dungeon.system;

import engine.api.ChunkAPI;
import engine.api.ChunkManagerAPI;
import engine.api.EntityAPI;
import engine.math.Dimension;
import engine.texture.SpriteSheet;
import examples.dungeon.generation.Location;
import examples.dungeon.generation.WorldManager;
import examples.dungeon.objects.Actor;
import examples.dungeon.objects.Camera;
import examples.dungeon.objects.Player;
import examples.dungeon.tiles.Tile;
import examples.dungeon.tiles.VoidTile;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.util.*;
import java.util.logging.Logger;

public class Render {

    // @TODO - Optimize rendering

    private static final Logger LOG = Logger.getLogger(Render.class.getName());

    private List<EntityAPI> entities;
    private final Actor player;
    private Dimension cameraDimensions;
    private Dimension tileSize;
    private List<List<Tile>> previousValidChunk = new ArrayList<>();
    private List<List<Tile>> previousChunk = new ArrayList<>();
    private Actor previousPlayerObject;
    private boolean edges;
    private List<EntityAPI> extraLayer = new ArrayList<>();
    private Turn turn;
    private List<ChunkManagerAPI> chunkManagersAPI;
    private List<Actor> actors = new ArrayList<>();
    private boolean preChunkWorld = false;
    private int extraTilesCounter = 0;

    public Render(List<EntityAPI> entities, Turn turn, Actor player, Dimension cameraDimensions, Dimension tileSize) {
        this.entities = entities;
        this.player = player;
        this.cameraDimensions = cameraDimensions;
        this.tileSize = tileSize;
        this.turn = turn;
    }

    public Render(List<EntityAPI> entities, List<ChunkManagerAPI> chunkManagerAPI, Turn turn, Actor player, Dimension cameraDimensions, Dimension tileSize) {
        this.entities = entities;
        this.player = player;
        this.cameraDimensions = cameraDimensions;
        this.tileSize = tileSize;
        this.turn = turn;
        this.chunkManagersAPI = chunkManagerAPI;
    }

    public void setCameraDimensions(Dimension cameraDimensions) {
        this.cameraDimensions = cameraDimensions;
    }

    public void setTileSize(Dimension tileSize) {
        this.tileSize = tileSize;
    }

    public void render() {
        try {
            if (chunkManagersAPI != null) {
                // rendering tiles batch
                renderChunk(getPlayerChunk(player), chunkManagersAPI.get(0));
            } else renderChunkFromMap(getPlayerChunk(player), player.getCurrentLocation().getMap());
        } catch (CloneNotSupportedException cloneNotSupportedException) {
            LOG.severe(cloneNotSupportedException.getMessage());
        }
    }

    public void renderTile(List<List<Tile>> map, Tile tile, int x, int y, int xGL, int yGL) {
        EntityAPI entityAPI = map.get(x).get(y).getEntityObject();
        entityAPI.setShouldRender(true);
        entityAPI.translate(new Vector3f(
                (float) tileSize.width * xGL,
                (float) tileSize.height * yGL, 0));
        entityAPI.setSpriteSheet(tile.getSprite());
        if (tile.isHidden() && tile.getLight() == null && !tile.isSelected())
            entityAPI.setRgbVector(new Vector3f(0.2f, 0.2f, 0.2f));
        else if (!tile.isHidden() && tile.getLight() == null && !tile.isSelected()) entityAPI.setRgbVector(null);
        if (tile.isSelected()) entityAPI.setRgbVector(new Vector3f(1.0f, 1.0f, 0));
        if (tile.getLight() != null && !tile.isSelected()) entityAPI.setRgbVector(tile.getLight());
        if (tile.getActor() != null && tile.getActor().getEntityObject() != null && !tile.getActor().getType().equals("light")) {
            int depth = 1;
            if (tile.getActor().getType().equals("player")) depth = 2;
            EntityAPI object = tile.getActor().getEntityObject();
            object.translate(new Vector3f(
                    (float) tileSize.width * xGL,
                    (float) tileSize.height * yGL, depth));
            object.setShouldRender(true);
        }
    }

    public void initRender(List<List<Tile>> map) {
        for (int x = 0; x < map.size(); x++) {
            for (int y = 0; y < map.get(x).size(); y++) {
                EntityAPI entityAPI = new EntityAPI(map.get(x).get(y).getSprite(),
                        new Vector3f(0, 0, 0),
                        new Dimension(tileSize.width, tileSize.height),
                        new Vector2f(0, 0));

                entityAPI.setRenderSpriteRetroCompatibility(false);
                map.get(x).get(y).setEntityObject(entityAPI);
                entityAPI.setShouldRender(false);
                this.entities.add(entityAPI);

                if (map.get(x).get(y).getActor() != null
                        && !map.get(x).get(y).getActor().getType().equals("light")) {
                    Actor object = map.get(x).get(y).getActor();

                    EntityAPI entityAPIForObject = new EntityAPI(new SpriteSheet(),
                            new Vector3f(0, 0, 1),
                            new Dimension(object.getWidth(), object.getHeight()),
                            new Vector2f(0, 0));

                    entityAPIForObject.setProp(object.getType(), true);

                    entityAPIForObject.setRenderSpriteRetroCompatibility(false);
                    object.setEntityObject(entityAPIForObject);
                    entityAPI.setRenderSpriteRetroCompatibility(false);
                    entityAPIForObject.setShouldRender(false);
                    if (entityAPIForObject.getProp("camera") != null) {
                        entityAPIForObject.setSpriteSheet(map.get(x).get(y).getSprite());
                    } else {
                        entityAPIForObject.setSpriteSheet(map.get(x).get(y).getActor().getSprite());
                    }
                    this.entities.add(entityAPIForObject);
                }
            }
        }
        if (chunkManagersAPI != null && preChunkWorld) {
            int pace = 0;
            int step = 400;
            int tilesPerColumn = 20;
            int tilesPerColumnDivisor = tilesPerColumn;

            boolean done = false;
            Set<Integer> ids = new HashSet<>();
            while (tilesPerColumnDivisor != map.size()) {
                while (!done) {
                    List<EntityAPI> entityPicker = new ArrayList<>();
                    for (int i = pace; i < i + step; i++) {
                        if (i < this.entities.size()) {
                            int col = i / map.size();
                            int mappedIndex = i - (col * map.size());
                            if (mappedIndex / tilesPerColumnDivisor <= 0 && !ids.contains(this.entities.get(i).getId())) {
                                entityPicker.add(this.entities.get(i));
                                ids.add(this.entities.get(i).getId());
                            }
                        } else {
                            done = true;
                            break;
                        }
                        if (entityPicker.size() % step == 0 && !entityPicker.isEmpty()) {
                            pace += step;
                            ChunkAPI chunkAPI = ChunkAPI.newInstance(step, entityPicker, new Dimension(tileSize.width, tileSize.height), tilesPerColumn);
                            chunkAPI.setOpenGlPosition(new Vector3f(-600, -400, 0));
                            chunkManagersAPI.get(0).addChunk(chunkAPI);
                            entityPicker.clear();
                        }
                    }
                }
                tilesPerColumnDivisor += tilesPerColumn;
                done = false;
            }
        }
    }

    public void renderChunk(List<List<Tile>> chunkToRender, ChunkManagerAPI manager) {
        int validChunkNumber = cameraDimensions.width - 1;
        if (chunkToRender.size() != validChunkNumber) return;
        List<EntityAPI> entitiesToRender = new ArrayList<>();
        manager.getChunks().clear();
        int latestEffectIndex = 0;
        float[] effects = new float[(cameraDimensions.width * cameraDimensions.height * 5) + extraTilesCounter];

        int xIt = 0;
        for (int i = -cameraDimensions.width / 2; i < cameraDimensions.width / 2; i++) {
            int yIt = 0;
            for (int j = -cameraDimensions.height / 2; j < cameraDimensions.height / 2; j++) {
                if (xIt >= chunkToRender.size() || yIt >= chunkToRender.get(xIt).size()) continue;
                Map<Object, Object> props = new HashMap<>();
                props.put("xNormalized", (float) i);
                props.put("yNormalized", (float) -j);
                chunkToRender.get(xIt).get(yIt).setScriptProperties(props);
                yIt++;
            }
            xIt++;
        }

        List<List<float[]>> effectsPerTileX = new ArrayList<>();
        for (List<Tile> tiles : chunkToRender) {
            List<float[]> effectsPerTileY = new ArrayList<>();
            for (Tile tile : tiles) {

                if (tile.isHidden() || tile.isSelected()) {
                    Vector3f color = null;

                    if (tile.isSelected()) {
                        color = new Vector3f(1.0f, 1.0f, 0);
                        effectsPerTileY.add(new float[]{1.0f, 1.0f, 0f});
                    }
                    else if (tile.isHidden() && tile.getType() == TileMapping.ROOM.getTile()) {
                        color = new Vector3f(0.2f, 0.2f, 0.2f);
                        effectsPerTileY.add(new float[]{0.0f, 0.0f, 0.0f});
                    } else if (tile.isHidden()) {
                        color = new Vector3f(0.2f, 0.2f, 0.2f);
                        effectsPerTileY.add(new float[]{0.4f, 0.4f, 0.4f});
                    }

                    if (tile.getScriptProperties().get("xNormalized") != null && tile.getScriptProperties().get("yNormalized") != null) {
                        effects[latestEffectIndex] = (float) tile.getScriptProperties().get("xNormalized");
                        effects[latestEffectIndex + 1] = (float) tile.getScriptProperties().get("yNormalized");
                        effects[latestEffectIndex + 2] = color.x;
                        effects[latestEffectIndex + 3] = color.y;
                        effects[latestEffectIndex + 4] = color.z;
                        latestEffectIndex += 5;
                    }
                } else {
                    effectsPerTileY.add(new float[]{1f, 1f, 1f});
                }
                if (tile.getActor() == null) {
                    entitiesToRender.add(tile.getEntityObject());
                } else {
                    tile.getActor().getEntityObject().setShouldRender(true);
                    entitiesToRender.add(tile.getActor().getEntityObject());
                    if (!actors.contains(tile.getActor())) actors.add(tile.getActor());
                }
            }
            effectsPerTileX.add(effectsPerTileY);
        }
        ChunkAPI chunkAPI = ChunkAPI.newInstance(entitiesToRender.size(), entitiesToRender,
                new Dimension(tileSize.width, tileSize.height), chunkToRender.size());
        chunkAPI.setOpenGlPosition(new Vector3f(-700, -450, 0));
        chunkAPI.setShouldRender(true);
        chunkAPI.setRgbTileEffects(effects);
        chunkAPI.setCameraDimensions(cameraDimensions);
        chunkAPI.setEffectsPerTile(effectsPerTileX);
        manager.addChunk(chunkAPI);
    }

    public void renderChunks(ChunkManagerAPI manager) {
        List<ChunkAPI> chunkToRender = new ArrayList<>();
        boolean playerFound = false;
        for (ChunkAPI chunk : manager.getChunks()) {
            if (playerFound) break;
            for (EntityAPI entityAPI : chunk.getChunk()) {
                if (entityAPI.getProp("player") != null || entityAPI.getProp("camera") != null) {
                    chunkToRender.add(chunk);
                    playerFound = true;
                    break;
                }
            }
        }
        if (!chunkToRender.isEmpty()) {
            chunkToRender.get(0).setShouldRender(true);
        }
    }

    public void renderChunkFromMap(List<List<Tile>> chunkToRender, List<List<Tile>> map) {
        int x = 0;
        for (int i = -cameraDimensions.width / 2; i < cameraDimensions.width / 2; i++) {
            if (x >= cameraDimensions.width - 1) break;
            int y = 0;
            for (int j = -cameraDimensions.height / 2; j < cameraDimensions.height / 2; j++) {
                if (y >= cameraDimensions.height - 1) break;
                Tile tile = chunkToRender.get(x).get(y);
                renderTile(map, tile, tile.getPositionX(), tile.getPositionY(), i, j);
                y++;
            }
            x++;
        }
    }

    public List<List<Tile>> getPlayerChunk(Actor player) throws CloneNotSupportedException {
        extraTilesCounter = 0;
        Location location = player.getCurrentLocation();
        Tile playerTile = player.getCurrentTile();
        List<List<Tile>> tilesToRender = new ArrayList<>();
        int validChunkNumber = cameraDimensions.width - 1;

        for (int i = 0; i < location.getMapWidth(); i++) {
            List<Tile> chunk = new ArrayList<>();
            for (int j = 0; j < location.getMapHeight(); j++) {
                if (i > playerTile.getPositionX() - (cameraDimensions.width / 2) && i < playerTile.getPositionX() + (cameraDimensions.width / 2) &&
                        j > playerTile.getPositionY() - (cameraDimensions.height / 2) && j < playerTile.getPositionY() + (cameraDimensions.height / 2)) {

                    Tile tileToAdd = WorldManager.tryGetTile(location.getXGlobal(), location.getYGlobal(), location.getZGlobal(), i, j);
                    if (tileToAdd != null) {
                        tileToAdd.getEntityObject().setShouldRender(true);
                        chunk.add(tileToAdd);
                    }

                } else {
                    Tile tileToRemove = WorldManager.tryGetTile(location.getXGlobal(), location.getYGlobal(), location.getZGlobal(), i, j);
                    if (tileToRemove != null) {
                        tileToRemove.getEntityObject().setShouldRender(false);
                        if (tileToRemove.getActor() != null) {
                            tileToRemove.getActor().getEntityObject().setShouldRender(false);
                        }
                    }
                }
            }
            if (chunk.isEmpty()) continue;
            tilesToRender.add(chunk);
        }
        // Adding possibly missing tiles
        for (List<Tile> tiles : tilesToRender) {
            if (tiles.size() < validChunkNumber) {
                int numTiles = validChunkNumber - tiles.size();
                for (int i = 0; i < numTiles; i++) {
                    Tile voidTile = new VoidTile();
                    EntityAPI entityAPI = EntityAPI._emptyInstance();
                    entityAPI.setShouldRender(false);
                    entityAPI.setSpriteSheet(voidTile.getSprite());
                    voidTile.setEntityObject(entityAPI);
                    extraTilesCounter++;
                    tiles.add(voidTile);
                }
            }
        }
        // Adding possibly missing layers of tiles
        if (tilesToRender.size() < validChunkNumber) {
            int numLayers = validChunkNumber - tilesToRender.size();
            for (int i = 0; i < numLayers; i++) {
                List<Tile> newChunk = new ArrayList<>();
                for (int j = 0; j < validChunkNumber; j++) {
                    Tile voidTile = new VoidTile();
                    EntityAPI entityAPI = EntityAPI._emptyInstance();
                    entityAPI.setShouldRender(false);
                    entityAPI.setSpriteSheet(voidTile.getSprite());
                    voidTile.setEntityObject(entityAPI);
                    newChunk.add(voidTile);
                    extraTilesCounter++;
                }
                tilesToRender.add(newChunk);
            }
        }
        // Hiding all tiles
        for (List<Tile> tiles : tilesToRender) {
            for (Tile tile : tiles) {
                if (tile.getLight() == null)
                    tile.setHidden(true);
            }
        }

        if (player.getType().equals("player")) {
            ((Player) player).setChunk(tilesToRender);
            for (Actor actor : turn.getJobs()) {
                if (actor.isEmitsLight())
                    actor.applyFOV(tilesToRender);
            }
            return player.rayCastingFOV(tilesToRender);
        }
        // if free camera-mode
        else {
            ((Camera) player).setChunk(tilesToRender);
            for (Actor actor : turn.getJobs()) {
                if (!actor.getType().equals("camera"))
                    actor.applyFOV(tilesToRender);
            }
            return tilesToRender;
        }
    }

    public Map<String, Integer> checkForDuplicateTile(List<List<Tile>> chunk) {
        Map<String, Integer> duplicate = new HashMap<>();
        Map<String, Integer> duplicateFinal = new HashMap<>();
        for (List<Tile> section : chunk) {
            for (Tile tile : section) {
                if (duplicate.get(tile.getPositionX() + "," + tile.getPositionY()) == null) {
                    duplicate.put(tile.getPositionX() + "," + tile.getPositionY(), 1);
                } else {
                    int occ = duplicate.get(tile.getPositionX() + "," + tile.getPositionY());
                    duplicate.put(tile.getPositionX() + "," + tile.getPositionY(), ++occ);
                }
            }
        }
        for (Map.Entry<String, Integer> entry : duplicate.entrySet()) {
            if (entry.getValue() > 1) duplicateFinal.put(entry.getKey(), entry.getValue());
        }
        return duplicateFinal;
    }
}