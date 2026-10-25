# Raspored_app — izdvojeni kalendar, izvorni izgled

Izvorna Android i iOS aplikacija **Raspored** prenosi se u `kalendar/` bez izmjene glavnih vizualnih komponenti. Poseban identifikator `hr.raspored.kalendar` čuva instalaciju i podatke prve aplikacije odvojeno.

## Što je preuzeto

- Izvorni mjesec/godina/sažetak, zaglavlje, navigacija, smjenske pločice, prilagodbe, lokalno spremanje i podsjetnici; iste UI komponente umjesto pojednostavljenih imitacija.
- Sve izračune vremena i smjena: prijelaze preko ponoći, praznike, vikende, fond sati, prekovremene, turnuse i dodatke.
- Izračun procjene s istom formulom kao izvornik, **ali bez preuzetog osobnog boda ili osnovice**. Prazna polja `Bod / koeficijent` i `Mjesečna osnovica (€)` dostupna su na Sažetak → Sati. Dok korisnik ne unese oba podatka, procjena nije prikazana.
- Zadržana su ostala podesiva polja staža, djece, uzdržavanih članova i fonda sati. Svi iznosi u kalkulatoru informativni su i ne zamjenjuju službeni obračun.
- Bez korisnikovih osobnih platnih lista, slikovnih priloga, unaprijed unesenog obračunskog profila i usporedbe s potvrđenom platnom listom.
- Odvojena lokalna pohrana i nezavisni Android/iOS paket. Ne postoji sinkronizacija privatnih podataka između izvorne i izdvojene aplikacije.

## Kompatibilnost i sigurnost podataka

Ovaj prijenos mijenja model zasebne aplikacije `kalendar` iz jednostavnih događaja u puni smjenski model prvog Rasporeda. Prethodni događaji ostaju u staroj lokalnoj pohrani i od verzije 1.13.31 mogu se **čitati u Postavke → Prethodni događaji**. Od verzije 1.13.32 možete pretražiti cijelu arhivu po datumu, nazivu, bilješci, kategoriji i vremenu; pregled više nije ograničen na prvih 200 zapisa. Dostupan je poseban JSON izvoz cijele stare arhive i uvoz `raspored-universal-calendar` v1 kopije bez prepisivanja unosa. Kategorije, napomene, satnice i više događaja istog datuma ostaju sačuvani.

Novi puni raspored smjena koristi drugi model s najviše jednom oznakom smjene po danu, stoga **događaje nije sigurno automatski pretvarati u smjene**. Stara arhiva pregledava se zasebno. Prije deinstalacije ili promjene uređaja izvezite obje kopije.  Izvorna aplikacija `hr.raspored.app` ne mijenja svoj podatkovni prostor.

## Provjera svih UI ekrana (1.13.34)

Zajednički Android/SwiftUI ekrani, ikone i boje automatski se uspoređuju s prvom aplikacijom. Iznimke su namjerne: prva aplikacija zadržava svoj obračun, dok samostalni kalendar ima prazan korisnički bod/osnovicu te zaseban arhivski pregled. CI provjerava 9 stvarnih iOS prikaza svake aplikacije, uključujući unos smjene, upravljanje, pretraživanje i dnevni odabir. Izvorna paritetna provjera nije sama po sebi dokaz identičnih piksela na svim kombinacijama telefona, rezolucija i postavki teksta.

Odabir smjena na Androidu ima ograničen pomični prozor i radi sa stotinama korisničkih definicija. Svaka postavka ima jasnu radnju te dovoljne površine za dodir. Pretraživanje se može očistiti jednim dodirom.

## Navigacija i pristupačnost (1.13.33)

U zaglavlju mjeseca prikazuje se akcija **Danas** kada korisnik gleda drugi mjesec; jedno dodirivanje vraća tekući mjesec. Glavni gumb za smjene sada jasno kaže **Upravljanje smjenama**. Čitači zaslona dobivaju cjelovit datum i naziv dodijeljene smjene. Izmjene vrijede za izvorni Raspored i zasebni kalendar; svi podaci i izračuni ostaju isti.

CI automatski izrađuje `build/reports/full-source-audit.json` za sve četiri Kotlin/Swift aplikacije, sa sažetkom neiskorištenih uvoza i simbola koje treba ručno pregledati. Lexical audit nije dokaz da se pojedini simbol može obrisati: pozivi iz platforme, refleksija i dinamički prikazi zahtijevaju dodatnu provjeru.

## Optimizacija (1.13.32)

Izbačeno je 15 starih, nedostupnih Android/iOS ekrana izdvojenog kalendara. Sačuvani su svi modeli i izvorna lokalna pohrana arhive te JSON izvoz/uvoz; uklanjanje zastarjelih UI datoteka ne briše korisničke događaje. Obje Android aplikacije koriste prilagodljivu ikonu i imaju optimiziran `release` build s R8 smanjenjem koda i resursa. Debug APK/AAB artefakti ostaju različiti od nepotpisanih release artefakata.

## Gradnja

Android: `cd kalendar/android && gradle :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:bundleDebug`

iOS: `xcodegen generate --spec kalendar/ios/project.yml`, zatim `xcodebuild -project kalendar/ios/RasporedUniversalCalendar.xcodeproj -scheme Raspored_app -sdk iphonesimulator CODE_SIGNING_ALLOWED=NO build`

CI radi zasebne Android/iOS provjere i slike stvarnih simulatora. APK/AAB paketi su razvojni, a iOS uređajski arhivi nepotpisani. Vizualno podudaranje na svim veličinama zaslona zahtijeva stvarnu usporedbu snimki.
