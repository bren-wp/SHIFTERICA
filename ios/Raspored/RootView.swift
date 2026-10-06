import SwiftUI

enum MainSectionIOS { case month, year, summary }

struct RootView: View {
    @EnvironmentObject var schedule: ScheduleStoreIOS
    @State private var section: MainSectionIOS = .month
    @State private var month: Date = Calendar.raspored.date(from: DateComponents(year: 2026, month: 10, day: 1)) ?? Date()
    @State private var showShifts = false
    @State private var showNewShift = false
    @State private var showSettings = false
    @State private var showSearch = false
    @State private var showSplash = true

    var body: some View {
        ZStack {
            LinearGradient(colors: [RColors.bg, RColors.bg2.opacity(0.88), .black.opacity(0.92)], startPoint: .top, endPoint: .bottom).ignoresSafeArea()
            VStack(spacing: 7) {
                HeaderView(onSearch: { showSearch = true }, onSettings: { showSettings = true }, onAdd: { showNewShift = true })
                topTabs
                Group {
                    switch section {
                    case .month: MonthView(month: $month, onOpenShifts: { showShifts = true })
                    case .year: YearOverviewView(year: Calendar.raspored.component(.year, from: month), onMonth: { month = $0; section = .month })
                    case .summary: SummaryView(month: $month)
                    }
                }.frame(maxWidth: .infinity, maxHeight: .infinity)
            }
            if showSplash { SplashOverlay().transition(.opacity) }
        }
        .task { try? await Task.sleep(for: .milliseconds(1200)); withAnimation(.easeOut(duration: 0.35)) { showSplash = false } }
        .sheet(isPresented: $showShifts) { ShiftManagerView(onNew: { showShifts = false; showNewShift = true }) }
        .sheet(isPresented: $showNewShift) { NewShiftView() }
        .sheet(isPresented: $showSettings) { SettingsView() }
        .sheet(isPresented: $showSearch) { SearchView(onPick: { month = $0; section = .month; showSearch = false }) }
    }

    private var topTabs: some View {
        HStack(spacing: 4) {
            tab(DateFormatter.monthOnly.string(from: month).uppercased(), active: section == .month) { section = .month }
            tab(String(Calendar.raspored.component(.year, from: month)), active: section == .year) { section = .year }
            tab("SAŽETAK", active: section == .summary) { section = .summary }
        }
        .padding(4).background(RColors.card).clipShape(RoundedRectangle(cornerRadius: 22)).overlay(RoundedRectangle(cornerRadius: 22).stroke(RColors.stroke.opacity(0.6), lineWidth: 1)).padding(.horizontal, 16)
    }

    private func tab(_ label: String, active: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(label)
                .font(.system(size: 16, weight: .heavy))
                .foregroundStyle(active ? .white : RColors.muted)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 15)
                .background(active ? RColors.accent.opacity(0.22) : .clear)
                .clipShape(RoundedRectangle(cornerRadius: 17))
                .overlay(RoundedRectangle(cornerRadius: 17).stroke(active ? RColors.accent : .clear, lineWidth: 1.2))
                .shadow(color: active ? RColors.accent.opacity(0.34) : .clear, radius: 8, y: 2)
        }.buttonStyle(.plain)
    }
}

private struct HeaderView: View {
    let onSearch: () -> Void, onSettings: () -> Void, onAdd: () -> Void
    var body: some View {
        HStack(spacing: 11) {
            AppMark().frame(width: 46, height: 46)
            Text("Raspored").font(.system(size: 31, weight: .black)).foregroundStyle(RColors.text)
            Spacer()
            headerButton("magnifyingglass", action: onSearch)
            headerButton("slider.horizontal.3", action: onSettings)
            Button(action: onAdd) {
                Image(systemName: "plus")
                    .font(.system(size: 28, weight: .bold))
                    .foregroundStyle(.white)
                    .frame(width: 56, height: 56)
                    .background(RColors.accent)
                    .clipShape(RoundedRectangle(cornerRadius: 20))
                    .overlay(RoundedRectangle(cornerRadius: 20).stroke(Color.white.opacity(0.55), lineWidth: 1))
                    .shadow(color: RColors.accent.opacity(0.45), radius: 12, y: 4)
            }.buttonStyle(.plain)
        }.padding(.horizontal, 18).padding(.vertical, 7)
    }

    private func headerButton(_ symbol: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Image(systemName: symbol)
                .font(.system(size: 23, weight: .semibold))
                .foregroundStyle(RColors.text)
                .frame(width: 50, height: 50)
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

private struct SplashOverlay: View {
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

            VStack(spacing:18) {
                AppMark().frame(width:126,height:126)
                Text("Raspored").font(.system(size:40,weight:.black)).foregroundStyle(RColors.text)
                Text("Pametni planer smjena").font(.system(size:17)).foregroundStyle(RColors.muted)
                Spacer().frame(height:78)
                ProgressView(value:0.68).tint(RColors.accent).frame(width:250)
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
            .background(color)
            .clipShape(RoundedRectangle(cornerRadius: 18))
            .overlay(RoundedRectangle(cornerRadius: 18).stroke(.white.opacity(0.5), lineWidth: 1))
            .shadow(color: color.opacity(0.55), radius: 14)
    }
}


private struct SplashCalendarBackdrop: View {
    private let columns = Array(repeating: GridItem(.flexible(), spacing: 6), count: 7)

    var body: some View {
        VStack(spacing: 8) {
            Text("LISTOPAD 2026")
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
