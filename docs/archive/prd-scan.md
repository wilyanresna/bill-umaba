# Product Requirement Document (PRD) — Fitur Scan Struk

> Dokumen ini adalah **add-on** dari `prd.md` utama. Berisi informasi produk (non-teknis) khusus untuk fitur Scan Struk yang akan diintegrasikan ke aplikasi Bill Umaba.

---

## 1. Latar Belakang

Saat ini pencatatan kunjungan kuliner di Bill Umaba dilakukan secara **manual** — user harus mengetik ulang nama tempat, daftar menu, jumlah, harga, dan total ke dalam form. Padahal, informasi tersebut sudah tersedia di **foto struk** yang dilampirkan.

Fitur **Scan Struk** bertujuan untuk:
- **Mempercepat** proses input data dengan mengekstraksi informasi dari struk secara otomatis.
- **Meningkatkan akurasi** dengan mengurangi human-error saat mengetik angka harga.
- **Meningkatkan engagement** karena proses pencatatan menjadi lebih "magic" dan menyenangkan.

---

## 2. Target Pengguna

Pengguna existing Bill Umaba yang ingin mencatat kunjungan kuliner dengan lebih cepat, terutama mereka yang sering makan di tempat yang sama (misal: resto langganan, kantor, atau retail yang sering dikunjungi).

---

## 3. Fitur & Alur Pengguna

### 3.1. Empat Tahap Pengembangan

Pengembangan fitur Scan Struk dilakukan dalam **4 tahap berurutan**, di mana setiap tahap dapat diuji secara independen sebelum lanjut ke tahap berikutnya.

| Tahap | Nama | Fungsi |
|---|---|---|
| **1** | Auto-Frame | Foto struk otomatis diluruskan & di-crop seperti hasil scanner. User bisa adjust manual jika auto-frame gagal. |
| **2** | OCR (Extract Text) | Mengenali teks dari foto struk yang sudah di-frame, menghasilkan daftar baris teks. |
| **3** | Text Mapping | Mengubah hasil OCR menjadi data terstruktur: nama tempat, daftar menu, total, pajak, dll. |
| **4** | Pattern Dinamis | Setiap tempat bisa punya "pola" parsing sendiri. User bisa buat pattern baru via UI visual (tanpa nulis regex). |

### 3.2. Alur Pengguna End-to-End

```
Dashboard
   ↓ tap "+" Tambah Catatan
AddEditScreen
   ↓ tap area foto (kosong)
BottomSheet: "Scan / Foto / Galeri"
   ↓ pilih "Scan dengan auto-frame"  [TAHAP 1]
Document Scanner terbuka (UI bawaan)
   ↓ capture → auto-frame → user adjust kalau perlu → confirm
Foto kembali ke AddEditScreen (sudah lurus & cropped)
   ↓ tap tombol "Ekstrak Teks"  [TAHAP 2]
OcrReviewScreen
   ↓ tampilkan daftar baris teks, user bisa edit
   ↓ label: "Terdeteksi sebagai: Resto / Retail / Umum"
   ↓ dropdown: ganti tipe parser kalau salah detect
   ↓ tap "Pakai Hasil Scan"  [TAHAP 3]
Form ter-isi otomatis (nama tempat, menu, total, dll)
   ↓ user review & lengkapi (rating, catatan, dll)
   ↓ (opsional) tap "Simpan Pattern"  [TAHAP 4]
PatternEditScreen: builder visual
   ↓ user configure via dropdown & chip (tanpa regex)
   ↓ "Test dengan foto" untuk preview
   ↓ simpan
Simpan Catatan → kembali ke Dashboard
```

### 3.3. Detail Tiap Tahap

#### Tahap 1 — Auto-Frame

- Saat area foto ditekan (kosong), user melihat bottom sheet dengan 3 opsi:
  - **Scan dengan auto-frame** (opsi default, paling atas)
  - Kamera biasa
  - Pilih dari Galeri
