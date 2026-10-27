# Kotlin_NYFF

Foydalanuvchi bilan o'zbek tilida (lotin) gaplashiladi — oraliq izohlar ham.

New York Fast Food mijoz ilovasi (Android, Kotlin + Jetpack Compose). Reja va kelishilgan qarorlar: @PLAN.md.
Backend API — `C:\BACKEND APPS\NestJS_NYFF\API.md` (5-bo'lim — mobil oqim). Ko'rinish va matnlar namunasi — web ilova
`C:\BACKEND APPS\ReactJS_NYFF` (`src/pages/mobile/*`, `src/i18n/*`). Umumiy g'oyalar ro'yxati — `ReactJS_NYFF/docs/ROADMAP.md`.

- Ish faqat foydalanuvchi aniq "boshla" deganidan keyin boshlanadi. "...qilasanmi?", "o'rganib chiq" — ruxsat emas.
- Har bosqich tugaganda `PLAN.md` dagi katakchalar va holat qatorini yangilang; yangi qaror — `PLAN.md` 2-bo'limiga.
- Ilova hech narsani o'zi hisoblamaydi (filial, narx, ochiq / yopiq — serverdan). Xato matni — backend `code` bo'yicha.
- Yangi matn — uchala tilda (`values/` — o'zbekcha, `values-ru/`, `values-en/`).
- Build: `JAVA_HOME` — Android Studio JBR (Gradle daemon JDK 25 talab qiladi). Kutubxona qo'shganda Kotlin 2.4 stdlib
  talab qilmasin (loyiha Kotlin 2.2.10 — AGP 9.3 ichidagi).
