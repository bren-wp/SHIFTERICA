import SwiftUI

struct SummaryView: View {
    @EnvironmentObject private var schedule: ScheduleStoreIOS
    @EnvironmentObject private var shifts: ShiftLibraryIOS
    @Binding var month: Date
    @State private var scope = 0
    @State private var query = ""
    @State private var filter = 1
    @State private var includedCodes: Set<String> = ["N", "D", "J", "GO", "BO"]

    var body: some View {
        ScrollView {
            VStack(spacing: 10) {
                segmented(["Mjesec", "Godina", "Razdoblje"], selected: scope) { scope = $0 }
                HStack {
                    arrow("chevron.left") { changeMonth(-1) }
                    Spacer()
                    Text(DateFormatter.monthTitle.string(from: month).uppercased())
                        .font(.system(size: 22, weight: .black))
                        .foregroundStyle(RColors.text)
                    Spacer()
                    arrow("chevron.right") { changeMonth(1) }
                }
                .padding(8)
                .background(RColors.card)
                .clipShape(RoundedRectangle(cornerRadius: 22))
                .overlay(RoundedRectangle(cornerRadius: 22).stroke(RColors.stroke, lineWidth: 1))
                .shadow(color: .black.opacity(0.23), radius: 8, y: 3)

                overview
                totals

                HStack {
                    Image(systemName: "magnifyingglass")
                    TextField("Pretraži smjene...", text: $query)
                        .textInputAutocapitalization(.never)
                }
                .foregroundStyle(RColors.muted)
                .padding(14)
                .background(RColors.card)
                .clipShape(RoundedRectangle(cornerRadius: 18))
                .overlay(RoundedRectangle(cornerRadius: 18).stroke(RColors.stroke, lineWidth: 1))

                segmented(["Prošle", "Sve", "Nadolazeće"], selected: filter) { filter = $0 }
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 7)
        }
    }

    private var overview: some View {
        let preferred = ["N", "D", "J", "GO", "BO"].compactMap { code in shifts.byCode(code) }
        return VStack(alignment: .leading, spacing: 8) {
            Text("Pregled smjena").font(.system(size: 22, weight: .black)).foregroundStyle(RColors.text)
            HStack {
                Text("Smjena").frame(maxWidth: .infinity, alignment: .leading)
                Text("Broj").frame(width: 42)
                Text("Vrijeme").frame(width: 90)
                Text("Uključeno").frame(width: 78)
            }
            .font(.caption.bold())
            .foregroundStyle(RColors.muted)

            ForEach(preferred) { shift in
                row(shift)
            }
        }
        .padding(14)
        .background(RColors.card)
        .clipShape(RoundedRectangle(cornerRadius: 24))
        .overlay(RoundedRectangle(cornerRadius: 24).stroke(RColors.stroke, lineWidth: 1))
        .shadow(color: .black.opacity(0.25), radius: 9, y: 3)
    }

