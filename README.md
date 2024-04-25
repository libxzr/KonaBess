# KonaBess

[中文版](https://github.com/xzr467706992/KonaBess/blob/master/README_zh-CN.md)

### Supported Devices

* Snapdragon 6 series:
  * Snapdragon 690

* Snapdragon 7 series:
  * Snapdragon 750
  * Snapdragon 765
  * Snapdragon 778G
  * Snapdragon 780G
  * Snapdragon 7 Gen 1
  * Snapdragon 7+ Gen 2

* Snapdragon 8 series:
  * Snapdragon 855
  * Snapdragon 865
  * Snapdragon 888
  * Snapdragon 8 Gen 1
  * Snapdragon 8+ Gen 1
  * Snapdragon 8 Gen 2
  * Snapdragon 8 Gen 3


### Overview

KonaBess is a straightforward application designed to customize GPU frequency and voltage tables without the need for kernel recompilation.

### How it Operates

The application achieves customization by unpacking the Boot/Vendor Boot image, decompiling and editing relevant dtb (device tree binary) files, and finally repacking and flashing the modified image.

### Usage Instructions

Refer to the "help" section for detailed instructions on usage.

### Why "KonaBess"?

- The name "Kona" corresponds to the code name of the Snapdragon 865 series.
- Given that the GPU of the Snapdragon 888 exhibits a decrease in energy efficiency, KonaBess allows users to overclock the Snapdragon 865 and surpass the performance of the Snapdragon 888. This is the motivation behind the app's creation.
- Despite the compatibility with Snapdragon 888, the app retains its original name.

### Performance Enhancement

The extent of improvement varies, with some users reporting a 25% reduction in power consumption in the graphics benchmark (4.2w->3.2w) after undervolting the Snapdragon 865. Actual improvement is chip-specific and contingent on stability requirements.

### Prebuilt Binaries

- [magiskboot](https://github.com/topjohnwu/Magisk)
- [dtc](https://github.com/xzr467706992/dtc-aosp/tree/standalone)

### Screenshots

<img src="https://raw.githubusercontent.com/xzr467706992/KonaBess/master/screenshots/ss1.jpg" width="180" height="400" /> <img src="https://raw.githubusercontent.com/xzr467706992/KonaBess/master/screenshots/ss2.jpg" width="180" height="400" /> <img src="https://raw.githubusercontent.com/xzr467706992/KonaBess/master/screenshots/ss3.jpg" width="180" height="400" />
