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

    XC_MethodHook.Unhook hook(BeforeCallback callback);

    XC_MethodHook.Unhook hook(AfterCallback callback);

    XC_MethodHook.Unhook replace(ReplaceCallback callback);

    XC_MethodHook.Unhook hook(HookCallback callback);

    MethodHook throwable(ThrowableCallback callback);

    interface HookCallback extends BeforeCallback, AfterCallback {

    }

    interface BeforeCallback {

        void onBefore(XC_MethodHook.MethodHookParam param);
    }

    interface AfterCallback {

        void onAfter(XC_MethodHook.MethodHookParam param);
    }

    interface ReplaceCallback {

        Object onReplace(XC_MethodHook.MethodHookParam param);
    }

    interface ThrowableCallback {

        void onThrowable(Throwable tr);
    }
}
