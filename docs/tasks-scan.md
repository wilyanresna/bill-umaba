# Tasks — Fitur Scan Struk

> Tasks disusun per **4 tahap** sesuai requirement awal. Setiap tahap berisi task-task kecil yang actionable untuk **menulis kode**.
>
> **Catatan:** Compile, build, dan testing (manual maupun automated) dilakukan sendiri oleh user. Tasks di sini hanya mencakup **apa yang harus ditulis/diubah**, tanpa verification step.

**Konvensi path:**
- `android/app/src/main/java/com/pndnwngi/billumaba/...`
- `android/app/src/main/res/...`
- `android/app/src/test/...` (unit test source set)
- `android/app/src/androidTest/...` (instrumented test source set)
- `android/gradle/libs.versions.toml`
- `android/app/build.gradle.kts`
- `android/app/src/main/AndroidManifest.xml`
- `docs/...`

---

## Tahap 1 — Auto-Frame Struk (Document Scanner Integration)

**Tujuan:** Scan jadi PRIMARY action di photo section. Foto struk otomatis lurus & cropped. GMS unavailable → fallback ke kamera biasa. Gallery flow → tawarkan scan (default Yes).

### 1.1. Setup Dependency
- [x] **Tambah ML Kit Document Scanner ke `libs.versions.toml`**
  - Version: `16.0.0-beta1`
  - Library entry: `androidx-mlkit-document-scanner = { group = "com.google.mlkit", name = "document-scanner", version.ref = "mlkit-document-scanner" }`

- [x] **Tambah dependency ke `app/build.gradle.kts`**
  - `implementation(libs.androidx.mlkit.document.scanner)`

### 1.2. GMS Availability Utility
- [x] **Buat `util/GooglePlayServicesUtil.kt`**
  - `object GooglePlayServicesUtil` atau top-level function
  - `fun isGooglePlayServicesAvailable(context: Context): Boolean`
  - Pakai `GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)`
  - Return true jika `ConnectionResult.SUCCESS`

### 1.3. ReceiptScanner Composable
- [x] **Buat `ui/components/ReceiptScanner.kt`**
  - `@Composable fun rememberReceiptScanner(onResult: (Uri?) -> Unit, onGmsUnavailable: () -> Unit): () -> Unit`
  - Pakai `rememberLauncherForActivityResult(StartScan())`
  - Konfigurasi `GmsDocumentScannerOptions`:
    - `setScannerMode(SCANNER_MODE_FULL)` (allow manual adjust)
    - `setGalleryImportAllowed(false)` (kita handle gallery sendiri)
    - `setResultFormats(RESULT_FORMAT_JPEG)` (JPG only)
    - `setPageLimit(1)`
  - Returned lambda: cek GMS via utility, jika OK launch scanner, jika tidak panggil `onGmsUnavailable`
  - Handle result: extract first page Uri, panggil `onResult(uri)`

### 1.4. Modifikasi AddEditUiState
- [x] **Edit `ui/addedit/AddEditUiState.kt`**
  - Tambah field:
    - `isProcessingScan: Boolean = false`
    - `pendingGalleryUri: String? = null`
    - `showRapikanDialog: Boolean = false`
    - `showGmsFallbackDialog: Boolean = false`

### 1.5. Modifikasi AddEditViewModel
- [x] **Edit `ui/addedit/AddEditViewModel.kt`**
  - Inject `ImageCompressor`, `StorageManager`, `@ApplicationContext Context` via Hilt (existing constructors)
  - Tambah handler `onScannedPhoto(uri: Uri)`:
    - Set `isProcessingScan = true`
    - Launch viewModelScope, panggil `imageCompressor.compressImage(context, uri)`
    - Simpan hasil ke `storageManager.saveReceiptImage(bytes)`
    - Update state: `existingPhotoPath = path`, `receiptPhotoUri = null`, `isProcessingScan = false`
  - Tambah handler `onGalleryPhotoSelected(uri: String)`:
    - Set `pendingGalleryUri = uri`, `showRapikanDialog = true`
  - Tambah handler `onConfirmRapikan(yes: Boolean)`:
    - Set `showRapikanDialog = false`
    - Jika yes + pendingGalleryUri != null → panggil scanner dengan import URI (Document Scanner bisa import)
    - Jika no → langsung panggil `onScannedPhoto(Uri.parse(pendingGalleryUri))`
    - Reset `pendingGalleryUri = null`
  - Tambah handler `onDismissGmsFallbackDialog()` dan `onGmsFallback()`:
    - Set `showGmsFallbackDialog = true`, lalu panggil `onCameraRequested` callback (parent handle)

### 1.6. Modifikasi PhotoPicker
- [x] **Edit `ui/components/PhotoPicker.kt`**
  - **Empty state**: ganti layout 2 tombol (Kamera/Galeri) jadi **1 tombol besar "Scan / Foto Struk"** (primary, full-width)
  - Tap tombol → tampilkan **ModalBottomSheet** dengan 3 opsi:
    - "Scan dengan auto-frame" (default focus, leading icon scanner)
    - "Kamera biasa"
    - "Pilih dari Galeri"
  - **Has photo state**: tampilkan tombol kecil "Ganti Foto" + "Edit / Scan Ulang"
  - Refactor: pisah jadi 2 composable `PhotoPickerEmpty` dan `PhotoPickerWithPhoto`
  - Tambah callback `onScanRequested`, `onGalleryRequested`, `onCameraRequested`, `onRescanRequested`

