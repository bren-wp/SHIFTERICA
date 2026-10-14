import SwiftUI
import UniformTypeIdentifiers

enum Palette {
    static let background = Color(red: 9/255, green: 15/255, blue: 27/255)
    static let bg2 = Color(red: 16/255, green: 33/255, blue: 57/255)
    static let panel = Color(red: 21/255, green: 36/255, blue: 59/255).opacity(0.94)
    static let card2 = Color(red: 27/255, green: 48/255, blue: 76/255)
    static let stroke = Color(red: 52/255, green: 82/255, blue: 115/255)
    static let text = Color(red: 244/255, green: 247/255, blue: 253/255)
    static let weekend = Color(red: 255/255, green: 160/255, blue: 181/255)
    static let empty = Color(red: 25/255, green: 43/255, blue: 66/255)
    static let weekendEmpty = Color(red: 49/255, green: 36/255, blue: 56/255)
    static let mint = Color(red: 124/255, green: 235/255, blue: 214/255)
    static let secondary = Color(red: 184/255, green: 199/255, blue: 219/255)
    static let selected = Color(red: 32/255, green: 81/255, blue: 106/255)
    static let colors: [Color] = [
        mint,
        Color(red: 0.471, green: 0.776, blue: 1.0),
        Color(red: 0.827, green: 0.702, blue: 1.0),
        Color(red: 1.0, green: 0.706, blue: 0.49),
        Color(red: 0.51, green: 0.875, blue: 0.678),
        Color(red: 0.973, green: 0.8, blue: 0.482)
    ]
}

struct EditorTarget: Identifiable {
    let id = UUID()
    let date: Date
    let item: CalendarEntry?
}

struct CalendarHome: View {
    @StateObject private var store = CalendarStore()
    @State private var month = CalendarClock.monthStart(Date())
    @State private var selected = Date()
    @State private var editor: EditorTarget?
    @State private var query = ""
    @State private var visibleCount = 80
    @State private var section: CalendarSectionIOS = .month
    @FocusState private var searchFocused: Bool
    @AppStorage("raspored.universal.weekView") private var weekView = false
    @State private var exportDocument: CalendarBackupDocument?
    @State private var showExport = false
    @State private var showImport = false
    @State private var showSettings = false
    @State private var backupNotice: String?