- **Scan dengan auto-frame** akan membuka UI Document Scanner bawaan, di mana:
  - Sistem otomatis mendeteksi tepi struk
  - Jika auto-detection berhasil, struk langsung di-frame & diluruskan
  - Jika tidak rapi, user bisa drag corner untuk adjust manual
- **Jika pilih dari Galeri**, setelah foto dipilih muncul dialog:
  - **"Rapikan foto dengan auto-scan?"** dengan default **Yes**
  - Yes → foto gallery dimasukkan ke Document Scanner untuk di-rapikan
  - No → foto gallery langsung dipakai apa adanya
- **Jika perangkat tidak mendukung** (tidak ada Google Play Services), fitur scan otomatis disembunyikan dan user diarahkan ke kamera biasa.
- Setelah ada foto, tersedia tombol **"Edit / Scan Ulang"** untuk re-scan foto yang sudah ada.

#### Tahap 2 — OCR

- Setelah foto struk ada, muncul tombol **"Ekstrak Teks"**.
- Proses OCR berjalan (sekitar 2-5 detik, on-device).
- User masuk ke **OcrReviewScreen** yang menampilkan:
  - Daftar baris teks hasil pengenalan
  - Setiap baris bisa diedit jika ada kesalahan baca
- Di bagian atas, ditampilkan label: **"✓ Terdeteksi sebagai: Resto / Retail / Umum"**.
- Tombol **"Lanjut"** untuk masuk ke tahap mapping.

#### Tahap 3 — Text Mapping

- Sistem otomatis mendeteksi tipe struk dari keyword:
  - **Resto**: ada "Subtotal" + "Pajak/PPN/Service"
  - **Retail**: ada "Tunai/Kembali/Kembalian" (struk kasir)
  - **Umum**: fallback untuk struk sederhana
- User bisa override pilihan tipe via dropdown kecil di OcrReviewScreen sebelum apply.
- Setelah tap **"Pakai Hasil Scan"**, form AddEditScreen terisi otomatis:
  - Nama tempat
  - Daftar menu (nama, jumlah, harga, subtotal)
  - Grand total
  - Pajak, service, diskon (jika ada)
  - Tanggal kunjungan (jika terdeteksi)
- User tetap bisa edit hasil mapping secara manual.

#### Tahap 4 — Pattern Dinamis

- Tersedia halaman **Manajemen Pattern** (ikon ⚙ di Dashboard).
- User bisa:
  - Melihat daftar pattern yang sudah tersimpan
  - Tambah pattern baru
  - Edit pattern existing
  - Hapus pattern
- Builder pattern menggunakan **UI visual** — user **tidak perlu nulis regex**:
  - Dropdown: sumber nama restoran (baris pertama / dua baris pertama / auto-detect)
  - Visual builder: token QTY, NAMA, HARGA, SUBTOTAL (drag & drop atau tap-to-insert)
  - Dropdown: strategi grand total (baris dengan "Total" terbesar / baris terakhir / custom)
  - Multi-select chips: skip keywords (subtotal, pajak, diskon, dll)
  - Field opsional: regex untuk tax, service, discount, tanggal
- Tersedia tombol **"Test dengan foto"** untuk preview parsing sebelum save:
  - Pilih foto dari galeri, **atau**
  - Pilih foto dari kunjungan sebelumnya (yang sudah pernah disimpan)
- Power user bisa membuka section **"Advanced: lihat raw regex"** untuk fine-tune regex yang di-generate otomatis.
- Saat scan struk dari tempat yang punya pattern tersimpan, pattern tersebut otomatis dipakai — bukan auto-detect default.

---

## 4. Persyaratan Non-Fungsional

### 4.1. Ketersediaan Offline (Offline-First)
- Semua proses (scan, OCR, mapping) berjalan **on-device** tanpa internet.
- Pattern disimpan lokal di database, tidak ada sync ke server.
- Jika perangkat tidak punya Google Play Services, fitur scan otomatis digantikan dengan kamera biasa (graceful degradation).

