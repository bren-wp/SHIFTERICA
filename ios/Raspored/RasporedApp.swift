import SwiftUI
import UserNotifications

@main
struct RasporedApp: App {
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
        if let documentationScreen = DocumentationScreen.current {
            DocumentationHost(screen: documentationScreen)
        } else {
            RootView()
        }
#else
        RootView()
#endif
    }
}
