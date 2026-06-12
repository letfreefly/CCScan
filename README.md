# CCScan 悬浮扫码

一个基于 Android 的悬浮球条形码识别工具。长按悬浮球即可自动识别快递面单上的条形码，并自动复制单号到剪贴板。

## ✨ 功能特点

- 🎈 **全局悬浮球** — 在任何应用界面都可使用
- 👆 **长按触发** — 长按悬浮球 500ms 启动扫码
- 🎯 **实时识别** — CameraX 实时预览 + Google ML Kit 自动识别
- 📋 **自动复制** — 识别成功自动复制到剪贴板，可直接粘贴
- 🌐 **无需联网** — 内置模型，完全离线识别
- 📦 **支持格式** — CODE-128 / CODE-39 / CODE-93 / EAN-13 / EAN-8 / UPC-A / UPC-E / ITF / Codabar

## 📦 下载安装

### 方式一：从 GitHub Actions 获取自动构建 APK

每次代码提交后，GitHub Actions 会自动构建 APK：

1. 打开仓库首页 → 点击顶部的 **Actions** 标签
2. 点击最近一次 **Build APK** 工作流
3. 页面底部 **Artifacts** 区域下载 `CCScan-debug-apk.zip`
4. 解压后安装 `app-debug.apk` 到 Android 设备

### 方式二：从 Releases 下载

当推送 tag（如 `v1.0`）时，GitHub Actions 会自动创建 Release：

```bash
git tag -a v1.0 -m "Release v1.0"
git push origin v1.0
```

然后在仓库右侧 **Releases** 区域下载 APK。

### 方式三：本地使用 Android Studio 构建

```bash
# 1. 用 Android Studio 打开项目文件夹
# 2. 等待 Gradle 同步完成
# 3. 连接 Android 设备（开启 USB 调试）
# 4. 点击 Run ▶ 按钮，或执行：
./gradlew assembleDebug   # macOS/Linux
gradlew.bat assembleDebug  # Windows
```

## 🚀 使用说明

1. **安装并打开 App** → 授予「相机权限」和「悬浮窗权限」
2. **点击「启动悬浮球」** → 屏幕边缘出现紫色悬浮球
3. **长按悬浮球** → 弹出透明扫描界面
4. **将摄像头对准条形码** → 自动识别并震动提示
5. **单号已复制** → 粘贴到任何输入框即可
6. **点击屏幕空白处** → 关闭扫描

## 🛠️ 技术栈

| 组件 | 技术 |
|------|------|
| 开发语言 | Kotlin 1.9 |
| 最低版本 | Android 7.0 (API 24) |
| 目标版本 | Android 14 (API 34) |
| 相机 | CameraX 1.4 |
| 条码识别 | Google ML Kit Barcode Scanning 17.2 |
| UI | AndroidX AppCompat + Material |
| 构建 | Gradle 8.6 |

## 📁 项目结构

```
CCSCAN/
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/ccscan/barcode/
│       │   ├── MainActivity.kt         # 权限申请 + 启动悬浮球
│       │   ├── FloatingBallService.kt  # 悬浮球 + 长按识别
│       │   └── ScanActivity.kt         # CameraX + ML Kit 实时识别
│       └── res/
│           ├── drawable/                # 悬浮球背景、扫描框
│           ├── layout/                  # 界面布局
│           └── values/                  # 字符串、颜色、主题
├── .github/workflows/build.yml          # GitHub Actions 自动构建
├── .gitignore
├── build.gradle
├── settings.gradle
└── gradle.properties
```

## 🔧 本地开发

### 前置要求

- Android Studio Hedgehog (2023.1) 或更高版本
- Android SDK Platform 34
- Java 17（项目已配置）

### 构建步骤

```bash
# 克隆项目
git clone https://github.com/你的用户名/CCScan.git
cd CCScan

# 使用 Android Studio 打开
# File → Open → 选择 CCScan 文件夹

# 或使用命令行构建
./gradlew assembleDebug
# 输出: app/build/outputs/apk/debug/app-debug.apk
```

## 📄 许可协议

MIT License - 可自由使用、修改、分发。

---

⚠️ **注意**：首次安装后需要手动授予悬浮窗权限（Android 6+），请在系统设置中开启。