    private func row(_ shift: ShiftTypeDef) -> some View {
        let count = schedule.count(month, code: shift.code)
        let minutes = count * ((shift.code == "GO" || shift.code == "BO") ? 8 * 60 : shift.durationMinutes)
        return HStack(spacing: 8) {
            Text(shift.code)
                .font(.system(size: 15, weight: .black))
                .foregroundStyle(shift.textColor)
                .frame(width: 42, height: 42)
                .background(shift.color)
                .clipShape(RoundedRectangle(cornerRadius: 10))
            VStack(alignment: .leading, spacing: 1) {
                Text(shift.shortName).font(.subheadline.bold()).foregroundStyle(RColors.text)
                if let time = shift.timeText { Text(time).font(.system(size: 9)).foregroundStyle(RColors.muted) }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            Text(String(count)).frame(width: 42).foregroundStyle(RColors.text)
            Text(format(minutes)).frame(width: 90).font(.caption.bold()).foregroundStyle(RColors.text)
            Toggle(
                "",
                isOn: Binding(
                    get: { includedCodes.contains(shift.code) },
                    set: { enabled in
                        if enabled { includedCodes.insert(shift.code) }
                        else { includedCodes.remove(shift.code) }
                    }
                )
            )
            .labelsHidden()
            .tint(RColors.accent)
            .frame(width: 78)
        }
        .padding(8)
        .background(RColors.card2)
        .clipShape(RoundedRectangle(cornerRadius: 15))
    }

    private var totals: some View {
        let summary = CroatianWorkTimeIOS.summarize(
            month: month,
            schedule: schedule,
            shifts: shifts.all,
            includedCodes: includedCodes
        )
        let columns = [
            GridItem(.flexible(), spacing: 7),
            GridItem(.flexible(), spacing: 7)
        ]

        return VStack(alignment: .leading, spacing: 9) {
            Text("Obračun sati")
                .font(.system(size: 21, weight: .black))
                .foregroundStyle(RColors.text)

            Text("Fond sati računa radne dane od ponedjeljka do petka. GO i BO priznaju 8 sati na radni dan, a prazan državni blagdan također se priznaje kao 8 sati.")
                .font(.system(size: 9.5))
                .foregroundStyle(RColors.muted)

            LazyVGrid(columns: columns, spacing: 7) {
                stat("clock.fill", "Odrađeni sati", format(summary.workedMinutes), RColors.day)
                stat("person.2.fill", "Redovni sati", format(summary.regularMinutes), RColors.accent)
                stat("calendar", "Fond sati", format(summary.fundMinutes), RColors.morning)
                stat("chart.bar.fill", "Prekovremeni sati", format(summary.overtimeMinutes), RColors.sick)
                stat("heart.text.square.fill", "Plaćene odsutnosti", format(summary.paidAbsenceMinutes), RColors.night)
                stat("checkmark.circle.fill", "Ukupno priznato", format(summary.creditedMinutes), RColors.annual)
            }
        }
        .padding(13)
        .background(RColors.card)
        .clipShape(RoundedRectangle(cornerRadius: 24))
        .overlay(RoundedRectangle(cornerRadius: 24).stroke(RColors.stroke, lineWidth: 1))
        .shadow(color: .black.opacity(0.25), radius: 9, y: 3)
    }

    private func stat(_ icon: String, _ label: String, _ value: String, _ tint: Color) -> some View {
        VStack(alignment: .leading, spacing: 5) {
            Image(systemName: icon).foregroundStyle(tint)
            Text(label).font(.system(size: 9)).foregroundStyle(RColors.muted)
            Text(value).font(.system(size: 16, weight: .black)).foregroundStyle(RColors.text)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(9)
        .background(RColors.card2)
        .clipShape(RoundedRectangle(cornerRadius: 15))
    }

    private func segmented(_ labels: [String], selected: Int, onSelect: @escaping (Int) -> Void) -> some View {
        HStack(spacing: 4) {
            ForEach(Array(labels.enumerated()), id: \.offset) { index, label in
                Button { onSelect(index) } label: {
                    Text(label)
                        .font(.subheadline.bold())
                        .foregroundStyle(index == selected ? .white : RColors.muted)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 11)
                        .background(index == selected ? RColors.accent.opacity(0.22) : .clear)
                        .clipShape(RoundedRectangle(cornerRadius: 15))
                        .overlay(RoundedRectangle(cornerRadius: 15).stroke(index == selected ? RColors.accent : .clear, lineWidth: 1))
                }
                .buttonStyle(.plain)
            }
        }
        .padding(4)
        .background(RColors.card)
        .clipShape(RoundedRectangle(cornerRadius: 20))
        .overlay(RoundedRectangle(cornerRadius: 20).stroke(RColors.stroke, lineWidth: 1))
    }

    private func arrow(_ symbol: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Image(systemName: symbol)
                .foregroundStyle(RColors.text)
                .frame(width: 42, height: 42)
                .background(RColors.card2)
                .clipShape(Circle())
        }
        .buttonStyle(.plain)
    }

    private func changeMonth(_ value: Int) {
        month = Calendar.raspored.date(byAdding: .month, value: value, to: month) ?? month
    }

    private func format(_ minutes: Int) -> String {
        String(minutes / 60) + " h " + String(minutes % 60) + " min"
    }
}
