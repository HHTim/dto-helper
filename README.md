# DTO Helper 🐱

![Version](https://img.shields.io/badge/version-1.3.2-blue.svg)
![Vendor](https://img.shields.io/badge/vendor-CatInCold-purple.svg)

**DTO Helper** is an IntelliJ IDEA plugin designed to accelerate your Java development workflow. It provides silky smooth code completion for DTOs, eliminating repetitive typing.

Built by **CatInCold**.

<p align="center">
  <img src="src/main/resources/META-INF/pluginIcon.png" alt="DTO Helper Icon" width="128" height="128">
</p>

## ✨ Features

### 1. Smart Builder Chain (`.builderChain`)
Type `UserDto.` and select `builderChain` to instantly generate a full builder pattern structure.

- **⚡ Interactive Renaming**: The cursor lands on the variable name. Type to rename, then press **Enter** to finish.
- **⚡ Smart Defaults**: Automatically fills `false` for booleans, `0` for integers, and `""` for Strings. No more manual `null` cleanup!
- **⚡ Context Aware**: Intelligently detects if you need a variable assignment (`UserDto user = ...`) or just the chain.

### 2. Generate All Getters (`.allGetters`)
Type `userDto.` and select `allGetters` to expand all properties.

- **⚡ Instant Assignments**: Generates `String name = userDto.getName();` for every field.
- **⚡ Smart Filtering**: Automatically excludes system methods like `getClass()`.
- **⚡ Perfect Indentation**: Respects your code style.

## 🚀 Installation

### Option 1: From Marketplace (Recommended)
1. Open **Settings** -> **Plugins** in IntelliJ IDEA.
2. Search for **"DTO Helper"**.
3. Click **Install**.

### Option 2: Build from Source
```bash
git clone https://github.com/your-repo/dto-helper.git
cd dto-helper
./gradlew clean buildPlugin
```
The plugin file will be generated at `build/distributions/dto-helper-1.3.2.zip`. Install it via "Install Plugin from Disk...".

## 🛠 Usage

### Builder Pattern
```java
// Type this:
UserDto.

// Select 'builderChain', and it becomes:
UserDto userDto = UserDto.builder()
    .id(0L)
    .username("")
    .isAdmin(false)
    .build();
```

### Getters
```java
// Type this:
userDto.

// Select 'allGetters', and it becomes:
Long id = userDto.getId();
String username = userDto.getUsername();
boolean isAdmin = userDto.isAdmin();
```

## 📝 License
This project is licensed under the Apache 2.0 License.
