import SwiftUI
import UserNotifications
import UIKit

struct ShiftReminderSettingsViewIOS: View {
    @EnvironmentObject private var settings: UISettingsStoreIOS
    @EnvironmentObject private var schedule: ScheduleStoreIOS
    @State private var notificationsAllowed = false
    @State private var permissionChecked = false
    @State private var permissionDenied = false

    var body: some View {
        VStack(alignment: .leading, spacing: 11) {
            settingToggle(
                "Podsjetnici uključeni",
                subtitle: "Zadano uključeni · potrebna dozvola",
                enabled: $settings.remindersEnabled
            )
            if settings.remindersEnabled {
                settingToggle(
                    "Večer prije · 20:00",
                    subtitle: "Sutra dnevna ili noćna smjena",
                    enabled: $settings.eveningReminderEnabled
                )
                settingToggle(
                    "Prije početka smjene",
                    subtitle: "Sat vremena prije početka D ili N smjene",
                    enabled: $settings.shiftTimeReminderEnabled
                )
                if permissionChecked && !notificationsAllowed {
                    Text("Obavijesti nisu dopuštene. Dopustite ih kako bi podsjetnici radili.")
                        .font(.caption)
                        .foregroundStyle(Color(hex: 0xFFC66B))
                    Button(permissionDenied ? "Otvori postavke obavijesti" : "Dopusti obavijesti") {
                        if permissionDenied {
                            guard let url = URL(string: UIApplication.openSettingsURLString) else { return }
                            UIApplication.shared.open(url)
                        } else {
                            Task {
                                notificationsAllowed =
                                    (try? await UNUserNotificationCenter.current()
                                        .requestAuthorization(options: [.alert, .sound, .badge])) ?? false
                                await ShiftReminderSchedulerIOS.shared.refresh(
                                    entries: schedule.entries,
                                    enabled: settings.remindersEnabled,
                                    evening: settings.eveningReminderEnabled,
                                    departure: settings.shiftTimeReminderEnabled
                                )
                                await refreshPermission()
                            }
                        }
                    }
                    .buttonStyle(.borderedProminent)
                    .tint(RColors.accent)
                }
                Text("Zvučne lokalne obavijesti, a ne neprekidni alarm. " +
                     "Način Ne ometaj i postavke sustava mogu utjecati na dostavu.")
                    .font(.caption)
                    .foregroundStyle(RColors.muted)
            }
        }
        .task { await refreshPermission() }
        .onReceive(NotificationCenter.default.publisher(
            for: UIApplication.willEnterForegroundNotification
        )) { _ in
            Task { await refreshPermission() }
        }
    }

    @MainActor
    private func refreshPermission() async {
        let status = await UNUserNotificationCenter.current().notificationSettings()
        notificationsAllowed = status.authorizationStatus == .authorized ||
            status.authorizationStatus == .provisional
        permissionDenied = status.authorizationStatus == .denied
        permissionChecked = true
    }

    private func settingToggle(
        _ title: String,
        subtitle: String,
        enabled: Binding<Bool>
    ) -> some View {
        Toggle(isOn: enabled) {
            VStack(alignment: .leading, spacing: 3) {
                Text(title)
                    .font(.subheadline.weight(.semibold))
                    .foregroundStyle(RColors.text)
                Text(subtitle)
                    .font(.caption)
                    .foregroundStyle(RColors.muted)
            }
        }
        .tint(RColors.accent)
        .padding(11)
        .background(RColors.card2)
        .clipShape(RoundedRectangle(cornerRadius: 14))
    }
}
