import SwiftUI

struct CalendarEditor: View {
    @Environment(\.dismiss) private var dismiss
    let target: EditorTarget
    @ObservedObject var store: CalendarStore
    let onSaved: (Date) -> Void
    @State private var entryDate: Date
    @State private var title: String
    @State private var note: String
    @State private var category: String
    @State private var start: String
    @State private var end: String
    @State private var color: Int
    @State private var showDelete = false
    @State private var showError = false

    init(target: EditorTarget, store: CalendarStore, onSaved: @escaping (Date) -> Void) {
        self.target = target
        self.store = store
        self.onSaved = onSaved
        _entryDate = State(initialValue: target.date)
        _title = State(initialValue: target.item?.title ?? target.draft?.title ?? "")
        _note = State(initialValue: target.item?.note ?? target.draft?.note ?? "")
        _category = State(initialValue: target.item?.category ?? target.draft?.category ?? "Događaj")
        _start = State(initialValue: target.item?.start ?? target.draft?.start ?? "")
        _end = State(initialValue: target.item?.end ?? target.draft?.end ?? "")
        _color = State(initialValue: target.item?.color ?? target.draft?.color ?? 0)
    }

    // Keep SwiftUI's type-checking graph small. A single deeply nested
    // NavigationStack/Form expression timed out on Xcode 26.3 (CI #38492568425).
    // These sections all bind to the original editor state; no parallel model.
    var body: some View {
        NavigationStack {
            editorForm
        }
    }

    private var editorForm: some View {
        Form {
            detailsSection
            timeSection
            noteSection
            colorSection
            if target.item != nil {
                editActionsSection
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

    private var detailsSection: some View {
        Section {
            DatePicker("Datum zapisa", selection: $entryDate,
                       displayedComponents: .date)
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
    }

    private var timeSection: some View {
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
            Text("Događaj ili smjena mogu prelaziti ponoć.")
                .font(.caption)
                .foregroundStyle(Palette.secondary)
        }
    }

    private var noteSection: some View {
        Section("Napomena") {
            TextEditor(text: $note)
                .frame(minHeight: 125)
                .onChange(of: note) { _, value in
                    if value.count > 10_000 { note = String(value.prefix(10_000)) }
                }
        }
    }

    private var colorSection: some View {
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
    }

    private var editActionsSection: some View {
        Section {
            Button("Dupliciraj zapis") { save(duplicate: true) }
            Button("Izbriši zapis", role: .destructive) {
                showDelete = true
            }
        }
    }

    private func save(duplicate: Bool = false) {
        var next = (duplicate ? nil : target.item) ?? CalendarEntry(
            date: CalendarClock.key(target.date), title: ""
        )
        next.date = CalendarClock.key(entryDate)
        next.title = title.trimmingCharacters(in: .whitespacesAndNewlines)
        next.note = note.trimmingCharacters(in: .whitespacesAndNewlines)
        next.category = category
        next.start = start.trimmingCharacters(in: .whitespacesAndNewlines)
        next.end = end.trimmingCharacters(in: .whitespacesAndNewlines)
        next.color = color
        if store.save(next) {
            onSaved(entryDate)
            dismiss()
        } else {
            showError = true
        }
    }
}
