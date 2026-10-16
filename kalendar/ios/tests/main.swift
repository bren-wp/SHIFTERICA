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
        check(CalendarEntry(date: date, title: "Novo").category == "Događaj",
              "universal events are the default")
        check(CalendarRules.categories.count == 14,
              "all everyday categories are supported")
        check(CalendarEventPresets.all.count == 15,
              "generic quick event suggestions")
        check(Set(CalendarEventPresets.all.map(\.code)).count == CalendarEventPresets.all.count,
              "event codes must be unique")
        check(CalendarEventPresets.all.allSatisfy {
            CalendarRules.valid($0.draft(for: CalendarClock.parse(date)!))
        }, "all generic event drafts are valid")
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
        check(CalendarClock.monthCells(first).count == 35,
              "October 2026 uses five compact calendar rows")
        check(CalendarClock.monthCells(CalendarClock.parse("2027-02-01")!).count == 28,
              "February 2027 uses four calendar rows")
        check(CalendarClock.monthCells(CalendarClock.parse("2026-08-01")!).count == 42,
              "August 2026 requires six calendar rows")
        check(CalendarClock.key(first) == "2026-10-01", "day key round trip")
        let december = CalendarClock.parse("2026-12-31")!
        let week = CalendarClock.weekCells(december).map(CalendarClock.key)
        check(week.count == 7, "week contains exactly seven dates")
        check(week.first == "2026-12-28" && week.last == "2027-01-03",
              "week spans the year boundary Monday through Sunday")
        let newYear = CalendarClock.parse("2027-01-01")!
        check(CalendarClock.weekCells(newYear).map(CalendarClock.key) == week,
              "dates within the same cross-year week share a week window")
        let bytes = try! CalendarBackup.encode([
            CalendarEntry(id: "e1", date: date, title: "Smjena",
                          note: "Važno: novi raspored", start: "22:00", end: "06:00"),
            CalendarEntry(id: "e2", date: date, title: "",
                          note: "Samo napomena", category: "Napomena", color: 3)
        ])
        check((try! CalendarBackup.decode(bytes)).count == 2,
              "backup round-trip retains multiple events and notes")
        let copied = CalendarEntry(id: "duplicate-copy", date: "2026-10-14", title: "Smjena")
        let original = CalendarEntry(id: "original", date: date, title: "Smjena")
        let restoredCopies = try! CalendarBackup.decode(
            CalendarBackup.encode([original, copied])
        )
        check(Set(restoredCopies.map(\.id)) == Set(["original", "duplicate-copy"]),
              "duplicated items retain unique IDs across backup round-trip")
        check((try? CalendarBackup.encode([original, original])) == nil,
              "reject duplicate IDs before iOS backup export")
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
        var wrongType = try! JSONSerialization.jsonObject(with: bytes) as! [String: Any]
        var typedEntries = wrongType["entries"] as! [[String: Any]]
        typedEntries[0]["title"] = 42
        wrongType["entries"] = typedEntries
        check((try? CalendarBackup.decode(
            JSONSerialization.data(withJSONObject: wrongType))) == nil,
              "reject non-string event title")
        wrongType = try! JSONSerialization.jsonObject(with: bytes) as! [String: Any]
        wrongType["version"] = "1"
        check((try? CalendarBackup.decode(
            JSONSerialization.data(withJSONObject: wrongType))) == nil,
              "reject non-integer backup version")
        check((try? CalendarBackup.decode(Data([0xC3, 0x28]))) == nil,
              "reject malformed UTF-8 input")
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
        let bulk = (1...240).map { number in
            CalendarEntry(id: "event-\(number)", date: date,
                          title: "Pregled smjena \(number)")
        }
        let allMatches = CalendarSearch.find(bulk, query: "pregled smjena")
        check(allMatches.count == 240,
              "search retains matches after the first 200 records")
        check(Set(allMatches.map(\.id)) == Set(bulk.map(\.id)),
              "search paging does not hide matching event IDs")
        let calendarPresets = CalendarShiftPresets.builtIn
        check(calendarPresets.map(\.code) == ["N", "D", "J", "P", "GO", "BO"],
              "the universal calendar preserves original Raspored shift choices")
        let presetDate = CalendarClock.parse("2026-10-14")!
        let savedPresets = calendarPresets.map { $0.record(for: presetDate) }
        check(savedPresets.allSatisfy(CalendarRules.valid),
              "every quick shift must be a valid standalone calendar record")
        check(Set(savedPresets.map(\.id)).count == 6,
              "each quick shift must get its own identity")
        check(savedPresets[0].start == "19:00" && savedPresets[0].end == "07:00",
              "overnight N shift follows the reference application")
        let notedPreset = CalendarEntry(
            id: "preserved-user-id", date: CalendarClock.key(presetDate),
            title: calendarPresets[0].title, note: "Sačuvaj korisničku bilješku",
            category: "Smjena", start: calendarPresets[0].start,
            end: calendarPresets[0].end, color: 3
        )
        check(CalendarShiftPresets.alreadyScheduled(
            [notedPreset], preset: calendarPresets[0], on: presetDate
        ), "a repeated quick shift must not create another same-day entry")
        check(!CalendarShiftPresets.alreadyScheduled(
            [notedPreset], preset: calendarPresets[1], on: presetDate
        ), "different built-in quick shifts remain independent")
        check(!CalendarShiftPresets.alreadyScheduled(
            [notedPreset], preset: calendarPresets[0],
            on: CalendarClock.parse("2026-10-15")!
        ), "a preset on another date must still be allowed")
        check(notedPreset.note == "Sačuvaj korisničku bilješku",
              "existing user notes are never overwritten by preset selection")
        check(CalendarScheduledTime.minutes(savedPresets[0]) == 720,
              "overnight shifts are twelve scheduled hours")
        check(CalendarScheduledTime.minutes(savedPresets[4]) == 0,
              "untimed holiday is not treated as paid hours")
        check(CalendarScheduledTime.display(735) == "12 h 15 min",
              "duration display preserves minutes")
        let allPresets = CalendarShiftPresets.all
        check(CalendarShiftPresets.additional.count >= 30,
              "expanded shift patterns available on iOS")
        check(Set(allPresets.map(\.code)).count == allPresets.count,
              "every shift abbreviation is unique")
        check(allPresets.map { $0.record(for: presetDate) }.allSatisfy(CalendarRules.valid),
              "all expanded presets make valid records")
        check(CalendarShiftPresets.code(for: allPresets.first { $0.code == "S3" }!.record(for: presetDate)) == "S3",
              "calendar tiles preserve preset codes")
        check(CalendarScheduledTime.minutes(allPresets.first { $0.code == "24" }!.record(for: presetDate)) == 1440,
              "24 hour shift keeps full duration")
        check(CalendarShiftPresets.matches(allPresets.first { $0.code == "6N" }!, query: "nocna"),
              "shift search is insensitive to diacritics")
        check(CalendarScheduledTime.minutes(allPresets.first { $0.code == "DV" }!.record(for: presetDate)) == 0,
              "split shift does not invent a continuous interval")
        print("Raspored_app calendar/backup/search regressions passed")
    }
}
