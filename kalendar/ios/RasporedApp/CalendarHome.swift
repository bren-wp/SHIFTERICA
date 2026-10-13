import SwiftUI
import UniformTypeIdentifiers

enum Palette {
    static let background = Color(red: 0.024, green: 0.086, blue: 0.141)
    static let panel = Color(red: 0.071, green: 0.169, blue: 0.247)
    static let mint = Color(red: 0.486, green: 0.922, blue: 0.839)
    static let secondary = Color(red: 0.72, green: 0.805, blue: 0.851)
    static let selected = Color(red: 0.125, green: 0.318, blue: 0.416)
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
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                HStack(alignment: .center) {
                    VStack(alignment: .leading, spacing: 5) {
                        Text("RASPORED")
                            .font(.caption.weight(.bold))
                            .tracking(2)
                            .foregroundStyle(Palette.mint)
                        Text("Moj kalendar")
                            .font(.largeTitle.bold())
                            .foregroundStyle(.white)
                            .minimumScaleFactor(0.8)
                        Text("Smjene, događaji i bilješke")
                            .foregroundStyle(Palette.secondary)
                    }
                    Spacer(minLength: 4)
                    Button {
                        showSettings = true
                    } label: {
                        Image(systemName: "gearshape")
                            .frame(width: 44, height: 44)
                            .foregroundStyle(Palette.mint)
                    }
                    .accessibilityLabel("Postavke")
                    Button {
                        editor = EditorTarget(date: selected, item: nil)
                    } label: {
                        Label("Dodaj", systemImage: "plus")
                            .font(.subheadline.bold())
                            .padding(.horizontal, 14)
                            .padding(.vertical, 12)
                            .background(Palette.mint, in: Capsule())
                            .foregroundStyle(Palette.background)
                    }
                    .accessibilityLabel("Dodaj novi zapis")
                }

                HStack(spacing: 12) {
                    Button {
                        do {
                            exportDocument = CalendarBackupDocument(
                                data: try CalendarBackup.encode(store.entries)
                            )
                            showExport = true
                        } catch {
                            backupNotice = "Izvoz nije uspio: " + error.localizedDescription
                        }
                    } label: {
                        Label("Izvezi kopiju", systemImage: "square.and.arrow.up")
                            .font(.subheadline)
                            .frame(maxWidth: .infinity, minHeight: 44)
                    }
                    Button {
                        showImport = true
                    } label: {
                        Label("Uvezi kopiju", systemImage: "square.and.arrow.down")
                            .font(.subheadline)
                            .frame(maxWidth: .infinity, minHeight: 44)
                    }
                }
                .buttonStyle(.bordered)
                .tint(Palette.mint)
                if let backupNotice {
                    Text(backupNotice)
                        .font(.caption)
                        .foregroundStyle(Palette.secondary)
                        .accessibilityAddTraits(.updatesFrequently)
                }

                HStack(spacing: 4) {
                    Button {
                        changeMonth(-1)
                    } label: {
                        Image(systemName: "chevron.left")
                            .frame(width: 44, height: 48)
                    }
                    .accessibilityLabel("Prethodni mjesec")
                    Text(CalendarClock.title(month, format: "LLLL yyyy").capitalized)
                        .font(.title3.bold())
                        .foregroundStyle(.white)
                        .frame(maxWidth: .infinity, alignment: .leading)
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
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled(false)
                        .accessibilityLabel("Pretraži kalendar")
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
                    ForEach(items.prefix(200)) { item in
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
                Spacer(minLength: 16)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 18)
        }
        .background(Palette.background.ignoresSafeArea())
        .sheet(isPresented: $showSettings) {
            CalendarSettings(weekView: $weekView)
                .presentationDragIndicator(.visible)
        }
        .sheet(item: $editor) { target in
            CalendarEditor(target: target, store: store)
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

    private var calendarGrid: some View {
        let cells = weekView ? CalendarClock.weekCells(selected) : CalendarClock.monthCells(month)
        let columns = Array(repeating: GridItem(.flexible(minimum: 0), spacing: 3), count: 7)
        let entriesByDate = Dictionary(grouping: store.entries, by: \.date)
        return VStack(spacing: 9) {
            HStack(spacing: 0) {
                ForEach(["Po", "Ut", "Sr", "Če", "Pe", "Su", "Ne"].indices, id: \.self) { i in
                    Text(["Po", "Ut", "Sr", "Če", "Pe", "Su", "Ne"][i])
                        .font(.caption2.weight(.bold))
                        .foregroundStyle(Palette.secondary)
                        .frame(maxWidth: .infinity)
                }
            }
            LazyVGrid(columns: columns, spacing: 3) {
                ForEach(0..<cells.count, id: \.self) { index in
                    let day = cells[index]
                    let key = CalendarClock.key(day)
                    let selectedKey = CalendarClock.key(selected)
                    let ownMonth = CalendarClock.calendar.isDate(day, equalTo: month,
                                                               toGranularity: .month)
                    let dayItems = entriesByDate[key] ?? []
                    Button {
                        selected = day
                        month = CalendarClock.monthStart(day)
                        query = ""
                    } label: {
                        VStack(spacing: 5) {
                            Text(String(CalendarClock.calendar.component(.day, from: day)))
                                .font(.subheadline.weight(key == selectedKey ? .bold : .regular))
                                .foregroundStyle((weekView || ownMonth) ? .white : Palette.secondary.opacity(0.45))
                            HStack(spacing: 3) {
                                ForEach(Array(dayItems.prefix(3))) { item in
                                    Circle()
                                        .fill(Palette.colors[item.color])
                                        .frame(width: 5, height: 5)
                                }
                            }
                            .frame(height: 6)
                        }
                        .frame(maxWidth: .infinity)
                        .frame(minHeight: 55)
                        .background(key == selectedKey ? Palette.selected : .clear,
                                    in: RoundedRectangle(cornerRadius: 10))
                        .overlay {
                            if key == CalendarClock.key(Date()) {
                                RoundedRectangle(cornerRadius: 10)
                                    .stroke(Palette.mint, lineWidth: 1)
                            }
                        }
                    }
                    .buttonStyle(.plain)
                    .accessibilityLabel(
                        CalendarClock.title(day, format: "d. MMMM yyyy.") +
                        ", " + String(dayItems.count) + " zapisa"
                    )
                }
            }
        }
        .padding(10)
        .background(Palette.panel, in: RoundedRectangle(cornerRadius: 20))
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
