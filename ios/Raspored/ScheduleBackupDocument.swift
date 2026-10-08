import Foundation
import SwiftUI
import UniformTypeIdentifiers

enum ScheduleBackupIOS {
    static let identifier = "raspored-schedule-backup"
    static let maximumBytes = 2_000_000

    struct Result {
        let addedDates: Int
        let importedCustom: Int
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
            [
                "code": s.code,
                "background": Int32(bitPattern: 0xFF000000 | (s.backgroundHex & 0x00FFFFFF)),
                "foreground": Int32(bitPattern: 0xFF000000 | (s.foregroundHex & 0x00FFFFFF))
            ]
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

        let builtIns = root["builtInColors"] as? [[String: Any]] ?? []
        if builtIns.count <= ShiftCatalogIOS.all.count {
            for item in builtIns {
                guard let code = item["code"] as? String,
                      ShiftCatalogIOS.byCode(code) != nil,
                      let background = item["background"] as? NSNumber,
                      let foreground = item["foreground"] as? NSNumber
                else { continue }
                _ = try shifts.updateBuiltIn(
                    code: code,
                    backgroundHex: UInt32(truncatingIfNeeded: background.int64Value),
                    foregroundHex: UInt32(truncatingIfNeeded: foreground.int64Value)
                )
            }
        }
        return Result(addedDates: schedule.mergeMissing(dates), importedCustom: imported)
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
