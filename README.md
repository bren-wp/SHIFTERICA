# SHIFTERICA — Raspored Premium

Native Android i iOS aplikacija za raspored smjena. Ovaj repozitorij je glavno razvojno mjesto za SHIFTERICA i sadrži produkcijski kod koji je dosad izrađen u chatu, prenesen iz radnog paketa **Raspored Premium v1.2.2**.

## Verzija

**1.2.2**

## Platforme

- **Android:** Kotlin + Jetpack Compose + Material 3
  - applicationId: `hr.raspored.app`
  - minSdk 26
  - target/compile SDK 36
  - Java 17
- **iOS:** SwiftUI
  - bundle identifier: `hr.raspored.app`
  - iOS 17+
  - XcodeGen projekt u `ios/project.yml`

## Implementirano

- splash / početni prikaz
- mjesečni kalendar
- početni raspored za listopad 2026. prema referenci
- način uređivanja s gumicom i oznakama N / D / GO / J / BO
- godišnji pregled svih 12 mjeseci
- sažetak i statistika
- pretraživanje rasporeda
- upravljanje smjenama
- izrada i trajno spremanje vlastitih smjena
- vlastite boje, boja teksta, skraćenica i veličina teksta smjene
- jedan ili dva vremenska intervala po vlastitoj smjeni
- Android i iOS JSON uvoz vlastitih smjena
- postavke s trajnim spremanjem
- odabir prvog dana tjedna
- prikaz/sakrivanje dana susjednih mjeseci
- isticanje vikenda i današnjeg datuma
- veličina brojeva dana
- postavke formata vremena i datuma
- postavke bilješki
- dodatno 1:1 vizualno usklađivanje headera, kalendara, godišnjeg pregleda, sažetka, uređivanja i postavki
- lokalno spremanje rasporeda bez računa i bez mrežne ovisnosti

## Hrvatski jezik

Primarno sučelje koristi hrvatsku terminologiju i dijakritičke znakove: **Listopad, Sažetak, Mjesec, Godina, Razdoblje, Noćna smjena, Dnevna smjena, Jutarnja smjena, Godišnji odmor, Bolovanje, Postavke, Bilješke, Način uređivanja, Izađi iz uređivanja** i ostale oznake iz referentnih ekrana.

## Android build

Otvorite mapu `android/` u Android Studiju s JDK-om 17 i omogućite Gradle sinkronizaciju.

```bash
cd android
gradle :app:assembleDebug
```

Očekivani izlaz: `android/app/build/outputs/apk/debug/app-debug.apk`.

## iOS build

Na macOS-u s Xcodeom i XcodeGenom:

```bash
cd ios
xcodegen generate --spec project.yml
open Raspored.xcodeproj
```

Pokrenite shemu `Raspored` na simulatoru ili fizičkom uređaju.

## Vlastite smjene

Ekran **Nova smjena** sprema novu definiciju lokalno. Skraćenica mora imati od 1 do 4 alfanumerička znaka i ne može zamijeniti ugrađene oznake N, D, GO, J ili BO. Vremenski intervali mogu ostati prazni za odsutnost/oznaku bez obračuna sati.

Na Androidu i iOS-u gumb **Uvezi smjenu** prihvaća JSON objekt ili JSON polje objekata s poljima `code`, `name`, `start`, `end`, `secondaryStart`, `secondaryEnd`, `background`, `foreground` i `fontSize`.

## Privatnost

SHIFTERICA/Raspored je local-first: raspored, postavke i vlastite smjene spremaju se lokalno na uređaju.

## QA

- Swift izvori prolaze parser provjeru.
- Kotlin izvori su prošli lokalnu sintaksnu provjeru; puni Android type-check zahtijeva Android SDK i Compose/Gradle ovisnosti.
- Provjeren je raspored za listopad 2026. i izračun 17 smjena / 110 h prema referenci.
- Provjerene su ključne hrvatske oznake i struktura projekta.

Detalji: `qa/QA_REPORT.md`.

---

**Daljnji razvoj od ove točke radi se izravno u `bren-wp/SHIFTERICA`.**


## Izdanje i artefakti

Svaki uspješan push na `main` nakon Android/iOS QA gradi GitHub izdanje prema datoteci `VERSION`.

Artefakti izdanja:
- Android debug APK
- Android debug AAB
- iOS Simulator `.app.zip`
- iOS unsigned `.xcarchive.zip`
- iOS unsigned `.ipa`

Za instalaciju na fizički iOS uređaj ili objavu u App Storeu unsigned artefakt mora se potpisati Apple Distribution certifikatom i odgovarajućim provisioning profilom.
