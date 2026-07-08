# Product Requirement Document (PRD) - Bill Umaba

## 1. Latar Belakang & Tujuan

**Bill Umaba** adalah aplikasi Android pencatat pengeluaran dan ulasan kuliner yang beroperasi secara mandiri di perangkat pengguna (offline-first). Aplikasi ini dirancang untuk mempermudah pecinta kuliner mencatat rincian pengeluaran makan, menyimpan foto struk pembayaran, serta memberikan ulasan dan rating pada menu makanan maupun tempat yang dikunjungi.

Tujuan utama dari proyek ini adalah menghadirkan aplikasi pencatatan yang intuitif, hemat ruang penyimpanan, bekerja secara offline, serta memiliki tampilan visual yang modern dan premium menggunakan standar **Material Design 3**.

---

## 2. Fitur & Alur Fungsional

### 2.1. Dashboard & Riwayat Kuliner (Dashboard Screen)
*   **Ringkasan Metrik**:
    *   **Total Pengeluaran Bulan Ini**: Akumulasi biaya kuliner di bulan berjalan, ditampilkan dalam format Rupiah (Rp).
    *   **Total Kunjungan**: Jumlah total kunjungan kuliner yang pernah dicatat oleh pengguna.
*   **Pencarian & Filter**: Kolam pencarian untuk menemukan riwayat kunjungan berdasarkan **nama tempat**, **alamat**, atau **nama menu** tertentu.
*   **Pengurutan (Sorting)**:
    *   Berdasarkan Tanggal (Terbaru / Terlama).
    *   Berdasarkan Grand Total (Termahal / Termurah).
    *   Berdasarkan Rating Tempat (Tertinggi / Terendah).
*   **Daftar Riwayat**: Kartu informasi kunjungan yang memuat:
    *   Thumbnail foto struk.
    *   Nama tempat & Alamat.
    *   Tanggal kunjungan.
    *   Rating tempat (angka desimal, misal: 4.5/5.0).
    *   Grand total pengeluaran.
    *   Tombol hapus cepat.
*   **Tombol Tambah (+)**: Floating Action Button untuk membuka form pencatatan baru.

### 2.2. Form Tambah & Edit Catatan (Add/Edit Screen)
*   **Lampiran Foto Struk**:
    *   **Scan Struk (Auto-Frame)**: Opsi utama untuk mengambil foto struk. Foto otomatis diluruskan & di-crop oleh ML Kit Document Scanner. User bisa adjust manual jika auto-frame gagal. Jika perangkat tidak mendukung Google Play Services, fitur ini otomatis disembunyikan dan user diarahkan ke kamera biasa.
    *   **Kamera Biasa**: Mengambil foto baru secara langsung dari Kamera perangkat.
    *   **Pilih dari Galeri**: Memilih foto yang sudah ada dari Galeri. Setelah dipilih, user ditawarkan dialog "Rapikan foto dengan auto-scan?" (default Yes).
    *   **Edit / Scan Ulang**: Tombol kecil di bawah preview foto untuk re-scan foto yang sudah ada.
    *   **Optimasi Penyimpanan**: Foto struk yang disimpan akan melalui proses kompresi otomatis agar ukuran file akhirnya **maksimal 500 KB** guna menghemat ruang penyimpanan internal ponsel pengguna.
    *   Kemampuan untuk menghapus atau mengganti foto struk yang dilampirkan.
*   **Ekstrak Teks Otomatis (OCR)**:
    *   Setelah foto struk terpasang, tersedia tombol **"Ekstrak Teks"**.
    *   Proses OCR berjalan **on-device** (2-5 detik) menggunakan ML Kit Text Recognition Latin — tanpa internet.
    *   User masuk ke **OcrReviewScreen** yang menampilkan daftar baris teks hasil pengenalan; setiap baris bisa diedit jika ada kesalahan baca.
    *   Sistem otomatis mendeteksi tipe struk (Resto / Retail / Umum) dan user bisa override pilihan via dropdown sebelum apply.
