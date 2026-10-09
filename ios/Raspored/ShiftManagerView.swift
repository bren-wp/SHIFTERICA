import SwiftUI

struct ShiftManagerView: View {
    @EnvironmentObject private var shifts: ShiftLibraryIOS
    @EnvironmentObject private var schedule: ScheduleStoreIOS
    @Environment(\.dismiss) private var dismiss
    let onNew: () -> Void
    let onEditCustom: (ShiftTypeDef) -> Void
    let onShowAssignedDate: (Date) -> Void
    @State private var editingBuiltIn: ShiftTypeDef?
    @State private var pendingDeletion: ShiftTypeDef?

    var body: some View {
        ZStack {
            LinearGradient(colors: [RColors.bg2, RColors.bg, .black], startPoint: .top, endPoint: .bottom).ignoresSafeArea()
            ScrollView {
                VStack(alignment: .leading, spacing: 12) {
                    Capsule().fill(RColors.muted.opacity(0.5)).frame(width: 54, height: 5).frame(maxWidth: .infinity)
                    HStack {
                        VStack(alignment: .leading, spacing: 3) {
                            Text("Smjene")
                                .font(.system(size: 31, weight: .black))
                                .foregroundStyle(RColors.text)
                            Text("Dodirnite smjenu za promjenu boje i vremena. Zadano možete vratiti.")
                                .font(.caption)
                                .foregroundStyle(RColors.muted)
                        }
                        Spacer()
                        Button { dismiss() } label: {
                            Image(systemName: "xmark").font(.title2.bold()).foregroundStyle(RColors.text)
                                .frame(width: 48, height: 48)
                                .background(RColors.card2)
                                .clipShape(Circle())
                                .overlay(Circle().stroke(RColors.stroke.opacity(0.72), lineWidth: 1))
                                .shadow(color: .black.opacity(0.28), radius: 6, y: 3)
                        }
                        .buttonStyle(.plain)
                    }
                    action("plus", "Nova smjena", active: true) { onNew() }
                    ForEach(shifts.all) { shift in shiftRow(shift) }
                }
                .padding(18)
            }
        }
        .sheet(item: $editingBuiltIn) { shift in
            BuiltInShiftColorView(shift: shift)
                .environmentObject(shifts)
        }
        .alert(
            "Brisanje smjene",
            isPresented: Binding(
                get: { pendingDeletion != nil },
                set: { if !$0 { pendingDeletion = nil } }
            )
        ) {
            if let shift = pendingDeletion {
                if ShiftDeletionPolicyIOS.canDelete(
                    Array(schedule.entries.values), code: shift.code
                ) {
                    Button("Izbriši smjenu", role: .destructive) {
                        // Recheck when confirming, not only when displaying the alert.
                        if ShiftDeletionPolicyIOS.canDelete(
                            Array(schedule.entries.values), code: shift.code
                        ) {
                            shifts.delete(shift.code, assignedCodes: Array(schedule.entries.values))
                        }
                        pendingDeletion = nil
                    }
                    Button("Odustani", role: .cancel) { pendingDeletion = nil }
                } else {
                    Button("Otvori datum") {
                        if let date = ShiftUsageNavigatorIOS.closestDate(
                            entries: schedule.entries, code: shift.code, today: Date()
                        ) {
                            pendingDeletion = nil
                            onShowAssignedDate(date)
                        }
                    }
                    Button("Razumijem", role: .cancel) { pendingDeletion = nil }
                }
            }
        } message: {
            if let shift = pendingDeletion {
                let count = ShiftDeletionPolicyIOS.assignedDates(
                    Array(schedule.entries.values), code: shift.code
                )
                if count > 0 {
                    Text("Smjena \(shift.code) upisana je na \(count) datuma. Najprije promijenite ili uklonite tu smjenu s tih datuma. Postojeći raspored ostaje sačuvan.")
                } else {
                    Text("Trajno ukloniti vlastitu smjenu \(shift.code)? Postojeći datumi ne mijenjaju se.")
                }
            }
        }
    }

    private func shiftRow(_ shift: ShiftTypeDef) -> some View {
        HStack(spacing: 14) {
            Text(shift.code)
                .font(.system(size: 21, weight: .black))
                .foregroundStyle(shift.textColor)
                .frame(width: 62, height: 62)
                .background(
                    LinearGradient(
                        colors: [shift.color, shift.color.opacity(0.80)],
                        startPoint: .top,
                        endPoint: .bottom
                    )
                )
                .clipShape(RoundedRectangle(cornerRadius: 17))
                .overlay(RoundedRectangle(cornerRadius: 17).stroke(shift.color.opacity(0.96), lineWidth: 1.2))
                .shadow(color: shift.color.opacity(0.40), radius: 10, y: 3)
            VStack(alignment: .leading, spacing: 3) {
                Text(shift.name).font(.system(size: 19, weight: .bold)).foregroundStyle(RColors.text)
                if let time = shift.timeText { Text(time).foregroundStyle(RColors.muted) }
                if shift.custom {
                    let count = ShiftDeletionPolicyIOS.assignedDates(
                        Array(schedule.entries.values), code: shift.code
                    )
                    if count > 0 {
                        Button {
                            if let date = ShiftUsageNavigatorIOS.closestDate(
                                entries: schedule.entries, code: shift.code, today: Date()
                            ) {
                                onShowAssignedDate(date)
                            }
                        } label: {
                            Text("U rasporedu: \(count) · Otvori datum")
                                .font(.caption2)
                                .foregroundStyle(RColors.accent)
                        }
                        .buttonStyle(.plain)
                        .accessibilityHint("Otvara najbliži datum ove smjene")
                    } else {
                        Text("Vlastita smjena · nije upisana")
                            .font(.caption2).foregroundStyle(RColors.muted)
                    }
                }
            }
            Spacer()
            if shift.custom {
                HStack(spacing: 6) {
                    Button { onEditCustom(shift) } label: {
                        Image(systemName: "slider.horizontal.3")
                            .foregroundStyle(RColors.text)
                            .frame(width: 44, height: 44)
                            .background(RColors.card2)
                            .clipShape(Circle())
                            .overlay(Circle().stroke(RColors.stroke.opacity(0.7), lineWidth: 1))
                    }
                    .buttonStyle(.plain)

                    Button { pendingDeletion = shift } label: {
                        Image(systemName: "trash").foregroundStyle(Color(hex: 0xFF6778))
                            .frame(width: 44, height: 44).background(RColors.card2).clipShape(Circle())
                    }
                    .buttonStyle(.plain)
                }
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
        .overlay(RoundedRectangle(cornerRadius: 21).stroke(RColors.stroke.opacity(0.95), lineWidth: 1))
        .shadow(color: shift.color.opacity(0.18), radius: 10, y: 4)
    }

    private func action(_ icon: String, _ label: String, active: Bool, perform: @escaping () -> Void) -> some View {
        Button(action: perform) {
            HStack {
                Image(systemName: icon)
                Text(label).font(.headline)
            }
            .foregroundStyle(RColors.text)
            .frame(maxWidth: .infinity)
            .frame(height: 64)
            .background(active ? RColors.accent.opacity(0.28) : RColors.card)
            .clipShape(RoundedRectangle(cornerRadius: 18))
            .overlay(RoundedRectangle(cornerRadius: 18).stroke(active ? RColors.accent : RColors.stroke, lineWidth: active ? 1.4 : 1))
            .shadow(color: active ? RColors.accent.opacity(0.40) : .black.opacity(0.18), radius: active ? 11 : 5, y: 3)
        }
        .buttonStyle(.plain)
    }
}
