# Architecture Change – Fitur Scan Struk & Autocomplete

Dokumen ini mendefinisikan perubahan arsitektur yang diperlukan untuk mengganti mekanisme Text Mapping + Pattern Dinamis menjadi mekanisme OCR Otomatis + Autocomplete. Gunakan bersama `prd-change-scan.md`.

---

## 1. Ringkasan Perubahan Arsitektur

```
SEBELUM:  Scan → Simpan Foto → User tap "Ekstrak Teks" → OcrReviewScreen → Mapping → Pattern → Form terisi
SESUDAH:  Scan → Simpan Foto → OCR otomatis → Autocomplete di AddEditScreen
```

| Lapisan | Status | Keterangan |
|---|---|---|
| `data/parser/` | **Dihapus** | 7 file parser + template + factory |
| `data/database/dao/ReceiptPatternDao` | **Dihapus** | Pattern DAO |
| `data/database/entities/ReceiptPatternEntity` | **Dihapus** | Pattern entity |
| `data/database/Migrations` | **Dihapus** | MIGRATION_1_2 |
| `data/database/AppDatabase` | **Diubah** | Kembali ke version 1 |
| `data/ocr/` | **Tidak berubah** | Tetap digunakan |
| `ui/ocr/` | **Dihapus** | 3 file (Screen, ViewModel, UiState) |
| `ui/patterns/` | **Dihapus** | 6 file |
| `ui/addedit/` | **Diubah** | Tambah autocomplete, hapus flow OCR/mapping |
| `ui/navigation/` | **Diubah** | Hapus 3 route, shared state |
| `ui/dashboard/` | **Diubah** | Hapus ikon ⚙ pattern management |
| `di/DatabaseModule` | **Diubah** | Hapus pattern DAO & migration |

---

## 2. Alur Data Baru

```
┌─────────────────────────────────────────────────────┐
│ Presentation Layer                                  │
│                                                     │
│  AddEditScreen                                      │
│  ├── PhotoSection (Scan/Kamera/Galeri)              │
│  ├── RestaurantInfoSection                          │
│  │   ├── Nama Tempat: OutlinedTextField + Dropdown  │
│  │   └── Alamat:      OutlinedTextField + Dropdown  │
│  ├── MenuItemsSection                               │
│  │   └── MenuItemCard                               │
│  │       ├── Nama Menu:    OutlinedTextField + DD   │
│  │       ├── Jumlah:       OutlinedTextField + DD   │
│  │       └── Harga Satuan: OutlinedTextField + DD   │
│  ├── GrandTotalSection                              │
│  │   └── Override Total: OutlinedTextField + DD     │
│  └── Tombol Simpan                                  │
│                                                     │
│  AddEditViewModel                                   │
│  ├── onScannedPhoto()    → compress + save + OCR    │
│  ├── onPhotoSelected()   → OCR (auto)               │
│  ├── ocrLines: List<String>                         │
│  └── getSuggestions(field, query): List<String>     │
│                                                     │
├─────────────────────────────────────────────────────┤
│ Data Layer                                          │
│                                                     │
│  ReceiptOcrEngine  ──→  ML Kit Text Recognition     │
│  StorageManager    ──→  Local Disk                  │
│  ImageCompressor   ──→  JPEG <500KB                 │
│  CulinaryRepository ──→ Room DB (v1)                │
│                                                     │
│  RoomDB: visits, menu_items  (2 tabel)              │
└─────────────────────────────────────────────────────┘
```

---

## 3. Struktur Paket Setelah Perubahan

```text
com.pndnwngi.billumaba/
│
├── data/
│   ├── database/
│   │   ├── AppDatabase.kt              # version 1 (tidak ada migration)
│   │   ├── dao/
│   │   │   ├── VisitDao.kt
│   │   │   └── MenuDao.kt
│   │   └── entities/
│   │       ├── VisitEntity.kt
│   │       └── MenuItemEntity.kt
│   │
│   ├── ocr/                            # tetap
│   │   ├── OcrModels.kt                # OcrResult, OcrLine
│   │   └── ReceiptOcrEngine.kt         # ML Kit wrapper
│   │
│   ├── repository/
│   │   ├── CulinaryRepository.kt
│   │   └── CulinaryRepositoryImpl.kt
│   │
│   └── storage/
│       ├── StorageManager.kt
│       └── ImageCompressor.kt
│
├── di/
│   ├── DatabaseModule.kt               # tanpa migration, tanpa patternDao
│   └── RepositoryModule.kt
│
├── ui/
│   ├── navigation/
│   │   ├── Screen.kt                   # hanya: dashboard, add_edit, detail
│   │   └── AppNavigation.kt            # 3 composable destinations
│   │
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   │
│   ├── components/                     # Komponen UI global
│   │   ├── StarRating.kt
│   │   ├── PhotoPicker.kt              # bottom sheet 3 opsi
│   │   └── ReceiptScanner.kt
│   │
│   ├── dashboard/
│   │   ├── DashboardScreen.kt          # tanpa ikon ⚙ pattern
│   │   ├── DashboardViewModel.kt
│   │   └── DashboardUiState.kt
│   │
│   ├── addedit/
│   │   ├── AddEditScreen.kt            # autocomplete di setiap field
│   │   ├── AddEditViewModel.kt         # auto-OCR + suggestion logic
│   │   ├── AddEditUiState.kt           # ocrLines: List<String>
│   │   └── TokenMatcher.kt             # NEW: fungsi prefix matching
│   │
│   ├── detail/
│   │   ├── DetailScreen.kt
│   │   ├── DetailViewModel.kt
│   │   └── DetailUiState.kt
│   │
│   └── util/
│       └── GooglePlayServicesUtil.kt
│
└── MainActivity.kt
```

