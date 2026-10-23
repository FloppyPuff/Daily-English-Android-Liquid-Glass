# Daily English · Liquid Glass

Daily English 的第二个 Android 基线：白色极简、扁平大圆角，叠加一套克制的液态玻璃控件。
它与 Quiet Glass 客户端是两个独立安装的应用，共用同一份语料与同一套后端 API。

## 关键信息

| 项 | 值 |
| --- | --- |
| Application ID | `com.dailyenglish.liquidglass` |
| Kotlin namespace | `com.dailyenglish.nativeapp` |
| 当前版本 | `1.3.3-liquidglass`（versionCode 15） |
| 最低系统 | Android 13（API 33） |
| compileSdk / targetSdk | 36 / 36 |
| UI | Jetpack Compose + Material 3 |
| 玻璃库 | [Kyant0/AndroidLiquidGlass](https://github.com/Kyant0/AndroidLiquidGlass) `io.github.kyant0:backdrop:1.0.6` |
| 许可 | MIT（本仓库），第三方见 `THIRD_PARTY_NOTICES.md` |

## 设计基线

- 白色/近白画布，内容卡片实色白、1dp 发丝线、大圆角；不使用营销式装饰。
- 强调色默认蓝色，可在设置切换蓝 / 绿 / 石墨 / 琥珀，选择写入 DataStore 并立即生效。
- 暗色模式使用烟熏玻璃，跟随系统自动切换。
- `minSdk 33` 保证 AGSL 折射与模糊始终可用，不做旧版本玻璃降级。

液态玻璃覆盖三类控件，共用同一套视觉配方（容器色、模糊、折射、整圈高光、投影、内阴影）：

1. **底部 dock**：静止时选中胶囊内嵌在 bar 内（50dp / 58dp），按压或拖动时放大 1.45 倍溢出
   bar；透镜由「可见 bar + 隐藏的强调色孪生层 + 合成 backdrop」三层实现，滑动带色散与速度
   挤压。点击与横向拖动由同一个手势层处理，避免两层手势互相吞事件。
2. **顶部栏按钮**：返回、专注、设置。按压以触点为原点向外扩张，拖动时按 0.05 比例跟手并沿
   拖动方向拉伸形变，松手弹回。
3. **分段滑块**：笔记筛选、练习模式、词库筛选、真题筛选、登录/注册切换，结构与 dock 一致，
   支持点击与拖动，按压时出现白色 bloom。

玻璃控件只采样独立的背景层，不采样自身，避免递归鬼影；详情页隐藏 dock，四个主页面底部
留白 112dp，保证内容可完整滚动到 dock 之上。

## 功能

- **今日**：今日计划、学习进度、继续学习、连续天数与近七天节奏。
- **练习**：词汇（闪卡记忆 / 拼写自测 / 词库浏览）、句子翻译、复习计划、真题（按套/逐组）、
  专注计时。
- **笔记**：收藏词与句、薄弱 & 错题、搜索、直接进入练习。
- **记录**：累计已学、较熟悉词汇、学习天数、专注分钟、真题答题、词汇掌握度。CET-6 与考研
  进度合并统计，不提供筛选。
- **设置**：账号、强调色、同步、退出登录、关于。
- **鉴权**：注册 / 登录 / 游客模式；本地保存会话。

## 技术栈

| 层 | 选型 |
| --- | --- |
| 构建 | AGP 9.0.1 · Gradle 9.1.0 · Kotlin 2.3.20 · JDK 17 toolchain |
| UI | Compose BOM 2026.03.01 · Material 3 · Material Icons Extended |
| 导航 | Navigation 3（NavDisplay + NavKey） |
| 依赖注入 | Hilt 2.60.1（KSP） |
| 本地数据 | Room 2.8.5 · DataStore Preferences 1.2.1 |
| 网络 | Retrofit 2.12.0 · OkHttp 4.12.0 · kotlinx.serialization |
| 后台 | WorkManager 2.11.2 |
| 玻璃 | Backdrop 1.0.6（含 Kyant Shapes） |

## 项目结构

```text
app/src/main/java/com/dailyenglish/nativeapp/
├── core/        领域模型、学习引擎、同步与校验（纯 Kotlin，可单测）
├── data/        语料、Room、DataStore、Retrofit、仓库与同步
├── di/          Hilt 模块
├── feature/     auth / study / notebook / papers / stats / settings
├── theme/       颜色、字体、形状、QuietBackground、玻璃工具
├── ui/          AppRoot、导航（MainScaffold）、components（LiquidGlassDock、LiquidControls）
└── MainActivity.kt
content/         唯一语料源（words.json、sentences.json、papers/**）
```

## 构建

要求 JDK 17+、Android SDK 36，不需要 Node。

```bash
./gradlew assembleDebug          # Windows 用 .\gradlew.bat
./gradlew testDebugUnitTest      # 单元测试
```

`local.properties` 中的 SDK 路径已被忽略：

```properties
sdk.dir=/path/to/Android/Sdk
```

### API 地址

生产地址不进入仓库，构建时注入（必须以 `/` 结尾）：

```bash
./gradlew assembleRelease -PapiBaseUrl=https://your-host:8443/
# 或
API_BASE_URL=https://your-host:8443/ ./gradlew assembleRelease
```

未注入时默认 `https://daily-english.invalid/`，请求会在运行时直接失败，不会误连其他服务。

### 本地并存调试

```bash
./gradlew assembleDebug -PdevSuffix=1   # 生成 com.dailyenglish.liquidglass.dev
```

## 签名

Release 签名需要四个参数（属性或环境变量），缺一即构建未签名包：

`keystoreFile` / `keystorePassword` / `keyAlias` / `keyPassword`
（对应 `KEYSTORE_FILE` / `KEYSTORE_PASSWORD` / `KEY_ALIAS` / `KEY_PASSWORD`）

必须使用 Liquid Glass 专属 keystore，不要复用 Quiet Glass 的 key；两把 key 都已在 GitHub
Secrets 配置，禁止重新生成。

## 内容同步

`syncContent` 任务挂在 `preBuild` 上，把仓库根 `content/` 下的 `words.json`、`sentences.json`
与 `papers/**` 复制到 `app/src/main/assets/content/`。该目录被 git 忽略：语料只有一份，不要
手改 assets 里的副本。

## CI / Release

- `.github/workflows/ci.yml`：push / PR 到 main 时跑单元测试与 debug 构建。
- `.github/workflows/android-release.yml`：推送 `android-v*` tag 或手动触发，构建签名 APK 并
  发布 GitHub Release。
  - Secrets：`ANDROID_KEYSTORE_BASE64`、`ANDROID_KEYSTORE_PASSWORD`、`ANDROID_KEY_ALIAS`、
    `ANDROID_KEY_PASSWORD`
  - Variable：`ANDROID_API_BASE_URL`
  - 产物：`Daily-English-Liquid-Glass-<version>-release.apk` 及同名 `.sha256`
  - Release 正文由 `CHANGELOG.md` 中对应版本的段落生成；同一版本重跑会刷新正文并以
    `--clobber` 覆盖附件。

发版流程：改 `versionCode` / `versionName` → 在 `CHANGELOG.md` 增加一节 → 提交 → 打
`android-v<version>` tag 并推送。

## 版本记录

见 [CHANGELOG.md](CHANGELOG.md)。当前发布页：
[Releases](https://github.com/FloppyPuff/Daily-English-Android-Liquid-Glass/releases)。

## 第三方许可

Backdrop 与 Kyant Shapes 使用 Apache License 2.0，完整声明见
[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。
