import Foundation
import SwiftUI

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
        let d = UserDefaults.standard
        themeMode = d.string(forKey: "themeMode") ?? "Automatski"
        showOutsideDays = d.object(forKey: "showOutsideDays") as? Bool ?? true
        dayNumberSize = d.string(forKey: "dayNumberSize") ?? "M"
        highlightWeekends = d.object(forKey: "highlightWeekends") as? Bool ?? true
        showAlarmIcons = d.object(forKey: "showAlarmIcons") as? Bool ?? true
        showNoteIcons = d.object(forKey: "showNoteIcons") as? Bool ?? true
        highlightToday = d.object(forKey: "highlightToday") as? Bool ?? true
        todayShape = d.string(forKey: "todayShape") ?? "Zaobljeni kvadrat"
        todayColorIndex = d.object(forKey: "todayColorIndex") as? Int ?? 1
        todayOpacity = d.object(forKey: "todayOpacity") as? Int ?? 50
        language = d.string(forKey: "language") ?? "Automatski (Hrvatski)"
        firstWeekday = d.string(forKey: "firstWeekday") ?? "PON"
        timeFormat = d.string(forKey: "timeFormat") ?? "Automatski"
        dateFormat = d.string(forKey: "dateFormat") ?? "Automatski"
        showNotesInCell = d.object(forKey: "showNotesInCell") as? Bool ?? true
        noteTextSize = d.string(forKey: "noteTextSize") ?? "M"
        noteBackgroundOpacity = d.object(forKey: "noteBackgroundOpacity") as? Int ?? 50
    }
}
