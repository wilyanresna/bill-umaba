# Task Breakdown – Fitur Scan Struk & Autocomplete

Daftar tugas implementasi untuk mengganti mekanisme Text Mapping + Pattern Dinamis menjadi OCR Otomatis + Autocomplete. Tugas dikelompokkan berdasarkan komponen dan diurutkan sesuai ketergantungan. Acuan: `prd-change-scan.md` dan `architecture-change-scan.md`.

---

## Grup 1: Penghapusan File (Delete Only)

Tidak ada ketergantungan antar file dalam grup ini. Kerjakan dalam urutan bebas.

| # | File | Keterangan |
|---|---|---|
| 1.1 | `data/parser/ParsedReceipt.kt` | Hapus data class, ParserType enum, ReceiptParser interface |
| 1.2 | `data/parser/GeneralReceiptParser.kt` | Hapus parser Umum |
| 1.3 | `data/parser/RestaurantReceiptParser.kt` | Hapus parser Resto |
| 1.4 | `data/parser/RetailThermalParser.kt` | Hapus parser Retail |
| 1.5 | `data/parser/ReceiptParserFactory.kt` | Hapus factory |
| 1.6 | `data/parser/PatternReceiptParser.kt` | Hapus parser berbasis pattern |
| 1.7 | `data/parser/TemplateToRegex.kt` | Hapus konverter |
| 1.8 | `data/database/dao/ReceiptPatternDao.kt` | Hapus DAO pattern |
| 1.9 | `data/database/entities/ReceiptPatternEntity.kt` | Hapus entity pattern |
| 1.10 | `data/database/Migrations.kt` | Hapus MIGRATION_1_2 |
| 1.11 | `ui/ocr/OcrReviewScreen.kt` | Hapus halaman review OCR |
| 1.12 | `ui/ocr/OcrReviewViewModel.kt` | Hapus ViewModel review OCR |
| 1.13 | `ui/ocr/OcrReviewUiState.kt` | Hapus UI state review OCR |
| 1.14 | `ui/patterns/PatternListScreen.kt` | Hapus daftar pattern |
| 1.15 | `ui/patterns/PatternListViewModel.kt` | Hapus ViewModel daftar pattern |
| 1.16 | `ui/patterns/PatternListUiState.kt` | Hapus UI state daftar pattern |
| 1.17 | `ui/patterns/PatternEditScreen.kt` | Hapus builder pattern |
| 1.18 | `ui/patterns/PatternEditViewModel.kt` | Hapus ViewModel builder pattern |
| 1.19 | `ui/patterns/PatternEditUiState.kt` | Hapus UI state builder pattern |

---

## Grup 2: TokenMatcher (New File)

Dibutuhkan oleh Grup 3 dan 4. Kerjakan terlebih dahulu.

| # | File | Keterangan |
|---|---|---|
| 2.1 | `ui/addedit/TokenMatcher.kt` | Buat utility object dengan fungsi `matches()` dan `filter()`. Lihat architecture-change-scan.md §4.4 untuk spesifikasi. |

---

## Grup 3: Perubahan Database & DI Layer

| # | File | Keterangan |
|---|---|---|
| 3.1 | `data/database/AppDatabase.kt` | Hapus `ReceiptPatternEntity` dari `entities` array. Hapus abstract method `receiptPatternDao()`. Ubah version ke `1`. Hapus export schema terkait migration. |
| 3.2 | `di/DatabaseModule.kt` | Hapus parameter `MIGRATION_1_2` dari `addMigrations()`. Hapus method `provideReceiptPatternDao()`. Hapus import terkait. |
| 3.3 | `data/repository/CulinaryRepositoryImpl.kt` | Hapus dependensi `ReceiptPatternDao` dari constructor jika ada. Hapus import `ReceiptPatternEntity`. |

---

## Grup 4: Perubahan AddEdit Layer (Core)

| # | File | Keterangan |
|---|---|---|
| 4.1 | `ui/addedit/AddEditUiState.kt` | Ganti `ocrResult: OcrResult?` menjadi `ocrLines: List<String>`. Hapus import `OcrResult`. |
| 4.2 | `ui/addedit/AddEditViewModel.kt` | **Hapus**: `ReceiptPatternDao` dari constructor injection. Method `runOcr()`, `onOcrConsumed()`, `applyParsedReceipt()`, `saveCurrentOcrAsPattern()`, `onNavigateToPatternEdit()`. Import `OcrResult`, `ParsedReceipt`, `ReceiptPatternEntity`, `ReceiptPatternDao`. **Ubah**: `onScannedPhoto()` → auto-run OCR setelah compress + save berhasil. **Tambah**: method private `runOcrForPhoto(path: String)` yang dipanggil dari `onScannedPhoto()` dan `onPhotoSelected()`. Method `getSuggestions(fieldType, query)` yang memanggil `TokenMatcher.filter()`. |
| 4.3 | `ui/addedit/AddEditScreen.kt` | **Hapus**: parameter `onNavigateToOcrReview`, `LaunchedEffect(ocrResult)`, tombol "Ekstrak Teks", block `if (isRunningOcr)`. **Tambah**: wrap setiap `OutlinedTextField` pada field Nama Tempat, Alamat, Nama Menu, Jumlah, Harga Satuan, dan Override Total dengan `ExposedDropdownMenuBox` + `DropdownMenu`. Lihat architecture-change-scan.md §4.3 untuk pola. |

