# SHIFTERICA / Raspored — full dead code audit

Datum audita: 7. listopada 2026.  
Izdanje: 1.2.4

## Obuhvat

Audit obuhvaća sav produkcijski Kotlin/Compose i Swift/SwiftUI kod u:

- `android/app/src/main/java`
- `ios/Raspored`
- build i release konfiguraciju
- modele, storeove i UI helper funkcije

## Rezultat

### Uklonjeno

- `ShiftLibraryStore.exportJson()` — nije imao nijednog pozivatelja ni UI ulaznu točku.
- stari neinteraktivni iOS helper `staticRow` zamijenjen je stvarnim interaktivnim redovima.
- neaktivni Android jezični setter više nije mrtav kod: povezan je s izbornikom jezika u Postavkama.

### Provjereno i zadržano

- `MainActivity` — Android entry point; prirodno nema internog pozivatelja.
- `ShiftLibraryError.errorDescription` — implementacija protokola `LocalizedError`; koristi je sustav kroz `localizedDescription`.
- SwiftUI `View` strukture i Compose `@Composable` entry pointovi pozivaju se kroz view hijerarhiju i nisu mrtav kod.
- storeovi rasporeda, smjena i UI postavki imaju aktivne čitatelje i pisatelje.
- modeli smjena koriste se u mjesecu, godini, sažetku, uređivanju i upravitelju smjena.

### Produkcijska higijena

U produkcijskom source treeju nema oznaka:

- `TODO`
- `FIXME`
- `HACK`
- `XXX`

CI to sada provjerava na svakom pushu i pull requestu. Android CI dodatno izvršava `lintDebug`.

## UX funkcionalni audit

Provjereni tokovi:

- Mjesec → prethodni/sljedeći mjesec
- Godina → promjena godine → otvaranje mjeseca
- Sažetak → mjesec / godina / razdoblje
- Pretraživanje → odabir rezultata → povratak na mjesec
- Smjene → nova smjena
- Smjene → uvoz JSON-a
- Smjene → brisanje vlastite smjene
- Način uređivanja → N / D / GO / J / BO / gumica
- Nova smjena → Izgled / Raspored → Spremi
- Postavke → trajno spremanje vrijednosti
- Postavke → izbor jezika
- Podrška i privatnost → funkcionalne informativne akcije

## Zaključak

Nakon ovog audita nema potvrđenog mrtvog produkcijskog koda. Svaki budući kandidat mora ili dobiti stvarnog pozivatelja/test ili biti uklonjen prije izdanja.
