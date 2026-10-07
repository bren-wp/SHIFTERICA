#if DEBUG
import SwiftUI

enum DocumentationScreen: String {
    case month
    case year
    case summary
    case settings
    case shifts
    case newShift = "new-shift"
    case splash

    static var current: DocumentationScreen? {
        let arguments = ProcessInfo.processInfo.arguments
        guard let index = arguments.firstIndex(of: "--documentation-screen"),
              arguments.indices.contains(index + 1) else {
            return nil
        }
        return DocumentationScreen(rawValue: arguments[index + 1])
    }
}

struct DocumentationHost: View {
    @EnvironmentObject private var schedule: ScheduleStoreIOS
    let screen: DocumentationScreen

    private static let referenceMonth: Date = {
        Calendar.raspored.date(from: DateComponents(year: 2026, month: 10, day: 1)) ?? Date()
    }()

    var body: some View {
        Group {
            switch screen {
            case .month:
                RootView(initialSection: .month, initialMonth: Self.referenceMonth, showsSplash: false)
            case .year:
                RootView(initialSection: .year, initialMonth: Self.referenceMonth, showsSplash: false)
            case .summary:
                RootView(initialSection: .summary, initialMonth: Self.referenceMonth, showsSplash: false)
            case .settings:
                SettingsView()
            case .shifts:
                ShiftManagerView(onNew: {})
            case .newShift:
                NewShiftView()
            case .splash:
                SplashOverlay()
            }
        }
        .task {
            seedReferenceSchedule()
        }
    }

    private func seedReferenceSchedule() {
        let fixture: [(Int, Int, String)] = [
            // Svibanj 2026
            (5, 1, "D"), (5, 2, "N"), (5, 3, "N"), (5, 4, "N"),
            (5, 5, "D"), (5, 6, "D"), (5, 8, "GO"), (5, 9, "GO"),
            (5, 11, "GO"), (5, 12, "N"), (5, 13, "D"), (5, 14, "D"),
            (5, 15, "N"), (5, 18, "N"), (5, 19, "D"), (5, 20, "D"),
            (5, 21, "GO"), (5, 22, "GO"), (5, 25, "GO"), (5, 26, "N"),
            (5, 27, "D"), (5, 28, "N"), (5, 29, "N"),
            // Lipanj 2026
            (6, 1, "D"), (6, 2, "N"), (6, 3, "N"), (6, 4, "D"),
            (6, 5, "GO"), (6, 8, "GO"), (6, 9, "D"), (6, 10, "N"),
            (6, 11, "D"), (6, 12, "N"), (6, 15, "N"), (6, 16, "GO"),
            (6, 17, "GO"), (6, 18, "D"), (6, 19, "D"), (6, 22, "D"),
            (6, 23, "N"), (6, 24, "N"), (6, 25, "GO"), (6, 26, "D"),
            (6, 29, "D"), (6, 30, "N"),
            // Listopad 2026
            (10, 2, "D"), (10, 3, "N"), (10, 6, "D"), (10, 7, "N"),
            (10, 10, "D"), (10, 11, "N"), (10, 14, "D"), (10, 15, "N"),
            (10, 18, "D"), (10, 19, "N"), (10, 22, "D"), (10, 23, "N"),
            (10, 26, "D"), (10, 27, "N"), (10, 28, "N"), (10, 30, "D"),
            (10, 31, "N")
        ]

        for (month, day, code) in fixture {
            guard let date = Calendar.raspored.date(
                from: DateComponents(year: 2026, month: month, day: day)
            ) else { continue }
            schedule.set(code, on: date)
        }

        // Izvan mjeseca, kako bi mjesečni prikaz imao isti kontekst kao stvarna aplikacija.
        if let september28 = Calendar.raspored.date(from: DateComponents(year: 2026, month: 9, day: 28)) {
            schedule.set("D", on: september28)
        }
        if let september29 = Calendar.raspored.date(from: DateComponents(year: 2026, month: 9, day: 29)) {
            schedule.set("N", on: september29)
        }
    }
}
#endif
