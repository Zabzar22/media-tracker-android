# 7/30 Notes — Write Review bonus feature (Week 1)

Branch: `week-10` · Feature: **Reviews** (Issa = Quotes, Samba = Priorities)
Spec: `ICS342/Bonus-feature/bonus-feature-reviews.md`

---

## Am I done with Week 1?

Yes. All three Week 1 targets are met.

| Week 1 target | Where it lives |
|---|---|
| `StarRatingRow(rating, onRatingChange)` extracted as its own composable, tapping star N sets rating = N | `ui/components/StarRating.kt` |
| `POST /reviews` wired from the form | `CreateReviewRequest` → `ReviewApiService` → `DefaultReviewRepository` → `WriteReviewViewModel` |
| Reviews list on Media Detail with real data | `MediaDetailViewModel.load()` + `ReviewCard` in `MediaDetailScreen.kt` |

Both spec hints are handled:

- **The classic star bug is avoided.** `onRatingChange(star)` is called with the star's number and nothing is compared against the current rating first, so tapping 2 after 5 drops you to 2. If anyone "fixes" this by adding a guard, they've reintroduced the bug the spec warns about.
- **Navigation happens on the result, not the tap.** A `LaunchedEffect(submitState)` watches for `Success` and calls `onNavigateBack()`. Doing it in the button's `onClick` would leave the screen before the server answered, and a failed post would look like it worked.

Four Week 2 items also came in early because they were cheap: the 409 handling, the character counter, "your review sorts first", and the detail screen refreshing after a post.

---

## Everything that changed, and why

### New files (2)

**`ui/components/StarRating.kt`**
Two composables, both stars, one file so there's a single place to change how a star looks.

- `StarRatingRow(rating, onRatingChange)` — five **tappable** stars. This is the extraction the spec asks for.
- `StarRatingDisplay(rating: Float)` — **read-only** stars, supports half stars. This used to be `private fun StarRow` inside `MediaDetailScreen.kt`, so nothing else could use it. Moving it here is why the detail header and the review cards now draw the same stars.

Why two functions instead of one: input is an `Int` (you pick 1–5 whole stars), display takes a `Float` because `media.averageRating` is a float — 47 people rating a book gives you 4.3, and that's what needs a half star. A single person's rating is always whole, so the half-star branch never fires on a review card.

**`data/network/CreateReviewRequest.kt`**
The POST body. Same shape as `AddLibraryItemRequest` next door. `reviewText` is nullable because a rating alone is a valid review, and a blank box is sent as `null` rather than `""`.

### Rewritten from the professor's stubs (2)

**`ui/review/WriteReviewViewModel.kt`**
Was a single `rating` StateFlow. Now holds `media`, `rating`, `reviewText`, `shareToFeed`, and a `SubmitState` sealed interface (`Idle` / `Submitting` / `Success` / `Error`).

- Errors carry a `@StringRes` id, not a string, so wording stays in `strings.xml` and the class stays testable without an Android context. Same approach as `AuthViewModel`.
- The 500-char cap is enforced **here**, not in the text field, so the counter under the box and the string we'd actually send can never disagree.
- `submit()` refuses to run below `MIN_RATING`, and returns early if a submit is already in flight — a fast double-tap would otherwise send two requests and the second would come back 409.

**`ui/review/WriteReviewScreen.kt`**
Was a "not implemented yet" box. Now: media summary, star row, 500-char field with counter (turns red at the cap), share-to-feed checkbox (checked by default), Post button disabled until a star is picked.

`shareToFeed` is an explicit checkbox rather than accepting the server default. The spec says to decide deliberately — sending it means the request states what the user chose instead of relying on a default we don't control.

### Edited (10)

| File | What changed |
|---|---|
| `ReviewApiService.kt` | Added `POST /reviews`, returns `Response<Review>` |
| `DefaultReviewRepository.kt` | Added `createReview()`, reads the 409 before the generic failure check |
| `ApiError.kt` | Added `AlreadyReviewedException`; promoted `errorMessage()` from a private helper into a shared extension |
| `DefaultMediaRepository.kt` | Uses the shared `errorMessage()` instead of its own copy |
| `MediaDetailViewModel.kt` | Added `refresh()`, `mineFirst()`, `currentUserId` on the Success state |
| `MediaDetailScreen.kt` | Refresh on `ON_RESUME`, "Your review" label, uses the shared star row |
| `TokenResponse.kt` | Added the `user` field the server was already sending |
| `DefaultUserRepository.kt` | Stores the user at login; clears the old session first |
| `TokenStore.kt` | Holds `currentUser`; added `clear()` |
| `SettingsScreen.kt` | Sign-out now calls `TokenStore.clear()` |
| `strings.xml` | 12 new strings, all in one block |
| `LibraryViewModel.kt` | Removed two dead imports (unrelated cleanup) |

### Three decisions worth knowing about

