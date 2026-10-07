# Raspored — 1:1 dizajnerski sustav

Ovaj dokument je produkcijska referenca za Android i iOS. Cilj je da obje aplikacije izgledaju i ponašaju se jednako, uz platformsku prilagodbu samo kada je nužna za pristupačnost ili sistemski API.

## Identitet

- Naziv aplikacije: **Raspored**
- Podnaslov na splashu: **Pametni planer smjena**
- Primarni vizualni smjer: tamno plavo/crno sučelje, staklasti paneli, cijan/tirkizni naglasak.
- Primarna akcentna boja: `#19DCE0`
- Sekundarna akcentna boja: `#08A8F0`
- Pozadina: `#051522`
- Sekundarna pozadina: `#0A263B`
- Kartica: `#112B41`
- Sekundarna kartica: `#102E46`
- Obrub: `#347291`
- Tekst: `#F6F8FB`
- Sekundarni tekst: `#AFC1D8`
- Vikend: `#FF7186`

## Slojevite smjenske pločice

Smjenske pločice na mjesecu, godini, Sažetku i ekranu Smjene koriste isti vizualni sustav:

- izvorna boja smjene ostaje primarna boja pločice
- gornji dio je punije zasićen, donji dio blago proziran radi glass dubine
- aktivna smjena ima jasniji obrub u svojoj boji i kontroliranu sjenu
- sjaj ne smije smanjiti čitljivost broja dana ili oznake smjene
- dani izvan aktivnog mjeseca ostaju vizualno utišani
- demo vrijednosti sa referentnih slika nisu izvor produkcijskih podataka

Godišnji prikaz smije imati kompaktnu horizontalnu legendu smjena. Mjesečni prikaz ne vraća zasebnu karticu „Vrste smjena” ispod kalendara.

## Boje smjena

| Oznaka | Naziv | Boja |
|---|---|---|
| N | Noćna | `#FFD21F` |
| D | Dnevna | `#13B7F3` |
| GO | Godišnji | `#6CEB82` |
| J | Jutarnja | `#77DED7` |
| P | Popodnevna | `#FF8A3D` |
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

Dostavljene slike služe kao **vizualna referenca** za proporcije, staklaste kartice, kontrast i aktivna stanja. Demo raspored prikazan na referentnim slikama ne smije se automatski unositi u produkcijsku aplikaciju.

Produkcijska pravila ugrađenih smjena ostaju zaključana:
- D = 07:00–19:00 = 12 h
- N = 19:00–07:00 = 12 h
- J = 07:00–15:00 = 8 h
- P = 14:00–22:00 = 8 h
- GO / BO = 8 h na radni dan

## Brza alatna traka

Kada način uređivanja nije otvoren:
- veliki gumb **UREDI RASPORED**
- zasebna akcija **Više / Smjene**

Kada je uređivanje uključeno, kompaktna traka sadrži:
- Gumicu
- N
- D
- J
- P
- GO
- BO
- zasebnu akciju završetka uređivanja

Kalendar mora zadržati maksimalnu raspoloživu visinu; ne vraćati zasebnu karticu „Vrste smjena” ispod mjesečnog kalendara.

## Način uređivanja

U načinu pregleda kalendar je zaštićen od slučajnih izmjena. Uređivanje se uključuje eksplicitno.

Odabrana smjena upisuje se jednim dodirom. Gumica je zasebna radnja za brisanje, a ponovni dodir iste smjene ne briše postojeći podatak.

Ugrađene N/D/J/P/GO/BO ostaju dostupne u kompaktnoj traci. Vlastitim smjenama upravlja se kroz ekran **Smjene**.

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

Sažetak se uvijek računa iz stvarnog lokalnog rasporeda korisnika. Vrijednosti sa vizualnih referenci nisu produkcijski seed niti očekivani fiksni rezultat.

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