### 1.7. Modifikasi AddEditScreen — Flow Integration
- [x] **Edit `ui/addedit/AddEditScreen.kt`**
  - Update `PhotoSection` signature: tambah parameter callbacks `onScanRequested`, `onGalleryRequested`, `onCameraRequested`, `onRescanRequested`
  - Wire up `rememberReceiptScanner` di `AddEditScreen` scope
  - **Empty state flow**:
    - Tap "Scan" → panggil `launchReceiptScanner()` (returned lambda)
    - Success → `viewModel.onScannedPhoto(uri)`
    - GMS unavailable → `viewModel.onGmsFallback()` → trigger dialog + camera biasa
  - **Gallery flow**:
    - Tap "Pilih dari Galeri" → existing galleryLauncher
    - Result → `viewModel.onGalleryPhotoSelected(uri)` → trigger dialog
    - Dialog result → `viewModel.onConfirmRapikan(yes/no)`
  - **Rescan flow**:
    - Tap "Edit / Scan Ulang" → `launchReceiptScanner()` dengan existing photo sebagai input (jika supported) atau foto baru
  - Tampilkan `AlertDialog`:
    - "Rapikan foto dengan auto-scan?" — Yes (default) / No
    - "Perangkat tidak mendukung scan otomatis" — OK → fallback ke Camera
  - Tampilkan `CircularProgressIndicator` overlay saat `isProcessingScan = true`

### 1.8. Update Architecture Docs (opsional)
- [x] **Append section ke `docs/architecture-scan.md`**
  - Section "8.1. Tahap 1 Implementation Notes" — flow diagram dan dependencies ML Kit Document Scanner

---

## Tahap 2 — OCR (Extract Text)

**Tujuan:** Dari foto struk yang sudah ada, extract text + koordinat per line. Tampilkan di OcrReviewScreen yang editable.

### 2.1. Setup Dependency
- [ ] **Tambah ML Kit Text Recognition Latin ke `libs.versions.toml`**
  - Version: `16.0.0.1`
  - Library entry: `androidx-mlkit-text-recognition-latin`

- [ ] **Tambah kotlinx-coroutines-play-services ke `libs.versions.toml`**
  - Version: `1.8.1`
  - Library entry: `kotlinx-coroutines-play-services`

- [ ] **Tambah dependencies ke `app/build.gradle.kts`**
  - `implementation(libs.androidx.mlkit.text.recognition.latin)`
  - `implementation(libs.kotlinx.coroutines.play.services)`

### 2.2. OCR Data Models
- [ ] **Buat `data/ocr/OcrModels.kt`**
  - `data class OcrResult(val lines: List<OcrLine>)`
  - `data class OcrLine(val text: String, val boundingBox: android.graphics.Rect?, val confidence: Float?)`

### 2.3. ReceiptOcrEngine
- [ ] **Buat `data/ocr/ReceiptOcrEngine.kt`**
  - `@Singleton class ReceiptOcrEngine @Inject constructor()`
  - Private field: `TextRecognition.getClient(LatinTextRecognizerOptions.Builder().build())`
  - `suspend fun recognize(context: Context, imageUri: Uri): OcrResult`
  - Pakai `withContext(Dispatchers.Default)` + `InputImage.fromFilePath(context, imageUri)`
  - `recognizer.process(input).await()` (extension dari kotlinx-coroutines-play-services)
  - Map `Text.TextBlock` → list of `OcrLine`

### 2.4. Hilt Module
- [ ] **Buat `di/OcrModule.kt`** (placeholder, jika perlu)
  - `@Module @InstallIn(SingletonComponent::class) object OcrModule`
  - Note: tidak wajib karena `ReceiptOcrEngine` sudah `@Singleton` + constructor-injectable

### 2.5. Modifikasi AddEditUiState — OCR Fields
- [ ] **Edit `ui/addedit/AddEditUiState.kt`**
  - Tambah field:
    - `isRunningOcr: Boolean = false`
    - `ocrResult: OcrResult? = null`

### 2.6. Modifikasi AddEditViewModel — OCR Handler
- [ ] **Edit `ui/addedit/AddEditViewModel.kt`**
  - Inject `ReceiptOcrEngine`
  - Tambah handler `runOcr()`:
    - Ambil `existingPhotoPath` dari state
    - Set `isRunningOcr = true`
    - Launch viewModelScope
    - Panggil `ocrEngine.recognize(context, Uri.fromFile(File(photoPath)))`
    - Set `ocrResult = result`, `isRunningOcr = false`
  - Tambah `onOcrConsumed()` untuk clear `ocrResult` setelah dipakai OcrReviewScreen

### 2.7. OcrReviewScreen — UI State
- [ ] **Buat `ui/ocr/OcrReviewUiState.kt`**
  - `isLoading: Boolean = false`
  - `ocrResult: OcrResult? = null`
  - `editedLines: List<String> = emptyList()` (text editable per line, mirror dari ocrResult)
  - `errorMessage: String? = null`
  - `parserTypeOverride: ParserType? = null` (untuk Tahap 3, nullable di Tahap 2)
  - `detectedParserType: ParserType? = null` (untuk Tahap 3)
  - `parsedReceipt: ParsedReceipt? = null` (untuk Tahap 3)
  - `isProcessingParse: Boolean = false` (untuk Tahap 3)

