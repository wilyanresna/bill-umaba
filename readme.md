Ini adalah folder aplikasi Bill Umaba.
Ini adalah aplikasi android.
Fitur utama aplikasi ini adalah mencatat pengeluaran dan review ketika kulineran.
Yang dicatat adalah:
1. Foto struk
2. nama tempat
3. alamat
4. pesanan kita:
    4.1 nama menu
    4.2 jumlah
    4.3 harga
    4.4 total
    4.5 rating untuk masing-masing menu
    4.6 catatan untuk masing-masing menu
5. grand total
6. rating untuk tempat
7. catatan untuk tempat

Gw mau aplikasi ini memiliki tampilan Material Design 3.

## Fitur Scan Struk

Aplikasi memiliki fitur **Scan Struk** yang terdiri dari 4 tahap:

1. **Auto-Frame** — Foto struk otomatis diluruskan & di-crop via ML Kit Document Scanner. Graceful fallback ke kamera biasa jika GMS tidak tersedia.
2. **OCR** — Teks struk diekstrak via ML Kit Text Recognition Latin (on-device). Hasil ditampilkan di OcrReviewScreen yang editable.
3. **Text Mapping** — OCR result diparsing otomatis ke data terstruktur (nama tempat, menu, total, pajak). 3 parser preset: Umum, Resto, Retail. Auto-detect dari keyword, user bisa override.
4. **Dynamic Pattern** — User bisa simpan pattern parsing per restoran via visual builder (tanpa regex). Pattern dipakai otomatis saat scan dari tempat yang sama.