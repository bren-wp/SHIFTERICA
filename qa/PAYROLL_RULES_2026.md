# Procjena plaće — pravila i izvori za 2026.

Procjena je lokalna i orijentacijska. Ne zamjenjuje obračun poslodavca i ne šalje podatke na mrežu.

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
