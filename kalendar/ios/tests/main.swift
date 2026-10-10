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
        print("Raspored_app calendar schema and date regressions passed")
    }
}