*   **Pemetaan Otomatis ke Form (Text Mapping)**:
    *   Setelah tap "Pakai Hasil Scan", form terisi otomatis:
        *   Nama tempat
        *   Daftar menu (nama, jumlah, harga, subtotal)
        *   Grand total
        *   Pajak, service, diskon (jika ada)
        *   Tanggal kunjungan (jika terdeteksi)
    *   User tetap bisa edit hasil mapping secara manual.
*   **Pattern Struk (Dynamic Pattern)**:
    *   Untuk tempat yang sering dikunjungi, user bisa menyimpan "pattern" parsing via UI visual (tanpa regex).
    *   Pattern dipakai otomatis saat scan dari tempat yang sama.
    *   Pola parsing: sumber nama restoran, template item menu (token QTY/NAMA/HARGA/SUBTOTAL), strategi grand total, skip keywords, regex opsional untuk tax/service/discount/date.
    *   Tombol "Test dengan foto" untuk preview parsing sebelum save.
*   **Informasi Tempat**:
    *   Input Nama Tempat (Wajib).
    *   Input Alamat Tempat (Opsional).
    *   **Rating Tempat**: Input rating berupa nilai desimal dari 1.0 hingga 5.0 (misalnya 4.2 atau 4.7) untuk penilaian yang lebih presisi.
    *   Catatan/Ulasan Tempat: Kolom ulasan bebas mengenai pengalaman umum di tempat tersebut.
*   **Daftar Menu yang Dipesan**:
    *   Formulir dinamis untuk menambah atau menghapus menu yang dipesan.
    *   Setiap baris menu memuat field:
        1.  **Nama Menu**
        2.  **Jumlah Porsi** (Quantity)
        3.  **Harga Satuan** (Price)
        4.  **Rating Menu**: Input rating berupa nilai desimal dari 1.0 hingga 5.0 (misalnya 4.8) khusus untuk menu tersebut.
        5.  **Catatan Menu**: Ulasan spesifik untuk menu tersebut (misal: "terlalu asin", "porsi sangat besar").
    *   **Subtotal Menu**: Dihitung dan ditampilkan otomatis berdasarkan perkalian `Jumlah * Harga Satuan`.
*   **Grand Total**:
    *   Secara otomatis terisi dengan penjumlahan dari seluruh subtotal menu.
    *   **Dapat diubah secara manual (Override)** untuk menyesuaikan biaya tambahan (seperti pajak restoran, biaya pelayanan, tips) atau potongan harga (diskon/voucher belanja).
*   **Validasi Pengisian**: Memastikan nama tempat terisi dan minimal ada satu menu yang dimasukkan sebelum data disimpan.

### 2.3. Tampilan Detail (Detail Screen)
*   **Tampilan Foto Struk**: Area foto struk yang dapat diklik untuk memperbesar gambar (zoom/fullscreen) agar teks struk mudah dibaca ulang.
*   **Informasi Tempat**: Menampilkan nama tempat, alamat, tanggal kunjungan, ulasan tempat, serta rating tempat dalam bentuk nilai desimal.
*   **Rincian Menu**: Daftar terstruktur dari menu-menu yang dipesan lengkap dengan kuantitas, harga, subtotal, catatan ulasan menu, dan rating desimal menu.
*   **Total Biaya**: Menampilkan angka Grand Total akhir kunjungan kuliner yang sudah diformat ke Rupiah.
*   **Aksi**: Tombol untuk melakukan penyuntingan (Edit) atau menghapus (Delete) catatan tersebut secara permanen.

### 2.4. Fitur Scan Struk
*   **Halaman Manajemen Pattern** (ikon ⚙ di Dashboard):
    *   Melihat daftar pattern yang sudah tersimpan (diurutkan berdasarkan terakhir dipakai).
    *   Tambah pattern baru, edit pattern existing, atau hapus pattern.
    *   Tiap pattern menampilkan jumlah pemakaian dan waktu terakhir dipakai.
