# KBBSToper

检测苦力怕BBS服务器宣传贴顶帖后, 玩家领取奖励的插件.

本插件与 BBSToper的权限、指令、变量均通用，但数据库不通用！！！

## 多模块结构

```
core/    平台无关的核心逻辑: 配置/语言/爬虫/奖励判定/SQL/命令/占位符取值
         (数据库表结构、奖励规则、抓取逻辑两平台完全一致)
bukkit/  Java 版(Paper/Spigot/Leaves): 箱子 GUI + 可点击聊天 + PlaceholderAPI + bStats
nukkit/  基岩版(Nukkit): 原生表单界面(FormWindow) + 内置 sqlite/mysql 驱动 + jsoup
```

### 构建

```bash
./gradlew build
```

产物:

- `bukkit/build/libs/KBBSToper-Bukkit-<版本>.jar` — Java 版, 放入 Paper/Spigot 的 plugins/
- `nukkit/build/libs/KBBSToper-Nukkit-<版本>.jar` — 基岩版, 放入 Nukkit 的 plugins/

两个版本共用 `core/src/main/resources/` 下的 `config.yml` 与 `lang.yml`
(单一来源, 改一份两平台生效). 版本号在 `config.yml` 的 `version` 字段,
与插件版本不一致时启动会自动备份旧配置并释放新模板.

### 两平台差异

| 功能        | Bukkit(Java版)                         | Nukkit(基岩版)                     |
| ----------- | -------------------------------------- | ---------------------------------- |
| 主界面      | 箱子GUI (gui.yml 布局)                 | 原生表单 (FormWindowSimple)        |
| 绑定输入    | 可点击聊天补全 + 临时聊天监听          | 表单输入框                          |
| 占位符      | PlaceholderAPI 扩展 (7+ 个)            | 不提供(基岩端无统一 PAPI)          |
| 宣传帖链接  | 可点击聊天消息                         | 表单内展示链接文本供复制            |
| 统计        | bStats                                 | 无                                 |

基岩版注意: 玩家 UUID 由服务端按名字生成, 与 Java 版 Mojang UUID 不互通,
因此**数据库不能跨 Java/基岩服共用**.

## 用到的库

1. [Jsoup](https://jsoup.org/)
2. [PlaceHolderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI) (仅 Bukkit 端)
3. sqlite-jdbc / mysql-connector-j (两平台均内置)

## 使用方法

1. 构建 jar (见上) 或去 [releases](https://github.com/SnowCherryServer/KBBSToper/releases) 下载
2. 将 jar 放入对应服务端的 plugins 文件夹
3. 前往 klpbbs 复制您的帖子id并替换掉配置文件中默认链接中的id
4. 重启/启动服务器

## 命令&权限

**玩家默认拥有`bbstoper.user`权限**

| bbstoper.user的子权限 |
| --------------------- |
| `bbstoper.binding`    |
| `bbstoper.reward`     |

**op默认拥有`bbstoper.admin`权限**

| bbstoper.admin的子权限         |
| ------------------------------ |
| `bbstoper.testreward`          |
| `bbstoper.list`                |
| `bbstoper.top`                 |
| `bbstoper.check`               |
| `bbstoper.delete`              |
| `bbstoper.reload`              |
| `bbstoper.bypassquerycooldown` |

**/kbbstoper /poster /bt 都是可用命令别名**

| 命令                               | 权限                           | 描述                                          |
| ---------------------------------- | ------------------------------ | --------------------------------------------- |
| `/bt`                        | 无需权限                       | 打开界面(Java版箱子GUI / 基岩版表单)          |
| `/bt help`                   | 无需权限                       | 显示帮助信息                                  |
| `/bt binding <苦力怕BBS论坛ID>`  | `bbstoper.binding`             | 绑定论坛账号, 注意这里是ID不是uid             |
| `/bt reward`                 | `bbstoper.reward`              | 领取奖励                                      |
| `/bt testreward [模式]`      | `bbstoper.testreward`          | 测试奖励, 模式: `normal` `incentive` `offday` |
| `/bt list <页数>`            | `bbstoper.list`                | 列出所有顶帖者                                |
| `/bt top <页数>`             | `bbstoper.top`                 | 按照顶贴次数列排名出所有已绑定玩家            |
| 无                                 | `bbstoper.bypassquerycooldown` | 绕过查询冷却                                  |
| `/bt check bbsid <论坛ID>`   | `bbstoper.check`               | 查看一个论坛id的绑定者                        |
| `/bt check player <玩家ID>`  | `bbstoper.check`               | 查看一个玩家绑定的论坛id                      |
| `/bt delete player <玩家ID>` | `bbstoper.delete`              | 删除一个玩家的数据                            |
| `/bt reload`                 | `bbstoper.reload`              | 重载插件                                      |

## PlaceholderAPI 占位符 (仅 Java 版)

要想使用这些占位符就必须在服务端上同时运行了[PlaceholderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI)插件.

| 占位符                | 描述                                               |
| --------------------- | -------------------------------------------------- |
| %bbstoper_bbsid%      | 当前玩家的KLPBBS用户名                              |
| %bbstoper_posttimes%  | 当前玩家的顶贴次数                                 |
| %bbstoper_pageid%     | 宣传贴的id                                         |
| %bbstoper_pageurl%    | 宣传贴的链接                                       |
| %bbstoper_lastpost%   | 上一次被顶贴的时间                                 |
| %bbstoper_top_<序号>% | 顶贴排行第"序号"个的顶贴信息, 例: %bbstoper_top_1% |

## 贡献者

感谢所有让这个插件变好的人:

<table>
  <tr>
    <td align="center" width="100">
      <a href="https://github.com/R-Josef"><img src="https://github.com/R-Josef.png?size=56" width="56" alt="R-Josef"><br><sub>R-Josef</sub></a>
    </td>
    <td align="center" width="100">
      <a href="https://github.com/NeglectDream"><img src="https://github.com/NeglectDream.png?size=56" width="56" alt="NeglectDream"><br><sub>NeglectDream</sub></a>
    </td>
    <td align="center" width="100">
      <a href="https://github.com/mincHR549"><img src="https://github.com/mincHR549.png?size=56" width="56" alt="XiaoHaoo"><br><sub>XiaoHaoo</sub></a>
    </td>
  </tr>
</table>

| 贡献者 | 贡献 |
| ----------------------------- | ---- |
| [R-Josef](https://github.com/R-Josef) | 上游 [BBSToper](https://github.com/R-Josef/BBSToper) 作者, 本插件的基础实现 |
| [NeglectDream](https://github.com/NeglectDream) | 反馈论坛改版导致的抓取失效, 提交域名修复 [PR #1](https://github.com/mincHR549/KBBSToper/pull/1) — 该思路已在新架构中作为 `website` 配置项落地(见 [config.yml](core/src/main/resources/config.yml), 五处链接统一走该配置) |
| [XiaoHaoo](https://github.com/mincHR549) | 现维护者: 多模块重构, Nukkit(基岩版)支持, 爬虫缓存与并发问题修复 |

历史提交记录中还有 SnowCherry、SnowyMC、Cherry、Karlatemp、apachezy、DreamVoid、流光溢彩Last、sandtechnology 等, 一并致谢。

[完整贡献者图谱](https://github.com/mincHR549/KBBSToper/graphs/contributors)
