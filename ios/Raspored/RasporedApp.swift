import SwiftUI

@main
struct RasporedApp: App {
    @StateObject private var schedule = ScheduleStoreIOS()
    @StateObject private var shiftLibrary = ShiftLibraryIOS()
    @StateObject private var settings = UISettingsStoreIOS()

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(schedule)
                .environmentObject(shiftLibrary)
                .environmentObject(settings)
                .preferredColorScheme(.dark)
        }
    }
}
