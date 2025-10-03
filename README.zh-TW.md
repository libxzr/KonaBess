# KonaBess

### 支援的裝置

- Snapdragon 6 系列：

  - Snapdragon 690

- Snapdragon 7 系列：

  - Snapdragon 750
  - Snapdragon 765
  - Snapdragon 778G
  - Snapdragon 780G
  - Snapdragon 7 Gen 1
  - Snapdragon 7+ Gen 2
  - Snapdragon 7+ Gen 3

- Snapdragon 8 系列：
  - Snapdragon 855
  - Snapdragon 865
  - Snapdragon 888
  - Snapdragon 8 Gen 1
  - Snapdragon 8+ Gen 1
  - Snapdragon 8 Gen 2
  - Snapdragon 8 Gen 3
  - Snapdragon 8s Gen 3

### 總覽

KonaBess 是一款簡單的應用程式，旨在無需重新編譯核心即可自訂 GPU 頻率與電壓表。

### 運作原理

此應用程式透過解開 Boot/Vendor Boot 映像檔、反編譯與編輯相關的 dtb (device tree binary) 檔案，最後再重新打包並刷入修改過的映像檔，來達成客製化功能。

### 使用說明

有關詳細的使用說明，請參閱「help」部分。

### 為什麼叫「KonaBess」？

- 名稱中的「Kona」是 Snapdragon 865 系列的代號。
- 鑑於 Snapdragon 888 的 GPU 能效有所降低，KonaBess 讓使用者可以超頻 Snapdragon 865 來超越 Snapdragon 888 的效能。這就是建立此應用的動機。
- 儘管此應用程式也相容 Snapdragon 888，但它仍保留了原來的名稱。

### 效能提升

改善的程度因人而異，有些使用者回報，在對 Snapdragon 865 進行降壓後，圖形基準測試中的功耗降低了 25% (4.2w->3.2w)。實際的改善效果取決於晶片體質和穩定性要求。

### 預先建構的二進位檔

- [magiskboot](https://github.com/topjohnwu/Magisk)
- [dtc](https://github.com/xzr467706992/dtc-aosp/tree/standalone)

### 螢幕截圖

<img src="https://raw.githubusercontent.com/xzr467706992/KonaBess/master/screenshots/ss1.jpg" width="180" height="400" />

<img src="https://raw.githubusercontent.com/xzr467706992/KonaBess/master/screenshots/ss2.jpg" width="180" height="400" /> <img src="https://raw.githubusercontent.com/xzr467706992/KonaBess/master/screenshots/ss3.jpg" width="180" height="400" />
