<p align="center">
  <img src="docs/assets/raspored-logo.svg" alt="Raspored" width="760">
</p>

<p align="center">
  <strong>Pametni planer smjena za Android i iOS.</strong><br>
  Brz, privatan i vizualno usklađen raspored rada s preciznim obračunom sati.
</p>

<p align="center">
  <code>Android 8.0+</code>
  <code>iOS 17+</code>
  <code>Kotlin + Compose</code>
  <code>Swift + SwiftUI</code>
  <code>v1.13.17</code>
</p>

<p align="center">
  <a href="https://github.com/bren-wp/SHIFTERICA/releases/latest"><strong>Preuzmi najnovije izdanje</strong></a>
  ·
  <a href="#mogućnosti">Mogućnosti</a>
  ·
  <a href="#obračun-radnog-vremena">Obračun sati</a>
  ·
  <a href="#privatnost-i-sigurnost">Privatnost</a>
</p>

---

## Raspored na prvi pogled

<p align="center">
  <img src="docs/assets/ui-showcase.svg" alt="Lokalni prikaz Raspored sučelja" width="100%">
</p>

Raspored je napravljen za korisnike koji rade u smjenama i žele jasan kalendar bez nepotrebnih računa, oglasa i složenih administracijskih ekrana. Namijenjen je državnim i javnim službama, privatnom sektoru i drugim radnim okruženjima bez vezivanja uz jednu ustanovu ili profesiju. Android i iOS imaju isti vizualni jezik, iste hrvatske nazive i isti način rada.

Sve grafike prikazane u ovom README-u nalaze se u repozitoriju. Aplikacija ne preuzima logotipe, ikone, pozadine ili druge vizualne resurse s CDN-a.

## Pregledniji mjesečni prikaz

Na početnom ekranu satni pokazatelji ostaju pregledni u dvije jednake kartice, dok se **neto iznos prikazuje u zasebnoj širokoj traci**, s jasnom razlikom između *potvrđene* i *procijenjene* plaće. Time se izbjegava odrezani iznos na uskim ekranima. Navigacija po mjesecima ima pristupačne opise, a unos iznosa prilagođen je radu s tipkovnicom na Androidu i iOS-u.

## Potvrđeni neto i stvarni iznos godišnjeg odmora

U **Sažetak → Sati** unesite iznos neto plaće koji se stvarno nalazi na vašoj obračunskoj listi. Raspored prikazuje potvrđeni neto odvojeno od izračunate procjene i odstupanje među njima. Po želji možete urediti i prosječnu **bruto satnicu godišnjeg odmora**, ako se razlikuje od procjene iz osnovice. Iznosi se zapisuju lokalno na uređaju i mogu se zasebno obrisati. Nikada ne morate slati sliku platne liste ili osobne podatke.

Unos u eurima podržava `1234,56`, `1.234,56` i `1234.56`, a pogrešni formati ili negativni iznosi se ne spremaju. Službena isplata uvijek ima prednost pred procjenom u kartici Neto, ali razlika je jasno označena.

## Premium dizajn i pristupačnost

Android i iOS dijele novu tamnu **midnight / sapphire** paletu s mint naglascima, nježnom lavandom i semantičkim bojama smjena. Aktivna donja navigacija, dodavanje smjene i mjesečni pregled imaju konzistentan kontrast. Zaglavlje i sažetak čitljiviji su na malim ekranima, a iOS sadrži VoiceOver oznake gumba pretraživanja, postavki i dodavanja smjene. Izvorne ikone i README ilustracije usklađene su s aplikacijom bez novih rasterskih resursa ili mrežnih zahtjeva.

Automatski QA provjerava podudarnost Android/iOS palete, zadane boje smjena, iOS naglasne boje, kontrast osnovnog teksta i ispravnost SVG/XML datoteka prije izrade paketa.

## Mjesec isplate i porez bez nagađanja

U **Sažetak → Sati** za svaki obračunski mjesec sada možete postaviti mjesec isplate. Standardno je postavljen sljedeći mjesec, kao na dostavljenim obračunima, ali se može odabrati i isplata istog mjeseca ili odgođena isplata. Model računa porez i osobni odbitak po mjesecu isplate, a osnovicu plaće, staž i odrađene sate po mjesecu rada. Promjena se sprema samo na uređaj, ne utječe na potvrđene isplate i uvijek se može poništiti.

