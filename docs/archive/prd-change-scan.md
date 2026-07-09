# Fitur Scan Struk & Autocomplete — Bill Umaba

Dokumen ini mendefinisikan mekanisme scan struk: pengambilan foto struk, ekstraksi teks (OCR), dan autocomplete suggestion pada form input. Dokumen ini menjadi acuan tunggal implementasi fitur scan.

---

## 1. Latar Belakang

Nota/struk di Indonesia memiliki format yang sangat bervariasi — tidak ada standar baku tata letak, urutan informasi, maupun penamaan. Alih-alih mencoba memetakan hasil OCR secara otomatis ke form (yang terbukti tidak akurat), pendekatan yang diambil adalah menyediakan hasil OCR sebagai data suggestion (autocomplete) saat user mengetik di form. Dengan cara ini, user tetap memegang kendali penuh atas data yang masuk, namun tetap terbantu karena tidak perlu mengetik ulang teks yang sudah ada di struk.

---

## 2. Arsitektur Fitur Scan

Fitur Scan Struk terdiri dari **2 subsistem**:

### 2.1. Auto-Frame (Pengambilan Foto)

- Foto struk diambil menggunakan ML Kit Document Scanner, yang otomatis meluruskan (de-skew) dan memotong (crop) area struk dari foto miring.
- User dapat menyesuaikan hasil crop secara manual jika auto-frame kurang tepat.
- Jika perangkat tidak mendukung Google Play Services, fitur ini otomatis disembunyikan dan user diarahkan menggunakan kamera biasa.
- Selain scan, user juga dapat: mengambil foto baru via kamera, atau memilih dari galeri. Setelah memilih dari galeri, user ditawari dialog "Rapikan foto dengan auto-scan?" (default: Ya).
- Foto yang disimpan dikompresi menjadi maksimal 500 KB.

### 2.2. OCR + Autocomplete (Ekstraksi & Saran Teks)

- Setelah foto struk berhasil disimpan, sistem **otomatis** menjalankan OCR (ML Kit Text Recognition Latin, on-device, tanpa internet).
- User tidak perlu menekan tombol apa pun untuk memicu OCR.
- Hasil OCR berupa daftar baris teks digunakan sebagai sumber data autocomplete pada field-field tertentu di form.
- Tidak ada halaman perantara untuk review/edit hasil OCR.

---

## 3. Mekanisme Autocomplete

### 3.1. Field yang Mendapatkan Autocomplete

| Field | Sumber Suggestion |
|---|---|
| Nama Tempat | Semua baris teks hasil OCR |
| Alamat | Semua baris teks hasil OCR |
| Nama Menu | Semua baris teks hasil OCR |
| Jumlah | Hanya baris yang mengandung angka |
| Harga Satuan | Hanya baris yang mengandung angka |
| Override Total | Hanya baris yang mengandung angka |

### 3.2. Perilaku Pencarian Autocomplete

Pencarian menggunakan **prefix matching berbasis token, case-insensitive**. Setiap baris OCR dipecah menjadi token (dipisahkan oleh spasi), kemudian setiap token diuji apakah dimulai dengan teks yang diketik user.

**Contoh** — misal hasil OCR berisi:
```
Ayam bandung, Bebek Bandung, Daging Bandung, Antasri Berkah,
Aya Naon Nya, Bebek Bangor, Makanan Anak, Makanan Ayang
```

Token untuk baris "Makanan Ayang" adalah `["Makanan", "Ayang"]`.

| Input User | Suggestions yang Muncul | Keterangan |
|---|---|---|
| `A` | Ayam bandung, Antasri Berkah, Aya Naon Nya, Makanan Anak, Makanan Ayang | Match token awal `A` dan `Ayang` |
| `Ay` | Ayam bandung, Aya Naon Nya, Makanan Ayang | Match `Ayam`, `Aya`, `Ayang` |
| `Ban` | Ayam bandung, Bebek Bandung, Daging Bandung | Match token `bandung` dan `Bandung` |
| `bay` | (tidak ada) | Tidak ada token yang dimulai dengan `bay` |

### 3.3. Field Numerik

