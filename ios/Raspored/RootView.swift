import SwiftUI
import UIKit
import Combine

enum MainSectionIOS { case month, year, summary }

struct RootView: View {
    @EnvironmentObject var schedule: ScheduleStoreIOS
    @StateObject private var accounting = MonthlyAccountingStoreIOS()
    @State private var section: MainSectionIOS
    @State private var month: Date
    @State private var showShifts = false
    @State private var showNewShift = false
    @State private var editingCustomShift: ShiftTypeDef?
    @State private var showSettings = false
    @State private var showSearch = false
    @State private var showSplash: Bool

    init(
        initialSection: MainSectionIOS = .month,
        initialMonth: Date? = nil,
        showsSplash: Bool = true
    ) {
        let currentMonth = Calendar.raspored.date(
            from: Calendar.raspored.dateComponents([.year, .month], from: Date())
        ) ?? Date()
        _section = State(initialValue: initialSection)
        _month = State(initialValue: initialMonth ?? currentMonth)
        _showSplash = State(initialValue: showsSplash)
    }

    var body: some View {
        ZStack {
            LinearGradient(colors: [RColors.bg, RColors.bg2.opacity(0.88), .black.opacity(0.92)], startPoint: .top, endPoint: .bottom).ignoresSafeArea()
            VStack(spacing: 5) {
                HeaderView(onSearch: { showSearch = true }, onSettings: { showSettings = true }, onAdd: { showNewShift = true })
                topTabs
                Group {
                    switch section {
                    case .month: MonthView(month: $month, onOpenShifts: { showShifts = true })
                    case .year: YearOverviewView(year: Calendar.raspored.component(.year, from: month), onMonth: { month = $0; section = .month })
                    case .summary: SummaryView(month: $month)
                    }
                }
                .environmentObject(accounting)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
            if showSplash { SplashOverlay().transition(.opacity) }
        }
        .task {
            guard showSplash else { return }
            try? await Task.sleep(for: .milliseconds(1200))
            withAnimation(.easeOut(duration: 0.35)) { showSplash = false }
        }
        .sheet(isPresented: $showShifts) {
            ShiftManagerView(
                onNew: {
                    showShifts = false
                    editingCustomShift = nil
                    showNewShift = true
                },
                onEditCustom: { shift in
                    showShifts = false
                    editingCustomShift = shift
                    showNewShift = true
                }
            )
        }
        .sheet(isPresented: $showNewShift, onDismiss: { editingCustomShift = nil }) {
            NewShiftView(initialShift: editingCustomShift)
        }
        .sheet(isPresented: $showSettings) { SettingsView() }
        .sheet(isPresented: $showSearch) { SearchView(onPick: { month = $0; section = .month; showSearch = false }) }
        // Refresh local notifications after any shift change or reminder settings edit.
        // No permission prompt occurs during app launch.
        .onReceive(schedule.$entries) { _ in refreshShiftReminders() }
        .onReceive(settingsPublisher) { _ in refreshShiftReminders() }
        .onReceive(NotificationCenter.default.publisher(
            for: UIApplication.willEnterForegroundNotification
        )) { _ in refreshShiftReminders() }
    }

    @EnvironmentObject private var settings: UISettingsStoreIOS

    private var settingsPublisher: AnyPublisher<Bool, Never> {
        Publishers.CombineLatest3(
            settings.$remindersEnabled,
            settings.$eveningReminderEnabled,
            settings.$shiftTimeReminderEnabled
        ).map { $0.0 || $0.1 || $0.2 }.eraseToAnyPublisher()
    }

    private func refreshShiftReminders() {
        let entries = schedule.entries
        let enabled = settings.remindersEnabled
        let evening = settings.eveningReminderEnabled
        let departure = settings.shiftTimeReminderEnabled
        Task {
            await ShiftReminderSchedulerIOS.shared.refresh(
                entries: entries, enabled: enabled,
                evening: evening, departure: departure
            )
        }
    }

    private var topTabs: some View {
        HStack(spacing: 4) {
            tab(DateFormatter.monthOnly.string(from: month).uppercased(), active: section == .month) { section = .month }
            tab(String(Calendar.raspored.component(.year, from: month)), active: section == .year) { section = .year }
            tab("SAŽETAK", active: section == .summary) { section = .summary }
        }
        .padding(4).background(RColors.card).clipShape(RoundedRectangle(cornerRadius: 22)).overlay(RoundedRectangle(cornerRadius: 22).stroke(RColors.stroke.opacity(0.6), lineWidth: 1)).padding(.horizontal, 10)
    }

    private func tab(_ label: String, active: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(label)
                .font(.system(size: 16, weight: .heavy))
                .foregroundStyle(active ? .white : RColors.muted)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 13)
                .background(active ? RColors.accent.opacity(0.27) : .clear)
                .clipShape(RoundedRectangle(cornerRadius: 17))
                .overlay(RoundedRectangle(cornerRadius: 17).stroke(active ? RColors.accent : .clear, lineWidth: 1.6))
                .shadow(color: active ? RColors.accent.opacity(0.40) : .clear, radius: 10, y: 3)
        }.buttonStyle(.plain)
    }
}

