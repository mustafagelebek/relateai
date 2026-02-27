# Proje Geliştirme Planı ve Mimari Yapı

## 1. Uygulamanın Genel Yapısı (Compose Odaklı Mimari)

Uygulamanın kalbinde "Tek Yönlü Veri Akışı" (Unidirectional Data Flow) olmalı. WhatsApp verisi uygulamaya girdiği andan itibaren UI, sadece ViewModel'dan gelen durumu (State) dinlemelidir.

* **Veri Katmanı (Data Layer):** `ChatParser`, Intent ile gelen `.txt` dosyasını okuyup `List<ChatMessage>` modeline çeviren sınıftır. `GeminiRepository` ise temizlenmiş veriyi Vertex AI / Gemini API'ye gönderen ve dönen JSON yanıtını parse eden katmandır.
* **İş Mantığı Katmanı (Domain/ViewModel Layer):** `AnalyzerViewModel`, dosyanın okunması, temizlenmesi ve API'ye gönderilmesi süreçlerini yönetir. Ekranın o anki durumunu bir StateFlow (örneğin; `Loading`, `Success`, `Error`) olarak tutar.
* **Sunum Katmanı (UI Layer - Jetpack Compose):** Sadece `AnalyzerViewModel`'ı dinleyen ve duruma göre (State) arayüzü anında güncelleyen, Composable fonksiyonlardan oluşan modern görünüm katmanıdır.

---

## 2. Geliştirme Kilometre Taşları (Compose Milestones)

Projeyi Compose mantığına uygun olarak 4 ana faza ayırdım. Her fazın sonunda çalışan bir ekran elde edilecek.

### Milestone 1: Veri Yakalama ve Temel State Yönetimi
**Odak:** Uygulamanın dış dünyadan veri alabilmesi ve UI durumunu değiştirebilmesi.

**Aksiyon:** * `MainActivity` içinde Intent filtresini kur.
* `AnalyzerViewModel` içinde bir `UiState` (sealed interface) oluştur: `Idle` (Bekliyor), `Parsing` (Dosya Okunuyor), `ReadyToSend` (Göndermeye Hazır).
* Tek bir Composable ekran (`HomeScreen`) yaz. Bu ekran sadece bir "Dosya Bekleniyor..." metni göstersin ve dosya geldiğinde state değiştiği için anında "X satır mesaj bulundu, analize hazır" şekline dönüşsün.

**Başarı Kriteri:** WhatsApp'tan uygulamaya dosya atıldığında, Compose ekranının anında güncellenerek dosya boyutunu/satır sayısını gösterebilmesi.

### Milestone 2: Gemini Entegrasyonu ve Asenkron İşlemler (Coroutines)
**Odak:** Arka planda yapay zeka ile konuşurken ekranın kilitlenmesini engellemek.

**Aksiyon:** * ViewModel üzerinden `viewModelScope.launch` ile Gemini API'ye veriyi gönder.
* Bu sırada `UiState`'i `Analyzing` olarak güncelle.
* Compose tarafında `Analyzing` state'ini dinleyen bir yükleme ekranı (`LoadingScreen`) tasarla. Ortada dönen bir `CircularProgressIndicator` ve altında "İlişki dinamikleri çözümleniyor..." gibi dinamik değişen metinler olsun.
* Yanıt geldiğinde state'i `Success(val result: AnalysisResult)` olarak güncelle ve dönen ham metni ekrana bas.

**Başarı Kriteri:** API isteği atıldığında ekranın donmaması, yükleme animasyonunun akıcı çalışması ve yanıt geldiğinde UI'ın otomatik tetiklenip metni göstermesi.

### Milestone 3: Özel Composable'lar ile Görselleştirme (Dashboard)
**Odak:** Gemini'dan gelen JSON verisini sıkıcı bir metin olmaktan çıkarıp şık UI bileşenlerine dönüştürmek.

**Aksiyon:** * Yanıtı göstermek için tekrar kullanılabilir (reusable) Custom Composable'lar yaz.
* `ScoreCard(score: Int)`: İlişki sağlığı puanını renkli bir çember içinde gösteren bileşen (Canvas kullanılarak çizilebilir).
* `RedFlagItem(text: String)`: Yanında uyarı ikonu olan, kırmızı tonlarında tehlike sinyali kartları.
* `ActionPlanList(tasks: List<String>)`: Tik atılabilir tasarıma sahip tavsiyeler listesi.
* Tüm bunları alt alta dizen bir `DashboardScreen` oluştur.

**Başarı Kriteri:** Yapay zeka çıktısının, Material Design 3 standartlarında, modern ve okunabilir kartlar halinde ekranda listelenmesi.

### Milestone 4: Navigasyon ve Animasyonlu Akış (Polish)
**Odak:** Kullanıcı deneyimini (UX) profesyonel bir seviyeye taşımak.

**Aksiyon:** * Navigation Compose kütüphanesini entegre et.
* Kullanıcıyı karşılayan bir `OnboardingScreen` (WhatsApp'tan dosyanın nasıl aktarılacağını gösteren rehber) ekle.
* Ekran geçişleri arasına `AnimatedVisibility` veya Navigation transition'ları ekleyerek sayfa değişimlerinin yumuşak olmasını sağla (örneğin yükleme ekranından sonuç ekranına geçerken sayfanın kayarak gelmesi).

**Başarı Kriteri:** Uygulamanın başından sonuna kadar (rehber -> dosya yükleme -> bekleme -> sonuç) pürüzsüz, tek bir sayfa (Single Activity) mantığıyla akması.

### "Sunucusuz" Mimari (BaaS - Sadece Android + Firebase)

Bir Android geliştiricisi olarak MVP'yi en hızlı çıkarabileceğin yöntem budur. Geleneksel bir backend (Node.js vb.) yazmak yerine, işlemleri cihazda yapıp bulut servislerini araç olarak kullanırsın.

* **Veri İşleme (Parsing):** WhatsApp `.txt` dosyasını temizleme ve formatlama işlemi tamamen Android cihazın içinde (istemci tarafında) yapılır.
* **Yapay Zeka İletişimi:** Temizlenen veri, Google'ın Android için sunduğu Generative AI SDK kullanılarak doğrudan uygulamadan Gemini API'ye gönderilir.
* **Kimlik Doğrulama ve Veritabanı:** Kullanıcı girişleri için Firebase Authentication, kullanıcının geçmiş analiz raporlarını (onayıyla) saklamak istersen Firebase Firestore kullanılır.
* **Avantajı:** Çok hızlı geliştirilir, sunucu maliyeti sıfırdır, gizlilik endişesi azdır (veri doğrudan Google'a gider, aracı bir sunucuda durmaz).
* **Dezavantajı:** Gemini API anahtarını uygulamanın içine gömmen gerekir (kötü niyetli kişiler tersine mühendislikle API key'ini bulabilir).