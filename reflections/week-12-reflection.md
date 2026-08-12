# Week 12 Reflection — Bonus Feature Sprint (Week 2 of 2, Final)

**Name:** Kenan Port
**Date:** 08-12-2026
**My assigned bonus feature:** Write Review

---

## Commits This Week

**Link:** https://github.com/Zabzar22/media-tracker-android/commits/week-12

**Pull Request:** https://github.com/Zabzar22/media-tracker-android/pull/13
---

## Code Review

**Reviewed:** Samba Kamara
**Link to my review:** https://github.com/fascineh1/media-tracker-android/pull/12#issuecomment-5270198729

### What I Looked At

His week-12 commit on Priorities, mainly the changes to `PrioritiesSection.kt` and the two new tests in `PrioritiesViewModelTest.kt`. I'd already left a comment on his week-11 PR about the drag handle being a small target to hit, so I went in this time specifically to see what changed about the drag gesture, plus whatever else moved between the two weeks.

### What I Noticed

The main thing I noticed is a good fix: the drag gesture moved off the little `DragHandle` icon and onto the whole `Card`, and the threshold for how far you have to drag before it registers dropped from 80f to 35f. Both of those directly answer what I'd flagged the first time around — a bigger touch target and a lower threshold should make it noticeably less stiff to actually grab and reorder something.

The thing I flagged going the other direction: it looks like `onClick = onEdit` came off the `Card` when the drag gesture moved onto it, and I couldn't find `onEdit` getting called anywhere else inside `PrioritiesSection.kt` anymore — the parameter's still there, it's just not wired to anything in that file. I said in the comment I wasn't fully sure, since I didn't build this part of the app and reading someone else's Compose code for the first time is harder than reading my own, but if tapping a card used to open the priority editor and drag ate that tap on the way in, that would explain it cleanly. I didn't go dig through `LibraryScreen.kt` to check whether there's a second way in from there — I wanted to leave the comment as a "worth a quick check" rather than a confirmed bug, since I hadn't traced the whole call chain.

### Comments I Left

Opened by connecting it back to my week-11 comment that I previously left — that moving the drag onto the whole card and lowering the threshold both directly address what I'd raised about the handle being small and stiff. Then raised the missing `onClick = onEdit` as something I noticed but wasn't certain about, and explained my reasoning (drag gesture eating the tap) rather than just saying "this looks broken." Closed with a note since this is close to the end of the semester.

---

## Bonus Feature — Final Status

**What works end-to-end, right now:**

The whole loop works now, not just the Week 1 slice. Open a book, write a review, see it on the detail screen with your name on top — same as last week — but now you can also tap Edit on your own review, land back in the same form with your rating and text already filled in, change either one, and save it as a `PUT` instead of a `POST`. Delete works too, straight from the review card with a confirm dialog first, and the review actually disappears without you needing to leave the screen.
I have yet to merge to see if they all work together, but from my understanding that wasn't part of this week's work.

- **One screen does both jobs.** `WriteReviewViewModel.load()` calls `GET /reviews?mediaId=&userId=` on the way in — the `userId` filter isn't documented in the spec, I found it reading the server — and if that comes back with something, `fillFormFrom()` drops it into the form and remembers the review's id. From then on `submit()` checks whether that id is null to decide `POST` vs `PUT`. The screen itself never has to know which mode it's in; it just calls `submit()` and watches `submitState`, same as Week 1.
- **The 409-into-edit-mode case is handled.** If `load()`'s lookup fails or misses (bad connection, or the review got written somewhere else in the meantime) and you post anyway, the server still comes back 409. `submit()` catches `AlreadyReviewedException`, goes and fetches the review that already exists, fills the form from it, and shows the error. Tap Save again and it's a `PUT` this time. I tested this by killing my connection right as `load()` ran and then posting anyway.
- **Delete is optimistic**, same pattern as Library remove from Week 10. `MediaDetailViewModel.deleteReview()` pulls the card off the list and drops the header's review count by one before the request goes out, and if `DELETE /reviews/{id}` fails it puts the card back at the top with `mineFirst()` and bumps the count back up. A 404 on delete — already gone, maybe from a double tap — counts as success rather than an error, since the end state you wanted is already true.
- **The three list states are actually three states now.** `MediaDetailUiState.Success` carries a `reviewsFailed: Boolean` alongside the reviews list, set from whether the `GET /reviews` call in `load()`/`refresh()` came back a failure. An empty list with `reviewsFailed = false` shows "Be the first to review this." with a button; the same empty list with `reviewsFailed = true` shows an error string instead. Last week those two were the same screen because `.getOrElse { emptyList() }` threw the failure away before anyone could ask about it.
- **The "Write a Review" button hides once you've reviewed**, the piece I deliberately skipped last week. `MediaDetailContent` finds `reviews.firstOrNull { it.userId == currentUserId }`; if that's non-null the header button doesn't show, and Edit/Delete appear on your own card instead. Everyone else's card gets neither button, since the server would 403 an edit or delete on a review that isn't yours and not showing the button is a nicer way to find that out.

**Tests written for this feature:**

`WriteReviewViewModelTest.kt`, two tests against mocked repositories. One posts a new review and checks `createReview` gets called and `updateReview` doesn't. The other pre-loads an existing review, checks the form actually filled itself in from it, then saves and checks `updateReview` got called with the review's own id — not the media id — since those are the two ints sitting next to each other in a `Review` that are easy to swap by accident.

**Known gaps or rough edges going into demos:**