Untuk field Jumlah, Harga Satuan, dan Override Total:
- Hanya baris OCR yang **mengandung setidaknya satu digit angka (0-9)** yang ditampilkan sebagai suggestion.
- Baris seperti "Rp 25.000" atau "x2" tetap muncul karena mengandung angka.
- Baris seperti "Ayam Goreng" tidak muncul karena tidak mengandung angka.

### 3.4. Integrasi dengan Form

- User mengisi form secara manual, field per field.
- Saat user mulai mengetik di salah satu field yang didukung, dropdown suggestion otomatis muncul di bawah field tersebut.
- User dapat mengetap salah satu suggestion untuk mengisi field.
- User dapat mengabaikan suggestion dan melanjutkan mengetik secara manual.
- Tidak ada tombol "Pakai Hasil Scan" — autocomplete sepenuhnya menggantikan mekanisme pemetaan otomatis.

---

## 4. Alur Pengguna

```
Dashboard
   ↓ tap "+" Tambah Catatan
AddEditScreen
   ↓ tap area foto (kosong)
BottomSheet: "Scan / Foto / Galeri"
   ↓ pilih "Scan dengan auto-frame"
Document Scanner terbuka (UI bawaan Google)
   ↓ capture → auto-frame → user adjust jika perlu → confirm
Foto kembali ke AddEditScreen (sudah lurus & cropped)
   ↓ sistem otomatis menjalankan OCR di background
   ↓ (jika OCR gagal, user tetap bisa mengisi form manual)
Form AddEditScreen tetap aktif, user mengisi field:
   ↓ ketik di "Nama Tempat" → dropdown muncul, pilih atau lanjut ketik
   ↓ ketik di "Alamat" → dropdown muncul, pilih atau lanjut ketik
   ↓ ketik "Nama Menu" → dropdown muncul, pilih atau lanjut ketik
   ↓ ketik "Jumlah" / "Harga Satuan" → dropdown numerik muncul
   ↓ user melengkapi rating, catatan, dsb.
Simpan Catatan → kembali ke Dashboard
```

---

## 5. Penanganan Skenario Khusus

| Skenario | Perilaku |
|---|---|
| OCR gagal (foto buram, tidak ada teks) | Tidak ada autocomplete tersedia. User mengisi form manual seperti biasa. |
| OCR selesai tapi user mengganti foto | OCR dijalankan ulang untuk foto baru. Suggestion diperbarui otomatis. |
| User menghapus foto | Semua data autocomplete dikosongkan. |
| User sedang mengedit catatan lama (mode edit) | Foto existing tetap diproses OCR saat layar dibuka. Jika tidak ada foto, tidak ada autocomplete. |

---

## 6. Kriteria Keberhasilan

| Aspek | Kriteria |
|---|---|
| Auto-Frame | User bisa scan struk miring → hasil foto lurus & cropped. Perangkat tanpa GMS → fallback ke kamera biasa. |
| OCR Otomatis | Foto struk berhasil disimpan → OCR berjalan otomatis (2–5 detik). Tidak ada tombol yang perlu ditekan. |
| Autocomplete | Saat mengetik di field yang didukung, dropdown muncul dengan suggestion yang relevan sesuai aturan token prefix matching. User merasa terbantu mengisi form. |
| Numerik | Field Jumlah/Harga/Override Total hanya menampilkan baris yang mengandung angka. |
| Degradasi | OCR gagal → user tetap bisa mengisi form manual tanpa gangguan. |

---

## 7. Di Luar Scope

Fitur-fitur berikut **tidak** termasuk dalam pengembangan fitur scan:

- Text mapping / pemetaan otomatis hasil OCR ke field form.
- Pattern parsing kustom per restoran.
- Halaman review dan edit hasil OCR secara terpisah.
- Auto-detect tipe struk (Resto / Retail / Umum).
- Cloud sync pattern antar device.
- Sharing pattern komunitas.
- OCR untuk bahasa non-Latin.
- Deteksi otomatis jenis usaha dari logo di struk.
- Integrasi payment gateway / dompet digital.
- Multi-currency (fokus Rupiah).
- Pattern recommendation berdasarkan kemiripan struk.
