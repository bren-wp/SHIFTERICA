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
    Aplikacija nije službeni obračun plaće niti evidencija radnog vremena.
    """
}

struct CalendarSettings: View {
    @Environment(\.dismiss) private var dismiss
    @Binding var weekView: Bool

    var body: some View {
        NavigationStack {
            Form {
                Section("Kalendar") {
                    Toggle("Tjedni prikaz", isOn: $weekView)
                }
                Section("Informacije") {
                    NavigationLink("Politika privatnosti") {
                        information("Politika privatnosti", text: CalendarInformation.privacy)
                    }
                    NavigationLink("Uvjeti korištenja") {
                        information("Uvjeti korištenja", text: CalendarInformation.terms)
                    }
                }
                Section {
                    Text("Autor: Brendigo")
                        .foregroundStyle(.secondary)
                }
            }
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
