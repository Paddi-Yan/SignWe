## SignWe
广东海洋大学图灵创新创业团队签到小程序<br/>
原提供服务接口是用微信小程序云函数编写，该项目是对后端的重构，使用了Java语言编写。<br/>
微信搜索SignWe即可体验<br/>
涉及的技术栈有SpringBoot+MyBatisPlus+MySQL+Redis。<br/>
### 功能列表
#### 用户
- 查看座位情况、开门状态以及可以订阅开门通知
- 查看公告
- 签到签退并记录学习时长，需要在规定范围内才能进行签到，也提供了无视范围签到的模式
- 查看总学习排行榜以及昨日学习排行榜
- 查看学习记录
#### 管理员
- 定期重置学习排行榜以及学习记录
- 修改公告
- 强制释放恶意签到挂时长的座位并且不计入学习记录
- 用户检索和权限管理
