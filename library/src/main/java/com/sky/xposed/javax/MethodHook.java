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

import de.robv.android.xposed.XC_MethodHook;

/**
 * Created by sky on 2018/7/18.
 */
public interface MethodHook {

    /**
     * @deprecated
     * @param callback
     * @return
     */
    Unhook hook(BeforeCallback callback);

    /**
     * @deprecated
     * @param callback
     * @return
     */
    Unhook hook(AfterCallback callback);

    Unhook before(BeforeCallback callback);

    Unhook after(AfterCallback callback);

    Unhook replace(ReplaceCallback callback);

    Unhook hook(HookCallback callback);

    Unhook hook(BeforeCallback beforeCallback, AfterCallback afterCallback);

    /**
     * 异常回调处理
     * @param callback
     * @return
     */
    MethodHook throwable(ThrowableCallback callback);

    /**
     * 处理所有的方法
     * @return
     */
    MethodHook multiple();

    /**
     * 设置优先级
     * @param priority {@link de.robv.android.xposed.callbacks.XCallback}
     * @return
     */
    MethodHook priority(int priority);

    /**
     * 监听方法回调处理时长
     * @param timeOut
     * @return
     */
    MethodHook monitor(int timeOut, MonitorCallback callback);


    interface HookCallback extends BeforeCallback, AfterCallback {

    }

    interface BeforeCallback {

        void onBefore(XC_MethodHook.MethodHookParam param) throws Throwable;
    }

    interface AfterCallback {

        void onAfter(XC_MethodHook.MethodHookParam param) throws Throwable;
    }

    /**
     * 替换的回调方法
     */
    interface ReplaceCallback {

        Object onReplace(XC_MethodHook.MethodHookParam param) throws Throwable;
    }

    /**
     * 异常回调的方法
     */
    interface ThrowableCallback {

        void onThrowable(Throwable tr);
    }

    /**
     * 监听回调的方法
     */
    interface MonitorCallback {

        void onMethodTimeOut(XC_MethodHook.MethodHookParam param);
    }

    interface Unhook {

        void unhook();
    }
}
