# 🎯 Quick Start Guide - Expense Manager Dashboard

## 📋 Yêu cầu hệ thống

- ✅ **Android Studio** Hedgehog (2023.1.1) hoặc mới hơn
- ✅ **JDK 11** hoặc cao hơn
- ✅ **Android SDK** API 24+ (Android 7.0)
- ✅ **Kotlin** 2.0.21
- ✅ **Gradle** 8.13.2

## 🚀 Các bước chạy ứng dụng

### Bước 1️⃣: Sync Project
```bash
# Trong terminal Android Studio
./gradlew clean build

# Hoặc Windows PowerShell
.\gradlew.bat clean build
```

### Bước 2️⃣: Chạy trên Emulator

#### Tạo AVD (nếu chưa có):
1. Tools → Device Manager
2. Create Device
3. Chọn: **Pixel 6 Pro** (hoặc thiết bị khác)
4. System Image: **API 34** (UpsideDownCake)
5. Finish

#### Run ứng dụng:
```bash
# Click nút Run trong Android Studio
Shift + F10

# Hoặc dùng Gradle
./gradlew installDebug
```

### Bước 3️⃣: Test Dark Theme
- Trong emulator: Settings → Display → Dark theme
- Hoặc toggle quick settings

## 📱 Kết quả mong đợi

### ✨ Màn hình sẽ hiển thị:
1. **Top Bar**: "Trang chủ" + Month picker (Tháng 1 2026)
2. **3 Cards**:
   - Số dư tháng: +5.200.000₫ (xanh)
   - Tổng thu: 12.500.000₫ (xanh dương)
   - Tổng chi: 7.300.000₫ (đỏ)
3. **Biểu đồ**: Line chart với 2 đường (thu/chi)
4. **Filter chips**: Hôm nay, Tuần này, Tháng này
5. **FAB**: Button "+" màu tím (bottom-right)

## 🐛 Troubleshooting

### ❌ Lỗi: "Unresolved reference: compose"
**Giải pháp:**
```bash
# Sync lại Gradle
File → Sync Project with Gradle Files
```

### ❌ Lỗi: "Cannot resolve symbol DashboardScreen"
**Giải pháp:**
```bash
# Rebuild project
Build → Rebuild Project
```

### ❌ Lỗi: Java version mismatch
**Giải pháp:**
```kotlin
// Kiểm tra app/build.gradle.kts
compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
kotlinOptions {
    jvmTarget = "11"
}
```

### ❌ Emulator quá chậm
**Giải pháp:**
1. Enable **Hardware Acceleration**
2. Tăng RAM cho AVD (4GB recommended)
3. Hoặc dùng thiết bị thật (USB Debugging)

## 🎨 Tính năng Interactive

### ✅ Đã có thể test:
- [x] Chọn tháng khác từ dropdown
- [x] Xem animation số tiền tăng dần
- [x] Scroll màn hình lên/xuống
- [x] Toggle dark/light theme
- [x] Click FAB button (hiện chưa navigate)

### 🔜 Chưa implement:
- [ ] Click vào card để xem chi tiết
- [ ] Filter chips (logic chưa có)
- [ ] Navigation sang màn hình khác
- [ ] Dữ liệu thật từ database

## 📊 Mock Data

Hiện tại dùng **random data** trong ViewModel:
- **Thu nhập**: 0 - 2.000.000₫/ngày
- **Chi tiêu**: 0 - 1.500.000₫/ngày
- **30 ngày** data points

**Để thay bằng data thật:**
1. Tạo Room Database
2. Implement Repository
3. Inject vào ViewModel
4. Replace `generateMockChartData()` 

## 🎯 Next Features to Implement

### Ưu tiên cao:
1. **Add Transaction Screen**
   - Form nhập thu/chi
   - Category picker
   - Date picker
   - Save to database

2. **Room Database**
   - Transaction entity
   - Category entity
   - DAOs
   - Database migration

3. **Navigation**
   - NavHost setup
   - Bottom navigation bar
   - Screen routes

### Ưu tiên trung bình:
4. **History Screen**
   - List transactions
   - Filter by date/category
   - Delete/Edit

5. **Category Management**
   - CRUD operations
   - Color picker
   - Icon picker

6. **Reports Screen**
   - Pie chart by category
   - Monthly comparison
   - Export PDF

### Nice to have:
7. **Settings Screen**
   - Currency selection
   - Language
   - Notifications
   - Backup/Restore

8. **Authentication**
   - Firebase Auth
   - Google Sign-in
   - Biometric

## 📦 Project Structure

```
app/src/main/java/com/example/expensemanager/
│
├── feature/              # Feature modules
│   └── dashboard/
│       ├── DashboardScreen.kt      ✅ Done
│       ├── DashboardViewModel.kt   ✅ Done
│       └── DashboardUiState.kt     ✅ Done
│
├── widget/               # Reusable UI components
│   ├── MonthPicker.kt              ✅ Done
│   └── SimpleLineChart.kt          ✅ Done
│
├── ui/theme/             # Theme & styling
│   ├── Theme.kt                    ✅ Done
│   └── Type.kt                     ✅ Done
│
├── utils/                # Utilities
│   └── FormatUtils.kt              ✅ Done
│
└── MainActivity.kt                  ✅ Done
```

## 📝 Code Quality

### ✅ Best Practices:
- [x] MVVM architecture
- [x] StateFlow for state management
- [x] Compose best practices
- [x] Material3 guidelines
- [x] Kotlin conventions
- [x] Responsive design

### 📚 Documentation:
- [x] KDoc comments
- [x] README files
- [x] Code organization

## 🎉 Chúc mừng!

Bạn đã có một **Dashboard screen hoàn chỉnh** với:
- ✨ Modern UI
- 🎨 Beautiful animations
- 🌙 Dark theme support
- 📱 Responsive layout
- 🏗️ Clean architecture

**Tiếp theo:** Implement Add Transaction screen để bắt đầu nhập dữ liệu thật! 🚀
