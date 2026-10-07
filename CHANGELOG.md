# Changelog

## 1.6.0

- Zaključan je ugrađeni bolnički profil obračuna na Androidu i iOS-u.
- Ispravljene su službene osnovice po mjesecima: 2025. prati 947,18 € / 975,60 € / 1.004,87 €, a 2026. 1.004,87 € / 1.015,00 € / 1.025,00 € / 1.035,00 € prema važećim razdobljima.
- Bod / koeficijent ostaje 1,25, a Grad Rijeka i pripadajuće porezne stope ostaju interni parametri.
- Sektor, bod, osnovica i porezni grad više se ne prikazuju niti se mogu mijenjati u Postavkama.
- Procjena plaće koristi sate iz kalendara i interne obračunske konstante bez ručnog unosa.
- Dodani su testovi granica promjene osnovice između mjeseci 2025. i 2026.
- Ispod kalendara potpuno je uklonjen blok „Vrste smjena“.
- Unos smjena pojednostavljen je na stalnu kompaktnu traku N, D, GO, J i BO te gumicu; odabrana smjena se jednim dodirom upisuje ili uklanja iz dana.
- Kalendar sada zauzima gotovo cijeli raspoloživi ekran, s većim ćelijama, manjim razmacima i manjim vanjskim marginama.
- Nastavljen je 1:1 UI/UX, stabilnost i dead-code audit za Android i iOS.
- Verzija povećana na 1.6.0.

## 1.5.0

- Dodana automatska lokalna procjena plaće za Android i iOS.
- Korisnik ne unosi sate ni iznose: procjena koristi stvarni mjesečni raspored i obračun sati.
- Zadani profil je Bolnica / javno zdravstvo, bod 1,25 i Grad Rijeka.
- Postavke obračuna svedene su na izbor sektora i boda / koeficijenta bez obveznog ručnog unosa.
- Ugrađene su službene osnovice javnih službi za 2026. i odabrani koeficijenti iz NN 22/2024.
- Porezni profil Rijeka za 2026. koristi stope 20% / 25% i osnovni osobni odbitak 600 €.
- Bolnički profil koristi 50% noćnog dodatka potvrđenog dostavljenim anonimiziranim obračunima; opći profil javne službe zadržava 40% prema TKU-u.
- Automatski se obrađuju noć, subota, nedjelja, blagdan, prekovremeni rad, turnus, GO i BO.
- Privatni sektor ne dobiva izmišljenu procjenu kada ne postoji jedinstvena službena osnovica.
- Logika za dodatne porezne olakšice za djecu i mlade ostaje u obračunskom modulu bez obveznog traženja osobnih podataka.
- Verzija povećana na 1.5.0.

## 1.4.0

- Dodana je ugrađena popodnevna smjena P, zadano 15:00–22:00 (7 sati).
- Ugrađene radne smjene N, D, P i J sada imaju prilagodljiv početak, završetak i opcionalni drugi interval na Androidu i iOS-u.
- Vlastite smjene sada se mogu ponovno otvoriti i uređivati bez gubitka postojećih veza u kalendaru.
- Dodana je postavka radnog okruženja za univerzalnu uporabu u državnim, javnim i privatnim sustavima bez sektorskog zaključavanja funkcija.
- Stare korisničke prilagodbe boja ugrađenih smjena migriraju se u novi model prilagodbi vremena i izgleda.
- Nastavljeno 1:1 UI/UX poliranje prema referentnim ekranima za Android i iOS.
- Dodan je automatizirani dokumentacijski način koji iz stvarno izgrađene iOS Simulator aplikacije snima kalendar, godinu, sažetak, postavke, smjene i novu smjenu.
- README prelazi s ilustrativnog SVG prikaza na stvarne snimke aplikacije pohranjene lokalno u repozitoriju.
- Uveden je stroži dead-code i održivost audit za produkcijski source, lokalne assete i velike datoteke.
- Uklanjaju se zastarjeli marketinški mockupovi kada stvarne snimke prođu CI provjeru.
- Dodatno se provjeravaju Android lint, unit testovi, dead-code hygiene, APK/AAB te iOS Simulator/device build.
- Verzija povećana na 1.4.0.

## 1.3.0

- Dnevna smjena D promijenjena na 07:00–19:00, noćna N na 19:00–07:00 i jutarnja J na 07:00–15:00.
- D i N sada računaju po 12 sati, a J 8 sati.
- GO i BO priznaju 8 sati na radni dan.
- Prazan državni blagdan koji pada na radni dan priznaje 8 sati.
- Dodan je zaseban obračun odrađenih, redovnih, fonda, prekovremenih, plaćenih odsutnosti i ukupno priznatih sati.
- Dodani su automatizirani testovi za trajanje smjena, blagdan i kontrolni primjer prekovremenog rada.
- Aplikacija se otvara na trenutačnom mjesecu; godišnji pregled ostaje siječanj–prosinac i fokusira trenutačni mjesec.
- Korisnik može promijeniti boju kockice i teksta za N, D, J, GO i BO na Androidu i iOS-u.
- Uklonjeno je produkcijsko seedanje demo rasporeda.
- Dodana je dobrovoljna podrška kroz Google Play Billing 9.1 i Apple StoreKit 2 bez otključavanja funkcija.
- Veliki Kotlin/Compose i Swift/SwiftUI fajlovi razdvojeni su u manje module radi lakšeg održavanja.
- README koristi samo vizualne assete iz repozitorija; uklonjene su vanjske shields.io slike.
- Lokalni UI showcase usklađen je s novim vremenima smjena.
- Napravljen je novi full dead-code i arhitekturni audit za 1.3.0.
- Verzija povećana na 1.3.0.

## 1.2.5

- Završni full dead code audit nakon 1.2.4 UI/UX poliranja.
- Uklonjen neiskorišteni `RasporedColors.AccentGradient` token.
- Potvrđeno da su preostali single-reference kandidati samo platform entry pointovi ili protokolarni hookovi.
- README, QA dokumentacija i verzije usklađeni s finalnim 1.2.5 izdanjem.
- Izdanje se generira tek nakon zelenih Android i iOS buildova.

## 1.2.4

- Nastavljen puni 1:1 UI/UX polish Android i iOS aplikacije prema referentnim ekranima.
- Dodatno ispolirani modal Smjene, Nova smjena, Pretraživanje i Postavke.
- Aktivni izbori, boje smjena, kartice i primarne akcije imaju konzistentniju dubinu i cijan glow.
- iOS Pretraživanje više ne koristi generički List izgled nego puni Raspored vizualni sustav.
- Jezik u Postavkama sada je stvarno interaktivan na obje platforme.
- Podrška i privatnost dobile su funkcionalne informativne akcije.
- Android sigurnost: isključen backup aplikacijskih podataka i cleartext promet.
- Napravljen full dead code audit; uklonjen potvrđeni mrtvi kod i dodana CI hygiene kontrola.
- Android CI sada izvršava i lintDebug.
- Glavni README potpuno je redizajniran kao marketinška prezentacija s logotipom, app ikonom, UI prikazom, badgeovima, mogućnostima, privatnošću i build uputama.
- Verzija povećana na 1.2.4.

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
