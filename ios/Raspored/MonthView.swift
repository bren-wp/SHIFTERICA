import SwiftUI

struct MonthView: View {
    @EnvironmentObject var schedule: ScheduleStoreIOS
    @EnvironmentObject var shifts: ShiftLibraryIOS
    @EnvironmentObject var settings: UISettingsStoreIOS
    @Binding var month: Date
    @Binding var focusedDate: Date?
    let onOpenShifts: () -> Void

    @State var selectedDate: Date?


    var body: some View {
        VStack(spacing: 4) {
            MonthInsightsView(month: month)
            calendarCard
            monthManageBar
        }
        .padding(.horizontal, 4)
        .padding(.bottom, 4)
        .onAppear { consumeCalendarFocus() }
        .onChange(of: focusedDate) { _, _ in consumeCalendarFocus() }
        .sheet(
            isPresented: Binding(
                get: { selectedDate != nil },
                set: { if !$0 { selectedDate = nil } }
            )
        ) {
            if let date = selectedDate {
                DayShiftPickerSheetIOS(
                    date: date,
                    currentCode: schedule.code(on: date),
                    onSelect: { code in
                        schedule.set(code, on: date)
                        selectedDate = nil
                    },
                    onOpenShifts: {
                        selectedDate = nil
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.25) {
                            onOpenShifts()
                        }
                    }
                )
            }
        }
    }

    private func consumeCalendarFocus() {
        guard let focusedDate,
              Calendar.raspored.isDate(focusedDate, equalTo: month, toGranularity: .month)
        else { return }
        selectedDate = focusedDate
        self.focusedDate = nil
    }
}
