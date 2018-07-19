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

    private XC_LoadPackage.LoadPackageParam mPackageParam;
    private MethodHook.ThrowableCallback mThrowableCallback;

    private static XposedPlus sXposedPlus;

    private XposedPlus(XC_LoadPackage.LoadPackageParam packageParam, MethodHook.ThrowableCallback throwableCallback) {
        mPackageParam = packageParam;
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

        if (sXposedPlus == null) {
            sXposedPlus = new Builder(packageParam).build();
        }

        if (sXposedPlus.mPackageParam == packageParam) {
            return sXposedPlus;
        }

        return new XposedPlus(packageParam, sXposedPlus.mThrowableCallback);
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
        return XposedHelpers.findClass(className, mPackageParam.classLoader);
    }

    private static class InternalMethodHook implements MethodHook {

        private boolean constructor;
        private XposedPlus xposedPlus;
        private XC_LoadPackage.LoadPackageParam packageParam;
        private MethodHook.ThrowableCallback throwableCallback;
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
            this(false, xposedPlus, className, null, methodName, parameterTypes);
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
            this.packageParam = xposedPlus.mPackageParam;
            this.throwableCallback = xposedPlus.mThrowableCallback;
        }

        @Override
        public XC_MethodHook.Unhook hook(BeforeCallback callback) {
            return handlerHook(
                    new InternalMethodHookAdapter(callback, xposedPlus.mThrowableCallback));
        }

        @Override
        public XC_MethodHook.Unhook hook(AfterCallback callback) {
            return handlerHook(
                    new InternalMethodHookAdapter(callback, xposedPlus.mThrowableCallback));
        }

        @Override
        public XC_MethodHook.Unhook replace(ReplaceCallback callback) {
            return handlerHook(
                    new InternalReplacementAdapter(callback, xposedPlus.mThrowableCallback));
        }

        @Override
        public XC_MethodHook.Unhook hook(HookCallback callback) {
            return handlerHook(
                    new InternalMethodHookAdapter(callback, xposedPlus.mThrowableCallback));
        }

        /**
         * 处理Hook
         * @param methodHook
         * @return
         */
        private XC_MethodHook.Unhook handlerHook(XC_MethodHook methodHook) {
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
        private XC_MethodHook.Unhook handlerMethod(XC_MethodHook methodHook) {

            try {
                return XposedHelpers.findAndHookMethod(getHookClass(), methodName,
                        mergeParameterTypesAndCallback(parameterTypes, methodHook));
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
        private XC_MethodHook.Unhook handlerConstructor(XC_MethodHook methodHook) {

            try {
                return XposedHelpers.findAndHookConstructor(getHookClass(),
                        mergeParameterTypesAndCallback(parameterTypes, methodHook));
            } catch (Throwable tr) {
                throwableCallback.onThrowable(tr);
            }
            return null;
        }

        /**
         * 获取Hook的Class
         * @return
         */
        private Class<?> getHookClass() {
            if (clazz == null) {
                clazz = XposedHelpers.findClass(className, packageParam.classLoader);
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

    public static class InternalMethodHookAdapter extends XC_MethodHook {

        private MethodHook.BeforeCallback beforeCallback;
        private MethodHook.AfterCallback afterCallback;
        private MethodHook.ThrowableCallback throwableCallback;

        public InternalMethodHookAdapter(
                MethodHook.HookCallback hookCallback, MethodHook.ThrowableCallback throwableCallback) {
            this(hookCallback, hookCallback, throwableCallback);
        }

        public InternalMethodHookAdapter(
                MethodHook.BeforeCallback beforeCallback, MethodHook.ThrowableCallback throwableCallback) {
            this(beforeCallback, null, throwableCallback);
        }

        public InternalMethodHookAdapter(
                MethodHook.AfterCallback afterCallback, MethodHook.ThrowableCallback throwableCallback) {
            this(null, afterCallback, throwableCallback);
        }

        public InternalMethodHookAdapter(
                MethodHook.BeforeCallback beforeCallback,
                MethodHook.AfterCallback afterCallback, MethodHook.ThrowableCallback throwableCallback) {
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

    public static class InternalReplacementAdapter extends XC_MethodReplacement {

        private MethodHook.ReplaceCallback replaceCallback;
        private MethodHook.ThrowableCallback throwableCallback;

        public InternalReplacementAdapter(MethodHook.ReplaceCallback replaceCallback,
                                          MethodHook.ThrowableCallback throwableCallback) {
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

    public static class InternalThrowableCallback implements MethodHook.ThrowableCallback {

        @Override
        public void onThrowable(Throwable tr) {
            // 直接输出
            XposedBridge.log(tr);
        }
    }

    public static class Builder {

        private final XC_LoadPackage.LoadPackageParam mPackageParam;
        private MethodHook.ThrowableCallback mThrowableCallback;

        public Builder(XC_LoadPackage.LoadPackageParam packageParam) {
            mPackageParam = packageParam;
        }

        public Builder throwableCallback(MethodHook.ThrowableCallback callback) {
            mThrowableCallback = callback;
            return this;
        }

        public XposedPlus build() {

            if (mPackageParam == null) {
                throw new IllegalArgumentException("LoadPackageParam must not be null.");
            }

            if (mThrowableCallback == null) {
                mThrowableCallback = new InternalThrowableCallback();
            }

            return new XposedPlus(mPackageParam, mThrowableCallback);
        }
    }
}
