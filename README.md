# ShareTask
> Tulis deskripsi singkat tapi menarik tentang aplikasi Anda dalam satu atau dua kalimat. Jelaskan fungsi utama dan untuk siapa aplikasi ini dibuat.

<br>

<p align="center">
  <img src="[LINK_KE_SCREENSHOT_ATAU_GIF_DEMO]" alt="demo aplikasi" width="300">
</p>

---

## ✨ Fitur Utama
* **Otentikasi Google:** Pengguna dapat SignIn dan LogIn secara aman dan cepat menggunakan akun Google yang sudah ada.
* **Database Real-time:** Data disimpan dan disinkronkan secara instan menggunakan Firestore Database.
* **Penyimpanan File:** Pengguna dapat mengunggah dan mengelola file seperti documen atau foto yang disimpan di Supabase Storage.
* **[Fitur Kunci Lainnya]:** Jelaskan fitur utama lain dari aplikasi Anda.

---

## 🛠️ Teknologi & Arsitektur
* **Bahasa:** [Kotlin](https://kotlinlang.org/)
* **UI Toolkit:** [Android XML Views]
* **Arsitektur:** [MVVM (Model-View-ViewModel)]
* **Database:** [Cloud Firestore](https://firebase.google.com/docs/firestore)
* **Otentikasi:** [Firebase Authentication](https://firebase.google.com/docs/auth)
* **Penyimpanan File:** [Supabase Storage](https://supabase.com/docs/guides/storage)

---

## 🚀 Instalasi dan Konfigurasi
Ikuti langkah-langkah berikut untuk menjalankan proyek ini di lingkungan lokal Anda.

### Prasyarat
* [Android Studio](https://developer.android.com/studio) (versi terbaru direkomendasikan)
* Akun [Firebase](https://firebase.google.com/) dan [Supabase](https://supabase.com/)

### Langkah-langkah Konfigurasi
1.  **Clone repositori ini**
    ```bash
    git clone [https://github.com/](https://github.com/)[USERNAME_ANDA]/[NAMA_REPO_ANDA].git
    ```

2.  **Buka proyek di Android Studio**
    * Buka Android Studio.
    * Pilih `Open` dan arahkan ke direktori proyek yang baru saja Anda clone.

3.  **Konfigurasi Firebase**
    * Buka [Firebase Console](https://console.firebase.google.com/) dan buat proyek baru.
    * Tambahkan aplikasi Android ke proyek Firebase Anda. Ikuti petunjuk untuk mendaftarkan nama paket aplikasi Anda (`com.example.namaaplikasianda`).
    * Aktifkan layanan **Authentication (Google Sign-In)** dan **Cloud Firestore**.
    * Unduh file konfigurasi `google-services.json` dan letakkan di dalam direktori **`app/`** proyek Anda.

4.  **Konfigurasi Supabase**
    * Buka [Supabase Dashboard](https://app.supabase.com/) dan buat proyek baru.
    * Di dalam proyek Supabase Anda, buka bagian **Storage** dan buat *Bucket* baru (misalnya, `user-uploads`). Atur kebijakan aksesnya jika diperlukan.
    * Buka **Project Settings > API**. Salin **Project URL** dan **anon (public) key** Anda.
    * Masukkan kredensial Supabase ke dalam file `local.properties` di root proyek Anda. **Jangan masukkan kredensial ini langsung ke dalam kode atau `build.gradle`!**
        ```properties
        # local.properties (File ini seharusnya sudah ada di .gitignore)
        SUPABASE_URL="URL_PROYEK_SUPABASE_ANDA"
        SUPABASE_ANON_KEY="ANON_KEY_ANDA"
        ```
    * *Catatan: Anda perlu mengambil nilai ini di dalam kode Kotlin Anda, biasanya melalui `BuildConfig`.*

5.  **Build dan Jalankan Aplikasi**
    * Tunggu hingga Android Studio selesai melakukan sinkronisasi Gradle.
    * Klik tombol **Run 'app'** (▶️) untuk membangun dan menjalankan aplikasi di emulator atau perangkat fisik.

---

## 🤝 Kontribusi
Kontribusi sangat kami hargai! Jika Anda ingin membantu, silakan:
1.  **Fork** repositori ini.
2.  Buat **Branch** baru (`git checkout -b fitur/NamaFiturBaru`).
3.  **Commit** perubahan Anda (`git commit -m 'feat: Menambahkan fitur baru'`).
4.  **Push** ke Branch Anda (`git push origin fitur/NamaFiturBaru`).
5.  Buka **Pull Request**.

---

## 📄 Lisensi
Proyek ini dilisensikan di bawah [Lisensi MIT](LICENSE).
