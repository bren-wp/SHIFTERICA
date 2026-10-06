# Changelog

## 1.2.3

- Dodatno 1:1 poliranje kompletnog Android i iOS sučelja prema dostavljenim referencama.
- Header, pretraživanje, postavke i gumb za novu smjenu dobili su preciznije dimenzije, obrube, dubinu i cijan naglaske.
- Aktivni glavni tabovi dobili su konzistentniji glow i visinu na obje platforme.
- Mjesečni kalendar dobio je preciznije proporcije ćelija, strelice, obrube i diskretno isticanje aktivnih smjena.
- Legenda, donja alatna traka i način uređivanja dodatno su usklađeni s referentnim ekranima.
- Godišnji pregled, sažetak, kartice i postavke dobili su ujednačenu dubinu i staklasti izgled.
- iOS birač oblika dana sada koristi stvarne vizualne oblike umjesto tekstualnih naziva.
- Android launcher ikona dodatno je ispolirana višeslojnim vektorskim prikazom.
- Dodan je nativni Swift generator iOS App Icon resursa kako bi stvarna instalirana ikona odgovarala identitetu aplikacije bez vanjskih generatora slika.
- Verzija povećana na 1.2.3; nakon zelenog CI-ja automatski se izdaju APK, AAB i svi iOS artefakti.
- Ispravljen Android BoxScope compile problem u dekorativnoj splash pozadini.
- Generator iOS ikone premješten je u zaseban CI korak prije Xcode builda kako simulator/device build ne bi naslijedio iPhone SDK pri pokretanju Swift generatora.

## 1.2.2

- Nastavljeno 1:1 usklađivanje prema dostavljenim Android/iOS referentnim ekranima.
- iOS gumb **Uvezi smjenu** više nije dekorativan: dodan je stvarni JSON uvoz s istim poljima kao na Androidu.
- Android popis smjena sada se može pomicati kada korisnik doda više vlastitih smjena.
- CI sada obvezno gradi Android APK i AAB.
- CI gradi i pakira iOS Simulator aplikaciju, unsigned uređajni XCArchive i unsigned IPA.
- Nakon zelenog Android + iOS CI prolaza automatski se objavljuje GitHub Release s artefaktima.
- Verzija izdanja povećana je na 1.2.2.

## 1.2.1

- Ispravljen JVM platform declaration clash u Android `UiSettingsStore` preimenovanjem eksplicitnih mutatora u `update…` metode.
- Sve Compose reference na postavke usklađene su s novim mutatorima.
- Verzija je povećana na 1.2.1 prije ponovne CI provjere.

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
