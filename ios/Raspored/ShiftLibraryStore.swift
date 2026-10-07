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

private struct BuiltInColorRecord: Codable {
    let backgroundHex: UInt32
    let foregroundHex: UInt32
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
            code: code, name: name, shortName: shortName,
            start: start, end: end,
            secondaryStart: secondaryStart, secondaryEnd: secondaryEnd,
            backgroundHex: backgroundHex, foregroundHex: foregroundHex,
            fontSize: fontSize, custom: true
        )
    }
}

@MainActor final class ShiftLibraryIOS: ObservableObject {
    @Published private(set) var custom: [ShiftTypeDef] = []
    @Published private var builtInColors: [String: BuiltInColorRecord] = [:]
    private let defaults = UserDefaults.standard
    private let key = "raspored.premium.customShifts"
    private let builtInColorsKey = "raspored.builtin.colors"

    init() {
        load()
        loadBuiltInColors()
    }

    var all: [ShiftTypeDef] {
        ShiftCatalogIOS.all.map { base in
            guard let override = builtInColors[base.code] else { return base }
            return ShiftTypeDef(
                code: base.code,
                name: base.name,
                shortName: base.shortName,
                start: base.start,
                end: base.end,
                secondaryStart: base.secondaryStart,
                secondaryEnd: base.secondaryEnd,
                backgroundHex: override.backgroundHex,
                foregroundHex: override.foregroundHex,
                fontSize: base.fontSize,
                custom: false
            )
        } + custom
    }
    func byCode(_ code: String?) -> ShiftTypeDef? { all.first { $0.code == code } }

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
        guard (1...4).contains(normalized.count) else { throw ShiftLibraryError.invalidCode }
        guard !name.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { throw ShiftLibraryError.emptyName }
        guard ShiftCatalogIOS.byCode(normalized) == nil else { throw ShiftLibraryError.reservedCode }

        let cleanName = name.trimmingCharacters(in: .whitespacesAndNewlines)
        let shift = ShiftTypeDef(
            code: normalized,
            name: cleanName,
            shortName: String(cleanName.prefix(14)),
            start: clean(start), end: clean(end),
            secondaryStart: clean(secondaryStart), secondaryEnd: clean(secondaryEnd),
            backgroundHex: backgroundHex,
            foregroundHex: foregroundHex,
            fontSize: min(24, max(8, fontSize)),
            custom: true
        )
        if let index = custom.firstIndex(where: { $0.code == normalized }) { custom[index] = shift }
        else { custom.append(shift) }
        persist()
        return shift
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

        guard !records.isEmpty else { throw ShiftLibraryError.invalidImport }

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
        persist()
    }

    func updateBuiltInColors(
        code: String,
        backgroundHex: UInt32,
        foregroundHex: UInt32
    ) {
        let normalized = normalize(code)
        guard ShiftCatalogIOS.byCode(normalized) != nil else { return }
        builtInColors[normalized] = BuiltInColorRecord(
            backgroundHex: backgroundHex,
            foregroundHex: foregroundHex
        )
        persistBuiltInColors()
    }

    func resetBuiltInColors(code: String) {
        builtInColors.removeValue(forKey: normalize(code))
        persistBuiltInColors()
    }

    private func normalize(_ value: String) -> String {
        String(value.uppercased(with: Locale(identifier: "hr_HR")).filter { $0.isLetter || $0.isNumber }.prefix(4))
    }

    private func clean(_ value: String?) -> String? {
        guard let value else { return nil }
        let trimmed = value.trimmingCharacters(in: .whitespacesAndNewlines)
        return trimmed.isEmpty ? nil : trimmed
    }

    private func load() {
        guard let data = defaults.data(forKey: key), let records = try? JSONDecoder().decode([UserShiftRecord].self, from: data) else { return }
        custom = records.map(\.definition)
    }

    private func loadBuiltInColors() {
        guard let data = defaults.data(forKey: builtInColorsKey),
              let decoded = try? JSONDecoder().decode(
                [String: BuiltInColorRecord].self,
                from: data
              ) else { return }
        builtInColors = decoded
    }

    private func persistBuiltInColors() {
        if let data = try? JSONEncoder().encode(builtInColors) {
            defaults.set(data, forKey: builtInColorsKey)
        }
    }

    private func persist() {
        let records = custom.map {
            UserShiftRecord(
                code: $0.code, name: $0.name, shortName: $0.shortName,
                start: $0.start, end: $0.end,
                secondaryStart: $0.secondaryStart, secondaryEnd: $0.secondaryEnd,
                backgroundHex: $0.backgroundHex, foregroundHex: $0.foregroundHex,
                fontSize: $0.fontSize
            )
        }
        if let data = try? JSONEncoder().encode(records) { defaults.set(data, forKey: key) }
    }

    enum ShiftLibraryError: LocalizedError {
        case invalidCode, emptyName, reservedCode, invalidImport
        var errorDescription: String? {
            switch self {
            case .invalidCode: return "Skraćenica mora imati od 1 do 4 znaka."
            case .emptyName: return "Naziv smjene ne može biti prazan."
            case .reservedCode: return "Ta je skraćenica rezervirana za ugrađenu smjenu."
            case .invalidImport: return "JSON za uvoz nije ispravan."
            }
        }
    }
}
