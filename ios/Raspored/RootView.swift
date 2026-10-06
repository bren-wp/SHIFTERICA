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
            Text(label).font(.system(size: 16, weight: .heavy)).foregroundStyle(active ? .white : RColors.muted).frame(maxWidth: .infinity).padding(.vertical, 13).background(active ? RColors.accent.opacity(0.22) : .clear).clipShape(RoundedRectangle(cornerRadius: 17)).overlay(RoundedRectangle(cornerRadius: 17).stroke(active ? RColors.accent : .clear, lineWidth: 1.2))
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
                Image(systemName: "plus").font(.system(size: 27, weight: .bold)).foregroundStyle(.white).frame(width: 54, height: 54).background(RColors.accent).clipShape(RoundedRectangle(cornerRadius: 19))
            }.buttonStyle(.plain)
        }.padding(.horizontal, 18).padding(.vertical, 7)
    }

    private func headerButton(_ symbol: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Image(systemName: symbol).font(.system(size: 23, weight: .semibold)).foregroundStyle(RColors.text).frame(width: 48, height: 48).background(RColors.card2).clipShape(RoundedRectangle(cornerRadius: 17))
        }.buttonStyle(.plain)
    }
}

struct AppMark: View {
    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 15).fill(RColors.card2).overlay(RoundedRectangle(cornerRadius: 15).stroke(RColors.accent, lineWidth: 1.5))
            VStack(spacing: 3) {
                RoundedRectangle(cornerRadius: 3).fill(RColors.accent).frame(width:25,height:7)
                HStack(spacing:3) {
                    ForEach(0..<3,id:\.self) { i in
                        RoundedRectangle(cornerRadius:2).fill(i==1 ? RColors.day : RColors.accent).frame(width:6,height:6)
                    }
                }
                HStack(spacing:3) {
                    ForEach(0..<3,id:\.self) { _ in
                        RoundedRectangle(cornerRadius:2).fill(RColors.accent.opacity(0.9)).frame(width:6,height:6)
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
            VStack(spacing:18) {
                AppMark().frame(width:122,height:122).padding(18).background(RColors.card2).clipShape(RoundedRectangle(cornerRadius:32)).overlay(RoundedRectangle(cornerRadius:32).stroke(RColors.accent,lineWidth:2))
                Text("Raspored").font(.system(size:40,weight:.black)).foregroundStyle(RColors.text)
                Text("Pametni planer smjena").font(.system(size:17)).foregroundStyle(RColors.muted)
                ProgressView(value:0.68).tint(RColors.accent).frame(width:220)
            }
        }
    }
}
