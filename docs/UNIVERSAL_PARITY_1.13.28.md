# SHIFTERICA / Raspored_app — 1.13.28 vizualni QA

## Referenca

Izvorni `Raspored` ostaje vizualni i UX predložak. Zasebni `Raspored_app` mora odgovarati izvornom obliku zaglavlja, kalendarskih pločica, donje navigacije i modala, **bez plaća, satnica, poreza i obračuna**.

## Provjerena vizualna regresija iz izdanja 1.13.27

Iz stvarnih CI artefakata izdvojene su snimke originalnog `Raspored` (`month.png`, `raspored-real-screenshots`, ID 11925268351) i samostalnog kalendara (`calendar.png`, `raspored-universal-real-screenshot`, ID 11925129269) s CI runa 38629674004. Obje imaju 1206 × 2622 px. Na samostalnom kalendaru potvrđen je veliki prazni razmak između dna mreže dana i donje kontrole.

Uzrok je bilo ograničenje visine ćelije na 57 pt u `CalendarParityMonthGrid`; uklonjeno je samo za mjesečni prikaz, a tjedni prikaz ostaje namjerno kompaktan. Obje samostalne platforme imaju raspored dvije gornje statističke kartice i jedne široke donje kartice, kao originalna aplikacija, ali koriste samo kalendarske brojače.

## Automatizirane provjere

- [x] CI sadrži snimanje stvarne aplikacije u iOS Simulatoru na ekranima **Mjesec, Godina, Sažetak i Postavke**.
- [x] Snimanje koristi DEBUG parametre za početni zaslon; produkcijska aplikacija ih ne sadrži.
- [x] Sve četiri slike zahtijevaju valjanu PNG strukturu, realne dimenzije i dovoljnu veličinu.
- [x] Izvorne brze smjene N/D/J/P/GO/BO ostaju dostupne na Androidu i iOS-u.
- [x] Ponovni odabir identične smjene istoga dana ne mijenja postojeći zapis ni korisničku bilješku.
- [x] Android/iOS postavke koriste tamnu paletu izvornog Rasporeda i nazive kategorija Vizualno i Podrška i privatnost.

## Dodatna vizualna i navigacijska usklađivanja

Godišnji prikaz na obje platforme sada ima prethodnu i sljedeću godinu uz centralnu godinu te stalnu legendu ugrađenih smjena N / D / GO ispod pomične mreže. Android godišnje pločice ponovno koriste iste kratice kao mjesečni prikaz i iOS. Unutar dnevnog modala postoji izravna radnja **Postavke**, jer gumb "SMJENE I POSTAVKE" mora voditi do obje funkcije.

## Preostala provjera za tvrdnju 1:1

- [ ] Usporediti stvarne snimke ekran po ekran s potvrđenim referentnim slikama originalnog Rasporeda, uključujući font, veličinu i razmake.
- [ ] Android snimke stvarnih ekrana na reprezentativnim rezolucijama.
- [ ] Ručna provjera svih modala i postavki, velikih fontova i malih ekrana.
- [ ] Test stvarnih dodira na fizičkim uređajima.

**Ne označavati puni 1:1 kao dovršen bez vizualne provjere.** Na svim platformama zadržati odvojenu pohranu podataka, JSON v1 backup i bez financijskih funkcija.