### File yang Dihapus (16 file)

| Path | Keterangan |
|---|---|
| `data/parser/ParsedReceipt.kt` | Data class + ParserType enum + interface |
| `data/parser/GeneralReceiptParser.kt` | Parser umum |
| `data/parser/RestaurantReceiptParser.kt` | Parser resto |
| `data/parser/RetailThermalParser.kt` | Parser retail |
| `data/parser/ReceiptParserFactory.kt` | Auto-detect + pattern lookup |
| `data/parser/PatternReceiptParser.kt` | Parser berbasis pattern |
| `data/parser/TemplateToRegex.kt` | Konverter template visual ke regex |
| `data/database/dao/ReceiptPatternDao.kt` | DAO pattern |
| `data/database/entities/ReceiptPatternEntity.kt` | Entity pattern |
| `data/database/Migrations.kt` | MIGRATION_1_2 |
| `ui/ocr/OcrReviewScreen.kt` | Halaman review OCR |
| `ui/ocr/OcrReviewViewModel.kt` | ViewModel review OCR |
| `ui/ocr/OcrReviewUiState.kt` | UI state review OCR |
| `ui/patterns/PatternListScreen.kt` | Daftar pattern |
| `ui/patterns/PatternListViewModel.kt` | ViewModel daftar pattern |
| `ui/patterns/PatternListUiState.kt` | UI state daftar pattern |
| `ui/patterns/PatternEditScreen.kt` | Builder pattern |
| `ui/patterns/PatternEditViewModel.kt` | ViewModel builder pattern |
| `ui/patterns/PatternEditUiState.kt` | UI state builder pattern |

---

## 4. Detail Perubahan per File

### 4.1. `AddEditUiState.kt`

```diff
- val isRunningOcr: Boolean = false,
- val ocrResult: OcrResult? = null
+ val isRunningOcr: Boolean = false,
+ val ocrLines: List<String> = emptyList()
```

- `ocrResult: OcrResult?` diganti menjadi `ocrLines: List<String>` — hasil OCR disimpan sebagai flat list baris teks.
- Tidak ada field pattern atau parsed receipt.

### 4.2. `AddEditViewModel.kt`

**Dependency yang dihapus dari constructor:**
- `ReceiptPatternDao` — tidak diperlukan
- `ParserType` / `ParsedReceipt` — tidak diperlukan

**Method yang dihapus:**
- `runOcr()` — OCR sekarang dijalankan otomatis, bukan via tombol
- `onOcrConsumed()` — tidak ada navigasi keluar
- `applyParsedReceipt(parsed: ParsedReceipt)` — mapping dihapus
- `saveCurrentOcrAsPattern(pattern)` — pattern dihapus
- `onNavigateToPatternEdit(onNavigate)` — pattern dihapus

**Method yang diubah:**
- `onScannedPhoto(uri)` — setelah compress & save, auto-run OCR
- `onPhotoSelected(uri)` — setelah foto dipilih, auto-run OCR (untuk flow kamera/galeri yang langsung save)

**Method baru:**
- `getSuggestions(fieldType: FieldType, query: String): List<String>` — mengembalikan daftar suggestion terfilter berdasarkan field type dan query user. Delegasi ke `TokenMatcher.matches(query, line)`.

### 4.3. `AddEditScreen.kt`

**Dihapus:**
- Parameter `onNavigateToOcrReview` dan `LaunchedEffect(ocrResult)` — tidak ada navigasi ke OcrReviewScreen
- Tombol "Ekstrak Teks" di `PhotoSection` — OCR jalan otomatis
- Loading indicator untuk `isRunningOcr` di button area (dipindahkan atau dihilangkan)

