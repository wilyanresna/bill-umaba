# Task Checklist: Bill Umaba (Offline-First Culinary Review & Expense Tracker)

Berikut adalah daftar tugas (checklist) yang dibagi berdasarkan tahapan implementasi. Anda dapat menandai `[ ]` menjadi `[x]` saat tugas selesai dilakukan.

---

## 📅 Tahap 1: Setup Proyek & Konfigurasi Dependensi
*Fokus: Mempersiapkan environment dan mendaftarkan pustaka yang dibutuhkan.*

- [x] **1.1. Konfigurasi `libs.versions.toml`**
  - [x] Tambahkan versi dan definisi pustaka untuk **Room Database** (v2.6.1).
  - [x] Tambahkan versi dan definisi pustaka untuk **Dagger Hilt** (v2.51.1) dan Hilt Navigation Compose (v1.2.0).
  - [x] Tambahkan versi dan definisi pustaka untuk **Compose Navigation** (v2.8.7).
- [x] **1.2. Konfigurasi Build Gradle**
  - [x] Daftarkan plugin Dagger Hilt di `build.gradle.kts` tingkat proyek dan modul `:app`.
  - [x] Daftarkan plugin Kotlin Kapt / KSP untuk kompilasi Room & Hilt.
  - [x] Tambahkan dependensi Room, Hilt, dan Navigation Compose ke dalam file `app/build.gradle.kts`.
- [x] **1.3. Setup Struktur Direktori (Packages)**
  - [x] Buat package `data/database/dao` dan `data/database/entities`.
  - [x] Buat package `data/repository`.
  - [x] Buat package `data/storage`.
  - [x] Buat package `di`.
  - [x] Buat package `ui/navigation`, `ui/components`, `ui/theme`, `ui/dashboard`, `ui/addedit`, dan `ui/detail`.

---

## 🗄️ Tahap 2: Lapisan Data (Data Layer) & Room Database
*Fokus: Mengimplementasikan persistensi data lokal, kompresi gambar, dan manajemen penyimpanan berkas.*

- [x] **2.1. Definisikan Entitas Database**
  - [x] Buat `VisitEntity.kt` untuk menyimpan info kunjungan restoran (nama, alamat, rating desimal, tanggal epoch, path foto struk, grand total).
  - [x] Buat `MenuItemEntity.kt` untuk menyimpan rincian item hidangan (nama, kuantitas, harga, rating desimal, catatan).
- [x] **2.2. Definisikan Interface DAO**
  - [x] Buat `VisitDao.kt` untuk operasi CRUD data kunjungan (mendukung query sorting & filter pencarian).
  - [x] Buat `MenuDao.kt` untuk operasi CRUD item menu hidangan.
- [x] **2.3. Implementasi AppDatabase**
  - [x] Buat class `AppDatabase.kt` yang menginisialisasi Room DB dengan tabel `visits` dan `menu_items`.
  - [x] Hubungkan kedua entitas dengan foreign key dan cascade delete.
- [x] **2.4. Implementasi Manajemen File & Kompresi Gambar**
  - [x] Buat class `ImageCompressor.kt` untuk meresize gambar ke max width/height 1920px dan mengompresinya ke format JPEG secara iteratif hingga ukurannya **< 500 KB**.
  - [x] Buat class `StorageManager.kt` untuk menangani penyimpanan foto struk ke direktori internal aplikasi (`Context.filesDir`).
- [x] **2.5. Implementasi Repository Pattern**
  - [x] Buat interface `CulinaryRepository.kt` sebagai abstraksi data.
  - [x] Buat implementasi `CulinaryRepositoryImpl.kt` yang mengintegrasikan Room DB, `StorageManager`, dan `ImageCompressor` dalam satu transaksi (`withTransaction`).

---

## 💉 Tahap 3: Dependency Injection (DI) dengan Hilt
*Fokus: Mengatur penyediaan instansi database, DAO, dan repository secara otomatis.*

- [ ] **3.1. Buat Application Class**
  - [ ] Buat `BillUmabaApplication.kt` yang mewarisi `Application` dan beranotasi `@HiltAndroidApp`.
  - [ ] Daftarkan class ini di dalam `AndroidManifest.xml`.
- [ ] **3.2. Setup DI Modules**
  - [ ] Buat `DatabaseModule.kt` untuk menyediakan instansi `AppDatabase`, `VisitDao`, `MenuDao`, dan helper storage.
  - [ ] Buat `RepositoryModule.kt` untuk melakukan binding interface `CulinaryRepository` ke `CulinaryRepositoryImpl`.

---

## 🎨 Tahap 4: Desain Sistem & Pondasi UI (Theme & Navigation)
*Fokus: Menentukan palet warna, navigasi antar halaman, dan komponen global.*

- [ ] **4.1. Setup Tema M3 (Material Design 3)**
  - [ ] Update `ui/theme/Color.kt` untuk mendefinisikan warna fallback bertema kuliner hangat (amber/orange/terracotta).
  - [ ] Update `ui/theme/Theme.kt` untuk mendukung **Dynamic Color** (Android 12+) dan fallback ke skema warna hangat.
  - [ ] Update `ui/theme/Type.kt` untuk tipografi modern (opsional: Outfit/Inter).
- [ ] **4.2. Setup Navigasi**
  - [ ] Buat file `ui/navigation/Screen.kt` yang mendefinisikan rute halaman (`Dashboard`, `AddEdit`, `Detail`).
  - [ ] Buat `ui/navigation/AppNavigation.kt` dengan `NavHost` untuk mengelola perpindahan layar dan passing argument ID kunjungan.
- [ ] **4.3. Komponen UI Global**
  - [ ] Buat `ui/components/StarRating.kt` untuk input dan tampilan rating bintang berbasis angka desimal (1.0 - 5.0).
  - [ ] Buat `ui/components/PhotoPicker.kt` untuk memicu kamera/galeri bawaan sistem Android.

