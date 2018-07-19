# Xposed-javax

主要为Xposed提供一些公共的方法，以方便自己在项目中使用。

### How to

To get a Git project into your build:

Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

Step 2. Add the dependency

```
dependencies {
        implementation 'com.github.sky-wei:xposed-javax:1.0.0'
}
```

Step 2. Use

```
XposedPlus.setDefaultInstance(new XposedPlus.Builder(LoadPackageParam).build());

XposedPlus.get()
        .findMethod("className", "methodName", "paramTypes")
        .hook(new MethodHook.AfterCallback() {
            @Override
            public void onAfter(XC_MethodHook.MethodHookParam methodHookParam) {
                // ....
            }
    }); 
    
XposedPlus.get()
        .findMethod("className", "methodName", "paramTypes")
        .hook(new MethodHook.HookCallback() {
            @Override
            public void onAfter(XC_MethodHook.MethodHookParam methodHookParam) {
                // ....
            }

            @Override
            public void onBefore(XC_MethodHook.MethodHookParam methodHookParam) {
                // ....
            }
    });  
    
XposedPlus.with(LoadPackageParam)
        .findConstructor("className", "paramTypes")
        .hook(new MethodHook.BeforeCallback() {
            @Override
            public void onBefore(XC_MethodHook.MethodHookParam methodHookParam) {
                // ....
            }
        });       
```



## License

    Copyright 2018 The sky Authors

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.