### 2.8. OcrReviewScreen — ViewModel
- [ ] **Buat `ui/ocr/OcrReviewViewModel.kt`**
  - `@HiltViewModel`
  - Inject `AddEditViewModel` via Hilt (shared activity scope) **atau** baca `ocrResult` dari `SavedStateHandle`
  - Pattern yang dipilih: inject `AddEditViewModel` melalui `viewModels()` di activity scope — dokumentasikan pilihan di code comment
  - Handlers:
    - `loadOcrResult()`: ambil dari `AddEditViewModel.uiState.ocrResult`, populate `editedLines`
    - `updateLineText(index: Int, newText: String)`: update `editedLines[index]`
    - `applyToForm(onApplied: () -> Unit)`: di Tahap 2 stub (langsung panggil `onApplied`); di Tahap 3 akan panggil parser
  - Error handling: `errorMessage` jika OCR gagal

### 2.9. OcrReviewScreen — UI
- [ ] **Buat `ui/ocr/OcrReviewScreen.kt`**
  - TopAppBar: "Review Hasil OCR" + back button
  - Loading indicator saat `isLoading` atau `isRunningOcr`
  - List baris OCR dengan `LazyColumn`:
    - Tiap item: index nomor, `OutlinedTextField` (editable, mirror `editedLines[index]`), confidence indicator (`AssistChip` kecil)
  - Di bawah list: tombol "Pakai Hasil Scan" (primary, full-width) — di Tahap 2 stub (langsung navigate back)
  - Placeholder section "Tipe Struk" — di-render di Tahap 3
  - Error handling: snackbar jika `errorMessage != null`
  - Pakai M3 components: `OutlinedTextField`, `AssistChip`, `Button`, `CircularProgressIndicator`

### 2.10. Navigation — Tambah Route
- [ ] **Edit `ui/navigation/Screen.kt`**
  - Tambah `data object OcrReview : Screen("ocr_review")`

- [ ] **Edit `ui/navigation/AppNavigation.kt`**
  - Tambah `composable("ocr_review") { OcrReviewScreen(onNavigateBack = ...) }`
  - Pass data: pakai `AddEditViewModel` activity-scoped (dokumentasikan di comment) **atau** `SavedStateHandle` dengan key `ocrResult`

### 2.11. Modifikasi AddEditScreen — Tombol Ekstrak Teks
- [ ] **Edit `ui/addedit/AddEditScreen.kt`**
  - Di `PhotoSection` (saat `hasPhoto == true`), tambah tombol `OutlinedButton "Ekstrak Teks"` di bawah preview foto
  - Tombol disabled saat `isRunningOcr`
  - OnClick → panggil `viewModel.runOcr()` lalu `onNavigateToOcrReview()` (callback parameter)
  - Tambah parameter `onNavigateToOcrReview: () -> Unit` di `AddEditScreen` signature

- [ ] **Edit `ui/navigation/AppNavigation.kt`**
  - Di composable `add_edit`, wire up `onNavigateToOcrReview = { navController.navigate("ocr_review") }`

---

## Tahap 3 — Text Mapping (3 Preset Parser + Auto-Detect)

**Tujuan:** Parse OCR result → ParsedReceipt. 3 parser preset (General, Restaurant, Retail Thermal). Auto-detect dari keyword. User bisa override tipe.

### 3.1. Parser Data Models
- [ ] **Buat `data/parser/ParsedReceipt.kt`**
  - `data class ParsedReceipt(...)` — field: detectedParserType, restaurantName, menuItems, grandTotal, tax, service, discount, visitDate
  - `enum class ParserType { GENERAL, RESTAURANT, RETAIL_THERMAL }`
  - `data class ParsedMenuItem(name, quantity, price, subtotal)`

### 3.2. ReceiptParser Interface
- [ ] **Buat `data/parser/ReceiptParser.kt`**
  - `interface ReceiptParser { fun parse(ocr: OcrResult): ParsedReceipt }`

### 3.3. GeneralReceiptParser
- [ ] **Buat `data/parser/GeneralReceiptParser.kt`**
  - `@Singleton class GeneralReceiptParser @Inject constructor() : ReceiptParser`
  - Regex constants (private vals di companion object atau top-level):
    - `PRICE_REGEX` — match angka dengan/tanpa `Rp`/`IDR`, format Indonesia (titik ribuan, koma decimal)
    - `QTY_REGEX` — match `2x`, `2 x`, `2*` di awal baris
    - `TOTAL_KEYWORDS` — list: `["total", "grand total", "jumlah", "tagihan", "amount due"]`
    - `TAX_KEYWORDS` — list: `["pajak", "ppn", "tax"]`
    - `SERVICE_KEYWORDS` — list: `["service", "service charge", "pelayanan", "pb1"]`
    - `DISCOUNT_KEYWORDS` — list: `["diskon", "discount", "potongan", "voucher"]`
    - `DATE_REGEX` — match `dd/MM/yyyy`, `dd-MM-yy`, `dd MMM yyyy`
    - `ADDRESS_KEYWORDS` — list: `["jl", "jalan", "no.", "telp", "phone"]` (untuk skip nama restaurant)
  - Implement `parse(ocr: OcrResult): ParsedReceipt`:
    - `extractRestaurantName(lines)`: 1-2 line teratas, filter yang mengandung `ADDRESS_KEYWORDS`
    - `extractMenuItems(lines)`: line yang punya price match `PRICE_REGEX`, exclude line dengan `TOTAL_KEYWORDS`/`TAX_KEYWORDS`/`SERVICE_KEYWORDS`/`DISCOUNT_KEYWORDS`
    - `extractGrandTotal(lines)`: cari line dengan `TOTAL_KEYWORDS` (prioritas "grand total" > "total") + angka terbesar
    - `extractTax/Service/Discount(lines)`: cari line dengan keyword + extract price
    - `extractDate(lines)`: scan semua line untuk `DATE_REGEX`
  - Helper `parsePrice(string)`: normalize "25.000" atau "25,000" atau "25000" → Double
  - Helper `parseQuantity(name)`: extract qty dari awal nama (e.g. "2x Nasi Goreng" → qty=2, name="Nasi Goreng")

