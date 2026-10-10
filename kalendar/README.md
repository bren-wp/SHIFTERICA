# Raspored_app — univerzalni kalendar

Zasebna Android i iOS aplikacija za **smjene, događaje i napomene**, bez plaće, koeficijenata, bodova, poreznih pravila ili računa.

## Funkcionalnosti
- Mjesečni kalendar, današnji datum, navigacija i obojene oznake zapisa.
- Vlastiti nazivi smjena, događaja i napomena, bez nametnutih smjenskih šablona.
- Više zapisa za isti datum; zasebne napomene bez obveznog naziva.
- Neobavezni početak i kraj `HH:mm`; dopušten prelazak preko ponoći.
- Uređivanje, brisanje s potvrdom, pretraga naziva, napomena, vrsta i datuma.
- Offline lokalna pohrana, bez korisničkog računa i internetskih dozvola.
- Tamni midnight/sapphire dizajn i mint naglasci kao u postojećem Rasporedu.

## Izdanja
Android: `Raspored_app-VERSION-debug.apk` i `Raspored_app-VERSION-debug.aab`.
iOS: `Raspored_app-VERSION-Simulator.app.zip`, `Raspored_app-VERSION-unsigned.ipa`, `Raspored_app-VERSION-unsigned.xcarchive.zip`.
Nepotpisani iOS paketi nisu spremni za izravnu instalaciju na iPhone. Debug Android paket nije Play Store release.

Odvojeni identifikator: `hr.raspored.kalendar` (Android/iOS). Izvorni Raspored ima `hr.raspored.app`.

## Build
Android (Java 17, Gradle 8.13):
```bash
cd kalendar/android
gradle :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:bundleDebug
```

iOS (Xcode 16.4, XcodeGen):
```bash
xcodegen generate --spec kalendar/ios/project.yml
xcodebuild -project kalendar/ios/RasporedUniversalCalendar.xcodeproj -scheme Raspored_app -sdk iphonesimulator CODE_SIGNING_ALLOWED=NO build
```
GitHub Actions generira ugrađenu ikonu iz postojećeg brendinga i snima stvarni iOS Simulator ekran.

## Privatnost
Zapisi se čuvaju samo unutar privatne pohrane aplikacije. Brisanjem aplikacije mogu nestati. Trenutno nema izvoza sigurnosnih kopija niti cloud sinkronizacije. Podaci postojećeg Rasporeda ostaju potpuno odvojeni.
