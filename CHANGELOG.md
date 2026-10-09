# Changelog

## 1.13.7 (u pripremi)

- **Usklađena stvarna satnica Androida i iOS-a pri promjeni sata**: u ožujku preskočeni sat ne pribraja se, a u listopadu ponovljeni sat uredno se evidentira. Noćna smjena 19:00–07:00 zato može trajati 11 ili 13 stvarnih sati tijekom prijelaza na ljetno ili zimsko računanje vremena.
- Preciznije se razvrstavaju noćni, subotnji i nedjeljni sati kod takvih prijelaza bez mijenjanja spremljenog rasporeda ili osnovne definicije smjene.
- Uređena ugrađena smjena **P** koja više nije stvarno popodnevna (primjerice 09:00–17:00) ne dobiva automatski dodatak za popodnevnu smjenu; provjera vremena i trajanja sada je jednaka na obje platforme.
- iOS računanje proteklih minuta izdvojeno je u testabilan Foundation modul. Dodani su Android JUnit i iOS Swift testovi za 11/13 sati, noćne i nedjeljne dodatke te izmijenjene smjene P. iOS regresija izvršava se u obveznom CI-ju prije Simulator builda.
- Obračun je i dalje procjena, a korisnički raspored i potvrđeni neto iznosi ostaju nepromijenjeni.

## 1.13.6 (objavljeno 9. 10. 2026)

- Obračun plaće Android/iOS koristi stvarno spremljena vremena smjena D/N/J, uključujući uređeni početak i završetak rada, satnice kroz ponoć te prijelaz u drugi mjesec.
- Turnus 5 % procjenjuje se samo uz obje 12-satne smjene D i N; jedan zapis ili promijenjena kraca smjena ne aktiviraju automatski dodatak.
- Ispravljen dodatak za rad u drugoj smjeni: više se pogrešno ne obračunava na sate 12-satnih dnevnih/noćnih turnusa samo zbog preklapanja s intervalom 14–22 h. P i kratke posebno definirane popodnevne smjene ostaju podržane.
- Za bruto sastavnice, mirovinske doprinose, porez i procjenu neta primjenjuje se dosljedno zaokruživanje u eurocente na obje platforme.
- Dodani Android regresijski testovi za uređeno vrijeme, vikende, granicu mjeseca i razvrstavanje smjena te neovisni iOS Swift regresijski testovi za cijeli obračunski modul.
- Zadržana je postojeća jednostavna početna kartica **Neto ≈**, bez novih postavki obračuna, osobnih obustava ili promjena korisničkih rasporeda.
- Rezultat ostaje **procjena**, jer iz samog rasporeda nije moguće automatski prepoznati sve pojedinačne stavke poslodavčeva obračuna.

## 1.13.5 (objavljeno 9. 10. 2026)

- Android/iOS: pretraživanje rasporeda sada prvo prikazuje najbliže nadolazeće datume, a zatim najnovije prošle; više ne skriva aktualne smjene među 50 najstarijih zapisa.
- Pretraživanje pronalazi i **oznake smjena** (D/N/GO/BO ili vlastite oznake), uz datum i puni naziv.
- Android: rezultati se osvježavaju i kada se promijeni smjena na već upisanom datumu, bez promjene ukupnog broja datuma.
- Odabir rezultata pretrage otvara odgovarajući mjesec i **točan datum** u postojećem biraču smjena na Androidu i iOS-u.
- Novo regresijsko testiranje za relevantnost rezultata, datume, oznake, ograničenje broja rezultata i osvježavanje nakon uređivanja.
- Podaci se ne šalju na vanjske servise. Izmjene su ograničene na lokalnu pretragu i navigaciju; raspored se ne mijenja samim otvaranjem.

## 1.13.4 (objavljeno 9. 10. 2026)

- Android/iOS: smjena koja se koristi sada ima **Otvori datum** izravno u upravitelju smjena i u dijalogu za zaštićeno brisanje.
- Otvara se najbliži budući upis odabrane smjene, odnosno posljednji prethodni ako nema budućega. Aplikacija otvara odgovarajući mjesec i postojeći birač dana.
- Uklanjanje smjene koja je upisana na datume i dalje je zabranjeno. Navigacija je samo za pregled/uređivanje i sama ne mijenja raspored.
- Android JUnit i iOS Swift regresije provjeravaju pronalazak stvarnog datuma, povratak na raniji mjesec i slučaj bez upisanih datuma.
- Android i iOS buildovi, lint, dead-code audit i stvarni iOS screenshot QA obvezni su prije izdanja.