### 3.4. RestaurantReceiptParser
- [ ] **Buat `data/parser/RestaurantReceiptParser.kt`**
  - `@Singleton class RestaurantReceiptParser @Inject constructor() : ReceiptParser`
  - Pakai **composition** (bukan inheritance dari General) — instance `GeneralReceiptParser` sebagai helper
  - Logic tambahan:
    - Identifikasi section "Subtotal" sebagai boundary items vs summary
    - Items hanya dari line sebelum "Subtotal"
    - Pajak + Service diparse terpisah (Pajak Restoran + Service Charge)
    - Discount diparse dari section antara Subtotal dan Grand Total
    - Grand Total = max angka setelah keyword "Total" / "Grand Total" di section summary
  - Pakai `PRICE_REGEX` & `DATE_REGEX` dari companion (duplicate constants atau extract ke file terpisah `ParserRegex.kt`)

### 3.5. RetailThermalParser
- [ ] **Buat `data/parser/RetailThermalParser.kt`**
  - `@Singleton class RetailThermalParser @Inject constructor() : ReceiptParser`
  - Deteksi keyword cash register: `["tunai", "kembali", "kembalian", "bayar", "kartu", "debit", "credit"]`
  - Item format support:
    - Format 1: `Nama   Qty x Harga = Subtotal` (aligned columns)
    - Format 2: `Nama  Harga` (satu angka per line, qty implicit 1)
  - Total di section bawah sebelum "Tunai/Kembali"
  - Auto-skip promo: `["potongan", "disc", "hemat", "voucher"]`
  - Extract `Tunai` (bayar) dan `Kembali` (change) sebagai field tambahan di `ParsedReceipt` (perlu tambah `cashPaid` dan `cashChange` ke `ParsedReceipt` — atau skip jika tidak perlu)

### 3.6. ReceiptParserFactory
- [ ] **Buat `data/parser/ReceiptParserFactory.kt`**
  - `@Singleton class ReceiptParserFactory @Inject constructor(...)`
  - Constructor params: `GeneralReceiptParser`, `RestaurantReceiptParser`, `RetailThermalParser`
  - `fun parse(ocr: OcrResult, overrideType: ParserType? = null): ParsedReceipt`:
    - Pilih parser type: `overrideType ?: autoDetectType(ocr)`
    - Panggil parser sesuai type
    - Return ParsedReceipt (override `detectedParserType` di result dengan type yang dipakai)
  - `private fun autoDetectType(ocr: OcrResult): ParserType`:
    - Lowercase + join semua `OcrLine.text` jadi satu string
    - Cek keyword:
      - Cash register markers (`tunai`/`kembali`/`kembalian`/`bayar`/`kartu`) → `RETAIL_THERMAL`
      - Resto markers (`pajak`/`ppn`/`service` + `subtotal`/`sub total`) → `RESTAURANT`
      - Else → `GENERAL`

### 3.7. Hilt Module
- [ ] **Buat `di/ParserModule.kt`** (opsional, hanya jika perlu qualifier)
  - Default: 3 parser sudah concrete-class dengan `@Inject constructor`, jadi Hilt auto-resolve — tidak perlu module
  - Module ini hanya untuk binding `ReceiptParserFactory` jika perlu

### 3.8. Modifikasi OcrReviewUiState (Tahap 3 fields)
- [ ] **Edit `ui/ocr/OcrReviewUiState.kt`**
  - Tambah field (Tahap 3):
    - `detectedParserType: ParserType? = null`
    - `parsedReceipt: ParsedReceipt? = null`
  - Field existing: `parserTypeOverride: ParserType? = null` (sudah ada di Tahap 2)
  - Hapus `isProcessingParse` (gabung dengan `isLoading`)

