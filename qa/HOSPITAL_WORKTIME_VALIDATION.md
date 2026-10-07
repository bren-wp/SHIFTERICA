# Validacija obračuna radnog vremena — anonimizirani bolnički uzorci

Ovaj dokument sadrži isključivo anonimizirane kontrolne vrijednosti potrebne za provjeru algoritma. Imena, OIB-i, IBAN-i, obustave, krediti, iznosi isplate i drugi osobni podaci nisu pohranjeni u repozitoriju.

## Kontrolne vrijednosti mjesečnog fonda

Dostavljene obračunske isprave potvrđuju da se redovni mjesečni fond računa kao 8 sati za svaki ponedjeljak–petak u kalendarskom mjesecu.

| Mjesec | Redovni mjesečni fond |
|---|---:|
| prosinac 2024. | 176 h |
| siječanj 2025. | 184 h |
| veljača 2025. | 160 h |
| ožujak 2025. | 168 h |
| svibanj 2025. | 176 h |
| lipanj 2025. | 168 h |
| srpanj 2025. | 184 h |
| kolovoz 2025. | 168 h |
| listopad 2025. | 184 h |
| lipanj 2026. | 176 h |
| srpanj 2026. | 184 h |
| kolovoz 2026. | 168 h |

Android unit testovi provjeravaju cijelu ovu matricu.

## Blagdani i prazne ćelije

Državni blagdan koji pada na radni dan ostaje dio mjesečnog fonda. Ako na tom danu nema evidentirane radne smjene ili druge plaćene odsutnosti, aplikacija priznaje 8 sati naknade za blagdan.

Ako je na blagdan evidentirana stvarna smjena, nema automatskog dodatnog kredita od 8 sati za isti dan; stvarno odrađeno vrijeme ostaje dio radnih sati i zasebno se označava kao rad na blagdan.

## Kontrolne vrijednosti fonda i prekovremenih

Dostavljeni uzorci potvrđuju i odnos ukupnih priznatih sati prema fondu:

| Mjesec | Fond | Ukupni obračunski sati | Prekovremeni |
|---|---:|---:|---:|
| ožujak 2025. | 168 h | 180 h | 12 h |
| svibanj 2025. | 176 h | 180 h | 4 h |
| srpanj 2025. | 184 h | 192 h | 8 h |
| kolovoz 2025. | 168 h | 176 h | 8 h |
| listopad 2025. | 184 h | 199 h | 15 h |
| lipanj 2026. | 176 h | 181 h | 5 h |
| srpanj 2026. | 184 h | 216 h | 32 h |
| kolovoz 2026. | 168 h | 211 h | 43 h |

Prekovremeni se zato ne računa prema broju smjena nego prema ukupnim priznatim minutama iznad raspoloživog redovnog fonda.

## Plaćene odsutnosti

GO, BO i druge priznate odsutnosti moraju sudjelovati u mjesečnom fondu, ali se ne smiju brojati kao stvarno odrađene minute.

Primjer iz anonimiziranog uzorka za lipanj 2025.:

- fond 168 h
- godišnji odmor 80 h
- bolovanje 88 h
- ukupno priznato 168 h

Time se potvrđuje da plaćene odsutnosti popunjavaju fond bez stvaranja lažnih radnih sati.

## Rad preko ponoći

Noćna smjena N traje 19:00–07:00. Za detaljnu evidenciju svaka minuta pripada stvarnom kalendarskom datumu na kojem je odrađena. To je nužno za ispravan obračun:

- noćnog rada 22:00–06:00
- rada subotom
- rada nedjeljom
- rada na blagdan
- prijelaza između mjeseci

Primjer: N smjena započeta zadnjeg dana mjeseca u 19:00 doprinosi 5 sati tom mjesecu i 7 sati sljedećem mjesecu.

## Anonimizirane kontrolne stope dodataka

Stope ispod služe isključivo kao kontrolni podaci iz dostavljenih obračunskih isprava. Nisu ugrađene kao opći pravni tarifnik i ne ekstrapoliraju se izvan stvarno potvrđenog razdoblja.

| Razdoblje potvrđeno uzorcima | Noć | Subota | Nedjelja | Blagdan | Druga smjena | Prekovremeni | Turnus |
|---|---:|---:|---:|---:|---:|---:|---:|
| prosinac 2024. | 40% | 25% | 50% | 150% | 10% | 50% | nije potvrđeno |
| siječanj–travanj 2025. | 50% | 25% | 50% | 150% | 10% | 50% | nije potvrđeno dostavljenim uzorcima |
| svibanj 2025.–kolovoz 2026. | 50% | 25% | 50% | 150% | 10% | 50% | 5% |

Kod zato vraća `null` za stopu koja nije potvrđena dostavljenim uzorcima umjesto da nagađa.

## Kategorije u sažetku

Android i iOS iz istog rasporeda izračunavaju:

- odrađene sate
- redovne sate
- mjesečni fond
- prekovremene sate
- plaćene odsutnosti
- ukupno priznate sate
- dnevne sate
- noćne sate 22:00–06:00
- sate subotom
- sate nedjeljom
- sate rada na blagdan
- sate u rasponu 14:00–22:00

Financijski iznos plaće nije izveden samo iz rasporeda jer pojedine bolničke kategorije, osobito turnus, mogu zahtijevati dodatnu klasifikaciju koja se iz same oznake D/N/J ne može uvijek sigurno zaključiti. Aplikacija zato ne izmišlja takvu klasifikaciju.
