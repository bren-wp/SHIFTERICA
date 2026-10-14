import SwiftUI

/// Matches the original Raspored branded launch composition without payroll.
struct CalendarParitySplash: View {
    @State private var progress: Double = 0

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [Palette.bg2, Palette.background, .black],
                startPoint: .top, endPoint: .bottom
            ).ignoresSafeArea()
            CalendarSplashBackdrop()
            splashTile("D", color: Palette.colors[1], size: 76)
                .offset(x: -128, y: -260).rotationEffect(.degrees(-12))
            splashTile("N", color: Palette.colors[5], size: 72)
                .offset(x: 132, y: -215).rotationEffect(.degrees(13))
            splashTile("GO", color: Palette.colors[4], size: 70)
                .offset(x: -150, y: 110).rotationEffect(.degrees(-11))
            splashTile("J", color: Palette.colors[0], size: 70)
                .offset(x: 150, y: 145).rotationEffect(.degrees(10))
            splashTile("BO", color: Palette.colors[2], size: 74)
                .offset(x: 95, y: 300).rotationEffect(.degrees(9))
            VStack(spacing: 18) {
                CalendarParityBrandMark()
                    .frame(width: 126, height: 126)
                    .padding(11)
                    .background(
                        Palette.mint.opacity(0.07),
                        in: RoundedRectangle(cornerRadius: 46)
                    )
                    .overlay(
                        RoundedRectangle(cornerRadius: 46)
                            .stroke(Palette.mint.opacity(0.34), lineWidth: 1)
                    )
                Text("Raspored")
                    .font(.system(size: 40, weight: .black))
                    .foregroundStyle(Palette.text)
                Text("Pametni planer smjena")
                    .font(.system(size: 17))
                    .tracking(1.2)
                    .foregroundStyle(Palette.secondary)
                Spacer().frame(height: 74)
                ProgressView(value: progress)
                    .tint(Palette.mint)
                    .frame(width: 270, height: 6)
            }
        }
        .onAppear {
            withAnimation(.linear(duration: 1.1)) { progress = 1 }
        }
        .accessibilityLabel("Raspored, pokretanje aplikacije")
    }

    private func splashTile(_ code: String, color: Color, size: CGFloat) -> some View {
        ZStack {
            RoundedRectangle(cornerRadius: 19)
                .fill(LinearGradient(
                    colors: [color, color.opacity(0.78)],
                    startPoint: .top, endPoint: .bottom
                ))
                .overlay(
                    RoundedRectangle(cornerRadius: 19)
                        .stroke(.white.opacity(0.56), lineWidth: 1)
                )
            Text(code)
                .font(.system(size: code.count == 1 ? 27 : 20, weight: .black))
                .foregroundStyle(Palette.background)
        }
        .frame(width: size, height: size)
        .shadow(color: .black.opacity(0.35), radius: 10, y: 4)
    }
}

private struct CalendarSplashBackdrop: View {
    var body: some View {
        VStack(spacing: 7) {
            Text(CalendarClock.title(Date(), format: "LLLL yyyy").uppercased())
                .font(.system(size: 24, weight: .black))
                .foregroundStyle(Palette.text.opacity(0.18))
            ForEach(0..<5, id: \.self) { row in
                HStack(spacing: 7) {
                    ForEach(0..<7, id: \.self) { column in
                        let shade = (row + column) % 5 == 0
                            ? Palette.colors[1].opacity(0.20)
                            : Palette.card2.opacity(0.30)
                        RoundedRectangle(cornerRadius: 9)
                            .fill(shade)
                            .frame(width: 39, height: 42)
                    }
                }
            }
        }
        .rotationEffect(.degrees(-5))
        .offset(y: -140)
        .accessibilityHidden(true)
    }
}