*   **Builder Pattern** menggunakan UI visual (user **tidak perlu nulis regex**):
    *   Dropdown sumber nama restoran (baris pertama / dua baris pertama / auto-detect).
    *   Visual builder token QTY, NAMA, HARGA, SUBTOTAL (tap-to-insert ke template).
    *   Dropdown strategi grand total (baris dengan "Total" terbesar / baris terakhir / custom regex).
    *   Multi-select chips: skip keywords (subtotal, pajak, diskon, service, dll).
    *   Field opsional: regex untuk tax, service, discount, tanggal.
    *   **Test dengan foto** untuk preview parsing sebelum save (pilih dari galeri atau pakai foto dari kunjungan sebelumnya).
    *   Section **Advanced: lihat raw regex** untuk power user.
*   **Auto-Apply Pattern**: Saat scan struk dari tempat yang punya pattern tersimpan, pattern tersebut otomatis dipakai — bukan auto-detect default.

### 2.5. Alur Pengguna Scan Struk End-to-End
```
Dashboard
   ↓ tap "+" Tambah Catatan
AddEditScreen
   ↓ tap area foto (kosong)
BottomSheet: "Scan / Foto / Galeri"
   ↓ pilih "Scan dengan auto-frame"  (Tahap 1)
Document Scanner terbuka (UI bawaan)
   ↓ capture → auto-frame → user adjust kalau perlu → confirm
Foto kembali ke AddEditScreen (sudah lurus & cropped)
   ↓ tap tombol "Ekstrak Teks"  (Tahap 2)
OcrReviewScreen
   ↓ tampilkan daftar baris teks, user bisa edit
   ↓ label: "Terdeteksi sebagai: Resto / Retail / Umum"
   ↓ dropdown: ganti tipe parser kalau salah detect
   ↓ tap "Pakai Hasil Scan"  (Tahap 3)
Form ter-isi otomatis (nama tempat, menu, total, dll)
   ↓ user review & lengkapi (rating, catatan, dll)
   ↓ (opsional) tap "Simpan Pattern"  (Tahap 4)
PatternEditScreen: builder visual
   ↓ user configure via dropdown & chip (tanpa regex)
   ↓ "Test dengan foto" untuk preview
   ↓ simpan
Simpan Catatan → kembali ke Dashboard
```

---

## 3. Persyaratan Non-Fungsional & Antarmuka

*   **Panduan Desain**: Menggunakan standar **Material Design 3** dengan dukungan Light Mode (Tema Terang) dan Dark Mode (Tema Gelap).
*   **Skema Warna Dinamis**: Mendukung fitur warna dinamis (Material You) pada perangkat Android yang mendukung, menyesuaikan dengan wallpaper pengguna.
*   **Warna Fallback**: Menggunakan skema warna bertema hangat (orange/amber/warm culinary colors) sebagai warna default jika perangkat tidak mendukung warna dinamis.
*   **Ketersediaan Offline**: Semua data kunjungan dan ulasan harus dapat disimpan, diubah, dan dibaca tanpa memerlukan koneksi internet. Proses scan (Document Scanner), OCR, dan mapping juga berjalan **on-device** tanpa internet.
*   **Privasi**: Foto struk tetap disimpan lokal di perangkat (tidak ada upload). OCR tidak mengirim data ke server eksternal (on-device ML). Pattern parsing tidak meninggalkan perangkat.
*   **Efisiensi Penyimpanan**: Menjamin konsumsi memori dan ruang penyimpanan sekecil mungkin melalui kompresi gambar struk di bawah batas 500 KB.
*   **Performa Scan**: Scan + OCR + mapping total di bawah **10 detik** untuk struk normal. Pattern lookup & parser berjalan di background thread (tidak blocking UI).
*   **Graceful Degradation**: Jika perangkat tidak punya Google Play Services, fitur scan otomatis digantikan dengan kamera biasa.
*   **Bahasa**: Semua label UI dalam Bahasa Indonesia. Pattern disimpan dengan nama restoran dalam Bahasa Indonesia.

---

