package app.Video;
import java.util.LinkedList;
import java.util.Queue;

import app.Core.Interfaces.Entity;

public class RenderQueue {
    private Queue<Entity> renderQueue = new LinkedList<Entity>();

    public void Enqueue(Entity entity) {
        this.renderQueue.offer(entity);
    }

    public void Dequeue() {
        this.renderQueue.poll();
    }

    public Queue<Entity> getRenderQueue() {
        return renderQueue;
    }
}
