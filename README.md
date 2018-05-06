# bridge
基于Android的Surfaceview组件实现的桥牌游戏，有机-机、人-机、人-人（WiFi）等三种游戏模式。

## 1 功能介绍

### 1.1 游戏流程描述（预期达到的流程）
- []开始后在界面上显示准备按钮
- []所有玩家就绪后，游戏开始
- []发牌动画，显示提示信息
- [*]显示叫牌界面
- []叫牌结束后，提示信息位置显示提示信息
- [*]首家攻牌后，显示明手
- []打牌结束后，显示结算画面
- []打牌结束后，重新显示准备按钮，进入下一局

### 1.2 其他游戏设定
- 竖屏显示，在上方设置区域显示额外内容（叫牌提示，赢墩等信息）
- 用户不显示头像
- 叫牌结束后用户不更换座位（只针对人-机游戏模式）

## 2 代码介绍
### 2.1 各部分代码的功能
- engine:这部分是对游戏底层实现的一个抽象，将SurfaceView和SurfaceView的更新线程做了封装
  - game:这是基于SurfaceView更新机制制作的一个简单demo，可以用自己的游戏类继承并重写这个类的函数来制作自己的游戏
  - view/thread:分别对应SurfaceView和SurfaceView的更新线程SurfaceThread
  - util:包含一个用户计算触摸范围的辅助类
- game:这部分是游戏业务的实现代码，继承自engine/game类
  - main:包含Game(游戏主类），Call（负责叫牌的类），Table（负责出牌的类）等公共类
  - player:主要包含参与游戏的三类不同的游戏角色Player,Robot,RemotePlayer和它们的父类AbstractPlayer
  - res:用于加载绘图资源的辅助类

## 3 开发纪要
### 游戏引擎要解决的问题有
- 如何隐藏底层实现，简化业务逻辑
- 如何分离触摸事件和绘图事件
- 只提供了GameView，是否要提供View接口

### 游戏业务要解决的问题
- 解决对不同屏幕的适配（18：27）（已解决）
- 解决玩家位置分配问题（未解决）
  - 本地主机绘制需要Left,Right,Top,Bottom
  - 不同主机之间协调需要Position和东西南北
  - 应该把position和stage建立联系——position不应该和stage建立联系
  - position应该和绘制逻辑无关，应该根据另外的一个变量来决定绘制逻辑
- 解决远程玩家链接和不同类型玩家的抽象（未解决）
- 解决Robot叫牌问题
- 解决Robot打牌问题

### TODO
- []绘制加倍和再加倍按钮
- []随机分配座位
- []绘制准备界面
- [*]首攻是第一个出牌的人
- [*]首攻打出之前，明手不能展示手牌
- []给机器人加延时
- []人机模式下，玩家要能替明手出牌
  - []人机模式下，当明手是玩家时，显示庄家的牌面
- []从机制上保证刷新率
- []解决显示四张牌的Bug
- []两侧玩家侧面显示的时候不能暴露花色分布
- []机机模式下增加触摸
- []优化性能表现
  - []启动时加载的资源过多
  - []正在绘制的时候返回，会触发一个NullPointerException的错误（initcanvas）
  - []有时候会延迟
- []解决联机协议问题
  - [] 玩家要发出准备信号
- []解决叫牌提示问题
  - []实现几个统计函数，写一个树状函数，对同级的几个进行有效值评估
  - []按照新瑞的做法，识别用户当前应该具有的行为，按照行为进行推荐
- []记录叫牌结果
- []对叫牌结果进行评分

- []修改Call显示和触摸逻辑（什么意思）
- []增加一个提示调整座位的提示(不做了）
- []解决出第四张牌时不显示的问题
- []解决人机模式下出牌事件不一致的问题（什么意思）
- []解决人机模式下，触摸事件依然有效的问题（解决了)
- []解决叫三张PASS后不能正常绘制的BUG
- []使用HandlerThread代替普通的Thread
- []叫牌策略：1. 象棋树形图 2. 分布空间

- [*]Call需要给出庄家和定约，方便之后出牌
- []解决gc问题
- [*]修改牌面为对称形状
- [*]将Bitmap和Paint等做成参数，减少GC——对减少GC收效甚微
- [*]加载图片浪费了太多时间，考虑改变图片的分辨率——不是分辨率的问题，更多的时间浪费在加载图片和GC
- [*]解决返回MainActivity后，线程没有退出的问题——停顿一段时间后会自动退出
- [*]解决process线程内容过多导致draw更新慢的问题——不是process内容过多的问题
- [*]1. 修改成被动更新——没必要，新睿桥牌就采用了主动更新的方式，且没有影响效率
- [*]出牌时CPU占有率过高——有一处资源加载方式没有改进
- [*]关闭时，将资源释放——解决，内存压下去了
