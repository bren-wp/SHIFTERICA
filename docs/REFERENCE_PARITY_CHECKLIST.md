# SHIFTERICA / Raspored — 1:1 kontrolna lista

Ovaj dokument je obvezna kontrola prije svakog izdanja.

## Globalno
- [ ] Android i iOS imaju isti raspored elemenata, boje, nazive i semantiku.
- [ ] Hrvatski jezik je gramatički ispravan i nema srpskih oblika.
- [ ] Tamna pozadina, staklaste kartice i cijan naglasak odgovaraju referentnom vizualnom sustavu.
- [ ] Header sadrži ikonu, Raspored, pretraživanje, postavke i +.
- [ ] Aktivni segment ima cijan obrub i diskretno cijan ispunjenje.

## Mjesec
- [ ] Brojevi dana su u gornjem lijevom kutu.
- [ ] SUB i NED imaju ružičasto/crveno isticanje.
- [ ] D je cijan, N žut, GO zelen, J mint, BO ljubičast.
- [ ] Listopad 2026. ima 8 D i 9 N = 17 smjena.
- [ ] Prethodni mjesec prikazuje 28 = D i 29 = N kada su vanjski dani uključeni.
- [ ] Kartica Vrste smjena i donja alatna traka prate referencu.

## Način uređivanja
- [ ] Naslov: Način uređivanja.
- [ ] Opis: Dodirnite dan kako biste primijenili smjenu.
- [ ] Akcija: Izađi iz uređivanja.
- [ ] Gumica + N/D/GO/J/BO i vlastite smjene rade odmah na dodir.

## Smjene / Nova smjena
- [ ] Modal Smjene ima Nova smjena i Uvezi smjenu.
- [ ] Svaka smjena prikazuje oznaku, naziv, vrijeme i chevron.
- [ ] Nova smjena ima Naziv smjene, Izgled/Raspored, Skraćenicu, boje, veličinu i intervale.
- [ ] Odustani i Spremi rade i imaju isti položaj na obje platforme.

## Godina
- [ ] Dvije kolone mjeseci.
- [ ] Datum ostaje vidljiv i kada ćelija sadrži oznaku smjene.
- [ ] Legenda odgovara bojama smjena.

## Sažetak
- [ ] Segmenti Mjesec / Godina / Razdoblje.
- [ ] Pregled smjena: Smjena / Broj / Vrijeme / Uključeno.
- [ ] Listopad 2026.: 17 smjena, 110 h 0 min, prosjek 6 h 28 min.
- [ ] Pretraži smjene… i Prošle / Sve / Nadolazeće odgovaraju referenci.

## Postavke
- [ ] Vizualno, Jezik i vrijeme, Bilješke, Podrška i privatnost.
- [ ] Oblik dana prikazan je ikonama oblika, ne tekstualnim nazivima.
- [ ] Nema dodatnih kontrola koje ne postoje na referentnom ekranu.
- [ ] Sve postavke se trajno spremaju.

## QA
- [ ] Android unit testovi.
- [ ] Android debug build i APK verify.
- [ ] iOS XcodeGen.
- [ ] iOS Simulator build bez code-signinga.
- [ ] CI je zelen prije sljedećeg izdanja.
