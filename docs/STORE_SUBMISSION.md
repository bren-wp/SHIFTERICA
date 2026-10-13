# Raspored — konfiguracija trgovina

Ovaj dokument opisuje produkcijsku konfiguraciju za Google Play i App Store.

## Dobrovoljna podrška

Podrška je jednokratna i ne otključava funkcije, sadržaj niti pogodnosti. Aplikacija ostaje potpuno funkcionalna bez kupnje.

Koriste se tri identična ID-a na obje platforme:

- raspored_tip_small
- raspored_tip_medium
- raspored_tip_large

Na Google Playu proizvodi trebaju biti jednokratni INAPP proizvodi. Na App Storeu trebaju biti Consumable In-App Purchase proizvodi.

Cijene se ne zapisuju u aplikaciju. Sučelje prikazuje cijenu koju vrati Google Play ili App Store za korisnikovu trgovinu i valutu.

## Zahtjevi platformi — provjereno 14. listopada 2026.

- Google Play od 31. kolovoza 2026. za nove i ažurirane standardne Android aplikacije zahtijeva **Android 16 / target API 36**. Oba projekta koriste `compileSdk = 36` i `targetSdk = 36` u Gradle konfiguraciji. To samo po sebi ne potvrđuje uspješnu predaju u Play Console.
  Izvor: https://support.google.com/googleplay/android-developer/answer/11926878
- Apple od 28. travnja 2026. za App Store Connect zahtijeva **Xcode 26 ili noviji i iOS 26 SDK ili noviji**. CI provjerava Xcode 26 i iOS 26 SDK; iOS aplikacije ciljaju iOS 17 kao najnižu podržanu verziju.
  Izvor: https://developer.apple.com/news/upcoming-requirements/
- Trenutačni javni Android APK/AAB su **debug**, a iOS IPA/XCArchive **unsigned**. Nisu zamjena za potpisane produkcijske pakete. Prije objave potrebni su sigurni ključevi/potvrde, Play App Signing, Apple provisioning, store metapodaci i odgovarajuće deklaracije o privatnosti.
- Uspješan build nije potvrda prihvaćanja od strane trgovine. Dokumentirajte zasebne provjere i stvarne račune za oba identifikatora aplikacija.

## Android

- Billing biblioteka: Google Play Billing 9.1.0.
- Kupnja se pokreće samo korisnikovim pritiskom na odabranu opciju.
- Potvrđena jednokratna podrška konzumira se kako bi se mogla ponoviti.
- Aplikacija ne prima broj kartice niti druge podatke o platnom instrumentu.

## iOS

- Integracija koristi StoreKit 2.
- Kupnja se pokreće samo korisnikovim pritiskom.
- Potvrđena transakcija završava se nakon StoreKit verifikacije.
- Aplikacija ne prima broj kartice niti druge podatke o platnom instrumentu.

## Privatnost

Raspored, smjene, prilagodbe boja i postavke pohranjuju se u spremnik aplikacije na uređaju. Aplikacija nema oglasne trackere niti vlastiti poslužitelj za raspored rada.

StoreKit i Google Play Billing komuniciraju s trgovinom platforme samo kada je to potrebno za prikaz ili obradu dobrovoljne kupnje.

## Vizualni resursi

Logo, ikone i prikazi korišteni u aplikaciji i README-u nalaze se u repozitoriju. Produkcijski UI ne učitava vizualne resurse s CDN-a.
