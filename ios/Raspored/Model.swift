import Foundation
import SwiftUI

extension Color {
    init(hex: UInt32) {
        self.init(
            red: Double((hex >> 16) & 0xFF) / 255.0,
            green: Double((hex >> 8) & 0xFF) / 255.0,
            blue: Double(hex & 0xFF) / 255.0
        )
    }
}

enum RColors {
    static let bg = Color(hex: 0x061624)
    static let bg2 = Color(hex: 0x0A2235)
    static let card = Color(hex: 0x12293D).opacity(0.94)
    static let card2 = Color(hex: 0x102A40)
    static let stroke = Color(hex: 0x2A5D7D)
    static let text = Color(hex: 0xF6F8FB)
    static let muted = Color(hex: 0xAFC1D8)
    static let accent = Color(hex: 0x19DCE0)
    static let day = Color(hex: 0x13B7F3)
    static let night = Color(hex: 0xFFD21F)
    static let annual = Color(hex: 0x6CEB82)
    static let morning = Color(hex: 0x77DED7)
    static let sick = Color(hex: 0xD991EE)
    static let weekend = Color(hex: 0xFF7186)
    static let empty = Color(hex: 0x122B40)
    static let weekendEmpty = Color(hex: 0x352436)
}

struct ShiftTypeDef: Identifiable, Hashable {
    let code: String
    let name: String
    let shortName: String
    let start: String?
    let end: String?
    let secondaryStart: String?
    let secondaryEnd: String?
    let backgroundHex: UInt32
    let foregroundHex: UInt32
    let fontSize: Int
    let custom: Bool

    var id: String { code }
    var color: Color { Color(hex: backgroundHex) }
    var textColor: Color { Color(hex: foregroundHex) }

    var durationMinutes: Int {
        intervalMinutes(start, end) + intervalMinutes(secondaryStart, secondaryEnd)
    }

    var timeText: String? {
        guard let start, let end else { return nil }
        if let secondaryStart, let secondaryEnd {
            return start + " – " + end + " / " + secondaryStart + " – " + secondaryEnd
        }
        return start + " – " + end
    }

    private func intervalMinutes(_ from: String?, _ to: String?) -> Int {
        guard let from, let to else { return 0 }
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.dateFormat = "HH:mm"
        guard let a = formatter.date(from: from), let b = formatter.date(from: to) else { return 0 }
        let raw = Int(b.timeIntervalSince(a) / 60)
        return raw > 0 ? raw : raw + 24 * 60
    }
}

enum ShiftCatalogIOS {
    static let night = ShiftTypeDef(code: "N", name: "Noćna smjena", shortName: "Noćna", start: "08:00", end: "14:00", secondaryStart: nil, secondaryEnd: nil, backgroundHex: 0xFFD21F, foregroundHex: 0x06131F, fontSize: 12, custom: false)
    static let day = ShiftTypeDef(code: "D", name: "Dnevna smjena", shortName: "Dnevna", start: "14:00", end: "21:00", secondaryStart: nil, secondaryEnd: nil, backgroundHex: 0x13B7F3, foregroundHex: 0x06131F, fontSize: 12, custom: false)
    static let annual = ShiftTypeDef(code: "GO", name: "Godišnji odmor", shortName: "Godišnji", start: nil, end: nil, secondaryStart: nil, secondaryEnd: nil, backgroundHex: 0x6CEB82, foregroundHex: 0x06131F, fontSize: 12, custom: false)
    static let morning = ShiftTypeDef(code: "J", name: "Jutarnja smjena", shortName: "Jutarnja", start: "21:00", end: "07:00", secondaryStart: nil, secondaryEnd: nil, backgroundHex: 0x77DED7, foregroundHex: 0x06131F, fontSize: 12, custom: false)
    static let sick = ShiftTypeDef(code: "BO", name: "Bolovanje", shortName: "Bolovanje", start: "10:00", end: "14:00", secondaryStart: "16:00", secondaryEnd: "20:00", backgroundHex: 0xD991EE, foregroundHex: 0x06131F, fontSize: 12, custom: false)
    static let all = [night, day, annual, morning, sick]
    static func byCode(_ code: String?) -> ShiftTypeDef? { all.first { $0.code == code } }
}

private struct UserShiftRecord: Codable {
    let code: String
    let name: String
    let shortName: String
    let start: String?
    let end: String?
    let secondaryStart: String?
    let secondaryEnd: String?
    let backgroundHex: UInt32
    let foregroundHex: UInt32
    let fontSize: Int

    var definition: ShiftTypeDef {
        ShiftTypeDef(
            code: code, name: name, shortName: shortName,
            start: start, end: end,
            secondaryStart: secondaryStart, secondaryEnd: secondaryEnd,
            backgroundHex: backgroundHex, foregroundHex: foregroundHex,
            fontSize: fontSize, custom: true
        )
    }
}

@MainActor final class ShiftLibraryIOS: ObservableObject {
    @Published private(set) var custom: [ShiftTypeDef] = []
    private let defaults = UserDefaults.standard
    private let key = "raspored.premium.customShifts"