## 1.13.3

- Android/iOS: popravljen uvoz sigurnosnih kopija vlastitih smjena — svi prvi i drugi intervali vremena provjeravaju se prije primjene bilo kakve promjene rasporeda.
- Neispravno, nedostajuće ili krivo tipizirano vrijeme, kao i naziv koji sadrži samo praznine, odbija se prije uvoza; izbjegnut je djelomičan uvoz uzrokovan takvim podacima.
- Očuvana je kompatibilnost sa starim sigurnosnim kopijama i mogućnost rada preko ponoći; podržane su i sekundarne smjene bez prvog intervala kao u prethodnim izdanjima.
- Android JUnit i iOS Swift regresije pokrivaju oba para vremena, neispravne minute/sate i prethodno podržane formate.
- iOS screenshot QA, Android lint/dead-code audit i stvarni buildovi i dalje su obvezni.

## 1.13.2 (objavljeno 9. 10. 2026)

- Android/iOS: uklanjanje vlastite smjene sada je sigurno — prije brisanja provjeravaju se svi postojeći datumi rasporeda; zauzeta smjena ne može izgubiti svoju definiciju.
- Prije brisanja prikazuje se potvrda, a uz vlastitu smjenu broj datuma na kojima se koristi. Zauzeta smjena daje jasan naputak da se najprije izmijene datumi.
- Zaštita postoji i u sloju lokalne pohrane, ne samo u gumbu; aplikacija ponovno provjerava stanje nakon potvrde.
- U uređivaču ugrađenih smjena vidljiva je važna napomena: promjena vremena vrijedi za sve upisane datume, uključujući prethodne mjesece.
- Android JUnit i iOS Swift regresijski testovi za brojanje korištenih smjena, odbijanje brisanja i sigurno brisanje nakon uklanjanja iz rasporeda.
- Nijedan korisnički datum ne briše se automatski niti se dodaju demo podaci.

## 1.13.1 (objavljeno 9. 10. 2026)

- Android/iOS: međusobno kompatibilna sigurnosna kopija sada čuva početak i kraj prilagođenih ugrađenih D/N/P/J smjena, uz boje, vlastite smjene i raspored.
- Stare JSON kopije s poljem `builtInColors` bez vremena i dalje se mogu uvesti; takav uvoz ne briše lokalno uređena vremena smjena.
- Uvoz odbija nepoznate ili ponovljene ugrađene oznake, nepotpune, nevaljane ili nedopuštene vremenske parove prije ikakve izmjene rasporeda.
- Nakon uvoza prikazuje se broj dodanih datuma, novih vlastitih smjena i obnovljenih ugrađenih smjena. Datumi koji već postoje nisu prepisani.
- Android/iOS: rjeđe korištene postavke izgleda, jezika, datuma i bilješki objedinjene su u proširivu grupu **Više prilagodbi**; postojeće vrijednosti se ne brišu.
- Android: dijalog za podešavanje vremena i boja smjena ima pomični sadržaj za manje ekrane.
- Novi Android JUnit i iOS Swift regresijski testovi provjeravaju stari format kopije, prijelaz preko ponoći, minute i odbijanje loših satnica.
- Objaviti tek nakon zelenog Android/iOS CI-ja, iOS screenshot QA i provjere distribucijskih artefakata.

## 1.13.0 (objavljeno 9. 10. 2026)

- Android/iOS: Sažetak sada ima samo Smjene i Sati. Uklonjeni su ekrani profila obračuna i godišnje zarade, bez brisanja prethodno spremljenih vrijednosti.
- Na mjesečnoj naslovnici **Neto plaća ≈** prikazuje orijentacijski neto iz rasporeda; nije prikazana lažna vrijednost kad nisu dostupne osnovice ili smjene.
- Strelice uz mjesečni tab omogućuju neposredan prelazak na prethodni/sljedeći mjesec.
- Uklonjen je rijetko korišteni ručni JSON uvoz jedne smjene iz sučelja; sigurnosna kopija cijelog rasporeda i dalje postoji.
- D/N/J/P vremena mogu se lokalno prilagoditi u upravitelju smjena s kontrolom formata i opcijom **Vrati zadano**.
- Lokalni D/N podsjetnici prate novo početno vrijeme (sat unaprijed), uz iste zadane podsjetnike u 06:00 i 18:00.
- Android i iOS splash indikator postupno se puni tijekom prikaza.
- Android/iOS regresijski testovi za izmijenjena vremena i granice podsjetnika; CI, lint, screenshot QA i buildovi ostaju obvezni.

