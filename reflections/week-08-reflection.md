# Week 08 Reflection

**Name:** Kenan Port
**Date:** 07-14-2026

---

## Commits This Week

**Link:** https://github.com/Zabzar22/media-tracker-android/commits/week-08

---

## Code Review

**Reviewed:** Samba Kamara
**Link to my review:** https://github.com/fascineh1/media-tracker-android/pull/9#issuecomment-4972957325

### What I Looked At

His week-08 PR, which starts the Write Review screen. It's one commit touching `WriteReviewViewModel.kt` and `WriteReviewScreen.kt`. I focused on the ViewModel, since that's where the actual work is this week — the three StateFlows (`rating`, `reviewText`, `shareToFeed`) and their change handlers — and then read the screen to see how much of it was wired up.

### What I Noticed

The main thing was `_shareToFeed = MutableStateFlow(false)`. The stub comment sitting directly above his code says the checkbox is supposed to be checked by default, so the default is backwards. It matters more than it looks, because nothing would appear broken: you'd post a review, it'd go through fine, it just wouldn't land in the activity feed. So you'd probably end up digging through the feed code before it occurred to you to check a default value. It's the kind of thing I only caught by re-reading the requirement instead of the code, which is not something I'd have thought to do a few weeks ago.

The other thing is that the screen collects all three flows with `collectAsState()` but still renders the "not implemented yet" placeholder, so none of that state reaches the UI yet. His commit is called "Start Week 8 write review screen," so that reads as honest work-in-progress rather than something broken — the ViewModel is ready and the screen just hasn't caught up to it.

### Comments I Left

The main thing I raised was `_shareToFeed` defaulting to `false`, and I explained why I thought it was worth catching rather than just saying it was wrong: nothing about it looks broken, reviews would post fine and just never reach the feed, so you'd end up hunting through the feed code before it occurred to you to check a default. I said it's probably just `MutableStateFlow(true)`. I told him the rest read like work-in-progress and I wasn't worried about it — the screen still sitting on the placeholder, the unused star imports, and the indent that got bumped a level. I also mentioned that I'd gone at week 8 from the media-details/API side because that's what was posted after class and I'd missed the end of it, and that since the stub says week 8 is write-review, I'm not sure whether my own work is actually complete.

---

## One Thing I Understood More Deeply

Last week I wrote that I couldn't tell when you're supposed to make a new model versus just adding fields to one you already have. I'd put the detail-only fields (`description`, `pageCount`, `runtimeMinutes`, `seasonCount`, `episodeCount`) straight onto my `Media` class, and I'd noticed the prof kept a separate `MediaDetail` instead, but I couldn't explain why you'd pick one.

This week it stopped being abstract, because the API answers it. `GET /media` and `GET /media/{id}` genuinely return two different shapes — the server has a `formatMedia()` for search results and a `formatMediaDetail()` for a single item, and the second is just the first plus `description`, `pageCount`, `isbn`, `reviewCount`. So the item that search hands me literally does not contain a description; those fields come back empty. That's the actual reason the detail screen can't reuse the object it was already given and has to re-fetch by id — which I had been doing as a rule I was following without knowing what it was protecting me from.

I stuck with my merged `Media`, but now I can state the tradeoff instead of guessing at it. Mine is less code and lets search and detail share one type, but it means a `Media` in my app might or might not have a description depending on which call produced it, and nothing about the type tells you which — you just have to know. The split version makes that mistake impossible to even write down. So it isn't that one is correct: mine moves a rule out of the compiler and into my head, and I'm the one who has to remember it every time. That's a real cost, I just couldn't name it last week.

---

## One Thing I'm Still Confused About

Coroutine exception handling, specifically around `async`. My detail screen fires three requests at once (the item, its library status, its reviews), and I ended up wrapping each one in `runCatching` so that none of them can throw. The reason is that a failing `async` doesn't only fail its own `await()` — it also cancels its parent and its siblings, and the exception can get past the `try`/`catch` you wrapped around `await()`, which is the opposite of what I'd have guessed.

I know that's true and I know what to do about it. What I don't have is any intuition for it. The part I can't hold in my head is that it seems to depend on whether the coroutine is a "root" coroutine or a child of another one, and the same `async` behaves differently in each case. So I can follow the rule without being able to look at a block of coroutine code and predict what it'll do, which feels like exactly the kind of thing that bites you on code you didn't just write. Comparing my version against the prof's didn't fully settle it either, since his structure and mine differ here and I'm not confident I've reasoned it through correctly rather than just picked the safer-looking option.

---

## Rubric

*You don't need to self-assess — this is here so you know what I'm looking at.*

| Section | Points | Full Credit | Half Credit | No Credit |
|:---|:---:|:---|:---|:---|
| **Reflection** | 10 | Specific, honest responses to "More Deeply" and "Still Confused" sections. Shows genuine thinking — not just "I learned X." | Responses are present but vague or generic ("I got better at Compose"). | Missing or one-word answers. |
| **Code Review** | 10 | Specific observation about the code with explanation of why it matters (or a substantive positive comment). Link to review present and verified. | A question or comment that shows you read the code, but lacks explanation. | "Looks good!" or equivalent. Missing link. Review not found on GitHub. |
| **Total** | **20** | | | |

**A note on the code review score:** I check that the review actually exists on GitHub before grading. The written summary here and the GitHub comment should match. If the review isn't there, the written summary can't earn credit.
