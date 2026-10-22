# Daily English · Liquid Glass

Daily English 的第二个 Android 基线：白色极简、扁平大圆角、黑色选中胶囊，以及一条
iOS 风格的液态玻璃底部导航。它与原 Quiet Glass 客户端是两个独立安装的应用，共用同一
份 `content/` 语料和 `backend/` API。

- Application ID：`com.dailyenglish.liquidglass`
- Kotlin namespace：`com.dailyenglish.nativeapp`（沿用共享数据/领域层的包名）
- 版本：`1.2.0-liquidglass`
- 最低系统：Android 13（API 33），compileSdk / targetSdk 36
- 液态玻璃：[Kyant0/AndroidLiquidGlass](https://github.com/Kyant0/AndroidLiquidGlass) `io.github.kyant0:backdrop:1.0.6`

## 设计基线

- 白色/近白画布，黑色选中胶囊；蓝色为默认强调色，可在设置切换蓝/绿/石墨/琥珀。
- 内容卡片扁平：实色白、1dp 发丝线、大圆角，不用玻璃叠玻璃。
- 液态玻璃只用于底部 dock：20dp 级背景模糊、折射、色散边缘和高光；选中透镜可左右拖动切 tab，顶部圆形按钮保持实色白。
- `minSdk 33` 保证 API 33+ 的 AGSL 折射始终可用，不做旧版本玻璃降级。
- 暗色模式使用烟熏玻璃，不沿用浅色白主调。

主要流程已迁移到新基线：今日、练习、词汇、句子、笔记、记录、真题、登录与设置均使用新的白/蓝 token 与黑胶囊；学习记录已合并 CET-6/考研，不再提供筛选。液态玻璃集中在底部 dock，边缘状态、平板与暗色仍需继续验收。

## 构建

要求 JDK 17+、Android SDK 36，不需要 Node。首次构建会下载 Gradle 与依赖。

```bash
./gradlew assembleDebug
```

本地 SDK 路径写在 `local.properties`（已忽略）：

```properties
sdk.dir=D\:\\Software\\Tool\\AndroidStudioSDK
```

Windows 使用 `.\\gradlew.bat`，并确保 Java 17+（Android Studio JBR 即可）。

### API 地址

仓库不包含生产地址。构建时注入：

```bash
./gradlew assembleRelease -PapiBaseUrl=https://your-host:8443/
# 或
API_BASE_URL=https://your-host:8443/ ./gradlew assembleRelease
```

不注入时默认值是 `https://daily-english.invalid/`，请求会直接失败。地址必须以 `/` 结尾。

### 签名

`release` 签名参数与旧客户端相同（`keystoreFile` / `keystorePassword` / `keyAlias` /
`keyPassword`），但必须使用单独的 Liquid Glass keystore。不要复用旧 Quiet Glass 的 key。

## 内容同步

`syncContent` 任务（挂在 `preBuild` 上）从仓库根的 `content/` 复制 `words.json`、
`sentences.json`、`papers/**` 到 `app/src/main/assets/content/`。该目录已被 git 忽略：
语料只有一份，不要手改 assets 里的副本。

## CI / Release

- `.github/workflows/ci.yml`：单元测试 + debug 构建。
- `.github/workflows/android-release.yml`：tag `android-v*` 或手动触发，构建签名 APK 并
  发布 GitHub Release。需要 4 个签名 secrets 和可选的 `ANDROID_API_BASE_URL` variable。

## 第三方许可

Backdrop 及 Kyant Shapes 使用 Apache License 2.0，详见 `THIRD_PARTY_NOTICES.md`。