## Precizan prijenos noćnih sati u sljedeći mjesec

Smjena od 19:00 do 07:00 zadnjeg dana mjeseca razdvaja se prema stvarnom vremenu rada. Ako sedam jutarnjih sati pripada sljedećem mjesecu, aplikacija ih uključuje u obračun i prikazuje procjenu neta čak i ako u novom mjesecu još nije unesena zasebna smjena. Primjer prijelaza 31. listopada na 1. studenoga 2026. dodatno uključuje nedjeljni i blagdanski dodatak za Svi svete, bez dvostrukog brojanja stvarnih sati. Na potpuno praznom rasporedu ne prikazuje se izmišljena procjena plaće. Android i iOS provjeravaju isti uvjet uz regresijske testove.

## Diskretna donja navigacija

Mjesec, Godina i Sažetak uvijek su dostupni u **nenametljivoj donjoj traci** s jasnim vektorskim ikonama, dovoljno velikim dodirnim površinama i označenim aktivnim odredištem. Strelice za promjenu mjeseca i godine ostaju uz sam kalendar/sažetak. Zaglavlje služi za pretraživanje, postavke i dodavanje smjena; nema dvostrukih kartica na vrhu.

Ikone navigacije učitavaju se iz ugrađenih vektorskih resursa Androida i iOS SF Symbols sustava, bez novih rasterskih datoteka ili dodatnih mrežnih zahtjeva. Grafički SVG resursi u dokumentaciji pojednostavljeni su radi bržeg iscrtavanja; JPEG/PNG dokumentacijske snimke nisu promijenjene.

## Jednostavniji pregled i točnost neta

Strelice uz naziv mjeseca omogućavaju brz prelazak na prethodni ili sljedeći mjesec. Sažetak sadrži dvije cjeline (**Smjene**, **Sati**); ne sadrži zasebne kartice profila obračuna, godišnje zarade niti ručnog uvoza pojedinačne smjene. Na početnoj je pregled fonda, prekovremenih sati i **Neto plaća ≈**. Simbol ≈ znači da izračun ovisi o potpunosti rasporeda, poreznim olakšicama i podacima poznatima aplikaciji, pa nije službena platna lista. Osobni podaci ni dokumenti o plaći ne ulaze u repozitorij.

Splash indikator sada se pomiče, a korisnik može promijeniti D/N/J/P početak i završetak u formatu HH:mm bez promjene zadanih vrijednosti drugim korisnicima. Uvijek postoji **Vrati zadano**.

## Podsjetnici za smjene

U **Postavke → Podsjetnici za smjene** nalaze se tri jednostavna prekidača: glavno uključivanje, **večer prije u 20:00** i **prije smjene**. Za zadana vremena **D (07:00–19:00)** obavijest dolazi u 06:00, a za **N (19:00–07:00)** u 18:00. Promjenom početka smjene pomiče se i podsjetnik na sat prije novog početka. Večer prije obje vrste smjene stiže informativna obavijest u 20:00. Korisnik može u potpunosti isključiti podsjetnike ili pojedini tip.

Podsjetnici su zadano uključeni u postavkama, ali bez odobrenja Androida/iOS-a ne dolaze. Dopuštenje se traži korisničkom radnjom u Postavkama. Ne šalju se podaci na poslužitelj niti se koriste oglašivačke ili analitičke mreže. Na Androidu sustav radi sa zakazanim (ne nužno točnim u minutu) obavijestima te se obnavlja nakon ponovnog pokretanja uređaja. Na iOS-u se održava do 60 najbližih obavijesti unutar idućih 60 dana, koje se obnavljaju prilikom korištenja aplikacije.

**Razlika od budilice:** standardna obavijest sa zvukom nije neprekidni alarm koji zvoni dok ga korisnik ne isključi. Potpuni alarmni način rada zahtijeva zasebnu integraciju s dopuštenjima i API-jima sustava. Za kritične smjene korisnik treba zadržati vlastiti sustav budilice dok takva integracija ne bude potvrđena na uređajima.

