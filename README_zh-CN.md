# KonaBess

### 这是什么？

- 一个无需重新编译内核就能实现GPU频率表&电压表调整的简单软件，目前适用于骁龙865系列、855系列、888以及765系列平台

### 它是如何工作的？

- 通过拆解Boot / Vendor Boot镜像，反编译并修改dtb（device tree binary）中的相关部分，在重新打包后刷入设备来实现

### 我该如何使用它？

- 详见软件内“帮助”

### 为什么叫这个名字？

- Kona是骁龙865系列的代号
- 这个软件最初是为了背刺牙膏倒吸的888而诞生的
- 后来虽处于人道主义原则支持了888，但是名称并不会因此改变

### 使用它能达到多大的提升？

- 我看到的一个骁龙865的降压案例是在性能保持不变的前提下，GPU去空载功耗从4.2W下降到了3.2W，降幅大概是25%
- 当然，能提升多少取决于你的芯片体质和你对稳定性的要求

### 预编译的二进制文件

- [magiskboot](https://github.com/topjohnwu/Magisk)
- [dtc](https://github.com/xzr467706992/dtc-aosp/tree/standalone)

### 截图
<img src="https://raw.githubusercontent.com/xzr467706992/KonaBess/master/screenshots/ss1.jpg" width="180" height="400" /> <img src="https://raw.githubusercontent.com/xzr467706992/KonaBess/master/screenshots/ss2.jpg" width="180" height="400" /> <img src="https://raw.githubusercontent.com/xzr467706992/KonaBess/master/screenshots/ss3.jpg" width="180" height="400" /> 
