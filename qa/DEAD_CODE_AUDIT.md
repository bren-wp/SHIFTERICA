# SHIFTERICA / Raspored — full dead code audit

Datum audita: 7. listopada 2026.  
Izdanje: 1.8.0

## Obuhvat

Audit obuhvaća produkcijski Kotlin/Compose i Swift/SwiftUI kod, storeove, modele, obračun radnog vremena, build konfiguraciju, lokalne vizualne assete i release pipeline.

## Arhitekturno čišćenje

Veliki fajlovi razdvojeni su bez promjene javnog ponašanja:

### Android

- monolitni Sheets.kt zamijenjen je datotekama za Smjene, Novu smjenu, Pretraživanje i Postavke
- RasporedApp.kt razdvojen je na app root, header, app mark i splash
- MonthScreen.kt razdvojen je na stanje ekrana, kalendar i kontrole
- Settings komponente izdvojene su iz glavnog settings sheeta
- obračun radnog vremena nalazi se u zasebnom CroatianWorkTime modulu
- Google Play podrška nalazi se u zasebnom SupportDialog modulu

### iOS

- monolitni Sheets.swift razdvojen je na zasebne SwiftUI viewove
- Model.swift razdvojen je na temu, modele smjena, library store, schedule store, settings store i date support
- MonthView razdvojen je na stanje, kalendarske komponente i kontrole
- uređivanje boja ugrađenih smjena izdvojeno je u zaseban view
- obračun radnog vremena nalazi se u zasebnom WorkTime modulu
- StoreKit podrška nalazi se u zasebnom SupportView modulu

## Uklonjeno

- produkcijsko automatsko seedanje referentnog listopada 2026.
- vanjski README badgeovi sa shields.io
- duplicirani SwiftUI shadow modifier u segmentiranim postavkama
- stari monolitni Kotlin i Swift fajlovi nakon uspješnog razdvajanja

## Provjereno i zadržano

- MainActivity — Android entry point
- RasporedApp — iOS @main entry point
- ShiftLibraryError.errorDescription — implementacija LocalizedError protokola
- SwiftUI View i Compose @Composable entry pointovi
- ScheduleStore / ScheduleStoreIOS
- ShiftLibraryStore / ShiftLibraryIOS
- UISettingsStore / UISettingsStoreIOS
- CroatianWorkTime / CroatianWorkTimeIOS
- platform-specific Play Billing i StoreKit integracije

## Produkcijska higijena

CI mora odbiti produkcijski source koji sadrži:

- TODO
- FIXME
- HACK
- XXX

Android CI dodatno izvršava unit testove i lint.

## Funkcionalni audit

Provjereni tokovi i pravila:

- aplikacija otvara trenutačni mjesec
- godišnji prikaz zadržava redoslijed siječanj–prosinac i fokusira trenutačni mjesec
- D = 07:00–19:00 = 12 h
- N = 19:00–07:00 = 12 h
- J = 07:00–15:00 = 8 h
- P = 14:00–22:00 = 8 h
- GO/BO = 8 h na radni dan
- prazan državni blagdan na radni dan = 8 h
- fond, redovni i prekovremeni sati računaju se odvojeno
- boje N/D/J/P/GO/BO mogu se mijenjati i trajno spremiti
- vlastite smjene ostaju podržane
- pretraživanje, uređivanje i brisanje smjena ostaju povezani
- dobrovoljna podrška ne mijenja funkcionalnost aplikacije
- aplikacijski vizualni resursi ne ovise o CDN-u

## Dodatna provjera 1.4.0

- popodnevna smjena P postoji kao ugrađena definicija 14:00–22:00 i traje 8 sati
- N, D, P i J koriste isti prilagodljivi model vremena na Androidu i iOS-u
- GO i BO ostaju plaćene odsutnosti od 8 sati na radni dan
- prilagodbe ugrađenih smjena migriraju stare spremljene boje bez gubitka korisničkih postavki
- custom smjene mogu se uređivati bez promjene šifre i bez prekida veza s kalendarom
- svi sektori koriste isti raspored i obračunski mehanizam bez hardkodirane ustanove

## Zaključak

Nakon refaktora nema namjerno zadržanog potvrđenog mrtvog produkcijskog koda. Konačna potvrda izdanja ovisi o zelenom Android lint/test/build i iOS build pipelineu.


## Dodatna provjera 1.6.0

- uklonjene su korisničke postavke za sektor, bod / koeficijent i porezni grad
- obračunski profil postoji samo u internom Android/iOS payroll modulu
- osnovica je zaključana na 1.025,00 € za podržanu 2026. godinu
- koeficijent je zaključan na 1,25
- uklonjeni su neiskorišteni modeli i preset popisi nastali nakon zaključavanja profila
- README više ne tvrdi da korisnik mijenja parametre obračuna
- Android i iOS koriste isti nepromjenjivi profil bez platformskog odstupanja


## Dodatna provjera 1.7.0

- novi UI/UX polish ne uvodi paralelne ili napuštene komponente za Android/iOS
- promjene vizualnih tokena koriste postojeće zajedničke ekrane i ne uvode duplicirane teme
- unutarnje glass kartice u Postavkama koriste postojeće kontrole i postojeći persistent settings store
- referentne slike tretiraju se kao dizajnerski ulaz; demo vrijednosti sa slika nisu dodane u produkcijski raspored
- zadržani su kompaktni način uređivanja i uklanjanje kartice „Vrste smjena” iz mjesečnog prikaza
- README i dizajnerska dokumentacija usklađeni su s GPL-3.0 licencom i produkcijskim pravilima smjena


## Dodatna provjera 1.8.0

- glass/neon prikaz koristi postojeće komponente i modele smjena bez paralelnih kopija poslovne logike
- mjesečni i godišnji prikaz dijele iste izvorne boje smjena i lokalni schedule store
- godišnja legenda čita postojeći ShiftLibraryStore / ShiftLibraryIOS i ne uvodi zaseban katalog smjena
- Sažetak i Smjene koriste iste definicije boja i oznaka kao kalendar
- nisu dodani demo rasporedi, hardkodirani korisnički datumi ni referentne smjene u produkcijsku pohranu
- produkcijska pravila D/N/J/P/GO/BO ostaju nepromijenjena
