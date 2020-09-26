package engine.meshes;

import engine.texture.SpriteSheet;

import java.awt.*;

public class ChunkMesh {
    private final float tileSizeX;
    private final float tileSizeY;
    private final int numberOfTiles;
    private final int numberOfCoordinatesPerPoint = 3;
    private final int numberOfPointsPerSquare = 4;
    private final int tilesPerRow;
    private SpriteSheet[] spriteSheets;

    private float[] mesh;
    private int[] indexes;
    private float[] textureCoordinates;

    private float xPos;
    private float yPos;

    private boolean shouldRender = false;

    public ChunkMesh(float tileSizeX, float tileSizeY, int tilesPerRow, int numberOfTiles, float xPos, float yPos, SpriteSheet[] sprites, boolean shouldRender) {
        this.tileSizeX = tileSizeX;
        this.tileSizeY = tileSizeY;
        this.tilesPerRow = tilesPerRow;
        this.xPos = xPos;
        this.yPos = yPos;
        this.numberOfTiles = numberOfTiles;
        this.spriteSheets = sprites;
        this.shouldRender = shouldRender;
        this.generate();
    }

    private void generate() {
        mesh = new float[numberOfTiles * numberOfCoordinatesPerPoint * numberOfPointsPerSquare];
        indexes = new int[numberOfTiles * 6];
        textureCoordinates = new float[numberOfTiles * 8];

        float startX = 0;
        float startY = 0;
        int tilesInRow = 0;

        for (int i = 0; i < mesh.length - 11; i += 12) {
            float[] points = new float[]{
                    startX, startY, 0,
                    startX + tileSizeX, startY, 0,
                    startX + tileSizeX, startY + tileSizeY, 0,
                    startX, startY + tileSizeY, 0
            };
            for (int j = 0; j < points.length; j++) {
                mesh[i + j] = points[j];
            }
            if (tilesInRow == tilesPerRow) {
                startX += tileSizeX;
                startY = 0;
                tilesInRow = 0;
            } else {
                startY += tileSizeY;
                tilesInRow++;
            }
        }
        int it = 0;
        for (int i = 0; i < indexes.length - 5; i += 6) {
            indexes[i] = it;
            indexes[i + 1] = it + 1;
            indexes[i + 2] = it + 3;
            indexes[i + 3] = it + 3;
            indexes[i + 4] = it + 1;
            indexes[i + 5] = it + 2;
            it+=4;
        }
        int it2 = 0;
        for (int i = 0; i < textureCoordinates.length - 7; i += 8) {
            Rectangle subImageSize = spriteSheets[it2].getTile();
            Rectangle fullImageSize = spriteSheets[it2].getFullImageSize();
            float uOffset = spriteSheets[it2].getTileX();
            float vOffset = spriteSheets[it2].getTileY();
            float u = (float) subImageSize.width / fullImageSize.width;
            float v = (float) subImageSize.height / fullImageSize.height;

            textureCoordinates[i] = (u * uOffset);
            textureCoordinates[i + 1] = (vOffset * v);
            textureCoordinates[i + 2] = (u + (u * uOffset));
            textureCoordinates[i + 3] = (vOffset * v);
            textureCoordinates[i + 4] = (u + (u * uOffset));
            textureCoordinates[i + 5] = (v + (vOffset * v));
            textureCoordinates[i + 6] = (u * uOffset);
            textureCoordinates[i + 7] = (v + (vOffset * v));
            it2++;
        }
    }

    public float[] getMesh() {
        return mesh;
    }

    public int[] getIndexes() {
        return indexes;
    }

    public float[] getTextureCoordinates() {
        return textureCoordinates;
    }

    public int getNumberOfTiles() {
        return numberOfTiles;
    }

    public float getTileSizeX() {
        return tileSizeX;
    }

    public float getTileSizeY() {
        return tileSizeY;
    }

    public int getTilesPerRow() {
        return tilesPerRow;
    }

    public float getxPos() {
        return xPos;
    }

    public float getyPos() {
        return yPos;
    }

    public boolean shouldRender() {
        return shouldRender;
    }

    public void setShouldRender(boolean shouldRender) {
        this.shouldRender = shouldRender;
    }
}
