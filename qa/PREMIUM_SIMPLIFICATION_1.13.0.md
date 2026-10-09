# Raspored v1.13.0 — kontrola jednostavnijeg premium sučelja

## Opseg
- Android/iOS Sažetak: samo **Smjene** i **Sati**, bez dodatnih kartica plaća/profil/godišnja zarada.
- U mjesečnom zaglavlju **Neto ≈** kada se računa iz rasporeda; **Neto plaća** samo kada već postoji lokalno potvrđena vrijednost za točno taj mjesec.
- Kalkulator i lokalno pohranjene vrijednosti zadržavaju se radi kompatibilnosti i nakon uklanjanja obrazaca iz sučelja. Procjena nije službeni obračun.
- Strelice oko naziva mjeseca rade bez napuštanja mjesečnog prikaza.
- Uvoz pojedinačne smjene putem JSON teksta nije dio glavnog UI-ja; uvoz/izvoz sigurnosne kopije rasporeda ostaju dostupni.
- Splash indikator stvarno animira od 0 do 100 % tijekom uvodnog prikaza.
- Ugrađene radne smjene D 07:00–19:00, N 19:00–07:00, J 07:00–15:00, P 14:00–22:00 ostaju zadane; izmijenjena vremena su lokalno spremljena, uređuju se u 24-satnom formatu, a **Vrati zadano** vraća i boje i satnicu. GO/BO ostaju plaćene odsutnosti bez intervala.
- Večernji D/N podsjetnik je u 20:00, a podsjetnik prije početka prati prilagođeno vrijeme smjene minus jedan sat.

## Kontrola obračuna i privatnosti

Obračun neta uzima u obzir zakonske doprinose, porez, osobni odbitak, osnovice i dodatke za evidentirane smjene. Povijesne stope i osnovice za 2025./2026. održavaju se u odvojenom obračunskom modulu. Potpun neto iznos ne može se jamčiti bez cjelovitog rasporeda, točnog osobnog poreznog profila i stavki platne liste koje aplikacija ne pohranjuje. Nikakva identifikacijska informacija, originalni PDF, stvarna platna lista ili osobni iznos nisu uključeni u repozitorij ili automatizirane testove.

## Regresijska kontrola

- [ ] Android JUnit / Kotlin compile, lint, dead-code audit, APK + AAB upload.
- [ ] iOS Swift regression, XcodeGen, Simulator compile i snimka svih stvarnih ekranâ, device archive, unsigned IPA.
- [ ] Izmjena D/N početka mijenja početak rada, obračun sati i vrijeme pripadajuće obavijesti.
- [ ] Smjena koja završava idućeg dana ostaje obračunata preko ponoći.
- [ ] Ponovno pokretanje aplikacije i promjena mjeseca čuvaju korisničke zapise.
- [ ] Reset vraća sva ugrađena zadana vremena; stare samo-color postavke ostaju čitljive.
- [ ] Čitanje sigurnosne kopije starijeg formata i merge bez prepisivanja postojećih datuma.
- [ ] Provjera stvarne isporuke lokalnih obavijesti na Android/iOS uređajima odvojena je od CI-ja.
- [ ] Release tek nakon zelenog CI-ja na završnom PR SHA i na `main`.

Screenshot QA potvrđuje stvarni prikaz aplikacije u simulatoru, ali ne dokazuje pikselnu identičnost na svim fizičkim uređajima.
