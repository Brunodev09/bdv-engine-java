package app.Core;

import app.API.Script;
import app.Core.Interfaces.Entity;
import app.Video.RenderQueue;

import javax.swing.JFrame;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

public class BdvRuntime extends Canvas implements Runnable {
    private static final long serialVersionUID = 1L;

    public static int width;
    public static int height;
    public static int scale;
    public static String title;
    public int background = 0x892D6F;

    private Thread thread;
    public JFrame frame;
    private boolean running = false;

    private BufferedImage image;
    private int[] pixels;
    private Script script;
    private RenderQueue queue;

    private int fps = 60;

    public BdvRuntime(int width, int height, int scale, String title) {
        BdvRuntime.width = width;
        BdvRuntime.height = height;
        BdvRuntime.scale = scale;
        BdvRuntime.title = title;
        Dimension size = new Dimension(width * scale, height * scale);
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        setPreferredSize(size);
        this.queue = new RenderQueue();

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

    public void setTemplate(Script script) {
        this.script = script;
        this.setupRenderQueue();
    }

    private void load() {
        frame = new JFrame();
    }


    public synchronized void start() {
        running = true;
        thread = new Thread(this, "bdv-engine 0.0.1");
        thread.start();
    }

    public synchronized void stop() {
        running = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void run() {

        long timer = System.currentTimeMillis();
        long lastTime = System.nanoTime();
        final double ns = 1000000000.0 / this.fps;
        double delta = 0;
        requestFocus();

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            while (delta >= 1) {
                update();
                render();
                delta--;
            }

            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                frame.setTitle(title + " | " + this.fps + " FPS | " + "By BrunoDev");
            }
        }
        stop();
    }

    public void update() {
        if (this.script == null) return;
        this.script.update();
    }

    public void render() {
        BufferStrategy buffer = getBufferStrategy();
        if (buffer == null) {
            createBufferStrategy(3);
            return;
        }

        Arrays.fill(pixels, this.background);
        Graphics display = buffer.getDrawGraphics();
        display.drawImage(image, 0, 0, getWidth(), getHeight(), null);


        if (this.queue.getRenderQueue().size() > 0) {
            for (Entity renderable : this.queue.getRenderQueue()) {
                switch (renderable.getMdl()) {
                    case TEXTURE:
                        break;
                    case SPRITESHEET:
                        break;
                    case ARC:
                        break;
                    case TEXT:
                        break;
                    case RECTANGLE:
                        int[] color = renderable.getColor().getColorCodes();
                        display.setColor(new Color(color[0], color[1], color[2], color[3]));
                        display.fillRect(renderable.getPosition().x, renderable.getPosition().y,
                                renderable.getDimension().width, renderable.getDimension().height);
                        break;

                }
            }
        }

        display.dispose();
        buffer.show();
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
}