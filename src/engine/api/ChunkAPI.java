package engine.api;

import engine.math.Dimension;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChunkAPI {
    private Vector3f openGlPosition;
    private final List<EntityAPI> chunk;
    private final int chunkSize;
    private final Dimension tileSize;
    private final int tilesPerRow;
    private boolean shouldRender = false;

    private ChunkAPI(int chunkSize, List<EntityAPI> chunk, Dimension tileSize, int tilesPerRow) {
        this.chunkSize = chunkSize;
        this.chunk = chunk;
        this.tileSize = tileSize;
        this.tilesPerRow = tilesPerRow;
    }

    public static ChunkAPI newInstance(int chunkSize, List<EntityAPI> chunk, Dimension tileSize, int tilesPerRow) {
        if (chunk.size() != chunkSize) return null;
        List<EntityAPI> listClone = new ArrayList<>();
        while (listClone.size() != chunk.size()) listClone.add(EntityAPI._doNotUse());
        Collections.copy(listClone, chunk);
        return new ChunkAPI(chunkSize, listClone, tileSize, tilesPerRow);
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public List<EntityAPI> getChunk() {
        return chunk;
    }

    public Vector3f getOpenGlPosition() {
        return openGlPosition;
    }

    public void setOpenGlPosition(Vector3f openGlPosition) {
        this.openGlPosition = openGlPosition;
    }

    public Dimension getTileSize() {
        return tileSize;
    }

    public boolean shouldRender() {
        return shouldRender;
    }

    public void setShouldRender(boolean shouldRender) {
        this.shouldRender = shouldRender;
    }

    public int getTilesPerRow() {
        return tilesPerRow;
    }
}
