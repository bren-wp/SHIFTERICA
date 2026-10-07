import SwiftUI

struct MonthView: View {
    @EnvironmentObject var schedule: ScheduleStoreIOS
    @EnvironmentObject var shifts: ShiftLibraryIOS
    @EnvironmentObject var settings: UISettingsStoreIOS
    @Binding var month: Date
    let onOpenShifts: () -> Void

    @State var selectedCode = "D"
    @State var erasing = false

    let columns = Array(repeating: GridItem(.flexible(), spacing: 6), count: 7)

    var body: some View {
        VStack(spacing: 6) {
            calendarCard
            compactShiftToolbar
        }
        .padding(.horizontal, 8)
        .padding(.bottom, 6)
    }
}