### Spesifikasi Tambahan untuk 4.3 (AddEditScreen.kt)

Setiap field autocomplete menerima parameter `suggestions: List<String>` yang dihitung dengan `getSuggestions(fieldType, currentValue)`:

```
Field Type           numericOnly
─────────────────────────────────
Nama Tempat          false
Alamat               false
Nama Menu (per item) false
Jumlah (per item)    true
Harga Satuan (item)  true
Override Total       true
```

---

## Grup 5: Perubahan Navigation Layer

| # | File | Keterangan |
|---|---|---|
| 5.1 | `ui/navigation/Screen.kt` | Hapus `OcrReview`, `PatternList`, `PatternEdit`. |
| 5.2 | `ui/navigation/AppNavigation.kt` | Hapus `pendingOcrResult`, `pendingParsedReceipt` shared state. Hapus 3 `composable()` destination (ocr_review, patterns, patterns/edit). Hapus `LaunchedEffect(pendingParsedReceipt)`. Hapus parameter `onNavigateToOcrReview` dari `AddEditScreen(...)`. Hapus import `OcrResult`, `ParsedReceipt`. |

---

## Grup 6: Perubahan Dashboard & Repository Interface

| # | File | Keterangan |
|---|---|---|
| 6.1 | `ui/dashboard/DashboardScreen.kt` | Hapus parameter `onNavigateToPatterns`. Hapus `IconButton` di `TopAppBar` untuk ⚙. |
| 6.2 | `ui/dashboard/DashboardViewModel.kt` | Tidak langsung berubah, tapi pastikan tidak ada referensi ke pattern. |
| 6.3 | `data/repository/CulinaryRepository.kt` | Hapus method terkait pattern jika ada di interface. |

---

## Grup 7: Verifikasi & Build

| # | Tugas | Keterangan |
|---|---|---|
| 7.1 | Build project | Pastikan tidak ada error kompilasi dari file yang dihapus (import hilang, dll). |
| 7.2 | Uji coba manual | Foto struk → pastikan OCR berjalan otomatis. Ketik di field Nama Tempat → dropdown muncul dengan suggestion terfilter. Field numerik → hanya baris angka yang muncul. Ganti foto → suggestion diperbarui. Hapus foto → suggestion kosong. OCR gagal → form tetap berfungsi normal. |

---

## Urutan Pengerjaan

```
Grup 1 (Delete)   ──┐
                     ├──→ Grup 3 (DB/DI) ──→ Grup 6 (Dashboard)
                     │
Grup 2 (TokenMatcher) ──→ Grup 4 (AddEdit) ──→ Grup 5 (Navigation)
                                                     │
                                                     ↓
                                               Grup 7 (Verify)
```

- **Grup 1** dan **Grup 2** bisa dikerjakan paralel.
- **Grup 3** butuh Grup 1 selesai (file yang direferensikan sudah dihapus).
- **Grup 4** butuh Grup 2.
- **Grup 5** butuh Grup 4 (karena `AddEditScreen` signature berubah).
- **Grup 6** bisa dikerjakan setelah Grup 1 dan 3.
- **Grup 7** setelah semua selesai.

---

## Catatan Penting

1. **Jangan menghapus direktori kosong** (`data/parser/`, `ui/ocr/`, `ui/patterns/`) — hanya hapus file di dalamnya.
2. **`data/ocr/OcrModels.kt` dan `data/ocr/ReceiptOcrEngine.kt` tetap dipertahankan** — masih digunakan untuk OCR otomatis.
3. **`ui/components/ReceiptScanner.kt` tetap dipertahankan** — masih digunakan untuk auto-frame scan.
4. **Tidak ada perubahan database schema** — tabel `visits` dan `menu_items` tidak berubah. Version database kembali ke 1 (tidak perlu migration karena fitur sebelumnya belum pernah release; jika sudah pernah release, gunakan destructive fallback atau migration 2→1).
5. **Tidak ada library baru** — `ExposedDropdownMenuBox` sudah tersedia di Material 3 Compose (`androidx.compose.material3`).