## 1.12.1 (objavljeno nakon CI potvrde)

- iOS: podsjetnici za promjenu rasporeda i prekidača koriste upravo objavljene vrijednosti umjesto prethodnog stanja iz `@Published` događaja.
- iOS: ponovno zakazivanje serijalizirano je radi sprječavanja utrke između brisanja i asinkronog dodavanja obavijesti.
- iOS/Android: ujednačen pomični horizont od 60 dana uz provjere prethodnog dana, granice horizonta i prekidača.
- iOS: izvedivi regresijski test plana podsjetnika u CI-ju, prije builda i snimanja stvarnih zaslona.
- Nema promjene lokalnih zapisa, sigurnosnih kopija, osobnih podataka ni svojstva podsjetnika (to nisu neprekidni alarmi).

## 1.12.0 (objavljeno 8. 10. 2026.)

- Android i iOS: lokalni podsjetnici za dnevnu smjenu **D** i noćnu smjenu **N** bez udaljenih servisa, računa i praćenja.
- Podsjetnik prethodne večeri u **20:00**; obavijest sa zvukom na dan smjene u **06:00 za D**, odnosno u **18:00 za N**.
- Podsjetnici su u aplikaciji zadano uključeni, ali se dopuštenje za slanje obavijesti zahtijeva izričitom korisničkom radnjom.
- Odvojeni prekidači za večernje i dnevne obavijesti, smješteni u preglednu grupu **Podsjetnici za smjene** u postavkama.
- Android: ponovno zakazivanje po promjeni, brisanju ili uvozu smjene, nakon pokretanja uređaja ili promjene vremenske zone te dnevno obnavljanje narednih 60 dana. Koristi Androidov inexact AlarmManager radi kompatibilnosti s Play pravilima.
- iOS: lokalne UNUserNotificationCenter obavijesti, odabrano do 60 najbližih budućih događaja, ponovno zakazivanje pri promjeni smjena, postavki i otvaranju aplikacije.
- Isporuka ovisi o odobrenju sustava, načinu rada baterije, načinu Ne ometaj i pravilima platforme. **Ovo nisu alarmi s neprekidnom zvonjavom**; takvu funkciju potrebno je zasebno implementirati s odgovarajućim sustavom alarma.
- Nisu promijenjeni niti obrisani prethodni ili budući zapisi rasporeda. Dodani su testovi raspoređivanja podsjetnika.
- Izdanje objaviti isključivo nakon zelenih Android i iOS CI poslova, provjere artefakata i iOS screenshot QA-a.


## 1.11.0
- Procjena neta sada izričito prikazuje **neto prije obustava**: krediti, ovrhe, administrativne zabrane i druge osobne obustave ne oduzimaju se. Obvezni mirovinski doprinosi i porez i dalje se obračunavaju.
- Unos potvrđene plaće za godišnju statistiku traži neto s platne liste prije obustava, a ne umanjenu bankovnu isplatu. Android regresijski test potvrđuje da izračun koristi samo zakonska davanja.

- Uklonjen problem preniske neto procjene u nepotpunom rasporedu: mjesečni prikaz jasno razlikuje procjenu za puni fond od upisanih dodataka te navodi pretpostavljene sate.
- Bruto satnica više se ne uvećava za staž; dodatak za staž obračunava se zasebno, u skladu s modelom obračunskih stavki.
- Turnus 5% računa se samo za pripadajuće D/N minute; druga smjena 10% obračunava se i kada postoji turnus.
- Lokalna porezna pravila Rijeke sada razlikuju 2025. (22/32%) i 2026. (20/25%).
- Postavke obračuna nalaze se iznad procjene, uz pregled koeficijenta, godina staža, djece i uzdržavanih članova. Za neunesene parametre slijedi upozorenje.
- Moguće je unijeti bruto satnicu godišnjeg odmora po prosjeku kako bi GO bio bliži stvarnom obračunu; ostaje opcionalna.
- Plaća uključuje detalje po stavkama na zahtjev i razumljivija upozorenja o mogućim odstupanjima od platne liste.
- Android obračunski mjesečni sažetak koristi memoizaciju kako bi se izbjegli skupi ponovljeni izračuni.
- iOS postavke plaće izdvojene su u zasebnu komponentu kako bi prikazi ostali manji, održiviji i responzivniji.
- Testovi dodataka, poreza, prekovremenih, pretpostavljenih sati i GO koriste sintetizirane kontrolne podatke bez identifikatora.
- Nema promjene korisničkih rasporeda, sigurnosnih kopija ili formata pohrane.
- Ne objavljivati izdanje bez zelenih Android i iOS CI poslova, QA i artefakata.


