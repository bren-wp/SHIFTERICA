import SwiftUI

struct BuiltInShiftColorView: View {
    @EnvironmentObject private var shifts: ShiftLibraryIOS
    @Environment(\.dismiss) private var dismiss

    let shift: ShiftTypeDef

    @State private var backgroundHex: UInt32
    @State private var foregroundHex: UInt32
    @State private var start: String
    @State private var end: String
    @State private var secondaryStart: String
    @State private var secondaryEnd: String
    @State private var error: String?

    private let backgrounds: [UInt32] = [
        0xFFD21F, 0x13B7F3, 0xFF8A3D, 0x6CEB82,
        0x77DED7, 0xD991EE, 0xFF5BAA, 0x36485A
    ]

    private let foregrounds: [UInt32] = [
        0xFFFFFF, 0xD8D8D8, 0xAAAAAA, 0x777777,
        0x444444, 0x06131F, 0x000000
    ]

    private var isPaidAbsence: Bool {
        shift.code == "GO" || shift.code == "BO"
    }

    init(shift: ShiftTypeDef) {
        self.shift = shift
        _backgroundHex = State(initialValue: shift.backgroundHex)
        _foregroundHex = State(initialValue: shift.foregroundHex)
        _start = State(initialValue: shift.start ?? "")
        _end = State(initialValue: shift.end ?? "")
        _secondaryStart = State(initialValue: shift.secondaryStart ?? "")
        _secondaryEnd = State(initialValue: shift.secondaryEnd ?? "")
    }

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [RColors.bg2, RColors.bg, .black],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            ScrollView {
                VStack(alignment: .leading, spacing: 14) {
                    Capsule()
                        .fill(RColors.muted.opacity(0.5))
                        .frame(width: 54, height: 5)
                        .frame(maxWidth: .infinity)

                    HStack {
                        VStack(alignment: .leading, spacing: 3) {
                            Text("Prilagodi " + shift.code)
                                .font(.system(size: 28, weight: .black))
                                .foregroundStyle(RColors.text)
                            Text(shift.name)
                                .font(.subheadline)
                                .foregroundStyle(RColors.muted)
                        }
                        Spacer()
                        preview
                    }

                    appearanceCard
                    scheduleCard

                    if let error {
                        Text(error)
                            .font(.caption)
                            .foregroundStyle(Color(hex: 0xFF6778))
                    }

                    HStack(spacing: 10) {
                        Button("Vrati zadano") {
                            shifts.resetBuiltIn(code: shift.code)
                            dismiss()
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 54)
                        .background(RColors.card2)
                        .foregroundStyle(RColors.text)
                        .clipShape(RoundedRectangle(cornerRadius: 17))

                        Button("Spremi") {
                            save()
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 54)
                        .background(RColors.accent)
                        .foregroundStyle(.black)
                        .fontWeight(.black)
                        .clipShape(RoundedRectangle(cornerRadius: 17))
                        .shadow(color: RColors.accent.opacity(0.35), radius: 9, y: 3)
                    }
                }
                .padding(18)
            }
        }
        .presentationDetents([.large])
        .presentationDragIndicator(.hidden)
    }

    private var preview: some View {
        Text(shift.code)
            .font(.system(size: 20, weight: .black))
            .foregroundStyle(Color(hex: foregroundHex))
            .frame(width: 58, height: 58)
            .background(Color(hex: backgroundHex))
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(Color(hex: backgroundHex).opacity(0.9), lineWidth: 1)
            )
            .shadow(color: Color(hex: backgroundHex).opacity(0.35), radius: 9, y: 3)
    }

    private var appearanceCard: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Izgled")
                .font(.headline.bold())
                .foregroundStyle(RColors.text)

            palette("Boja kockice", values: backgrounds, selected: $backgroundHex)
            palette("Boja teksta", values: foregrounds, selected: $foregroundHex)
        }
        .padding(14)
        .background(RColors.card)
        .clipShape(RoundedRectangle(cornerRadius: 20))
        .overlay(RoundedRectangle(cornerRadius: 20).stroke(RColors.stroke, lineWidth: 1))
        .shadow(color: .black.opacity(0.22), radius: 8, y: 3)
    }

    private var scheduleCard: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("Radno vrijeme")
                .font(.headline.bold())
                .foregroundStyle(RColors.text)

            if isPaidAbsence {
                Text("GO i BO su plaćene odsutnosti i u obračunu priznaju 8 sati na radni dan.")
                    .font(.caption)
                    .foregroundStyle(RColors.muted)
            } else {
                timePair(
                    "Prvi interval",
                    start: $start,
                    end: $end
                )
                timePair(
                    "Drugi interval (neobavezno)",
                    start: $secondaryStart,
                    end: $secondaryEnd
                )
                Text("Koristite format HH:mm. Smjene preko ponoći podržane su automatski.")
                    .font(.caption2)
                    .foregroundStyle(RColors.muted)
            }
        }
        .padding(14)
        .background(RColors.card)
        .clipShape(RoundedRectangle(cornerRadius: 20))
        .overlay(RoundedRectangle(cornerRadius: 20).stroke(RColors.stroke, lineWidth: 1))
        .shadow(color: .black.opacity(0.22), radius: 8, y: 3)
    }

    private func palette(
        _ title: String,
        values: [UInt32],
        selected: Binding<UInt32>
    ) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.subheadline.bold())
                .foregroundStyle(RColors.text)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 9) {
                    ForEach(values, id: \.self) { value in
                        Button {
                            selected.wrappedValue = value
                        } label: {
                            RoundedRectangle(cornerRadius: 11)
                                .fill(Color(hex: value))
                                .frame(width: 46, height: 46)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 11)
                                        .stroke(
                                            selected.wrappedValue == value
                                                ? RColors.accent
                                                : RColors.stroke.opacity(0.5),
                                            lineWidth: selected.wrappedValue == value ? 2 : 1
                                        )
                                )
                                .shadow(
                                    color: selected.wrappedValue == value
                                        ? RColors.accent.opacity(0.32)
                                        : .clear,
                                    radius: 7,
                                    y: 2
                                )
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
        }
    }

    private func timePair(
        _ title: String,
        start: Binding<String>,
        end: Binding<String>
    ) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(title)
                .font(.caption)
                .foregroundStyle(RColors.muted)

            HStack(spacing: 8) {
                timeField("Početak", text: start, placeholder: "07:00")
                timeField("Završetak", text: end, placeholder: "15:00")
            }
        }
    }

    private func timeField(
        _ title: String,
        text: Binding<String>,
        placeholder: String
    ) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(title)
                .font(.caption2)
                .foregroundStyle(RColors.muted)
            TextField(placeholder, text: text)
                .textFieldStyle(.plain)
                .keyboardType(.numbersAndPunctuation)
                .foregroundStyle(RColors.text)
                .padding(12)
                .background(RColors.card2)
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(RColors.stroke.opacity(0.8), lineWidth: 1)
                )
        }
    }

    private func save() {
        do {
            _ = try shifts.updateBuiltIn(
                code: shift.code,
                backgroundHex: backgroundHex,
                foregroundHex: foregroundHex,
                start: start,
                end: end,
                secondaryStart: secondaryStart,
                secondaryEnd: secondaryEnd
            )
            dismiss()
        } catch {
            self.error = error.localizedDescription
        }
    }
}