- **Still not merged with Quotes or Priorities.** My commits say "waiting for podmates' respective portions to merge and finalize," and that's still true as of tonight — this is my branch in isolation, not the combined app.
- **`GET /reviews` is still not paginated.** Same note as last week, still unaddressed. An item with more than 20 reviews would quietly cut off with no indication there's more.
- **The Activity Feed is still mock data.** A review posted with "share to feed" checked still doesn't show up there, same as last week.
- **The `userId` filter on `GET /reviews` isn't in the written spec.** It works against the live server and I tested it, but I'm relying on server behavior I found by reading rather than something documented, so if that ever changes on the API side this is the first thing that would quietly break.
- **The two-buttons-side-by-side layout on the review card hasn't been checked against a long username or a long review** — it's fine with the test accounts we've been using, but I haven't tried it with something that would actually wrap or crowd the row.

---

## One Thing I Understood More Deeply

Looking back at both weeks together, the thing that actually shifted is that "the screen might not know the server changed" isn't a one-off bug I fixed last week, it's a category, and this week gave me a second, different-shaped example of it to compare against the first.

Last week's version was passive; the detail screen just sat there not knowing a review had been posted underneath it, and `LifecycleEventEffect(ON_RESUME)` was the fix, because coming back from Write Review is the one thing I could reliably hang a refetch off of. This week, with delete, I didn't reach for that pattern at all, and I think that's actually the more useful realization than the delete code itself. Delete happens on the same screen as the thing it's changing — you tap Delete on a card that's already visible, so there's no "come back to a screen" moment for `ON_RESUME` to catch. The optimistic update from Week 10 is the right tool there instead: pull the card off the list yourself, since you're the one making the change, and only fall back to a network round trip if the delete actually fails. Edit is closer to last week's shape again, since saving an edit navigates back to the detail screen the same way posting a new review does, so `ON_RESUME` catches that one for free without me having to think about it a second time.

So the same underlying problem arose though, a screen's copy of the data getting out of sync with the server — has at least two different right answers depending on whether the screen making the change is the one that has to redraw, or a different screen entirely. I didn't have language for that distinction going into this week. I just had "the ON_RESUME thing" as a fix I'd found once, and now I think of it as one tool out of at least two, picked based on which side of the screen boundary the change happens on.

---

## One Thing I'm Still Confused About

Whether the delete flow is doing more work than it needs to. `deleteReview()` in `MediaDetailViewModel` does the optimistic removal, sends `DELETE /reviews/{id}`, and then — on success — calls `refresh(mediaId)` on top of that, because deleting your review also moves the average rating and I don't know what the new average is without asking the server again. So a successful delete is actually two network calls back to back, the `DELETE` itself and then a full `refresh()` that re-pulls both the media and the reviews list. It works, and I tested it, but it feels like I'm reaching for the blunt "just ask again" tool a second time in the same method where I already did the sharper optimistic-update thing once.

I can think of a leaner version — the server could hand back the new average and count in the `DELETE` response body, or I could recompute the average myself client-side from the reviews I already have in memory minus the one I just removed — but I don't know if either of those is actually better or just different. The client-side math version means the header's number is only ever as good as my own arithmetic and never actually confirmed against the server, which feels like exactly the kind of thing that quietly drifts wrong over time in ways a demo wouldn't catch. I don't have a strong intuition yet for when "just refetch, it's simpler" stops being the right call and starts being wasteful.

---

## Anything Else

On the two-week format as a whole: having a full week between "here's the spec" and "here's the demo" with no professor branch to check my work against was a different kind of hard than the earlier weeks. Most weeks this semester, if I got stuck I had somewhere to go look — even just to confirm I was pointed in a reasonable direction. This sprint, the closest thing I had to that was my own week-11 reflection and whatever I could work out by reading the server. I think that's actually why the `ON_RESUME` fix stuck with me as much as it did — I had to actually reason about what `LaunchedEffect`'s key does instead of pattern-matching to something that already worked.

Being assigned a feature instead of picking one worked out fine for me specifically, since Write Review ended up being the one closest in shape to what Quotes needed too, which meant the pattern I built in Week 1 was worth writing up and handing off rather than something only I'd ever use. I don't know if it would've felt as fine if I'd landed on Priorities instead, which looks like it had a rougher road with the drag gesture across both weeks based on what I saw reviewing Samba's PRs.

Last thing: both code reviews this sprint landed later than I wanted relative to when the work actually happened, since neither pod mate had a `week-11` branch up yet when I was writing my own week-11 reflection, and this week's review is going up the same night as the reflection instead of ahead of it. Nothing to do about it at this point except be honest about the timing here, which is what the "What I Looked At" sections in both weeks say.

---

## Rubric

*You don't need to self-assess — this is here so you know what I'm looking at.*

| Section | Points | Full Credit | Half Credit | No Credit |
|:---|:---:|:---|:---|:---|
| **Reflection** | 10 | Honest final-status report — what works end-to-end, what's rough, what's tested — plus a specific, genuine "Understood More Deeply" that reflects on the sprint as a whole, not just this week. | Present but vague, or only reports on this week rather than the feature's overall state. | Missing or one-word answers. |
| **Code Review** | 10 | Specific observation about the code with explanation of why it matters (or a substantive positive comment). Link to review present and verified. | A question or comment that shows you read the code, but lacks explanation. | "Looks good!" or equivalent. Missing link. Review not found on GitHub. |
| **Total** | **20** | | | |

**A note on the code review score:** same as every other week — I check the link before grading.