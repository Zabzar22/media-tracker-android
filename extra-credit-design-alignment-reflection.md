# Extra Credit Reflection — Design Alignment

*See `extra-credit-design-alignment.md` for submission requirements and the full assignment description.*

**Name:** Kenan Port
**Date:** 07-02-2026

---

## The Audit

I did this the way the assignment suggested — pulled up the wireframes page next to my running app and went screen by screen. A lot of it looked "close enough" at a glance, but once I actually lined components up side by side the gaps were obvious. What I found:

1. **Login/Register screens** — the "Media Tracker" title was rendered in the primary indigo color, but in the wireframe the title is black (`onSurface`) and only the **Log In / Sign Up** links are purple. Mine was tinting the whole heading.

2. **Register screen link row** — the entire "Don't have an account? Sign Up" line was purple. In the wireframe only the "Sign Up" part is the link color; the question stays gray (`onSurfaceVariant`). Same problem on the login side.

3. **Bottom navigation** — the active tab pill was showing up pink-ish and the inactive panels had a slight background tint. The wireframe is a plain white bar with the active item in a **primary-container purple pill** and inactive items in `onSurfaceVariant`.

4. **Search results** — every result had its rating text tinted, and the tint was pink. The wireframe only colors the **star + number** in amber (`#D97706`), and the "· Movie · 2012" metadata after it stays gray. Mine was also drawing a rating line even when the item had no rating.

5. **Status badges (Library + Profile)** — this was the one I almost missed. My "Want To / In Progress / Finished" labels were all the same gray, so you couldn't tell the statuses apart at a glance. The wireframe gives each status its own light-colored pill (purple / blue / green).

6. **Feed avatars** — all the avatars were the same single color, so the feed didn't read as multiple different users like the wireframe does.

---

## What You Changed

### Color System

Before, `Color.kt` had a working palette but it wasn't fully lined up with the spec table, and the wrong accent (pink `Secondary`) was leaking into places it didn't belong (nav, chips, ratings). I set the core tokens to the exact spec values — `Primary #6366F1`, `PrimaryContainer #E0E0FF`, `OnPrimaryContainer #3730A3`, `Secondary #DB2777`, `SecondaryContainer #FCE7F3`, `Tertiary #D97706` — and they all get wired into `lightColorScheme(...)` in `Theme.kt` so components pull them through `MaterialTheme.colorScheme` instead of hardcoding.

I also added the status accents and, this time, their **containers**: `WantTo #7C3AED` / `WantToContainer #EDE9FE`, `InProgress #2563EB` / `InProgressContainer #DBEAFE`, `Finished #059669` / `FinishedContainer #D1FAE5`. That container + saturated-text pairing is what the status badges needed. I also hunted down the one leftover hardcoded literal — a `Color.White` on the feed avatar initials — and swapped it for `MaterialTheme.colorScheme.onPrimary` so there are no raw color literals left in the Composables.

And I added an `AvatarColors` list plus a small `avatarColor(key)` helper that hashes a user id into a stable color, which fixed the "all avatars are the same" problem.

### Typography

`Type.kt` already routes everything through `MaterialTheme.typography` rather than hardcoding font sizes in each screen, which was the main thing. Titles are SemiBold and body is Normal, set on the type styles instead of being sprinkled into individual Composables. I'll be honest that this is the area I did the least to — I left a couple of explicit `fontWeight = FontWeight.Bold` calls on the auth titles to force the heading bold, and my label style is still Medium rather than the SemiBold the spec asks for. I held off on changing the shared weights in `Type.kt` because I wasn't confident it wouldn't quietly restyle text all over the app.

### Buttons

I didn't have to rework the button variants much — the app already used the standard filled `Button` for primary actions and `TextButton` for links. What I *did* change was the link buttons on the auth screens: I split "prompt + link" into a `Row` so the gray question and the purple `TextButton` are separate pieces, and tightened the `contentPadding` so the link sits right next to the text like the wireframe.

### Text Fields

The register/login screens use `OutlinedTextField` throughout, which was already the right component, and they read their border/focus color from the theme. I mostly left these alone since my job here was color/badge alignment rather than reshaping every field.

### Other Components

