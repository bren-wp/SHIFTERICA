# QA report — Raspored 1.3.0

## Status

**CI validacija je obvezna prije izdanja.**

## U ovoj iteraciji

1. Dnevna smjena D postavljena je na 07:00–19:00 i računa 12 sati.
2. Noćna smjena N postavljena je na 19:00–07:00 i računa 12 sati.
3. Jutarnja smjena J postavljena je na 07:00–15:00 i računa 8 sati.
4. GO i BO priznaju 8 sati na radni dan.
5. Prazna ćelija na hrvatskom državnom blagdanu koji pada na radni dan priznaje 8 sati.
6. Dodan je obračun odrađenih, redovnih, fonda, prekovremenih, plaćenih odsutnosti i ukupno priznatih sati.
7. Dodani su automatizirani Android testovi za trajanje smjena, blagdan i prekovremeni rad.
8. Aplikacija se otvara na trenutačnom mjesecu.
9. Godišnji pregled ostaje poredan siječanj–prosinac, ali se pri otvaranju pomiče na trenutačni mjesec.
10. Korisnik može promijeniti boju kockice i teksta za N, D, J, GO i BO.
11. Uklonjeni su produkcijski demo podaci iz rasporeda.
12. Dodana je dobrovoljna podrška kroz Google Play Billing i StoreKit bez otključavanja funkcija.
13. Veliki Kotlin/Swift UI i model fajlovi razdvojeni su u manje tematske cjeline.
14. README više ne učitava badge slike s vanjskog CDN-a; svi vizualni asseti koje prikazuje nalaze se u repozitoriju.
15. Hrvatski tekstovi i nazivi ostaju obvezni na obje platforme.

## Obvezna CI provjera

- Android unit testovi
- Android lint
- Android debug APK build
- Android AAB build
- APK/AAB artifact verify
- iOS XcodeGen
- iOS Simulator build bez code-signinga
- iOS device archive bez code-signinga
- unsigned IPA pakiranje
- dead-code hygiene provjera

Izdanje 1.3.0 smatra se provjerenim tek kada Android i iOS CI završe zeleno.

## Obračun kontrolne vrijednosti

Za kontrolni primjer s 17 smjena D/N u listopadu 2026.:

- 17 × 12 h = 204 odrađena sata
- fond za 22 radna dana = 176 sati
- redovni sati = 176 sati
- prekovremeni sati = 28 sati

Ove vrijednosti pokriva automatizirani test.

## 1:1 UI/UX provjera 1.3.0

- header i primarne akcije
- glavni tabovi
- mjesečni kalendar i navigacija
- smjenske ćelije i vikendi
- legenda i donja alatna traka
- način uređivanja
- izbornik Smjene
- uređivanje boja ugrađenih smjena
- Nova smjena
- Godina
- Sažetak i obračun sati
- Postavke i prekidači
- dobrovoljna podrška
- Android launcher ikona
- iOS App Icon

## Distribucijski artefakti 1.3.0

CI mora provjeriti i objaviti:

- Android APK
- Android AAB
- iOS Simulator .app ZIP
- unsigned iOS .xcarchive ZIP
- unsigned IPA

GitHub Release smije se objaviti tek nakon uspješnih Android i iOS jobova.
