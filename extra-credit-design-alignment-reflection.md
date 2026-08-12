# Extra Credit Reflection — Design Alignment

*See `extra-credit-design-alignment.md` for submission requirements and the full assignment description.*

**Name:** Kenan Port
**Date:** 07-09-2026

---

## The Audit

I did this the way the assignment suggested — pulled the wireframes page up next to my running app and went screen by screen. My group and I had already gotten the app "working," so a lot of it looked close enough at a glance, but the second we lined components up side by side the gaps were obvious. A big chunk of this assignment ended up being me (and my pod, when we compared notes) just sitting and staring at the emulator against the wireframe for a few hours straight, tracing which Composable and which theme token was driving each little element. It's not hard work exactly, but it is tedious in a way I didn't expect — a lot of "why is this three pixels off / one shade too light" that turns into a real hunt through the code.

Concrete differences I found:

1. **Feed & Search headers were plain white.** The wireframe header sits on the light grey background, but the whole app already sits on that same grey, so a white top bar read like a floating title in an empty white void instead of a header band.

2. **Search screen said "Media Tracker" at the top instead of "Search,"** its header was white, and the search bar itself was grey (it was transparent over the grey background) where the wireframe shows a **white** pill.

3. **Search Results lost the bottom-nav highlight.** When you actually run a search you're still in the Search section, but the "Search" tab went un-highlighted because the results screen is a sibling route (`search_results`), so the nav's `hierarchy` check never matched `Routes.SEARCH`.

4. **Create Account field order was wrong** — mine rendered Display Name → **Email → Username**, but the wireframe is Display Name → **Username → Email**.

5. **Every tonal button came out pink, not purple.** The Follow buttons (and the new "+Add") looked red/pink instead of the indigo the spec calls for.

6. **Library cards had a muddy lavender-grey fill** — Material's default card container — instead of clean white cards on the grey backdrop. The colored media cover tile had also gone flat/grey.

7. **Star ratings floored.** A 4.5 title showed **4** filled stars instead of a half-star (or rounding up).

8. **The Library status filter drew a checkmark** on the selected segment on top of the purple highlight.

9. **Feed timestamps were bold and tiny** (a `labelSmall`, SemiBold 600, 11sp) and showed absolute `2024-01-22` dates instead of relative "2 hours ago" like the wireframe.

10. **Typography weights were hardcoded** — I counted roughly twenty `fontWeight = FontWeight.Bold/SemiBold` calls sprinkled directly into individual screens, and my `labelSmall` was `Medium` (500) instead of the `SemiBold` (600) the spec asks for.

---

## What You Changed

### Color System

`Color.kt` already had a palette, but I set the core tokens to the exact spec hexes — `Primary #6366F1`, `PrimaryContainer #E0E0FF`, `OnPrimaryContainer #3730A3`, `Secondary #DB2777`, `SecondaryContainer #FCE7F3`, `Tertiary #D97706` — plus all six status tokens with their containers (`WantTo #7C3AED` / `WantToContainer #EDE9FE`, `InProgress #2563EB` / `InProgressContainer #DBEAFE`, `Finished #059669` / `FinishedContainer #D1FAE5`). Everything is wired into `lightColorScheme(...)` in `Theme.kt` so components pull colors through `MaterialTheme.colorScheme` instead of hardcoding them.

The one hard rule here is "no raw color literals in Composables," and I actually caught myself violating it late: I had used `Color.Transparent` on the Search and Library app bars to let the grey background show through. That technically counts as a named literal, so I swapped both for `MaterialTheme.colorScheme.background`, which looks identical (grey header over grey screen) but keeps every color coming from the theme. After that pass there are zero `Color(0xFF...)` / `Color.<name>` literals left in the UI.

I also kept the small `avatarColor(key)` helper that hashes a user id to a stable brand color so the feed reads as multiple different people instead of one repeated color.

### Typography

This is the area I was most behind on before and put the most work into now. I rebuilt `Type.kt` as a full type scale with the spec weights baked into the *styles*: **Display / H1 = Bold (700)** (`displaySmall`, `headlineMedium`, `headlineSmall`), **H2 / H3 = SemiBold (600)** (the `title*` styles), **Body = Regular (400)** (`body*`), **Label / Caption = SemiBold (600)** (`label*`, so `labelSmall` is finally 600 instead of 500).

