import SwiftUI

/// Local user-entered figures; the estimate is retained independently.
struct ConfirmedNetEditorIOS: View {
    @EnvironmentObject private var accounting: MonthlyAccountingStoreIOS
    @EnvironmentObject private var schedule: ScheduleStoreIOS
    @EnvironmentObject private var shifts: ShiftLibraryIOS

    let month: Date
    @State private var netInput = ""
    @State private var annualInput = ""
    @State private var netError = false
    @State private var annualError = false

    private func moneyText(_ value: Double) -> String {
        String(format: "%.2f", value).replacingOccurrences(of: ".", with: ",")
    }

    private func displayEuro(_ value: Double) -> String {
        let formatter = NumberFormatter()
        formatter.locale = Locale(identifier: "hr_HR")
        formatter.numberStyle = .currency
        formatter.currencyCode = "EUR"
        return formatter.string(from: NSNumber(value: value)) ?? moneyText(value) + " €"
    }

    private func loadValues() {
        netInput = accounting.actualNet(month).map(moneyText) ?? ""
        annualInput = accounting.annualLeaveHourlyGross > 0
            ? moneyText(accounting.annualLeaveHourlyGross) : ""
        netError = false
        annualError = false
    }

    var body: some View {
        let actual = accounting.actualNet(month)
        let estimated = payrollEstimateForMonthIOS(
            month: month,
            schedule: schedule,
            shifts: shifts,
            fundOverrideMinutes: accounting.fundOverrideMinutes(month),
            serviceYears: accounting.serviceYearsForMonth(month),
            children: accounting.children,
            dependents: accounting.dependents,
            annualLeaveHourlyGross: accounting.annualLeaveHourlyGross,
            paymentDelayMonths: accounting.paymentDelayMonths(month)
        )?.netMonthly

        VStack(alignment: .leading, spacing: 9) {
            Text("Usporedba s isplatnom listom")
                .font(.system(size: 19, weight: .black))
                .foregroundStyle(RColors.text)
            Text("Upišite samo iznos, bez fotografije platne liste i osobnih podataka. Vrijednost ostaje na uređaju.")
                .font(.caption)
                .foregroundStyle(RColors.muted)
            Text("Procijenjeni neto: " + (estimated.map(displayEuro) ?? "—"))
                .font(.subheadline)
                .foregroundStyle(RColors.muted)

            if let actual {
                Text("Potvrđeni neto: " + displayEuro(actual))
                    .font(.subheadline.bold())
                    .foregroundStyle(RColors.accent)
                if let estimated {
                    let difference = actual - estimated
                    Text("Razlika (potvrđeni − procjena): " +
                         (difference >= 0 ? "+" : "−") + displayEuro(abs(difference)))
                        .font(.caption)
                        .foregroundStyle(RColors.text)
                }
            }

            TextField("Stvarno isplaćeni neto (€)", text: $netInput)
                .keyboardType(.decimalPad)
                .autocorrectionDisabled()
                .textInputAutocapitalization(.never)
                .padding(12)
                .background(RColors.card2, in: RoundedRectangle(cornerRadius: 12))
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(netError ? RColors.danger : RColors.stroke, lineWidth: 1)
                )
                .accessibilityLabel("Stvarno isplaćeni neto u eurima")
                .onChange(of: netInput) { _, _ in netError = false }
            if netError {
                Text("Neispravan iznos. Koristite npr. 1234,56 (najviše 1.000.000 €).")
                    .font(.caption)
                    .foregroundStyle(RColors.danger)
            }
            Button("Spremi potvrđeni neto") {
                guard let cents = PayrollMoneyInputIOS.parseCents(netInput) else {
                    netError = true
                    return
                }
                accounting.setActualNet(Double(cents) / 100, month: month)
                netInput = moneyText(Double(cents) / 100)
            }
            .buttonStyle(.borderedProminent)
            .tint(RColors.accent)
            .foregroundStyle(RColors.bg)
            if actual != nil {
                Button("Ukloni potvrđeni neto za ovaj mjesec") {
                    accounting.setActualNet(nil, month: month)
                    netInput = ""
                    netError = false
                }
                .font(.subheadline)
                .foregroundStyle(RColors.accent)
            }

            Text("Prosječna bruto satnica godišnjeg odmora")
                .font(.subheadline.bold())
                .foregroundStyle(RColors.text)
            Text("Opcionalno: unesite prosječnu bruto satnicu s obračuna. Bez unosa koristi se procjena prema osnovici.")
                .font(.caption)
                .foregroundStyle(RColors.muted)
            TextField("Bruto satnica GO (€/h)", text: $annualInput)
                .keyboardType(.decimalPad)
                .autocorrectionDisabled()
                .textInputAutocapitalization(.never)
                .padding(12)
                .background(RColors.card2, in: RoundedRectangle(cornerRadius: 12))
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(annualError ? RColors.danger : RColors.stroke, lineWidth: 1)
                )
                .accessibilityLabel("Prosječna bruto satnica za godišnji odmor u eurima")
                .onChange(of: annualInput) { _, _ in annualError = false }
            if annualError {
                Text("Unesite iznos veći od 0 i do 1.000 € po satu.")
                    .font(.caption)
                    .foregroundStyle(RColors.danger)
            }
            Button("Spremi satnicu za GO") {
                guard let cents = PayrollMoneyInputIOS.parseCents(annualInput),
                      cents > 0, cents <= 100_000 else {
                    annualError = true
                    return
                }
                accounting.annualLeaveHourlyGross = Double(cents) / 100
                accounting.saveProfile()
                annualInput = moneyText(Double(cents) / 100)
            }
            .buttonStyle(.bordered)
            .tint(RColors.accent)
            if accounting.annualLeaveHourlyGross > 0 {
                Button("Vrati zadanu procjenu GO") {
                    accounting.annualLeaveHourlyGross = 0
                    accounting.saveProfile()
                    annualInput = ""
                    annualError = false
                }
                .font(.subheadline)
                .foregroundStyle(RColors.accent)
            }
        }
        .padding(13)
        .background(RColors.card)
        .clipShape(RoundedRectangle(cornerRadius: 20))
        .overlay(
            RoundedRectangle(cornerRadius: 20).stroke(RColors.stroke, lineWidth: 1)
        )
        .onAppear(perform: loadValues)
        .onChange(of: month) { _, _ in loadValues() }
    }
}
