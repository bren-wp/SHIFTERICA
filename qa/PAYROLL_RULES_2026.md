# Procjena plaće — pravila i izvori za 2026.

Procjena je lokalna i orijentacijska. Ne zamjenjuje obračun poslodavca i ne šalje podatke na mrežu.

## Točnost evidencije i eurski centi (od v1.13.6)

- Obračun koristi stvarno spremljen početak i završetak svake D/N/J/P ili vlastite smjene. Izmjena vremena odmah utječe na noćne sate (22:00–06:00), subotu, nedjelju, blagdan i sate koji prelaze u sljedeći mjesec.
- Sama vremenska podudarnost s 14:00–22:00 **nije** dokaz rada u drugoj smjeni: dugački dnevni/noćni turnusi D/N ne dobivaju automatski dodatak druge smjene. P i stvarno definirane kratke popodnevne smjene mogu ga ostvariti; poslodavčeva organizacija rada ipak je mjerodavna.
- Svaka obračunska stavka te mirovinski doprinosi, porezna osnovica i porez zaokružuju se u eurocente, sukladno uobičajenom obračunu po stavkama. Jednaka je matematika implementirana na Androidu i iOS-u.
- Prikaz **Neto ≈** predstavlja procijenjenu neto plaću nakon zakonskih davanja, ali bez osobnih obustava. Ne pretpostavlja se dodavanje neoporezivih naknada, individualnih korekcija ili posebnih dodataka koji nisu potvrđeni rasporedom.
- Podaci iz ranije dostavljenih obračunskih isprava koriste se isključivo za **provjeru algoritma**; identifikacijski podaci, izvorni dokumenti i njihovi iznosi nisu dodani u javni izvorni kod.
- Korisnička vremena, rasporedi i ranije potvrđeni neto iznosi ostaju u lokalnoj pohrani i ne mijenjaju se nadogradnjom.

### Službena referentna pravila

- [NN 11/2026 — osnovica javnih službi](https://narodne-novine.nn.hr/clanci/sluzbeni/full/2026_01_11_86.html)
- [NN 149/2025 — porezne stope Grada Rijeke](https://narodne-novine.nn.hr/clanci/sluzbeni/2025_12_149_2229.html)
- [NN 29/2024 — čl. 59. Temeljnog kolektivnog ugovora](https://narodne-novine.nn.hr/clanci/sluzbeni/full/2024_03_29_458.html)
- [Službeno tumačenje čl. 59. — uvjeti rada u smjenama](https://mrosp.gov.hr/najcesca-pitanja-i-odgovori-12153/rad-i-zaposljavanje/zajednicko-povjerenstvo-za-tumacenje-temeljnog-kolektivnog-ugovora-za-zaposlenike-u-javnim-sluzbama-od-1-ozujka-2024/clanak-59-13439/13439)

## Ugrađeni profil

Parametri obračuna zaključani su u aplikaciji i nisu dostupni kao korisničke postavke:

- profil: bolnica / javno zdravstvo
- osnovica se određuje prema mjesecu obračuna:
  - siječanj–ožujak 2026.: 1.004,87 €
  - travanj–srpanj 2026.: 1.015,00 €
  - kolovoz–studeni 2026.: 1.025,00 €
  - prosinac 2026.: 1.035,00 €
- bod / koeficijent: 1,25
- porezni grad: Rijeka
- niža stopa: 20%
- viša stopa: 25%
- osobni odbitak bez dodatnih osobnih podataka: 600 € mjesečno

Sati i parametri obračuna ne upisuju se ručno. Sati se preuzimaju iz mjesečnog rasporeda, a ugrađeni bolnički profil koristi se interno na Androidu i iOS-u. Osnovice za 2025. i 2026. prate službene odluke te su dodatno uspoređene s dostavljenim obračunskim ispravama.

## Dodaci

Temeljni kolektivni ugovor za zaposlenike u javnim službama, NN 29/2024, propisuje:

- prekovremeni 50%
- noćni rad 22:00–06:00 40%
- druga smjena 10%
- subota 25%
- nedjelja 50%
- blagdan / zakonski neradni dan 150%

Službeno tumačenje članka 109. TKU-a potvrđuje 5% za turnus 12–24–12–48 u javnim službama kada je to povoljnije za zaposlenika.

Dodatak IV. Kolektivnom ugovoru za državne službenike i namještenike, NN 4/2025, povećava noćni dodatak državnih službenika na 50%.

Bolnički profil aplikacije koristi 50% noćnog dodatka jer je to potvrđeno na anonimiziranim stvarnim obračunskim ispravama zdravstvenog sustava dostavljenima za razvoj. Opći profil Javna služba ostaje na 40% prema TKU-u.

## Rijeka i porez na dohodak

Izvor: Odluka o visini poreznih stopa godišnjeg poreza na dohodak na području Grada Rijeke, NN 149/2025.

Od 1. siječnja 2026.:

- niža stopa 20%
- viša stopa 25%
- viša stopa primjenjuje se iznad godišnje osnovice od 60.000 €, odnosno u mjesečnoj procjeni iznad 5.000 €.

## Osobni odbitak i djeca

Izvor: Zakon o izmjenama i dopunama Zakona o porezu na dohodak, NN 152/2024.

Osnovni osobni odbitak iznosi 600 € mjesečno. Obračunski modul podržava dodatna uvećanja za uzdržavane članove i djecu, ali ona nisu obvezan korisnički unos u automatskom načinu.

## Mladi do 30 godina

Izvor: Pravilnik o izmjenama i dopunama Pravilnika o porezu na dohodak, NN 16/2025.

Za porezno razdoblje 2026.:

- godišta 1996.–2000.: 50% umanjenja pripadajućeg godišnjeg poreza u nižem poreznom razredu
- godišta 2001. i mlađi: 100% umanjenja pripadajućeg godišnjeg poreza u nižem poreznom razredu

To je godišnje umanjenje. Aplikacija ga ne prikazuje kao zajamčeno mjesečno povećanje neta ako nema podatak o godini rođenja.

## Granice procjene

Bez osobnih podataka aplikacija ne može znati:

- broj djece na poreznoj kartici
- uzdržavane članove
- godinu rođenja
- navršeni radni staž
- posebne dodatke pojedinog radnog mjesta ili ustanove

Zato je zadani izračun namjerno konzervativan i koristi samo podatke koje aplikacija pouzdano ima: raspored te ugrađene službene i lokalne obračunske parametre.