## 1.10.0

- Android i iOS: fond sati i prekovremeni i dalje se automatski obračunavaju iz stvarnih mjeseci i smjena; u Sažetku > Sati fond je sada moguće ručno prilagoditi i vratiti na automatski.
- Procjena plaće i kalendarske metrike koriste isti računovodstveni model i lokalno spremljeni fond, godine staža, broj djece i uzdržavanih članova.
- Sažetak > Plaća: potvrđeni mjesečni neto, godišnji zbroj stvarno potvrđenih isplata, mjesečni prikaz i prosjek tri posljednje potvrđene isplate. Procjene nisu stvarne isplate.
- Potvrđeni iznosi ostaju lokalni i ne zahtijevaju identitet, OIB ni učitavanje platne liste.
- Mjesečne kalendarske ćelije na obje platforme vrlo su malo manje: razmak mreže povećan je za jedan piksel/point.
- Android launcher ikona dobila je slojevitiju grafiku i proporcije u smjeru iOS staklene ikone.
- Postavke: otvaranje uvjeta korištenja, politike privatnosti i stranice autora Brendigo; puna adresa nije ispisana na zaslonu.
- Donacije ostaju u službenom Google Play Billing / Apple StoreKit postupku dok uvjeti za vanjsko dobrovoljno darivanje nisu provjereni.
- Raspored se i dalje čuva bez vremenskog roka u lokalnoj pohrani; izvoz/uvoz interoperabilne JSON sigurnosne kopije omogućuje korisničko čuvanje podataka.
- Uvoz sigurnosne kopije dodaje samo nedostajuće datume i ne mijenja prethodno spremljene datume.
- Nema produkcijskog demo rasporeda, praćenja plaća na serveru niti učitavanja identifikacijskih podataka.
- Novi release zahtijeva zeleni Android i iOS CI, QA i provjeru distribucijskih artefakata.


## 1.9.0

- Nastavljeno je 1:1 poliranje Android i iOS aplikacije prema dostavljenim referentnim slikama.
- Ekran **Nova smjena** dobio je preciznije glass kartice, jače cijan aktivne tabove i dosljednije dimenzije kontrola.
- Birači boje pozadine i teksta povećani su i vizualno bolje odvajaju odabranu boju.
- Kontrola veličine teksta dobila je veće minus/plus tipke, jasniji brojčani prikaz i usklađene obrube.
- Vremenska polja dobila su usklađena fokusna stanja i čitljivije obrube.
- Android forma koristi IME padding kako tipkovnica ne bi zaklonila akcije pri dnu.
- iOS forma koristi veliki presentation detent, vidljiv drag indikator i interaktivno zatvaranje tipkovnice.
- Modal **Smjene** dodatno je usklađen po close gumbu i prioritetu akcija Nova smjena / Uvezi smjenu.
- Splash ekran dobio je izraženiji glass halo oko app marke, slojevite smjenske pločice i precizniju progress traku.
- Android i iOS zadržavaju isti vizualni identitet, raspored elemenata i semantiku dodira.
- Zaključana pravila smjena ostaju D 07:00–19:00, N 19:00–07:00, J 07:00–15:00, P 14:00–22:00 te GO/BO 8 h na radni dan.
- Iznad mjesečnog kalendara dodan je kompaktni pregled **Fond sati / Prekovremeni / Plaća** koji se automatski osvježava iz stvarnog rasporeda.
- Dodir na bilo koji datum sada otvara izravni birač smjene D/N/J/P/GO/BO i vlastitih smjena; odabrana smjena odmah se sprema za taj datum.
- Stari zasebni način „Uredi raspored” uklonjen je iz mjesečnog toka kako ne bi postojala dva različita načina za istu radnju.
- Birač datuma ima zasebnu akciju **Obriši** i pristup **Sve smjene**.
- **Sažetak** je razdvojen na tri jasna prikaza: **Smjene / Sati / Plaća**, umjesto jednog dugog ekrana sa svim podacima.
- Android i iOS koriste isti novi tok unosa smjene i isti raspored mjesečnih metrika.
- Android lint/test/build, iOS build/archive i full dead-code audit ostaju obvezni release gate.
- Verzija povećana na 1.9.0.


