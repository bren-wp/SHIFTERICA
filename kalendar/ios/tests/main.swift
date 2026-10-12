import Foundation

@main
enum CalendarRegression {
    static func main() {
        func check(_ value: @autoclosure () -> Bool, _ message: String) {
            if !value() {
                fputs("Calendar regression failed: " + message + "\n", stderr)
                exit(1)
            }
        }
        let date = "2026-10-11"
        let ordinary = CalendarEntry(id: "a", date: date, title: "Moja smjena")
        check(CalendarRules.valid(ordinary), "ordinary event")
        check(CalendarRules.valid(CalendarEntry(
            id: "b", date: date, title: "", note: "Slobodni tekst"
        )), "note-only event")
        check(CalendarRules.valid(CalendarEntry(
            id: "c", date: date, title: "Noćna",
            start: "22:00", end: "06:00"
        )), "overnight shift")
        check(!CalendarRules.valid(CalendarEntry(
            id: "x", date: "2026-02-30", title: "Bad"
        )), "reject invalid date")
        check(!CalendarRules.valid(CalendarEntry(
            id: "x", date: date, title: "Bad", start: "25:00", end: "08:00"
        )), "reject invalid clock")
        check(!CalendarRules.valid(CalendarEntry(
            id: "x", date: date, title: "Bad", start: "08:00"
        )), "reject incomplete interval")
        let note = CalendarEntry(id: "d", date: date, title: "Napomena")
        let result = CalendarRules.forDay([
            note, ordinary, CalendarEntry(
                id: "b", date: date, title: "Zora",
                start: "07:00", end: "15:00"
            ),
            CalendarEntry(id: "skip", date: "2026-10-12", title: "Other")
        ], key: date)
        check(result.map(\.id) == ["b", "a", "d"], "day sorting and filtering")
        let first = CalendarClock.parse("2026-10-01")!
        check(CalendarClock.monthCells(first).count == 42, "42 calendar cells")
        check(CalendarClock.key(first) == "2026-10-01", "day key round trip")
        let bytes = try! CalendarBackup.encode([
            CalendarEntry(id: "e1", date: date, title: "Smjena",
                          note: "Važno: novi raspored", start: "22:00", end: "06:00"),
            CalendarEntry(id: "e2", date: date, title: "",
                          note: "Samo napomena", category: "Napomena", color: 3)
        ])
        check((try! CalendarBackup.decode(bytes)).count == 2,
              "backup round-trip retains multiple events and notes")
        var broken = try! JSONSerialization.jsonObject(with: bytes) as! [String: Any]
        broken["version"] = 500
        let invalid = try! JSONSerialization.data(withJSONObject: broken)
        check((try? CalendarBackup.decode(invalid)) == nil, "reject unsupported version")
        broken["version"] = 1
        broken["entries"] = [
            (broken["entries"] as! [[String: Any]])[0],
            (broken["entries"] as! [[String: Any]])[0]
        ]
        let repeated = try! JSONSerialization.data(withJSONObject: broken)
        check((try? CalendarBackup.decode(repeated)) == nil, "reject duplicate IDs")
        check((try? CalendarBackup.decode(Data(repeating: 32, count: CalendarBackup.maxBytes + 1))) == nil,
              "reject oversized backups")
        let named = [
            CalendarEntry(id: "test1", date: date, title: "Noćna smjena",
                          note: "Đurđica čeka"),
            CalendarEntry(id: "test2", date: "2026-10-13",
                          title: "Jutarnja smjena")
        ]
        check(CalendarSearch.find(named, query: "  NOCNA  ").map(\.id) == ["test1"],
              "accent-insensitive shift names")
        check(CalendarSearch.find(named, query: "durdica").map(\.id) == ["test1"],
              "accent-insensitive notes")
        check(CalendarSearch.find(named, query: "nema").isEmpty,
              "search without results")
        print("Raspored_app calendar/backup/search regressions passed")
    }
}
