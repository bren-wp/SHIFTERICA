import Foundation
import CoreGraphics
import ImageIO
import UniformTypeIdentifiers

let args = CommandLine.arguments
guard args.count == 2 else { fputs("usage: generate_app_icon.swift <output.png>\n", stderr); exit(2) }
let output = URL(fileURLWithPath: args[1])
let size = 1024
let colorSpace = CGColorSpaceCreateDeviceRGB()
guard let ctx = CGContext(data: nil, width: size, height: size, bitsPerComponent: 8, bytesPerRow: size * 4, space: colorSpace, bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue) else { exit(3) }

func rgb(_ hex: UInt32, _ alpha: CGFloat = 1) -> CGColor {
    CGColor(red: CGFloat((hex >> 16) & 0xff) / 255, green: CGFloat((hex >> 8) & 0xff) / 255, blue: CGFloat(hex & 0xff) / 255, alpha: alpha)
}
func rounded(_ rect: CGRect, _ radius: CGFloat, _ color: CGColor) {
    ctx.setFillColor(color); ctx.addPath(CGPath(roundedRect: rect, cornerWidth: radius, cornerHeight: radius, transform: nil)); ctx.fillPath()
}
func strokeRounded(_ rect: CGRect, _ radius: CGFloat, _ color: CGColor, _ width: CGFloat) {
    ctx.setStrokeColor(color); ctx.setLineWidth(width); ctx.addPath(CGPath(roundedRect: rect, cornerWidth: radius, cornerHeight: radius, transform: nil)); ctx.strokePath()
}
func glow(_ rect: CGRect, radius: CGFloat, color: CGColor, blur: CGFloat) {
    ctx.saveGState(); ctx.setShadow(offset: .zero, blur: blur, color: color); strokeRounded(rect, radius, color, 9); ctx.restoreGState()
}

ctx.setFillColor(rgb(0x090F1B)); ctx.fill(CGRect(x: 0, y: 0, width: size, height: size))
for i in stride(from: 0, through: 512, by: 8) {
    let t = CGFloat(i) / 512
    let c = CGColor(red: 0.04 + 0.02 * t, green: 0.08 + 0.06 * t, blue: 0.13 + 0.10 * t, alpha: 1)
    rounded(CGRect(x: CGFloat(i) * 0.16, y: CGFloat(i) * 0.10, width: CGFloat(size - i) * 0.98, height: CGFloat(size - i) * 0.98), 220 - CGFloat(i) * 0.18, c)
}
let outer = CGRect(x: 75, y: 70, width: 874, height: 884)
glow(outer, radius: 185, color: rgb(0x7CEBD6, 0.60), blur: 20)
strokeRounded(outer, 185, rgb(0xB2F2E5, 0.92), 11)
strokeRounded(outer.insetBy(dx: 18, dy: 18), 168, rgb(0x78C6FF, 0.70), 5)

ctx.saveGState(); ctx.setShadow(offset: .zero, blur: 16, color: rgb(0x7CEBD6, 0.45))
rounded(CGRect(x: 242, y: 260, width: 540, height: 145), 54, rgb(0x7CEBD6)); ctx.restoreGState()
for x in [350.0, 650.0] {
    ctx.saveGState(); ctx.setShadow(offset: .zero, blur: 12, color: rgb(0x78C6FF, 0.42))
    rounded(CGRect(x: x, y: 192, width: 78, height: 155), 38, rgb(0x78C6FF)); ctx.restoreGState()
}
let body = CGRect(x: 245, y: 423, width: 534, height: 381)
rounded(body, 44, rgb(0x15243B, 0.96)); strokeRounded(body, 44, rgb(0xB2F2E5, 0.58), 6)
let colors: [UInt32] = [0x7CEBD6, 0x91A7FF, 0x7CEBD6, 0x91A7FF, 0x7CEBD6, 0x91A7FF]
var idx = 0
for row in 0..<2 {
    for col in 0..<3 {
        let x = 310 + col * 157, y = 505 + row * 145
        ctx.saveGState(); ctx.setShadow(offset: .zero, blur: 8, color: rgb(colors[idx], 0.35))
        rounded(CGRect(x: x, y: y, width: 112, height: 112), 28, rgb(colors[idx])); ctx.restoreGState(); idx += 1
    }
}
let shine = CGMutablePath()
shine.move(to: CGPoint(x: 105, y: 110))
shine.addCurve(to: CGPoint(x: 700, y: 100), control1: CGPoint(x: 270, y: 40), control2: CGPoint(x: 520, y: 45))
shine.addLine(to: CGPoint(x: 470, y: 315))
shine.addCurve(to: CGPoint(x: 118, y: 365), control1: CGPoint(x: 335, y: 275), control2: CGPoint(x: 220, y: 300))
shine.closeSubpath()
ctx.setFillColor(rgb(0xFFFFFF, 0.10)); ctx.addPath(shine); ctx.fillPath()

guard let image = ctx.makeImage(),
      let dest = CGImageDestinationCreateWithURL(output as CFURL, UTType.png.identifier as CFString, 1, nil) else { exit(4) }
CGImageDestinationAddImage(dest, image, nil)
guard CGImageDestinationFinalize(dest) else { exit(5) }
print("Generated \(output.path)")
