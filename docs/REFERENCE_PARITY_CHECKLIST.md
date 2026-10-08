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
- [x] Kartica „Vrste smjena” nije prikazana ispod mjesečnog kalendara; kalendar zadržava maksimalnu visinu, a upravljanje je u kompaktnoj alatnoj traci.

## Unos smjene iz kalendara
- [x] Dodir na datum izravno otvara birač smjene.
- [x] D/N/J/P/GO/BO i vlastite smjene mogu se odabrati bez posebnog načina uređivanja.
- [x] Odabir se odmah sprema na odabrani datum.
- [x] Brisanje je zasebna radnja.
- [x] Stari gumb **UREDI RASPORED** i zasebni način uređivanja nisu dio produkcijskog toka.

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
- [x] Sažetak je razdvojen na **Smjene / Sati / Plaća**.
- [x] Pregled smjena prikazuje Smjena / Broj / Vrijeme / Uključeno.
- [x] Obračun sati je zaseban prikaz.
- [x] Procjena plaće je zaseban prikaz.
- [x] Mjesec i navigacija mjeseca zajednički su svim prikazima.

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


## 1.9.0 new shift + splash polish

- [x] Iznad kalendara prikazani su Fond sati / Prekovremeni / Plaća.
- [x] Dodir na datum otvara izravni birač smjene na obje platforme.
- [x] Sažetak je razdvojen na Smjene / Sati / Plaća.

- [x] Nova smjena koristi jednake glass kartice, aktivne tabove i akcijske gumbe na Androidu i iOS-u.
- [x] Birači boje i kontrola veličine teksta imaju isti vizualni prioritet na obje platforme.
- [x] Vremenska polja imaju jasna fokusna stanja i ostaju čitljiva na tamnoj podlozi.
- [x] Android forma ostaje dostupna iznad softverske tipkovnice.
- [x] iOS forma koristi veliki sheet i interaktivno zatvaranje tipkovnice.
- [x] Modal Smjene ima isti prioritet Nova smjena / Uvezi smjenu i usklađen close gumb.
- [x] Splash koristi isti halo, slojevite pločice i progress tretman na Androidu i iOS-u.
- [x] Nisu dodani novi vanjski vizualni resursi ni demo rasporedi.

## 1.8.0 calendar glass polish

- [x] Mjesečne N/D/J/P/GO/BO ćelije imaju usklađen slojeviti glass/neon prikaz na Androidu i iOS-u.
- [x] Godišnje ćelije koriste isti vizualni smjer kao mjesečni kalendar.
- [x] Godišnji pregled prikazuje kompaktnu horizontalnu legendu smjena.
- [x] Mjesečni pregled ostaje bez zasebne kartice „Vrste smjena”.
- [x] Sažetak i Smjene koriste isti tretman oznaka smjena kao kalendar.
- [x] Referentni demo podaci nisu uneseni u produkcijski raspored.

## 1.7.0 referentni polish

- [x] Nove dostavljene slike koriste se kao vizualna referenca, ne kao izvor demo rasporeda ili vremena.
- [x] Tamni glass vizualni sustav, cijan aktivna stanja i kontrast kartica usklađeni su na Androidu i iOS-u.
- [x] Postavke koriste jasne unutarnje kartice/redove na obje platforme.
- [x] Mjesečni prikaz ostaje bez dodatne kartice „Vrste smjena”.
- [x] Produkcijska vremena ostaju D 07:00–19:00, N 19:00–07:00, J 07:00–15:00 i P 14:00–22:00.

## 1.2.4 završni polish

- [x] Smjene modal, Nova smjena, Pretraživanje i Postavke imaju isti vizualni jezik na Androidu i iOS-u.
- [x] Aktivni izbori imaju cijan glow, a kartice konzistentnu dubinu.
- [x] Postavka jezika je interaktivna na obje platforme.
- [x] Podrška i privatnost imaju funkcionalne akcije.
- [x] Full dead code audit dokumentiran je u `qa/DEAD_CODE_AUDIT.md`.
- [x] Android lint i source hygiene provjere dio su CI-ja.
