# QA report — Raspored 1.10.0

## Status

**CI validacija je obvezna prije izdanja.**

## U ovoj iteraciji

1. Dnevna smjena D postavljena je na 07:00–19:00 i računa 12 sati.
2. Noćna smjena N postavljena je na 19:00–07:00 i računa 12 sati.
3. Jutarnja smjena J postavljena je na 07:00–15:00 i računa 8 sati.
4. GO i BO priznaju 8 sati na radni dan.
5. Prazna ćelija na hrvatskom državnom blagdanu koji pada na radni dan priznaje 8 sati.
6. Dodan je obračun odrađenih, redovnih, fonda, prekovremenih, plaćenih odsutnosti i ukupno priznatih sati.
7. Dodani su automatizirani Android testovi za trajanje smjena, blagdan i prekovremeni rad.
8. Aplikacija se otvara na trenutačnom mjesecu.
9. Godišnji pregled ostaje poredan siječanj–prosinac, ali se pri otvaranju pomiče na trenutačni mjesec.
10. Korisnik može promijeniti boju kockice i teksta za N, D, P, J, GO i BO.
11. Uklonjeni su produkcijski demo podaci iz rasporeda.
12. Dodana je dobrovoljna podrška kroz Google Play Billing i StoreKit bez otključavanja funkcija.
13. Veliki Kotlin/Swift UI i model fajlovi razdvojeni su u manje tematske cjeline.
14. README više ne učitava badge slike s vanjskog CDN-a; svi vizualni asseti koje prikazuje nalaze se u repozitoriju.
15. Hrvatski tekstovi i nazivi ostaju obvezni na obje platforme.

16. Popodnevna smjena P zadano koristi 14:00–22:00 i računa 8 sati.
17. Ugrađene radne smjene N, D, P i J imaju zaključane produkcijske intervale radi dosljednog obračuna, dok se njihove boje mogu prilagoditi.
18. Vlastite smjene mogu se ponovno uređivati bez promjene njihove šifre.
19. Obračunski profil za bolnički način rada ostaje interni i ne traži ručni unos sektora, grada ni osnovice.
20. Stare prilagodbe boja ugrađenih smjena migriraju se bez gubitka korisničkih postavki.
21. Obračun je dodatno validiran prema anonimiziranim stvarnim fondovima 176 h, 184 h i 168 h za lipanj–kolovoz 2026.
22. Noćna smjena preko ponoći sada se u mjesečnoj analitici raspoređuje prema stvarnom kalendarskom datumu.
23. Sažetak prikazuje dnevne, noćne, subotnje, nedjeljne, blagdanske i 14–22 sate.
24. Mjesečni prikaz je u načinu pregleda zaštićen od slučajnog uređivanja.
25. U načinu uređivanja odabrana smjena se upisuje jednim dodirom, a brisanje je posebna radnja.
26. Blok „Vrste smjena” ispod kalendara nije prisutan; kalendar dobiva maksimalnu raspoloživu visinu.
27. Novi set referentnih slika korišten je za daljnje vizualno poliranje bez kopiranja demo rasporeda ili zastarjelih vremena smjena.
28. Android i iOS koriste usklađenu dublju glass paletu, aktivne cijan obrube i jači kontrast kartica.
29. Header, glavni tabovi i kompaktna traka uređivanja dodatno su usklađeni 1:1.
30. Postavke imaju unutarnje glass redove za prekidače, segmentirane kontrole, jezik i privatnost na obje platforme.
31. Dokumentacijski opis licence usklađen je sa stvarnom GPL-3.0 licencom repozitorija.
32. Mjesečne i godišnje smjenske ćelije koriste isti slojeviti glass/neon tretman na Androidu i iOS-u.
33. Godišnji pregled ima horizontalnu legendu N/D/GO/J/P/BO, dok mjesečni prikaz ostaje bez zasebne kartice „Vrste smjena”.
34. Sažetak i ekran Smjene koriste isti vizualni tretman oznaka smjena kao kalendar.
35. Aktivna segmentirana stanja u Sažetku dodatno su usklađena s cijan vizualnim sustavom.
36. Nova smjena na Androidu i iOS-u koristi iste dimenzije kartica, tabova, birača boja i akcijskih gumba.
37. Android Nova smjena ostaje dostupna iznad tipkovnice zahvaljujući IME paddingu.
38. iOS Nova smjena koristi veliki sheet, drag indikator i interaktivno zatvaranje tipkovnice.
39. Splash na obje platforme koristi isti halo oko app marke, slojevite pločice i usklađenu progress traku.
40. Modal Smjene ima usklađen close gumb i isti vizualni prioritet primarne i sekundarne akcije.
41. Iznad mjesečnog kalendara prikazuju se Fond sati, Prekovremeni i Plaća iz stvarnog odabranog mjeseca.
42. Dodir na datum otvara birač smjene na Androidu i iOS-u bez prethodnog uključivanja načina uređivanja.
43. Odabir D/N/J/P/GO/BO ili vlastite smjene odmah sprema promjenu za odabrani datum.
44. Brisanje smjene iz datuma zasebna je akcija i ne ovisi o ponovnom odabiru iste smjene.
45. Stari mjesečni način „Uredi raspored” više nije dio produkcijskog toka.
46. Sažetak je razdvojen na Smjene / Sati / Plaća i prikazuje samo odabranu cjelinu.
47. Android i iOS imaju isti redoslijed N/D/P/J/GO/BO u pregledu smjena.

