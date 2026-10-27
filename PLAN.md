# NYFF Android — reja va ishlar ro'yxati

> **Holat (2026-10-27):** 0–5 va 7 bosqichlar tayyor (pastda). Unit testlar: 35 ta (`gradlew testDebugUnitTest`), lint — 0 xato.
> Emulyatorda (AVD `nyff_api36`, API 36.1, Google Play) lokal backend bilan uchidan-uchiga tekshirilgan: menyu, xarita +
> GPS → Chilonzor hududi, dostavka buyurtmasi → 5 daqiqadan keyin Yunusobodga o'tkazildi (raqam socket orqali jonli
> yangilandi) → kassir qabul qildi → "Tayyor"; olib ketish buyurtmasi → kassir bekor qildi; tarix, ruscha, tungi mavzu.
> Kuryer bilan (7): #019 dostavka — "Tayyor · Kuryer olib ketadi" → "Kuryer yo'lda" → "Yetkazildi", socket orqali jonli.
> **Keyingi qadam:** haqiqiy API manzili (foydalanuvchi beradi) → `local.properties` dagi `nyff.apiUrl`; keyin FCM push,
> Click (backend PLAN 10 bilan birga).
> Yangi ish faqat foydalanuvchi aniq **"boshla"** deganidan keyin boshlanadi. Har bosqich tugaganda shu fayldagi
> katakchalarni va holat qatorini yangilang.

## 1. Maqsad

