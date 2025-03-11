# Smart Medicine Reminder

Smart Medicine Reminder adalah aplikasi Android yang membantu pengguna mengelola pengingat minum obat dengan fitur CRUD (Create, Read, Update, Delete). Aplikasi ini menggunakan Room Database untuk menyimpan data pengingat dan AlarmManager untuk memberikan notifikasi tepat waktu.

## Fitur Utama

- **Tambah Pengingat**: Pengguna dapat menambahkan pengingat obat dengan waktu dan dosis yang sesuai.
- **Lihat Pengingat**: Daftar pengingat yang tersimpan dapat dilihat oleh pengguna.
- **Edit Pengingat**: Pengguna dapat memperbarui informasi pengingat obat.
- **Hapus Pengingat**: Pengingat yang tidak dibutuhkan dapat dihapus dari sistem.
- **Notifikasi**: Aplikasi mengirimkan pemberitahuan ketika waktu minum obat tiba.
- **Getar**: Menggetarkan Perangkat

## Teknologi yang Digunakan

- **Java**: Bahasa pemrograman utama aplikasi.
- **Android Room Database**: Untuk menyimpan dan mengelola data pengingat obat.
- **AlarmManager**: Untuk mengatur alarm dan mengirimkan notifikasi.
- **BroadcastReceiver**: Untuk menangani alarm dan menampilkan notifikasi.
- **Material Design**: Untuk tampilan UI yang modern dan user-friendly.

## Instalasi & Penggunaan

1. Clone repository ini:
   ```sh
   git clone https://github.com/Xnuvers007/smart_medicine_reminder.git
   ```
2. Buka proyek di Android Studio.
3. Pastikan SDK minimal yang didukung adalah **API 26 (Android 8.0 Oreo)**.
4. Jalankan aplikasi di emulator atau perangkat fisik.
5. Tambahkan pengingat dan coba fitur CRUD serta notifikasi.

## Struktur Proyek

```
app/src/main/java/dev/indra/smartmedicinereminder/
│-- AddEditMedicationActivity.java
│-- AlarmHelper.java
│-- BootReceiver.java
│-- DatabaseHelper.java
│-- MainActivity.java
│-- Medication.java
│-- MedicationAdapter.java
│-- ReminderReceiver.java
```

## Izin yang Digunakan

Aplikasi ini memerlukan beberapa izin untuk berfungsi dengan baik:

- **`POST_NOTIFICATIONS`**: Mengirim notifikasi ke pengguna.
- **`RECEIVE_BOOT_COMPLETED`**: Memastikan alarm tetap aktif setelah reboot.
- **`WAKE_LOCK`**: Memastikan alarm dapat membangunkan perangkat.
- **`SCHEDULE_EXACT_ALARM`**: Menjadwalkan alarm dengan presisi tinggi.
- **`VIBRATE`**: Menggetarkan Perangkat ke pengguna ketika notifikasi berhasil terkirim

## Kontribusi

Jika ingin berkontribusi pada proyek ini:

1. Fork repository ini.
2. Buat branch baru (`feature-branch`).
3. Lakukan perubahan dan commit.
4. Kirimkan pull request.

## Lisensi

Aplikasi ini dirilis di bawah lisensi **MIT**. Silakan lihat file `LICENSE` untuk detail lebih lanjut.

