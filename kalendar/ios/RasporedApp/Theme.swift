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
    static let bg = Color(hex: 0x090F1B)
    static let bg2 = Color(hex: 0x102139)
    static let card = Color(hex: 0x15243B).opacity(0.94)
    static let card2 = Color(hex: 0x1B304C)
    static let stroke = Color(hex: 0x345273)
    static let text = Color(hex: 0xF4F7FD)
    static let muted = Color(hex: 0xB8C7DB)
    static let accent = Color(hex: 0x7CEBD6)
    static let accent2 = Color(hex: 0x91A7FF)
    static let danger = Color(hex: 0xFF8C9C)
    static let day = Color(hex: 0x78C6FF)
    static let night = Color(hex: 0xF8CC7B)
    static let annual = Color(hex: 0x82DFAD)
    static let morning = Color(hex: 0x87DDD5)
    static let afternoon = Color(hex: 0xFFB47D)
    static let sick = Color(hex: 0xD3B3FF)
    static let weekend = Color(hex: 0xFFA0B5)
    static let empty = Color(hex: 0x192B42)
    static let weekendEmpty = Color(hex: 0x312438)
}
