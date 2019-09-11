/*
 * Copyright (c) 2018 The sky Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sky.xposed.javax;

import android.text.TextUtils;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.callbacks.XCallback;

public class XposedPlus {

    private ClassLoader mClassLoader;
    private MethodHook.ThrowableCallback mThrowableCallback;

    private static XposedPlus sXposedPlus;

    private XposedPlus(ClassLoader classLoader, MethodHook.ThrowableCallback throwableCallback) {
        mClassLoader = classLoader;
        mThrowableCallback = throwableCallback;
    }

    public static void setDefaultInstance(XposedPlus xposedPlus) {
        sXposedPlus = xposedPlus;
    }

    public static XposedPlus get() {
        if (sXposedPlus == null) {
            throw new IllegalArgumentException("XposedPlus must not be null.");
        }
        return sXposedPlus;
    }

    public static XposedPlus with(XC_LoadPackage.LoadPackageParam packageParam) {
        return with(packageParam.classLoader);
    }

    public static XposedPlus with(ClassLoader classLoader) {

        if (sXposedPlus == null) {
            sXposedPlus = new Builder(classLoader).build();
        }

        if (sXposedPlus.mClassLoader == classLoader) {
            return sXposedPlus;
        }

        return new XposedPlus(classLoader, sXposedPlus.mThrowableCallback);
    }

    public MethodHook findMethod(Class<?> clazz, String methodName, Object... parameterTypes) {
        return new InternalMethodHook(this, clazz, methodName, parameterTypes);
    }

    public MethodHook findMethod(String className, String methodName, Object... parameterTypes) {
        return new InternalMethodHook(this, className, methodName, parameterTypes);
    }

    public MethodHook findConstructor(Class<?> clazz, Object... parameterTypes) {
        return new InternalMethodHook(this, clazz, parameterTypes);
    }

    public MethodHook findConstructor(String className, Object... parameterTypes) {
        return new InternalMethodHook(this, className, parameterTypes);
    }

    public Class<?> findClass(String className) {
        return XposedHelpers.findClass(className, mClassLoader);
    }

    private final static class InternalMethodHook implements MethodHook {

        private boolean multiple;
        private boolean constructor;
        private ClassLoader classLoader;
        private MethodHook.ThrowableCallback throwableCallback;
        private String className;
        private Class<?> clazz;
        private String methodName;
        private Object[] parameterTypes;
        private int priority = XCallback.PRIORITY_DEFAULT;

        InternalMethodHook(XposedPlus xposedPlus, String className, Object[] parameterTypes) {
            this(true, xposedPlus, className, null, null, parameterTypes);
        }

        InternalMethodHook(XposedPlus xposedPlus, Class<?> clazz, Object[] parameterTypes) {
            this(true, xposedPlus, null, clazz, null, parameterTypes);
        }

        InternalMethodHook(XposedPlus xposedPlus, String className, String methodName, Object[] parameterTypes) {
            this(false, xposedPlus, className, null, methodName, parameterTypes);
        }

        InternalMethodHook(XposedPlus xposedPlus, Class<?> clazz, String methodName, Object[] parameterTypes) {
            this(false, xposedPlus, null, clazz, methodName, parameterTypes);
        }

        InternalMethodHook(boolean constructor, XposedPlus xposedPlus, String className,
                                  Class<?> clazz, String methodName, Object[] parameterTypes) {
            this.constructor = constructor;
            this.className = className;
            this.clazz = clazz;
            this.methodName = methodName;
            this.parameterTypes = parameterTypes;
            this.classLoader = xposedPlus.mClassLoader;
            this.throwableCallback = xposedPlus.mThrowableCallback;
        }

        @Override
        public Unhook hook(BeforeCallback callback) {
            return before(callback);
        }

        @Override
        public Unhook hook(AfterCallback callback) {
            return after(callback);
        }

        @Override
        public Unhook before(BeforeCallback callback) {
            return handlerHook(
                    new InternalMethodHookAdapter(priority, callback, throwableCallback));
        }

        @Override
        public Unhook after(AfterCallback callback) {
            return handlerHook(
                    new InternalMethodHookAdapter(priority, callback, throwableCallback));
        }

        @Override
        public Unhook replace(ReplaceCallback callback) {
            return handlerHook(
                    new InternalReplacementAdapter(priority, callback, throwableCallback));
        }

        @Override
        public Unhook hook(HookCallback callback) {
            return handlerHook(
                    new InternalMethodHookAdapter(priority, callback, throwableCallback));
        }

        @Override
        public Unhook hook(BeforeCallback beforeCallback, AfterCallback afterCallback) {
            return handlerHook(
                    new InternalMethodHookAdapter(priority, beforeCallback, afterCallback, throwableCallback));
        }

        @Override
        public MethodHook throwable(ThrowableCallback callback) {
            this.throwableCallback = callback;
            return this;
        }

        @Override
        public MethodHook multiple() {
            if (parameterTypes != null && parameterTypes.length > 0) {
                throw new IllegalArgumentException("parameterTypes is not null");
            }
            this.multiple = true;
            return this;
        }

        @Override
        public MethodHook setPriority(int priority) {

            return null;
        }

        /**
         * 处理Hook
         * @param methodHook
         * @return
         */
        private Unhook handlerHook(XC_MethodHook methodHook) {
            if (clazz == null && TextUtils.isEmpty(className)) {
                throw new IllegalArgumentException("clazz and className is null");
            }
            return constructor ? handlerConstructor(methodHook) : handlerMethod(methodHook);
        }

        /**
         * 处理Hook类的方法
         * @param methodHook
         * @return
         */
        private Unhook handlerMethod(XC_MethodHook methodHook) {

            try {
                if (multiple) {
                    return createUnhook(XposedBridge.hookAllMethods(getHookClass(), methodName, methodHook));
                }
                return createUnhook(XposedHelpers.findAndHookMethod(
                        getHookClass(), methodName, mergeParameterTypesAndCallback(parameterTypes, methodHook)));
            } catch (Throwable tr) {
                throwableCallback.onThrowable(tr);
            }
            return null;
        }

        /**
         * 处理Hook类的构造方法
         * @param methodHook
         * @return
         */
        private Unhook handlerConstructor(XC_MethodHook methodHook) {

            try {
                if (multiple) {
                    return createUnhook(XposedBridge.hookAllConstructors(getHookClass(), methodHook));
                }
                return createUnhook(XposedHelpers.findAndHookConstructor(
                        getHookClass(), mergeParameterTypesAndCallback(parameterTypes, methodHook)));
            } catch (Throwable tr) {
                throwableCallback.onThrowable(tr);
            }
            return null;
        }

        private Unhook createUnhook(XC_MethodHook.Unhook unhook) {
            return new InternalUnhookAdapter(unhook);
        }

        private Unhook createUnhook(Set<XC_MethodHook.Unhook> unhooks) {
            return new InternalUnhookAdapter(unhooks);
        }

        /**
         * 获取Hook的Class
         * @return
         */
        private Class<?> getHookClass() {
            if (clazz == null) {
                clazz = XposedHelpers.findClass(className, classLoader);
            }
            return clazz;
        }

        /**
         * 把参数与回调的类进行合并返回成Object[]数组
         * @param parameterTypes 参数数组
         * @param methodHook Hook的回调类
         * @return
         */
        private Object[] mergeParameterTypesAndCallback(Object[] parameterTypes, XC_MethodHook methodHook) {

            if (parameterTypes == null || parameterTypes.length == 0) {
                return new Object[] { methodHook };
            }

            Object[] parameterTypesAndCallback = new Object[parameterTypes.length + 1];
            System.arraycopy(parameterTypes, 0, parameterTypesAndCallback, 0, parameterTypes.length);
            parameterTypesAndCallback[parameterTypes.length] = methodHook;

            return parameterTypesAndCallback;
        }
    }

    public final static class InternalMethodHookAdapter extends XC_MethodHook {

        private MethodHook.BeforeCallback beforeCallback;
        private MethodHook.AfterCallback afterCallback;
        private MethodHook.ThrowableCallback throwableCallback;

        InternalMethodHookAdapter(int priority,
                MethodHook.HookCallback hookCallback, MethodHook.ThrowableCallback throwableCallback) {
            this(priority, hookCallback, hookCallback, throwableCallback);
        }

        InternalMethodHookAdapter(int priority,
                MethodHook.BeforeCallback beforeCallback, MethodHook.ThrowableCallback throwableCallback) {
            this(priority, beforeCallback, null, throwableCallback);
        }

        InternalMethodHookAdapter(int priority,
                MethodHook.AfterCallback afterCallback, MethodHook.ThrowableCallback throwableCallback) {
            this(priority, null, afterCallback, throwableCallback);
        }

        InternalMethodHookAdapter(int priority,
                MethodHook.BeforeCallback beforeCallback,
                MethodHook.AfterCallback afterCallback, MethodHook.ThrowableCallback throwableCallback) {
            super(priority);
            this.beforeCallback = beforeCallback;
            this.afterCallback = afterCallback;
            this.throwableCallback = throwableCallback;
        }

        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            super.beforeHookedMethod(param);

            if (beforeCallback == null) return;

            try {
                beforeCallback.onBefore(param);
            } catch (Throwable tr) {
                throwableCallback.onThrowable(tr);
            }
        }

        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            super.afterHookedMethod(param);

            if (afterCallback == null) return;

            try {
                afterCallback.onAfter(param);
            } catch (Throwable tr) {
                throwableCallback.onThrowable(tr);
            }
        }
    }

    public final static class InternalReplacementAdapter extends XC_MethodReplacement {

        private MethodHook.ReplaceCallback replaceCallback;
        private MethodHook.ThrowableCallback throwableCallback;

        InternalReplacementAdapter(int priority, MethodHook.ReplaceCallback replaceCallback,
                                          MethodHook.ThrowableCallback throwableCallback) {
            super(priority);
            this.replaceCallback = replaceCallback;
            this.throwableCallback = throwableCallback;
        }

        @Override
        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
            try {
                return replaceCallback.onReplace(param);
            } catch (Throwable tr) {
                throwableCallback.onThrowable(tr);
            }
            return null;
        }
    }

    public final static class InternalUnhookAdapter implements MethodHook.Unhook {

        private XC_MethodHook.Unhook unhook;
        private Set<XC_MethodHook.Unhook> unhooks;

        InternalUnhookAdapter(XC_MethodHook.Unhook unhook) {
            this.unhook = unhook;
        }

        InternalUnhookAdapter(Set<XC_MethodHook.Unhook> unhooks) {
            this.unhooks = unhooks;
        }

        @Override
        public void unhook() {

            if (unhook != null) unhook.unhook();

            if (unhooks == null) return;

            for (XC_MethodHook.Unhook unhook : unhooks) {
                unhook.unhook();
            }
        }
    }

    public final static class InternalThrowableCallback implements MethodHook.ThrowableCallback {

        @Override
        public void onThrowable(Throwable tr) {
            // 直接输出
            XposedBridge.log(tr);
        }
    }

    public static class Builder {

        private ClassLoader mClassLoader;
        private MethodHook.ThrowableCallback mThrowableCallback;

        public Builder(XC_LoadPackage.LoadPackageParam packageParam) {
            mClassLoader = packageParam.classLoader;
        }

        public Builder(ClassLoader classLoader) {
            mClassLoader = classLoader;
        }

        public Builder throwableCallback(MethodHook.ThrowableCallback callback) {
            mThrowableCallback = callback;
            return this;
        }

        public XposedPlus build() {

            if (mClassLoader == null) {
                throw new IllegalArgumentException("LoadPackageParam must not be null.");
            }

            if (mThrowableCallback == null) {
                mThrowableCallback = new InternalThrowableCallback();
            }

            return new XposedPlus(mClassLoader, mThrowableCallback);
        }
    }
}
