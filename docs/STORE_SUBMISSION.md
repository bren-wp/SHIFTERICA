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
