import SwiftUI

@main
struct RasporedUniversalApp: App {
    var body: some Scene {
        WindowGroup {
            CalendarHome()
                .preferredColorScheme(.dark)
        }
    }
}
