# SHIFTERICA / Raspored_app — 1.13.28 vizualni QA

## Referenca

Izvorni `Raspored` ostaje vizualni i UX predložak. Zasebni `Raspored_app` mora odgovarati izvornom obliku zaglavlja, kalendarskih pločica, donje navigacije i modala, **bez plaća, satnica, poreza i obračuna**.

## Automatizirane provjere

- [x] CI sadrži snimanje stvarne aplikacije u iOS Simulatoru na ekranima **Mjesec, Godina, Sažetak i Postavke**.
- [x] Snimanje koristi DEBUG parametre za početni zaslon; produkcijska aplikacija ih ne sadrži.
- [x] Sve četiri slike zahtijevaju valjanu PNG strukturu, realne dimenzije i dovoljnu veličinu.
- [x] Izvorne brze smjene N/D/J/P/GO/BO ostaju dostupne na Androidu i iOS-u.
- [x] Ponovni odabir identične smjene istoga dana ne mijenja postojeći zapis ni korisničku bilješku.
- [x] Android/iOS postavke koriste tamnu paletu izvornog Rasporeda i nazive kategorija Vizualno i Podrška i privatnost.

## Preostala provjera za tvrdnju 1:1

- [ ] Usporediti stvarne snimke ekran po ekran s potvrđenim referentnim slikama originalnog Rasporeda, uključujući font, veličinu i razmake.
- [ ] Android snimke stvarnih ekrana na reprezentativnim rezolucijama.
- [ ] Ručna provjera svih modala i postavki, velikih fontova i malih ekrana.
- [ ] Test stvarnih dodira na fizičkim uređajima.

**Ne označavati puni 1:1 kao dovršen bez vizualne provjere.** Na svim platformama zadržati odvojenu pohranu podataka, JSON v1 backup i bez financijskih funkcija.
