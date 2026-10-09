import Foundation

/// Prevents dangling schedule references when deleting a custom shift definition.
enum ShiftDeletionPolicyIOS {
    static func assignedDates(_ codes: [String], code: String) -> Int {
        codes.filter { $0 == code }.count
    }

    static func canDelete(_ codes: [String], code: String) -> Bool {
        assignedDates(codes, code: code) == 0
    }
}
