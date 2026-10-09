import Foundation

/// Optional built-in times in interoperable schema v1.
enum ShiftBackupTimeRulesIOS {
    private static let working: Set<String> = ["N", "D", "P", "J"]
    private static let absences: Set<String> = ["GO", "BO"]
    private static let timePattern = #"^(?:[01]\d|2[0-3]):[0-5]\d$"#

    /// Match Android's preflight rules for both custom-shift intervals.
    static func validCustom(
        start: String?, end: String?,
        secondaryStart: String?, secondaryEnd: String?
    ) -> Bool {
        validPair(start: start, end: end) &&
            validPair(start: secondaryStart, end: secondaryEnd)
    }

    private static func validPair(start: String?, end: String?) -> Bool {
        if start == nil && end == nil { return true }
        guard let start, let end else { return false }
        return start.range(of: timePattern, options: .regularExpression) != nil &&
            end.range(of: timePattern, options: .regularExpression) != nil
    }

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