## 4. Pengembangan Fitur Scan Struk — 4 Tahap

Pengembangan fitur Scan Struk dilakukan secara inkremental dalam **4 tahap berurutan**, di mana setiap tahap dapat diuji secara independen sebelum lanjut ke tahap berikutnya.

| Tahap | Nama | Fungsi |
|---|---|---|
| **1** | Auto-Frame | Foto struk otomatis diluruskan & di-crop oleh ML Kit Document Scanner. User bisa adjust manual jika auto-frame gagal. GMS unavailable → fallback ke kamera biasa. |
| **2** | OCR (Extract Text) | Mengenali teks dari foto struk yang sudah di-frame, menghasilkan daftar baris teks yang bisa diedit. |
| **3** | Text Mapping | Mengubah hasil OCR menjadi data terstruktur: nama tempat, daftar menu, total, pajak, dll. 3 parser preset (Umum / Resto / Retail) + auto-detect dari keyword + override manual. |
| **4** | Pattern Dinamis | Setiap tempat bisa punya "pola" parsing sendiri yang disimpan di database lokal. User buat pattern via UI visual (tanpa nulis regex). Pattern dipakai otomatis saat scan dari tempat yang sama. |

### 4.1. Kriteria Sukses per Tahap

| Tahap | Kriteria Sukses |
|---|---|
| **1** Auto-Frame | User bisa scan struk miring → hasil foto lurus & cropped. GMS unavailable → graceful fallback. Gallery flow dengan dialog rapikan → jalan. |
| **2** OCR | Foto struk → list baris teks editable, akurasi cukup untuk dibaca manusia. |
| **3** Mapping | 3 tipe parser (Resto, Retail, Umum) auto-detect dari keyword. User bisa override. Form terisi benar untuk 80%+ struk umum. |
| **4** Pattern | User bisa buat pattern via UI visual (tanpa regex). Test pattern jalan. Pattern dipakai otomatis saat scan dari tempat yang sama. DB migration v1→v2 preserve data lama. |

---

## 5. Di luar Scope (Untuk Iterasi Berikutnya)

Item-item ini **tidak** termasuk dalam pengembangan awal fitur Scan Struk:

- Cloud sync pattern antar device.
- Sharing pattern komunitas (download pattern dari internet).
- OCR untuk bahasa non-Latin (China, Jepang, Korea, Aksara Jawa, dll).
- Deteksi otomatis jenis usaha dari logo di struk.
- Integrasi payment gateway / dompet digital.
- Multi-currency (saat ini fokus Rupiah).
- Pattern recommendation berdasarkan kemiripan struk.

---

## 6. Ringkasan Perubahan App Existing untuk Fitur Scan Struk

| Komponen | Status | Catatan |
|---|---|---|
| `PhotoPicker` | **Modifikasi** | Dari 2 tombol (Kamera/Galeri) jadi bottom sheet 3 opsi (Scan default). |
| `AddEditScreen` | **Modifikasi** | Tambah tombol "Ekstrak Teks", "Edit/Scan Ulang", integrasi OcrReviewScreen. |
| `AddEditViewModel` | **Modifikasi** | Tambah handler OCR, apply parsed receipt, save pattern. |
| `AddEditUiState` | **Modifikasi** | Tambah field scan/OCR/pattern. |
| Database | **Modifikasi** | Tambah tabel `receipt_patterns`, DB version 1→2 (proper migration preserve data). |
| Navigation | **Modifikasi** | Tambah 3 routes: `ocr_review`, `patterns`, `patterns/edit`. |
| Dashboard | **Modifikasi** | Tambah ikon ⚙ di TopAppBar → masuk ke Pattern Management. |

Tidak ada perubahan pada:
- Skema warna & tema (tetap M3 warm fallback).
- Struktur data `visits` dan `menu_items` (tetap sama).
- Format foto (tetap JPEG <500KB).
- Dependencies utama (Room, Hilt, Compose, Coil) — hanya tambah ML Kit libraries untuk fitur Scan Struk.
