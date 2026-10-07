import SwiftUI

struct MonthView: View {
    @EnvironmentObject var schedule: ScheduleStoreIOS
    @EnvironmentObject var shifts: ShiftLibraryIOS
    @EnvironmentObject var settings: UISettingsStoreIOS
    @Binding var month: Date
    let onOpenShifts: () -> Void

    @State var selectedCode = "D"
    @State var erasing = false
    @State var editing = false


    var body: some View {
        VStack(spacing: 4) {
            calendarCard
            compactShiftToolbar
        }
        .padding(.horizontal, 4)
        .padding(.bottom, 4)
    }
}
