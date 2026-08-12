# Week 11 Reflection — Bonus Feature Sprint (Week 1 of 2)

**Name:** Kenan Port
**Date:** 07-30-2026
**My assigned bonus feature:** Write Review

---

## Commits This Week

**Link:** https://github.com/Zabzar22/media-tracker-android/commits/week-11

**Pull request:** https://github.com/fascineh1/media-tracker-android/pull/11

---

## Code Review

**Reviewed:** Issa Ismail Ali
**Link to my review:** https://github.com/Issa-Ismail-Ali/media-tracker-android/pull/10#issuecomment-5270108826

### What I Looked At

His week-11 PR, the first pass at Quotes. I went through `QuotesViewModel.kt` and `QuotesScreen.kt` mostly, since that's where the new state and the list itself live, and skimmed the `MediaDetailScreen.kt`/`MediaDetailViewModel.kt` changes to see how saving a quote from the detail page hooks in. This is later than I meant to get to it — there wasn't a `week-11` branch on his repo until after our own week-11 was basically wrapped, so this review is going up alongside the reflection instead of before it.

### What I Noticed

Two things I liked, one thing I flagged. `loadNextPageIfNeeded()` fires a few items before the actual bottom of the list instead of waiting for the very last item, which means the next page has a head start loading instead of showing a spinner right as you hit the end. Small touch, but it's the kind of thing that's easy to skip in a Week 1 pass and he didn't. The card styling on `QuoteCard` is clean too — reads well, nothing crowded.

The thing I flagged: in `loadNextPageIfNeeded()`, `current` gets grabbed once at the top of the function, and then after the fetch comes back the new state is built off of `current.copy(...)` instead of reading state fresh. If anything else changed `_uiState` while that request was in flight, this would write over it with the older snapshot. I said in the comment I'm not sure it can actually happen given how things are wired right now, and I didn't try to prove it — I flagged it because I ran into basically the same shape of bug in my own feature this week (the detail screen not knowing the reviews list under it had gone stale), so it was the first thing that jumped out reading someone else's ViewModel right after fighting my own version of it.

### Comments I Left

Opened with the two positives — the early pagination trigger and the card styling — before getting into the `current` staleness question, and was upfront that I wasn't certain it was a real bug rather than a "looks a little familiar" flag, since I don't have full context on how the rest of his state updates are sequenced. Closed the comment on a personal note since it's the last stretch of the class.

---

## Bonus Feature Progress

**What's working:**

To be clear about the scope up front: what I finished this week is Week 1's part of Write Review, and that's it. The feature as a whole isn't done — you can write a review and see it, but you can't change it or get rid of it once it's posted. All three of the Week 1 targets are working, and I tested each one by hand in the emulator against the live server rather than just reading my own code.

Here's the whole flow that works end to end right now: open a book from search, tap "Write a Review" on the detail screen, pick a star rating, optionally type up to 500 characters, hit Post, and the app comes back to the detail screen with your review at the top of the list with your username and the date on it. The header's average and review count update too. That's the piece I'd feel comfortable demoing. Everything past that point is next week.

- **`StarRatingRow(rating, onRatingChange)` is pulled out into its own composable**, in `ui/components/StarRating.kt`. Tapping star N calls `onRatingChange(N)`, and I don't compare N against the rating I already have before accepting it. That's what keeps tapping star 2 after star 5 from being ignored. The spec warns about this exact bug and I think the reason people hit it is that adding the guard feels like the natural thing to write. While I was in there I also moved the read-only star row into the same file. It used to be a `private fun StarRow` inside `MediaDetailScreen.kt`, so nothing else could call it, which is why the review cards and the header now use the same stars instead of two near-identical copies.
- **`POST /reviews` is wired up from the form.** It goes `CreateReviewRequest` → `ReviewApiService.createReview()` → `DefaultReviewRepository` → `WriteReviewViewModel.submit()`. The form has the star row, a 500-character text box with a counter that turns red at the cap, and a share-to-feed checkbox. Going back to the detail screen happens in a `LaunchedEffect(submitState)` that watches for `Success`, not in the button's `onClick`. The spec says to do it that way and I get why now — doing it on the tap would leave the screen before the server answered, so a post that failed would look like it worked.
- **The reviews list shows real data on Media Detail**, from `GET /reviews?mediaId={id}` in `MediaDetailViewModel.load()`. I didn't sort it. The server already orders by `created_at` descending and then by `id`, so newest-first is already done by the time I get the list and re-sorting it would just be a second place for that to go wrong.

A few small things off the Week 2 list came along for free while I was building the Week 1 stuff. I want to be careful not to oversell these — they're four small pieces, not a head start on Week 2, and the two big Week 2 items (edit and delete) are completely untouched:

