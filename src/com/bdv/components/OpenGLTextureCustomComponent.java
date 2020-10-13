package com.bdv.components;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class OpenGLTextureCustomComponent {

    private final int textureId;
    private float shineDamper = 1;
    private float reflectivity = 0;
    private Vector3f colorOffset = new Vector3f(1.0f, 1.0f, 1.0f);
    private Vector3f ambientLight;
    private boolean toggleGlow = false;
    private boolean ambientLightToggle = false;
    private Vector3f glowColor;
    private boolean player;
    private float[] rgbTilesetEffects;
    private Vector2f chunkTileSize;
    private boolean chunkRendering = false;
    private float[] colorPointer;

    public OpenGLTextureCustomComponent(int id) {
        this.textureId = id;
    }

    public int getId() {
        return this.textureId;
    }

    public float getReflectivity() {
        return reflectivity;
    }

    public void setReflectivity(float reflectivity) {
        this.reflectivity = reflectivity;
    }

    public float getShineDamper() {
        return shineDamper;
    }

    public void setShineDamper(float shineDamper) {
        this.shineDamper = shineDamper;
    }

    public Vector3f getColorOffset() {
        return colorOffset;
    }

    public void setColorOffset(Vector3f colorOffset) {
        this.colorOffset = colorOffset;
    }

    public Vector3f getAmbientLight() {
        return ambientLight;
    }

    public void setAmbientLight(Vector3f ambientLight) {
        this.ambientLight = ambientLight;
    }

    public Vector3f getGlowColor() {
        return glowColor;
    }

    public boolean isToggleGlow() {
        return toggleGlow;
    }

    public void setGlowColor(Vector3f glowColor) {
        this.glowColor = glowColor;
    }

    public void setToggleGlow(boolean toggleGlow) {
        this.toggleGlow = toggleGlow;
    }

    public boolean isAmbientLightToggle() {
        return ambientLightToggle;
    }

    public void setAmbientLightToggle(boolean ambientLightToggle) {
        this.ambientLightToggle = ambientLightToggle;
    }

    public boolean isPlayer() {
        return player;
    }

    public void setPlayer(boolean player) {
        this.player = player;
    }

    public float[] getRgbTilesetEffects() {
        return rgbTilesetEffects;
    }

    public void setRgbTilesetEffects(float[] rgbTilesetEffects) {
        this.rgbTilesetEffects = rgbTilesetEffects;
    }

    public Vector2f getChunkTileSize() {
        return chunkTileSize;
    }

    public boolean isChunkRendering() {
        return chunkRendering;
    }

    public void setChunkRendering(boolean chunkRendering) {
        this.chunkRendering = chunkRendering;
    }

    public void setChunkTileSize(Vector2f chunkTileSize) {
        this.chunkTileSize = chunkTileSize;
    }

    public float[] getColorPointer() {
        return colorPointer;
    }

    public void setColorPointer(float[] colorPointer) {
        this.colorPointer = colorPointer;
    }
}