# Validacija obračuna radnog vremena — 2026

Ovaj dokument bilježi samo anonimizirane kontrolne vrijednosti korištene za provjeru algoritma. Osobni podaci, identifikatori, računi, iznosi isplate i drugi podaci zaposlenika nisu pohranjeni u repozitoriju.

## Kontrolne vrijednosti mjesečnog fonda

Tri stvarne obračunske isprave iz zdravstvenog sustava potvrđuju sljedeće redovne mjesečne fondove za 2026.:

| Mjesec | Redovni mjesečni fond |
|---|---:|
| lipanj 2026. | 176 h |
| srpanj 2026. | 184 h |
| kolovoz 2026. | 168 h |

Fond se zato računa kao 8 sati za svaki ponedjeljak–petak u mjesecu. Državni blagdan koji pada na radni dan ostaje dio mjesečnog fonda; ako korisnik na taj dan nema evidentiranu radnu smjenu, aplikacija priznaje 8 sati naknade za blagdan.

## Kontrolne vrijednosti prekovremenih sati

U kontrolnim ispravama vrijedi odnos:

| Mjesec | Fond | Ukupni obračunski sati | Prekovremeni |
|---|---:|---:|---:|
| lipanj 2026. | 176 h | 181 h | 5 h |
| srpanj 2026. | 184 h | 216 h | 32 h |
| kolovoz 2026. | 168 h | 211 h | 43 h |

Aplikacija zato odvojeno prikazuje fond, priznate sate i prekovremene sate.

## Rad preko ponoći

Noćna smjena N traje 19:00–07:00. Za detaljnu evidenciju sati pojedine minute pripadaju stvarnom kalendarskom datumu na kojem su odrađene. To je važno za:

- noćni rad 22:00–06:00
- subotu i nedjelju
- rad na blagdan
- prijelaz iz jednog mjeseca u drugi

Sama definicija N smjene ostaje 12 sati, ali mjesečna analitika raspoređuje sate preko ponoći prema stvarnom datumu.

## Dodatne kategorije

Sažetak na Androidu i iOS-u prikazuje i:

- dnevne sate
- noćne sate 22:00–06:00
- sate subotom
- sate nedjeljom
- sate rada na blagdan
- sate u rasponu 14:00–22:00

Ove kategorije služe evidenciji i provjeri obračuna. Financijski postoci dodataka nisu dio osnovnog obračuna sati u aplikaciji Raspored.
