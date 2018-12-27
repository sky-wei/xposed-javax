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

/**
 * Created by sky on 2018/12/27.
 */
public final class XposedUtil {

    private XposedUtil() {
    }

    public static MethodHook findMethod(Class<?> clazz, String methodName, Object... parameterTypes) {
        return XposedPlus.get().findMethod(clazz, methodName, parameterTypes);
    }

    public static MethodHook findMethod(String className, String methodName, Object... parameterTypes) {
        return XposedPlus.get().findMethod(className, methodName, parameterTypes);
    }

    public static MethodHook findConstructor(Class<?> clazz, Object... parameterTypes) {
        return XposedPlus.get().findConstructor(clazz, parameterTypes);
    }

    public static MethodHook findConstructor(String className, Object... parameterTypes) {
        return XposedPlus.get().findConstructor(className, parameterTypes);
    }

    public static Class<?> findClass(String className) {
        return XposedPlus.get().findClass(className);
    }
}