## Mogućnosti

| | Mogućnost | Opis |
|---|---|---|
| 📅 | **Mjesečni kalendar** | Aplikacija se otvara na trenutačnom mjesecu; iznad kalendara prikazuje fond sati, prekovremene i procjenu plaće. |
| 🗓️ | **Godišnji pregled** | Godina ostaje poredana od siječnja do prosinca, a prikaz se otvara na trenutačnom mjesecu. |
| ⏱️ | **Precizan obračun sati** | Redovni sati, fond, prekovremeni, plaćene odsutnosti i noćni rad; uključuje stvarno protekle sate pri promjeni ljetnog/zimskog vremena. |
| 💶 | **Automatska procjena plaće** | Iz rasporeda procjenjuje neto plaću: primjenjuje izmijenjena vremena smjena, odgovarajuće dodatke i zaokruživanje novčanih iznosa u eurocente, bez nepotrebnih postavki. |
| 🌙 | **Smjene N i D** | Dnevna 07:00–19:00, noćna 19:00–07:00, obje po 12 sati. |
| 🌆 | **Popodnevna P** | Zadano 14:00–22:00, ukupno 8 sati. |
| ☀️ | **Jutarnja smjena** | Jutarnja 07:00–15:00, ukupno 8 sati. |
| 🛠️ | **Brzi unos smjene** | Dodirnite datum i odmah odaberite D/N/J/P/GO/BO ili vlastitu smjenu; brisanje je zasebna radnja. |
| 🛠️ | **Vlastite smjene** | Ugrađene D/N/J/P smjene imaju postojeća zadana vremena, ali početak i kraj mogu se urediti i vratiti; vlastite smjene podržavaju dva intervala i rad preko ponoći. |
| 🏖️ | **GO i BO** | Godišnji odmor i bolovanje priznaju 8 sati na radni dan. |
| 🇭🇷 | **Hrvatski blagdani** | Prazan radni dan koji je državni blagdan priznaje se kao 8 sati. |
| 🎨 | **Boje smjena** | Korisnik može promijeniti boju kockice i teksta za N, D, P, J, GO i BO. |
| ➕ | **Vlastite smjene** | Naziv, skraćenica, boje, veličina teksta i do dva vremenska intervala; ako se intervali preklapaju, iste se minute računaju samo jednom. |
| 📊 | **Razdvojeni sažetak** | Sažetak ima samo **Smjene** i **Sati**. Mjesečna početna kartica pokazuje procijenjeni neto, bez dodatnih ekrana plaće. |
| 🔎 | **Pretraživanje** | Pronalazi datume, oznake i nazive smjena; prikazuje prvo najbliže nadolazeće, pa najnovije prošle. Dodir rezultata otvara upravo taj dan bez automatske izmjene. |
| ⚙️ | **Postavke** | Izgled, vikendi, današnji datum, format vremena i datuma te bilješke. |
| ❤️ | **Dobrovoljna podrška** | Jednokratna podrška kroz Google Play Billing i Apple StoreKit; ne otključava funkcije. |
| 🔒 | **Privatnost** | Raspored i postavke ostaju na uređaju; nema oglasa ni oglasnih trackera. |

## Obračun radnog vremena

Ugrađene smjene koriste ove zadane vrijednosti:

| Oznaka | Smjena | Vrijeme | Evidencija |
|---|---|---:|---:|
| **D** | Dnevna | 07:00–19:00 | 12 h |
| **N** | Noćna | 19:00–07:00 | 12 h |
| **J** | Jutarnja | 07:00–15:00 | 8 h |
| **P** | Popodnevna | 14:00–22:00 | 8 h |
| **GO** | Godišnji odmor | — | 8 h na radni dan |
| **BO** | Bolovanje | — | 8 h na radni dan |

Fond sati računa radne dane od ponedjeljka do petka. Ako korisnik na radni dan koji je državni blagdan ostavi praznu ćeliju, aplikacija taj dan priznaje kao 8 sati. Odrađeno vrijeme iznad raspoloživog redovnog fonda prikazuje se kao prekovremeno.

