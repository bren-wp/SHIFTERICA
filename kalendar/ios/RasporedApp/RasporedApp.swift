import SwiftUI
import UserNotifications

@main
struct RasporedUniversalApp: App {
    @StateObject private var schedule = ScheduleStoreIOS()
    @StateObject private var shiftLibrary = ShiftLibraryIOS()
    @StateObject private var settings = UISettingsStoreIOS()
    private static let foregroundNotifications = ShiftReminderForegroundDelegateIOS()

    init() {
        UNUserNotificationCenter.current().delegate = Self.foregroundNotifications
    }


    var body: some Scene {
        WindowGroup {
            appContent
                .environmentObject(schedule)
                .environmentObject(shiftLibrary)
                .environmentObject(settings)
                .preferredColorScheme(.dark)
        }
    }

    @ViewBuilder
    private var appContent: some View {
#if DEBUG
        let args = ProcessInfo.processInfo.arguments
        if args.contains("--qa-calendar-settings") {
            RootView(showsSplash: false, showsSettings: true)
        } else if args.contains("--qa-calendar-summary") {
            RootView(initialSection: .summary, showsSplash: false)
        } else if args.contains("--qa-calendar-year") {
            RootView(initialSection: .year, showsSplash: false)
        } else if args.contains("--qa-calendar-nosplash") {
            RootView(showsSplash: false)
        } else if let documentationScreen = DocumentationScreen.current {
            DocumentationHost(screen: documentationScreen)
        } else {
            RootView()
        }
#else
        RootView()
#endif
    }
}