### 3.9. Modifikasi OcrReviewViewModel — Parser Integration
- [ ] **Edit `ui/ocr/OcrReviewViewModel.kt`**
  - Inject `ReceiptParserFactory`
  - Handler `runDetection()`:
    - Bangun `OcrResult` baru dari `editedLines` (user-edited text tetap dipakai)
    - Launch viewModelScope
    - Set `isLoading = true`
    - Panggil `factory.parse(editedOcr, parserTypeOverride)`
    - Set `parsedReceipt = result`, `detectedParserType = result.detectedParserType`
    - Set `isLoading = false`
  - Auto-run `runDetection()` di `init` block setelah `loadOcrResult()` selesai
  - Handler `setParserTypeOverride(type: ParserType?)`:
    - Update state
    - Trigger `runDetection()` ulang
  - Handler `applyToForm(onApplied: () -> Unit)`:
    - Panggil `addEditViewModel.applyParsedReceipt(parsedReceipt)`
    - Panggil `addEditViewModel.onOcrConsumed()` (clear ocrResult)
    - Panggil `onApplied()` (navigate back)

### 3.10. Modifikasi OcrReviewScreen — UI Parser Section
- [ ] **Edit `ui/ocr/OcrReviewScreen.kt`**
  - Aktifkan section "Tipe Struk" yang sebelumnya placeholder:
    - Label dinamis: "✓ Terdeteksi sebagai: **[detectedParserType.displayName]**" (di bawah list baris)
    - `ExposedDropdownMenuBox` dengan opsi: "Auto", "Umum", "Resto", "Retail"
      - "Auto" → `setParserTypeOverride(null)` (gunakan auto-detect)
      - "Umum/Resto/Retail" → `setParserTypeOverride(ParserType.XXX)`
    - On change → `viewModel.setParserTypeOverride(...)`
  - Preview parsed result ringkas di bawah dropdown: "X item, Total Rp XXX.XXX" (parsed dari `parsedReceipt`)
  - Tombol "Pakai Hasil Scan" → `viewModel.applyToForm(onApplied = onNavigateBack)`

### 3.11. Modifikasi AddEditViewModel — Apply Parsed Receipt
- [ ] **Edit `ui/addedit/AddEditViewModel.kt`**
  - Inject `ReceiptParserFactory` (jika perlu akses langsung, atau via OcrReviewViewModel)
  - Handler `applyParsedReceipt(parsed: ParsedReceipt)`:
    - Update `restaurantName = parsed.restaurantName.takeUnless { it.isNullOrBlank() } ?: state.restaurantName`
    - Update `visitDate = parsed.visitDate ?: state.visitDate`
    - Replace `menuItems` dengan `parsed.menuItems.map { MenuItemInput(name = ..., quantity = ..., price = ..., rating = 5.0f, notes = "") }`
    - Override `grandTotal`: set `isGrandTotalOverridden = true`, `grandTotalOverride = parsed.grandTotal.toString()` (jika `parsed.grandTotal != null`)
    - Set `ocrResult = null` (clear setelah apply)

### 3.12. Unit Tests untuk Parser
- [ ] **Buat `app/src/test/java/com/pndnwngi/billumaba/data/parser/GeneralReceiptParserTest.kt`**
  - Test dengan `OcrResult` hardcoded dari fixture struk café
  - Assert: `restaurantName`, `menuItems.size`, `grandTotal`, `tax == null`

- [ ] **Buat `app/src/test/java/com/pndnwngi/billumaba/data/parser/RestaurantReceiptParserTest.kt`**
  - Test dengan `OcrResult` hardcoded dari fixture struk resto
  - Assert: `tax != null`, `service != null`, `grandTotal` = expected

- [ ] **Buat `app/src/test/java/com/pndnwngi/billumaba/data/parser/RetailThermalParserTest.kt`**
  - Test dengan `OcrResult` hardcoded dari fixture struk Indomaret
  - Assert: item count, total, tunai, kembali

- [ ] **Buat `app/src/test/java/com/pndnwngi/billumaba/data/parser/ReceiptParserFactoryTest.kt`**
  - Test auto-detect untuk 3 tipe dari `OcrResult` berbeda
  - Test override type diprioritaskan dari auto-detect

- [ ] **Buat fixture files di `app/src/test/resources/receipts/`**
  - `simple_cafe.txt` — list of OCR lines untuk struk café
  - `restaurant_with_tax.txt` — list of OCR lines untuk struk resto
  - `indomaret.txt` — list of OCR lines untuk struk Indomaret
  - Helper `OcrResult.fromFixture(filename: String): OcrResult` (di test util atau per-test inline)

---

## Tahap 4 — Dynamic Pattern (Visual Builder + DB v2)

**Tujuan:** User bisa simpan "pattern" parsing per restaurant via UI visual (no regex). Pattern dipakai otomatis saat scan dari tempat yang sama. DB migration v1→v2 proper (preserve data).

### 4.1. ReceiptPatternEntity
- [ ] **Buat `data/database/entities/ReceiptPatternEntity.kt`**
  - `@Entity(tableName = "receipt_patterns", indices = [Index(value = ["restaurantName"], unique = true)])`
  - Field: id, restaurantName, displayName, menuLineTemplate, totalLineStrategy, totalLineRegex (nullable), taxLineRegex (nullable), serviceLineRegex (nullable), discountLineRegex (nullable), dateRegex (nullable), restaurantNameStrategy, headerLineCount, skipKeywords, parserType, createdAt, lastUsedAt, usageCount

