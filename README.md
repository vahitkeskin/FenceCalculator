<p align="center">
  <img src="https://raw.githubusercontent.com/vahitkeskin/FenceCalculator/refs/heads/main/app/src/main/res/drawable/app_icon_professional.png" width="128" height="128" alt="App Icon">
</p>

# 🤺 Tel Çit Hesaplayıcı (Fence Calculator) 🏁

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=for-the-badge&logo=kotlin" alt="Kotlin">
  <img src="https://img.shields.io/badge/Android-15%20(SDK%2035)-3DDC84?style=for-the-badge&logo=android" alt="Android">
  <img src="https://img.shields.io/badge/Compose-Latest-4285F4?style=for-the-badge&logo=jetpackcompose" alt="Jetpack Compose">
  <img src="https://img.shields.io/badge/Architecture-MVVM-FFCA28?style=for-the-badge" alt="Architecture">
</p>

Profesyonel tel çit projeleri için geliştirilmiş, ileri düzey maliyet analizi, dinamik raporlama ve premium kullanıcı deneyimi sunan endüstriyel bir Android çözümüdür. Modern Android ekosisteminin en güncel kütüphaneleri ve mimari yaklaşımları ile inşa edilmiştir.

---

## 📸 Görsel Deneyim & Tasarım
> [!IMPORTANT]
> Uygulama, **Glassmorphism** estetiğiyle birleşen dinamik **MeshBackground** (Canvas tabanlı animasyonlar) kullanarak standart bir hesap makinesinden çok, premium bir dijital araç hissi verir.

*   **Dinamik Tema**: Karanlık/Aydınlık mod desteği ve uyumlu renk paletleri.
*   **Mikro Animasyonlar**: Lottie ve Compose animasyon API'ları ile akıcı geçişler.
*   **Responsive Layout**: Tüm ekran boyutları için optimize edilmiş esnek yapı.

---

## 🚀 İleri Düzey Özellikler

### 1. 🏗️ Akıllı Hesaplama ve Formülizasyon
*   **Hassas Metraj Analizi**: Toplam uzunluğa göre direk, boru, tel, beton ve yardımcı malzeme ihtiyaçlarını milimetrik hesaplar.
*   **Boy Boru Optimizasyonu**: 6 metrelik standart boru boylarını baz alarak fireyi minimize eden yuvarlama mantığı.
*   **Özel Kart Sistemi**: Kullanıcıların kendi işçilik veya ek malzeme kalemlerini oluşturabileceği, diğer değerlere bağımlı (Dependent) dinamik formül yapısı.

### 2. 📄 Kurumsal Raporlama & QR Entegrasyonu
*   **Profesyonel PDF**: Şirket bilgileri, müşteri detayları ve kalem kalem maliyet dökümü içeren markalı raporlar.
*   **IBAN & Ödeme QR**: IBAN bilgisini otomatik olarak QR koda dönüştürüp rapor içerisine veya profil sekmesine entegre eder.
*   **ML Kit QR Scanner**: Mevcut QR kodları tarayarak IBAN bilgilerini anında içeri aktarma.

### 3. 🛡️ Gelişmiş Navigasyon & UX
*   **Multi-Stack Navigation**: Sekmeler arası durum korumalı (State Preservation) ve geri tuşuyla tüm geçmişi (Unique Tab History) gezdirme mantığı.
*   **Debouce Protection**: Hızlı tıklamalarda hatalı navigasyonu önleyen tıklama debouncing mekanizması.
*   **App Exit Control**: Home ekranında akıllı çıkış yönetimi.

---

## 🛠️ Teknik Altyapı (Technical Stack)

Uygulama, profesyonel Android geliştirme standartlarına (Modern Android Development - MAD) tam uyumludur:

### ⚡ Core & UI
- **Language:** Kotlin 2.0.21 (Kotlin Symbol Processing - KSP desteğiyle)
- **UI:** Jetpack Compose (Declarative UI) ile Material 3 bileşenleri.
- **Image Loading:** Async Image Loading & Bitmap Processing.
- **Splash:** Android 12+ SplashScreen API entegrasyonu.

### 🏛️ Architecture & DI
- **Pattern:** Clean Architecture prensiplerine sadık MVVM (Model-View-ViewModel).
- **Dependency Injection:** **Hilt (Dagger)** - Uygulama genelinde modüler ve test edilebilir bağımlılık yönetimi.
- **State Management:** StateFlow ve SharedFlow ile reaktif veri akışları.

### 💾 Data Persistence & Logic
- **DataStore Preferences:** Kullanıcı ayarları ve persist veriler için Jetpack DataStore.
- **Coroutines & Flow:** Asenkron işlemler ve thread güvenliği.
- **Safe Navigation:** Tip güvenli NavHost ve custom back stack yönetimi.

### 🔌 External Integrations
- **ML Kit Barcode Scanning:** QR ve Barkod okuma yetenekleri.
- **ZXing:** QR kod üretimi ve yönetimi.
- **IText / PDF Generator:** Profesyonel PDF çıktıları üretimi.
- **AdMob:** Google Mobile Ads ile gelir modelleri ve reklam optimizasyonu.
- **Libphonenumber:** Uluslararası telefon numarası doğrulama ve formatlama.

---

## 📂 Paket Yapısı ve Organizasyon
```text
com.vahitkeskin.fencecalculator
├── di                # Hilt Modülleri (Dependency Injection)
├── ui
│   ├── components    # Yeniden kullanılabilir Premium UI bileşenleri
│   ├── screen        # Ekran Bazlı Composables (Home, Calculations, vb.)
│   ├── theme         # Material3 Tema, Renk ve Tipografi tanımları
│   └── viewmodel     # UI Business Logic (CalculatorViewModel)
└── util              # Yardımcı araçlar (PdfGen, QrGen, AdManager, DnsDetector)
```

---

## 📦 Kurulum & Build

### APK İndir
👉 **[FenceCalculator.apk](FenceCalculator.apk)**

### Geliştirici Modu (Build from Source)
1.  Depoyu klonlayın.
2.  `local.properties` içine AdMob ve gerekli key tanımlarını yapın.
3.  Android Studio Ladybug veya üstü ile projeyi açın.
4.  `./gradlew assembleDebug` komutuyla APK oluşturun.

---

## 🤝 İletişim & Katkıda Bulunma
Her türlü özellik isteği veya hata bildirimi için **Pull Request** açabilir veya **Issues** kısmını kullanabilirsiniz.

**Developer:** [Vahit Keskin](https://github.com/vahitkeskin)
**Copyright:** © 2024 Tel Çit Hesaplayıcı 🏁