### 4.2. Privasi
- Foto struk tetap disimpan lokal di perangkat (tidak ada upload).
- OCR tidak mengirim data ke server eksternal (on-device ML).
- Pattern parsing tidak meninggalkan perangkat.

### 4.3. Performa
- Scan + OCR + mapping total di bawah **10 detik** untuk struk normal.
- File foto tetap di bawah **500 KB** (kompresi otomatis seperti fitur existing).
- Pattern lookup & parser berjalan di background thread (tidak blocking UI).

### 4.4. Akurasi
- Auto-frame Document Scanner: akurasi tinggi untuk struk di permukaan datar dengan pencahayaan cukup.
- OCR Latin: akurasi tinggi untuk struk Indonesia yang menggunakan huruf Latin.
- Mapping default: hasil bisa bervariasi; user selalu punya kesempatan untuk koreksi manual.

### 4.5. Material Design 3
- Semua UI baru mengikuti standar M3 (sudah digunakan di app existing).
- Bottom sheet, dialog, dropdown, chips — semua komponen M3.
- Dukungan Light & Dark Mode (otomatis dari theme existing).

### 4.6. Bahasa
- Semua label UI dalam Bahasa Indonesia (konsisten dengan app existing).
- Pattern disimpan dengan nama restaurant dalam Bahasa Indonesia.

---

## 5. Di luar Scope (Untuk Iterasi Berikutnya)

Item-item ini **tidak** termasuk dalam 4 tahap pengembangan awal:

- Cloud sync pattern antar device.
- Sharing pattern komunitas (download pattern dari internet).
- OCR untuk bahasa non-Latin (China, Jepang, Korea, Aksara Jawa, dll).
- Deteksi otomatis jenis usaha dari logo di struk.
- Integrasi payment gateway / dompet digital.
- Multi-currency (saat ini fokus Rupiah).
- Pattern recommendation berdasarkan kemiripan struk.

---

## 6. Kriteria Sukses per Tahap

| Tahap | Kriteria Sukses |
|---|---|
| **1** Auto-Frame | User bisa scan struk miring → hasil foto lurus & cropped. GMS unavailable → graceful fallback. Gallery flow dengan dialog rapikan → jalan. |
| **2** OCR | Foto struk → list baris teks editable, akurasi cukup untuk dibaca manusia. |
| **3** Mapping | 3 tipe parser (Resto, Retail, Umum) auto-detect dari keyword. User bisa override. Form terisi benar untuk 80%+ struk umum. |
| **4** Pattern | User bisa buat pattern via UI visual (tanpa regex). Test pattern jalan. Pattern dipakai otomatis saat scan dari tempat yang sama. DB migration v1→v2 preserve data lama. |

---

## 7. Ringkasan Perubahan terhadap App Existing

| Komponen | Status | Catatan |
|---|---|---|
| `PhotoPicker` | **Modifikasi** | Dari 2 tombol (Kamera/Galeri) jadi bottom sheet 3 opsi (Scan default) |
| `AddEditScreen` | **Modifikasi** | Tambah tombol "Ekstrak Teks", "Edit/Scan Ulang", "Simpan Pattern" |
| Database | **Modifikasi** | Tambah tabel `receipt_patterns`, DB version 1→2 (proper migration) |
| Navigation | **Modifikasi** | Tambah 3 routes: `ocr_review`, `patterns`, `patterns/edit` |
| Dashboard | **Modifikasi** | Tambah ikon ⚙ → masuk ke Pattern Management |

Tidak ada perubahan pada:
- Skema warna & tema (tetap M3 warm fallback)
- Struktur data `visits` dan `menu_items` (tetap sama)
- Format foto (tetap JPEG <500KB)
- Dependencies utama (Room, Hilt, Compose, Coil) — hanya tambah ML Kit libraries
