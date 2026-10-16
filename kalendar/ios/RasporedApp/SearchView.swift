import SwiftUI

struct SearchView: View {
    @EnvironmentObject private var schedule: ScheduleStoreIOS
    @EnvironmentObject private var shifts: ShiftLibraryIOS
    @Environment(\.dismiss) private var dismiss
    let onPick: (Date) -> Void
    @State private var query = ""

    var body: some View {
        ZStack {
            LinearGradient(colors: [RColors.bg2, RColors.bg, .black], startPoint: .top, endPoint: .bottom).ignoresSafeArea()
            ScrollView {
                VStack(alignment: .leading, spacing: 10) {
                    Capsule().fill(RColors.muted.opacity(0.5)).frame(width: 54, height: 5).frame(maxWidth: .infinity)
                    HStack {
                        Text("Pretraži raspored")
                            .font(.system(size: 29, weight: .black))
                            .foregroundStyle(RColors.text)
                        Spacer()
                        Button { dismiss() } label: {
                            Image(systemName: "xmark")
                                .font(.title3.bold())
                                .foregroundStyle(RColors.text)
                                .frame(width: 46, height: 46)
                                .background(RColors.card2)
                                .clipShape(Circle())
                                .overlay(Circle().stroke(RColors.stroke.opacity(0.7), lineWidth: 1))
                                .shadow(color: .black.opacity(0.25), radius: 6, y: 3)
                        }
                        .buttonStyle(.plain)
                    }

                    HStack(spacing: 10) {
                        Image(systemName: "magnifyingglass").foregroundStyle(RColors.muted)
                        TextField("Datum, oznaka ili naziv smjene", text: $query)
                            .textInputAutocapitalization(.never)
                            .foregroundStyle(RColors.text)
                    }
                    .padding(.horizontal, 14)
                    .frame(height: 52)
                    .background(RColors.card2)
                    .clipShape(RoundedRectangle(cornerRadius: 17))
                    .overlay(RoundedRectangle(cornerRadius: 17).stroke(query.isEmpty ? RColors.stroke : RColors.accent, lineWidth: 1))

                    Text("Nadolazeće smjene prve, zatim najnoviji prethodni datumi.")
                        .font(.caption2)
                        .foregroundStyle(RColors.muted)

                    if results.isEmpty {
                        Text("Nema pronađenih smjena.")
                            .frame(maxWidth: .infinity)
                            .padding(20)
                            .foregroundStyle(RColors.muted)
                            .background(RColors.card)
                            .clipShape(RoundedRectangle(cornerRadius: 18))
                            .overlay(RoundedRectangle(cornerRadius: 18).stroke(RColors.stroke, lineWidth: 1))
                    } else {
                        ForEach(results, id: \.0) { date, code in
                            let shift = shifts.byCode(code)
                            Button {
                                onPick(date)
                            } label: {
                                HStack(spacing: 12) {
                                    Text(code)
                                        .fontWeight(.black)
                                        .foregroundStyle(shift?.textColor ?? RColors.text)
                                        .frame(width: 46, height: 46)
                                        .background(shift?.color ?? RColors.empty)
                                        .clipShape(RoundedRectangle(cornerRadius: 11))
                                        .shadow(color: (shift?.color ?? .clear).opacity(0.32), radius: 6, y: 3)
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text(DateFormatter.scheduleKey.string(from: date))
                                            .fontWeight(.bold)
                                            .foregroundStyle(RColors.text)
                                        Text(shift?.name ?? code)
                                            .font(.caption)
                                            .foregroundStyle(RColors.muted)
                                    }
                                    Spacer()
                                    Image(systemName: "chevron.right").foregroundStyle(RColors.muted)
                                }
                                .padding(12)
                                .background(RColors.card)
                                .clipShape(RoundedRectangle(cornerRadius: 18))
                                .overlay(RoundedRectangle(cornerRadius: 18).stroke(RColors.stroke, lineWidth: 1))
                                .shadow(color: .black.opacity(0.20), radius: 7, y: 3)
                            }
                            .buttonStyle(.plain)
                        }
                    }
                }
                .padding(18)
            }
        }
    }

    private var results: [(Date, String)] {
        let names = Dictionary(uniqueKeysWithValues:
            shifts.all.map { ($0.code, $0.name) }
        )
        return ScheduleSearchIOS.find(
            entries: schedule.entries,
            namesByCode: names,
            query: query,
            todayKey: DateFormatter.scheduleKey.string(from: Date())
        ).compactMap { key, code in
            guard let date = DateFormatter.scheduleKey.date(from: key) else {
                return nil
            }
            return (date, code)
        }
    }
}
