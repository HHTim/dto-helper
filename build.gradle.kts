

// 這是 Gradle 的 plugins 區塊，用來載入需要的插件
plugins {
    // Kotlin 支援，因為我們是用 Kotlin 來寫插件的
    id("org.jetbrains.kotlin.jvm") version "1.9.21"
    
    // 這是開發 IntelliJ 插件最核心的 Gradle Plugin
    // 它幫我們處理了下載 IntelliJ SDK、執行 IDE、打包插件等繁瑣工作
    id("org.jetbrains.intellij") version "1.17.0"
}

// 設定 Group ID，通常是公司的網域倒過來寫
group = "com.catincold"
// 設定版本號
version = "1.3.4"

// 設定依賴庫的倉庫來源
repositories {
    // 使用 Maven Central，最標準的 Java/Kotlin 函式庫來源
    mavenCentral()
}

// 設定 Kotlin 編譯選項
kotlin {
    jvmToolchain(17) // IntelliJ 插件現在通常建議使用 Java 17
}

// IntelliJ 插件的詳細設定
intellij {
    // 設定我們要開發針對哪個版本的 IntelliJ IDEA
    // "2023.2.5" 是指 IntelliJ IDEA 2023.2.5 版本
    version.set("2023.2.5")
    
    // 設定插件類型，"IC" 代表 IntelliJ Community (免費版)
    // 這樣我們可以確保插件在免費版也能運作
    type.set("IC") 
    
    // 這裡可以列出這個插件依賴的其他 IntelliJ 內建插件
    // "java" 代表我們需要 Java 語言支援的功能 (PSI 等)
    plugins.set(listOf("com.intellij.java"))
}

// 設定編譯任務
tasks {
    // 設定 PatchPluginXml 任務，這是用來更新 plugin.xml 內的版本號等資訊
    patchPluginXml {
        sinceBuild.set("232") // 支援的最低 IntelliJ build版號 (對應 2023.2)
        untilBuild.set("253.*") // 支援的最高 IntelliJ build版號 (對應 2025.3)
    }

    // 當我們執行 ./gradlew runIde 時的設定
    runIde {
        // 為了避免記憶體不足，可以把這個功能開啟
        autoReloadPlugins.set(true)
    }

    buildSearchableOptions {
        enabled = false
    }
}
