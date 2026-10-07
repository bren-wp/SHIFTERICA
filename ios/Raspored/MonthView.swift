import SwiftUI

struct MonthView: View {
    @EnvironmentObject var schedule: ScheduleStoreIOS
    @EnvironmentObject var shifts: ShiftLibraryIOS
    @EnvironmentObject var settings: UISettingsStoreIOS
    @Binding var month: Date
    let onOpenShifts: () -> Void

    @State var editing = false
    @State var selectedCode: String?
    @State var erasing = false

    let columns = Array(repeating: GridItem(.flexible(), spacing: 5), count: 7)

    var body: some View {
        VStack(spacing: 10) {
            calendarCard
            if editing { editingDock } else { legend; quickToolbar }
        }
        .padding(.horizontal, 14)
        .padding(.bottom, 8)
    }
}