## Obvezna CI provjera

- Android unit testovi
- Android lint
- Android debug APK build
- Android AAB build
- APK/AAB artifact verify
- iOS XcodeGen
- iOS Simulator build bez code-signinga
- iOS device archive bez code-signinga
- unsigned IPA pakiranje
- dead-code hygiene provjera

Izdanje 1.9.0 smatra se provjerenim tek kada Android i iOS CI završe zeleno.

## Obračun kontrolnih vrijednosti

Anonimizirane stvarne obračunske isprave iz zdravstvenog sustava koriste se samo kao kontrolne vrijednosti algoritma:

- lipanj 2026.: fond 176 h, ukupno 181 h, prekovremeno 5 h
- srpanj 2026.: fond 184 h, ukupno 216 h, prekovremeno 32 h
- kolovoz 2026.: fond 168 h, ukupno 211 h, prekovremeno 43 h

Android unit testovi provjeravaju fondove 176/184/168 h. Poseban test provjerava da se noćna smjena 19:00–07:00 na granici mjeseca raspodjeljuje prema stvarnom kalendarskom datumu. Detalji su u `qa/HOSPITAL_WORKTIME_VALIDATION.md`.

## 1:1 UI/UX provjera 1.9.0

- header i primarne akcije
- glavni tabovi
- mjesečni kalendar i navigacija
- smjenske ćelije i vikendi
- legenda i donja alatna traka
- način uređivanja
- izbornik Smjene
- uređivanje boja ugrađenih smjena
- Nova smjena
- Godina
- Sažetak i obračun sati
- Postavke i prekidači
- dobrovoljna podrška
- Android launcher ikona
- iOS App Icon

## Distribucijski artefakti 1.10.0

CI mora provjeriti i objaviti:

- Android APK
- Android AAB
- iOS Simulator .app ZIP
- unsigned iOS .xcarchive ZIP
- unsigned IPA

GitHub Release smije se objaviti tek nakon uspješnih Android i iOS jobova.

## Provjere 1.10.0

- [ ] Android unit, lint, build.
- [ ] iOS Simulator build i device archive.
- [ ] Validacija kodnog toka: promjena mjeseca, odabir smjene, očuvanje ranijeg i budućeg rasporeda.
- [ ] Fond automatski / ručni / reset, preračun prekovremenih i procjene plaće na oba OS-a.
- [ ] Uvoz / izvoz kompatibilnog JSON formata s kontrolom veličine i merge-only pravilom.
- [ ] Potvrđene isplate prikazuju godišnji zbroj i prosjek tri posljednje plaće bez miješanja procjena.
- [ ] Ikone na stvarnom Android i iOS uređaju — nije potvrđena pikselna identičnost samo iz izvornog koda.
- [ ] Pravila trgovina, javne pravne stranice i podrška — nije potvrđena automatska certifikacija.

## Neto bez osobnih obustava — v1.11.0

- [ ] Android unit test: bruto 1 – MIO I – MIO II – porez = prikazani neto prije obustava.
- [ ] iOS Simulator i archive potvrđuju isti tekst i model izračuna.
- [ ] Krediti, ovrhe, administrativne zabrane i druge obustave ne umanjuju procjenu na Androidu/iOS-u.
- [ ] U potvrđeni mjesečni neto unosi se vrijednost prije obustava, a ne bankovna isplata nakon obustava.
- [ ] Rasporedi/smjene i njihove pohranjene vrijednosti ostaju netaknuti.

## 1.12.0 – podsjetnici za smjene

- [ ] Android unit testovi provjeravaju D 06:00, N 18:00, večer prije 20:00, brisanje smjene i ograničenje budućeg horizonta.
- [ ] Android stvarni uređaj: POST_NOTIFICATIONS upit, dolazak obavijesti, restart, promjena vremenske zone i uređivanje datuma.
- [ ] iOS stvarni uređaj: dopuštenje obavijesti, ispravan prikaz pri otvorenoj/zatvorenoj aplikaciji, Focus mode, ponovno zakazivanje i lokalna privatnost.
- [ ] iOS screenshot QA pokazuje novu postavku (na kraju testa, nakon što se dozvola izričito uključi); test bez pravih notifikacija ne dokazuje isporuku u pozadini.
- [ ] Screenshot, Simulator build, unsigned device archive, IPA i Android APK/AAB potvrđeni nakon posljednjeg commita.
- [ ] Nije moguće jamčiti kontinuirani zvučni alarm pomoću standardnih obavijesti; korisnički tekst to mora eksplicitno navoditi.
- [ ] Upisane smjene ostaju trajno spremljene, neovisno o statusu ili isključivanju podsjetnika.
