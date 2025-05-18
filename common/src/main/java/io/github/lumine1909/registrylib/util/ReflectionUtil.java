package io.github.lumine1909.registrylib.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ReflectionUtil {

    private static final Cache<String, Field> FIELD_CACHE = CacheBuilder.newBuilder().build();
    private static final Cache<String, Method> METHOD_CACHE = CacheBuilder.newBuilder().build();

    public static void copyFields(Object from, Object to) {
        Class<?> fromClass = from.getClass();
        Class<?> toClass = to.getClass();

        while (fromClass != null) {
            for (Field field : fromClass.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                field.setAccessible(true);
                try {
                    Field toField = getFieldInHierarchy(toClass, field.getName());
                    toField.setAccessible(true);
                    toField.set(to, field.get(from));
                } catch (Exception ignored) {
                }
            }
            fromClass = fromClass.getSuperclass();
        }
    }

    private static Field getFieldInHierarchy(Class<?> clazz, String name) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<?> clazz, String fieldName, Object instance) {
        try {
            String cacheKey = clazz.getName() + "." + fieldName;
            Field field;
            if ((field = FIELD_CACHE.getIfPresent(cacheKey)) == null) {
                field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                FIELD_CACHE.put(cacheKey, field);
            }
            return (T) field.get(instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void set(Class<?> clazz, String fieldName, Object instance, Object value) {
        try {
            String cacheKey = clazz.getName() + "." + fieldName;
            Field field;
            if ((field = FIELD_CACHE.getIfPresent(cacheKey)) == null) {
                field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                FIELD_CACHE.put(cacheKey, field);
            }
            field.set(instance, value);
        } catch (Exception ignored) {
        }
    }

    public static boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T invoke(Class<?> clazz, String methodName, Class<?>[] arguments, Object instance, Object... argumentValues) {
        try {
            String cacheKey = clazz.getName() + "." + methodName;
            Method method;
            if ((method = METHOD_CACHE.getIfPresent(cacheKey)) == null) {
                method = clazz.getDeclaredMethod(methodName, arguments);
                method.setAccessible(true);
                METHOD_CACHE.put(cacheKey, method);
            }
            return (T) method.invoke(instance, argumentValues);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static <T> T invoke(Class<?> clazz, String methodName, Class<?> argument1, Object instance, Object... argumentValues) {
        return invoke(clazz, methodName, new Class[]{argument1}, instance, argumentValues);
    }

    public static <T> T invoke(Class<?> clazz, String methodName, Class<?> argument1, Class<?> argument2, Object instance, Object... argumentValues) {
        return invoke(clazz, methodName, new Class[]{argument1, argument2}, instance, argumentValues);
    }

    public static <T> T invoke(Class<?> clazz, String methodName, Class<?> argument1, Class<?> argument2, Class<?> argument3, Object instance, Object... argumentValues) {
        return invoke(clazz, methodName, new Class[]{argument1, argument2, argument3}, instance, argumentValues);
    }
}