- **Status badges** (`ui/components/StatusBadge.kt`, new): I pulled the badge out into one small reusable Composable that maps each `LibraryStatus` to its container background + saturated text color, then used it in both `LibraryScreen` (tap-to-change status) and `MyProfileScreen`. So now the same badge looks the same everywhere and actually shows status color.
- **Filter chips** (Search): selected state set to `primaryContainer` / `onPrimaryContainer` via `FilterChipDefaults.filterChipColors(...)` so the active chip is purple, not pink.
- **Library filters**: removed the ALL/BOOKS/MOVIES/etc. chip row entirely (the app only tracks a few types and it was cluttered), leaving the purple status segmented control — which also killed a stray pink highlight.
- **Bottom nav** (`BottomNavBar.kt`): white `containerColor`, `tonalElevation = 0.dp`, and `NavigationBarItemDefaults.colors(...)` with the selected icon/label in `primary`, indicator in `primaryContainer`, unselected in `onSurfaceVariant`.
- **Search rating** (`SearchComponents.kt`): split the rating line into a `Row` so only the `★ X.X` gets amber `tertiary`, and it only renders when `averageRating > 0f`. The "· Type · Year" stays gray.
- **Cards**: bumped the Library, Search, and Feed cards to `elevation = 2.dp` and a 12dp corner radius to match the spec (a couple were sitting at 1dp / 8dp).

---

## What Was Hard

The most confusing part was realizing how many places were pulling the **wrong theme slot** without me noticing, because pink and purple aren't that far apart on a phone screen. The search ratings were the clearest example — I assumed "rating is pink" was one hardcoded color somewhere, but it was actually that the whole rating string was inheriting a color it shouldn't have, *and* the star and the metadata were sharing one `Text`. Once I split them into a `Row` with two separate `Text`s, it was obvious only the star needed `tertiary`. So the fix wasn't "change a color," it was "restructure the Composable so the color can land on only the part that needs it."

The status badges were the other one. At first I was setting the badge background to the strong `WantTo` purple and the text was unreadable on it. That's when the spec's container idea clicked — the light `Container` color is the *background* and the saturated color is the *text*, not the other way around. Pulling it into a single `StatusBadge` Composable also meant I fixed it once instead of in two different screens that would've drifted apart.

---

## What You Understand Now

Before this, I kind of treated `MaterialTheme.colorScheme` as a bag of named colors and grabbed whatever looked right. What clicked is that the color roles are **paired** on purpose: `primary` goes with `onPrimary`, `primaryContainer` goes with `onPrimaryContainer`, and the "container" version is meant for a filled background behind content while the base version is meant for the content itself (text, icons, active nav, borders). So a highlighted pill should be `container` background + the saturated color as text — which is exactly the pattern the status badges needed. Once I understood that pairing, the component defaults (`NavigationBarItemDefaults`, `FilterChipDefaults`, `SegmentedButtonDefaults`) stopped feeling like magic — they're just slots asking which role you want in each spot, and if you feed them the matching pair it comes out looking like the spec.

The way I'd explain it to a pod mate: don't hardcode `Color(0xFF...)` in a screen. Define the value once in `Color.kt`, map it to a role, and in the UI reach for the *role* (or a small shared Composable like `StatusBadge`) so the whole app stays consistent and you can retheme in one place.

---

## Self-Assessment

| Section | Possible | My Estimate |
|:---|:---:|:---:|
| Color System | 13 | 12 |
| Typography | 5 | 3 |
| Component Styling | 15 | 11 |
| Navigation & Cards | 5 | 5 |
| Reflection | 12 | 11 |
| **Total** | **50** | **42** |

*One thing I think I did well:* The color system and the status badges. I got the exact spec hexes in, defined the status containers, applied them through one reusable `StatusBadge`, and cleared out the last hardcoded literal — so the "wrong slot / pink-vs-purple" problems that were all over the app are actually gone at the source, not just patched screen by screen.

*One thing I know I left incomplete or could have done better:* Typography and a few component *shapes*. I aligned colors, elevations, and card radii, but I didn't set the button 20dp radius or the text-field 8dp / search 28dp shapes explicitly, and my type weights don't fully match the 700/600 spec yet. Those are the honest remaining gaps.