**1. Posting a review now actually shows up.**
`LaunchedEffect(mediaId)` does *not* re-run when you come back from Write Review — the detail screen never left the back stack, so as far as Compose is concerned `mediaId` never changed. Without a fix, a review you just posted stays invisible until you navigate away and back. `MediaDetailScreen` now calls `viewModel.refresh(mediaId)` on `ON_RESUME`. `refresh()` returns immediately unless the page already loaded, so it can't double-fetch on the way in. It re-fetches the media too, because a new review moves the average and the count in the header.

**2. 409 gets its own exception type.**
`AlreadyReviewedException`, same idea as the existing `MediaNotFoundException`. The server sends 409 when you've already reviewed an item — that's the one-review-per-item rule working, not a breakage. The form shows "You've already reviewed this" and stays put instead of crashing.

**3. Two session bugs fixed along the way.**
Sign-out only navigated to Login; the old token sat in memory. And a new login could inherit the previous account's cached profile. Both matter specifically because we each test with our own account — without these, "is this my review?" answers for whoever logged in last.

---

## Live data vs. mock data — where we actually stand

I grepped `FakeMediaRepository` on our branch and on `prof/10-optimistic-updates-and-library-polish`. **The footprint is identical** — same six screens, same lines. The professor has *not* moved off mock data at this point, so neither should we.

| Screen | Us | Professor |
|---|---|---|
| Search results, Media Detail, My Library, Favorites, Reviews | **live** | **live** |
| Activity Feed | mock | mock |
| Connections (followers / following) | mock | mock |
| Profile / Edit Profile / other users' profiles | mock | mock |
| Search "popular" row | mock | mock |
| Settings email | mock | mock |

**Note:** My Library is already live — it uses `DefaultLibraryRepository` against `GET /library`. The screen still showing fake data is the **Activity Feed**, which is a different tab.

**Why a posted review doesn't appear on the Feed.** `ActivityFeedViewModel` sets `_feedItems.value = FakeMediaRepository.activityFeed` and `GET /activity` is never called anywhere in the app. But the event *is* real — the API's `reviews` function calls `createActivityRecord(...)` whenever `shareToFeed` is true. The data is on the server waiting; only the screen isn't reading it. Wiring it is not part of any of our three features and isn't in either week's target, so we're leaving it.

**One divergence I found and removed.** I had added a `ProfileApiService` + `DefaultProfileRepository` calling `GET /users/me` to find out who's signed in. The professor never calls that endpoint — `POST /tokens` already returns the full `user` object alongside the tokens, and he just saves it from the login response. Both files are deleted and `TokenResponse` now has the `user` field the server was already sending. Net effect: one fewer network call per detail screen, one fewer API service, and we're using exactly the endpoints he uses.

---

## How to work off this code

### Files you can reuse directly

- **`ui/components/StarRating.kt`** — shared, not feature-specific. If a quote card or a priority row wants stars, call these rather than writing new ones.
- **`ApiError.errorMessage()`** — `Response<*>.errorMessage()` pulls the server's `{ "message": ... }` out of an error body. Use it anywhere you want the server's own wording instead of a bare status code.
- **`WriteReviewScreen` + `WriteReviewViewModel`** — the Quotes feature is the same shape (form → POST → list). The `SubmitState` pattern and the `LaunchedEffect`-navigates-on-success setup port over directly.

### Conventions this code follows

These are the existing house rules, not new ones. Matching them keeps our diffs readable:

1. API interfaces return `Response<T>`; the repository decides what a non-2xx means.
2. Status codes that are *answers* get their own exception type (`MediaNotFoundException`, `AlreadyReviewedException`). Codes that mean "not yet" (404 on favorites) return `null` instead of throwing.
3. ViewModels take repositories as constructor parameters with defaults, plus `@JvmOverloads` — so `viewModel()` works with no arguments *and* a test can pass mockk fakes in.
4. Screen-level failure → the `UiState` sealed interface. Action-level failure → a separate `actionError` StateFlow shown in a snackbar and cleared by the screen.
5. Optimistic writes: change the state, fire the request, roll back and snackbar on failure.
6. All user-facing text goes in `strings.xml`. No inline strings.

### Merge conflict hot spots

Four files all three of us will touch. To keep diffs from overlapping, **append to the end of your own commented block** rather than inserting mid-file:

- `strings.xml` — review strings are grouped at the bottom under their own comment. Add a `<!-- Quotes -->` / `<!-- Priorities -->` block after it.
- `RetrofitInstance.kt` — one `val yourApiService` line each, at the end of the authed section.
- `Routes.kt` and `NavGraph.kt` — **Reviews touched neither.** The `write_review/{mediaId}` route already existed from Week 8 and `onNavigateBack` was enough. Both files are free for you two.

### One thing not to "fix"

`MediaDetailUiState.Success` gained a `currentUserId` field and the reviews list is pre-sorted by `mineFirst()`. If you add a quote button to the detail screen, don't restructure `Success` — the Week 2 three-states work needs it and I'll be changing it next week.

### Watch out for

