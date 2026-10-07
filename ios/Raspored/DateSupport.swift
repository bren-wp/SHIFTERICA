import Foundation

extension Calendar {
    static var raspored: Calendar {
        var c = Calendar(identifier: .gregorian)
        c.locale = Locale(identifier: "hr_HR")
        c.firstWeekday = 2
        c.timeZone = .current
        return c
    }
}

extension DateFormatter {
    static let scheduleKey: DateFormatter = {
        let f = DateFormatter(); f.calendar = .raspored; f.locale = Locale(identifier:"en_US_POSIX"); f.dateFormat = "yyyy-MM-dd"; return f
    }()
    static let monthTitle: DateFormatter = {
        let f = DateFormatter(); f.calendar = .raspored; f.locale = Locale(identifier:"hr_HR"); f.dateFormat = "LLLL yyyy"; return f
    }()
    static let monthOnly: DateFormatter = {
        let f = DateFormatter(); f.calendar = .raspored; f.locale = Locale(identifier:"hr_HR"); f.dateFormat = "LLLL"; return f
    }()
}
