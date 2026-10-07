# SHIFTERICA / Raspored — full dead code audit

Datum audita: 7. listopada 2026.  
Izdanje: 1.3.0

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
- GO/BO = 8 h na radni dan
- prazan državni blagdan na radni dan = 8 h
- fond, redovni i prekovremeni sati računaju se odvojeno
- boje N/D/J/GO/BO mogu se mijenjati i trajno spremiti
- vlastite smjene ostaju podržane
- pretraživanje, uređivanje i brisanje smjena ostaju povezani
- dobrovoljna podrška ne mijenja funkcionalnost aplikacije
- aplikacijski vizualni resursi ne ovise o CDN-u

## Zaključak

Nakon refaktora nema namjerno zadržanog potvrđenog mrtvog produkcijskog koda. Konačna potvrda izdanja ovisi o zelenom Android lint/test/build i iOS build pipelineu.
