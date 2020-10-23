package com.bdv.ECS;

import com.bdv.exceptions.InvalidInstance;
import com.bdv.pool.Pool;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Logger;

public class SystemManager {
    private Set<Entity> createdEntities = new HashSet<>();
    private Set<Entity> killedEntities = new HashSet<>();

    private Deque<Integer> freeIds = new LinkedList<>();
    private List<Pool<Object>> componentPools = new ArrayList<>();
    private List<Signature> entityComponentSignatures = new ArrayList<>();
    private Map<String, Object> systems = new HashMap<>();
    private int totalEntities = 0;

    private final Logger log = Logger.getLogger(SystemManager.class.getName());

    public Entity createEntity() {
        int entityId;

        if (freeIds.isEmpty()) {
            entityId = totalEntities++;

            if (entityId > entityComponentSignatures.size()) {
                entityComponentSignatures.add(new Signature());
            }
        } else {
            entityId = freeIds.getFirst();
            freeIds.pop();
        }

        Entity entity = new Entity(entityId);
        entity.manager = this;

        createdEntities.add(entity);

        log.info("[SYSTEM_MANAGER] Created Entity with ID " + entity + " (total = " + totalEntities + ")");

        return entity;
    }

    public void destroyEntity(Entity entity) {
        final int entityId = entity.getId();

        freeIds.add(entityId);
        entityComponentSignatures.get(entityId).getSet().clear();

        for (Map.Entry<String, Object> entry : systems.entrySet()) {
            ((System) entry.getValue()).removeEntity(entity);
        }
    }

    public void killEntity(Entity entity) {
        killedEntities.add(entity);
    }

    public void update() {
        for (Entity itEntity : createdEntities) {
            this.addEntityToSystems(itEntity);
        }

        createdEntities.clear();

        for (Entity entity : killedEntities) {
            this.destroyEntity(entity);
        }

        killedEntities.clear();
    }

    public Signature getComponentSignature(Entity entity) {
        final int entityId = entity.getId();
        return entityComponentSignatures.get(entityId);
    }

    public void addEntityToSystems(Entity entity) {
        try {
            Signature compSign = this.getComponentSignature(entity);
            for (Map.Entry<String, Object> entry : systems.entrySet()) {
                final Signature sysSignature = ((System) entry.getValue()).getSignature();
                boolean isSimilar = compSign.equals(sysSignature);

                if (isSimilar) {
                    ((System) entry.getValue()).addEntity(entity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public <T> void addComponent(Entity entity, Class<T> type) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        final int componentId = Component.<T>getId();
        final int entityId = entity.getId();

        if (componentPools.get(componentId) == null) {
            Pool<T> newComponentPool = new Pool<>();
            componentPools.set(componentId, (Pool<Object>) newComponentPool);
        }

        Pool<T> componentPool = (Pool<T>) componentPools.get(componentId);
        T component = getInstanceOfT(type);

        componentPool.set(entityId, component);
        entityComponentSignatures.get(entityId).getSet().set(componentId);
    }

    public <T> T getInstanceOfT(Class<T> aClass) throws IllegalAccessException,
            InstantiationException, NoSuchMethodException, InvocationTargetException {
        return aClass.getDeclaredConstructor().newInstance();
    }

    public <T> void removeComponent(Entity entity) {
        final int componentId = Component.<T>getId();
        final int entityId = entity.getId();
        entityComponentSignatures.get(entityId).getSet().set(componentId, false);
    }

    public <T> T getComponent(Entity entity) {
        final int componentId = Component.<T>getId();
        final int entityId = entity.getId();
        Pool<T> pool = (Pool<T>) componentPools.get(componentId);
        return pool.get(entityId);
    }

    public <T> boolean hasComponent(Entity entity) {
        final int componentId = Component.<T>getId();
        final int entityId = entity.getId();
        return entityComponentSignatures.get(entityId).getSet().get(componentId);
    }

    public <T> void addSystem(Class<T> system) throws InvalidInstance,
            NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Constructor<T> constructor = system.getConstructor();
        T instance = constructor.newInstance();
        if (!(instance instanceof System)) throw new InvalidInstance(System.class.getName());
        String className = system.getName();
        systems.putIfAbsent(className, instance);
    }

    public <T> Object getSystem(Class<T> system) {
        return systems.get(system.getName());
    }

    public <T> void removeSystem(Class<T> system) {
        systems.remove(system.getName());
    }

    public <T> boolean hasSystem(Class<T> system) {
        return systems.get(system.getName()) != null;
    }

}