Then I went screen by screen and deleted the ~20 hardcoded `fontWeight` overrides so each `Text` inherits its weight from the style. Where a text was being forced bold with a slightly-wrong style (e.g. a list title using `bodyMedium` + `fontWeight = Medium`), I switched it to the correct style (`titleSmall`, which is already 600) rather than re-adding a hardcoded weight. I also fixed two spots that were the *wrong kind* of style: the feed timestamps and the "★ · Type · Year" line were tagged as `labelSmall` (bold labels) when the wireframe treats them as body text — those are now `bodySmall` (regular), which matches and is still a real typography style, not a hardcode.

The one deliberate exception is a single `SpanStyle(fontWeight = SemiBold)` in the feed that bolds just the first name inside a sentence ("**Jordan** finished watching"). Bolding one word mid-string genuinely needs a span; it's the only weight override left and I left it on purpose.

### Buttons

The spec wants three variants — filled, tonal, outlined — all with a **20dp** radius. I added `shape = RoundedCornerShape(20.dp)` to every `Button` / `OutlinedButton` in the app. The app had **no tonal variant at all**, so I introduced `FilledTonalButton` where the wireframe uses it: the "Follow" button and the Library "+Add" button.

That's where the pink-button problem came from, and it was the most annoying thing I chased: `FilledTonalButton` **defaults to `secondaryContainer`**, and our secondary is pink — so a "tonal" button is pink out of the box even though the spec says tonal = *primary* container. I had to explicitly pass `ButtonDefaults.filledTonalButtonColors(containerColor = primaryContainer, contentColor = onPrimaryContainer)` on all three tonal buttons to get the correct indigo. I only noticed because the "+Add" button was obviously red; once I understood the cause I fixed the Follow buttons too, which had been quietly wrong the whole time.

### Text Fields

Standard `OutlinedTextField`s (login + register) now use `shape = RoundedCornerShape(8.dp)` with `OutlinedTextFieldDefaults.colors(focusedBorderColor = primary)`, and the search bars use the `28.dp` pill shape. I also set the search fields' container to `surface` so the bar reads as a white pill on the grey background instead of disappearing into it.

### Other Components

- **Filter chips** (`SearchComponents.kt`): `shape = 8.dp`, active = `primaryContainer` / `onPrimaryContainer`, inactive = `surface`.
- **Status badges** (`StatusBadge.kt`): each `LibraryStatus` maps to its container background + saturated text color, reused across Library and Profile so status is instantly readable.
- **Cards**: every content card is `elevation = 2.dp` + `RoundedCornerShape(12.dp)`, and I set `containerColor = surface` explicitly so cards are clean white on the grey screen (the Library card was defaulting to a tinted grey).
- **Bottom nav** (`BottomNavBar.kt`): white bar, active icon/label in `primary`, indicator pill in `primaryContainer`, inactive in `onSurfaceVariant` — and I fixed the highlight so it persists on the search-results screen by treating `search_results` as part of the Search section.
- **Headers**: Feed uses a grey header with a white feed body; Search uses a fully grey backdrop with a white search bar; Library matches (grey backdrop, white cards, colored cover tiles) — all using `background` / `surface` / `surfaceVariant` tokens, nothing hardcoded.
- **Library**: renamed to "My Library," added the purple "+Add" tonal button in the top bar (routes to Search), restored the type-colored cover tiles (book = indigo, movie = pink, show = amber), and removed the segmented checkmark with `icon = {}`.
- **Ratings**: rewrote the detail-screen `StarRow` to round to the nearest half-star using Material's built-in `Icons.Filled.StarHalf`, so 4.5 shows four full + one half instead of flooring to four.
- **Feed** (`ActivityFeedScreen.kt`): first names only (still bold), media-type-aware action text ("finished **reading**" for books, "finished **watching**" for movies/shows), relative timestamps, and — see below — stars on "finished" posts, not just reviews.

A note on that last one, because it was a small design decision and not just a style fix. We're wiring the real Write Review screen this coming week, and it has a "share to activity feed" toggle that will eventually populate review/rating data. But I intentionally built the feed early to show a star rating on **finished** items too (pulled from the title's average rating), not only on reviews. The reasoning: not everyone who finishes something is going to review it, and for a personal media tracker, reviewing shouldn't be a prerequisite for logging that you finished something. So the feed treats "finished" as a first-class event that can still show stars, and "review" adds the quote on top. Getting that in now means the feed already reads well whether or not a user chooses to review — a nice touch we didn't want to leave for later.

