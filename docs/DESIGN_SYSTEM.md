# Raspored — 1:1 dizajnerski sustav

Ovaj dokument je produkcijska referenca za Android i iOS. Cilj je da obje aplikacije izgledaju i ponašaju se jednako, uz platformsku prilagodbu samo kada je nužna za pristupačnost ili sistemski API.

## Identitet

- Naziv aplikacije: **Raspored**
- Podnaslov na splashu: **Pametni planer smjena**
- Primarni vizualni smjer: tamno plavo/crno sučelje, staklasti paneli, cijan/tirkizni naglasak.
- Primarna akcentna boja: `#19DCE0`
- Sekundarna akcentna boja: `#08A8F0`
- Pozadina: `#061624`
- Sekundarna pozadina: `#0A2235`
- Kartica: `#12293D`
- Sekundarna kartica: `#102A40`
- Obrub: `#2A5D7D`
- Tekst: `#F6F8FB`
- Sekundarni tekst: `#AFC1D8`
- Vikend: `#FF7186`

## Boje smjena

| Oznaka | Naziv | Boja |
|---|---|---|
| N | Noćna | `#FFD21F` |
| D | Dnevna | `#13B7F3` |
| GO | Godišnji | `#6CEB82` |
| J | Jutarnja | `#77DED7` |
| BO | Bolovanje | `#D991EE` |

## Globalni header

Header mora sadržavati:
1. ikonu aplikacije,
2. naziv **Raspored**,
3. pretraživanje,
4. postavke/filter,
5. veliki gumb **+**.

Ispod headera je trodijelna navigacija:
- **LISTOPAD** / naziv trenutačnog mjeseca,
- godina, npr. **2026**,
- **SAŽETAK**.

Aktivni segment ima cijan obrub i diskretno cijan ispunjenje.

## Mjesečni prikaz

Naslov kartice: **LISTOPAD 2026**.

Redoslijed dana:
**PON UTO SRI ČET PET SUB NED**.

Subota i nedjelja koriste ružičasto/crveno isticanje kada je opcija uključena.

Referentni raspored za listopad 2026.:

- D: 2, 6, 10, 14, 18, 22, 26, 30
- N: 3, 7, 11, 15, 19, 23, 27, 28, 31
- prethodni mjesec: 28 = D, 29 = N

To daje 8 dnevnih + 9 noćnih = **17 smjena**.

## Brza alatna traka

Kada način uređivanja nije otvoren:
- Gumica
- N
- D
- GO
- J
- BO
- Više

Vlastite smjene dodaju se u horizontalni popis bez uklanjanja ugrađenih smjena.

## Način uređivanja

Naslov: **Način uređivanja**

Opis: **Dodirnite dan kako biste primijenili smjenu**

Akcija: **Izađi iz uređivanja**

Odabir smjene odmah mijenja dan. Gumica briše oznaku.

## Vrste smjena

Kartica **Vrste smjena** prikazuje:
- Noćna
- Dnevna
- Godišnji
- Jutarnja
- Bolovanje
- sve vlastite smjene korisnika

## Godišnji pregled

Godina se prikazuje u dvije kolone mjeseci. Svi mjeseci moraju biti dostupni skrolanjem.

Nazivi mjeseci koriste hrvatski:
SIJEČANJ, VELJAČA, OŽUJAK, TRAVANJ, SVIBANJ, LIPANJ, SRPANJ, KOLOVOZ, RUJAN, LISTOPAD, STUDENI, PROSINAC.

## Sažetak

Segmenti:
- Mjesec
- Godina
- Razdoblje

Kartica **Pregled smjena**:
- Smjena
- Broj
- Vrijeme
- Uključeno

Referentni listopad 2026.:
- Noćna: 9, 54 h 0 min
- Dnevna: 8, 56 h 0 min
- Ukupno smjena: 17
- Ukupno sati: 110 h 0 min
- Prosjek po smjeni: 6 h 28 min

## Smjene

Modal **Smjene** mora imati:
- **Nova smjena**
- **Uvezi smjenu**
- popis smjena s bojom, oznakom, nazivom i vremenom
- vlastite smjene moraju biti moguće obrisati

## Nova smjena

Kartice:
- Naziv smjene
- Izgled / Raspored
- Skraćenica
- Boja pozadine
- Boja teksta
- Veličina teksta
- jedan ili dva vremenska intervala

Akcije:
- **Odustani**
- **Spremi**

## Postavke

Sekcije:
- Vizualno
- Jezik i vrijeme
- Bilješke
- Podrška i privatnost

Sve postavke moraju se stvarno spremati, a ne biti samo dekorativne.

## Hrvatski jezik

Zabranjeni su srpski oblici poput:
- mesec
- podešavanja
- beleške
- nedelja u značenju tjedna

Koristiti:
- mjesec
- postavke
- bilješke
- tjedan
- sažetak
- smjena
- godišnji odmor
- pravila privatnosti

## Android / iOS paritet

Svaka funkcija dodana na Android mora postojati i na iOS-u, i obrnuto, osim ako je eksplicitno označena kao platformski specifična.

Glavni tokovi moraju imati jednak raspored elemenata, terminologiju, boje i semantiku dodira.
