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
        Color(red: 135/255, green: 221/255, blue: 213/255),
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
    @State private var section: CalendarSectionIOS = .month
    @State private var showRecords = false
    @State private var searchEverywhere = false
    @State private var showSplash = true
    @AppStorage("raspored.universal.weekView") private var weekView = false
    @State private var exportDocument: CalendarBackupDocument?
    @State private var showExport = false
    @State private var showImport = false
    @State private var showSettings = false
    @State private var backupNotice: String?

    #if DEBUG
    /// Only compiled for simulator QA. Production builds always use the
    /// standard month screen and normal splash.
    init() {
        let args = ProcessInfo.processInfo.arguments
        if args.contains("--qa-calendar-year") {
            _section = State(initialValue: .year)
        } else if args.contains("--qa-calendar-summary") {
            _section = State(initialValue: .summary)
        }
        if args.contains("--qa-calendar-settings") {
            _showSettings = State(initialValue: true)
        }
        if args.contains("--qa-calendar-nosplash") {
            _showSplash = State(initialValue: false)
        }
    }
    #endif

    var body: some View {
        VStack(spacing: 5) {
            CalendarParityHeader(
                onSearch: {
                    section = .month
                    searchEverywhere = true
                    showRecords = true
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
        .overlay {
            if showSplash {
                CalendarParitySplash()
                    .transition(.opacity)
            }
        }
        .task {
            guard showSplash else { return }
            try? await Task.sleep(for: .milliseconds(1200))
            withAnimation(.easeOut(duration: 0.35)) {
                showSplash = false
            }
        }
        .sheet(isPresented: $showSettings) {
            CalendarSettings(weekView: $weekView)
                .presentationDragIndicator(.visible)
        }
        .sheet(isPresented: $showRecords) {
            CalendarRecordsSheet(
                date: selected, entries: store.entries,
                searchAllInitially: searchEverywhere,
                onEdit: { item in
                    showRecords = false
                    guard let date = CalendarClock.parse(item.date) else { return }
                    selected = date
                    month = CalendarClock.monthStart(date)
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                        editor = EditorTarget(date: date, item: item)
                    }
                },
                onAdd: {
                    showRecords = false
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                        editor = EditorTarget(date: selected, item: nil)
                    }
                },
                onPreset: { preset in
                    CalendarShiftPresets.alreadyScheduled(
                        store.entries, preset: preset, on: selected
                    ) || store.save(preset.record(for: selected))
                }
            )
            .presentationDragIndicator(.visible)
        }
        .sheet(item: $editor) { target in
            CalendarEditor(target: target, store: store) { savedDate in
                selected = savedDate
                month = CalendarClock.monthStart(savedDate)
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
        let inMonth = store.entries.filter {
            $0.date.hasPrefix(String(CalendarClock.key(month).prefix(7)))
        }
        return VStack(spacing: 4) {
            // Match the original Raspored's two top cards and one wide
            // summary card, substituting calendar-only values for payroll.
            HStack(spacing: 6) {
                metricCard("Zapisa", inMonth.count,
                           icon: "calendar", accent: Palette.mint)
                metricCard("Smjena",
                           inMonth.filter { $0.category == "Smjena" }.count,
                           icon: "chart.bar.fill", accent: Palette.colors[2])
            }
            metricWide("Aktivnih dana", Set(inMonth.map(\.date)).count)
            HStack(spacing: 4) {
                monthArrow("chevron.left", "Prethodni mjesec") { changeMonth(-1) }
                Text(CalendarClock.title(month, format: "LLLL yyyy").uppercased())
                    .font(.system(size: 22, weight: .black))
                    .foregroundStyle(Palette.text)
                    .frame(maxWidth: .infinity)
                    .lineLimit(1)
                    .minimumScaleFactor(0.75)
                monthArrow("chevron.right", "Sljedeći mjesec") { changeMonth(1) }
            }
            calendarGrid.frame(maxWidth: .infinity, maxHeight: .infinity)
            Button {
                searchEverywhere = false
                showRecords = true
            } label: {
                HStack(spacing: 8) {
                    Image(systemName: "ellipsis").foregroundStyle(Palette.mint)
                    Text("SMJENE I POSTAVKE")
                        .font(.system(size: 13, weight: .black))
                        .foregroundStyle(Palette.text)
                }
                .frame(maxWidth: .infinity, minHeight: 46)
                .background(Palette.panel, in: RoundedRectangle(cornerRadius: 17))
                .overlay(RoundedRectangle(cornerRadius: 17)
                    .stroke(Palette.stroke, lineWidth: 1))
            }
            .buttonStyle(.plain)
        }
        .padding(.horizontal, 4)
        .padding(.bottom, 4)
    }

    private func metricCard(
        _ title: String, _ count: Int, icon: String, accent: Color
    ) -> some View {
        VStack(alignment: .leading, spacing: 3) {
            Image(systemName: icon)
                .font(.system(size: 16, weight: .bold))
                .foregroundStyle(accent)
            Text(title)
                .font(.system(size: 12))
                .foregroundStyle(Palette.secondary)
            Text(String(count))
                .font(.system(size: 22, weight: .bold))
                .foregroundStyle(Palette.text)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.horizontal, 12)
        .frame(height: 77)
        .background(Palette.panel, in: RoundedRectangle(cornerRadius: 20))
        .overlay(RoundedRectangle(cornerRadius: 20)
            .stroke(accent.opacity(0.42), lineWidth: 1))
    }

    private func metricWide(_ title: String, _ count: Int) -> some View {
        HStack(spacing: 9) {
            Image(systemName: "calendar.badge.clock")
                .font(.system(size: 18, weight: .bold))
                .foregroundStyle(Palette.mint)
            Text(title)
                .font(.system(size: 15, weight: .semibold))
                .foregroundStyle(Palette.secondary)
            Spacer()
            Text(String(count))
                .font(.system(size: 22, weight: .bold))
                .foregroundStyle(Palette.text)
        }
        .padding(.horizontal, 14)
        .frame(height: 45)
        .background(Palette.panel, in: RoundedRectangle(cornerRadius: 17))
        .overlay(RoundedRectangle(cornerRadius: 17)
            .stroke(Palette.mint.opacity(0.30), lineWidth: 1))
    }

    private func monthArrow(_ symbol: String, _ label: String,
                            action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Image(systemName: symbol)
                .font(.title2.weight(.semibold))
                .foregroundStyle(Palette.text)
                .frame(width: 46, height: 46)
                .background(Palette.card2, in: Circle())
                .overlay(Circle().stroke(Palette.stroke.opacity(0.5), lineWidth: 1))
        }
        .buttonStyle(.plain)
        .accessibilityLabel(label)
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
            searchEverywhere = false
            showRecords = true
        }
    }

    private func changeMonth(_ by: Int) {
        let newMonth = CalendarClock.calendar.date(
            byAdding: .month, value: by, to: month
        ) ?? month
        month = CalendarClock.monthStart(newMonth)
        selected = month
    }
}
