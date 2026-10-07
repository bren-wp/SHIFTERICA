import SwiftUI

extension MonthView {
    private var quickCodes: [String] { ["N", "D", "GO", "J", "BO"] }

    var compactShiftToolbar: some View {
        HStack(spacing: 6) {
            shiftToolButton(
                selected: erasing,
                background: RColors.card2,
                border: erasing ? RColors.accent : RColors.stroke
            ) {
                erasing = true
            } content: {
                Image(systemName: "eraser.fill")
                    .font(.system(size: 17, weight: .bold))
                    .foregroundStyle(erasing ? RColors.accent : RColors.text)
            }

            ForEach(quickCodes, id: \.self) { code in
                if let shift = shifts.byCode(code) {
                    shiftToolButton(
                        selected: !erasing && selectedCode == shift.code,
                        background: shift.color,
                        border: !erasing && selectedCode == shift.code
                            ? RColors.accent
                            : shift.color.opacity(0.85)
                    ) {
                        selectedCode = shift.code
                        erasing = false
                    } content: {
                        Text(shift.code)
                            .font(.system(
                                size: shift.code.count > 1 ? 12 : 17,
                                weight: .black
                            ))
                            .foregroundStyle(shift.textColor)
                    }
                }
            }

            Rectangle()
                .fill(RColors.stroke)
                .frame(width: 1, height: 32)

            shiftToolButton(
                selected: false,
                background: RColors.card2,
                border: RColors.stroke
            ) {
                onOpenShifts()
            } content: {
                Image(systemName: "ellipsis")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundStyle(RColors.text)
            }
        }
        .padding(6)
        .background(RColors.card)
        .clipShape(RoundedRectangle(cornerRadius: 21))
        .overlay(
            RoundedRectangle(cornerRadius: 21)
                .stroke(RColors.stroke, lineWidth: 1)
        )
        .shadow(color: .black.opacity(0.28), radius: 9, y: 3)
    }

    func shiftToolButton<Content: View>(
        selected: Bool,
        background: Color,
        border: Color,
        action: @escaping () -> Void,
        @ViewBuilder content: () -> Content
    ) -> some View {
        Button(action: action) {
            content()
                .frame(maxWidth: .infinity)
                .frame(height: 46)
                .background(background)
                .clipShape(RoundedRectangle(cornerRadius: 13))
                .overlay(
                    RoundedRectangle(cornerRadius: 13)
                        .stroke(border, lineWidth: selected ? 2 : 1)
                )
                .shadow(
                    color: selected ? RColors.accent.opacity(0.35) : .black.opacity(0.14),
                    radius: selected ? 7 : 2,
                    y: 2
                )
        }
        .buttonStyle(.plain)
    }
}