    private var items: [CalendarEntry] {
        if query.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            return CalendarRules.forDay(store.entries, key: CalendarClock.key(selected))
        }
        return CalendarSearch.find(store.entries, query: query)
    }

    var body: some View {
        VStack(spacing: 5) {
            CalendarParityHeader(
                onSearch: {
                    section = .month
                    searchFocused = true
                },
                onSettings: { showSettings = true },
                onAdd: { editor = EditorTarget(date: selected, item: nil) }
            )
            Group {
                switch section {
                case .month:
                    monthContent
                case .year:
                    CalendarParityYear(
                        year: CalendarClock.calendar.component(.year, from: month),
                        entries: store.entries,
                        onYearChange: { newYear in
                            let currentMonth = CalendarClock.calendar.component(.month, from: month)
                            if let target = CalendarClock.calendar.date(from:
                                DateComponents(year: newYear, month: currentMonth, day: 1)) {
                                month = target
                            }
                        },
                        onMonthSelect: { chosen in
                            month = CalendarClock.monthStart(chosen)
                            selected = chosen
                            query = ""
                            section = .month
                        }
                    )
                case .summary:
                    CalendarParitySummary(
                        month: month,
                        entries: store.entries,
                        onExport: exportBackup,
                        onImport: { showImport = true },
                        notice: backupNotice
                    )
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            CalendarParityBottomNav(section: $section)
        }
        .background(
            LinearGradient(
                colors: [Palette.background, Palette.bg2, Color(red: 0.027, green: 0.047, blue: 0.086)],
                startPoint: .top, endPoint: .bottom
            ).ignoresSafeArea()
        )
        .onChange(of: query) { _, _ in visibleCount = 80 }
        .onChange(of: selected) { _, _ in visibleCount = 80 }
        .sheet(isPresented: $showSettings) {
            CalendarSettings(weekView: $weekView)
                .presentationDragIndicator(.visible)
        }
        .sheet(item: $editor) { target in
            CalendarEditor(target: target, store: store) { savedDate in
                selected = savedDate
                month = CalendarClock.monthStart(savedDate)
                query = ""
            }
            .presentationDragIndicator(.visible)
        }
        .fileExporter(
            isPresented: $showExport,
            document: exportDocument,
            contentType: .json,
            defaultFilename: "Raspored_app-backup"
        ) { result in
            switch result {
            case .success:
                backupNotice = "Sigurnosna kopija spremljena."
            case .failure(let error):
                backupNotice = "Izvoz nije uspio: " + error.localizedDescription
            }
            exportDocument = nil
        }
        .fileImporter(isPresented: $showImport, allowedContentTypes: [.json]) { result in
            do {
                let url = try result.get()
                let access = url.startAccessingSecurityScopedResource()
                defer { if access { url.stopAccessingSecurityScopedResource() } }
                let attributes = try FileManager.default.attributesOfItem(atPath: url.path)
                let size = (attributes[.size] as? NSNumber)?.intValue ?? 0
                guard size <= CalendarBackup.maxBytes else {
                    throw CalendarBackupError.oversized
                }
                let data = try Data(contentsOf: url)
                let imported = try CalendarBackup.decode(data)
                let count = try store.mergeMissing(imported)
                backupNotice = "Uvezeno " + String(count) +
                    " novih zapisa. Postojeći su sačuvani."
            } catch {
                backupNotice = "Uvoz nije uspio: " + error.localizedDescription
            }
        }
    }

    private var monthContent: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                HStack(spacing: 10) {
                    Text("PREGLED MJESECA")
                        .font(.system(size: 12, weight: .black))
                        .foregroundStyle(Palette.mint)
                    Spacer()
                    Text("Bez plaća i obračuna")
                        .font(.caption2)
                        .foregroundStyle(Palette.secondary)
                }
                .padding(.horizontal, 2)

                HStack(spacing: 4) {
                    Button {
                        changeMonth(-1)
                    } label: {
                        Image(systemName: "chevron.left")
                            .frame(width: 44, height: 48)
                    }
                    .accessibilityLabel("Prethodni mjesec")
                    Text(CalendarClock.title(month, format: "LLLL yyyy").uppercased())
                        .font(.system(size: 22, weight: .black))
                        .foregroundStyle(Palette.text)
                        .frame(maxWidth: .infinity, alignment: .center)
                        .minimumScaleFactor(0.8)
                    Button {
                        changeMonth(1)
                    } label: {
                        Image(systemName: "chevron.right")
                            .frame(width: 44, height: 48)
                    }
                    .accessibilityLabel("Sljedeći mjesec")
                    Button("Danas") {
                        selected = Date()
                        month = CalendarClock.monthStart(selected)
                        query = ""
                    }
                    .font(.subheadline.weight(.semibold))
                    .padding(.leading, 6)
                }
                .foregroundStyle(Palette.mint)

                Picker("Prikaz kalendara", selection: $weekView) {
                    Text("Mjesec").tag(false)
                    Text("Tjedan").tag(true)
                }
                .pickerStyle(.segmented)
                .accessibilityLabel("Prikaz kalendara")

                calendarGrid

                HStack(spacing: 12) {
                    Button {
                        changeDay(-1)
                    } label: {
                        Label("Prethodni dan", systemImage: "chevron.left")
                            .frame(maxWidth: .infinity, minHeight: 44)
                    }
                    Button {
                        changeDay(1)
                    } label: {
                        Label("Sljedeći dan", systemImage: "chevron.right")
                            .frame(maxWidth: .infinity, minHeight: 44)
                    }
                }
                .buttonStyle(.bordered)
                .tint(Palette.mint)

                HStack {
                    Image(systemName: "magnifyingglass")
                        .foregroundStyle(Palette.secondary)
                    TextField("Pretraži nazive i napomene", text: $query)
                        .focused($searchFocused)
                        .textInputAutocapitalization(.never)
                        .submitLabel(.search)
                        .accessibilityLabel("Pretraži kalendar")
                    if !query.isEmpty {
                        Button {
                            query = ""
                        } label: {
                            Image(systemName: "xmark.circle.fill")
                                .foregroundStyle(Palette.secondary)
                        }
                        .accessibilityLabel("Očisti pretragu")
                    }
                }
                .padding(14)
                .background(Palette.panel, in: RoundedRectangle(cornerRadius: 16))

                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text(query.isEmpty
                             ? CalendarClock.title(selected, format: "EEEE, d. MMMM yyyy.")
                             : "Rezultati pretrage")
                            .font(.headline)
                            .foregroundStyle(.white)
                        Text(String(items.count) + " zapisa")
                            .font(.caption)
                            .foregroundStyle(Palette.secondary)
                    }
                    Spacer()
                    if query.isEmpty {
                        Button("Dodaj +") {
                            editor = EditorTarget(date: selected, item: nil)
                        }
                        .foregroundStyle(Palette.mint)
                        .frame(minHeight: 44)
                    }
                }

                if items.isEmpty {
                    Text(query.isEmpty
                         ? "Nema zapisa za ovaj dan. Dodaj smjenu, događaj ili napomenu."
                         : "Nema rezultata za uneseni pojam.")
                        .font(.subheadline)
                        .foregroundStyle(Palette.secondary)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(22)
                        .background(Palette.panel, in: RoundedRectangle(cornerRadius: 18))
                } else {
                    ForEach(items.prefix(visibleCount)) { item in
                        Button {
                            guard let date = CalendarClock.parse(item.date) else { return }
                            selected = date
                            month = CalendarClock.monthStart(date)
                            editor = EditorTarget(date: date, item: item)
                        } label: {
                            HStack(spacing: 13) {
                                RoundedRectangle(cornerRadius: 3)
                                    .fill(Palette.colors[item.color])
                                    .frame(width: 5, height: 44)
                                VStack(alignment: .leading, spacing: 5) {
                                    Text(item.title.isEmpty ? "Napomena" : item.title)
                                        .font(.subheadline.bold())
                                        .foregroundStyle(.white)
                                        .lineLimit(2)
                                    Text([
                                        item.category,
                                        item.start.isEmpty ? "" : item.start + " – " + item.end,
                                        query.isEmpty ? "" : item.date
                                    ].filter { !$0.isEmpty }.joined(separator: " · "))
                                    .font(.caption)
                                    .foregroundStyle(Palette.secondary)
                                    if !item.note.isEmpty {
                                        Text(item.note)
                                            .font(.caption)
                                            .foregroundStyle(Palette.secondary)
                                            .lineLimit(2)
                                    }
                                }
                                Spacer(minLength: 4)
                                Image(systemName: "pencil")
                                    .foregroundStyle(Palette.mint)
                            }
                            .padding(14)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background(Palette.panel, in: RoundedRectangle(cornerRadius: 16))
                        }
                        .buttonStyle(.plain)
                        .accessibilityLabel("Uredi: " + (item.title.isEmpty ? "Napomena" : item.title))
                    }
                }
                if items.count > visibleCount {
                    Button {
                        visibleCount = min(items.count, visibleCount + 80)
                    } label: {
                        Text("Prikaži još · preostalo " + String(items.count - visibleCount))
                            .font(.subheadline.weight(.semibold))
                            .frame(maxWidth: .infinity, minHeight: 48)
                    }
                    .buttonStyle(.bordered)
                    .tint(Palette.mint)
                }
                Spacer(minLength: 16)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 18)
        }
        .scrollDismissesKeyboard(.interactively)
    }

    private func exportBackup() {
        do {
            exportDocument = CalendarBackupDocument(
                data: try CalendarBackup.encode(store.entries)
            )
            showExport = true
        } catch {
            backupNotice = "Izvoz nije uspio: " + error.localizedDescription
        }
    }

    private var calendarGrid: some View {
        CalendarParityMonthGrid(
            month: month, selected: selected, entries: store.entries,
            weekOnly: weekView
        ) { day in
            selected = day
            month = CalendarClock.monthStart(day)
            query = ""
        }
    }

    private func changeDay(_ by: Int) {
        guard let next = CalendarClock.calendar.date(
            byAdding: .day, value: by, to: selected
        ) else { return }
        selected = next
        month = CalendarClock.monthStart(next)
        query = ""
    }

    private func changeMonth(_ by: Int) {
        let newMonth = CalendarClock.calendar.date(
            byAdding: .month, value: by, to: month
        ) ?? month
        month = CalendarClock.monthStart(newMonth)
        selected = month
        query = ""
    }
}
