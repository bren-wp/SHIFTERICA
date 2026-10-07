import Foundation
import SwiftUI

struct ShiftTypeDef: Identifiable, Hashable {
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
    let custom: Bool

    var id: String { code }
    var color: Color { Color(hex: backgroundHex) }
    var textColor: Color { Color(hex: foregroundHex) }

    var durationMinutes: Int {
        intervalMinutes(start, end) + intervalMinutes(secondaryStart, secondaryEnd)
    }

    var timeText: String? {
        guard let start, let end else { return nil }
        if let secondaryStart, let secondaryEnd {
            return start + " – " + end + " / " + secondaryStart + " – " + secondaryEnd
        }
        return start + " – " + end
    }

    private func intervalMinutes(_ from: String?, _ to: String?) -> Int {
        guard let from, let to else { return 0 }
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.dateFormat = "HH:mm"
        guard let a = formatter.date(from: from), let b = formatter.date(from: to) else { return 0 }
        let raw = Int(b.timeIntervalSince(a) / 60)
        return raw > 0 ? raw : raw + 24 * 60
    }
}

enum ShiftCatalogIOS {
    static let night = ShiftTypeDef(code: "N", name: "Noćna smjena", shortName: "Noćna", start: "19:00", end: "07:00", secondaryStart: nil, secondaryEnd: nil, backgroundHex: 0xFFD21F, foregroundHex: 0x06131F, fontSize: 12, custom: false)
    static let day = ShiftTypeDef(code: "D", name: "Dnevna smjena", shortName: "Dnevna", start: "07:00", end: "19:00", secondaryStart: nil, secondaryEnd: nil, backgroundHex: 0x13B7F3, foregroundHex: 0x06131F, fontSize: 12, custom: false)
    static let annual = ShiftTypeDef(code: "GO", name: "Godišnji odmor", shortName: "Godišnji", start: nil, end: nil, secondaryStart: nil, secondaryEnd: nil, backgroundHex: 0x6CEB82, foregroundHex: 0x06131F, fontSize: 12, custom: false)
    static let morning = ShiftTypeDef(code: "J", name: "Jutarnja smjena", shortName: "Jutarnja", start: "07:00", end: "15:00", secondaryStart: nil, secondaryEnd: nil, backgroundHex: 0x77DED7, foregroundHex: 0x06131F, fontSize: 12, custom: false)
    static let sick = ShiftTypeDef(code: "BO", name: "Bolovanje", shortName: "Bolovanje", start: nil, end: nil, secondaryStart: nil, secondaryEnd: nil, backgroundHex: 0xD991EE, foregroundHex: 0x06131F, fontSize: 12, custom: false)
    static let all = [night, day, annual, morning, sick]
    static func byCode(_ code: String?) -> ShiftTypeDef? { all.first { $0.code == code } }
}
