import SwiftUI

struct PayrollProfileViewIOS: View {
    @EnvironmentObject private var accounting: MonthlyAccountingStoreIOS
    @State private var payrollProfileExpanded = false
    @State private var annualHourlyInput = ""
    @State private var annualHourlyInvalid = false

    var body: some View {
        VStack(alignment: .leading, spacing: 9) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text("Postavke obračuna")
                        .font(.system(size: 18, weight: .black))
                        .foregroundStyle(RColors.text)
                    Text("Rijeka · koef. 1,25 · staž \(accounting.serviceYears) god. · djece \(accounting.children)")
                        .font(.system(size: 11))
                        .foregroundStyle(RColors.muted)
                }
                Spacer(minLength: 6)
                Button(payrollProfileExpanded ? "Sakrij" : "Uredi") {
                    withAnimation(.easeInOut(duration: 0.2)) {
                        payrollProfileExpanded.toggle()
                    }
                }
                .font(.subheadline.bold())
                .foregroundStyle(RColors.accent)
            }
            if payrollProfileExpanded {
                Text("Upišite podatke s platne liste. Bez staža i dječjih olakšica procjena može biti preniska. Sve ostaje na uređaju.")
                    .font(.system(size: 11))
                    .foregroundStyle(RColors.muted)
                Stepper("Godine staža: \(accounting.serviceYears)",
                    value: $accounting.serviceYears, in: 0...60)
                Stepper("Djeca za olakšicu: \(accounting.children)",
                    value: $accounting.children, in: 0...9)
                Stepper("Uzdržavani članovi: \(accounting.dependents)",
                    value: $accounting.dependents, in: 0...10)
                Text("Godišnji odmor · bruto satnica po prosjeku")
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundStyle(RColors.text)
                HStack(spacing: 8) {
                    TextField("€/h (opcionalno)", text: $annualHourlyInput)
                        .keyboardType(.decimalPad)
                        .textFieldStyle(.roundedBorder)
                    Button("Spremi") {
                        let normalized = annualHourlyInput.replacingOccurrences(of: ",", with: ".")
                        if annualHourlyInput.isEmpty {
                            accounting.annualLeaveHourlyGross = 0
                            accounting.saveProfile()
                            annualHourlyInvalid = false
                        } else if let value = Double(normalized), value.isFinite,
                                  value > 0 && value <= 1000 {
                            accounting.annualLeaveHourlyGross = value
                            accounting.saveProfile()
                            annualHourlyInvalid = false
                        } else {
                            annualHourlyInvalid = true
                        }
                    }
                    .font(.subheadline.bold())
                }
                if annualHourlyInvalid {
                    Text("Unesite valjanu bruto satnicu u eurima.")
                        .font(.caption).foregroundStyle(.red)
                }
                Text("Ako nije poznata, ostavite prazno; izračun GO bit će orijentacijski.")
                    .font(.system(size: 10))
                    .foregroundStyle(RColors.muted)
            }
        }
        .font(.subheadline)
        .foregroundStyle(RColors.text)
        .onChange(of: accounting.serviceYears) { _, _ in accounting.saveProfile() }
        .onChange(of: accounting.children) { _, _ in accounting.saveProfile() }
        .onChange(of: accounting.dependents) { _, _ in accounting.saveProfile() }
        .onAppear {
            if accounting.annualLeaveHourlyGross > 0 {
                annualHourlyInput = String(
                    format: "%.2f", accounting.annualLeaveHourlyGross
                ).replacingOccurrences(of: ".", with: ",")
            }
            if accounting.serviceYears == 0 && accounting.children == 0 &&
               accounting.dependents == 0 {
                payrollProfileExpanded = true
            }
        }
        .padding(14)
        .background(RColors.card)
        .clipShape(RoundedRectangle(cornerRadius: 22))
        .overlay(RoundedRectangle(cornerRadius: 22)
            .stroke(RColors.stroke, lineWidth: 1))
    }


}
