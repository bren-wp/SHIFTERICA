import Foundation

/// Optional built-in times in interoperable schema v1.
enum ShiftBackupTimeRulesIOS {
    private static let working: Set<String> = ["N", "D", "P", "J"]
    private static let absences: Set<String> = ["GO", "BO"]
    private static let timePattern = #"^(?:[01]\d|2[0-3]):[0-5]\d$"#

    static func valid(code: String, start: String?, end: String?) -> Bool {
        guard working.contains(code) || absences.contains(code) else { return false }
        if start == nil && end == nil { return true }
        guard working.contains(code), let start, let end, start != end else {
            return false
        }
        return start.range(of: timePattern, options: .regularExpression) != nil &&
            end.range(of: timePattern, options: .regularExpression) != nil
    }
}
