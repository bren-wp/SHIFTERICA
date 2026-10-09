# iOS Simulator screenshot QA — oporavak od zastoja pri pokretanju

## Utvrđeni pad nakon PR-a #12

GitHub Actions main run [#37859834338](https://github.com/bren-wp/SHIFTERICA/actions/runs/37859834338), iOS job:
- Swift regresijski testovi, XcodeGen i iOS Simulator build završili uspješno.
- iPhone 16 Pro (iOS 18.5) je bootan, aplikacija je instalirana.
- Prvo pokretanje `simctl launch --terminate-running-process ... --documentation-screen month`
  nije dovršeno u 60 s; Python je bacio `subprocess.TimeoutExpired`.
- CI je prekinuo obvezno snimanje stvarnih zaslona, pa release job nije krenuo.
- Log ne dokazuje rušenje aplikacije niti neispravnost izračuna.

## Izmjena

- Kod prvog pokretanja ne koristi se `--terminate-running-process`, jer se
  aplikacija još nije pokrenula.
- Prvi zaslon dobiva do tri ograničena pokušaja, ostali do dva.
- Nakon privremenog timeouta ponovno se kratko provjerava boot status
  i dopušta sustavu da dovrši inicijalizaciju.
- Ako se stvarno pokretanje ne uspije postići, QA i dalje pada s korisnom
  dijagnostikom; nema lažnih snimaka ni zaobilaženja obvezne provjere.
- Ostaje obvezno svih sedam stvarnih screenshotova, provjera najmanje
  `month`, `year` i `settings`, upload, simulator build i iOS archive.

## Kriteriji završetka

- [ ] PR CI (Android + iOS uključujući snimke) zelen.
- [ ] Nakon spajanja u main, glavni CI zelen.
- [ ] Postoji stvarni v1.12.1 release s oba Android i iOS paketa.
- [ ] Nema promjena lokalnih rasporeda niti korisničkih podataka.
