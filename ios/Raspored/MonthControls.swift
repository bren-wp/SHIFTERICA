import SwiftUI

extension MonthView {
    private var quickCodes: [String] { ["N", "D", "J", "P", "GO", "BO"] }

    @ViewBuilder
    var compactShiftToolbar: some View {
        if editing {
            HStack(spacing: 4) {
                shiftToolButton(
                    selected: erasing,
                    background: RColors.card2,
                    border: erasing ? RColors.accent : RColors.stroke
                ) {
                    erasing = true
                } content: {
                    Image(systemName: "eraser.fill")
                        .font(.system(size: 16, weight: .bold))
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
                                    size: shift.code.count > 1 ? 10 : 15,
                                    weight: .black
                                ))
                                .lineLimit(1)
                                .foregroundStyle(shift.textColor)
                        }
                    }
                }

                Rectangle()
                    .fill(RColors.stroke)
                    .frame(width: 1, height: 28)

                shiftToolButton(
                    selected: false,
                    background: RColors.card2,
                    border: RColors.accent.opacity(0.72)
                ) {
                    editing = false
                    erasing = false
                } content: {
                    Image(systemName: "checkmark")
                        .font(.system(size: 17, weight: .black))
                        .foregroundStyle(RColors.accent)
                }
            }
            .padding(5)
            .background(RColors.card)
            .clipShape(RoundedRectangle(cornerRadius: 18))
            .overlay(
                RoundedRectangle(cornerRadius: 18)
                    .stroke(RColors.accent.opacity(0.56), lineWidth: 1)
            )
            .shadow(color: .black.opacity(0.30), radius: 9, y: 3)
        } else {
            HStack(spacing: 6) {
                Button {
                    editing = true
                } label: {
                    HStack(spacing: 10) {
                        Image(systemName: "pencil")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundStyle(RColors.accent)
                        Text("UREDI RASPORED")
                            .font(.system(size: 14, weight: .black))
                            .foregroundStyle(RColors.text)
                        Spacer(minLength: 0)
                    }
                    .padding(.horizontal, 15)
                    .frame(maxWidth: .infinity)
                    .frame(height: 50)
                    .background(RColors.card)
                    .clipShape(RoundedRectangle(cornerRadius: 18))
                    .overlay(
                        RoundedRectangle(cornerRadius: 18)
                            .stroke(RColors.accent.opacity(0.82), lineWidth: 1.2)
                    )
                }
                .buttonStyle(.plain)

                Button(action: onOpenShifts) {
                    Image(systemName: "ellipsis")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundStyle(RColors.text)
                        .frame(width: 50, height: 50)
                        .background(RColors.card)
                        .clipShape(RoundedRectangle(cornerRadius: 18))
                        .overlay(
                            RoundedRectangle(cornerRadius: 18)
                                .stroke(RColors.stroke, lineWidth: 1)
                        )
                }
                .buttonStyle(.plain)
            }
            .shadow(color: .black.opacity(0.22), radius: 7, y: 3)
        }
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
                .frame(height: 44)
                .background(background)
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(border, lineWidth: selected ? 2 : 1)
                )
                .shadow(
                    color: selected
                        ? RColors.accent.opacity(0.35)
                        : .black.opacity(0.12),
                    radius: selected ? 6 : 1,
                    y: 2
                )
        }
        .buttonStyle(.plain)
    }
}