- **The 409 is handled.** The repository throws `AlreadyReviewedException` and the form shows "You've already reviewed this." and stays put instead of crashing. I checked it by posting twice to the same book.
- **The character counter works.** The 500 cap is enforced in `onReviewTextChange()` rather than in the text field, so the number under the box and the text that would actually get sent can't disagree with each other.
- **Your own review sorts to the top** with a "Your review" label next to it, in `mineFirst()`.
- **The detail screen refreshes after you post**, so a review you just wrote shows up right away. This one turned out to be the interesting problem of the week, and it's what I wrote about below.

I also fixed two login bugs I ran into on the way. Sign-out was only navigating back to Login and leaving the old token sitting in memory, and logging in as someone new could inherit the last account's cached profile. Neither one is part of the feature, but both had to go before "is this review mine?" could give the right answer, and they only show up because the three of us test with our own accounts.

**What's still stubbed, fake, or not started:**

- **Edit and delete.** `PUT /reviews/{id}` and `DELETE /reviews/{id}` aren't touched yet — no API methods, no repository functions, no UI. That's most of Week 2.
- **The "Write a Review" button is still always showing.** The spec wants it hidden and replaced with "Edit" once you've reviewed something. I have everything I need to do that now, but I left it on purpose. Hiding it before Edit exists would mean there's no way at all to change a review you already posted, and I'd rather have a button that comes back with a 409 than no button.
- **The three list states.** Empty has text ("No reviews yet. Be the first!") but no button, and the spec asks for one. Loading doesn't have a state of its own — the reviews just come in with the whole-page spinner. Error isn't actually possible right now, because `load()` does `.getOrElse { emptyList() }` when the review fetch fails, so a dead connection and "nobody has reviewed this yet" produce the exact same screen. Telling those two apart means `Success` has to carry a smaller state for just the reviews list, and that's the one part of Week 2 that needs real restructuring instead of copying something I've already written.
- **No test yet.** The spec wants one, either a Compose test on `StarRatingRow` or a ViewModel test on submitting. `LibraryViewModelTest.kt` from Week 10 is a working template for the second one.
- **The Activity Feed still shows mock data**, so a review posted with the checkbox on doesn't appear there. The event itself is real — the server calls `createActivityRecord()` whenever `shareToFeed` is true — it's just that `ActivityFeedViewModel` never calls `GET /activity`. That isn't part of any of our three features or either week's target, so I left it alone.

**What I'm blocked on, if anything:**

Nothing on the feature. The code review was the one open item and it's done now, a bit later than I wanted — see the note above about waiting on the `week-11` branch to actually exist.

One smaller thing worth mentioning: I still can't build from the command line. There's no Gradle wrapper in the repo and only JDK 8 on my PATH, so everything happens inside Android Studio. That means everything I called "tested" above was tested by hand in the emulator, not by a test suite. That's fine this week and it stops being fine next week, when I owe you a test.

---

## One Thing I Understood More Deeply

The thing that taught me the most this week was a bug where nothing had actually broken.

I got `POST /reviews` working, watched Logcat come back with a `201` and a real review id, went back to the detail screen, and my review wasn't there. No error anywhere. If I left the screen and came back it showed up fine. My first thought was that I'd broken the refresh, which was wrong, because there wasn't a refresh to break.

The detail screen loads with `LaunchedEffect(mediaId) { viewModel.load(mediaId) }`. I'd been reading that for weeks as "run this when the screen shows up," and that isn't what it says. It's closer to "run this once, and again if `mediaId` changes." Going to Write Review pushes a new screen on top of the detail screen — the detail screen never leaves the back stack, its composition and its ViewModel are both still alive, and `mediaId` is the same number it always was. So as far as Compose is concerned nothing happened at all, and it's right about that. The screen that changed the data was a different screen.

Once I put it that way I could see the bigger version of the problem. The reviews list sitting in `MediaDetailUiState.Success` isn't the data, it's a copy of the data from one moment, and nothing anywhere tells that copy it's out of date. Week 10 gave me half of this already: with optimistic updates the screen is deliberately ahead of the server, and it knows it's ahead because it's the thing that made the change. This week is the other half, where the screen is behind the server and doesn't know, because something else made the change. Only one of those two situations announces itself.

My fix is `LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh(mediaId) }`. `refresh()` returns immediately if the page hasn't loaded yet, so it can't fire a second round of requests on the way in. It re-fetches the media as well as the reviews, because a new review also moves the average and the count up in the header. I only thought of that part because the stars in the header didn't change the first time I tested it.

The other reason this stuck with me is that it's the first bug all semester I couldn't solve by opening the professor's branch, because there isn't one this week. I had to work out why the effect wasn't re-running instead of comparing my version to one that already worked, and honestly that taught me what `LaunchedEffect`'s key actually does better than reading about it would have.

---

## One Thing I'm Still Confused About

Whether `ON_RESUME` is the real answer here or just the one that happened to work.

