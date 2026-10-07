import SwiftUI

struct ShiftManagerView: View {
    @EnvironmentObject private var shifts: ShiftLibraryIOS
    @Environment(\.dismiss) private var dismiss
    let onNew: () -> Void
    @State private var showImport = false
    @State private var importText = ""
    @State private var importError: String?
    @State private var importedCount: Int?
    @State private var editingBuiltIn: ShiftTypeDef?

    var body: some View {
        ZStack {
            LinearGradient(colors: [RColors.bg2, RColors.bg, .black], startPoint: .top, endPoint: .bottom).ignoresSafeArea()
            ScrollView {
                VStack(alignment: .leading, spacing: 12) {
                    Capsule().fill(RColors.muted.opacity(0.5)).frame(width: 54, height: 5).frame(maxWidth: .infinity)
                    HStack {
                        Text("Smjene").font(.system(size: 31, weight: .black)).foregroundStyle(RColors.text)
                        Spacer()
                        Button { dismiss() } label: {
                            Image(systemName: "xmark").font(.title2.bold()).foregroundStyle(RColors.text)
                                .frame(width: 48, height: 48).background(RColors.card2).clipShape(Circle())
                        }
                        .buttonStyle(.plain)
                    }
                    HStack(spacing: 10) {
                        action("plus", "Nova smjena", active: true) { onNew() }
                        action("square.and.arrow.down", "Uvezi smjenu", active: false) {
                            importError = nil
                            importedCount = nil
                            showImport = true
                        }
                    }
                    ForEach(shifts.all) { shift in shiftRow(shift) }
                }
                .padding(18)
            }
        }
        .sheet(isPresented: $showImport) {
            ZStack {
                LinearGradient(colors: [RColors.bg2, RColors.bg, .black], startPoint: .top, endPoint: .bottom)
                    .ignoresSafeArea()
                VStack(alignment: .leading, spacing: 14) {
                    Capsule().fill(RColors.muted.opacity(0.5)).frame(width: 54, height: 5).frame(maxWidth: .infinity)
                    HStack {
                        Text("Uvezi smjenu")
                            .font(.system(size: 28, weight: .black))
                            .foregroundStyle(RColors.text)
                        Spacer()
                        Button { showImport = false } label: {
                            Image(systemName: "xmark")
                                .font(.title3.bold())
                                .foregroundStyle(RColors.text)
                                .frame(width: 44, height: 44)
                                .background(RColors.card2)
                                .clipShape(Circle())
                        }
                        .buttonStyle(.plain)
                    }
                    Text("Zalijepite JSON jedne smjene ili popisa smjena.")
                        .font(.subheadline)
                        .foregroundStyle(RColors.muted)
                    TextEditor(text: $importText)
                        .scrollContentBackground(.hidden)
                        .foregroundStyle(RColors.text)
                        .font(.system(.body, design: .monospaced))
                        .padding(10)
                        .frame(minHeight: 180)
                        .background(RColors.card2)
                        .clipShape(RoundedRectangle(cornerRadius: 16))
                        .overlay(RoundedRectangle(cornerRadius: 16).stroke(RColors.stroke, lineWidth: 1))
                    if let importError {
                        Text(importError).font(.caption).foregroundStyle(Color(hex: 0xFF6778))
                    }
                    if let importedCount {
                        Text("Uvezeno smjena: \(importedCount)")
                            .font(.caption.bold())
                            .foregroundStyle(RColors.accent)
                    }
                    HStack(spacing: 10) {
                        Button("Odustani") { showImport = false }
                            .frame(maxWidth: .infinity).frame(height: 54)
                            .background(RColors.card2)
                            .foregroundStyle(RColors.text)
                            .clipShape(RoundedRectangle(cornerRadius: 17))
                        Button("Uvezi") {
                            do {
                                importedCount = try shifts.importJSON(importText)
                                importError = nil
                                importText = ""
                            } catch {
                                importedCount = nil
                                importError = error.localizedDescription
                            }
                        }
                        .frame(maxWidth: .infinity).frame(height: 54)
                        .background(RColors.accent)
                        .foregroundStyle(.black)
                        .fontWeight(.black)
                        .clipShape(RoundedRectangle(cornerRadius: 17))
                    }
                }
                .padding(18)
            }
            .presentationDetents([.medium, .large])
            .presentationDragIndicator(.hidden)
        }
        .sheet(item: $editingBuiltIn) { shift in
            BuiltInShiftColorView(shift: shift)
                .environmentObject(shifts)
        }
    }

    private func shiftRow(_ shift: ShiftTypeDef) -> some View {
        HStack(spacing: 14) {
            Text(shift.code)
                .font(.system(size: 21, weight: .black))
                .foregroundStyle(shift.textColor)
                .frame(width: 62, height: 62)
                .background(shift.color)
                .clipShape(RoundedRectangle(cornerRadius: 16))
                .overlay(RoundedRectangle(cornerRadius: 16).stroke(shift.color.opacity(0.9), lineWidth: 1))
                .shadow(color: shift.color.opacity(0.38), radius: 9, y: 3)
            VStack(alignment: .leading, spacing: 3) {
                Text(shift.name).font(.system(size: 19, weight: .bold)).foregroundStyle(RColors.text)
                if let time = shift.timeText { Text(time).foregroundStyle(RColors.muted) }
                if shift.custom { Text("Vlastita smjena").font(.caption2).foregroundStyle(RColors.accent) }
            }
            Spacer()
            if shift.custom {
                Button { shifts.delete(shift.code) } label: {
                    Image(systemName: "trash").foregroundStyle(Color(hex: 0xFF6778))
                        .frame(width: 44, height: 44).background(RColors.card2).clipShape(Circle())
                }
                .buttonStyle(.plain)
            } else {
                Button { editingBuiltIn = shift } label: {
                    Image(systemName: "paintpalette.fill")
                        .font(.title3.bold())
                        .foregroundStyle(RColors.text)
                        .frame(width: 44, height: 44)
                        .background(RColors.card2)
                        .clipShape(Circle())
                        .overlay(Circle().stroke(RColors.stroke.opacity(0.7), lineWidth: 1))
                }
                .buttonStyle(.plain)
            }
        }
        .padding(14)
        .background(RColors.card)
        .clipShape(RoundedRectangle(cornerRadius: 21))
        .overlay(RoundedRectangle(cornerRadius: 21).stroke(RColors.stroke, lineWidth: 1))
        .shadow(color: shift.color.opacity(0.16), radius: 9, y: 4)
    }

    private func action(_ icon: String, _ label: String, active: Bool, perform: @escaping () -> Void) -> some View {
        Button(action: perform) {
            HStack {
                Image(systemName: icon)
                Text(label).font(.headline)
            }
            .foregroundStyle(RColors.text)
            .frame(maxWidth: .infinity)
            .frame(height: 62)
            .background(active ? RColors.accent.opacity(0.22) : RColors.card)
            .clipShape(RoundedRectangle(cornerRadius: 18))
            .overlay(RoundedRectangle(cornerRadius: 18).stroke(active ? RColors.accent : RColors.stroke, lineWidth: 1))
            .shadow(color: active ? RColors.accent.opacity(0.34) : .black.opacity(0.18), radius: active ? 10 : 5, y: 3)
        }
        .buttonStyle(.plain)
    }
}
