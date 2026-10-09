import SwiftUI

struct BuiltInShiftColorView: View {
    @EnvironmentObject private var shifts: ShiftLibraryIOS
    @Environment(\.dismiss) private var dismiss

    let shift: ShiftTypeDef

    @State private var backgroundHex: UInt32
    @State private var foregroundHex: UInt32
    @State private var startTime: String
    @State private var endTime: String
    @State private var error: String?

    private let backgrounds: [UInt32] = [
        0xFFD21F, 0x13B7F3, 0xFF8A3D, 0x6CEB82,
        0x77DED7, 0xD991EE, 0xFF5BAA, 0x36485A
    ]

    private let foregrounds: [UInt32] = [
        0xFFFFFF, 0xD8D8D8, 0xAAAAAA, 0x777777,
        0x444444, 0x06131F, 0x000000
    ]

    init(shift: ShiftTypeDef) {
        self.shift = shift
        _backgroundHex = State(initialValue: shift.backgroundHex)
        _foregroundHex = State(initialValue: shift.foregroundHex)
        _startTime = State(initialValue: shift.start ?? "")
        _endTime = State(initialValue: shift.end ?? "")
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
                            Text(
                                shift.timeText ??
                                    "Plaćena odsutnost · 8 h na radni dan"
                            )
                            .font(.caption)
                            .foregroundStyle(RColors.muted)
                        }
                        Spacer()
                        preview
                    }

                    if shift.start != nil && shift.end != nil {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Vrijeme smjene").font(.headline).foregroundStyle(RColors.text)
                            HStack {
                                VStack(alignment: .leading, spacing: 4) {
                                    Text("Početak").font(.caption).foregroundStyle(RColors.muted)
                                    TextField("HH:mm", text: $startTime)
                                        .textInputAutocapitalization(.never)
                                        .keyboardType(.numbersAndPunctuation)
                                }
                                VStack(alignment: .leading, spacing: 4) {
                                    Text("Kraj").font(.caption).foregroundStyle(RColors.muted)
                                    TextField("HH:mm", text: $endTime)
                                        .textInputAutocapitalization(.never)
                                        .keyboardType(.numbersAndPunctuation)
                                }
                            }
                            .textFieldStyle(.roundedBorder)
                            Text("Zadano: \(ShiftCatalogIOS.byCode(shift.code)?.timeText ?? "")")
                                .font(.caption).foregroundStyle(RColors.muted)
                        }
                        .padding(13).background(RColors.card)
                        .clipShape(RoundedRectangle(cornerRadius: 17))
                    }

                    appearanceCard

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
                        .shadow(
                            color: RColors.accent.opacity(0.35),
                            radius: 9,
                            y: 3
                        )
                    }
                }
                .padding(18)
            }
        }
        .presentationDetents([.medium, .large])
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
                    .stroke(
                        Color(hex: backgroundHex).opacity(0.9),
                        lineWidth: 1
                    )
            )
            .shadow(
                color: Color(hex: backgroundHex).opacity(0.35),
                radius: 9,
                y: 3
            )
    }

    private var appearanceCard: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Izgled")
                .font(.headline.bold())
                .foregroundStyle(RColors.text)

            palette(
                "Boja kockice",
                values: backgrounds,
                selected: $backgroundHex
            )
            palette(
                "Boja teksta",
                values: foregrounds,
                selected: $foregroundHex
            )
        }
        .padding(14)
        .background(RColors.card)
        .clipShape(RoundedRectangle(cornerRadius: 20))
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(RColors.stroke, lineWidth: 1)
        )
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
                                            lineWidth:
                                                selected.wrappedValue == value
                                                ? 2
                                                : 1
                                        )
                                )
                                .shadow(
                                    color:
                                        selected.wrappedValue == value
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

    private func save() {
        do {
            _ = try shifts.updateBuiltIn(
                code: shift.code,
                backgroundHex: backgroundHex,
                foregroundHex: foregroundHex,
                start: shift.start == nil ? nil : startTime,
                end: shift.end == nil ? nil : endTime
            )
            dismiss()
        } catch {
            self.error = error.localizedDescription
        }
    }
}