private struct HeaderView: View {
    let onSearch: () -> Void, onSettings: () -> Void, onAdd: () -> Void
    var body: some View {
        HStack(spacing: 11) {
            AppMark().frame(width: 48, height: 48)
            Text("Raspored").font(.system(size: 30, weight: .black)).foregroundStyle(RColors.text)
            Spacer()
            headerButton("magnifyingglass", action: onSearch)
            headerButton("slider.horizontal.3", action: onSettings)
            Button(action: onAdd) {
                Image(systemName: "plus")
                    .font(.system(size: 28, weight: .bold))
                    .foregroundStyle(.white)
                    .frame(width: 52, height: 52)
                    .background(RColors.accent)
                    .clipShape(RoundedRectangle(cornerRadius: 16))
                    .overlay(RoundedRectangle(cornerRadius: 20).stroke(Color.white.opacity(0.55), lineWidth: 1))
                    .shadow(color: RColors.accent.opacity(0.45), radius: 12, y: 4)
            }.buttonStyle(.plain)
        }.padding(.horizontal, 10).padding(.vertical, 5)
    }

    private func headerButton(_ symbol: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Image(systemName: symbol)
                .font(.system(size: 23, weight: .semibold))
                .foregroundStyle(RColors.text)
                .frame(width: 48, height: 48)
                .background(RColors.card2)
                .clipShape(RoundedRectangle(cornerRadius: 18))
                .overlay(RoundedRectangle(cornerRadius: 18).stroke(RColors.stroke.opacity(0.7), lineWidth: 1))
                .shadow(color: .black.opacity(0.28), radius: 7, y: 3)
        }.buttonStyle(.plain)
    }
}

struct AppMark: View {
    var body: some View {
        GeometryReader { proxy in
            let unit = min(proxy.size.width, proxy.size.height)
            ZStack {
                RoundedRectangle(cornerRadius: unit * 0.32)
                    .fill(RColors.card2)
                    .overlay(RoundedRectangle(cornerRadius: unit * 0.32).stroke(RColors.accent, lineWidth: 1.5))
                    .shadow(color: RColors.accent.opacity(0.34), radius: unit * 0.16, y: unit * 0.05)
                VStack(spacing: unit * 0.065) {
                    RoundedRectangle(cornerRadius: unit * 0.07)
                        .fill(RColors.accent)
                        .frame(width: unit * 0.54, height: unit * 0.15)
                    ForEach(0..<2, id: \.self) { row in
                        HStack(spacing: unit * 0.065) {
                            ForEach(0..<3, id: \.self) { column in
                                RoundedRectangle(cornerRadius: unit * 0.04)
                                    .fill(row == 0 && column == 1 ? RColors.day : RColors.accent)
                                    .frame(width: unit * 0.13, height: unit * 0.13)
                            }
                        }
                    }
                }
            }
        }
    }
}

