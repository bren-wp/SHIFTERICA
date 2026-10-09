import Foundation
import SwiftUI

private struct ImportedShiftRecord: Decodable {
    let code: String
    let name: String
    let start: String?
    let end: String?
    let secondaryStart: String?
    let secondaryEnd: String?
    let background: Int64?
    let foreground: Int64?
    let fontSize: Int?
}

private struct LegacyBuiltInColorRecord: Codable {
    let backgroundHex: UInt32
    let foregroundHex: UInt32
}

private struct BuiltInOverrideRecord: Codable {
    let backgroundHex: UInt32
    let foregroundHex: UInt32
    // Optional keys preserve decoding of color-only overrides from older versions.
    let start: String?
    let end: String?
}

private struct UserShiftRecord: Codable {
    let code: String
    let name: String
    let shortName: String
    let start: String?
    let end: String?
    let secondaryStart: String?
    let secondaryEnd: String?
    let backgroundHex: UInt32
    let foregroundHex: UInt32
    let fontSize: Int

    var definition: ShiftTypeDef {
        ShiftTypeDef(
            code: code,
            name: name,
            shortName: shortName,
            start: start,
            end: end,
            secondaryStart: secondaryStart,
            secondaryEnd: secondaryEnd,
            backgroundHex: backgroundHex,
            foregroundHex: foregroundHex,
            fontSize: fontSize,
            custom: true
        )
    }
}

private struct ShiftIntervalsIOS {
    let start: String?
    let end: String?
    let secondaryStart: String?
    let secondaryEnd: String?
}

@MainActor final class ShiftLibraryIOS: ObservableObject {
    @Published private(set) var custom: [ShiftTypeDef] = []
    @Published private var builtInOverrides: [String: BuiltInOverrideRecord] = [:]
    @Published private(set) var revision: Int = 0

    private let defaults = UserDefaults.standard
    private let customKey = "raspored.premium.customShifts"
    private let builtInOverridesKey = "raspored.builtin.overrides.v2"
    private let legacyBuiltInColorsKey = "raspored.builtin.colors"

    init() {
        loadCustom()
        loadBuiltInOverrides()
    }

    var all: [ShiftTypeDef] {
        ShiftCatalogIOS.all.map { base in
            guard let override = builtInOverrides[base.code] else { return base }
            return ShiftTypeDef(
                code: base.code,
                name: base.name,
                shortName: base.shortName,
                start: override.start ?? base.start,
                end: override.end ?? base.end,
                secondaryStart: base.secondaryStart,
                secondaryEnd: base.secondaryEnd,
                backgroundHex: override.backgroundHex,
                foregroundHex: override.foregroundHex,
                fontSize: base.fontSize,
                custom: false
            )
        } + custom
    }

    func byCode(_ code: String?) -> ShiftTypeDef? {
        all.first { $0.code == code }
    }

    @discardableResult
    func save(
        name: String,
        code: String,
        backgroundHex: UInt32,
        foregroundHex: UInt32,
        fontSize: Int,
        start: String?,
        end: String?,
        secondaryStart: String?,
        secondaryEnd: String?
    ) throws -> ShiftTypeDef {
        let normalized = normalize(code)
        guard (1...4).contains(normalized.count) else {
            throw ShiftLibraryError.invalidCode
        }
        guard !name.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
            throw ShiftLibraryError.emptyName
        }
        guard ShiftCatalogIOS.byCode(normalized) == nil else {
            throw ShiftLibraryError.reservedCode
        }

        let intervals = try normalizeIntervals(
            start: start,
            end: end,
            secondaryStart: secondaryStart,
            secondaryEnd: secondaryEnd
        )
        let cleanName = name.trimmingCharacters(in: .whitespacesAndNewlines)
        let shift = ShiftTypeDef(
            code: normalized,
            name: cleanName,
            shortName: String(cleanName.prefix(14)),
            start: intervals.start,
            end: intervals.end,
            secondaryStart: intervals.secondaryStart,
            secondaryEnd: intervals.secondaryEnd,
            backgroundHex: backgroundHex,
            foregroundHex: foregroundHex,
            fontSize: min(24, max(8, fontSize)),
            custom: true
        )