Throughout, we leaned on the professor's reference code for the patterns we weren't sure about (the repository/ViewModel split, the colored cover-tile approach, the nav graph structure) and mirrored those rather than inventing our own, so the app stays consistent with how the course is teaching it.

---

## What Was Hard

The single most confusing thing was learning that **Material components have their own default color slots that are not the ones you'd guess.** The tonal button is the perfect example: I read "tonal = primary container" in the spec, dropped in a `FilledTonalButton`, and got a pink button. I spent a while assuming I'd hardcoded a wrong color somewhere before I realized the component *default* for a tonal button is `secondaryContainer`, and our secondary is pink. The lesson is that "use the right variant" isn't enough — you have to explicitly feed the component the color roles the spec wants, because its defaults have opinions.

The grey headers were the other real head-scratcher, and it cost me a couple of rebuild-and-screenshot cycles where I swore nothing was changing. The whole app is drawn on the theme's `background` grey (via the Scaffold), and I first tried tinting the headers with `surfaceVariant` — which is actually a *lighter* shade than `background`, so the header was blending in or even reading slightly whiter, not standing out. I had to actually think about the `surface` vs `surfaceVariant` vs `background` hierarchy to understand why "make it grey" was doing nothing, and land on painting the header with `background` and the cards/search bar with `surface`.

Beyond the two big ones, it was a lot of small, tedious tracing: the segmented control's checkmark turned out to be a default `icon` slot (`icon = {}` to remove it); the star flooring was a `>= 6` tenths threshold hiding in `StarRow`; the search-results nav highlight was a route-matching detail; the "random colored" library items were just the default card container. None of these are individually hard, but finding each one meant reading the composable carefully and figuring out which default or which token was responsible. Honestly the most accurate description of the work is "three hours of staring at code to move UI elements a little," and I have a lot more respect now for how much detail lives under a screen that "already works."

---

## What You Understand Now

Before this, I treated `MaterialTheme.colorScheme` like a bag of named colors and grabbed whatever looked right. Two things clicked:

1. **The color roles are paired on purpose.** `primary`/`onPrimary`, `primaryContainer`/`onPrimaryContainer`, `secondaryContainer`/`onSecondaryContainer` — the "container" is a filled background and the base/`on-` color is the content on top of it. A highlighted pill (nav indicator, active chip, status badge, tonal button) is `container` background + the matching `on`/saturated color for text. Once I saw that pattern, `NavigationBarItemDefaults`, `FilterChipDefaults`, and `filledTonalButtonColors` stopped feeling like magic — they're just slots asking which role goes where, and their *defaults* are frequently not the role the spec wants, so you override them.

2. **Weights and shapes belong in the theme, not the screen.** `Type.kt` owns the weights (700/600/400/600) so a `Text` just picks a style and inherits the weight; `RoundedCornerShape(20.dp)` / `8.dp` / `12.dp` go on the components. When a value lives in one place, every screen stays consistent and you can retheme without touching the UI. The way I'd explain it to a pod mate: never hardcode a color or a weight in a screen — define it once (`Color.kt` / `Type.kt`), reach for the *role* or the *style* in the UI, and pull repeated pieces (like the status pill) into one small Composable so it can only look one way.

---

## Self-Assessment

| Section | Possible | My Estimate |
|:---|:---:|:-----------:|
| Color System | 13 |     12      |
| Typography | 5 |      5      |
| Component Styling | 15 |     13      |
| Navigation & Cards | 5 |      5      |
| Reflection | 12 |     10      |
| **Total** | **50** |   **45**    |

*One thing I think I did well:* Tracking the color problems back to their actual source instead of patching them screen by screen. The tonal-button fix is the best example — instead of recoloring one button, I understood *why* every tonal button was pink (the `secondaryContainer` default) and fixed the variant everywhere at once. Same with the status badges and the shared `StatusBadge` Composable: fix it in one place, and it's right across the whole app.

*One thing I know I left incomplete or could have done better:* A couple of things sit just outside the strict spec that I'd want to revisit. The feed rounds ratings to whole text stars rather than drawing half-stars like the detail screen (fine for now, but inconsistent), several of the wireframe polish items depend on mock data that'll change once the API is wired, and there's the one intentional `SpanStyle` weight in the feed. None of those cost rubric points, but they're the honest rough edges.
