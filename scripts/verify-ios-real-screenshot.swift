// Validates a REAL CoreSimulator screenshot before it enters visual parity QA.
// The app uses a dark interface. An almost-white capture indicates an iOS
// transition / SpringBoard frame rather than an app view, even if PNG is valid.
import Foundation
import CoreGraphics
import ImageIO

func reject(_ message: String) -> Never {
    fputs("INVALID SIMULATOR SCREENSHOT: \(message)\n", stderr)
    exit(1)
}

guard CommandLine.arguments.count == 2 else {
    reject("expected screenshot PNG path")
}
let path = CommandLine.arguments[1]
let url = URL(fileURLWithPath: path)
guard let source = CGImageSourceCreateWithURL(url as CFURL, nil),
      let image = CGImageSourceCreateImageAtIndex(source, 0, nil),
      image.width >= 300, image.height >= 300 else {
    reject("missing, unreadable or incorrectly sized PNG at \(path)")
}

let side = 64
var pixels = [UInt8](repeating: 0, count: side * side * 4)
let rendered = pixels.withUnsafeMutableBytes { raw -> Bool in
    guard let context = CGContext(
        data: raw.baseAddress,
        width: side, height: side, bitsPerComponent: 8,
        bytesPerRow: side * 4,
        space: CGColorSpaceCreateDeviceRGB(),
        bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue
    ) else { return false }
    context.interpolationQuality = .medium
    context.draw(image, in: CGRect(x: 0, y: 0, width: side, height: side))
    return true
}
guard rendered else { reject("could not examine PNG pixels") }

// Exclude status bar and edges; visually dark app screens must never have
// a blank white body. Check every sampled interior point, not PNG file size.
var almostWhite = 0
var checked = 0
for y in 8..<60 {
    for x in 4..<60 {
        let p = 4 * (y * side + x)
        checked += 1
        if pixels[p] >= 245 && pixels[p + 1] >= 245 && pixels[p + 2] >= 245 {
            almostWhite += 1
        }
    }
}
let fraction = Double(almostWhite) / Double(checked)
if fraction > 0.80 {
    reject(String(format: "blank/white frame %.1f%% white: %@", fraction * 100, path))
}
print(String(format: "Real screenshot accepted (white interior %.2f%%): %@", fraction * 100, path))
