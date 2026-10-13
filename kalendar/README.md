# Raspored_app — univerzalni kalendar

Zasebna Android i iOS aplikacija za **smjene, događaje i napomene**, bez plaće, koeficijenata, bodova, poreznih pravila ili računa.

## Funkcionalnosti
- Kompaktni mjesečni (4–6 redaka) i tjedni prikaz kalendara s jednostavnim prebacivanjem i pamćenjem odabira na uređaju, današnji datum, brzi prijelaz na prethodni ili sljedeći dan te obojene oznake zapisa.
- Vlastiti nazivi smjena, događaja i napomena, bez nametnutih smjenskih šablona.
- Više zapisa za isti datum; zasebne napomene bez obveznog naziva.
- Neobavezni početak i kraj `HH:mm`; dopušten prelazak preko ponoći.
- Uređivanje, promjena datuma i dupliciranje zapisa uz novi identifikator, brisanje s potvrdom te pretraga naziva, napomena, vrsta i datuma. Pretraživanje prihvaća i hrvatska slova bez kvačica (npr. nocna).
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

## Sigurnosna kopija i privatnost
U kalendaru odaberite **Izvezi kopiju** kako biste spremili JSON datoteku u Datoteke/Files ili lokalni prostor na uređaju. **Uvezi kopiju** učitava isti JSON s Androida ili iOS-a, provjerava cijelu datoteku prije promjena te dodaje samo zapise čiji ID još ne postoji. Postojeći zapisi **nikada se ne zamjenjuju ni brišu**; neispravan format, duplicirani ID-evi i prevelike kopije odbijaju se. Format `raspored-universal-calendar`, verzija 1, do 5 MB / 15.000 zapisa. Datoteka nije šifrirana: čuvajte je na sigurnom mjestu.

Zapisi ostaju u privatnoj lokalnoj pohrani. Brisanjem aplikacije mogu nestati ako prethodno ne izvezete kopiju. Nema cloud sinkronizacije ni praćenja. Izvorni Raspored čuva vlastite podatke potpuno odvojeno.
