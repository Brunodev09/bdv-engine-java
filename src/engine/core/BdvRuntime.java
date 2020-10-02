package engine.core;

import engine.api.BdvScript;
import engine.core.interfaces.Entity;
import engine.video.Render;
import engine.video.RenderQueue;

import javax.swing.JFrame;

import java.awt.*;
import java.awt.image.BufferStrategy;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BdvRuntime extends Canvas implements Runnable {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(BdvRuntime.class.getName());

    public static int width;
    public static int height;
    public static int scale;
    public static String title;
    public int background = 0x892D6F;
    public int fpsCap = 60;

    public JFrame frame;
    private boolean running = false;


    private final RenderQueue queue = new RenderQueue();
    private Render render;

    private BdvScript script;

    private int fps = 0;

    private ExecutorService exec;

    public BdvRuntime(int width, int height, int scale, String title) {
        Dimension size = new Dimension(width * scale, height * scale);

        setPreferredSize(size);

        load();
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    public void setTitle(String title) {
        BdvRuntime.title = title;
    }

    public void setScale(int scale) {
        BdvRuntime.scale = scale;
    }

    public void setTemplate(BdvScript script) {
        render = new Render(this.queue, script.resolution.width, script.resolution.height);
        this.script = script;
        this.resizeFrame(script.resolution.width, script.resolution.height);
        this.setupRenderQueue();
        render.setBackground(this.background);
        render.setDimensions(script.resolution.width, script.resolution.height);
//        render.init();
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

    public void run() {

//        long lastTime = System.nanoTime();
//        final double ns = 1000000000.0 / 60.0;
//        double delta = 0;

        long lastTimeFps = System.currentTimeMillis();
        long nowFps = System.currentTimeMillis();
        requestFocus();

        while (running) {
//            long now = System.nanoTime();

            while ((nowFps - lastTimeFps) < 1000) {
                if (fps >= fpsCap) break;
                fps++;
                update(Math.abs((nowFps - lastTimeFps) / 1000.0) > 0.05 ? Math.abs((nowFps - lastTimeFps) / 1000.0) : 0.05);
                render();
                nowFps = System.currentTimeMillis();
            }
            lastTimeFps = nowFps;
            frame.setTitle(title + " | " + fps + " FPS | " + "By BrunoDev");
            fps = 0;

//            delta += (now - lastTime) / ns;
//            lastTime = now;
//            while (delta >= 1) {
//
//                delta--;
//            }
        }
        stop();
    }

    public void update(double deltaTime) {
        if (this.script == null) return;
        this.script.update(deltaTime);
    }

    public void render() {
        BufferStrategy buffer = getBufferStrategy();
        if (buffer == null) {
            createBufferStrategy(3);
            return;
        }
        this.render.render(buffer);
    }


    public void setDefaultBackgroundColor(int backgroundColor) {
        this.background = backgroundColor;
    }

    public void setupRenderQueue() {
        if (this.script == null) return;
        for (Entity obj : this.script.entities) {
            this.queue.Enqueue(obj);
        }
    }

    public void resizeFrame(int w, int h) {
        this.frame.setSize(new Dimension(w, h));
        this.frame.pack();
    }
}