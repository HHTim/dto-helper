# DTO Helper 插件開發與部署指南 v1.3.2

這份指南是專門為您準備的，包含如何理解程式碼、如何修改、以及如何將插件安裝到您的 IntelliJ IDEA 中。

## 1. 快速開始 (Quick Start)

### 1-1. 編譯插件 (Build)
在您的專案根目錄 (`dto-helper`) 打開終端機 (Terminal)，執行以下指令：

```bash
# 如果是 Mac / Linux
./gradlew clean buildPlugin

# 如果是 Windows
gradlew.bat clean buildPlugin
```

> **常見錯誤**: 如果您看到 "Unable to locate a Java Runtime"，請確認您的終端機環境變數 `JAVA_HOME` 已正確指向 JDK 17+。

成功後，您會在 `dto-helper/build/distributions/` 資料夾下看到 `dto-helper-1.3.2.zip`。這個 ZIP 就是您的插件安裝檔。

### 1-2. 安裝插件 (Install)
1. 打開您的 IntelliJ IDEA。
2. 進入 **Settings** (Mac: Cmd+, / Windows: Ctrl+Alt+S) -> **Plugins**。
3. 點擊上方的齒輪圖標 ⚙️，選擇 **Install Plugin from Disk...**。
4. 瀏覽到剛剛生成的 `dto-helper-1.3.2.zip` 並選擇它。
5. 點擊 **Restart IDE** 重啟。

---

## 2. 功能使用說明 (Code Completion)

本插件已全面升級為 **代碼補全 (Code Completion)** 模式，讓您雙手不離鍵盤即可完成操作。

### 功能 A: 智慧 Builder 樣板 (.builderChain)
**情境**: 您要建立一個物件 `UserDto`。
**操作**:
1. 輸入類別名稱並加上 `.`，例如 `UserDto.`。
2. 在彈出的選單中選擇 **builderChain**。
**結果**:
- 自動生成 `UserDto userDto = UserDto.builder()...` 結構。
- **互動式命名**: 游標首先停在 `userDto` 上，您可以直接打字改名。
- **一鍵完成**: 改完名字後，按下 **Enter** 或 **Tab**，游標直接跳出，完成操作！
- **智慧預設值**: Boolean 填 `false`、Int 填 `0`、String 填 `""`。

### 功能 B: 一鍵展開 Getters (.allGetters)
**情境**: 您已經有一個變數 `userDto`，想取出所有值。
**操作**:
1. 輸入變數名稱並加上 `.`，例如 `userDto.`。
2. 選擇 **allGetters**。
**結果**:
```java
String name = userDto.getName();
int age = userDto.getAge();
// 自動生成所有變數與賦值，且排版整齊
```

---

## 3. 開發者指南 (程式碼導讀)

所有的程式碼都在 `src/main/kotlin/com/catincold/dtohelper/` 底下。

### 核心架構: CompletionContributor
我們使用 IntelliJ 的 `CompletionContributor` API 來注入代碼建議，而非傳統的 `AnAction`。

### 檔案說明
1. **plugin.xml**
   - 設定檔，將 `DTOHelperCompletionContributor` 註冊到 Java 語言的自動補全擴充點。
   - 包含新的 HTML 描述與 Vendor (CatInCold) 資訊。

2. **DTOHelperCompletionContributor.kt** (核心邏輯)
   - **`extend(...)`**: 註冊了兩個 Provider (`allGetters`, `builderChain`)。
   - **`allGetters` 邏輯**:
     - 檢查游標前的變數型別 (`PsiType`)。
     - 掃描該 Class 的 Getter 方法。
     - 生成 `Type var = ref.getXXX();` 格式的字串。
   - **`builderChain` 邏輯**:
     - 使用 **Live Template (`TemplateManager`)** 技術。
     - 建立 Template 讓使用者可以互動改名 (`VAR_NAME`)。
     - 為不同欄位型別 (`PsiType`) 注入智慧預設值。

3. **GenerateAction.kt** (已棄用)
   - 舊版的 `GenerateGettersAction` 與 `GenerateBuilderAction` 仍保留在專案中作為參考，但在 `plugin.xml` 中已非主要入口。

## 4. 常見問題

**Q: 為什麼改了 code 沒反應？**
A: 記得修改後要重新 `./gradlew clean buildPlugin` 並重新安裝。

**Q: 如何改版本號？**
A: 修改 `build.gradle.kts` 裡的 `version = "1.3.2"`。

**Q: 編譯失敗 "Unsupported Class Version"？**
A: 請確保 Gradle 和 IntelliJ 專案都使用 JDK 17。
