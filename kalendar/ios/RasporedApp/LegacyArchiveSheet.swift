import SwiftUI

/// Read-only access to the complete pre-migration event archive.
struct LegacyArchiveSheet: View {
    @ObservedObject var archive: CalendarStore
    @Environment(\.dismiss) private var dismiss
    @State private var searchText = ""

    private var results: [CalendarEntry] {
        let sorted = archive.entries.sorted {
            if $0.date != $1.date { return $0.date < $1.date }
            if $0.start != $1.start { return $0.start < $1.start }
            if $0.title != $1.title { return $0.title < $1.title }
            return $0.id < $1.id
        }
        let query = searchText.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !query.isEmpty else { return sorted }
        return sorted.filter { entry in
            [entry.date, entry.title, entry.note, entry.category, entry.start, entry.end]
                .contains { $0.localizedCaseInsensitiveContains(query) }
        }
    }

    var body: some View {
        NavigationStack {
            List {
                Section {
                    let matches = results
                    if matches.isEmpty {
                        Text(
                            archive.entries.isEmpty
                                ? "Nema starih događaja. JSON kopiju možete uvesti u Postavkama."
                                : "Nema događaja za ovu pretragu."
                        )
                        .foregroundStyle(RColors.muted)
                    } else {
                        ForEach(matches) { item in
                            VStack(alignment: .leading, spacing: 4) {
                                Text(item.date + " · " + item.category)
                                    .font(.caption)
                                    .foregroundStyle(RColors.muted)
                                Text(item.title.isEmpty ? "Napomena" : item.title)
                                    .font(.headline)
                                if !item.start.isEmpty {
                                    Text(item.start + " – " + item.end)
                                        .font(.subheadline)
                                }
                                if !item.note.isEmpty {
                                    Text(item.note)
                                        .font(.subheadline)
                                        .lineLimit(5)
                                }
                            }
                            .padding(.vertical, 4)
                        }
                    }
                } header: {
                    Text("Pronađeno: \(results.count) · Arhiva: \(archive.entries.count)")
                }
            }
            .listStyle(.plain)
            .searchable(text: $searchText, prompt: "Datum, naziv, kategorija ili bilješka")
            .navigationTitle("Prethodni događaji")
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Zatvori") { dismiss() }
                }
            }
        }
    }
}
