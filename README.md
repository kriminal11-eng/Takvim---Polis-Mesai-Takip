# Mesai Takvimi – Polis Mesai Takip (Android)

"Çakma 12/36" vardiya sistemine göre çalışan polisler için takvim + ajanda uygulaması.

## Özellikler
- **Otomatik mesai takvimi** (9 günlük döngü): 3 gün üst üste gündüz (08:00–18:00), ardından gün aşırı 3 gece (18:00–08:00).
  Örnek: 20-21-22 Kasım gündüz → 23-25-27 Kasım gece → 29 Kasım yeni döngü.
- **Renkler:** Sarı = gündüz, Mavi = gece, Gri = izin/rapor, boş = dinlenme. Takvimin altında açıklama (lejant) bulunur.
  Renkler Ayarlar'dan değiştirilebilir.
- **Ajanda:** Her güne not, görev, hatırlatma ekleme. Türlerin renkleri:
  Not (mor), Hatırlatma (kırmızı), Görev (yeşil), Önemli (turuncu), Kişisel (turkuaz).
- **Bildirimler:** Kayıtlar için zamanında / 10 dk / 30 dk / 1 saat / 2 saat / 1 gün önce uyarı.
  Telefon yeniden başlasa da alarmlar korunur. (İsteğe bağlı mesai uyarısı Ayarlar'dan açılabilir; varsayılan kapalı.)
- **Elle değişiklik:** Bir günü izin, rapor ya da farklı vardiya olarak işaretleme.
- **Aylık özet:** Gündüz/gece sayısı ve toplam çalışma saati.

## Kurulum (telefona)
1. GitHub'da depo sayfasındaki **Releases** bölümünden en son `MesaiTakvimi.apk` dosyasını indirin.
2. Dosyayı açın; "bilinmeyen kaynaklardan yüklemeye" izin verin.
3. İlk açılışta bildirim iznini onaylayın.
4. Ayarlar → "İlk gündüz günü" kısmından kendi döngünüzün ilk gündüz gününü seçin (varsayılan 20 Kasım 2026).

## Derleme
APK her push'ta GitHub Actions ile otomatik derlenir (`.github/workflows/android.yml`).
Yerelde: Android Studio ile açıp çalıştırın veya `./gradlew assembleRelease`.

---
Ozan KARTOL tarafından yapılmıştır.
