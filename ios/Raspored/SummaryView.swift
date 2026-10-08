import SwiftUI

struct SummaryView: View {
    @EnvironmentObject private var schedule: ScheduleStoreIOS
    @EnvironmentObject private var shifts: ShiftLibraryIOS
    @EnvironmentObject private var accounting: MonthlyAccountingStoreIOS
    @Binding var month: Date
    @State private var section = 0
    @State private var includedCodes: Set<String> = ["N", "D", "P", "J", "GO", "BO"]

    var body: some View {
        ScrollView {
            VStack(spacing: 10) {
                segmented(
                    ["Smjene", "Sati", "Plaća"],
                    selected: section
                ) { section = $0 }

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
                .overlay(
                    RoundedRectangle(cornerRadius: 22)
                        .stroke(RColors.stroke, lineWidth: 1)
                )
                .shadow(color: .black.opacity(0.23), radius: 8, y: 3)

                summarySection
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 7)
        }
    }

    @ViewBuilder
    private var summarySection: some View {
        switch section {
        case 0:
            overview
        case 1:
            fundEditor
            totals
        default:
            PayrollEstimateCardIOS(month: month)
            annualEarnings
        }
    }


    private var fundEditor: some View {
        let computed = CroatianWorkTimeIOS.summarize(
            month: month,
            schedule: schedule,
            shifts: shifts.all
        ).fundMinutes / 60
        let override = accounting.fundOverrideMinutes(month).map { $0 / 60 }
        let shown = override ?? computed

        return VStack(alignment: .leading, spacing: 8) {
            Text("Fond sati")
                .font(.system(size: 20, weight: .black))
                .foregroundStyle(RColors.text)
            Text("Automatski: \(computed) h. Ručno promijenite samo ako službeni fond odstupa.")
                .font(.caption)
                .foregroundStyle(RColors.muted)
            HStack {
                Button("−1 h") {
                    accounting.setFundHours(max(0, shown - 1), month: month)
                }
                .buttonStyle(.bordered)
                Spacer()
                Text("\(shown) h")
                    .font(.headline.bold())
                    .foregroundStyle(RColors.text)
                Spacer()
                Button("+1 h") {
                    accounting.setFundHours(min(744, shown + 1), month: month)
                }
                .buttonStyle(.bordered)
            }
            if override != nil {
                Button("Vrati automatski fond") {
                    accounting.setFundHours(nil, month: month)
                }
                .font(.subheadline)
                .foregroundStyle(RColors.accent)
            }
        }
        .padding(13)
        .background(RColors.card)
        .clipShape(RoundedRectangle(cornerRadius: 20))
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(RColors.stroke, lineWidth: 1)
        )
    }

    private var annualEarnings: some View {
        let actual = accounting.actualForYear(Calendar.raspored.component(.year, from: month))
        let recent = accounting.latestThreeActual(upTo: month)
        return VStack(alignment: .leading, spacing: 10) {
            Text("Godišnja zarada")
                .font(.system(size: 21, weight: .black))
                .foregroundStyle(RColors.text)
            Text("Potvrđeni neto unesite nakon primitka platne liste. Procjene se ne zbrajaju kao stvarna zarada.")
                .font(.caption)
                .foregroundStyle(RColors.muted)

            ConfirmedNetField(month: month)

            Text("Potvrđeno: \(money(actual.reduce(0) { $0 + $1.1 })) (\(actual.count) mj.)")
                .font(.subheadline.bold())
                .foregroundStyle(RColors.text)
            Text(
                recent.count == 3
                    ? "Prosjek zadnje 3 potvrđene plaće: \(money(recent.reduce(0,+) / 3))"
                    : "Prosjek 3 plaće bit će vidljiv nakon tri potvrđena unosa."
            )
            .font(.caption)
            .foregroundStyle(RColors.muted)
            ForEach(actual, id: \.0) { item in
                HStack {
                    Text(item.0).foregroundStyle(RColors.muted)
                    Spacer()
                    Text(money(item.1))
                        .fontWeight(.semibold)
                        .foregroundStyle(RColors.text)
                }
            }
        }
        .padding(13)
        .background(RColors.card)
        .clipShape(RoundedRectangle(cornerRadius: 22))
        .overlay(RoundedRectangle(cornerRadius: 22).stroke(RColors.stroke, lineWidth: 1))
    }

    private func money(_ value: Double) -> String {
        let formatter = NumberFormatter()
        formatter.locale = Locale(identifier: "hr_HR")
        formatter.numberStyle = .currency
        formatter.currencyCode = "EUR"
        return formatter.string(from: NSNumber(value: value)) ?? String(format: "%.2f €", value)
    }

    private var overview: some View {
        let preferred = ["N", "D", "P", "J", "GO", "BO"].compactMap { code in shifts.byCode(code) }
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
                .background(
                    LinearGradient(
                        colors: [shift.color, shift.color.opacity(0.80)],
                        startPoint: .top,
                        endPoint: .bottom
                    )
                )
                .clipShape(RoundedRectangle(cornerRadius: 11))
                .overlay(
                    RoundedRectangle(cornerRadius: 11)
                        .stroke(shift.color.opacity(0.95), lineWidth: 1)
                )
                .shadow(color: shift.color.opacity(0.28), radius: 5, y: 2)
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
            includedCodes: includedCodes,
            fundOverrideMinutes: accounting.fundOverrideMinutes(month)
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

            Text("Raspodjela stvarno odrađenih sati")
                .font(.system(size: 14, weight: .heavy))
                .foregroundStyle(RColors.text)
                .padding(.top, 2)

            LazyVGrid(columns: columns, spacing: 7) {
                stat("sun.max.fill", "Dnevni sati", format(summary.dayMinutes), RColors.day)
                stat("moon.stars.fill", "Noćni 22–06", format(summary.nightMinutes), RColors.night)
                stat("calendar", "Subota", format(summary.saturdayMinutes), RColors.morning)
                stat("calendar", "Nedjelja", format(summary.sundayMinutes), RColors.sick)
                stat("star.fill", "Blagdan — rad", format(summary.holidayWorkedMinutes), RColors.annual)
                stat("clock.fill", "Sati 14–22", format(summary.secondShiftMinutes), RColors.accent)
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
                        .background(index == selected ? RColors.accent.opacity(0.28) : .clear)
                        .clipShape(RoundedRectangle(cornerRadius: 15))
                        .overlay(RoundedRectangle(cornerRadius: 15).stroke(index == selected ? RColors.accent : .clear, lineWidth: index == selected ? 1.2 : 1))
                        .shadow(color: index == selected ? RColors.accent.opacity(0.30) : .clear, radius: 7, y: 2)
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


private struct ConfirmedNetField: View {
    @EnvironmentObject private var accounting: MonthlyAccountingStoreIOS
    let month: Date
    @State private var amount = ""
    @State private var invalid = false

    var body: some View {
        VStack(alignment: .leading, spacing: 7) {
            TextField("Stvarni neto (€)", text: $amount)
                .keyboardType(.decimalPad)
                .textFieldStyle(.roundedBorder)
                .accessibilityLabel("Potvrđena neto plaća za mjesec")
            HStack {
                Button("Spremi neto") {
                    let normalized = amount.contains(",")
                        ? amount.replacingOccurrences(of: ".", with: "")
                            .replacingOccurrences(of: ",", with: ".")
                        : amount
                    guard let value = Double(normalized),
                          value.isFinite, (0...1_000_000).contains(value)
                    else { invalid = true; return }
                    accounting.setActualNet(value, month: month)
                    invalid = false
                }
                .buttonStyle(.borderedProminent)
                .tint(RColors.accent)
                if accounting.actualNet(month) != nil {
                    Button("Ukloni") {
                        accounting.setActualNet(nil, month: month)
                        amount = ""
                        invalid = false
                    }
                    .buttonStyle(.bordered)
                }
            }
            if invalid {
                Text("Upišite valjan iznos u eurima.")
                    .font(.caption)
                    .foregroundStyle(.red)
            }
        }
        .onAppear(perform: refresh)
        .onChange(of: month) { _, _ in refresh() }
    }

    private func refresh() {
        let formatter = NumberFormatter()
        formatter.locale = Locale(identifier: "hr_HR")
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        amount = accounting.actualNet(month).flatMap {
            formatter.string(from: NSNumber(value: $0))
        } ?? ""
        invalid = false
    }
}
