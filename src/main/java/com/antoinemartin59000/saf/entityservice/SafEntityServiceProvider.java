package com.antoinemartin59000.saf.entityservice;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import com.antoinemartin59000.saf.entity.SafEntity;
import com.antoinemartin59000.saf.entity.SafEntitySearch;
import com.antoinemartin59000.saf.entitydao.SafEntityDao;
import com.antoinemartin59000.saf.entitydao.SafEntityDaoProvider;

public class SafEntityServiceProvider {

    private static final Map<Class<? extends SafEntity>, Class<? extends SafEntityService>> MAP = new HashMap<>();

    static {

        // Configure Reflections to scan the desired package(s)
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(""))
                .setExpandSuperTypes(false) // faster, we only care about direct subclasses
        );

        // Get ALL classes that extend SafEntityService (including indirect subclasses)
        Set<Class<? extends SafEntityService>> subClasses = reflections.getSubTypesOf(SafEntityService.class);

        for (Class<? extends SafEntityService> subClass : subClasses) {
            // Skip interfaces and abstract classes if you only want concrete implementations
            if (subClass.isInterface() || java.lang.reflect.Modifier.isAbstract(subClass.getModifiers())) {
                continue;
            }

            // Get the generic superclass (SafEntityService<E, S>)
            Type genericSuper = subClass.getGenericSuperclass();

            if (genericSuper instanceof ParameterizedType pt) {
                Type[] typeArgs = pt.getActualTypeArguments();

                if (typeArgs.length == 0) {
                    continue;
                }

                Type eType = typeArgs[0];

                if (eType instanceof Class eClass) {
                    MAP.put(eClass, subClass);
                }

            }
        }
    }

    private final Map<Class<? extends SafEntity>, SafEntityService<?, ?>> safEntityServices = new HashMap<>();

    public SafEntityServiceProvider(String cacheHost, int cachePort, Set<Class<?>> localCache) {

        SafEntityDaoProvider safEntityDaoProvider = new SafEntityDaoProvider(cacheHost, cachePort, localCache);

        for (Map.Entry<Class<? extends SafEntity>, Class<? extends SafEntityService>> entry : MAP.entrySet()) {
            Class<? extends SafEntity> safEntityClass = entry.getKey();
            Class<? extends SafEntityService> safEntityServiceClass = entry.getValue();

            SafEntityDao<?, ?> safEntityDao = safEntityDaoProvider.getEntityDao(safEntityClass);

            SafEntityService safEntityService;
            try {
                safEntityService = (SafEntityService) safEntityServiceClass.getConstructors()[0].newInstance(safEntityDao);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
                throw new RuntimeException(e);
            }

            safEntityServices.put(safEntityClass, safEntityService);
        }

    }

    public <E extends SafEntity, S extends SafEntitySearch, T extends SafEntityService<E, S>> T get(Class<E> safEntityClass) {
        return (T) safEntityServices.get(safEntityClass);
    }

}
