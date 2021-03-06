package com.bdv.ECS;

import com.bdv.components.*;
import com.bdv.exceptions.InvalidInstance;
import com.bdv.pool.Pool;
import com.bdv.renders.opengl.OpenGLModel;
import com.bdv.renders.opengl.OpenGLTextureCustom;
import org.lwjgl.util.Dimension;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

public class SystemManager {
    private Set<Entity> createdEntities = new HashSet<>();
    private Set<Entity> killedEntities = new HashSet<>();

    private Deque<Integer> freeIds = new LinkedList<>();
    private List<Pool<Object>> componentPools = new ArrayList<>();
    private List<Signature> entityComponentSignatures = new ArrayList<>();
    private Map<String, Object> systems = new HashMap<>();

    private Map<Entity, Map<Class<?>, Integer>> _typeManager = new HashMap<>();

    private int totalEntities = 0;

    private final Logger logger = Logger.getLogger(SystemManager.class.getName());

    public Entity createEntity() {
        int entityId;

        if (freeIds.isEmpty()) {
            entityId = ++totalEntities;
        } else {
            entityId = freeIds.getFirst();
            freeIds.pop();
        }

        while (entityComponentSignatures.size() < entityId + 1) {
            entityComponentSignatures.add(null);
        }

        Entity entity = new Entity(entityId);
        entity.manager = this;

        createdEntities.add(entity);
        entityComponentSignatures.set(entityId, new Signature());

        logger.info("[SYSTEM_MANAGER]- Created Entity with ID " + entityId + " (total = " + totalEntities + ")");

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
        Signature compSign = this.getComponentSignature(entity);
        for (Map.Entry<String, Object> entry : systems.entrySet()) {
            final Signature sysSignature = ((System) entry.getValue()).getSignature();
            boolean isSimilar = compSign.equals(sysSignature);

            if (isSimilar) {
                ((System) entry.getValue()).addEntity(entity);
            }
        }
    }

    public <T> void addComponent(Entity entity, Class<T> type, Object... args)
            throws InstantiationException,
            IllegalAccessException,
            NoSuchMethodException,
            InvocationTargetException,
            ClassNotFoundException {

        int componentId = Component.getNextId();

        Map<Class<?>, Integer> entityTypeMap = _typeManager.get(entity);

        if (entityTypeMap == null) {
            entityTypeMap = new HashMap<>();
        }
        entityTypeMap.put(type, componentId);
        _typeManager.put(entity, entityTypeMap);

//        final int componentId = Component.<T>getId() + 1;
        final int entityId = entity.getId();

        if (componentId >= componentPools.size()) {
            while (componentPools.size() != componentId + 1) {
                componentPools.add(null);
            }
            Pool<T> newComponentPool = new Pool<>();
            componentPools.set(componentId, (Pool<Object>) newComponentPool);
        }

        Pool<T> componentPool = (Pool<T>) componentPools.get(componentId);

        T component;
        if (args.length == 0) component = getInstanceOfT(type);
        else component = getInstanceOfT(type, args);

        Component.nextId++;

        componentPool.set(entityId, component);
        entityComponentSignatures.get(entityId).getSet().set(componentId);
    }

    public <T> T getInstanceOfT(Class<T> aClass) throws IllegalAccessException,
            InstantiationException, NoSuchMethodException, InvocationTargetException {
        return aClass.getDeclaredConstructor().newInstance();
    }

    public <T> T getInstanceOfT(Class<T> aClass, Object... args)
            throws IllegalAccessException,
            InstantiationException,
            NoSuchMethodException,
            InvocationTargetException,
            ClassNotFoundException {

        Class<?>[] paramSignature = new Class[args.length];

        String componentsFolder = new File("src/com/bdv/components").getAbsolutePath();
        File directory = new File(componentsFolder);
        File[] files = directory.listFiles();

        if (files != null && files.length != 0) {
            for (File file : files) {
                String className = "com.bdv.components." + file.getName().split(".java")[0];
                Class<?> _class = Class.forName(className);

                for (int i = 0; i < args.length; i++) {
                    if (Class.forName(className).isInstance(args[i])) {
                        paramSignature[i] = _class;
                    }
                }
            }
        }
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Integer) {
                paramSignature[i] = Integer.TYPE;
            } else if (args[i] instanceof String) {
                paramSignature[i] = String.class;
            } else if (args[i] instanceof Vector3f) {
                paramSignature[i] = Vector3f.class;
            } else if (args[i] instanceof BufferedImage) {
                paramSignature[i] = BufferedImage.class;
            } else if (args[i] instanceof OpenGLModel) {
                paramSignature[i] = OpenGLModel.class;
            } else if (args[i] instanceof OpenGLTextureCustom) {
                paramSignature[i] = OpenGLTextureCustom.class;
            } else if (args[i] instanceof Vector2f) {
                paramSignature[i] = Vector2f.class;
            } else if (args[i] instanceof Dimension) {
                paramSignature[i] = Dimension.class;
            }
        }

        String methodName = "invoke";
        String className = aClass.getName();
        Class<?> _class = Class.forName(className);
        Object _instance = _class.getDeclaredConstructor().newInstance();
        Method myMethod = _class.getDeclaredMethod(methodName, paramSignature);
        Object returnedInstance = myMethod.invoke(_instance, args);

        return (T) returnedInstance;
    }

    public <T> void removeComponent(Entity entity, Class<T> type) {
//        final int componentId = Component.<T>getId();
        Map<Class<?>, Integer> entityMap = _typeManager.get(entity);
        final int componentId = entityMap.get(type);
        final int entityId = entity.getId();
        entityComponentSignatures.get(entityId).getSet().set(componentId, false);
    }

    public <T> T getComponent(Entity entity, Class<T> type) {
//        final int componentId = Component.<T>getId();
        Map<Class<?>, Integer> entityMap = _typeManager.get(entity);
        if (entityMap.get(type) == null) return null;
        final int componentId = entityMap.isEmpty() ? 0 : entityMap.get(type);
        if (componentId == 0) return null;
        final int entityId = entity.getId();
        Pool<T> pool = (Pool<T>) componentPools.get(componentId);
        if (entityId >= pool.getSize()) return null;
        return pool.get(entityId);
    }

    public <T> void updateComponent(Entity entity, Class<T> type, Object... args)
            throws InvocationTargetException,
            NoSuchMethodException,
            ClassNotFoundException,
            InstantiationException,
            IllegalAccessException {
        Map<Class<?>, Integer> entityMap = _typeManager.get(entity);
        if (entityMap.get(type) == null) return;
        final int componentId = entityMap.isEmpty() ? 0 : entityMap.get(type);
        if (componentId == 0) return;
        final int entityId = entity.getId();
        Pool<T> pool = (Pool<T>) componentPools.get(componentId);
        if (entityId >= pool.getSize()) return;
        T component = getInstanceOfT(type, args);
        pool.set(entityId, component);
    }

    public <T> boolean hasComponent(Entity entity, Class<T> type) {
//        final int componentId = Component.<T>getId();
        Map<Class<?>, Integer> entityMap = _typeManager.get(entity);
        final int componentId = entityMap.get(type);
        final int entityId = entity.getId();
        return entityComponentSignatures.get(entityId).getSet().get(componentId);
    }

    public <T> void addSystem(Class<T> system)
            throws InvalidInstance,
            NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException,
            InstantiationException {

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