        if let index = custom.firstIndex(where: { $0.code == normalized }) {
            custom[index] = shift
        } else {
            custom.append(shift)
        }
        persistCustom()
        return shift
    }

    @discardableResult
    func updateBuiltIn(
        code: String,
        backgroundHex: UInt32,
        foregroundHex: UInt32,
        start: String? = nil,
        end: String? = nil
    ) throws -> ShiftTypeDef {
        let normalized = normalize(code)
        guard let base = ShiftCatalogIOS.byCode(normalized) else {
            throw ShiftLibraryError.unknownBuiltIn
        }

        let previous = builtInOverrides[normalized]
        let first = start ?? previous?.start ?? base.start
        let last = end ?? previous?.end ?? base.end
        if base.start != nil {
            guard let first, let last, first != last else {
                throw ShiftLibraryError.incompleteInterval
            }
            _ = try normalizeTime(first)
            _ = try normalizeTime(last)
        }
        builtInOverrides[normalized] = BuiltInOverrideRecord(
            backgroundHex: backgroundHex,
            foregroundHex: foregroundHex,
            start: base.start == nil ? nil : first,
            end: base.end == nil ? nil : last
        )
        persistBuiltInOverrides()
        revision &+= 1

        return ShiftTypeDef(
            code: base.code,
            name: base.name,
            shortName: base.shortName,
            start: base.start == nil ? nil : first,
            end: base.end == nil ? nil : last,
            secondaryStart: base.secondaryStart,
            secondaryEnd: base.secondaryEnd,
            backgroundHex: backgroundHex,
            foregroundHex: foregroundHex,
            fontSize: base.fontSize,
            custom: false
        )
    }

    func resetBuiltIn(code: String) {
        builtInOverrides.removeValue(forKey: normalize(code))
        persistBuiltInOverrides()
        revision &+= 1
    }

    func importJSON(_ raw: String) throws -> Int {
        let source = raw.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !source.isEmpty, let data = source.data(using: .utf8) else {
            throw ShiftLibraryError.invalidImport
        }

        let decoder = JSONDecoder()
        let records: [ImportedShiftRecord]
        if source.hasPrefix("[") {
            records = try decoder.decode([ImportedShiftRecord].self, from: data)
        } else {
            records = [try decoder.decode(ImportedShiftRecord.self, from: data)]
        }

        guard !records.isEmpty else {
            throw ShiftLibraryError.invalidImport
        }

        var imported = 0
        for record in records {
            _ = try save(
                name: record.name,
                code: record.code,
                backgroundHex: UInt32(truncatingIfNeeded: record.background ?? Int64(0xFF13B7F3)),
                foregroundHex: UInt32(truncatingIfNeeded: record.foreground ?? Int64(0xFF06131F)),
                fontSize: record.fontSize ?? 12,
                start: record.start,
                end: record.end,
                secondaryStart: record.secondaryStart,
                secondaryEnd: record.secondaryEnd
            )
            imported += 1
        }
        return imported
    }

    func delete(_ code: String) {
        custom.removeAll { $0.code == normalize(code) }
        persistCustom()
    }

    private func normalizeIntervals(
        start: String?,
        end: String?,
        secondaryStart: String?,
        secondaryEnd: String?
    ) throws -> ShiftIntervalsIOS {
        let primaryStart = try normalizeTime(start)
        let primaryEnd = try normalizeTime(end)
        let secondStart = try normalizeTime(secondaryStart)
        let secondEnd = try normalizeTime(secondaryEnd)

        guard (primaryStart == nil) == (primaryEnd == nil) else {
            throw ShiftLibraryError.incompleteInterval
        }
        guard (secondStart == nil) == (secondEnd == nil) else {
            throw ShiftLibraryError.incompleteSecondaryInterval
        }

        return ShiftIntervalsIOS(
            start: primaryStart,
            end: primaryEnd,
            secondaryStart: secondStart,
            secondaryEnd: secondEnd
        )
    }

    private func normalizeTime(_ value: String?) throws -> String? {
        guard let value else { return nil }
        let trimmed = value.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return nil }

        guard trimmed.range(
            of: #"^(?:[01]\d|2[0-3]):[0-5]\d$"#,
            options: .regularExpression
        ) != nil else {
            throw ShiftLibraryError.invalidTime
        }

        return trimmed
    }

    private func normalize(_ value: String) -> String {
        String(
            value.uppercased(with: Locale(identifier: "hr_HR"))
                .filter { $0.isLetter || $0.isNumber }
                .prefix(4)
        )
    }

    private func loadCustom() {
        guard let data = defaults.data(forKey: customKey),
              let records = try? JSONDecoder().decode([UserShiftRecord].self, from: data) else {
            return
        }
        custom = records.map(\.definition)
    }

    private func loadBuiltInOverrides() {
        if let data = defaults.data(forKey: builtInOverridesKey),
           let decoded = try? JSONDecoder().decode([String: BuiltInOverrideRecord].self, from: data) {
            builtInOverrides = decoded
            return
        }

        migrateLegacyBuiltInColors()
    }

    private func migrateLegacyBuiltInColors() {
        guard let data = defaults.data(forKey: legacyBuiltInColorsKey),
              let legacy = try? JSONDecoder().decode([String: LegacyBuiltInColorRecord].self, from: data) else {
            return
        }

        for (code, colors) in legacy {
            guard let base = ShiftCatalogIOS.byCode(code) else { continue }
            builtInOverrides[code] = BuiltInOverrideRecord(
                backgroundHex: colors.backgroundHex,
                foregroundHex: colors.foregroundHex,
                start: nil,
                end: nil
            )
        }

        persistBuiltInOverrides()
        defaults.removeObject(forKey: legacyBuiltInColorsKey)
    }

    private func persistBuiltInOverrides() {
        if let data = try? JSONEncoder().encode(builtInOverrides) {
            defaults.set(data, forKey: builtInOverridesKey)
        }
    }

    private func persistCustom() {
        let records = custom.map {
            UserShiftRecord(
                code: $0.code,
                name: $0.name,
                shortName: $0.shortName,
                start: $0.start,
                end: $0.end,
                secondaryStart: $0.secondaryStart,
                secondaryEnd: $0.secondaryEnd,
                backgroundHex: $0.backgroundHex,
                foregroundHex: $0.foregroundHex,
                fontSize: $0.fontSize
            )
        }

        if let data = try? JSONEncoder().encode(records) {
            defaults.set(data, forKey: customKey)
        }
    }

    enum ShiftLibraryError: LocalizedError {
        case invalidCode
        case emptyName
        case reservedCode
        case invalidImport
        case invalidTime
        case incompleteInterval
        case incompleteSecondaryInterval
        case unknownBuiltIn

        var errorDescription: String? {
            switch self {
            case .invalidCode:
                return "Skraćenica mora imati od 1 do 4 znaka."
            case .emptyName:
                return "Naziv smjene ne može biti prazan."
            case .reservedCode:
                return "Ta je skraćenica rezervirana za ugrađenu smjenu."
            case .invalidImport:
                return "JSON za uvoz nije ispravan."
            case .invalidTime:
                return "Vrijeme mora biti u formatu HH:mm."
            case .incompleteInterval:
                return "Početak i završetak smjene moraju biti uneseni zajedno."
            case .incompleteSecondaryInterval:
                return "Početak i završetak drugog intervala moraju biti uneseni zajedno."
            case .unknownBuiltIn:
                return "Nepoznata ugrađena smjena."
            }
        }
    }

}
