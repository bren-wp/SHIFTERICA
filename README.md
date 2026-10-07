<p align="center">
  <img src="docs/assets/raspored-logo.svg" alt="Raspored" width="760">
</p>

<p align="center">
  <strong>Pametni planer smjena za Android i iOS.</strong><br>
  Brz, privatan i vizualno dosljedan raspored rada — mjesec, godina, sažetak i uređivanje smjena u jednom premium sučelju.
</p>

<p align="center">
  <img alt="Android" src="https://img.shields.io/badge/Android-26%2B-3DDC84?logo=android&logoColor=white">
  <img alt="iOS" src="https://img.shields.io/badge/iOS-17%2B-000000?logo=apple&logoColor=white">
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-Compose-7F52FF?logo=kotlin&logoColor=white">
  <img alt="Swift" src="https://img.shields.io/badge/Swift-SwiftUI-F05138?logo=swift&logoColor=white">
  <img alt="Version" src="https://img.shields.io/badge/release-v1.2.4-19DCE0">
  <img alt="License" src="https://img.shields.io/badge/license-MIT-2A5D7D">
</p>

<p align="center">
  <a href="https://github.com/bren-wp/SHIFTERICA/releases/latest"><strong>Preuzmi najnovije izdanje</strong></a>
  ·
  <a href="#što-raspored-nudi">Mogućnosti</a>
  ·
  <a href="#razvoj-i-build">Build</a>
  ·
  <a href="#privatnost">Privatnost</a>
</p>

---

## Raspored u jednoj slici

<p align="center">
  <img src="docs/assets/ui-showcase.svg" alt="Pregled Raspored sučelja" width="100%">
</p>

## Zašto Raspored?

Raspored je napravljen za ljude koji rade u smjenama i žele jasan kalendar bez nepotrebnih računa, oglasa i kompliciranih administracijskih ekrana. Vizualni sustav koristi tamnu podlogu, staklaste kartice i cijan naglaske, dok svaka vrsta smjene ima vlastitu prepoznatljivu boju.

Aplikacija je razvijena odvojeno za Android i iOS, ali oba izdanja slijede isti raspored elemenata, iste hrvatske nazive i istu semantiku dodira. Cilj projekta je **1:1 funkcionalni i vizualni paritet**.

## Što Raspored nudi

| | Mogućnost | Opis |
|---|---|---|
| 📅 | **Mjesečni kalendar** | Brzi pregled smjena po danima, istaknuti vikendi i dani iz susjednih mjeseci. |
| 🗓️ | **Godišnji pregled** | Svih 12 mjeseci u kompaktnom prikazu s bojama smjena. |
| 📊 | **Sažetak rada** | Broj smjena, ukupni sati i prosjek po smjeni za odabrano razdoblje. |
| ✏️ | **Način uređivanja** | Jedan dodir za N, D, GO, J, BO ili vlastitu smjenu. |
| ➕ | **Vlastite smjene** | Naziv, skraćenica, boja, tekst, veličina i do dva vremenska intervala. |
| 🔎 | **Pretraživanje** | Pronalaženje smjene prema datumu ili nazivu. |
| 🎨 | **Prilagodba izgleda** | Veličina datuma, vikendi, isticanje dana, oblik, boja i prozirnost. |
| 🇭🇷 | **Hrvatski jezik** | Terminologija i tekstovi uređeni za hrvatski jezik. |
| 🔒 | **Local-first privatnost** | Raspored i postavke ostaju lokalno na uređaju; nema oglasnih trackera. |

## Vizualni identitet

<p align="center">
  <img src="docs/assets/app-icon.svg" alt="Raspored app icon" width="180">
</p>

Primarni akcent je **#19DCE0**, uz tamnu pozadinu **#061624**. Smjene koriste stabilan sustav boja:

| Oznaka | Smjena | Boja |
|---|---|---|
| **N** | Noćna | 🟨 `#FFD21F` |
| **D** | Dnevna | 🟦 `#13B7F3` |
| **GO** | Godišnji odmor | 🟩 `#6CEB82` |
| **J** | Jutarnja | 🟢 `#77DED7` |
| **BO** | Bolovanje | 🟪 `#D991EE` |

Detaljni dizajnerski sustav nalazi se u [docs/DESIGN_SYSTEM.md](docs/DESIGN_SYSTEM.md), a kontrola 1:1 pariteta u [docs/REFERENCE_PARITY_CHECKLIST.md](docs/REFERENCE_PARITY_CHECKLIST.md).

## Android

Android izdanje koristi **Kotlin + Jetpack Compose + Material 3**.

Minimalna verzija: Android 8.0 / API 26.  
Target: API 36.

Build proizvodi:

- APK za instalaciju i QA
- AAB za bundle provjeru i daljnju distribuciju

## iOS

iOS izdanje koristi **Swift 5.10 + SwiftUI**.

Minimalna verzija: iOS 17.

Build proizvodi:

- Simulator `.app`
- unsigned `.xcarchive`
- unsigned `.ipa`

Za instalaciju na fizički uređaj, TestFlight ili App Store potreban je Apple certifikat i odgovarajući provisioning profil.

## Razvoj i build

### Android

```bash
cd android
gradle :app:testDebugUnitTest
gradle :app:lintDebug
gradle :app:assembleDebug :app:bundleDebug
```

### iOS

```bash
brew install xcodegen
xcodegen generate --spec ios/project.yml

xcodebuild \
  -project ios/Raspored.xcodeproj \
  -scheme Raspored \
  -sdk iphonesimulator \
  CODE_SIGNING_ALLOWED=NO \
  build
```

GitHub Actions automatski izvršava Android testove i lint, Android APK/AAB build, iOS simulator build, iOS device archive te izdavanje artefakata nakon uspješnog CI-ja.

## Privatnost

Raspored je zamišljen kao **local-first** aplikacija:

- ne zahtijeva korisnički račun
- ne sadrži oglasne trackere
- ne šalje raspored smjena na udaljeni poslužitelj
- Android blokira cleartext mrežni promet i backup podataka aplikacije
- iOS sadrži Privacy Manifest
- korisničke smjene i postavke spremaju se lokalno

## Kvaliteta koda

Za izdanje 1.2.4 napravljen je puni dead-code audit. Rezultati i iznimke dokumentirani su u [qa/DEAD_CODE_AUDIT.md](qa/DEAD_CODE_AUDIT.md).

CI dodatno provjerava produkcijski source tree na nedovršene `TODO`, `FIXME`, `HACK` i `XXX` oznake.

## Struktura repozitorija

```text
SHIFTERICA/
├── android/                 # Kotlin + Jetpack Compose
├── ios/                     # SwiftUI + XcodeGen
├── docs/                    # dizajn, paritet i vizualni asseti
├── qa/                      # QA i dead-code audit
├── .github/workflows/       # CI + release pipeline
├── CHANGELOG.md
├── VERSION
└── README.md
```

## Izdanje

Najnovija verzija je **v1.2.4**.

➡️ [GitHub Releases](https://github.com/bren-wp/SHIFTERICA/releases/latest)

## Licenca

Projekt je dostupan pod MIT licencom. Pogledajte [LICENSE](LICENSE).

---

<p align="center">
  <strong>Raspored</strong><br>
  Pametni planer smjena za uredniji radni mjesec.
</p>
