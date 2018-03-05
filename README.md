# 捉小猪
## 捉小猪是一款基于SurfaceView的Android休闲小游戏.
## 游戏主页：https://wuyr.github.io/
## 详细见: http://blog.csdn.net/u011387817/article/details/79439383


## 预览图:
![preview](https://github.com/wuyr/CatchPiggy/raw/master/preview1.gif) ![preview](https://github.com/wuyr/CatchPiggy/raw/master/preview2.gif)


## 思路:
### 我们可以把每一个树桩, 小猪, 车厢都看成是一个Drawable, 这个Drawable里面保存了x, y坐标, 我们的SurfaceView在draw的时候, 就把这些Drawable draw出来.
### 我们的SurfaceView里面有一个Rect二维数组, 用来存放这些矩形, 小猪离开手指之后, 就开始从小猪当前所在的矩形,用广度优先遍历, 找到一条最短的路径(比如: [5,5 5,4 5,3 5,2 5,1 5,0]这样的), 然后再根据这条路径在Rect数组中找到对应的矩形, 最后根据这些对应的矩形的坐标来确定出Path. 哈哈, 有了Path小猪就可以跑了.
![preview](https://github.com/wuyr/CatchPiggy/raw/master/preview3.gif)
![preview](https://github.com/wuyr/CatchPiggy/raw/master/preview4.gif)

### 待改进的地方: 亡猪补牢模式:弹框需改成SurfaceView直接draw.(现在是dialog)
