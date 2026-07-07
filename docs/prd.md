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
    *   Dukungan untuk mengambil foto baru secara langsung dari Kamera perangkat.
    *   Dukungan untuk memilih foto yang sudah ada dari Galeri foto.
    *   **Optimasi Penyimpanan**: Foto struk yang disimpan akan melalui proses kompresi otomatis agar ukuran file akhirnya **maksimal 500 KB** guna menghemat ruang penyimpanan internal ponsel pengguna.
    *   Kemampuan untuk menghapus atau mengganti foto struk yang dilampirkan.
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

---

## 3. Persyaratan Non-Fungsional & Antarmuka

*   **Panduan Desain**: Menggunakan standar **Material Design 3** dengan dukungan Light Mode (Tema Terang) dan Dark Mode (Tema Gelap).
*   **Skema Warna Dinamis**: Mendukung fitur warna dinamis (Material You) pada perangkat Android yang mendukung, menyesuaikan dengan wallpaper pengguna.
*   **Warna Fallback**: Menggunakan skema warna bertema hangat (orange/amber/warm culinary colors) sebagai warna default jika perangkat tidak mendukung warna dinamis.
*   **Ketersediaan Offline**: Semua data kunjungan dan ulasan harus dapat disimpan, diubah, dan dibaca tanpa memerlukan koneksi internet.
*   **Efisiensi Penyimpanan**: Menjamin konsumsi memori dan ruang penyimpanan sekecil mungkin melalui kompresi gambar struk di bawah batas 500 KB.