**Diubah:**
Setiap `OutlinedTextField` di field berikut di-wrap dengan `ExposedDropdownMenuBox`:
- `RestaurantInfoSection` → Nama Tempat, Alamat
- `MenuItemCard` → Nama Menu, Jumlah, Harga Satuan
- `GrandTotalSection` → Override Total

**Struktur pattern untuk setiap field autocomplete:**

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoCompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<String>,
    label: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && suggestions.isNotEmpty(),
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = true
            },
            modifier = modifier.menuAnchor(),
            label = { Text(label) },
            singleLine = true
        )
        DropdownMenu(
            expanded = expanded && suggestions.isNotEmpty(),
            onDismissRequest = { expanded = false }
        ) {
            suggestions.forEach { suggestion ->
                DropdownMenuItem(
                    text = { Text(suggestion) },
                    onClick = {
                        onValueChange(suggestion)
                        expanded = false
                    }
                )
            }
        }
    }
}
```

### 4.4. `TokenMatcher.kt` (File Baru)

File utilitas di package `com.pndnwngi.billumaba.ui.addedit`:

```kotlin
object TokenMatcher {
    /**
     * Memeriksa apakah baris teks match dengan query menggunakan
     * prefix matching berbasis token, case-insensitive.
     *
     * Baris dipecah menjadi token berdasarkan spasi.
     * Mengembalikan true jika minimal satu token dimulai dengan query.
     */
    fun matches(line: String, query: String): Boolean {
        if (query.isEmpty()) return true
        val lowerQuery = query.lowercase()
        return line.split(" ")
            .any { token -> token.lowercase().startsWith(lowerQuery) }
    }

    /**
     * Filter daftar baris OCR.
     * Untuk field numerik, hanya baris yang mengandung digit.
     */
    fun filter(
        lines: List<String>,
        query: String,
        numericOnly: Boolean = false
    ): List<String> {
        return lines
            .filter { line ->
                if (numericOnly) line.any { it.isDigit() } else true
            }
            .filter { line -> matches(line, query) }
    }
}
```

### 4.5. `AppNavigation.kt`

- Hapus `pendingOcrResult` dan `pendingParsedReceipt` shared state.
- Hapus `composable(route = Screen.OcrReview.route)`.
- Hapus `composable(route = Screen.PatternList.route)`.
- Hapus `composable(route = Screen.PatternEdit.route)`.
- Hapus `LaunchedEffect(pendingParsedReceipt)`.
- Hapus parameter `onNavigateToOcrReview` dari pemanggilan `AddEditScreen`.

### 4.6. `Screen.kt`

```diff
sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object AddEdit : Screen("add_edit?visitId={visitId}") { ... }
    data object Detail : Screen("detail/{visitId}") { ... }
-   data object OcrReview : Screen("ocr_review")
-   data object PatternList : Screen("patterns")
-   data object PatternEdit : Screen("patterns/edit?id={id}") { ... }
}
```

### 4.7. `AppDatabase.kt`

- Kembali ke `@Database(version = 1, entities = [VisitEntity::class, MenuItemEntity::class])`.
- Hapus abstract method `receiptPatternDao(): ReceiptPatternDao`.
- Hapus `@TypeConverters` jika tidak ada lagi.

### 4.8. `DatabaseModule.kt`

```diff
- MIGRATION_1_2 dihapus dari addMigrations()
- provideReceiptPatternDao() dihapus
+ return Room.databaseBuilder(context, AppDatabase::class.java, "bill_umaba.db")
+     .addCallback(AppDatabaseCallback(repository))
+     .build()
```

### 4.9. `DashboardScreen.kt`

- Hapus `onNavigateToPatterns` callback.
- Hapus `IconButton` di `TopAppBar` untuk navigasi ke Pattern Management.

### 4.10. `CulinaryRepository.kt` & `CulinaryRepositoryImpl.kt`

- Hapus import atau referensi ke `ReceiptPatternEntity` / `ReceiptPatternDao` jika ada.

---

## 5. Navigation Routes (Setelah Perubahan)

```kotlin
sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object AddEdit : Screen("add_edit?visitId={visitId}") {
        fun createRoute(visitId: Long? = null): String =
            if (visitId != null) "add_edit?visitId=$visitId" else "add_edit"
    }
    data object Detail : Screen("detail/{visitId}") {
        fun createRoute(visitId: Long): String = "detail/$visitId"
    }
}
```

---

## 6. Dependensi

### Tetap
```toml
mlkit-document-scanner       = "16.0.0-beta1"
mlkit-text-recognition-latin = "16.0.0.1"
coroutines-play-services     = "1.8.1"
room                         = "2.6.1"
hilt                         = "2.51.1"
navigationCompose            = "2.8.7"
```

Tidak ada library baru yang dibutuhkan. `ExposedDropdownMenuBox` dan `DropdownMenu` sudah tersedia di Material 3 Compose.
