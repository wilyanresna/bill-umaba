# Product Requirement Document (PRD) - Bill Umaba

## 1. Latar Belakang & Tujuan

**Bill Umaba** adalah aplikasi Android pencatat pengeluaran dan ulasan kuliner yang beroperasi secara mandiri di perangkat pengguna (offline-first). Aplikasi ini dirancang untuk mempermudah pecinta kuliner mencatat rincian pengeluaran makan, menyimpan foto struk pembayaran, serta memberikan ulasan dan rating pada menu makanan maupun tempat yang dikunjungi.

Tujuan utama dari proyek ini adalah menghadirkan aplikasi pencatatan yang intuitif, hemat ruang penyimpanan, bekerja secara offline, serta memiliki tampilan visual yang modern dan premium menggunakan standar **Material Design 3**.

---

## 2. Fitur & Alur Fungsional

### 2.1. Dashboard & Riwayat Kuliner (Dashboard Screen) — **IMPLEMENTED**

- **Ringkasan Metrik**:
    - **Total Pengeluaran Bulan Ini**: Akumulasi biaya kuliner di bulan berjalan, ditampilkan dalam format Rupiah (Rp).
    - **Total Kunjungan**: Jumlah total kunjungan kuliner yang pernah dicatat oleh pengguna.
- **Pencarian & Filter**: Kolom pencarian untuk menemukan riwayat kunjungan berdasarkan **nama tempat**, **alamat**, atau **nama menu** tertentu.
- **Pengurutan (Sorting)**:
    - Berdasarkan Tanggal (Terbaru / Terlama).
    - Berdasarkan Grand Total (Termahal / Termurah).
    - Berdasarkan Rating Tempat (Tertinggi / Terendah).
- **Daftar Riwayat**: Kartu informasi kunjungan yang memuat:
    - Thumbnail foto struk.
    - Nama tempat & Alamat.
    - Tanggal kunjungan.
    - Rating tempat (angka desimal, misal: 4.5/5.0).
    - Grand total pengeluaran.
    - Tombol hapus cepat.
- **Tombol Tambah (+)**: Floating Action Button untuk membuka form pencatatan baru.

### 2.2. Form Tambah & Edit Catatan (Add/Edit Screen) — **IMPLEMENTED**

- **Lampiran Foto Struk**:
    - **Scan Struk (Auto-Frame)**: Opsi utama untuk mengambil foto struk. Foto otomatis diluruskan & di-crop oleh ML Kit Document Scanner. User bisa adjust manual jika auto-frame gagal. Jika perangkat tidak mendukung Google Play Services, fitur ini otomatis disembunyikan dan user diarahkan ke kamera biasa.
    - **Kamera Biasa**: Mengambil foto baru secara langsung dari Kamera perangkat.
    - **Pilih dari Galeri**: Memilih foto yang sudah ada dari Galeri. Setelah dipilih, user ditawarkan dialog "Rapikan foto dengan auto-scan?" (default Yes).
    - **Edit / Scan Ulang**: Tombol kecil di bawah preview foto untuk re-scan foto yang sudah ada.
    - **Optimasi Penyimpanan**: Foto struk yang disimpan akan melalui proses kompresi otomatis agar ukuran file akhirnya **maksimal 500 KB** guna menghemat ruang penyimpanan internal ponsel pengguna.
    - Kemampuan untuk menghapus atau mengganti foto struk yang dilampirkan.
- **Ekstrak Teks Otomatis (OCR)**:
    - Setelah foto struk terpasang (dari scan, kamera, atau galeri), proses OCR berjalan **otomatis** di background.
    - Proses OCR berjalan **on-device** (2-5 detik) menggunakan ML Kit Text Recognition Latin — tanpa internet.
    - Indikator loading "Mengekstrak teks..." ditampilkan selama proses OCR.
    - Hasil OCR berupa daftar baris teks disimpan di state ViewModel dan muncul sebagai **token-based autocomplete suggestions** di setiap field form (nama tempat, alamat, nama menu, harga, grand total). User mengetik di field, suggestions dari hasil OCR otomatis muncul sebagai popup.
- **Informasi Tempat**:
    - Input Nama Tempat (Wajib).
    - Input Alamat Tempat (Opsional).
    - **Rating Tempat**: Input rating berupa nilai desimal dari 1.0 hingga 5.0 (misalnya 4.2 atau 4.7) untuk penilaian yang lebih presisi.
    - Catatan/Ulasan Tempat: Kolom ulasan bebas mengenai pengalaman umum di tempat tersebut.
