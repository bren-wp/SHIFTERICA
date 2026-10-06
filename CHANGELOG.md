# Changelog

## 1.2.0

- Nastavljeno 1:1 usklađivanje Android i iOS sučelja s dostavljenim referentnim ekranima.
- Ispravljen Android CI compile pad: uklonjena nepostojeća ikona `Eraser` i dodan nedostajući `LazyColumn` import.
- Brojevi dana u mjesečnom prikazu poravnani su kao na referenci.
- Godišnji pregled sada u obojenim ćelijama istodobno prikazuje datum i oznaku smjene.
- Postavke dobivaju vizualni birač oblika dana umjesto tekstualnog popisa.
- Uklonjen je dodatni gumb „Gotovo” iz Postavki kako bi sheet slijedio referentni raspored.
- Splash na obje platforme dodatno je usklađen s referencom pomoću plutajućih N/D/GO/J/BO pločica.
- App mark je skalabilan i zadržava isti omjer u headeru i splash prikazu.
- Android/iOS paritet ostaje obvezan za sve vizualne i funkcionalne izmjene.

## 1.1.0

- Prenesen cijeli dosadašnji Android + iOS razvoj u `bren-wp/SHIFTERICA`.
- Nastavljeno usklađivanje Android i iOS sučelja s dostavljenim 1:1 referentnim ekranima bez generiranja novih slika.
- Dodana trajna biblioteka vlastitih smjena na obje platforme.
- Ekran **Nova smjena** sprema naziv, skraćenicu, boju, boju teksta, veličinu teksta i vremenske intervale.
- Vlastite smjene pojavljuju se u legendi, brzoj alatnoj traci, načinu uređivanja, pretraživanju i popisu smjena.
- Android **Uvezi smjenu** podržava uvoz jedne smjene ili popisa smjena iz JSON-a.
- Postavke su trajno spremljene i povezane s kalendarom.
- Sažetak računa vrijeme iz stvarne definicije smjene, uključujući smjene preko ponoći i dva vremenska intervala.
- Godišnji pregled i kalendar koriste i vlastite smjene.
- Ispravljena hrvatska terminologija i konzistentnost Android/iOS prikaza.
