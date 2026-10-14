import SwiftUI

/// Keep the primary calendar at full height; search and multiple records use a sheet.
struct CalendarRecordsSheet: View {
    @Environment(\.dismiss) private var dismiss
    let date: Date
    let entries: [CalendarEntry]
    let searchAllInitially: Bool
    let onEdit: (CalendarEntry) -> Void
    let onAdd: () -> Void
    let onPreset: (CalendarShiftPreset) -> Bool

    @State private var query = ""
    @State private var visibleCount = 80
    @State private var saveError = false
    @FocusState private var searchFocused: Bool

    private var results: [CalendarEntry] {
        if !query.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            return CalendarSearch.find(entries, query: query)
        }
        if searchAllInitially {
            return entries.sorted { a, b in
                if a.date != b.date { return a.date < b.date }
                if a.start != b.start { return a.start < b.start }
                return a.id < b.id
            }
        }
        return CalendarRules.forDay(entries, key: CalendarClock.key(date))
    }

    private var quickShiftChoices: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("ODABERI SMJENU")
                .font(.system(size: 12, weight: .black))
                .foregroundStyle(Palette.text)
            ForEach(0..<2, id: \.self) { row in
                HStack(spacing: 8) {
                    ForEach(0..<3, id: \.self) { column in
                        let preset = CalendarShiftPresets.builtIn[row * 3 + column]
                        let alreadyPresent = CalendarShiftPresets.alreadyScheduled(
                            entries, preset: preset, on: date
                        )
                        Button {
                            if onPreset(preset) {
                                dismiss()
                            } else {
                                saveError = true
                            }
                        } label: {
                            VStack(spacing: 4) {
                                Text(preset.code)
                                    .font(.system(size: 23, weight: .black))
                                Text((alreadyPresent ? "✓ " : "") + preset.title)
                                    .font(.system(size: 10, weight: .semibold))
                                    .lineLimit(1)
                                    .minimumScaleFactor(0.55)
                            }
                            .foregroundStyle(Palette.background)
                            .frame(maxWidth: .infinity, minHeight: 67)
                            .background(
                                Palette.colors[preset.color],
                                in: RoundedRectangle(cornerRadius: 16)
                            )
                            .overlay(RoundedRectangle(cornerRadius: 16)
                                .stroke(alreadyPresent ? Palette.background
                                    : .white.opacity(0.38),
                                    lineWidth: alreadyPresent ? 2 : 1))
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
        }
    }

    var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 13) {
                HStack {
                    Image(systemName: "magnifyingglass")
                        .foregroundStyle(Palette.secondary)
                    TextField("Pretraži nazive i napomene", text: $query)
                        .focused($searchFocused)
                        .textInputAutocapitalization(.never)
                        .submitLabel(.search)
                    if !query.isEmpty {
                        Button {
                            query = ""
                        } label: {
                            Image(systemName: "xmark.circle.fill")
                        }
                        .accessibilityLabel("Očisti pretragu")
                    }
                }
                .padding(14)
                .background(Palette.panel, in: RoundedRectangle(cornerRadius: 16))
                if !searchAllInitially && query.isEmpty {
                    quickShiftChoices
                }
                if saveError {
                    Text("Nije moguće spremiti zapis. Provjeri slobodan prostor.")
                        .font(.caption)
                        .foregroundStyle(Palette.weekend)
                }
                HStack {
                    VStack(alignment: .leading) {
                        Text(searchAllInitially ? "Svi zapisi" :
                            CalendarClock.title(date, format: "EEEE, d. MMMM yyyy."))
                            .font(.headline)
                            .foregroundStyle(Palette.text)
                        Text(String(results.count) + " zapisa")
                            .font(.caption)
                            .foregroundStyle(Palette.secondary)
                    }
                    Spacer()
                    Button("Dodaj +", action: onAdd)
                        .font(.subheadline.bold())
                        .foregroundStyle(Palette.mint)
                }
                ScrollView {
                    LazyVStack(spacing: 9) {
                        if results.isEmpty {
                            Text("Nema zapisa. Dodaj smjenu, događaj ili napomenu.")
                                .font(.subheadline)
                                .foregroundStyle(Palette.secondary)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .padding(20)
                                .background(
                                    Palette.panel, in: RoundedRectangle(cornerRadius: 18)
                                )
                        }
                        ForEach(results.prefix(visibleCount)) { item in
                            CalendarRecordRow(item: item) { onEdit(item) }
                        }
                        if results.count > visibleCount {
                            Button {
                                visibleCount = min(results.count, visibleCount + 80)
                            } label: {
                                Text("Prikaži još · preostalo " +
                                     String(results.count - visibleCount))
                                    .frame(maxWidth: .infinity, minHeight: 48)
                            }
                            .tint(Palette.mint)
                            .buttonStyle(.bordered)
                        }
                    }
                }
                .scrollDismissesKeyboard(.interactively)
            }
            .padding(16)
            .background(Palette.background.ignoresSafeArea())
            .navigationTitle(searchAllInitially ? "Pretraži" : "Zapisi i smjene")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Gotovo") { dismiss() }
                        .tint(Palette.mint)
                }
            }
        }
        .task {
            if searchAllInitially { searchFocused = true }
        }
        .onChange(of: query) { _, _ in visibleCount = 80 }
    }
}

private struct CalendarRecordRow: View {
    let item: CalendarEntry
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 12) {
                RoundedRectangle(cornerRadius: 4)
                    .fill(Palette.colors[item.color])
                    .frame(width: 5, height: 44)
                VStack(alignment: .leading, spacing: 5) {
                    Text(item.title.isEmpty ? "Napomena" : item.title)
                        .font(.subheadline.bold())
                        .foregroundStyle(Palette.text)
                        .lineLimit(2)
                    Text([item.category, item.date,
                          item.start.isEmpty ? "" : item.start + " – " + item.end]
                            .filter { !$0.isEmpty }.joined(separator: " · "))
                        .font(.caption)
                        .foregroundStyle(Palette.secondary)
                    if !item.note.isEmpty {
                        Text(item.note)
                            .font(.caption)
                            .foregroundStyle(Palette.secondary)
                            .lineLimit(2)
                    }
                }
                Spacer()
                Image(systemName: "chevron.right")
                    .foregroundStyle(Palette.mint)
            }
            .padding(14)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Palette.panel, in: RoundedRectangle(cornerRadius: 16))
            .overlay(RoundedRectangle(cornerRadius: 16)
                .stroke(Palette.stroke.opacity(0.5), lineWidth: 1))
        }
        .buttonStyle(.plain)
        .accessibilityLabel("Uredi: " + (item.title.isEmpty ? "Napomena" : item.title))
    }
}
