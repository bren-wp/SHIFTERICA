# QA report — Raspored 1.1.0

## Status

**PASS za provjere dostupne u razvojnom okruženju.**

## Izvršene provjere

1. iOS Swift izvori prolaze parser provjeru.
2. Android Kotlin izvori nemaju poznate parser/sintaksne pogreške; puni Android type-check zahtijeva Android SDK i Gradle ovisnosti.
3. Verzija projekta je **1.1.0**.
4. Početni listopad 2026. sadrži isti D/N uzorak kao referentni ekran.
5. Sažetak početnog uzorka daje 9 noćnih + 8 dnevnih smjena, odnosno **17 smjena i 110 h**.
6. Android i iOS imaju lokalno spremanje rasporeda, postavki i vlastitih smjena.
7. Provjerene su glavne hrvatske oznake i dijakritički znakovi.
8. Godišnji pregled obuhvaća svih 12 mjeseci.
9. Postavke prvog dana tjedna i prikaza dana iz susjednih mjeseci povezane su s mjesečnim kalendarom.
10. U ovom prijenosu nisu generirane nove slike.

## Ograničenje okruženja

Za stvarni potpisani APK/AAB i App Store arhivu potrebni su Android SDK odnosno macOS/Xcode i odgovarajući signing identiteti.
