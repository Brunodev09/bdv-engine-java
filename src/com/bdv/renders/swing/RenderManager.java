package com.bdv.renders.swing;

import com.bdv.api.BdvScript;
import com.bdv.systems.SpriteRendererSystem;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RenderManager extends Canvas implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(RenderManager.class.getName());
    public JFrame frame;

    private ExecutorService exec;
    private SpriteRendererSystem renderSystem;

    private boolean running = false;
    private final int fpsCap;
    private final String windowTitle;
    private final BdvScript script;
    private int width;
    private int height;

    private int fps;

    public RenderManager(BdvScript script) {
        this.script = script;

        this.fpsCap = script.getFPS();
        this.windowTitle = script.windowTitle;
        this.width = script.width;
        this.height = script.height;

        setPreferredSize(new Dimension(width, height));
        load();
    }

    private void load() {
        frame = new JFrame();
    }

    public void start() {
        running = true;
        this.exec = Executors.newSingleThreadExecutor();
        this.exec.submit(this);
    }

    public void stop() {
        running = false;
        try {
            LOGGER.info("Attempting to shutdown thread.");
            this.exec.shutdown();
            this.exec.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
            LOGGER.info("Thread interrupted.");
        } finally {
            if (!this.exec.isTerminated()) {
                LOGGER.info("Cancel non finished tasks in thread.");
            }
            this.exec.shutdownNow();
            LOGGER.info("Thread shutdown.");
        }
    }

    @Override
    public void run() {

        renderSystem = (SpriteRendererSystem) this.script.manager.getSystem(SpriteRendererSystem.class);

        if (renderSystem == null) return;

        long lastTimeFps = System.currentTimeMillis();
        long nowFps = System.currentTimeMillis();

        requestFocus();

        while (running) {
            while ((nowFps - lastTimeFps) < 1000) {
                if (fps >= fpsCap) break;
                fps++;
                update(Math.max(Math.abs((nowFps - lastTimeFps) / 1000.0), 0.05));
                render();
                nowFps = System.currentTimeMillis();
            }

            lastTimeFps = nowFps;
            frame.setTitle("[SWING] - " + windowTitle + " | " + fps + " FPS");
            fps = 0;
        }
        stop();
    }

    private void update(double deltaTime) {
        this.script.update(deltaTime);
        this.script.manager.update();
    }

    private void render() {
        BufferStrategy buffer = getBufferStrategy();
        if (buffer == null) {
            createBufferStrategy(3);
            return;
        }
        this.renderSystem.render(buffer);
    }

}
