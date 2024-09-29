## SettingsFirewall
An Xposed module that blocks shitty apps from accessing your system settings (for example, check if development settings is enabled on the device, or check if there are running accessibility services).

Please note that only accesses to system settings (e.g. [Settings APIs](https://developer.android.com/reference/android/provider/Settings) or `/system/bin/settings get`) can be intercepted by this module. 
Accesses to system properties (e.g. `android.os.SystemProperties APIs`, `__system_property_get` or `getprop`) or other system APIs cannot be blocked.

### Usage
For LSPosed users, select only "System Framework" and reboot.

For [Dreamland](https://github.com/canyie/Dreamland) users, select "Settings Provider" (`com.android.providers.settings`) and reboot.



```
SettingsFirewall
├─ .gitignore
├─ app
│  ├─ .gitignore
│  ├─ proguard-rules.pro
│  └─ src
│     └─ main
│        ├─ aidl
│        │  └─ top
│        │     └─ canyie
│        │        └─ settingsfirewall
│        │           ├─ ISettingsFirewall.aidl
│        │           └─ Replacement.aidl
│        ├─ AndroidManifest.xml
│        ├─ assets
│        │  └─ xposed_init
│        ├─ java
│        │  └─ top
│        │     └─ canyie
│        │        └─ settingsfirewall
│        │           ├─ App.java
│        │           ├─ AppInfo.java
│        │           ├─ AppListAdapter.java
│        │           ├─ MainActivity.java
│        │           ├─ Replacement.java
│        │           ├─ SettingListAdapter.java
│        │           ├─ SettingsEditActivity.java
│        │           ├─ SettingsFirewallService.java
│        │           └─ SettingsProviderHook.java
│        └─ res
│           ├─ drawable
│           │  └─ ic_launcher_background.xml
│           ├─ drawable-v24
│           │  └─ ic_launcher_foreground.xml
│           ├─ layout
│           │  ├─ app_item.xml
│           │  ├─ edit_dialog.xml
│           │  ├─ main.xml
│           │  ├─ settings.xml
│           │  └─ setting_item.xml
│           ├─ mipmap-anydpi-v26
│           │  ├─ ic_launcher.xml
│           │  └─ ic_launcher_round.xml
│           ├─ mipmap-hdpi
│           │  ├─ ic_launcher.webp
│           │  └─ ic_launcher_round.webp
│           ├─ mipmap-mdpi
│           │  ├─ ic_launcher.webp
│           │  └─ ic_launcher_round.webp
│           ├─ mipmap-xhdpi
│           │  ├─ ic_launcher.webp
│           │  └─ ic_launcher_round.webp
│           ├─ mipmap-xxhdpi
│           │  ├─ ic_launcher.webp
│           │  └─ ic_launcher_round.webp
│           ├─ mipmap-xxxhdpi
│           │  ├─ ic_launcher.webp
│           │  └─ ic_launcher_round.webp
│           ├─ values
│           │  ├─ arrays.xml
│           │  ├─ colors.xml
│           │  ├─ strings.xml
│           │  └─ themes.xml
│           ├─ values-night
│           │  └─ themes.xml
│           ├─ values-zh-rCN
│           │  └─ strings.xml
│           └─ xml
│              ├─ backup_rules.xml
│              └─ data_extraction_rules.xml
├─ gradle
│  └─ wrapper
│     ├─ gradle-wrapper.jar
│     └─ gradle-wrapper.properties
├─ gradle.properties
├─ gradlew
├─ gradlew.bat
└─ README.md

```