`LibraryViewModelTest` may fail with **"Method w in android.util.Log not mocked"**. That's not the test being wrong — `Log.w()` doesn't exist in a plain JVM unit test. The fix goes inside the `extensions.configure<ApplicationExtension>` block in `app/build.gradle.kts`:

```kotlin
testOptions {
    unitTests.isReturnDefaultValues = true
}
```

It's deliberately not committed, since it isn't needed until someone runs that test.

---

# Going into next week (Week 2 — demo-ready)

## Already done, pulled forward from Week 2

These are off the list — don't redo them:

- ✅ **409 handled** — `AlreadyReviewedException`, shows "You've already reviewed this", no crash
- ✅ **Character counter** — with the cap enforced in the ViewModel so it can't disagree with what's sent
- ✅ **Current-user identity** — comes free from the login response, no extra call. This was the blocker for everything below.
- ✅ **"Your review" sorts to the top** with a label. That's the foundation of requirement 4.
- ✅ **Detail screen refreshes after a post**, so a new review is visible immediately.

## Still to do

**1. Edit — `PUT /reviews/{id}`**
API method, repo function, and a way to open the form pre-filled. Plan: reuse `WriteReviewScreen` with an optional `reviewId` rather than building a second screen. **The `id` you pass is `review.id`, not `review.mediaId`** — the spec calls this out specifically, and both are integers sitting next to each other in the response.

**2. Delete — `DELETE /reviews/{id}` + confirm dialog**
Returns `204 No Content`, so `Response<Unit>` — same shape as `removeFromLibrary`. `LibraryScreen.kt` already has the exact `AlertDialog` to copy.

**3. Hide "Write a Review" once you've reviewed, show "Edit" instead**
Now unblocked. It's `reviews.firstOrNull { it.userId == TokenStore.currentUserId }`. I deliberately left the Write Review button visible this week — hiding it before Edit exists would leave no way to change a review you'd already posted.

**4. Three states on the reviews list — the real work**
- *Empty* — the text exists ("No reviews yet. Be the first!") but there's **no button**, and the spec asks for one.
- *Loading* — reviews currently load as part of the whole-page spinner, with no state of their own.
- *Error* — **not currently possible.** `load()` does `.getOrElse { emptyList() }` on a failed review fetch, so a network failure looks identical to "no reviews yet." `Success` needs to carry a reviews sub-state to tell those apart. This is the piece that needs actual restructuring.

**5. One test**
The spec says "a Compose UI test on `StarRatingRow` **or** a ViewModel test on the submit flow" — it's an `or`, so **one** satisfies it. The ViewModel one is much easier than the Compose one. `LibraryViewModelTest.kt` is a working template: same `StandardTestDispatcher` setup, same `mockk` repository.

## Suggested order

1. PUT + DELETE plumbing (small, copies existing patterns)
2. Hide/show the Write vs. Edit button (small, unblocked now)
3. Delete confirm dialog (copy from `LibraryScreen`)
4. Edit UI (medium — the real work)
5. Three states (medium — needs the `Success` restructure)
6. One test

Roughly two genuinely new things and four copy-the-pattern things, so Week 2 should be *lighter* than this week. Week 1 was the expensive one: a screen from scratch, a new shared component, and a new submit-state pattern.

## Verified working (tested 7/30, live API, emulator)

Week 1 was confirmed end-to-end on a real device against the live server:

- Login succeeds and `TokenResponse` decodes the `user` object — no `GET /users/me` anywhere in Logcat.
- Tapping star N sets the rating to N, downward as well as upward.
- The text field stops at 500 characters and the counter turns red at the cap.
- A posted review appears immediately on Media Detail, with the `@username` and the "Reviews (N)" count both correct.
- Posting a second review for the same item is rejected with the "You've already reviewed this" snackbar — the 409 path — and does not crash.

Known-and-fine: the Profile tab still shows mock data. That matches the professor's branch and isn't part of any of our three features.

## Risks / things to know

- **Nothing was compiled from the command line.** No Gradle wrapper in the repo and only JDK 8 on PATH — builds happen in Android Studio. The verification above was done by hand in the emulator, not by a test suite.
- **`TokenResponse` now requires `user`.** The server sends it on every `POST /tokens` and all four required `UserProfile` fields are present in `formatUser`. If it ever *did* fail to decode, `DefaultUserRepository` only catches `IOException`, so a `SerializationException` would escape as a crash on the Login tap rather than an error message. Worth knowing as a symptom.
- **Tokens expire after ~30 minutes** and nothing is persisted, by design — so expect to log in on every run. This is deliberate: we each test with our own account, and persistence would keep the app signed in as whoever logged in last.
- **The character counter hard-stops at 500** rather than letting you overrun into a negative count. The cap lives in `WriteReviewViewModel.onReviewTextChange()`, which is what makes the counter and the sent payload incapable of disagreeing. The spec lists "character counter polish" under *not required yet*, so this is a deliberate choice, not an oversight.
