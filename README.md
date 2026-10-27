# New York Fast Food — Android ilova (Kotlin_NYFF)

Mijoz ilovasi: filial yoki dostavka manzili (Google Maps) → menyu → savat → buyurtma → holatni jonli kuzatish
(Socket.IO). Backend — [NestJS_NYFF](../NestJS_NYFF) (`API.md` 5-bo'lim), web ilova — [ReactJS_NYFF](../ReactJS_NYFF).
Reja va qarorlar — [PLAN.md](PLAN.md).

**Kuryer qismi** shu ilova ichida: bosh sahifadagi "NY" belgisiga ketma-ket 10 marta bosilsa — kuryer kirish oynasi
(login va parolni admin web ilovada beradi: Xodimlar → rol "Kuryer"; demo: `bobur` / 222222). Kuryer kirgan bo'lsa
ilova har safar kuryer ekranida ochiladi; "Chiqish" — mijoz qismiga (API.md 12-bo'lim).

Stek: Kotlin 2.2, Jetpack Compose (Material 3), Hilt, Navigation Compose, Retrofit 3 + OkHttp + kotlinx.serialization,
Socket.IO, Room, DataStore, Coil 3, Google Maps (maps-compose), Play Services Location. minSdk 28, targetSdk 37.

## Sozlash

`local.properties` (git'ga tushmaydi):

```properties
sdk.dir=...
# backend manzili — kompyuterning LAN IP si (ipconfig): haqiqiy telefon (shu Wi-Fi da) ham, emulyator ham ulanadi.
# 10.0.2.2 faqat emulyatorda ishlaydi — haqiqiy telefonda "Server bilan aloqa yo'q" bo'ladi.
nyff.apiUrl=http://192.168.1.12:3000
# Google Maps kaliti (Maps SDK for Android) — web ilovaning .env dagi VITE_GOOGLE_MAPS_API_KEY
MAPS_API_KEY=...
```

Haqiqiy server manzili berilganda faqat `nyff.apiUrl` almashtiriladi (`https://` bo'lsa cleartext o'zi o'chadi).
Kompyuter IP si o'zgarsa (DHCP) — `nyff.apiUrl` ni yangilab, qayta build qiling. Windows firewall'da Node.js'ga kiruvchi
ulanish ruxsati bo'lishi kerak (telefondan tekshirish: brauzerda `http://<IP>:3000/api/health` → `{"ok":true}`).

## Ishga tushirish

```bash
# JAVA_HOME — Android Studio JBR (Gradle daemon JDK 25)
./gradlew assembleDebug          # app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest      # unit testlar
```

Backend lokal ishlashi kerak (`NestJS_NYFF`: `npm run start:dev`, port 3000). Filial `open` bo'lishi uchun kassa ekrani
socket'ga ulangan bo'lishi kerak — yoki backend `ONLINE_REQUIRE_KASSA=0` bilan.

Emulyatorda joylashuvni berish: `adb emu geo fix 69.2045 41.282` (avval uzunlik, keyin kenglik — Chilonzor hududi).

## Tuzilma

```
app/src/main/java/pr/com/kotlin_nyff/
  core/        ilova tili (uz / ru / en)
  data/        model (API turlari), remote (Retrofit, xatolar, Socket.IO), local (Room, DataStore),
               CatalogRepository, OrderRepository, DraftStore (savat va tanlov)
  di/          Hilt moduli
  domain/      toza funksiyalar: savat, menyu filiali, formatlash, telefon, buyurtma holati
  ui/          theme, common (komponentlar, matnlar), home, location, checkout, tracking, orders, NyffNavHost,
               AppRoot (mijoz yoki kuryer qismi)
  courier/     kuryer qismi: data (modellar, Retrofit, token, Socket.IO, sessiya), di, domain (pul, xarita
               havolalari), ui (kirish, "Yangi" / "Meniki" / "Bugun", xarita, bildirishnoma)
```

Matnlar: mijoz — `res/values*/strings.xml`, kuryer — `res/values*/strings_courier.xml` (`courier_` prefiksi).
