import SwiftUI

private enum CalendarInformation {
    static let privacy = """
    Raspored_app pohranjuje unose i odabir prikaza na uređaju. Ne zahtijeva račun,
    nema oglase ni analitičko praćenje. Uvoz i izvoz obavljate samo vlastitom radnjom
    kroz sistemski odabir datoteka. Izvezene JSON datoteke nisu šifrirane: čuvajte
    ih na sigurnom mjestu. Prije brisanja aplikacije izvezite sigurnosnu kopiju.
    """

    static let terms = """
    Raspored_app služi za osobno planiranje događaja, termina, obveza i smjena.
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
            ScrollView {
                VStack(alignment: .leading, spacing: 12) {
                    Text("Postavke")
                        .font(.system(size: 30, weight: .black))
                        .foregroundStyle(Palette.text)
                    Text("Prilagodite Raspored svojim potrebama")
                        .font(.system(size: 14))
                        .foregroundStyle(Palette.secondary)

                    preferencesPanel
                    dataPanel
                    privacyPanel
                    aboutPanel
                }
                .padding(.horizontal, 17)
                .padding(.top, 12)
                .padding(.bottom, 28)
            }
            .scrollDismissesKeyboard(.interactively)
            .background(Palette.bg2.ignoresSafeArea())
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Gotovo") { dismiss() }
                        .tint(Palette.mint)
                }
            }
        }
        .tint(Palette.mint)
    }

    private var preferencesPanel: some View {
        settingsPanel("Više prilagodbi", symbol: "slider.horizontal.3") {
            Toggle(isOn: $weekView) {
                VStack(alignment: .leading, spacing: 3) {
                    Text("Tjedni prikaz")
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundStyle(Palette.text)
                    Text("Prikaži sedam dana umjesto cijelog mjeseca")
                        .font(.caption)
                        .foregroundStyle(Palette.secondary)
                }
            }
            .tint(Palette.mint)
        }
    }

    private var dataPanel: some View {
        settingsPanel("Sigurnost podataka", symbol: "externaldrive.fill") {
            settingAction(
                title: "Izvezi raspored",
                caption: "Događaji, datumi, boje i vremena",
                symbol: "square.and.arrow.up",
                action: onExport
            )
            settingAction(
                title: "Uvezi raspored",
                caption: "Dopuni kalendar bez prepisivanja postojećih zapisa",
                symbol: "square.and.arrow.down",
                action: onImport
            )
            Text("Kopiju redovito izvezite prije promjene ili brisanja uređaja.")
                .font(.caption)
                .foregroundStyle(Palette.secondary)
            if let notice {
                Text(notice)
                    .font(.caption)
                    .foregroundStyle(Palette.mint)
            }
        }
    }

    private var privacyPanel: some View {
        settingsPanel("Podrška i privatnost", symbol: "lock.shield.fill") {
            NavigationLink {
                information("Politika privatnosti", text: CalendarInformation.privacy)
            } label: {
                settingsLink("Politika privatnosti")
            }
            NavigationLink {
                information("Uvjeti korištenja", text: CalendarInformation.terms)
            } label: {
                settingsLink("Uvjeti korištenja")
            }
        }
    }

    private var aboutPanel: some View {
        settingsPanel("O aplikaciji", symbol: "info.circle.fill") {
            HStack {
                Text("Autor").foregroundStyle(Palette.secondary)
                Spacer()
                Text("Brendigo").foregroundStyle(Palette.text)
            }
            HStack {
                Text("Verzija").foregroundStyle(Palette.secondary)
                Spacer()
                Text(Bundle.main.infoDictionary?["CFBundleShortVersionString"]
                     as? String ?? "—")
                    .foregroundStyle(Palette.text)
            }
        }
    }

    private func settingsPanel<Content: View>(
        _ title: String, symbol: String,
        @ViewBuilder content: () -> Content
    ) -> some View {
        VStack(alignment: .leading, spacing: 11) {
            HStack(spacing: 9) {
                Image(systemName: symbol)
                    .font(.system(size: 16, weight: .bold))
                    .foregroundStyle(Palette.mint)
                Text(title)
                    .font(.system(size: 17, weight: .black))
                    .foregroundStyle(Palette.text)
            }
            content()
        }
        .padding(14)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Palette.panel, in: RoundedRectangle(cornerRadius: 21))
        .overlay(RoundedRectangle(cornerRadius: 21)
            .stroke(Palette.stroke.opacity(0.82), lineWidth: 1))
    }

    private func settingAction(
        title: String, caption: String, symbol: String,
        action: @escaping () -> Void
    ) -> some View {
        Button(action: action) {
            HStack(spacing: 9) {
                Image(systemName: symbol)
                    .font(.system(size: 17))
                    .foregroundStyle(Palette.mint)
                    .frame(width: 24)
                VStack(alignment: .leading, spacing: 3) {
                    Text(title)
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundStyle(Palette.text)
                    Text(caption)
                        .font(.system(size: 11))
                        .foregroundStyle(Palette.secondary)
                        .multilineTextAlignment(.leading)
                }
                Spacer(minLength: 3)
                Image(systemName: "chevron.right")
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundStyle(Palette.secondary)
            }
            .frame(maxWidth: .infinity, minHeight: 48)
            .padding(.horizontal, 10)
            .padding(.vertical, 5)
            .background(Palette.card2, in: RoundedRectangle(cornerRadius: 13))
            .overlay(RoundedRectangle(cornerRadius: 13)
                .stroke(Palette.stroke.opacity(0.6), lineWidth: 1))
        }
        .buttonStyle(.plain)
    }

    private func settingsLink(_ text: String) -> some View {
        HStack {
            Text(text).foregroundStyle(Palette.text)
            Spacer()
            Image(systemName: "chevron.right")
                .foregroundStyle(Palette.secondary)
        }
        .frame(minHeight: 43)
        .padding(.horizontal, 9)
        .background(Palette.card2, in: RoundedRectangle(cornerRadius: 12))
    }

    private func information(_ title: String, text: String) -> some View {
        ScrollView {
            Text(text)
                .foregroundStyle(Palette.text)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding()
        }
        .background(Palette.bg2)
        .navigationTitle(title)
        .navigationBarTitleDisplayMode(.inline)
    }
}