Pravila su implementirana u zasebnom modulu za obračun i pokrivena automatiziranim testovima.

## Automatska procjena plaće

Procjena plaće ne traži ručni unos sati, iznosa niti parametara obračuna. Aplikacija koristi podatke koje već ima u kalendaru: fond, redovni rad, prekovremene sate, noćni rad, subote, nedjelje, blagdane, GO i BO. Parametri bolničkog profila ugrađeni su u obračunski modul i nisu izloženi u Postavkama niti ih korisnik može mijenjati.

Izračun je orijentacijski i nije zamjena za službenu platnu listu. Bez dodatnih osobnih podataka koristi se osnovni osobni odbitak. Logika za djecu i godišnju olakšicu za mlade ostaje u obračunskom modulu, ali nije nametnuta korisniku kao obvezan unos. Detalji održavanja i izvori nalaze se u `qa/PAYROLL_RULES_2026.md`.

## Vizualni identitet

<p align="center">
  <img src="docs/assets/app-icon.svg" alt="Raspored ikona aplikacije" width="180">
</p>

Primarni naglasak je **#19DCE0**, uz tamnu podlogu **#051522**. Zadane boje smjena su:

| Oznaka | Smjena | Zadana boja |
|---|---|---|
| **N** | Noćna | 🟨 #FFD21F |
| **D** | Dnevna | 🟦 #13B7F3 |
| **GO** | Godišnji odmor | 🟩 #6CEB82 |
| **J** | Jutarnja | 🟢 #77DED7 |
| **P** | Popodnevna | 🟧 #FF8A3D |
| **BO** | Bolovanje | 🟪 #D991EE |

Boje i vremena ugrađenih radnih smjena mijenjaju se iz upravitelja smjena i čuvaju na uređaju. Zadano ostaju D 07:00–19:00, N 19:00–07:00, J 07:00–15:00 i P 14:00–22:00. Pritiskom na **Vrati zadano** vraćaju se izvorne boje i sati.

Za vlastite smjene prikazuje se broj datuma na kojima su upisane. Dodirnite **Otvori datum** uz broj upisa ili u objašnjenju zaštićenog brisanja da biste izravno otvorili najbliži nadolazeći dan, a ako ga nema, posljednji prethodni. Raspored se ne mijenja samim otvaranjem. **Smjenu koja je u upotrebi nije moguće obrisati** dok se ne ukloni ili promijeni na tim datumima; za nekorištenu smjenu potrebna je potvrda brisanja. Oznake i spremljeni datumi time ostaju usklađeni. Promjena početka ili kraja ugrađene smjene mijenja tumačenje svih njezinih datuma, uključujući već protekle mjesece.

## Android

Android izdanje koristi **Kotlin + Jetpack Compose + Material 3**.

- minimalno: Android 8.0 / API 26
- target: API 36
- Google Play Billing Library 9.1 za dobrovoljnu podršku
- APK za instalaciju i QA
- AAB za Google Play distribuciju

## iOS

iOS izdanje koristi **Swift 5.10 + SwiftUI + StoreKit 2**.

- minimalno: iOS 17
- Simulator .app
- unsigned .xcarchive
- unsigned .ipa

Za instalaciju na fizički uređaj, TestFlight ili App Store potreban je odgovarajući Apple certifikat i provisioning profil.

## Sigurnosna kopija i jednostavne postavke

U **Postavke → Sigurnost podataka** izvoz uključuje sve datume, vlastite smjene, boje i (od 1.13.1) prilagođene početke i krajeve ugrađenih smjena. Format JSON ostaje kompatibilan s kopijama prethodnih izdanja na Androidu i iOS-u. Vraćanje datuma je **merge-only** — već spremljeni datumi ostaju nepromijenjeni. Prilagodbe ugrađenih smjena se pri uvozu primjenjuju iz datoteke.

**Više prilagodbi** otvara dodatne mogućnosti izgleda, jezičnog prikaza, kalendara i bilješki; ostale su postavke vidljive bez otvaranja te grupe. Ta promjena ne mijenja postojeće spremljene postavke.

## Privatnost i sigurnost

Raspored je projektiran tako da osobni raspored rada ostaje na uređaju:

