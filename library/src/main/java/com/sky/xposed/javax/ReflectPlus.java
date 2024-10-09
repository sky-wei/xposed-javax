package com.sky.xposed.javax;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.robv.android.xposed.XposedHelpers;

public class ReflectPlus {

    public static XClass with(Class<?> clazz) {
        return new InternalClass(clazz);
    }

    public static XClass with(Object object) {
        return new InternalClass(object);
    }


    public static final class InternalClass implements XClass {

        private final Object object;
        private final Class<?> clazz;

        public InternalClass(Object object) {
            this(object, object.getClass());
        }

        public InternalClass(Class<?> clazz) {
            this(null, clazz);
        }

        public InternalClass(Object object, Class<?> clazz) {
            this.object = object;
            this.clazz = clazz;
        }

        @Override
        public Object getObject() {
            return object;
        }

        @Override
        public Class<?> getClazz() {
            return clazz;
        }

        @Override
        public XMethod findMethod(String methodName, Object... args) {
            Method method = XposedHelpers.findMethodBestMatch(clazz, methodName, args);
            return createXMethod(method);
        }

        @Override
        public XMethod findMethod(String methodName, Class<?>[] parameterTypes) {
            Method method = XposedHelpers.findMethodBestMatch(clazz, methodName, parameterTypes);
            return createXMethod(method);
        }

        @Override
        public XMethod findMethod(String methodName, Class<?>[] parameterTypes, Object... args) {
            Method method = XposedHelpers.findMethodBestMatch(clazz, methodName, parameterTypes, args);
            return createXMethod(method);
        }

        @Override
        public XMethod findMethod(Filter<Method> filter) {
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (filter.accept(method)) {
                    return createXMethod(method);
                }
            }
            return null;
        }

        @Override
        public XMethod findMethodByReturnType(Filter<Class<?>> filter) {
            return findMethod(value -> filter.accept(value.getReturnType()));
        }

        @Override
        public XField findField(String fieldName) {
            Field field = XposedHelpers.findField(clazz, fieldName);
            return createXField(field);
        }