New York Fast Food mijoz ilovasi (Android): filial / dostavka manzili → menyu → savat → buyurtma → holatni jonli
kuzatish. Backend — `C:\BACKEND APPS\NestJS_NYFF` (`API.md` 5-bo'lim — mobil oqim). Ilova hech narsani o'zi
hisoblamaydi: filialni tanlash, narx, hudud narxi, ochiq / yopiq — serverda. Ko'rinish va matnlar — web ilovaning mobil
demosidan (`ReactJS_NYFF/src/pages/mobile/*`, `src/i18n/*` → `mobile`, `errors`).

## 2. Kelishilgan qarorlar

| Mavzu | Qaror |
|---|---|
| Uslub | KotlinERPSYSTEM ga o'xshatilmaydi ("bu boshqa loyiha"): MVVM + StateFlow, Hilt, Navigation Compose (type-safe), Room, DataStore |
| Tarmoq | Retrofit 3 + OkHttp 4 + kotlinx.serialization (converter). Socket.IO — `io.socket:socket.io-client` 2.1 (`org.json` chiqarib tashlangan) |
| Server manzili | Ilovada kiritilmaydi. `local.properties` → `nyff.apiUrl` → `BuildConfig.API_URL`. Hozir kompyuterning LAN IP si `http://192.168.1.12:3000` — haqiqiy telefon (TECNO LG6n, Android 12, shu Wi-Fi) ham, emulyator ham ulanadi (`10.0.2.2` faqat emulyatorda — telefonda ishlamagani topilgan). `http://` bo'lsa cleartext o'zi yoqiladi (manifest placeholder) |
| Joylashuv | Yaqinda (2 daqiqa ichida) ma'lum bo'lgan joy darhol olinadi, aks holda yangi aniqlash 20 s gacha kutiladi (telefonda birinchi GPS aniqlash 10 s dan oshib ketgan) |
| Xarita | Google Maps (maps-compose). Xarita joriy joylashuvda ochiladi, mijoz xaritani surib markazdagi pinni aniq joyga keltiradi. Kalit — web `.env` dagi `VITE_GOOGLE_MAPS_API_KEY`, `local.properties` dagi `MAPS_API_KEY` (git'ga tushmaydi). Pullik Geocoding / Places ishlatilmaydi — manzil matni qo'lda (ROADMAP 3) |
| Tillar | uz (standart, `values/`), ru, en. AppCompat per-app language (`AppCompatDelegate.setApplicationLocales`); birinchi ishga tushishda — o'zbekcha. Android 13+ da sozlamalardagi "Ilova tili" bilan bir xil |
| Mavzu | Yorug' va tungi — telefon sozlamasiga qarab. Ranglar web tokenlaridan: taksi sarig'i `#FFC20E`, qora `#121212`, tanlangan `#FE9A00` |
| Buyurtmalar tarixi | Telefonda (Room) saqlanadi, **o'chirilmaydi**. Serverdagi buyurtmalarga tegilmaydi. Har yozuvda `trackingToken` (faqat buyurtma javobida keladi) |
| Kontakt | Ism, telefon (`+998` + 9 raqam), manzil, to'lov usuli — DataStore'da, keyingi buyurtmada qayta yozilmaydi. Standart to'lov — naqd (Click hali ulanmagan) |
| Qoralama | Buyurtma turi, olib ketish filiali, dostavka nuqtasi — DataStore'da; savat — faqat xotirada |
| Menyu filiali | Olib ketish — tanlangan filial (tanlanmagan bo'lsa birinchi ochiq); dostavka — `GET /delivery/route` dagi `branchId`; manzil hali yo'q yoki ochiq filial yo'q — ko'rib turish uchun birinchi ochiq filial (`domain/Shop.kt`) |
| Buyurtma berish | Idempotent emas — tarmoq xatosida avtomatik qayta yuborilmaydi, alohida ogohlantirish matni. So'rov va saqlash ilova doirasida (`ApplicationScope`) — ekran yopilsa ham token yo'qolmaydi |
| Yetkazish narxi | Faqat ko'rsatiladi ("kuryerga alohida"), summaga qo'shilmaydi (API.md 5.2). Buyurtma berilgandagi narx Room'da saqlanadi |
| Jonli holat | Kuzatish ekrani ko'rinib turganda Socket.IO (`order:<id>`); har ulanishda va ekranga qaytishda REST; socket ulanmagan paytda har 12 s so'rov. Bosh sahifa / tarix — ochilganda REST |
| Xatolar | `AppError`: `Server(status, code, detail)` / `Network` / `Unexpected`; matn — `code` bo'yicha (`ui/common/Texts.kt`), `detail` ko'rsatilmaydi |
| Kotlin | AGP 9.3 ichidagi Kotlin 2.2.10. Kotlin 2.4 stdlib talab qiladigan kutubxonalar olinmaydi (masalan `maps-compose` 8.4+ → 8.3.0) |
| Server vaqti | Qabul muddati sanog'i (`routedAt` + 5 daqiqa) telefon soati bo'yicha emas — HTTP javoblaridagi `Date` sarlavhasidan (`domain/ServerClock.kt`); emulyatorda soat 2 daqiqa farq qilgani topilgan |
| Xarita va Play xizmatlari | Manifestda `org.apache.http.legacy` (`required="false"`) — busiz Google Play Maps moduli ba'zi qurilmalarda yiqiladi (emulyatorda tasdiqlangan) |
| Kuryer holati (2026-10-27) | Mijozga faqat holat: dostavka 4 bosqich — qabul → tayyorlanmoqda → "Tayyor · Kuryer olib ketadi" → "Kuryer yo'lda" (`departedAt`) → "Yetkazildi" (`deliveredAt`); "Yetkazib bo'lmadi" (`returnedAt`) — alohida karta va filialga qo'ng'iroq. Kuryer kimligi, navbati va kelish vaqti ko'rsatilmaydi. Buyurtma `status` i `ready` da qoladi — bosqich `domain/OrderProgress.kt` da. Yetkazish narxi — serverdagi `deliveryFee`, bo'lmasa saqlangani. Yetkazilmagan dostavka 12 soat "joriy" sanaladi |
| Server manzilini vaqtincha almashtirish | `./gradlew assembleDebug -Pnyff.apiUrl=http://...` — `local.properties` ga tegmasdan (masalan sinov uchun boshqa port) |

## 3. Bosqichlar

- [x] **0. Karkas** — kutubxonalar, ruxsatlar, Hilt, Room, DataStore, Retrofit + xatolar, tema (2 ta), 3 til, ikonka
- [x] **1. Bosh sahifa** — filiallar va holati, yetkazib berish / olib ketish, menyu (kategoriyalar, 2 ustun), taom oynasi (tarkib, izoh, soni), savat paneli, til tanlash
- [x] **2. Xarita** — joriy joylashuv (ruxsat), markazdagi pin, filial hududlari, `/delivery/route` → filial va hudud narxi
- [x] **3. Rasmiylashtirish** — savat, joy (manzil / filial), ism / telefon / manzil / izoh, to'lov, tekshiruvlar, `POST /mobile/orders`
- [x] **4. Kuzatish va tarix** — 3 bosqich, qabul muddati sanog'i, filialga o'tkazish, bekor qilinish sababi, filialga qo'ng'iroq; "Buyurtmalarim"
- [x] **5. Testlar** — unit (savat, formatlash, telefon, filial tanlash, holat matnlari, API o'qish / xatolar — MockWebServer); emulyatorda backend bilan
- [ ] **6. Keyinroq** — haqiqiy API manzili (https), FCM push (ilova yopiq bo'lsa), Click to'lovi, hudud narxi summaga (backend PLAN 10), iOS
- [x] **7. Kuryer holatlari** — `CustomerOrder` ga `deliveryFee`, `departedAt`, `deliveredAt`, `returnedAt`; 4 bosqichli dostavka, "Yetkazildi" / "Qaytdi" holatlari va matnlari (3 til), testlar. Kuryer ilovasi — `Kotlin_NYFF_Courier`