---

## 📊 Tahap 5: Fitur Dashboard & Riwayat Kuliner (Dashboard Screen)
*Fokus: Menampilkan metrik keuangan dan daftar histori kuliner.*

- [ ] **5.1. Setup State & ViewModel**
  - [ ] Buat `DashboardUiState.kt` untuk menampung data metrik pengeluaran bulanan, total kunjungan, query pencarian, jenis sorting, dan list kunjungan.
  - [ ] Buat `DashboardViewModel.kt` untuk mengambil data secara reaktif (`Flow`) dari repository.
- [ ] **5.2. Logika Perhitungan Metrik & Filter**
  - [ ] Implementasikan kalkulasi total pengeluaran bulan berjalan dalam Rupiah (Rp).
  - [ ] Implementasikan pencarian berdasarkan nama tempat, alamat, atau nama menu.
  - [ ] Implementasikan logika pengurutan (Sorting) berdasarkan Tanggal (Terbaru/Terlama), Grand Total (Termahal/Termurah), dan Rating Tempat (Tertinggi/Terendah).
- [ ] **5.3. Pembuatan UI Dashboard**
  - [ ] Buat layout `DashboardScreen.kt` dengan header ringkasan metrik (Total Pengeluaran & Kunjungan).
  - [ ] Buat list kunjungan menggunakan kartu informasi (termasuk thumbnail struk terkompresi, rating desimal, grand total, dan tombol hapus cepat).
  - [ ] Tambahkan Floating Action Button (FAB) untuk navigasi ke form tambah catatan.

---

## 📝 Tahap 6: Fitur Tambah & Edit Catatan (Add/Edit Screen)
*Fokus: Mengatur pengisian data kunjungan, menu yang dipesan, dan pengambilan foto struk.*

- [ ] **6.1. Setup State & ViewModel**
  - [ ] Buat `AddEditUiState.kt` untuk melacak input form (foto struk, nama tempat, alamat, rating, ulasan tempat, daftar menu hidangan, dan status validasi).
  - [ ] Buat `AddEditViewModel.kt` yang menangani inisialisasi data (jika mode edit), validasi form, dan proses simpan.
- [ ] **6.2. Formulir Dinamis Menu Hidangan**
  - [ ] Implementasikan UI baris menu hidangan yang bisa ditambah dan dihapus secara dinamis.
  - [ ] Implementasikan kalkulasi subtotal menu secara realtime (`Quantity * Price`).
- [ ] **6.3. Override Grand Total**
  - [ ] Hitung akumulasi subtotal menu secara otomatis sebagai default `Grand Total`.
  - [ ] Berikan opsi input field untuk me-override Grand Total secara manual jika ada pajak, tips, atau diskon.
- [ ] **6.4. Validasi Form & Proses Simpan**
  - [ ] Terapkan validasi: Nama tempat wajib diisi dan minimal ada 1 menu yang dipesan.
  - [ ] Jalankan kompresi foto struk (< 500 KB) secara asinkron di ViewModel menggunakan `Dispatchers.Default` sebelum disimpan.
  - [ ] Panggil repository untuk menyimpan data kunjungan beserta relasi menunya ke database.

---

## 🔍 Tahap 7: Fitur Tampilan Detail (Detail Screen)
*Fokus: Menampilkan detail riwayat kuliner secara terperinci.*

- [ ] **7.1. Setup State & ViewModel**
  - [ ] Buat `DetailUiState.kt` untuk menyimpan data detail satu kunjungan berdasarkan ID.
  - [ ] Buat `DetailViewModel.kt` untuk meload data kunjungan beserta relasi menu-menunya.
- [ ] **7.2. Tampilan Detail & Zoom Struk**
  - [ ] Buat layout detail yang menampilkan foto struk di bagian atas.
  - [ ] Buat interaksi klik foto struk untuk membuka mode pembesaran (zoom/fullscreen).
  - [ ] Tampilkan informasi tempat lengkap dengan alamat, ulasan, dan rating bintang desimal.
- [ ] **7.3. Rincian Tabel Menu & Tombol Aksi**
  - [ ] Tampilkan list menu yang dipesan dalam bentuk tabel terstruktur (kuantitas, harga, subtotal, rating menu, dan catatan menu).
  - [ ] Tampilkan Grand Total akhir yang terformat ke mata uang Rupiah.
  - [ ] Tambahkan tombol Edit (navigasi ke `AddEditScreen`) dan tombol Hapus (konfirmasi hapus permanen dari DB dan disk).

---

## 🧪 Tahap 8: Pengujian & Refinement Akhir
*Fokus: Memastikan keandalan aplikasi secara keseluruhan.*

- [ ] **8.1. Pengujian Ketersediaan Offline (Offline-First)**
  - [ ] Jalankan aplikasi dalam Mode Pesawat (Airplane Mode) dan pastikan semua data CRUD bekerja dengan lancar.
- [ ] **8.2. Verifikasi Batas Ukuran Foto Struk**
  - [ ] Ambil gambar struk berukuran besar menggunakan kamera, lalu verifikasi ukuran file output kompresi di penyimpanan internal emulator/device (harus berada di bawah 500 KB).
- [ ] **8.3. Pengujian Light & Dark Theme**
  - [ ] Ubah tema sistem perangkat Android Anda dan verifikasi transisi warna pada aplikasi tetap nyaman dibaca dan elegan.
- [ ] **8.4. Validasi Relasi Database**
  - [ ] Verifikasi bahwa menghapus kunjungan (visit) akan otomatis menghapus menu hidangan terkait (`ON DELETE CASCADE`) dan file gambar struk fisik dari penyimpanan internal.
