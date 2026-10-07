import SwiftUI

extension MonthView {
    var legend: some View {
        VStack(alignment: .leading, spacing: 9) {
            HStack {
                Text("Vrste smjena").font(.headline).foregroundStyle(RColors.text)
                Spacer()
                Button(action: onOpenShifts) { Image(systemName: "chevron.right").foregroundStyle(RColors.muted) }
            }
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(shifts.all) { shift in
                        VStack(spacing: 5) {
                            Text(shift.code)
                                .font(.system(size: 15, weight: .black))
                                .foregroundStyle(shift.textColor)
                                .frame(width: 44, height: 40)
                                .background(shift.color)
                                .clipShape(RoundedRectangle(cornerRadius: 10))
                            Text(shift.shortName).font(.caption2).foregroundStyle(RColors.text)
                        }
                        .padding(8)
                        .background(RColors.card2)
                        .clipShape(RoundedRectangle(cornerRadius: 15))
                    }
                }
            }
        }
        .padding(13)
        .background(RColors.card)
        .clipShape(RoundedRectangle(cornerRadius: 24))
        .overlay(RoundedRectangle(cornerRadius: 24).stroke(RColors.stroke, lineWidth: 1))
        .shadow(color: .black.opacity(0.26), radius: 10, y: 4)
    }

    var quickToolbar: some View {
        HStack(spacing: 8) {
            toolIcon("eraser") { editing = true; erasing = true; selectedCode = nil }
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 7) {
                    ForEach(shifts.all) { shift in
                        Button {
                            editing = true
                            selectedCode = shift.code
                            erasing = false
                        } label: {
                            Text(shift.code)
                                .font(.system(size: 16, weight: .black))
                                .foregroundStyle(shift.textColor)
                                .frame(width: 46, height: 46)
                                .background(shift.color)
                                .clipShape(RoundedRectangle(cornerRadius: 13))
                                .shadow(color: shift.color.opacity(0.30), radius: 6, y: 3)
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
            Divider().frame(height: 36).overlay(RColors.stroke)
            toolIcon("ellipsis") { onOpenShifts() }
        }
        .padding(8)
        .background(RColors.card)
        .clipShape(RoundedRectangle(cornerRadius: 23))
        .overlay(RoundedRectangle(cornerRadius: 23).stroke(RColors.stroke, lineWidth: 1))
        .shadow(color: .black.opacity(0.30), radius: 10, y: 4)
    }

    func toolIcon(_ symbol: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Image(systemName: symbol)
                .foregroundStyle(RColors.text)
                .frame(width: 46, height: 46)
                .background(RColors.card2)
                .clipShape(RoundedRectangle(cornerRadius: 13))
        }
        .buttonStyle(.plain)
    }

    var editingDock: some View {
        VStack(spacing: 10) {
            Capsule().fill(RColors.muted.opacity(0.45)).frame(width: 54, height: 5)
            HStack {
                VStack(alignment: .leading) {
                    Text("Način uređivanja").font(.system(size: 22, weight: .black)).foregroundStyle(RColors.text)
                    Text("Dodirnite dan kako biste primijenili smjenu").font(.caption).foregroundStyle(RColors.muted)
                }
                Spacer()
                Button {
                    editing = false; selectedCode = nil; erasing = false
                } label: {
                    Label("Izađi iz uređivanja", systemImage: "rectangle.portrait.and.arrow.forward")
                        .font(.caption.bold())
                        .foregroundStyle(RColors.accent)
                        .padding(11)
                        .overlay(RoundedRectangle(cornerRadius: 13).stroke(RColors.accent, lineWidth: 1))
                }
                .buttonStyle(.plain)
            }
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 10) {
                    editChip(label: "Gumica", code: "", color: RColors.card2, textColor: RColors.text, selected: erasing, icon: "eraser") {
                        erasing = true; selectedCode = nil
                    }
                    ForEach(shifts.all) { shift in
                        editChip(label: shift.shortName, code: shift.code, color: shift.color, textColor: shift.textColor, selected: selectedCode == shift.code, icon: nil) {
                            selectedCode = shift.code; erasing = false
                        }
                    }
                }
            }
        }
        .padding(15)
        .background(RColors.card)
        .clipShape(RoundedRectangle(cornerRadius: 26))
        .overlay(RoundedRectangle(cornerRadius: 26).stroke(RColors.stroke, lineWidth: 1))
        .shadow(color: .black.opacity(0.32), radius: 12, y: 5)
    }

    func editChip(label: String, code: String, color: Color, textColor: Color, selected: Bool, icon: String?, action: @escaping () -> Void) -> some View {
        VStack(spacing: 4) {
            Button(action: action) {
                Group {
                    if let icon { Image(systemName: icon).font(.title2).foregroundStyle(RColors.text) }
                    else { Text(code).font(.system(size: 19, weight: .black)).foregroundStyle(textColor) }
                }
                .frame(width: 62, height: 62)
                .background(color)
                .clipShape(RoundedRectangle(cornerRadius: 17))
                .overlay(RoundedRectangle(cornerRadius: 17).stroke(selected ? RColors.accent : RColors.stroke.opacity(0.5), lineWidth: selected ? 2 : 1))
                .shadow(color: selected ? RColors.accent.opacity(0.35) : color.opacity(0.20), radius: selected ? 8 : 4, y: 3)
            }
            .buttonStyle(.plain)
            Text(label).font(.caption2).foregroundStyle(RColors.text)
        }
    }


}