## 1.8.0

- Nastavljeno je 1:1 Android/iOS poliranje prema dostavljenim referentnim slikama.
- Mjesečne smjenske ćelije dobile su slojeviti vertikalni glass/neon gradijent, izraženiji obrub i kontrolirani sjaj.
- Brojevi dana i oznake smjena zadržavaju postojeću semantiku i čitljivost; produkcijski raspored nije seedan demo podacima.
- Godišnji pregled dobio je isti vizualni tretman smjenskih ćelija kao mjesečni prikaz.
- Godišnji pregled sada ima kompaktnu horizontalnu legendu N/D/GO/J/P/BO kao na referentnom ekranu.
- Mjesečni prikaz i dalje nema zasebnu karticu „Vrste smjena” te zadržava maksimalnu raspoloživu visinu kalendara.
- Sažetak i ekran Smjene dobili su usklađene glass/neon oznake smjena i jača aktivna stanja.
- Dodatno su ujednačeni obrubi, sjene i dubina kartica kroz Android i iOS.
- Zaključana pravila smjena ostaju nepromijenjena: D 07:00–19:00, N 19:00–07:00, J 07:00–15:00, P 14:00–22:00 te GO/BO 8 h na radni dan.
- Android lint, unit testovi, APK/AAB, iOS build/archive i full dead-code audit ostaju obvezni release gate.
- Verzija povećana na 1.8.0.


## 1.7.0

- Nastavljeno je 1:1 poliranje Android i iOS sučelja prema novim dostavljenim referentnim slikama.
- Vizualni sustav dobio je dublju tamnoplavu podlogu, čišće staklaste kartice, čitljivije obrube i izraženiji cijan naglasak.
- Header, glavni tabovi i kompaktne kontrole uređivanja dodatno su usklađeni po dimenzijama, kontrastu i aktivnim stanjima.
- Postavke na obje platforme sada imaju jasnije odvojene unutarnje redove/kartice, bliže referentnom rasporedu uz zadržanu responzivnost.
- Referentne slike koriste se samo za UI/UX smjer; demo rasporedi i zastarjela vremena sa slika ne ulaze u produkcijske podatke.
- Zadržana su zaključana pravila D 07:00–19:00, N 19:00–07:00, J 07:00–15:00, P 14:00–22:00 te GO/BO po 8 sati na radni dan.
- Dokumentacija dizajna i parity checklist usklađeni su s aktualnim kompaktnim načinom uređivanja i uklonjenom karticom „Vrste smjena” ispod mjesečnog kalendara.
- Ispravljena je README oznaka licence tako da odgovara stvarnoj GPL-3.0 licenci repozitorija.
- Android/iOS build, lint/testovi i full dead-code audit ostaju obvezni release gate.
- Verzija povećana na 1.7.0.


## 1.6.0

- Zaključan je ugrađeni bolnički profil obračuna na Androidu i iOS-u.
- Ispravljene su službene osnovice po mjesecima: 2025. prati 947,18 € / 975,60 € / 1.004,87 €, a 2026. 1.004,87 € / 1.015,00 € / 1.025,00 € / 1.035,00 € prema važećim razdobljima.
- Bod / koeficijent ostaje 1,25, a Grad Rijeka i pripadajuće porezne stope ostaju interni parametri.
- Sektor, bod, osnovica i porezni grad više se ne prikazuju niti se mogu mijenjati u Postavkama.
- Procjena plaće koristi sate iz kalendara i interne obračunske konstante bez ručnog unosa.
- Dodani su testovi granica promjene osnovice između mjeseci 2025. i 2026.
- Ispod kalendara potpuno je uklonjen blok „Vrste smjena“.
- Mjesečni prikaz dobio je siguran način uređivanja: pregled je samo za čitanje, a u načinu uređivanja odabrana smjena se upisuje jednim dodirom; brisanje je zasebna radnja.
- Brza traka sada uključuje N, D, J, P, GO i BO uz zaseban završetak uređivanja.
- Unos smjena pojednostavljen je na kompaktnu traku u načinu uređivanja; odabrana smjena se jednim dodirom upisuje u dan, a gumica služi isključivo za brisanje.
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

- Dodana je ugrađena popodnevna smjena P; u 1.6.0 zadano je 14:00–22:00 (8 sati).
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
