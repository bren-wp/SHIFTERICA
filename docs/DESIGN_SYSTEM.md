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

## Mjesečne metrike i unos smjene

Neposredno iznad mjesečnog kalendara nalazi se kompaktna traka s tri podatka:
- **Fond sati**
- **Prekovremeni**
- **Plaća** — orijentacijska procjena neta kada postoji dovoljno podataka

Dodir na datum otvara kontekstni birač smjene za taj datum. Birač mora nuditi:
- N, D, J, P, GO i BO
- sve vlastite smjene
- zasebnu akciju **Obriši**
- pristup upravljanju svim smjenama

Odabir smjene odmah se sprema. Nema zasebnog globalnog načina „Uredi raspored”, jer bi to dupliciralo isti tok i povećalo mogućnost pogreške.

Ispod kalendara ostaje samo kompaktna akcija za upravljanje smjenama. Kalendar ne vraća zasebnu karticu „Vrste smjena”.

## Godišnji pregled

Godina se prikazuje u dvije kolone mjeseci. Svi mjeseci moraju biti dostupni skrolanjem.

Nazivi mjeseci koriste hrvatski:
SIJEČANJ, VELJAČA, OŽUJAK, TRAVANJ, SVIBANJ, LIPANJ, SRPANJ, KOLOVOZ, RUJAN, LISTOPAD, STUDENI, PROSINAC.

## Sažetak

Sažetak je funkcionalno razdvojen na tri odvojena prikaza:
- **Smjene** — broj, trajanje i uključivanje pojedinih vrsta smjena
- **Sati** — fond, redovni, prekovremeni, odsutnosti i raspodjela odrađenih sati
- **Plaća** — automatska orijentacijska procjena iz stvarnog mjesečnog rasporeda

Navigacija mjeseca zajednička je sva tri prikaza. Sažetak se uvijek računa iz stvarnog lokalnog rasporeda korisnika.

## Smjene

Modal **Smjene** mora imati:
- **Nova smjena**
- **Uvezi smjenu**
- popis smjena s bojom, oznakom, nazivom i vremenom
- vlastite smjene moraju biti moguće obrisati

## Nova smjena

Forma **Nova smjena** mora se ponašati kao jedan konzistentan glass sheet:
- aktivni tab Izgled/Raspored ima cijan ispunjenje, obrub i diskretan glow
- birači boja koriste velike dodirne ciljeve i jasno ističu odabranu boju
- minus/plus kontrola veličine teksta mora ostati pogodna za dodir i na manjim ekranima
- vremenska polja imaju vidljivo fokusno stanje
- tipkovnica ne smije zakloniti akcije Odustani/Spremi
- Android i iOS moraju zadržati istu hijerarhiju i redoslijed elemenata

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

## Splash i branding

Splash koristi postojeći AppMark kao središnji identitet, s diskretnim cijan glass haloom, tamnom pozadinom i plutajućim smjenskim pločicama. Pločice koriste isti slojeviti tretman kao ostatak aplikacije, a progress indikator mora biti vizualno usklađen na Androidu i iOS-u.

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