### 4.2. ReceiptPatternDao
- [ ] **Buat `data/database/dao/ReceiptPatternDao.kt`**
  - `@Dao interface ReceiptPatternDao`
  - `@Query("SELECT * FROM receipt_patterns WHERE LOWER(restaurantName) = LOWER(:name) LIMIT 1") suspend fun findByName(name: String): ReceiptPatternEntity?`
  - `@Query("SELECT * FROM receipt_patterns ORDER BY lastUsedAt DESC") fun observeAll(): Flow<List<ReceiptPatternEntity>>`
  - `@Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(pattern: ReceiptPatternEntity): Long`
  - `@Delete suspend fun delete(pattern: ReceiptPatternEntity)`
  - `@Query("UPDATE receipt_patterns SET lastUsedAt = :ts, usageCount = usageCount + 1 WHERE id = :id") suspend fun touch(id: Long, ts: Long)`
  - `@Query("SELECT receiptPhotoPath FROM visits WHERE receiptPhotoPath IS NOT NULL ORDER BY visitDate DESC LIMIT :limit") suspend fun getRecentPhotoPaths(limit: Int = 20): List<String>`

### 4.3. Modifikasi AppDatabase
- [ ] **Edit `data/database/AppDatabase.kt`**
  - Tambah `ReceiptPatternEntity::class` ke `entities` list
  - `version = 2`
  - `exportSchema = true` (pastikan `schemas` folder di `.gitignore` exclusion atau di-include)
  - Tambah `abstract fun receiptPatternDao(): ReceiptPatternDao`

### 4.4. Migrations File
- [ ] **Buat `data/database/Migrations.kt`**
  - `val MIGRATION_1_2 = object : Migration(1, 2) { override fun migrate(db: SupportSQLiteDatabase) { ... } }`
  - SQL: `CREATE TABLE IF NOT EXISTS receipt_patterns (...)` + `CREATE UNIQUE INDEX IF NOT EXISTS index_receipt_patterns_restaurantName ON receipt_patterns(restaurantName)`
  - Schema columns sesuai `ReceiptPatternEntity`

### 4.5. Modifikasi DatabaseModule
- [ ] **Edit `di/DatabaseModule.kt`**
  - Tambah `.addMigrations(MIGRATION_1_2)` (HILANGKAN `fallbackToDestructiveMigration()` jika ada)
  - Provide `ReceiptPatternDao`:
    - `@Provides fun provideReceiptPatternDao(db: AppDatabase): ReceiptPatternDao = db.receiptPatternDao()`

### 4.6. Test DB Migration (kode)
- [ ] **Buat `app/src/androidTest/java/com/pndnwngi/billumaba/data/database/MigrationTest.kt`**
  - Extend `AndroidJUnit4` + `MigrationTestHelper`
  - Test: create v1 DB (manual SQL), insert sample visit, run `MIGRATION_1_2.migrate(db)`, verify:
    - Existing data masih ada
    - Tabel `receipt_patterns` exist dengan schema benar
    - Tabel `receipt_patterns` kosong

### 4.7. TemplateToRegex Utility
- [ ] **Buat `data/parser/TemplateToRegex.kt`**
  - `object TemplateToRegex`
  - `private val TEMPLATE_TOKENS = mapOf(...)` untuk `{qty}`, `{name}`, `{price}`, `{subtotal}`
  - `fun convert(template: String): Regex`:
    - Escape seluruh template dengan `Regex.escape()`
    - Replace token (yang sudah di-escape) dengan named group regex
    - Return `Regex(result, RegexOption.IGNORE_CASE)`
  - Unit test inline atau file terpisah

### 4.8. PatternReceiptParser
- [ ] **Buat `data/parser/PatternReceiptParser.kt`**
  - `class PatternReceiptParser(private val pattern: ReceiptPatternEntity) : ReceiptParser`
  - Build regex dari `pattern.menuLineTemplate` via `TemplateToRegex.convert()`
  - Build regex untuk `taxLineRegex`, `serviceLineRegex`, `discountLineRegex`, `dateRegex`, `totalLineRegex` jika ada
  - Apply `skipKeywords` (split CSV, lowercase, filter matching lines)
  - Apply `restaurantNameStrategy`:
    - `FIRST_LINE` → `lines.firstOrNull()?.text`
    - `FIRST_TWO_LINES` → `lines.take(2).joinToString(" ") { it.text }`
    - `AUTO_TOP` → sama seperti GeneralReceiptParser (1-2 line atas, filter alamat)
  - Apply `totalLineStrategy`:
    - `BIGGEST_TOTAL_KEYWORD` → cari line dengan "total" + angka terbesar
    - `LAST_LINE` → parse price dari line terakhir
    - `CUSTOM_REGEX` → pakai `totalLineRegex` langsung
  - Return `ParsedReceipt` dengan `detectedParserType = ParserType.valueOf(pattern.parserType)`

### 4.9. Modifikasi ReceiptParserFactory — Pattern Lookup
- [ ] **Edit `data/parser/ReceiptParserFactory.kt`**
  - Tambah constructor param: `patternDao: ReceiptPatternDao`
  - Update `parse()` signature: `suspend fun parse(ocr: OcrResult, restaurantName: String? = null, overrideType: ParserType? = null): ParsedReceipt`
  - Logic:
    - Jika `restaurantName` not blank → `patternDao.findByName(restaurantName)`
    - Jika pattern != null → pakai `PatternReceiptParser(pattern)`, panggil `patternDao.touch(pattern.id, System.currentTimeMillis())`
    - Else → pakai auto-detect atau override (existing logic)
  - Convert ke suspend function (jika sebelumnya bukan)

