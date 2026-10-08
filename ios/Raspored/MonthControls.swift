import SwiftUI

extension MonthView {
    var monthManageBar: some View {
        Button(action: onOpenShifts) {
            HStack(spacing: 8) {
                Image(systemName: "ellipsis")
                    .font(.system(size: 17, weight: .bold))
                    .foregroundStyle(RColors.accent)
                Text("SMJENE I POSTAVKE")
                    .font(.system(size: 13, weight: .black))
                    .foregroundStyle(RColors.text)
            }
            .frame(maxWidth: .infinity)
            .frame(height: 46)
            .background(RColors.card)
            .clipShape(RoundedRectangle(cornerRadius: 17))
            .overlay(
                RoundedRectangle(cornerRadius: 17)
                    .stroke(RColors.stroke.opacity(0.88), lineWidth: 1)
            )
            .shadow(color: .black.opacity(0.18), radius: 4, y: 2)
        }
        .buttonStyle(.plain)
    }
}

struct DayShiftPickerSheetIOS: View {
    @EnvironmentObject private var shifts: ShiftLibraryIOS
    @Environment(\.dismiss) private var dismiss

    let date: Date
    let currentCode: String?
    let onSelect: (String?) -> Void
    let onOpenShifts: () -> Void

    private let quickCodes = ["N", "D", "J", "P", "GO", "BO"]

    private var orderedShifts: [ShiftTypeDef] {
        let preferred = quickCodes.compactMap { shifts.byCode($0) }
        let custom = shifts.all.filter { !quickCodes.contains($0.code) }
        return preferred + custom
    }

    private var dateTitle: String {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "hr_HR")
        formatter.dateFormat = "EEEE, d. MMMM yyyy."
        return formatter.string(from: date).capitalized(with: formatter.locale)
    }

    private let columns = [
        GridItem(.flexible(), spacing: 8),
        GridItem(.flexible(), spacing: 8),
        GridItem(.flexible(), spacing: 8)
    ]

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
                    HStack {
                        VStack(alignment: .leading, spacing: 3) {
                            Text("Odaberi smjenu")
                                .font(.system(size: 27, weight: .black))
                                .foregroundStyle(RColors.text)
                            Text(dateTitle)
                                .font(.caption)
                                .foregroundStyle(RColors.muted)
                        }
                        Spacer()
                        Button { dismiss() } label: {
                            Image(systemName: "xmark")
                                .font(.headline.bold())
                                .foregroundStyle(RColors.text)
                                .frame(width: 46, height: 46)
                                .background(RColors.card2)
                                .clipShape(Circle())
                                .overlay(
                                    Circle()
                                        .stroke(RColors.stroke.opacity(0.72), lineWidth: 1)
                                )
                        }
                        .buttonStyle(.plain)
                    }

                    Text(
                        currentCode == nil
                            ? "Trenutačno nema smjene za ovaj datum."
                            : "Trenutačno: \(currentCode!)"
                    )
                    .font(.system(size: 11, weight: .semibold))
                    .foregroundStyle(currentCode == nil ? RColors.muted : RColors.accent)

                    LazyVGrid(columns: columns, spacing: 8) {
                        ForEach(orderedShifts) { shift in
                            shiftButton(shift)
                        }
                    }

                    HStack(spacing: 8) {
                        Button {
                            onSelect(nil)
                            dismiss()
                        } label: {
                            HStack(spacing: 7) {
                                Image(systemName: "eraser.fill")
                                    .foregroundStyle(Color(hex: 0xFF6778))
                                Text("Obriši")
                                    .fontWeight(.bold)
                                    .foregroundStyle(RColors.text)
                            }
                            .frame(maxWidth: .infinity)
                            .frame(height: 52)
                            .background(RColors.card2)
                            .clipShape(RoundedRectangle(cornerRadius: 17))
                            .overlay(
                                RoundedRectangle(cornerRadius: 17)
                                    .stroke(Color(hex: 0xFF6778).opacity(0.58), lineWidth: 1)
                            )
                        }
                        .buttonStyle(.plain)

                        Button {
                            dismiss()
                            onOpenShifts()
                        } label: {
                            Text("Sve smjene")
                                .fontWeight(.black)
                                .foregroundStyle(RColors.text)
                                .frame(maxWidth: .infinity)
                                .frame(height: 52)
                                .background(RColors.accent.opacity(0.20))
                                .clipShape(RoundedRectangle(cornerRadius: 17))
                                .overlay(
                                    RoundedRectangle(cornerRadius: 17)
                                        .stroke(RColors.accent, lineWidth: 1.2)
                                )
                        }
                        .buttonStyle(.plain)
                    }

                    Text("Odabirom smjene ona se odmah sprema za odabrani datum.")
                        .font(.system(size: 10))
                        .foregroundStyle(RColors.muted)
                        .frame(maxWidth: .infinity)
                        .multilineTextAlignment(.center)
                }
                .padding(18)
            }
        }
        .presentationDetents([.medium, .large])
        .presentationDragIndicator(.visible)
    }

    private func shiftButton(_ shift: ShiftTypeDef) -> some View {
        Button {
            onSelect(shift.code)
            dismiss()
        } label: {
            VStack(spacing: 2) {
                Text(shift.code)
                    .font(.system(
                        size: shift.code.count == 1 ? 21 : 15,
                        weight: .black
                    ))
                Text(shift.shortName)
                    .font(.system(size: 9))
                    .lineLimit(1)
                    .opacity(0.76)
            }
            .foregroundStyle(shift.textColor)
            .frame(maxWidth: .infinity)
            .frame(height: 72)
            .background(shift.color)
            .clipShape(RoundedRectangle(cornerRadius: 18))
            .overlay(
                RoundedRectangle(cornerRadius: 18)
                    .stroke(
                        currentCode == shift.code
                            ? RColors.accent
                            : shift.color.opacity(0.92),
                        lineWidth: currentCode == shift.code ? 2.5 : 1
                    )
            )
            .shadow(
                color: currentCode == shift.code
                    ? RColors.accent.opacity(0.34)
                    : shift.color.opacity(0.20),
                radius: currentCode == shift.code ? 9 : 4,
                y: 2
            )
        }
        .buttonStyle(.plain)
    }
}
