# PVPRank

## 一个允许服务器进行组队和solo PVP的插件

![](https://img.shields.io/badge/Spigot%2FPaper-1.13%2B-orange)
![](https://img.shields.io/github/license/MinecraftProgrammingTeam/PVPRank)
![](https://img.shields.io/badge/made%20in-MPT-important)

# 注意

- 本插件强依赖于[Multiverse-Core](https://modrinth.com/plugin/multiverse-core)插件，请确保您的服务器在安装本插件之前先安装了mv插件！
- 本插件部分代码由AI编写，目前未经完全测试，可能存在bug
- 本插件测试环境：`Spigot1.20.1 + Minecraft1.20.1(Forge + Optifine)`，本插件理论上适配所有1.13+的BukkitAPI服务端。
- 本插件`config.yml`里的配置注释写得十分详细，`kits/kits.yml`里面写入的物品会挨个添加到玩家物品栏内，目前不支持自定义添加位置，以BukkitAPI默认的添加为准。写入物品需要使用命名空间(即Minecraft原生/give指令需要使用的命名空间)，支持mod物品。

# 介绍

本插件自带组队系统、solo系统、积分系统、组内权限系统。团队和个人之间都可以互相PVP。本插件自带UI和指令两套系统，输入`/pvp`即可打开UI系统，输入`/pvprank`即可查看指令系统帮助。

本插件使用的外部库有：

- sqlite-jdbc进行数据存储
- HikariCP进行高性能连接
- anvilgui替代原生铁砧GUI

注意：指令系统在现阶段的Tab提示并不完善，甚至可以说完全没有，强烈建议普通用户使用UI系统！

# 如何使用

先下载[Multiverse-Core](https://modrinth.com/plugin/multiverse-core)插件，之后下载最新的[Release]([Releases · MinecraftProgrammingTeam/PVPRank](https://github.com/MinecraftProgrammingTeam/PVPRank/releases/latest))，将二者全部丢入包含BukkitAPI的服务端插件包里面即可。

# TODOs:

- [x] UI系统
- [ ] 积分排行榜
- [ ] 对手bossbar
- [ ] 场地刷新系统
- [ ] 护甲自动穿戴系统