    init() { load() }

    var all: [ShiftTypeDef] { ShiftCatalogIOS.all + custom }
    func byCode(_ code: String?) -> ShiftTypeDef? { all.first { $0.code == code } }

    @discardableResult
    func save(
        name: String,
        code: String,
        backgroundHex: UInt32,
        foregroundHex: UInt32,
        fontSize: Int,
        start: String?,
        end: String?,
        secondaryStart: String?,
        secondaryEnd: String?
    ) throws -> ShiftTypeDef {
        let normalized = normalize(code)
        guard (1...4).contains(normalized.count) else { throw ShiftLibraryError.invalidCode }
        guard !name.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { throw ShiftLibraryError.emptyName }
        guard ShiftCatalogIOS.byCode(normalized) == nil else { throw ShiftLibraryError.reservedCode }

        let cleanName = name.trimmingCharacters(in: .whitespacesAndNewlines)
        let shift = ShiftTypeDef(
            code: normalized,
            name: cleanName,
            shortName: String(cleanName.prefix(14)),
            start: clean(start), end: clean(end),
            secondaryStart: clean(secondaryStart), secondaryEnd: clean(secondaryEnd),
            backgroundHex: backgroundHex,
            foregroundHex: foregroundHex,
            fontSize: min(24, max(8, fontSize)),
            custom: true
        )
        if let index = custom.firstIndex(where: { $0.code == normalized }) { custom[index] = shift }
        else { custom.append(shift) }
        persist()
        return shift
    }

    func delete(_ code: String) {
        custom.removeAll { $0.code == normalize(code) }
        persist()
    }

    private func normalize(_ value: String) -> String {
        String(value.uppercased(with: Locale(identifier: "hr_HR")).filter { $0.isLetter || $0.isNumber }.prefix(4))
    }

    private func clean(_ value: String?) -> String? {
        guard let value else { return nil }
        let trimmed = value.trimmingCharacters(in: .whitespacesAndNewlines)
        return trimmed.isEmpty ? nil : trimmed
    }

    private func load() {
        guard let data = defaults.data(forKey: key), let records = try? JSONDecoder().decode([UserShiftRecord].self, from: data) else { return }
        custom = records.map(\.definition)
    }

    private func persist() {
        let records = custom.map {
            UserShiftRecord(
                code: $0.code, name: $0.name, shortName: $0.shortName,
                start: $0.start, end: $0.end,
                secondaryStart: $0.secondaryStart, secondaryEnd: $0.secondaryEnd,
                backgroundHex: $0.backgroundHex, foregroundHex: $0.foregroundHex,
                fontSize: $0.fontSize
            )
        }
        if let data = try? JSONEncoder().encode(records) { defaults.set(data, forKey: key) }
    }

    enum ShiftLibraryError: LocalizedError {
        case invalidCode, emptyName, reservedCode
        var errorDescription: String? {
            switch self {
            case .invalidCode: return "Skraćenica mora imati od 1 do 4 znaka."
            case .emptyName: return "Naziv smjene ne može biti prazan."
            case .reservedCode: return "Ta je skraćenica rezervirana za ugrađenu smjenu."
            }
        }
    }
}

@MainActor final class ScheduleStoreIOS: ObservableObject {
    @Published private(set) var entries: [String: String] = [:]
    private let defaults = UserDefaults.standard
    private let key = "raspored.premium.schedule"

    init() {
        if let data = defaults.data(forKey: key), let decoded = try? JSONDecoder().decode([String:String].self, from: data) { entries = decoded }
        if entries.isEmpty { seedReferenceOctober2026() }
    }

    func code(on date: Date) -> String? { entries[Self.keyFor(date)] }

    func set(_ code: String?, on date: Date) {
        let k = Self.keyFor(date)
        if let code { entries[k] = code } else { entries.removeValue(forKey: k) }
        persist()
    }

    func monthEntries(_ month: Date) -> [(Date,String)] {
        entries.compactMap { key, value in
            guard let date = DateFormatter.scheduleKey.date(from: key), Calendar.raspored.isDate(date, equalTo: month, toGranularity: .month) else { return nil }
            return (date, value)
        }.sorted { $0.0 < $1.0 }
    }

    func count(_ month: Date, code: String) -> Int { monthEntries(month).filter { $0.1 == code }.count }

    private func persist() {
        if let data = try? JSONEncoder().encode(entries) { defaults.set(data, forKey: key) }
    }

    private func seedReferenceOctober2026() {
        let pairs: [(Int,String)] = [(2,"D"),(3,"N"),(6,"D"),(7,"N"),(10,"D"),(11,"N"),(14,"D"),(15,"N"),(18,"D"),(19,"N"),(22,"D"),(23,"N"),(26,"D"),(27,"N"),(28,"N"),(30,"D"),(31,"N")]
        var c = DateComponents(); c.calendar = .raspored; c.year = 2026; c.month = 10
        for (d, code) in pairs { c.day = d; if let date = c.date { entries[Self.keyFor(date)] = code } }
        if let d28 = Calendar.raspored.date(from: DateComponents(year: 2026, month: 9, day: 28)) { entries[Self.keyFor(d28)] = "D" }
        if let d29 = Calendar.raspored.date(from: DateComponents(year: 2026, month: 9, day: 29)) { entries[Self.keyFor(d29)] = "N" }
        persist()
    }