        @Override
        public XField findField(Filter<Field> filter) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (filter.accept(field)) {
                    return createXField(field);
                }
            }
            return null;
        }

        @Override
        public XField findFieldByType(Filter<Class<?>> filter) {
            return findField(value -> filter.accept(value.getType()));
        }

        private XMethod createXMethod(Method method) {
            return new InternalMethod(this, method);
        }

        private XField createXField(Field field) {
            return new InternalField(this, field);
        }
    }


    public static final class InternalMethod implements XMethod {

        private final XClass xClass;
        private final Object object;
        private final Method method;

        public InternalMethod(XClass xClass, Method method) {
            this.xClass = xClass;
            this.object = xClass.getObject();
            this.method = method;
        }

        @Override
        public XClass getXClass() {
            return xClass;
        }

        @Override
        public Object callMethod(Object... args) {
            try {
                return method.invoke(object, args);
            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (InvocationTargetException e) {
                throw new InvocationTargetError(e.getCause());
            }
        }
    }

    public static final class InternalField implements XField {

        private final XClass xClass;
        private final Object object;
        private final Field field;

        public InternalField(XClass xClass, Field field) {
            this.xClass = xClass;
            this.object = xClass.getObject();
            this.field = field;
        }

        @Override
        public XClass getXClass() {
            return xClass;
        }

        @Override
        public Object getObject() {
            try {
                return field.get(object);
            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            } catch (IllegalArgumentException e) {
                throw e;
            }
        }

        @Override
        public boolean getBoolean() {
            try {
                return field.getBoolean(object);
            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            } catch (IllegalArgumentException e) {
                throw e;
            }
        }

        @Override
        public byte getByte() {
            try {
                return field.getByte(object);
            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            } catch (IllegalArgumentException e) {
                throw e;
            }
        }

        @Override
        public char getChar() {
            try {
                return field.getChar(object);
            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            } catch (IllegalArgumentException e) {
                throw e;
            }
        }

        @Override
        public double getDouble() {
            try {
                return field.getDouble(object);
            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            } catch (IllegalArgumentException e) {
                throw e;
            }
        }

        @Override
        public float getFloat() {
            try {
                return field.getFloat(object);
            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            } catch (IllegalArgumentException e) {
                throw e;
            }
        }

        @Override
        public int getInt() {
            try {
                return field.getInt(object);
            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            } catch (IllegalArgumentException e) {
                throw e;
            }
        }

        @Override
        public long getLong() {
            try {
                return field.getLong(object);
            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            } catch (IllegalArgumentException e) {
                throw e;
            }
        }

        @Override
        public short getShort() {
            try {
                return field.getShort(object);
            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            } catch (IllegalArgumentException e) {
                throw e;
            }
        }

        @Override
        public XClass setObject(Object value) {
            try {
                field.set(object, value);
            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            } catch (IllegalArgumentException e) {
                throw e;
            }
            return getXClass();
        }

        @Override
        public XClass setBoolean(boolean value) {
            try {
                field.setBoolean(object, value);
            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            } catch (IllegalArgumentException e) {
                throw e;
            }
            return getXClass();
        }

        @Override
        public XClass setByte(byte value) {
            try {
                field.setByte(object, value);
            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            } catch (IllegalArgumentException e) {
                throw e;
            }
            return getXClass();
        }

        @Override
        public XClass setChar(char value) {
            try {
                field.setChar(object, value);
            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            } catch (IllegalArgumentException e) {
                throw e;
            }
            return getXClass();
        }

        @Override
        public XClass setDouble(double value) {
            try {
                field.setDouble(object, value);
            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            } catch (IllegalArgumentException e) {
                throw e;
            }
            return getXClass();
        }

        @Override
        public XClass setFloat(float value) {
            try {
                field.setFloat(object, value);
            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            } catch (IllegalArgumentException e) {
                throw e;
            }
            return getXClass();
        }

        @Override
        public XClass setInt(int value) {
            try {
                field.setInt(object, value);
            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            } catch (IllegalArgumentException e) {
                throw e;
            }
            return getXClass();
        }

        @Override
        public XClass setLong(long value) {
            try {
                field.setLong(object, value);
            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            } catch (IllegalArgumentException e) {
                throw e;
            }
            return getXClass();
        }

        @Override
        public XClass setShort(short value) {
            try {
                field.setShort(object, value);
            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            } catch (IllegalArgumentException e) {
                throw e;
            }
            return getXClass();
        }
    }


    public interface XMethod {

        XClass getXClass();

        Object callMethod(Object... args);
    }


    public interface XField {

        XClass getXClass();

        Object getObject();

        boolean getBoolean();

        byte getByte();

        char getChar();

        double getDouble();

        float getFloat();

        int getInt();

        long getLong();

        short getShort();


        XClass setObject(Object value);

        XClass setBoolean(boolean value);

        XClass setByte(byte value);

        XClass setChar(char value);

        XClass setDouble(double value);

        XClass setFloat(float value);

        XClass setInt(int value);

        XClass setLong(long value);

        XClass setShort(short value);
    }


    public interface XClass {

        Object getObject();

        Class<?> getClazz();

        XMethod findMethod(String methodName, Object... args);

        XMethod findMethod(String methodName, Class<?>[] parameterTypes);

        XMethod findMethod(String methodName, Class<?>[] parameterTypes, Object... args);

        XMethod findMethod(Filter<Method> filter);

        XMethod findMethodByReturnType(Filter<Class<?>> filter);

        XField findField(String fieldName);

        XField findField(Filter<Field> filter);

        XField findFieldByType(Filter<Class<?>> filter);
    }


    public static final class InvocationTargetError extends Error {

        public InvocationTargetError(Throwable cause) {
            super(cause);
        }
    }

    /**
     * 过滤的接口
     * @param <T>
     */
    public interface Filter<T> {

        boolean accept(T value);
    }
}