- nema korisničkog računa
- nema oglasa ni oglasnih trackera
- raspored se ne šalje na vlastiti udaljeni poslužitelj
- aplikacijski vizualni resursi ne ovise o CDN-u
- Android zabranjuje cleartext promet i sigurnosne kopije aplikacijskih podataka
- iOS uključuje Privacy Manifest
- dobrovoljnu kupnju obrađuje isključivo trgovina platforme; aplikacija ne prima podatke platne kartice

## Stabilnost i održavanje

Kod je podijeljen u manje, tematski jasne module. Veliki UI i model fajlovi razdvojeni su na ekran, komponente, pohranu, obračun i platformsku integraciju kako bi izmjene bile sigurnije i jednostavnije za pregled.

CI provjerava:

- Android unit testove
- Android lint
- nedovršene produkcijske oznake TODO, FIXME, HACK i XXX
- Android APK i AAB build
- iOS Simulator build
- iOS uređajni archive bez potpisa
- pakiranje distribucijskih artefakata

## Razvoj i build

### Android

~~~bash
cd android
gradle :app:testDebugUnitTest
gradle :app:lintDebug
gradle :app:assembleDebug :app:bundleDebug
~~~

### iOS

~~~bash
brew install xcodegen
xcodegen generate --spec ios/project.yml

xcodebuild \
  -project ios/Raspored.xcodeproj \
  -scheme Raspored \
  -sdk iphonesimulator \
  CODE_SIGNING_ALLOWED=NO \
  build
~~~

## Struktura repozitorija

~~~text
SHIFTERICA/
├── android/                 # Android aplikacija i testovi
├── ios/                     # iOS aplikacija
├── docs/                    # lokalni vizualni resursi i dokumentacija
├── qa/                      # QA i dead-code audit
├── .github/workflows/       # CI i release pipeline
├── CHANGELOG.md
├── VERSION
└── README.md
~~~

## Izdanje

Verzija koda je **v1.13.9**; objavljena izdanja dostupna su na GitHub Releases. GitHub Release objavljuje se tek nakon zelenih Android i iOS buildova na glavnoj grani.

➡️ [GitHub Releases](https://github.com/bren-wp/SHIFTERICA/releases/latest)

## Licenca

Projekt je dostupan pod licencom **GNU General Public License v3.0 (GPL-3.0)**. Pogledajte [LICENSE](LICENSE).

---

<p align="center">
  <strong>Raspored</strong><br>
  Pametni planer smjena za uredniji radni mjesec.
</p>

### Sigurnost rasporeda i obračun plaće

- Svaki datum ostaje spremljen lokalno i dostupan u prošlim i budućim godinama. Sama lokalna pohrana ne štiti od brisanja aplikacije ili gubitka uređaja; redovito izvozite sigurnosnu kopiju iz **Postavke > Sigurnost podataka**.
- Sigurnosna kopija može se uvesti na Android ili iOS: sadrži datume, vlastite smjene, boje i prilagođene početke i krajeve ugrađenih radnih smjena. Postojeći datumi neće se prepisati. Uvoz postavki ugrađenih smjena primjenjuje postavke iz kopije.
- Kompatibilnost: starije JSON kopije sadrže samo boje ugrađenih smjena i ostaju podržane bez poništavanja već spremljenih lokalnih vremena. Neispravni zapisi vremena i nepotpuni prvi ili drugi interval vlastite smjene odbijaju se provjerom cijele datoteke **prije primjene promjena**. Postojeći raspored pritom ostaje netaknut.
- Sažetak prikazuje **Smjene / Sati**. Sati nude automatski mjesečni fond ili ručnu korekciju za konkretan mjesec.
- Parametri obračuna (staž, djeca, uzdržavani članovi) ostaju na uređaju; procjena plaće nije službeni obračun.
- Mjesečna kartica **Neto plaća ≈** daje informativan izračun iz upisanih smjena; povijesni lokalno spremljeni parametri ostaju očuvani bez dodatnih ekrana.
- Android i iOS upotrebljavaju ugrađenu trgovinsku naplatu za dobrovoljnu podršku, bez otključavanja dodatnih funkcija.
