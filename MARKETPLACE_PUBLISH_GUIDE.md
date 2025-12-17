# JetBrains Marketplace 上架指南

恭喜您！您的 **DTO Helper** (by CatInCold) 已經準備好面對全世界的開發者了。
請按照以下步驟將插件發布到 JetBrains Marketplace。

## 1. 準備工作 (Preparation)

### 1-1. 確認版本與資訊
確保 `plugin.xml` 和 `build.gradle.kts` 的資訊正確無誤：
- **Version**: `1.3.2`
- **Vendor**: `CatInCold`
- **ID**: `com.catincold.dtohelper`

### 1-2. 產生安裝包
在終端機執行：
```bash
./gradlew clean buildPlugin
```
生成的檔案位於：`build/distributions/dto-helper-1.3.2.zip`。這是您等一下要上傳的檔案。

### 1-3. 準備素材
您需要準備好我們之前生成的素材：
1.  **Icon圖示**: `dto_helper_cat_icon_*.png` (在 `.gemini/...` 目錄下，或見 `RELEASE_SUMMARY.md`)
2.  **插件描述**: `RELEASE_SUMMARY.md` 中的 HTML 內容。

---

## 2. 註冊與登入 (Account)

1. 前往 [JetBrains Marketplace](https://plugins.jetbrains.com/)。
2. 點擊右上角 **Log in** (如果沒有帳號請註冊)。
3. 注意：如果您希望以 "CatInCold" 這個組織名稱發布，您可能需要先建立一個 Organization，或者先以個人身份發布後再轉移。初步建議先用個人帳號發布即可。

---

## 3. 上傳插件 (Upload)

1. 登入後，點擊右上角的個人頭像，選擇 **Upload Plugin**。
2. **Upload Plugin File**: 選擇剛剛生成的 `dto-helper-1.3.2.zip`。
3. **License**: 建議選擇 `Apache 2.0` 或 `MIT` (開源友善)。
4. 點擊 **Upload Plugin**。

---

## 4. 編輯詳情頁 (Edit Page)

上傳成功後，您會進入編輯頁面。這是使用者第一眼看到的地方，非常重要！

### 4-1. General Information
- **Icon**: 上傳那張可愛的貓咪圖示。
- **Description**: 複製 `RELEASE_SUMMARY.md` 裡面準備好的 HTML 代碼貼上。

### 4-2. Compatibility
系統會自動讀取 ZIP 檔裡的設定，通常不需要手動修改。

### 4-3. Save
填寫完畢後，點擊 **Save**。

---

## 5. 等待審核 (Review)

- 剛上傳的插件狀態會是 **Pending Approval**。
- JetBrains 團隊會進行審核 (通常需要 2 個工作天左右)。
- 審核通過後，您會收到 Email 通知，此時全世界的開發者就可以在 IDE 裡搜尋到 "DTO Helper" 並安裝了！

祝您的插件大受歡迎！🚀