### 4.10. Modifikasi AddEditViewModel — Save Pattern
- [ ] **Edit `ui/addedit/AddEditViewModel.kt`**
  - Inject `ReceiptPatternDao` (atau via Repository pattern)
  - Tambah handler `saveCurrentOcrAsPattern(pattern: ReceiptPatternEntity)`:
    - Launch viewModelScope
    - Panggil `patternDao.upsert(pattern)`
  - Tambah state field: `showSavePatternDialog: Boolean = false` (jika pakai dialog di AddEditScreen)
  - Tambah `onNavigateToPatternEdit(onNavigate: () -> Unit)`: trigger navigasi ke PatternEditScreen

### 4.11. Navigation — Pattern Routes
- [ ] **Edit `ui/navigation/Screen.kt`**
  - Tambah `data object PatternList : Screen("patterns")`
  - Tambah `data object PatternEdit : Screen("patterns/edit?id={id}")` dengan `fun createRoute(id: Long? = null): String`

- [ ] **Edit `ui/navigation/AppNavigation.kt`**
  - Tambah `composable("patterns") { PatternListScreen(onNavigateBack = ..., onNavigateToEdit = { id -> ... }) }`
  - Tambah `composable("patterns/edit?id={id}", arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = -1L })) { PatternEditScreen(onNavigateBack = ...) }`

### 4.12. PatternListScreen — UI State
- [ ] **Buat `ui/patterns/PatternListUiState.kt`**
  - `isLoading: Boolean = false`
  - `patterns: List<ReceiptPatternEntity> = emptyList()`
  - `pendingDeleteId: Long? = null` (untuk konfirmasi delete)

### 4.13. PatternListScreen — ViewModel
- [ ] **Buat `ui/patterns/PatternListViewModel.kt`**
  - `@HiltViewModel`
  - Inject `ReceiptPatternDao` (langsung atau via Repository baru `PatternRepository`)
  - Init: collect `dao.observeAll()` → set `patterns`
  - Handler `deletePattern(id: Long)`: panggil `dao.delete(...)` (cari entity dari list, atau buat entity baru dengan id saja)
  - Handler `confirmDelete(id: Long)`: set `pendingDeleteId`
  - Handler `cancelDelete()`: clear `pendingDeleteId`

### 4.14. PatternListScreen — UI
- [ ] **Buat `ui/patterns/PatternListScreen.kt`**
  - TopAppBar: "Manajemen Pattern" + back button
  - `LazyColumn` dari patterns:
    - Tiap item: `ListItem` dengan:
      - Headline: `pattern.displayName` (fallback `pattern.restaurantName`)
      - Supporting: "Dipakai ${usageCount} kali · ${lastUsedFormatted}"
      - Trailing: icon delete (`Icons.Default.Delete`) dengan konfirmasi
  - `FloatingActionButton` `+` → navigate ke `PatternEdit.createRoute(null)`
  - Empty state: ilustrasi + text "Belum ada pattern tersimpan"
  - Delete confirmation: `AlertDialog` dengan "Hapus pattern X?" — Ya / Tidak

### 4.15. PatternEditScreen — UI State
- [ ] **Buat `ui/patterns/PatternEditUiState.kt`**
  - Field: id, restaurantName, displayName, parserType, restaurantNameStrategy, menuLineTemplate, separator, totalLineStrategy, totalLineRegex, taxEnabled, taxLineRegex, serviceEnabled, serviceLineRegex, discountEnabled, discountLineRegex, dateEnabled, dateRegex, headerLineCount, skipKeywords, isLoading, isSaving, showAdvanced, testPhotoUri, testResult, isRunningTest
  - `enum class NameStrategy { FIRST_LINE, FIRST_TWO_LINES, AUTO_TOP }`
  - `enum class TotalStrategy { BIGGEST_TOTAL_KEYWORD, LAST_LINE, CUSTOM_REGEX }`
  - `enum class Separator { X, SPACE, DASH, CUSTOM }`

### 4.16. PatternEditScreen — ViewModel
- [ ] **Buat `ui/patterns/PatternEditViewModel.kt`**
  - `@HiltViewModel`
  - Inject `ReceiptPatternDao`, `ReceiptOcrEngine`, `ReceiptParserFactory`, `StorageManager`, `@ApplicationContext Context`
  - Load existing pattern dari `SavedStateHandle` arg `id` (jika != -1L)
  - Handlers:
    - `updateNama(name: String)`, `updateDisplayName(name: String)`, `updateParserType(type: ParserType)`
    - `updateNameStrategy(strategy: NameStrategy)`, `updateTotalStrategy(strategy: TotalStrategy)`
    - `insertToken(token: String)`: insert ke `menuLineTemplate` di posisi cursor
    - `updateTemplate(template: String)`, `updateSeparator(sep: Separator)`
    - `toggleAdvanced()`, `updateTotalRegex(s: String)`, `updateTax(enabled, regex)`, dst untuk field opsional
    - `updateHeaderLineCount(n: Int)`
    - `addSkipKeyword(keyword: String)`, `removeSkipKeyword(keyword: String)`
    - `testWithGalleryPhoto(uri: Uri)`: copy foto ke cache, run OCR, run factory, set testResult
    - `testWithExistingVisit(visitPhotoPath: String)`: copy foto existing ke cache, run OCR, run factory, set testResult
    - `validate()`: cek `restaurantName` not blank, return boolean
    - `save(onSuccess: () -> Unit)`: validate, build entity, upsert ke DAO, panggil `onSuccess`
  - Method `buildEntity(): ReceiptPatternEntity` (private): convert UiState → Entity

