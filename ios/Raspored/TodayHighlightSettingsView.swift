import SwiftUI

/// Compact, self-contained editor for the highlighted current-day badge.
struct TodayHighlightSettingsIOS: View {
    @EnvironmentObject private var settings: UISettingsStoreIOS

    private let highlightColors: [Color] = [
        RColors.night, RColors.day, RColors.annual, RColors.morning,
        Color(hex: 0xB16CE4), Color(hex: 0xFF5BAA), Color(hex: 0xFF853A)
    ]

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            shapeChoices
            colorChoices
        }
    }

    private var shapeChoices: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text("Oblik").fontWeight(.bold).foregroundStyle(RColors.text)
            Text("Odaberite oblik isticanja").font(.caption2).foregroundStyle(RColors.muted)
            HStack(spacing: 7) {
                shapeButton("Zaobljeni kvadrat", symbol: "rounded")
                shapeButton("Krug", symbol: "circle")
                shapeButton("Kvadrat", symbol: "square")
                shapeButton("Pill", symbol: "pill")
            }
        }
        .padding(.vertical, 4)
    }

    private func shapeButton(_ value: String, symbol: String) -> some View {
        let active = settings.todayShape == value
        return Button { settings.todayShape = value } label: {
            ZStack {
                RoundedRectangle(cornerRadius: 11)
                    .fill(active ? RColors.accent.opacity(0.20) : RColors.card2)
                RoundedRectangle(cornerRadius: 11)
                    .stroke(active ? RColors.accent : RColors.stroke.opacity(0.55), lineWidth: active ? 1.5 : 1)
                shapeSymbol(symbol).foregroundStyle(RColors.text)
            }
            .frame(maxWidth: .infinity)
            .frame(height: 46)
            .shadow(color: active ? RColors.accent.opacity(0.30) : .clear, radius: 7, y: 2)
        }
        .buttonStyle(.plain)
    }

    @ViewBuilder
    private func shapeSymbol(_ symbol: String) -> some View {
        switch symbol {
        case "circle":
            Circle().stroke(lineWidth: 1.7).frame(width: 24, height: 24)
        case "square":
            RoundedRectangle(cornerRadius: 2).stroke(lineWidth: 1.7).frame(width: 25, height: 25)
        case "pill":
            Capsule().stroke(lineWidth: 1.7).frame(width: 34, height: 21)
        default:
            RoundedRectangle(cornerRadius: 7).stroke(lineWidth: 1.7).frame(width: 25, height: 25)
        }
    }

    private var colorChoices: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text("Boja").fontWeight(.bold).foregroundStyle(RColors.text)
            HStack(spacing: 7) {
                ForEach(Array(highlightColors.enumerated()), id: \.offset) { index, color in
                    Button { settings.todayColorIndex = index } label: {
                        RoundedRectangle(cornerRadius: 9).fill(color).frame(width: 37, height: 37)
                            .overlay(RoundedRectangle(cornerRadius: 9).stroke(settings.todayColorIndex == index ? RColors.accent : RColors.stroke.opacity(0.45), lineWidth: settings.todayColorIndex == index ? 2 : 1))
                    }
                    .buttonStyle(.plain)
                }
            }
        }
        .padding(.vertical, 4)
    }

}
