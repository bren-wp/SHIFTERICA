import SwiftUI

private enum CalendarInformation {
    static let privacy = """
    Raspored_app pohranjuje unose i odabir prikaza na uređaju. Ne zahtijeva račun,
    nema oglase ni analitičko praćenje. Uvoz i izvoz obavljate samo vlastitom radnjom
    kroz sistemski odabir datoteka. Izvezene JSON datoteke nisu šifrirane: čuvajte
    ih na sigurnom mjestu. Prije brisanja aplikacije izvezite sigurnosnu kopiju.
    """

    static let terms = """
    Raspored_app služi za osobno planiranje smjena, događaja i bilješki.
    Prije važnih obveza provjerite unesene datume i vremena i redovito
    spremite sigurnosnu kopiju. Dostupnost datoteka ovisi o sustavu uređaja.
    Unesene obveze i vremena provjerite prije njihove uporabe.
    """
}

struct CalendarSettings: View {
    @Environment(\.dismiss) private var dismiss
    @Binding var weekView: Bool
    let onExport: () -> Void
    let onImport: () -> Void
    let notice: String?

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    HStack(spacing: 10) {
                        Image(systemName: "paintpalette")
                            .foregroundStyle(Palette.mint)
                        Text("Vizualno")
                            .font(.headline.weight(.bold))
                            .foregroundStyle(Palette.text)
                    }
                    Toggle(isOn: $weekView) {
                        VStack(alignment: .leading, spacing: 3) {
                            Text("Tjedni prikaz")
                                .foregroundStyle(Palette.text)
                            Text("Prikaži sedam dana umjesto cijelog mjeseca")
                                .font(.caption)
                                .foregroundStyle(Palette.secondary)
                        }
                    }
                    .tint(Palette.mint)
                }
                Section {
                    Label("Sigurnost podataka", systemImage: "externaldrive")
                        .font(.headline.weight(.bold))
                        .foregroundStyle(Palette.mint)
                    Text("Izvezite smjene, datume i bilješke prije promjene uređaja.")
                        .font(.caption)
                        .foregroundStyle(Palette.secondary)
                    Button(action: onExport) {
                        Label("Izvezi raspored", systemImage: "square.and.arrow.up")
                    }
                    .frame(minHeight: 44)
                    Button(action: onImport) {
                        Label("Uvezi raspored", systemImage: "square.and.arrow.down")
                    }
                    .frame(minHeight: 44)
                    Text("Uvoz spaja nedostajuće zapise bez brisanja postojećih.")
                        .font(.caption)
                        .foregroundStyle(Palette.secondary)
                    if let notice {
                        Text(notice).font(.caption).foregroundStyle(Palette.mint)
                    }
                }
                Section {
                    HStack(spacing: 10) {
                        Image(systemName: "lock.shield")
                            .foregroundStyle(Palette.mint)
                        Text("Podrška i privatnost")
                            .font(.headline.weight(.bold))
                            .foregroundStyle(Palette.text)
                    }
                    NavigationLink("Politika privatnosti") {
                        information("Politika privatnosti", text: CalendarInformation.privacy)
                    }
                    NavigationLink("Uvjeti korištenja") {
                        information("Uvjeti korištenja", text: CalendarInformation.terms)
                    }
                }
                Section {
                    Label("O aplikaciji", systemImage: "info.circle")
                        .font(.headline.weight(.bold))
                        .foregroundStyle(Palette.mint)
                    Text("Autor: Brendigo")
                        .foregroundStyle(Palette.text)
                    Text("Verzija: " +
                         (Bundle.main.infoDictionary?["CFBundleShortVersionString"]
                          as? String ?? "—"))
                        .font(.caption)
                        .foregroundStyle(Palette.secondary)
                }
            }
            .scrollContentBackground(.hidden)
            .background(Palette.background)
            .tint(Palette.mint)
            .navigationTitle("Postavke")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Gotovo") { dismiss() }
                }
            }
        }
    }

    private func information(_ title: String, text: String) -> some View {
        ScrollView {
            Text(text)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding()
        }
        .navigationTitle(title)
        .navigationBarTitleDisplayMode(.inline)
    }
}