    static func keyFor(_ date: Date) -> String { DateFormatter.scheduleKey.string(from: date) }
}

@MainActor final class UISettingsStoreIOS: ObservableObject {
    private let defaults = UserDefaults.standard

    @Published var themeMode: String { didSet { defaults.set(themeMode, forKey: "themeMode") } }
    @Published var showOutsideDays: Bool { didSet { defaults.set(showOutsideDays, forKey: "showOutsideDays") } }
    @Published var dayNumberSize: String { didSet { defaults.set(dayNumberSize, forKey: "dayNumberSize") } }
    @Published var highlightWeekends: Bool { didSet { defaults.set(highlightWeekends, forKey: "highlightWeekends") } }
    @Published var showAlarmIcons: Bool { didSet { defaults.set(showAlarmIcons, forKey: "showAlarmIcons") } }
    @Published var showNoteIcons: Bool { didSet { defaults.set(showNoteIcons, forKey: "showNoteIcons") } }
    @Published var highlightToday: Bool { didSet { defaults.set(highlightToday, forKey: "highlightToday") } }
    @Published var todayShape: String { didSet { defaults.set(todayShape, forKey: "todayShape") } }
    @Published var todayColorIndex: Int { didSet { defaults.set(todayColorIndex, forKey: "todayColorIndex") } }
    @Published var todayOpacity: Int { didSet { defaults.set(todayOpacity, forKey: "todayOpacity") } }
    @Published var language: String { didSet { defaults.set(language, forKey: "language") } }
    @Published var firstWeekday: String { didSet { defaults.set(firstWeekday, forKey: "firstWeekday") } }
    @Published var timeFormat: String { didSet { defaults.set(timeFormat, forKey: "timeFormat") } }
    @Published var dateFormat: String { didSet { defaults.set(dateFormat, forKey: "dateFormat") } }
    @Published var showNotesInCell: Bool { didSet { defaults.set(showNotesInCell, forKey: "showNotesInCell") } }
    @Published var noteTextSize: String { didSet { defaults.set(noteTextSize, forKey: "noteTextSize") } }
    @Published var noteBackgroundOpacity: Int { didSet { defaults.set(noteBackgroundOpacity, forKey: "noteBackgroundOpacity") } }

    init() {
        themeMode = defaults.string(forKey: "themeMode") ?? "Automatski"
        showOutsideDays = defaults.object(forKey: "showOutsideDays") as? Bool ?? true
        dayNumberSize = defaults.string(forKey: "dayNumberSize") ?? "M"
        highlightWeekends = defaults.object(forKey: "highlightWeekends") as? Bool ?? true
        showAlarmIcons = defaults.object(forKey: "showAlarmIcons") as? Bool ?? true
        showNoteIcons = defaults.object(forKey: "showNoteIcons") as? Bool ?? true
        highlightToday = defaults.object(forKey: "highlightToday") as? Bool ?? true
        todayShape = defaults.string(forKey: "todayShape") ?? "Zaobljeni kvadrat"
        todayColorIndex = defaults.object(forKey: "todayColorIndex") as? Int ?? 1
        todayOpacity = defaults.object(forKey: "todayOpacity") as? Int ?? 50
        language = defaults.string(forKey: "language") ?? "Automatski (Hrvatski)"
        firstWeekday = defaults.string(forKey: "firstWeekday") ?? "PON"
        timeFormat = defaults.string(forKey: "timeFormat") ?? "Automatski"
        dateFormat = defaults.string(forKey: "dateFormat") ?? "Automatski"
        showNotesInCell = defaults.object(forKey: "showNotesInCell") as? Bool ?? true
        noteTextSize = defaults.string(forKey: "noteTextSize") ?? "M"
        noteBackgroundOpacity = defaults.object(forKey: "noteBackgroundOpacity") as? Int ?? 50
    }
}

extension Calendar {
    static var raspored: Calendar {
        var c = Calendar(identifier: .gregorian)
        c.locale = Locale(identifier: "hr_HR")
        c.firstWeekday = 2
        c.timeZone = .current
        return c
    }
}

extension DateFormatter {
    static let scheduleKey: DateFormatter = {
        let f = DateFormatter(); f.calendar = .raspored; f.locale = Locale(identifier:"en_US_POSIX"); f.dateFormat = "yyyy-MM-dd"; return f
    }()
    static let monthTitle: DateFormatter = {
        let f = DateFormatter(); f.calendar = .raspored; f.locale = Locale(identifier:"hr_HR"); f.dateFormat = "LLLL yyyy"; return f
    }()
    static let monthOnly: DateFormatter = {
        let f = DateFormatter(); f.calendar = .raspored; f.locale = Locale(identifier:"hr_HR"); f.dateFormat = "LLLL"; return f
    }()
}