- **Daftar Menu yang Dipesan**:
    - Formulir dinamis untuk menambah atau menghapus menu yang dipesan.
    - Setiap baris menu memuat field:
        1. **Nama Menu**
        2. **Jumlah Porsi** (Quantity)
        3. **Harga Satuan** (Price)
        4. **Rating Menu**: Input rating berupa nilai desimal dari 1.0 hingga 5.0 (misalnya 4.8) khusus untuk menu tersebut.
        5. **Catatan Menu**: Ulasan spesifik untuk menu tersebut (misal: "terlalu asin", "porsi sangat besar").
    - **Subtotal Menu**: Dihitung dan ditampilkan otomatis berdasarkan perkalian `Jumlah * Harga Satuan`.
- **Grand Total**:
    - Secara otomatis terisi dengan penjumlahan dari seluruh subtotal menu.
    - **Dapat diubah secara manual (Override)** untuk menyesuaikan biaya tambahan (seperti pajak restoran, biaya pelayanan, tips) atau potongan harga (diskon/voucher belanja).
- **Validasi Pengisian**: Memastikan nama tempat terisi dan minimal ada satu menu yang dimasukkan sebelum data disimpan.

### 2.3. Tampilan Detail (Detail Screen) — **IMPLEMENTED**

- **Tampilan Foto Struk**: Area foto struk yang dapat diklik untuk memperbesar gambar (zoom/fullscreen dialog) agar teks struk mudah dibaca ulang.
- **Informasi Tempat**: Menampilkan nama tempat, alamat, tanggal kunjungan, ulasan tempat, serta rating tempat dalam bentuk nilai desimal.
- **Rincian Menu**: Daftar terstruktur dari menu-menu yang dipesan lengkap dengan kuantitas, harga, subtotal, catatan ulasan menu, dan rating desimal menu.
- **Total Biaya**: Menampilkan angka Grand Total akhir kunjungan kuliner yang sudah diformat ke Rupiah.
- **Aksi**: Tombol untuk melakukan penyuntingan (Edit) atau menghapus (Delete) catatan tersebut secara permanen.

### 2.4. Fitur Scan Struk — **IMPLEMENTED (Tahap 1-2)**

Fitur scan struk diimplementasikan dalam 2 tahap sejauh ini:

| Tahap | Nama | Status | Fungsi |
|---|---|---|---|
| **1** | Auto-Frame | **IMPLEMENTED** | Foto struk otomatis diluruskan & di-crop oleh ML Kit Document Scanner. User bisa adjust manual jika auto-frame gagal. GMS unavailable → fallback ke kamera biasa. |
| **2** | OCR (Extract Text) | **IMPLEMENTED** | Mengenali teks dari foto struk yang sudah di-frame, menghasilkan daftar baris teks. Hasil OCR muncul sebagai autocomplete suggestions di form field (nama tempat, alamat, menu, harga, grand total) — user tinggal mengetik, suggestion dari OCR akan muncul. |

### 2.5. Alur Pengguna Scan Struk (Current)

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
   ↓ proses kompresi otomatis (<500 KB)
   ↓ OCR otomatis berjalan (loading "Mengekstrak teks...")  (Tahap 2)
Form siap diisi — user mengetik di field
   ↓ suggestion dari hasil OCR muncul sebagai autocomplete popup
