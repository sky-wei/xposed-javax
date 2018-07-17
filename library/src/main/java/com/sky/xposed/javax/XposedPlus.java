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

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedPlus {

    XC_LoadPackage.LoadPackageParam packageParam;
    ThrowableListener throwableListener;

    private static XposedPlus sXposedPlus;

    private XposedPlus(XC_LoadPackage.LoadPackageParam packageParam, ThrowableListener throwableListener) {
        this.packageParam = packageParam;
        this.throwableListener = throwableListener;
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

        if (sXposedPlus == null) {
            sXposedPlus = new Builder(packageParam).build();
        }

        if (sXposedPlus.packageParam == packageParam) {
            return sXposedPlus;
        }

        return new XposedPlus(packageParam, sXposedPlus.throwableListener);
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
        return XposedHelpers.findClass(className, packageParam.classLoader);
    }

    private static class InternalMethodHook implements MethodHook {

        private boolean constructor;
        private XposedPlus xposedPlus;
        private XC_LoadPackage.LoadPackageParam packageParam;
        private ThrowableListener throwableListener;
        private String className;
        private Class<?> clazz;
        private String methodName;
        private Object[] parameterTypes;

        public InternalMethodHook(XposedPlus xposedPlus, String className, Object[] parameterTypes) {
            this(true, xposedPlus, className, null, null, parameterTypes);
        }

        public InternalMethodHook(XposedPlus xposedPlus, Class<?> clazz, Object[] parameterTypes) {
            this(true, xposedPlus, null, clazz, null, parameterTypes);
        }

        public InternalMethodHook(XposedPlus xposedPlus, String className, String methodName, Object[] parameterTypes) {
            this(false, xposedPlus, className, null, null, parameterTypes);
        }

        public InternalMethodHook(XposedPlus xposedPlus, Class<?> clazz, String methodName, Object[] parameterTypes) {
            this(false, xposedPlus, null, clazz, methodName, parameterTypes);
        }

        public InternalMethodHook(boolean constructor, XposedPlus xposedPlus, String className,
                                  Class<?> clazz, String methodName, Object[] parameterTypes) {
            this.constructor = constructor;
            this.xposedPlus = xposedPlus;
            this.className = className;
            this.clazz = clazz;
            this.methodName = methodName;
            this.parameterTypes = parameterTypes;
            this.packageParam = xposedPlus.packageParam;
            this.throwableListener = xposedPlus.throwableListener;
        }

        @Override
        public XC_MethodHook.Unhook beforeHook(BeforeHookCallback callback) {

            if (clazz == null || TextUtils.isEmpty(className)) {
                throw new IllegalArgumentException("clazz or className must not be null.");
            }
            return handlerHook(
                    new InternalMethodHookAdapter(callback, xposedPlus.throwableListener));
        }

        @Override
        public XC_MethodHook.Unhook afterHook(AfterHookCallback callback) {

            if (clazz == null || TextUtils.isEmpty(className)) {
                throw new IllegalArgumentException("clazz or className must not be null.");
            }
            return handlerHook(
                    new InternalMethodHookAdapter(callback, xposedPlus.throwableListener));
        }

        @Override
        public XC_MethodHook.Unhook replace(ReplaceCallback callback) {

            if (clazz == null || TextUtils.isEmpty(className)) {
                throw new IllegalArgumentException("clazz or className must not be null.");
            }
            return handlerHook(
                    new InternalReplacementAdapter(callback, xposedPlus.throwableListener));
        }

        @Override
        public XC_MethodHook.Unhook hook(HookCallback callback) {

            if (clazz == null || TextUtils.isEmpty(className)) {
                throw new IllegalArgumentException("clazz or className must not be null.");
            }
            return handlerHook(
                    new InternalMethodHookAdapter(callback, xposedPlus.throwableListener));
        }

        private XC_MethodHook.Unhook handlerHook(XC_MethodHook methodHook) {
            return constructor ? handlerConstructor(methodHook) : handlerMethod(methodHook);
        }

        private XC_MethodHook.Unhook handlerMethod(XC_MethodHook methodHook) {

            try {
                return XposedHelpers.findAndHookMethod(getHookClass(), methodName,
                        mergeParameterTypesAndCallback(parameterTypes, methodHook));
            } catch (Throwable tr) {
                throwableListener.onThrowable(tr);
            }
            return null;
        }

        private XC_MethodHook.Unhook handlerConstructor(XC_MethodHook methodHook) {

            try {
                return XposedHelpers.findAndHookConstructor(getHookClass(),
                        mergeParameterTypesAndCallback(parameterTypes, methodHook));
            } catch (Throwable tr) {
                throwableListener.onThrowable(tr);
            }
            return null;
        }

        private Class<?> getHookClass() {
            if (clazz == null) {
                clazz = XposedHelpers.findClass(className, packageParam.classLoader);
            }
            return clazz;
        }

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

    public static class InternalMethodHookAdapter extends XC_MethodHook {

        private BeforeHookCallback beforeCallback;
        private AfterHookCallback afterCallback;
        private ThrowableListener listener;

        public InternalMethodHookAdapter(
                HookCallback callback, ThrowableListener listener) {
            this(callback, callback, listener);
        }

        public InternalMethodHookAdapter(
                BeforeHookCallback callback, ThrowableListener listener) {
            this(callback, null, listener);
        }

        public InternalMethodHookAdapter(
                AfterHookCallback callback, ThrowableListener listener) {
            this(null, callback, listener);
        }

        public InternalMethodHookAdapter(
                BeforeHookCallback beforeCallback,
                AfterHookCallback afterCallback, ThrowableListener listener) {
            this.beforeCallback = beforeCallback;
            this.afterCallback = afterCallback;
            this.listener = listener;
        }

        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            super.beforeHookedMethod(param);

            if (beforeCallback == null) return;

            try {
                beforeCallback.onBeforeHook(param);
            } catch (Throwable tr) {
                listener.onThrowable(tr);
            }
        }

        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            super.afterHookedMethod(param);

            if (afterCallback == null) return;

            try {
                afterCallback.onAfterHook(param);
            } catch (Throwable tr) {
                listener.onThrowable(tr);
            }
        }
    }

    public static class InternalReplacementAdapter extends XC_MethodReplacement {

        private ReplaceCallback callback;
        private ThrowableListener listener;

        public InternalReplacementAdapter(ReplaceCallback callback, ThrowableListener listener) {
            this.callback = callback;
            this.listener = listener;
        }

        @Override
        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
            try {
                return callback.onReplace(param);
            } catch (Throwable tr) {
                listener.onThrowable(tr);
            }
            return null;
        }
    }

    public static class InternalThrowableListener implements ThrowableListener {

        @Override
        public void onThrowable(Throwable tr) {
            // 直接输出
            XposedBridge.log(tr);
        }
    }

    public interface MethodHook {

        XC_MethodHook.Unhook beforeHook(BeforeHookCallback callback);

        XC_MethodHook.Unhook afterHook(AfterHookCallback callback);

        XC_MethodHook.Unhook replace(ReplaceCallback callback);

        XC_MethodHook.Unhook hook(HookCallback callback);
    }

    public interface HookCallback extends BeforeHookCallback, AfterHookCallback {

    }

    public interface BeforeHookCallback {

        void onBeforeHook(XC_MethodHook.MethodHookParam param);
    }

    public interface AfterHookCallback {

        void onAfterHook(XC_MethodHook.MethodHookParam param);
    }

    public interface ReplaceCallback {

        Object onReplace(XC_MethodHook.MethodHookParam param);
    }

    public interface ThrowableListener {

        void onThrowable(Throwable tr);
    }

    public static class Builder {

        private final XC_LoadPackage.LoadPackageParam packageParam;
        private ThrowableListener throwableListener;

        public Builder(XC_LoadPackage.LoadPackageParam packageParam) {
            this.packageParam = packageParam;
        }

        public Builder throwableListener(ThrowableListener throwableListener) {
            this.throwableListener = throwableListener;
            return this;
        }

        public XposedPlus build() {

            if (packageParam == null) {
                throw new IllegalArgumentException("LoadPackageParam must not be null.");
            }

            if (throwableListener == null) {
                throwableListener = new InternalThrowableListener();
            }

            return new XposedPlus(packageParam, throwableListener);
        }
    }
}