I can see that it's blunt. It refetches every single time the screen comes to the front, which includes coming back from Write Review but also coming back from the home screen, or from a notification, or after the phone locks. Most of those times nothing changed and I've spent two requests redrawing the same screen. It also feels a bit like luck, because it only works since Write Review is a screen you come *back* from. If a review could get posted somewhere that doesn't return you to the detail screen, the detail screen would go stale again and this fix wouldn't help.

I know there are at least two other ways to do it and I can't tell which one is normal. One is passing a result back through the nav controller's back stack entry, where the write screen sets something on its way out and the detail screen reads it. That's precise, but it means the detail screen has to know that a "someone posted a review" signal exists in the first place. The other is making the repository the single source of truth and having both screens watch the same flow, so posting a review updates the list without anybody having to tell anybody. That second one sounds like the actual answer to me, since it gets rid of the problem instead of patching around it, but it's a much bigger change than one line and I don't know if that's what you'd reach for in an app this size or if it's something you only build once several screens really need it.

The other thing I'm unsure about is `TokenStore`. It's a plain `object`, so it's a global with mutable state and now it holds `currentUser` on top of the two tokens, and my ViewModels read `TokenStore.currentUserId` straight out of it. It works, and pulling the user out of the `POST /tokens` response instead of calling `GET /users/me` saves a request on every detail screen, which I was happy about. But every ViewModel reaching into a global is exactly the thing I thought constructor injection was supposed to prevent, and I can already see it costing me something. `mineFirst()` decides what "mine" means by reading a singleton, so a unit test of it can't just hand in a user — it has to set a global first and remember to unset it afterward. In Week 10 I made the repositories injectable specifically so tests would have a seam, and then this week I put the user identity somewhere with no seam at all. I don't know if that's a real inconsistency I should fix before I write next week's test, or if session state is just the case where a singleton is considered fine.

---

## Anything Else

Two things I want to be upfront about.

**`GET /reviews` is paginated and I'm ignoring the pagination.** I found this reading the server rather than the spec. The reviews endpoint goes through the same paginated response helper that `GET /media` does, sends `X-Next-Cursor` and `X-Has-More`, and defaults to a limit of 20. My `ReviewApiService` returns `Response<List<Review>>` and never looks at any of that, so an item with 25 reviews would quietly show 20 with nothing on screen suggesting there are more. Nothing in our test data is anywhere near 20 so it doesn't bite right now. It's the same call I made about library pagination in Week 10, except that time I skipped it because your own `LibraryViewModel` built the page and then never used the cursor, and this time I skipped it because it was outside the Week 1 target, which is a weaker reason.

**I throw away the server's own 409 wording.** `DefaultReviewRepository` reads the error body and puts the server's message ("You have already reviewed this media item.") into `AlreadyReviewedException`, and then `WriteReviewViewModel` ignores it and uses `R.string.review_error_already_reviewed` instead. That's on purpose, since a `@StringRes` id keeps the wording in `strings.xml` and keeps the ViewModel testable without an Android context. But it does mean I wrote the code that pulls the message out and then didn't use the message. `MediaNotFoundException` has the same problem. I think the right version is the server's wording as a fallback with the string resource as the default, but I haven't written that.

**On working with my pod.** Since all three of us are building different features onto the same app and they'll have to live together eventually, I wrote up `7-30-notes.md` in the repo root. It lists the files all three of us are likely to touch (`strings.xml`, `RetrofitInstance.kt`, `Routes.kt`, `NavGraph.kt`) and which of my files are shared rather than review-specific — `StarRating.kt` isn't tied to reviews, and `Response<*>.errorMessage()` works for any repository that wants the server's own error text instead of a status code. Reviews didn't touch `Routes.kt` or `NavGraph.kt` at all, because the `write_review/{mediaId}` route already existed from Week 8, so those two files are clear for Quotes and Priorities. Quotes especially is the same shape as my feature — a form, a POST, and a list — so `WriteReviewScreen` and the `SubmitState` pattern should carry over pretty directly. I'd much rather Issa copy mine than rebuild it from scratch during finals week.

---

## Rubric

*You don't need to self-assess — this is here so you know what I'm looking at.*

| Section | Points | Full Credit | Half Credit | No Credit |
|:---|:---:|:---|:---|:---|
| **Reflection** | 10 | Concrete progress report (what's wired, what's not) plus specific, honest "Understood More Deeply" and "Still Confused" sections. | Present but vague — "I worked on my feature" with no specifics on what's actually working. | Missing or one-word answers. |
| **Code Review** | 10 | Specific observation about the code with explanation of why it matters (or a substantive positive comment). Link to review present and verified. | A question or comment that shows you read the code, but lacks explanation. | "Looks good!" or equivalent. Missing link. Review not found on GitHub. |
| **Total** | **20** | | | |

**A note on the code review score:** I check that the review actually exists on GitHub before grading. The written summary here and the GitHub comment should match.