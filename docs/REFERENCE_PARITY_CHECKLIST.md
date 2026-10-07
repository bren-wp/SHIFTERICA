# SHIFTERICA / Raspored — 1:1 kontrolna lista

Ovaj dokument je obvezna kontrola prije svakog izdanja.

## Globalno
- [x] Android i iOS imaju isti raspored elemenata, boje, nazive i semantiku.
- [x] Hrvatski jezik je gramatički ispravan i nema srpskih oblika.
- [x] Tamna pozadina, staklaste kartice i cijan naglasak odgovaraju referentnom vizualnom sustavu.
- [x] Header sadrži ikonu, Raspored, pretraživanje, postavke i +.
- [x] Aktivni segment ima cijan obrub i diskretno cijan ispunjenje.

## Mjesec
- [x] Brojevi dana su u gornjem lijevom kutu.
- [x] SUB i NED imaju ružičasto/crveno isticanje.
- [x] D je cijan, N žut, GO zelen, J mint, BO ljubičast.
- [x] Listopad 2026. ima 8 D i 9 N = 17 smjena.
- [x] Prethodni mjesec prikazuje 28 = D i 29 = N kada su vanjski dani uključeni.
- [x] Kartica Vrste smjena i donja alatna traka prate referencu.

## Način uređivanja
- [x] Naslov: Način uređivanja.
- [x] Opis: Dodirnite dan kako biste primijenili smjenu.
- [x] Akcija: Izađi iz uređivanja.
- [x] Gumica + N/D/GO/J/BO i vlastite smjene rade odmah na dodir.

## Smjene / Nova smjena
- [x] Modal Smjene ima Nova smjena i Uvezi smjenu.
- [x] Svaka smjena prikazuje oznaku, naziv, vrijeme i chevron.
- [x] Nova smjena ima Naziv smjene, Izgled/Raspored, Skraćenicu, boje, veličinu i intervale.
- [x] Odustani i Spremi rade i imaju isti položaj na obje platforme.

## Godina
- [x] Dvije kolone mjeseci.
- [x] Datum ostaje vidljiv i kada ćelija sadrži oznaku smjene.
- [x] Legenda odgovara bojama smjena.

## Sažetak
- [x] Segmenti Mjesec / Godina / Razdoblje.
- [x] Pregled smjena: Smjena / Broj / Vrijeme / Uključeno.
- [x] Listopad 2026.: 17 smjena, 110 h 0 min, prosjek 6 h 28 min.
- [x] Pretraži smjene… i Prošle / Sve / Nadolazeće odgovaraju referenci.

## Postavke
- [x] Vizualno, Jezik i vrijeme, Bilješke, Podrška i privatnost.
- [x] Oblik dana prikazan je ikonama oblika, ne tekstualnim nazivima.
- [x] Nema dodatnih kontrola koje ne postoje na referentnom ekranu.
- [x] Sve postavke se trajno spremaju.

## QA
- [x] Android unit testovi.
- [x] Android debug build i APK verify.
- [x] iOS XcodeGen.
- [x] iOS Simulator build bez code-signinga.
- [x] CI je zelen prije sljedećeg izdanja.


## 1.2.4 završni polish

- [x] Smjene modal, Nova smjena, Pretraživanje i Postavke imaju isti vizualni jezik na Androidu i iOS-u.
- [x] Aktivni izbori imaju cijan glow, a kartice konzistentnu dubinu.
- [x] Postavka jezika je interaktivna na obje platforme.
- [x] Podrška i privatnost imaju funkcionalne akcije.
- [x] Full dead code audit dokumentiran je u `qa/DEAD_CODE_AUDIT.md`.
- [x] Android lint i source hygiene provjere dio su CI-ja.
