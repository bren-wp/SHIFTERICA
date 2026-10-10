import SwiftUI

private enum Palette {
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

private struct EditorTarget: Identifiable {
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

    private var items: [CalendarEntry] {
        if query.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            return CalendarRules.forDay(store.entries, key: CalendarClock.key(selected))
        }
        let term = query.trimmingCharacters(in: .whitespacesAndNewlines)
        return store.entries.filter {
            $0.title.localizedStandardContains(term) ||
            $0.note.localizedStandardContains(term) ||
            $0.category.localizedStandardContains(term) ||
            $0.date.localizedStandardContains(term)
        }.sorted {
            if $0.date != $1.date { return $0.date < $1.date }
            return $0.start < $1.start
        }
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

                calendarGrid

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
        .sheet(item: $editor) { target in
            CalendarEditor(target: target, store: store)
                .presentationDragIndicator(.visible)
        }
    }

    private var calendarGrid: some View {
        let cells = CalendarClock.monthCells(month)
        let columns = Array(repeating: GridItem(.flexible(minimum: 0), spacing: 3), count: 7)
        return VStack(spacing: 9) {
            HStack(spacing: 0) {
                ForEach(["P", "U", "S", "Č", "P", "S", "N"].indices, id: \.self) { i in
                    Text(["P", "U", "S", "Č", "P", "S", "N"][i])
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
                    let dayItems = store.entries.filter { $0.date == key }
                    Button {
                        selected = day
                        month = CalendarClock.monthStart(day)
                        query = ""
                    } label: {
                        VStack(spacing: 5) {
                            Text(String(CalendarClock.calendar.component(.day, from: day)))
                                .font(.subheadline.weight(key == selectedKey ? .bold : .regular))
                                .foregroundStyle(ownMonth ? .white : Palette.secondary.opacity(0.45))
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

    private func changeMonth(_ by: Int) {
        let newMonth = CalendarClock.calendar.date(
            byAdding: .month, value: by, to: month
        ) ?? month
        month = CalendarClock.monthStart(newMonth)
        selected = month
        query = ""
    }
}

private struct CalendarEditor: View {
    @Environment(\.dismiss) private var dismiss
    let target: EditorTarget
    @ObservedObject var store: CalendarStore
    @State private var title: String
    @State private var note: String
    @State private var category: String
    @State private var start: String
    @State private var end: String
    @State private var color: Int
    @State private var showDelete = false
    @State private var showError = false

    init(target: EditorTarget, store: CalendarStore) {
        self.target = target
        self.store = store
        _title = State(initialValue: target.item?.title ?? "")
        _note = State(initialValue: target.item?.note ?? "")
        _category = State(initialValue: target.item?.category ?? "Smjena")
        _start = State(initialValue: target.item?.start ?? "")
        _end = State(initialValue: target.item?.end ?? "")
        _color = State(initialValue: target.item?.color ?? 0)
    }

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    TextField("Naziv (ili ostavi samo napomenu)", text: $title)
                        .onChange(of: title) { _, value in
                            if value.count > 120 { title = String(value.prefix(120)) }
                        }
                    Picker("Vrsta zapisa", selection: $category) {
                        ForEach(CalendarRules.categories, id: \.self) { kind in
                            Text(kind).tag(kind)
                        }
                    }
                } header: {
                    Text(CalendarClock.title(target.date, format: "EEEE, d. MMMM yyyy."))
                }
                Section("Vrijeme, neobavezno") {
                    HStack {
                        TextField("Od HH:mm", text: $start)
                            .textInputAutocapitalization(.never)
                            .keyboardType(.numbersAndPunctuation)
                        Text("–")
                        TextField("Do HH:mm", text: $end)
                            .textInputAutocapitalization(.never)
                            .keyboardType(.numbersAndPunctuation)
                    }
                    Text("Smjena smije prelaziti ponoć.")
                        .font(.caption)
                        .foregroundStyle(Palette.secondary)
                }
                Section("Napomena") {
                    TextEditor(text: $note)
                        .frame(minHeight: 125)
                        .onChange(of: note) { _, value in
                            if value.count > 10_000 { note = String(value.prefix(10_000)) }
                        }
                }
                Section("Boja zapisa") {
                    HStack(spacing: 16) {
                        ForEach(0..<Palette.colors.count, id: \.self) { index in
                            Button {
                                color = index
                            } label: {
                                Circle()
                                    .fill(Palette.colors[index])
                                    .frame(width: 35, height: 35)
                                    .overlay {
                                        if color == index {
                                            Circle().stroke(.white, lineWidth: 3)
                                        }
                                    }
                            }
                            .buttonStyle(.plain)
                            .accessibilityLabel("Boja " + String(index + 1))
                            .accessibilityAddTraits(color == index ? [.isSelected] : [])
                        }
                    }
                    .padding(.vertical, 8)
                }
                if target.item != nil {
                    Section {
                        Button("Izbriši zapis", role: .destructive) {
                            showDelete = true
                        }
                    }
                }
            }
            .scrollContentBackground(.hidden)
            .background(Palette.background)
            .navigationTitle(target.item == nil ? "Novi zapis" : "Uredi zapis")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Odustani") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Spremi") { save() }
                        .fontWeight(.semibold)
                }
            }
            .tint(Palette.mint)
            .alert("Zapis nije spremljen", isPresented: $showError) {
                Button("U redu", role: .cancel) {}
            } message: {
                Text("Provjeri naziv, napomenu i vrijeme (HH:mm).")
            }
            .confirmationDialog(
                "Trajno izbrisati ovaj zapis?", isPresented: $showDelete,
                titleVisibility: .visible
            ) {
                Button("Izbriši", role: .destructive) {
                    if let old = target.item, store.delete(old.id) {
                        dismiss()
                    } else {
                        showError = true
                    }
                }
                Button("Zadrži", role: .cancel) {}
            }
        }
    }

    private func save() {
        var next = target.item ?? CalendarEntry(
            date: CalendarClock.key(target.date), title: ""
        )
        next.title = title.trimmingCharacters(in: .whitespacesAndNewlines)
        next.note = note.trimmingCharacters(in: .whitespacesAndNewlines)
        next.category = category
        next.start = start.trimmingCharacters(in: .whitespacesAndNewlines)
        next.end = end.trimmingCharacters(in: .whitespacesAndNewlines)
        next.color = color
        if store.save(next) {
            dismiss()
        } else {
            showError = true
        }
    }
}
