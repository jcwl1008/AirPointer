# AirPointer 1.2

為 Android 製作的 iPhone 風格簡報遙控器。

## 安裝更新

將 AirPointer-v1.2.apk 下載到手機，開啟並安裝更新。與 BluePointer 1.1 使用相同套件及簽署，毋須先解除安裝；原有配對電腦及靈敏度設定會保留。畫面及手機 App 名稱會變成 AirPointer。

## 操作

1. 點「連接」選擇已配對電腦，等待顯示已連接。
2. 在 Windows 開啟 PowerPoint，點一下投影片取得焦點。
3. 點「開始簡報」，從目前投影片開始。
4. 底部大型按鈕：上一頁／下一頁；固定位置，不隨內容捲動。
5. 按住「移動游標」，左右轉動或上下傾斜手機；放開即停。
6. 觸控板：滑動移動游標；輕點一下點擊。
7. 雷射游標：切換 PowerPoint 雷射／箭頭。聚光燈：先執行 desktop/Start-Spotlight.cmd，再於手機切換。
8. 頂部「設定」可調整靈敏度、反轉上下或停止連線；點連線狀態可查看診斷。

## 1.2 介面更新

- 淺灰白底、白色圓角卡片、藍色重點按鈕與系統字型。
- 94dp 高的固定翻頁按鈕；112dp 高的指標控制區與觸控板。
- 主要操作按鈕至少 52dp；設定及說明至少 48dp。
- 按住游標控制時變成藍色；觸控板觸碰時有視覺回饋。
- 系統列留白，避免導覽手勢遮住按鈕；小畫面可捲動。
- 保留 1.1 的 Bluetooth HID 前景服務、有限重連、Windows 鍵盤報告與快捷鍵。

Android 9 或以上；手機須支援 Bluetooth HID。這是 Android App，以 iPhone 介面風格設計；個人製作，並非 Apple、Samsung 或 Logitech 官方產品。

## 建置原始碼

需要 Android SDK 35、Build Tools 35.0.0 與 Java 17 JDK。設定 ANDROID_SDK_ROOT 及 AIRPOINTER_KEYSTORE、AIRPOINTER_KEY_ALIAS、AIRPOINTER_KEYSTORE_PASSWORD 後執行 bash build.sh。

本儲存庫不含簽署私鑰或 Samsung 字型。介面使用 Android 系統字型。下載區 APK 為已簽署的個人版本；自行編譯需使用自己的金鑰，與現有版本的簽署不同時不能直接更新安裝。
