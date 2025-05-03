# 🚍 RouteAlarm

**RouteAlarm**, toplu taşıma kullanan kullanıcıların belirli bir durağa yaklaştığında uyarı 
almasını sağlayan konum tabanlı bir alarm uygulamasıdır. Uygulama Android ve iOS üzerinde Kotlin 
Multiplatform (KMP) kullanılarak geliştirilmiştir ve Jetpack Compose ile oluşturulmuş modern, 
sade bir kullanıcı arayüzüne sahiptir.

---

## 📱 Uygulama Özeti

RouteAlarm, kullanıcıların:
- Bilmedikleri bir yere giderken,
- Yolculuk sırasında uyuyakalabilecekleri durumlarda,
- Birden fazla taşıma aracıyla (metro, metrobüs vb.) yolculuk yaparken

hedef durağa yaklaştıklarında zamanında haberdar edilmelerini sağlar.

---

## 🧩 Ekranlar ve İşlevleri

### 🖼️ Welcome Screen
- Uygulama tanıtımı.
- "Devam Et" butonu ile giriş.

### 🔐 Auth Screen (şimdilik pasif)
- Kayıt / Giriş ekranı.
- İleride Firebase veya başka bir auth çözümüyle entegre edilebilir.

### 🏠 Home Screen
- Kayıtlı alarmların listesi.
- "Yeni Alarm Ekle" butonu.
- Alarm seçilince detay ekranına yönlendirme.

### 📋 Alarm Detail Screen
- Seçili alarmın durakları, zaman bilgisi, harita gösterimi.
- Alarm düzenleme veya silme.

### ➕ Add Alarm Screen
- Arama çubuğu veya harita ile durak/lokasyon seçimi.
- Birden fazla durak eklenebilir.
- Alarm kaydetme.

### 📍 Stop Picker Screen
- Bir alarm için birden fazla iniş yeri belirleme (örneğin önce metro, sonra metrobüs).
- Sıralı olarak durak listesi gösterilir.

### ⏱️ Active Alarm Screen
- Harita ile canlı konum takibi.
- Yaklaşık süre ve durak sayısı bilgisi.
- Alarmı erteleme veya durdurma.

### ⚙️ Settings Screen
- Alarm sesi seçimi.
- Titreşim, dil ve bildirim ayarları.



----------------------------------
RouteAlarm

RouteAlarm is a location-based alarm app for Android and iOS built using Kotlin Multiplatform and 
Jetpack Compose Multiplatform. The app helps users traveling by public transport to set alarms that 
notify them before reaching their destination stop.

📄 App Overview

Use Case:

You are traveling by bus, metro, or metrobus and want to get off at the right stop.

You may be tired, sleepy, or unfamiliar with the route.

RouteAlarm helps by notifying you a few minutes or stops before you arrive at your chosen location.

Key Features:

Set alarms for multiple locations (e.g., transfer from metro to metrobus).

View live location and estimated arrival.

Alarms with sound, vibration, and visual feedback.

Works on both Android and iOS.

👜 Screens

1. Welcome Screen

App introduction.

"Continue" button to proceed to the Home screen.

2. Auth Screen (optional, future)

Sign up / Login UI.

Not used initially, but planned for future.

3. Home Screen

List of saved alarms.

Button to add new alarm.

Click an alarm to open Alarm Detail.

4. Alarm Detail Screen

Shows selected stops and map preview.

Estimated time and controls for delete/edit.

5. Add Alarm Screen

Search bar or map to pick a stop.

Option to add multiple stop points.

Save button to confirm.

6. Stop Picker Screen

Shows list of selected stops in order.

Allows adding or removing stop points.

7. Active Alarm Screen

Live map showing user location.

Countdown (time or stop-based).

Option to snooze or cancel alarm.

8. Settings Screen

Alarm sound selection.

Vibration on/off.

Preferred language.

📅 Development Plan (To-Do)



🌐 Technologies Used

Kotlin Multiplatform (KMP)

Jetpack Compose Multiplatform

Kotlinx Serialization

Map integration (Google Maps / Apple MapKit)

StateFlow + ViewModel

Platform-specific services (location, notifications)

✈️ Future Plans

User accounts and cloud sync

Smart alarm based on real-time transport data

Offline support

Analytics and crash reporting