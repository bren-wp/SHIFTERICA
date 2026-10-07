import SwiftUI

struct PayrollSettingsView: View {
    @EnvironmentObject private var settings: PayrollSettingsStoreIOS
    @Environment(\.dismiss) private var dismiss

    @State private var coefficientText = ""
    @State private var birthYearText = ""
    @State private var lowerTaxText = ""
    @State private var higherTaxText = ""

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [RColors.bg2, RColors.bg, .black],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            ScrollView {
                VStack(alignment: .leading, spacing: 12) {
                    Capsule()
                        .fill(RColors.muted.opacity(0.5))
                        .frame(width: 54, height: 5)
                        .frame(maxWidth: .infinity)

                    Text("Procjena plaće")
                        .font(.system(size: 31, weight: .black))
                        .foregroundStyle(RColors.text)
                    Text("Lokalni orijentacijski obračun iz rasporeda i službenih pravila.")
                        .foregroundStyle(RColors.muted)

                    group {
                        Toggle("Prikaži procjenu", isOn: $settings.enabled)
                            .tint(RColors.accent)
                            .foregroundStyle(RColors.text)

                        Menu {
                            ForEach(CroatianPayrollRulesIOS.coefficientPresets) { preset in
                                Button {
                                    settings.selectPreset(preset.id)
                                    coefficientText = String(format: "%.2f", settings.coefficient)
                                } label: {
                                    Text("\(preset.label) — \(String(format: "%.2f", preset.coefficient))")
                                }
                            }
                        } label: {
                            HStack {
                                VStack(alignment: .leading, spacing: 2) {
                                    Text("Radno mjesto / koeficijent")
                                        .fontWeight(.bold)
                                        .foregroundStyle(RColors.text)
                                    Text("\(settings.coefficientLabel) — \(String(format: "%.2f", settings.coefficient))")
                                        .font(.caption)
                                        .foregroundStyle(RColors.muted)
                                }
                                Spacer()
                                Image(systemName: "chevron.down")
                                    .foregroundStyle(RColors.accent)
                            }
                        }
                        .buttonStyle(.plain)

                        field("Ručni koeficijent", text: $coefficientText, keyboard: .decimalPad)

                        Stepper(
                            "Navršene godine staža: \(settings.completedYearsService)",
                            value: $settings.completedYearsService,
                            in: 0...60
                        )
                        .foregroundStyle(RColors.text)

                        Stepper(
                            "Djeca na poreznoj kartici: \(settings.children)",
                            value: $settings.children,
                            in: 0...9
                        )
                        .foregroundStyle(RColors.text)

                        Stepper(
                            "Ostali uzdržavani članovi: \(settings.dependents)",
                            value: $settings.dependents,
                            in: 0...9
                        )
                        .foregroundStyle(RColors.text)

                        field(
                            "Godina rođenja — opcionalno",
                            text: $birthYearText,
                            keyboard: .numberPad
                        )
                        Text("Godina rođenja služi samo za procjenu godišnjeg poreznog povrata za mlade.")
                            .font(.caption2)
                            .foregroundStyle(RColors.muted)

                        Toggle("Rad u turnusu", isOn: $settings.turnusEnabled)
                            .tint(RColors.accent)
                            .foregroundStyle(RColors.text)
                        Text("Kada je turnus uključen, procjena primjenjuje 5% dodatka i ne kumulira dodatak za drugu smjenu za iste sate.")
                            .font(.caption2)
                            .foregroundStyle(RColors.muted)

                        HStack(spacing: 8) {
                            field("Niža %", text: $lowerTaxText, keyboard: .decimalPad)
                            field("Viša %", text: $higherTaxText, keyboard: .decimalPad)
                        }
                        Text("Zadano je 20% / 25% za Rijeku u 2026. Porezne stope ovise o prebivalištu i mogu se promijeniti.")
                            .font(.caption2)
                            .foregroundStyle(RColors.muted)

                        Button {
                            save()
                            dismiss()
                        } label: {
                            Text("Spremi")
                                .font(.headline)
                                .foregroundStyle(.white)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 13)
                                .background(RColors.accent)
                                .clipShape(RoundedRectangle(cornerRadius: 15))
                                .shadow(color: RColors.accent.opacity(0.35), radius: 9, y: 3)
                        }
                        .buttonStyle(.plain)
                    }

                    Text("Izvori ugrađenih pravila: NN 11/2026, NN 29/2024, NN 22/2024, NN 4/2025, NN 152/2024 i službena tumačenja TKU-a. Procjena nije službena obračunska isprava.")
                        .font(.caption2)
                        .foregroundStyle(RColors.muted)
                        .padding(.bottom, 16)
                }
                .padding(18)
            }
        }
        .onAppear {
            coefficientText = String(format: "%.2f", settings.coefficient)
            birthYearText = settings.birthYear > 0 ? String(settings.birthYear) : ""
            lowerTaxText = String(format: "%.1f", settings.lowerTaxPercent)
            higherTaxText = String(format: "%.1f", settings.higherTaxPercent)
        }
    }

    private func group<Content: View>(@ViewBuilder content: () -> Content) -> some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack(spacing: 8) {
                Image(systemName: "eurosign.circle.fill")
                    .foregroundStyle(RColors.accent)
                Text("Parametri obračuna")
                    .font(.system(size: 20, weight: .black))
                    .foregroundStyle(RColors.text)
            }
            content()
        }
        .padding(14)
        .background(RColors.card)
        .clipShape(RoundedRectangle(cornerRadius: 22))
        .overlay(RoundedRectangle(cornerRadius: 22).stroke(RColors.stroke, lineWidth: 1))
        .shadow(color: .black.opacity(0.25), radius: 9, y: 3)
    }

    private func field(
        _ title: String,
        text: Binding<String>,
        keyboard: UIKeyboardType
    ) -> some View {
        TextField(title, text: text)
            .keyboardType(keyboard)
            .foregroundStyle(RColors.text)
            .padding(12)
            .background(RColors.card2)
            .clipShape(RoundedRectangle(cornerRadius: 13))
            .overlay(RoundedRectangle(cornerRadius: 13).stroke(RColors.stroke, lineWidth: 1))
    }

    private func save() {
        if let value = Double(coefficientText.replacingOccurrences(of: ",", with: ".")) {
            settings.useManualCoefficient(value)
        }
        settings.birthYear = Int(birthYearText) ?? 0
        if let lower = Double(lowerTaxText.replacingOccurrences(of: ",", with: ".")) {
            settings.lowerTaxPercent = min(max(lower, 0), 50)
        }
        if let higher = Double(higherTaxText.replacingOccurrences(of: ",", with: ".")) {
            settings.higherTaxPercent = min(max(higher, 0), 50)
        }
    }
}
