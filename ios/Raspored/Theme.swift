import Foundation
import SwiftUI

extension Color {
    init(hex: UInt32) {
        self.init(
            red: Double((hex >> 16) & 0xFF) / 255.0,
            green: Double((hex >> 8) & 0xFF) / 255.0,
            blue: Double(hex & 0xFF) / 255.0
        )
    }
}

enum RColors {
    static let bg = Color(hex: 0x061624)
    static let bg2 = Color(hex: 0x0A2235)
    static let card = Color(hex: 0x12293D).opacity(0.94)
    static let card2 = Color(hex: 0x102A40)
    static let stroke = Color(hex: 0x2A5D7D)
    static let text = Color(hex: 0xF6F8FB)
    static let muted = Color(hex: 0xAFC1D8)
    static let accent = Color(hex: 0x19DCE0)
    static let day = Color(hex: 0x13B7F3)
    static let night = Color(hex: 0xFFD21F)
    static let annual = Color(hex: 0x6CEB82)
    static let morning = Color(hex: 0x77DED7)
    static let afternoon = Color(hex: 0xFF8A3D)
    static let sick = Color(hex: 0xD991EE)
    static let weekend = Color(hex: 0xFF7186)
    static let empty = Color(hex: 0x122B40)
    static let weekendEmpty = Color(hex: 0x352436)
}
