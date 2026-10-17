import SwiftUI

/// Local, user-entered coefficient and base; no previous payslip values are imported.
struct CalculationProfileEditorIOS: View {
    @EnvironmentObject private var accounting: MonthlyAccountingStoreIOS
    @State private var coefficient = ""
    @State private var base = ""
    @State private var invalid = false

    var body: some View {
        VStack(alignment: .leading, spacing: 9) {
            Text("Podaci za izračun")
                .font(.system(size: 19, weight: .black))
                .foregroundStyle(RColors.text)
            Text("Bod i mjesečna osnovica nisu unaprijed uneseni. Unesite vlastite vrijednosti za procjenu.")
                .font(.caption)
                .foregroundStyle(RColors.muted)
            TextField("Bod / koeficijent", text: $coefficient)
                .keyboardType(.decimalPad)
                .textInputAutocapitalization(.never)
                .padding(12)
                .background(RColors.bg2, in: RoundedRectangle(cornerRadius: 12))
                .accessibilityLabel("Bod ili koeficijent")
            TextField("Mjesečna osnovica (€)", text: $base)
                .keyboardType(.decimalPad)
                .textInputAutocapitalization(.never)
                .padding(12)
                .background(RColors.bg2, in: RoundedRectangle(cornerRadius: 12))
                .accessibilityLabel("Mjesečna osnovica u eurima")
            if invalid {
                Text("Unesite valjane pozitivne brojeve.")
                    .font(.caption)
                    .foregroundStyle(RColors.danger)
            }
            Button("Spremi parametre") {
                let point = Double(coefficient.replacingOccurrences(of: ",", with: "."))
                let amount = Double(base.replacingOccurrences(of: ",", with: "."))
                guard let point, let amount,
                      point.isFinite, amount.isFinite,
                      (0.0001...100).contains(point),
                      (0.01...100_000).contains(amount) else {
                    invalid = true
                    return
                }
                accounting.userCoefficient = point
                accounting.userMonthlyBase = amount
                accounting.saveProfile()
                invalid = false
            }
            .font(.subheadline.bold())
            .foregroundStyle(RColors.accent)
            .frame(maxWidth: .infinity, minHeight: 46)
            Button("Očisti parametre") {
                accounting.userCoefficient = 0
                accounting.userMonthlyBase = 0
                accounting.saveProfile()
                coefficient = ""
                base = ""
                invalid = false
            }
            .foregroundStyle(RColors.muted)
            .frame(maxWidth: .infinity, minHeight: 42)
        }
        .padding(13)
        .background(RColors.card, in: RoundedRectangle(cornerRadius: 20))
        .overlay(RoundedRectangle(cornerRadius: 20)
            .stroke(RColors.stroke, lineWidth: 1))
        .onAppear {
            coefficient = accounting.userCoefficient > 0
                ? String(accounting.userCoefficient) : ""
            base = accounting.userMonthlyBase > 0
                ? String(accounting.userMonthlyBase) : ""
        }
    }
}
