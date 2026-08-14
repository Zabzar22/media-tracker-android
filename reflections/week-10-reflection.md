# Week 10 Reflection

**Name:** Kenan Port
**Date:** 07-23-2026
**Updated:** 07-29-2026

---

## Commits This Week

**Link:** https://github.com/Zabzar22/media-tracker-android/commits/week-10

**Pull request:** https://github.com/Zabzar22/media-tracker-android/pull/11

---

## Code Review

**Reviewed:** Samba Kamara
**Link to my review:** https://github.com/fascineh1/media-tracker-android/pull/10#issuecomment-5120655868

### What I Looked At

His week-10 PR (#10), four commits covering the library and favorites endpoints, the profile and detail updates, and a rollback test at the end. I focused on `LibraryViewModel`, `MediaDetailViewModel`, `DefaultMediaRepository`, and `LibraryViewModelTest`, since the point of this week was making the taps optimistic and proving the rollback actually works.

### What I Noticed

Two things I liked before the one thing I'd change. He put his repository behind a `MediaRepository` interface, which meant his test could mock the interface instead of the concrete class — I skipped that step and ended up mocking `DefaultLibraryRepository` directly, so his is the cleaner version of the same idea. The other one is in his `loadLibrary()`: he saves the selected status before the request and checks it still matches before writing the result. Neither I nor the professor did that, and it means a slow request for one tab can't land on top of a tab you already switched away to.

The thing worth changing is in `MediaDetailViewModel.load()`. His repository already turns a 404 into `null` for both the library check and the favorite check, so "not added" and "not favorited" are handled correctly. But all three `async` calls get awaited inside one `try`, and the two side calls aren't treated as optional. So if the favorite check fails for a real reason — an expired token, a 500, a dropped connection — the whole detail page goes to the error state even though `GET /media/{id}` came back fine. There's the coroutine problem underneath it too: an `async` that throws cancels its siblings, and the exception can get past the `try` you wrapped around `await()`. That's the exact thing I said I had no intuition for in my week-08 reflection, so it was strange to be the one pointing at it this time.

### Comments I Left

I opened with the interface and the stale-tab guard, and was specific that his mocking setup was cleaner than mine rather than just saying it looked good. Then I raised the `load()` issue and explained the consequence instead of only naming it — that a failed favorite check takes down a page whose media call succeeded — and mentioned the sibling-cancellation part as the reason a `try` around `await()` isn't enough on its own. I suggested the fix I used, which is `runCatching` inside each of the two side `async` blocks and then `.getOrNull()` on them, so only the media call can fail the screen. I closed by saying a good chunk of what the professor's branch has this week reads like stretch goals, so I wouldn't worry about whatever he left out.

---

## One Thing I Understood More Deeply

I started this week with the safe version: tell the server, wait, then call `loadLibrary()` again and draw whatever came back. It worked, and it couldn't get out of sync, because the screen never held an opinion the server hadn't confirmed. What I didn't appreciate is that "correct" was doing all the work there, and what it cost was a spinner flash on every single tap.

Going optimistic meant giving that up on purpose, and the part that actually clicked is that the list on screen stops being a copy of the truth and becomes a prediction of it. Once it's a prediction you need two things you never needed before: a saved copy of what you're about to destroy, and a path back to it. That's all `rollBack()` is in my `LibraryViewModel` — grab the item first, filter it out, and if the request throws, put it back and say why. I'd been thinking of optimistic updates as a speed trick, and they're really a bookkeeping problem where speed is the payoff.

The test is what made that concrete instead of just something I'd typed. In `removeItem takes the item out right away` I deliberately do *not* call `advanceUntilIdle()` before asserting, because the whole claim is that the row is gone before `DELETE` has even run. Move that one line up and the test still passes, but it stops testing anything. The other thing I got out of writing it is that constructor injection wasn't a style preference — my ViewModel built its own `DefaultLibraryRepository()` inside the class, and that doesn't make the test hard, it makes it impossible, because there's no seam to put a fake through. So the `@JvmOverloads constructor` with a default was a prerequisite, not a cleanup.

---

## One Thing I'm Still Confused About

What happens when two optimistic actions are in flight at once. My `rollBack()` appends the backup to whatever the list currently is, so if I remove A and then remove B and A's request fails, A comes back onto a list that has already lost B — which I think is right. Samba restores the whole `previousItems` snapshot he captured before his tap, and if I'm reading it correctly that would put B back too, undoing a removal that actually succeeded. I only spotted the difference because I was reviewing his code right after writing mine, and I genuinely don't know which one is the accepted answer, or whether real apps just serialize the requests so the situation can't come up in the first place.

The same question shows up on the favorite button. I removed the `isUpdatingFavorite` flag when I went optimistic, since disabling the button defeats the point of it flipping instantly, but that means a fast double-tap fires `POST` and then `DELETE` and the server settles on whichever one lands last. I think that's fine, because the heart already shows the state the user asked for either way. I can't prove it though, and I don't have a way to test it that isn't just tapping quickly and hoping.

---

## Anything Else

Scope notes, so it's clear what's mine and what isn't. Beyond the handout I added `DELETE /favorites/{mediaId}`, because the Save button had no way to undo itself, and a favorites list on the profile screen, because otherwise saving something had no visible result anywhere except the button that saved it. The profile around it is still mock data, so it's a real list sitting inside a fake profile.

I kept my full-screen error and Retry for a failed `GET /library`, and added a separate `actionError` for taps that got rolled back, which go to a snackbar instead. That split fixed something I'd shipped without noticing: a failed `DELETE` used to set the same `errorMessage` the loader uses, so one failed removal replaced a library that had loaded perfectly fine with an error screen.

I skipped one thing the professor has, which is library pagination — the `LibraryPage` wrapper that reads `X-Next-Cursor` and `X-Has-More`. I left it out because his own `LibraryViewModel` builds the page and then only reads `page.items`; the cursor and `hasMore` never get used, so copying it would have been scaffolding that does nothing.

Two honest ones. My snackbar messages are hardcoded English strings inside the ViewModels instead of living in `strings.xml`, which is inconsistent with how the rest of this project handles text — a plain `ViewModel` has no `Context`, and I took the shortcut rather than passing the mapping back through the screen. And the copy of the API repo I have locally doesn't have a `/favorites` route in it at all, so I couldn't check that contract against the source the way I did for `/library`. I went off the professor's Android branch instead and confirmed he calls the same endpoints I do, which is good enough to build against, but it isn't the same as reading the server code.

Also still true and still not from this week: search result cards never check `coverUrl`, so they always show the placeholder icon even though the server sends a real cover. Library, detail, and now the profile favorites all check it. It's only `SearchComponents.kt`.

---

## Rubric

*You don't need to self-assess — this is here so you know what I'm looking at.*

| Section | Points | Full Credit | Half Credit | No Credit |
|:---|:---:|:---|:---|:---|
| **Reflection** | 10 | Specific, honest responses to "More Deeply" and "Still Confused" sections. Shows genuine thinking — not just "I learned X." | Responses are present but vague or generic ("I got better at Compose"). | Missing or one-word answers. |
| **Code Review** | 10 | Specific observation about the code with explanation of why it matters (or a substantive positive comment). Link to review present and verified. | A question or comment that shows you read the code, but lacks explanation. | "Looks good!" or equivalent. Missing link. Review not found on GitHub. |
| **Total** | **20** | | | |

**A note on the code review score:** I check that the review actually exists on GitHub before grading. The written summary here and the GitHub comment should match. If the review isn't there, the written summary can't earn credit.
