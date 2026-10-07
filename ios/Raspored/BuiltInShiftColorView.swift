import SwiftUI

struct BuiltInShiftColorView: View {
    @EnvironmentObject private var shifts: ShiftLibraryIOS
    @Environment(\.dismiss) private var dismiss

    let shift: ShiftTypeDef

    @State private var backgroundHex: UInt32
    @State private var foregroundHex: UInt32

    private let backgrounds: [UInt32] = [
        0xFFD21F, 0x13B7F3, 0x6CEB82, 0x77DED7,
        0xD991EE, 0xFF5BAA, 0xFF5F67, 0x36485A
    ]

    private let foregrounds: [UInt32] = [
        0xFFFFFF, 0xD8D8D8, 0xAAAAAA, 0x777777,
        0x444444, 0x06131F, 0x000000
    ]

    init(shift: ShiftTypeDef) {
        self.shift = shift
        _backgroundHex = State(initialValue: shift.backgroundHex)
        _foregroundHex = State(initialValue: shift.foregroundHex)
    }

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [RColors.bg2, RColors.bg, .black],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            VStack(alignment: .leading, spacing: 16) {
                Capsule()
                    .fill(RColors.muted.opacity(0.5))
                    .frame(width: 54, height: 5)
                    .frame(maxWidth: .infinity)

                Text("Boje — \(shift.name)")
                    .font(.system(size: 27, weight: .black))
                    .foregroundStyle(RColors.text)

                palette("Boja kockice", values: backgrounds, selected: $backgroundHex)
                palette("Boja teksta", values: foregrounds, selected: $foregroundHex)

                Spacer()

                HStack(spacing: 10) {
                    Button("Vrati zadano") {
                        shifts.resetBuiltInColors(code: shift.code)
                        dismiss()
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 52)
                    .background(RColors.card2)
                    .foregroundStyle(RColors.text)
                    .clipShape(RoundedRectangle(cornerRadius: 17))

                    Button("Spremi") {
                        shifts.updateBuiltInColors(
                            code: shift.code,
                            backgroundHex: backgroundHex,
                            foregroundHex: foregroundHex
                        )
                        dismiss()
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 52)
                    .background(RColors.accent)
                    .foregroundStyle(.black)
                    .fontWeight(.black)
                    .clipShape(RoundedRectangle(cornerRadius: 17))
                    .shadow(color: RColors.accent.opacity(0.35), radius: 9, y: 3)
                }
            }
            .padding(18)
        }
        .presentationDetents([.medium])
        .presentationDragIndicator(.hidden)
    }

    private func palette(
        _ title: String,
        values: [UInt32],
        selected: Binding<UInt32>
    ) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            Text(title)
                .fontWeight(.bold)
                .foregroundStyle(RColors.text)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 9) {
                    ForEach(values, id: \.self) { value in
                        Button { selected.wrappedValue = value } label: {
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
        .padding(14)
        .background(RColors.card)
        .clipShape(RoundedRectangle(cornerRadius: 20))
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(RColors.stroke, lineWidth: 1)
        )
    }
}
