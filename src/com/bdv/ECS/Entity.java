package com.bdv.ECS;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

public class Entity {
    private final Logger logger = Logger.getLogger(Entity.class.getName());

    private final int id;
    private boolean kill = false;

    public SystemManager manager;

    public Entity(int id) {
        this.id = id;
    }

    public void setKill(boolean kill) {
        this.kill = kill;
    }

    public boolean isKill() {
        return kill;
    }

    public int getId() {
        return id;
    }

    public <T> void addComponent(Class<T> type, Object... args) throws InvocationTargetException,
            NoSuchMethodException, InstantiationException, IllegalAccessException {
        try {
            manager.<T>addComponent(this, type, args);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public <T> T getComponent() {
        return manager.getComponent(this);
    }

    public <T> boolean hasComponent() {
        return manager.<T>hasComponent(this);
    }

    public <T> void removeComponent() {
        manager.<T>removeComponent(this);
    }


}
