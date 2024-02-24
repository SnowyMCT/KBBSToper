# KBBSToper

这是一个检测mcbbs服务器宣传贴顶帖后，玩家输入指令领取奖励的bukkit插件.

本插件与 BBSToper的权限、指令、变量均通用，但数据库不通用！！！

## 用到的库

1. [Jsoup](https://jsoup.org/)
2. [bStats](https://bstats.org/)
3. [PlaceHolderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI)

## 使用方法

1. 获得一份构建好的jar文件, 请查看[https://github.com/SnowCherryServer/KBBSToper/releases/tag/114514](https://github.com/SnowCherryServer/KBBSToper/releases/tag/114514)
2. 将构建好的文件放入plugins文件夹
3. 前往klpbbs复制您的帖子id并替换掉配置文件中默认链接中的id
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
| `/bt`                        | 无需权限                       | 显示箱子GUI                                   |
| `/bt help`                   | 无需权限                       | 显示帮助信息                                  |
| `/bt binding <MCBBS论坛ID>`  | `bbstoper.binding`             | 绑定论坛账号, 注意这里是ID不是uid             |
| `/bt reward`                 | `bbstoper.reward`              | 领取奖励                                      |
| `/bt testreward [模式]`      | `bbstoper.testreward`          | 测试奖励, 模式: `normal` `incentive` `offday` |
| `/bt list <页数>`            | `bbstoper.list`                | 列出所有顶帖者                                |
| `/bt top <页数>`             | `bbstoper.top`                 | 按照顶贴次数列排名出所有已绑定玩家            |
| 无                                 | `bbstoper.bypassquerycooldown` | 绕过查询冷却                                  |
| `/bt check bbsid <论坛ID>`   | `bbstoper.check`               | 查看一个论坛id的绑定者                        |
| `/bt check player <玩家ID>`  | `bbstoper.check`               | 查看一个玩家绑定的论坛id                      |
| `/bt delete player <玩家ID>` | `bbstoper.delete`              | 删除一个玩家的数据                            |
| `/bt reload`                 | `bbstoper.reload`              | 重载插件                                      |

## PlaceholderAPI 占位符

本插件提供了一些基于PlaceHolderAPI的占位符(Placeholders), 要想使用这些占位符就必须在服务端上同时运行了[PlaceholderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI)插件.

| 占位符                | 描述                                               |
| --------------------- | -------------------------------------------------- |
| %bbstoper_bbsid%      | 当前玩家的MCBBS用户名                              |
| %bbstoper_posttimes%  | 当前玩家的顶贴次数                                 |
| %bbstoper_pageid%     | 宣传贴的id                                         |
| %bbstoper_pageurl%    | 宣传贴的链接                                       |
| %bbstoper_lastpost%   | 上一次被顶贴的时间                                 |
| %bbstoper_top_<序号>% | 顶贴排行第"序号"个的顶贴信息, 例: %bbstoper_top_1% |