User review & lengkapi (rating, catatan, dll)
Simpan Catatan → kembali ke Dashboard
```

---

## 3. Pengembangan Fitur Mendatang (Planned — Di Luar Scope Saat Ini)

Fitur-fitur berikut sudah direncanakan dalam PRD awal namun **belum diimplementasikan**:

| Tahap | Nama | Fungsi |
|---|---|---|
| **3** | Text Mapping | Mengubah hasil OCR menjadi data terstruktur: nama tempat, daftar menu, total, pajak, dll. 3 parser preset (Umum / Resto / Retail) + auto-detect dari keyword + override manual. |
| **4** | Pattern Dinamis | Setiap tempat bisa punya "pola" parsing sendiri yang disimpan di database lokal. User buat pattern via UI visual (tanpa nulis regex). Pattern dipakai otomatis saat scan dari tempat yang sama. |

Detail fitur yang direncanakan untuk tahap 3-4:

### Tahap 3 — Text Mapping (Planned)
- **OcrReviewScreen**: Halaman review hasil OCR terpisah dengan daftar baris teks editable dan dropdown pemilihan tipe parser.
- **3 Parser Preset**: `GeneralReceiptParser`, `RestaurantReceiptParser`, `RetailThermalParser`.
- **Auto-detect**: `ReceiptParserFactory` mendeteksi tipe struk dari keyword.
- Form terisi otomatis setelah tap "Pakai Hasil Scan".

### Tahap 4 — Pattern Dinamis (Planned)
- **Halaman Manajemen Pattern** (ikon ⚙ di Dashboard):
    - Melihat daftar pattern yang sudah tersimpan.
    - Tambah pattern baru, edit pattern existing, atau hapus pattern.
- **Builder Pattern** menggunakan UI visual (user **tidak perlu nulis regex**):
    - Dropdown sumber nama restoran.
    - Visual builder token QTY, NAMA, HARGA, SUBTOTAL (tap-to-insert ke template).
    - Dropdown strategi grand total.
    - Multi-select chips: skip keywords.
    - Field opsional: regex untuk tax, service, discount, tanggal.
    - **Test dengan foto** untuk preview parsing sebelum save.
- **Auto-Apply Pattern**: Saat scan struk dari tempat yang punya pattern tersimpan, pattern tersebut otomatis dipakai.
- **Database**: Tambah tabel `receipt_patterns`, DB migration v1→v2.

---

## 4. Persyaratan Non-Fungsional & Antarmuka

- **Panduan Desain**: Menggunakan standar **Material Design 3** dengan dukungan Light Mode (Tema Terang) dan Dark Mode (Tema Gelap).
- **Skema Warna Dinamis**: Mendukung fitur warna dinamis (Material You) pada perangkat Android yang mendukung, menyesuaikan dengan wallpaper pengguna.
- **Warna Fallback**: Menggunakan skema warna bertema hangat (orange/amber/warm culinary colors) sebagai warna default jika perangkat tidak mendukung warna dinamis.
- **Ketersediaan Offline**: Semua data kunjungan dan ulasan harus dapat disimpan, diubah, dan dibaca tanpa memerlukan koneksi internet. Proses scan (Document Scanner), OCR, dan mapping juga berjalan **on-device** tanpa internet.
- **Privasi**: Foto struk tetap disimpan lokal di perangkat (tidak ada upload). OCR tidak mengirim data ke server eksternal (on-device ML). Pattern parsing tidak meninggalkan perangkat.
- **Efisiensi Penyimpanan**: Menjamin konsumsi memori dan ruang penyimpanan sekecil mungkin melalui kompresi gambar struk di bawah batas 500 KB.
- **Performa Scan**: Scan + compress + OCR total di bawah **10 detik** untuk struk normal.
- **Graceful Degradation**: Jika perangkat tidak punya Google Play Services, fitur scan otomatis digantikan dengan kamera biasa.
- **Bahasa**: Semua label UI dalam Bahasa Indonesia.

---

## 5. Di luar Scope (Untuk Iterasi Berikutnya)

Item-item ini **tidak** termasuk dalam pengembangan:

- Cloud sync pattern antar device.
- Sharing pattern komunitas (download pattern dari internet).
- OCR untuk bahasa non-Latin (China, Jepang, Korea, Aksara Jawa, dll).
- Deteksi otomatis jenis usaha dari logo di struk.
- Integrasi payment gateway / dompet digital.
- Multi-currency (saat ini fokus Rupiah).
- Pattern recommendation berdasarkan kemiripan struk.

---

## 6. Ringkasan Status Implementasi

| Komponen | Status |
|---|---|
| Dashboard (visit list, search, sort, metrics) | **IMPLEMENTED** |
| Add/Edit form (photo, scan, camera, gallery, GMS fallback) | **IMPLEMENTED** |
| OCR engine (ML Kit Text Recognition, on-device) | **IMPLEMENTED** |
| Inline OCR suggestions (token-based autocomplete) | **IMPLEMENTED** |
| Detail view (zoomable photo, edit/delete) | **IMPLEMENTED** |
| Image compression (500KB limit) | **IMPLEMENTED** |
| M3 theme (dynamic color, warm fallback, light/dark) | **IMPLEMENTED** |
| OcrReviewScreen (editable OCR review) | Planned — Tahap 3 |
| Receipt parsers (General, Restaurant, Retail) | Planned — Tahap 3 |
| ReceiptParserFactory (auto-detect) | Planned — Tahap 3 |
| Pattern management (list, builder, CRUD) | Planned — Tahap 4 |
| receipt_patterns table & DB v2 migration | Planned — Tahap 4 |