struct SplashOverlay: View {
    var body: some View {
        ZStack {
            LinearGradient(colors:[RColors.bg2,RColors.bg,.black],startPoint:.top,endPoint:.bottom).ignoresSafeArea()

            SplashCalendarBackdrop()
            SplashTileIOS(code: "D", color: RColors.day, size: 76)
                .offset(x: -128, y: -260).rotationEffect(.degrees(-12))
            SplashTileIOS(code: "N", color: RColors.night, size: 72)
                .offset(x: 132, y: -215).rotationEffect(.degrees(13))
            SplashTileIOS(code: "GO", color: RColors.annual, size: 70)
                .offset(x: -150, y: 110).rotationEffect(.degrees(-11))
            SplashTileIOS(code: "J", color: RColors.morning, size: 70)
                .offset(x: 150, y: 145).rotationEffect(.degrees(10))
            SplashTileIOS(code: "BO", color: RColors.sick, size: 74)
                .offset(x: 95, y: 300).rotationEffect(.degrees(9))

            VStack(spacing: 18) {
                ZStack {
                    RoundedRectangle(cornerRadius: 46)
                        .fill(RColors.accent.opacity(0.07))
                        .frame(width: 148, height: 148)
                        .overlay(
                            RoundedRectangle(cornerRadius: 46)
                                .stroke(RColors.accent.opacity(0.34), lineWidth: 1)
                        )
                        .shadow(color: RColors.accent.opacity(0.18), radius: 18, y: 5)
                    AppMark().frame(width: 126, height: 126)
                }
                Text("Raspored")
                    .font(.system(size: 40, weight: .black))
                    .foregroundStyle(RColors.text)
                Text("Pametni planer smjena")
                    .font(.system(size: 17))
                    .tracking(1.2)
                    .foregroundStyle(RColors.muted)
                Spacer().frame(height: 74)
                ZStack(alignment: .leading) {
                    Capsule()
                        .fill(RColors.card2.opacity(0.86))
                        .frame(width: 270, height: 6)
                    Capsule()
                        .fill(
                            LinearGradient(
                                colors: [RColors.accent, Color(hex: 0x52F6E8)],
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        )
                        .frame(width: 194, height: 6)
                        .shadow(color: RColors.accent.opacity(0.45), radius: 7)
                }
            }
        }
    }
}

private struct SplashTileIOS: View {
    let code: String
    let color: Color
    let size: CGFloat

    var body: some View {
        Text(code)
            .font(.system(size: code.count == 1 ? 27 : 20, weight: .black))
            .foregroundStyle(Color(hex: 0x06131F))
            .frame(width: size, height: size)
            .background(
                LinearGradient(
                    colors: [color, color.opacity(0.78)],
                    startPoint: .top,
                    endPoint: .bottom
                )
            )
            .clipShape(RoundedRectangle(cornerRadius: 19))
            .overlay(
                RoundedRectangle(cornerRadius: 19)
                    .stroke(.white.opacity(0.56), lineWidth: 1)
            )
            .overlay(alignment: .top) {
                Capsule()
                    .fill(Color.white.opacity(0.30))
                    .frame(width: size * 0.58, height: 1)
                    .padding(.top, 2)
            }
            .shadow(color: color.opacity(0.55), radius: 15, y: 4)
    }
}


private struct SplashCalendarBackdrop: View {
    private let columns = Array(repeating: GridItem(.flexible(), spacing: 6), count: 7)

    var body: some View {
        VStack(spacing: 8) {
            Text(DateFormatter.monthTitle.string(from: Date()).uppercased())
                .font(.system(size: 25, weight: .black))
                .foregroundStyle(RColors.text.opacity(0.18))
            LazyVGrid(columns: columns, spacing: 6) {
                ForEach(0..<35, id: \.self) { index in
                    RoundedRectangle(cornerRadius: 8)
                        .fill(tileColor(index))
                        .aspectRatio(0.94, contentMode: .fit)
                }
            }
        }
        .frame(width: 320)
        .rotationEffect(.degrees(-5))
        .offset(y: -235)
        .opacity(0.75)
    }

    private func tileColor(_ index: Int) -> Color {
        if index % 5 == 0 { return RColors.day.opacity(0.20) }
        if index % 4 == 0 { return RColors.night.opacity(0.16) }
        return RColors.card2.opacity(0.30)
    }
}
