# Raspored 1.12.1 — sinkronizacija podsjetnika

## Otklonjeni problem

Swift `@Published` objavljuje novu vrijednost prije dovršetka upisa u svojstvo (`willSet`).
Stari `RootView` reagirao je na događaj, ali je ponovno čitao `schedule.entries`
ili `settings.*`, koji su u tom trenutku mogli još sadržavati **prethodnu** vrijednost.
To je moglo ostaviti stari podsjetnik nakon brisanja smjene ili zadržati
obavijesti nakon isključivanja određenog prekidača.

Sada `onReceive` prosljeđuje snimku vrijednosti iz samog događaja. Ostale
vrijednosti čitaju se iz pohrane, a raspoređivanje se provodi asinkrono.
Scheduler serijalizira upise prema `UNUserNotificationCenter` i zadržava samo
najnoviji zahtjev dok je prethodni u tijeku. Time se sprječava utrka u kojoj
stari asinkroni `add` ponovno postavi obavijest nakon novijeg brisanja.

## Automatizirane provjere

- Android JUnit: D/N vrijeme, izbacivanje prošlih obavijesti, prekidači,
  brisanje/zamjena smjena i inkluzivna granica od 60 dana.
- macOS CI prije iOS Simulator builda: Swift izvršivi test istih pravila,
  jedinstveni ID-ovi, prekidači, današnja N smjena i dan 60/61.
- Oba sustava: obavijesti su lokalne, D u 06:00, N u 18:00, obje večer prije u 20:00.
- Android nastavlja dnevni rolling refresh; iOS koristi do 60 najbližih
  obavijesti u idućih 60 dana. Zakazivanje ne mijenja raspored.

## Ručna provjera na stvarnom uređaju (nije zamjena za CI)

1. Dopuštene obavijesti; dodati D za sutra i potvrditi večernji podsjetnik.
2. Brisati D prije zakazanog vremena; potvrditi uklanjanje oba pending podsjetnika.
3. Promijeniti D u N; očekivati 18:00 umjesto 06:00.
4. Isključiti glavni prekidač; potvrditi praznu listu upravljanih podsjetnika.
5. Ponovno uključiti i zasebno mijenjati večernji i odlazni prekidač.
6. Zatvoriti i vratiti aplikaciju, promijeniti vremensku zonu i ponoviti provjeru.
7. Provjeriti Focus/Ne ometaj i ograničenja pozadinske dostave.

**Granica:** CI provjerava plan i build, ali stvarnu isporuku, zvuk i Focus
mora potvrditi test na uređaju. Ovo nije neprekidni alarm.
