import Foundation
import SwiftUI
import UniformTypeIdentifiers

enum ScheduleBackupIOS {
    static let identifier = "raspored-schedule-backup"
    static let maximumBytes = 2_000_000

    struct Result {
        let addedDates: Int
        let importedCustom: Int
        let restoredBuiltIns: Int
    }

    @MainActor
    static func encode(schedule: ScheduleStoreIOS, shifts: ShiftLibraryIOS) throws -> Data {
        let custom = shifts.all.filter { $0.custom }.map { s -> [String: Any] in
            var record: [String: Any] = [
                "code": s.code, "name": s.name,
                "background": Int32(bitPattern: 0xFF000000 | (s.backgroundHex & 0x00FFFFFF)),
                "foreground": Int32(bitPattern: 0xFF000000 | (s.foregroundHex & 0x00FFFFFF)),
                "fontSize": s.fontSize
            ]
            for (key, value) in [
                ("start", s.start),
                ("end", s.end),
                ("secondaryStart", s.secondaryStart),
                ("secondaryEnd", s.secondaryEnd)
            ] {
                if let value { record[key] = value }
            }
            return record
        }
        let builtIns = shifts.all.filter { !$0.custom }.map { s -> [String: Any] in
            var record: [String: Any] = [
                "code": s.code,
                "background": Int32(bitPattern: 0xFF000000 | (s.backgroundHex & 0x00FFFFFF)),
                "foreground": Int32(bitPattern: 0xFF000000 | (s.foregroundHex & 0x00FFFFFF))
            ]
            // Add optional hours without changing the existing Android/iOS v1 schema.
            if let start = s.start, let end = s.end {
                record["start"] = start
                record["end"] = end
            }
            return record
        }
        let root: [String: Any] = [
            "format": identifier,
            "version": 1,
            "dates": schedule.entries,
            "customShifts": custom,
            "builtInColors": builtIns
        ]
        return try JSONSerialization.data(withJSONObject: root, options: [.prettyPrinted, .sortedKeys])
    }

    @MainActor
    static func restore(
        data: Data,
        schedule: ScheduleStoreIOS,
        shifts: ShiftLibraryIOS
    ) throws -> Result {
        guard data.count <= maximumBytes,
              let root = try JSONSerialization.jsonObject(with: data) as? [String: Any],
              root["format"] as? String == identifier,
              root["version"] as? Int == 1,
              let dates = root["dates"] as? [String: String],
              dates.count <= 25_000
        else { throw BackupError.invalidBackup }

        let custom = root["customShifts"] as? [[String: Any]] ?? []
        guard custom.count <= 100 else { throw BackupError.invalidBackup }
        var codes = Set(shifts.all.map(\.code))
        var encounteredImported = Set<String>()
        let timePattern = #"^(?:[01]\d|2[0-3]):[0-5]\d$"#
        for record in custom {
            guard let code = record["code"] as? String,
                  (1...4).contains(code.count),
                  code.range(of: #"^[A-Z0-9]{1,4}$"#, options: .regularExpression) != nil,
                  ShiftCatalogIOS.byCode(code) == nil,
                  encounteredImported.insert(code).inserted,
                  let name = record["name"] as? String,
                  (1...100).contains(name.count)
            else { throw BackupError.invalidBackup }
            codes.insert(code)
            for field in ["start", "end", "secondaryStart", "secondaryEnd"] {
                if let value = record[field] as? String,
                   value.range(of: timePattern, options: .regularExpression) == nil {
                    throw BackupError.invalidBackup
                }
            }
        }
        let formatter = DateFormatter()
        formatter.calendar = Calendar.raspored
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.dateFormat = "yyyy-MM-dd"
        formatter.isLenient = false
        for (day, code) in dates {
            guard let date = formatter.date(from: day),
                  formatter.string(from: date) == day,
                  (1900...2200).contains(Calendar.raspored.component(.year, from: date)),
                  codes.contains(code)
            else { throw BackupError.invalidBackup }
        }

        // Reject malformed hours/unknown codes before importing any new definitions.
        let builtIns = root["builtInColors"] as? [[String: Any]] ?? []
        guard builtIns.count <= ShiftCatalogIOS.all.count else {
            throw BackupError.invalidBackup
        }
        var encounteredBuiltIns = Set<String>()
        var verifiedBuiltIns: [(code: String, background: UInt32, foreground: UInt32, start: String?, end: String?)] = []
        for item in builtIns {
            guard let code = item["code"] as? String,
                  ShiftCatalogIOS.byCode(code) != nil,
                  encounteredBuiltIns.insert(code).inserted,
                  let background = item["background"] as? NSNumber,
                  let foreground = item["foreground"] as? NSNumber
            else { throw BackupError.invalidBackup }

            for field in ["start", "end"] {
                if let value = item[field], !(value is NSNull), !(value is String) {
                    throw BackupError.invalidBackup
                }
            }
            let start = item["start"] as? String
            let end = item["end"] as? String
            guard ShiftBackupTimeRulesIOS.valid(code: code, start: start, end: end) else {
                throw BackupError.invalidBackup
            }
            verifiedBuiltIns.append((
                code: code,
                background: UInt32(truncatingIfNeeded: background.int64Value),
                foreground: UInt32(truncatingIfNeeded: foreground.int64Value),
                start: start, end: end
            ))
        }

        var imported = 0
        let novelShifts = custom.filter { item in
            guard let code = item["code"] as? String else { return false }
            return shifts.byCode(code) == nil
        }
        if !novelShifts.isEmpty {
            let encoded = try JSONSerialization.data(withJSONObject: novelShifts)
            guard let raw = String(data: encoded, encoding: .utf8) else {
                throw BackupError.invalidBackup
            }
            imported = try shifts.importJSON(raw)
        }

        for entry in verifiedBuiltIns {
            _ = try shifts.updateBuiltIn(
                code: entry.code,
                backgroundHex: entry.background,
                foregroundHex: entry.foreground,
                start: entry.start,
                end: entry.end
            )
        }
        return Result(
            addedDates: schedule.mergeMissing(dates),
            importedCustom: imported,
            restoredBuiltIns: verifiedBuiltIns.count
        )
    }

    enum BackupError: LocalizedError {
        case invalidBackup
        var errorDescription: String? {
            "Neispravna, prevelika ili nepodržana sigurnosna kopija."
        }
    }
}

struct ScheduleBackupDocument: FileDocument {
    static var readableContentTypes: [UTType] { [.json] }
    var data: Data

    init(data: Data) { self.data = data }

    init(configuration: ReadConfiguration) throws {
        guard let data = configuration.file.regularFileContents else {
            throw ScheduleBackupIOS.BackupError.invalidBackup
        }
        self.data = data
    }

    func fileWrapper(configuration: WriteConfiguration) throws -> FileWrapper {
        FileWrapper(regularFileWithContents: data)
    }
}
