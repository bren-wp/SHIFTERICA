# Universal calendar visual parity with Raspored

Source of truth is the original Android RasporedApp/AppHeader/MonthCalendar/Theme/YearScreen/SplashView and iOS RootView/MonthCalendarComponents/Theme/YearOverviewView.

The independent Android/iOS calendar deliberately retains separate app IDs, offline storage, event editing and merge-only JSON v1 backups.

## Implemented source-level parity
- [x] Dark sapphire/midnight/mint color system on Android and iOS
- [x] Premium fixed header with calendar mark, search, settings and add
- [x] Fixed Mjesec / Godina / Sažetak three-tab bottom navigation
- [x] Full-height non-scrollable month screen; day entries/search are inside a modal
- [x] D/N/J/P/GO/BO quick selection uses original shift titles, colors and times
- [x] Monday-first grid, PON to NED labels, rounded colored date tiles and shift abbreviations
- [x] Two-column twelve-month year overview
- [x] Non-financial summary by calendar counts, categories and backups
- [x] Branded splash composition and progress animation
- [x] No payroll model imports, salaries, coefficients or tax calculations

## Outstanding verification
- [ ] Actual per-screen and per-device screenshot similarity compared with original Raspored
- [ ] Modal, settings and screen flow pixel parity on Android and iPhone
- [ ] Touch interaction and accessibility regression on physical devices
- [ ] Store production signing and submission

Never describe this patch as fully pixel-perfect until those explicit checks pass. Maintain mandatory Android/iOS build, lint, regression and simulator screenshot gates.
