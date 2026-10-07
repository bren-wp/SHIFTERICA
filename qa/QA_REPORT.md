# QA report — Raspored 1.2.4

## Status

**CI validacija je obvezna nakon ovog commita.**

## U ovoj iteraciji

1. Ispravljen je Android compile problem s nepostojećom Compose ikonom `Eraser`.
2. Dodan je nedostajući `LazyColumn` import u Android sheetove.
3. Ispravljen je JVM platform declaration clash u `UiSettingsStore` preimenovanjem eksplicitnih mutatora postavki.
3. iOS je u prethodnom CI pokušaju uspješno prošao simulator build.
4. Android i iOS dodatno su usklađeni s 1:1 referentnim ekranima.
5. Brojevi dana u mjesečnom prikazu premješteni su u gornji lijevi kut.
6. Godišnji pregled u ćelijama sa smjenama prikazuje i datum i oznaku.
7. Postavke su približene referenci vizualnim biračem oblika te uklanjanjem dodatne donje akcije.
8. Splash i app mark dodatno su usklađeni bez generiranja novih slika.
9. Referentni listopad 2026. ostaje 9 noćnih + 8 dnevnih = **17 smjena i 110 h**.
10. Hrvatska terminologija ostaje obvezna na obje platforme.

## Obvezna CI provjera

- Android unit testovi
- Android debug APK build
- APK artifact verify
- iOS XcodeGen
- iOS Simulator build bez code-signinga

Izdanje 1.2.4 smatra se provjerenim tek kada CI završi zeleno.


## Distribucijski artefakti 1.2.4

CI mora provjeriti i objaviti: Android APK, Android AAB, iOS Simulator ZIP, unsigned iOS XCArchive ZIP i unsigned IPA. GitHub Release se smije objaviti tek nakon zelenih Android i iOS buildova.


## 1:1 polish provjera 1.2.4

- header i primarne akcije
- glavni tabovi
- mjesečni kalendar i navigacija
- smjenske ćelije i vikendi
- legenda i donja alatna traka
- način uređivanja
- izbornik Smjene
- Nova smjena
- Godina
- Sažetak
- Postavke i birač oblika
- Android launcher ikona
- iOS generirani App Icon


## Završni UI/UX i dead-code prolaz 1.2.4

- Android: Smjene, Nova smjena, Pretraživanje i Postavke dodatno ispolirani.
- iOS: Smjene, Nova smjena, Pretraživanje i Postavke dodatno ispolirani.
- iOS pretraživanje prebačeno s generičkog List prikaza na puni Raspored vizualni sustav.
- Jezik u Postavkama je interaktivan na obje platforme.
- Podrška i privatnost imaju funkcionalne informativne akcije.
- Android backup podataka je onemogućen, a cleartext mrežni promet blokiran.
- Full dead code audit: `qa/DEAD_CODE_AUDIT.md`.
- Android CI: unit testovi + lintDebug + APK/AAB.
- iOS CI: XcodeGen + Simulator build + unsigned device archive/IPA.
- Release se objavljuje samo nakon uspješnih Android i iOS jobova.
