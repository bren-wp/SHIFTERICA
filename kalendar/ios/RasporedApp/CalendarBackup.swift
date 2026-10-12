import Foundation
import SwiftUI
import UniformTypeIdentifiers

private struct CalendarBackupPayload: Codable {
    let format: String
    let version: Int
    let entries: [CalendarEntry]
}

enum CalendarBackupError: LocalizedError {
    case invalidFormat, invalidEntries, oversized

    var errorDescription: String? {
        switch self {
        case .invalidFormat: return "Datoteka nije sigurnosna kopija aplikacije Raspored_app."
        case .invalidEntries: return "Sigurnosna kopija sadrži neispravne ili ponovljene zapise."
        case .oversized: return "Sigurnosna kopija je prevelika."
        }
    }
}

enum CalendarBackup {
    static let maxBytes = 5_000_000
    static let maxEntries = 15_000
    private static let format = "raspored-universal-calendar"

    static func encode(_ entries: [CalendarEntry]) throws -> Data {
        guard entries.count <= maxEntries, entries.allSatisfy(CalendarRules.valid) else {
            throw CalendarBackupError.invalidEntries
        }
        let sorted = entries.sorted {
            if $0.date != $1.date { return $0.date < $1.date }
            if $0.start != $1.start { return $0.start < $1.start }
            if $0.title != $1.title { return $0.title < $1.title }
            return $0.id < $1.id
        }
        let encoder = JSONEncoder()
        encoder.outputFormatting = [.prettyPrinted, .sortedKeys]
        let data = try encoder.encode(CalendarBackupPayload(
            format: format, version: 1, entries: sorted
        ))
        guard data.count <= maxBytes else { throw CalendarBackupError.oversized }
        return data
    }

    static func decode(_ data: Data) throws -> [CalendarEntry] {
        guard !data.isEmpty, data.count <= maxBytes else {
            throw CalendarBackupError.oversized
        }
        let payload = try JSONDecoder().decode(CalendarBackupPayload.self, from: data)
        guard payload.version == 1, payload.format == format else {
            throw CalendarBackupError.invalidFormat
        }
        guard payload.entries.count <= maxEntries else {
            throw CalendarBackupError.oversized
        }
        var seen = Set<String>()
        guard payload.entries.allSatisfy({
            CalendarRules.valid($0) && seen.insert($0.id).inserted
        }) else {
            throw CalendarBackupError.invalidEntries
        }
        return payload.entries
    }
}

/// Used solely for an explicit user-initiated export via the system Files picker.
struct CalendarBackupDocument: FileDocument {
    static let readableContentTypes: [UTType] = [.json]
    var data: Data

    init(data: Data) { self.data = data }

    init(configuration: ReadConfiguration) throws {
        guard let data = configuration.file.regularFileContents else {
            throw CalendarBackupError.invalidFormat
        }
        self.data = data
    }

    func fileWrapper(configuration: WriteConfiguration) throws -> FileWrapper {
        FileWrapper(regularFileWithContents: data)
    }
}