### 4.17. PatternEditScreen — UI (Visual Builder)
- [ ] **Buat `ui/patterns/PatternEditScreen.kt`**

  **Layout sections (top to bottom):**

  1. **Header**: `TopAppBar` "Tambah/Edit Pattern" + back button + trailing save icon button

  2. **Basic Info** (dalam `OutlinedCard`):
     - `OutlinedTextField` "Nama Restoran *"
     - `OutlinedTextField` "Nama Tampilan (opsional)"
     - `ExposedDropdownMenuBox` "Tipe Parser": General / Resto / Retail

  3. **Sumber Nama Restoran** (dalam `OutlinedCard`):
     - `Column` dengan `RadioButton` per option:
       - "Baris pertama"
       - "Dua baris pertama"
       - "Auto detect (line teratas non-alamat)"

  4. **Template Item Menu** (dalam `OutlinedCard`):
     - Text "Format item menu"
     - `Row` dari `AssistChip`: QTY, NAMA, HARGA, SUBTOTAL — tap untuk insert ke template
     - `OutlinedTextField` "Template" (editable, show `{qty}x {name} {price}`)
     - `Row` dengan `ExposedDropdownMenuBox` "Separator" + input custom jika "Custom"

  5. **Strategi Grand Total** (dalam `OutlinedCard`):
     - `Column` dengan `RadioButton` per option
     - Jika `CUSTOM_REGEX`: tampilkan `OutlinedTextField` "Regex Total"

  6. **Field Opsional** (dalam `OutlinedCard`, semua collapsible / inline):
     - `Row` "Tax/PPN": `Switch` + `OutlinedTextField` "Regex"
     - `Row` "Service": `Switch` + `OutlinedTextField` "Regex"
     - `Row` "Discount": `Switch` + `OutlinedTextField` "Regex"
     - `Row` "Tanggal": `Switch` + `OutlinedTextField` "Regex"

  7. **Skip Keywords** (dalam `OutlinedCard`):
     - `FlowRow` dari `FilterChip` (X to remove)
     - `Row` dengan `OutlinedTextField` + `TextButton` "Tambah" untuk input custom
     - Suggestion chips: "subtotal", "pajak", "diskon", "service"

  8. **Header lines to skip**:
     - `OutlinedTextField` number (default 2)

  9. **Test Section** (dalam `ElevatedCard`):
     - Text "Test dengan foto" + icon play
     - `Row` 2 tombol: "Pilih dari Galeri" + "Pakai Foto dari Kunjungan"
     - Loading indicator saat `isRunningTest`
     - Result display: "Terdeteksi: [X] item, Total Rp XXX, Resto: [nama]"

  10. **Advanced** (collapsible, default collapsed):
      - `TextButton` "Advanced: lihat raw regex" → expand
      - Section "Raw Regex yang di-generate" — `Text` read-only dengan `TemplateToRegex.convert(template).pattern`
      - Tombol copy (optional)

  11. **Save button**: `Button` di bottom (primary, full-width)

- [ ] **Pakai M3 components**:
  - `OutlinedTextField`, `OutlinedCard`, `ElevatedCard`
  - `ExposedDropdownMenuBox`, `FilterChip`, `AssistChip`
  - `Switch`, `RadioButton`, `Button`, `TextButton`
  - `ModalBottomSheet` (untuk photo source picker di Test Section)
  - `TopAppBar`, `FloatingActionButton`

### 4.18. Dashboard Entry Point
- [ ] **Edit `ui/dashboard/DashboardScreen.kt`**
  - Tambah `IconButton` di `TopAppBar` `actions`: icon `Icons.Default.Tune` atau `Icons.Default.Settings`
  - OnClick → panggil `onNavigateToPatterns()` (callback parameter)
  - Tambah parameter `onNavigateToPatterns: () -> Unit` di `DashboardScreen` signature

- [ ] **Edit `ui/navigation/AppNavigation.kt`**
  - Di composable `dashboard`, wire up `onNavigateToPatterns = { navController.navigate("patterns") }`

### 4.19. Repository Pattern (opsional refactor)
- [ ] **Buat `data/repository/PatternRepository.kt`** (interface) dan `PatternRepositoryImpl.kt` (opsional, untuk konsistensi dengan `CulinaryRepository`)
  - `interface PatternRepository` dengan method: `getAll()`, `findByName(name)`, `save(pattern)`, `delete(pattern)`, `touch(id)`
  - `class PatternRepositoryImpl @Inject constructor(private val dao: ReceiptPatternDao) : PatternRepository`
  - Atau skip jika `PatternListViewModel` dan `PatternEditViewModel` inject DAO langsung sudah cukup

### 4.20. Update Content Docs (opsional)
- [ ] **Update `docs/architecture-scan.md`** dengan section "8.4. Tahap 4 Implementation Notes" — visual builder UX detail
- [ ] **Update `readme.md`** — tambah section singkat "## Fitur Scan Struk" (di bawah section existing)
