# Week 06 Reflection

**Name:** Kenan Port
**Date:** 06-25-2026

---

## Commits This Week

**Link:** https://github.com/Zabzar22/media-tracker-android/commits/week-06

---

## Code Review

**Reviewed:** Samba Kamara
**Link to my review:** https://github.com/fascineh1/media-tracker-android/pull/2#issuecomment-4832846117

### What I Looked At

I looked at Samba's week-06 search PR; `SearchViewModel`, `SearchScreen`, and `UserApiService` mainly, since the task this week was to make the search screen pull real results from `GET /media` and page 20 at a time.

### What I Noticed

His `SearchViewModel` returns four hard-coded items with no pagination from what I see, so it never loads results in pages of 20 and he built his own local `MediaItem(id, title, subtitle)` class instead of using the shared `Media` model. None of us actually have the live `GET /media` call wired yet (ours is still faked too), so I didn't focus on that; the two things that stood out as actual gaps were the missing pagination and the local model, since the JSON response maps to `Media`, not `MediaItem`, and that mismatch will bite him once the API is connected. The "Wire to GET /media" stub comment is also still in `SearchScreen`.

### Comments I Left

Led with credit for landing `@Serializable` on the auth models, since that was what blocked him last week. Then noted, while being upfront that I wasn't 100% sure, that the `SearchViewModel` is still returning four hard-coded items so it isn't paging in 20s yet, and that his local `MediaItem` class might bite us once the API is wired since the response maps to the shared `Media` model — so it'd probably be worth switching over before we hook it up. Pointed out the "Wire to GET /media" stub comment is still in `SearchScreen` too, and framed the whole thing as us being in the same boat since none of us have the live call hooked up yet. Suggested we brainstorm the big-picture shape of this together rather than just dropping it into the code, so it makes sense to all of us, and closed on the fact that his PRs have gotten a lot easier to follow.

---

## One Thing I Understood More Deeply

The "load 20 at a time" behavior is really a small state machine, and the guard is what makes it work. In my `loadNextPage()` the `isLoadingMore` flag plus `loadedCount` are what keep a single scroll-to-bottom from dumping the entire list — it appends exactly one `drop(loadedCount).take(20)` slice and then flips the flag back. Before this week I thought pagination was mostly a UI thing (a spinner at the bottom); now I see the list, the loaded count, and the "is a page already in flight?" flag have to move together or you get duplicates or the whole list at once. When the real `GET /media` gets wired, the only part that changes is where the next slice comes from — instead of `drop/take` on a local list, the next page (and whether there even is one) comes back from the request.

_---_

## One Thing I'm Still Confused About

Cursor-based paging on the real endpoint. The prof's branch doesn't use a page number or an offset — it sends `after` and reads the next cursor out of the `X-Next-Cursor` response header, with `X-Has-More` telling it whether to keep going. I get that an opaque cursor is more stable than an offset when the data changes, but I don't really understand how the server decides what the cursor is, or why it rides in a header instead of in the JSON body. Right now I'm faking all of this with a counter, so I haven't had to actually round-trip a cursor yet.

---

## Anything Else

Worth being honest: my search is still running on `fakeSearchResults` with a `delay(500)` to fake the network because I couldn't tell if it was working or not; and so are both my pod mates' — the prof's branch is the one that actually deleted the fake data and moved to the live API. So this week was less "we hit the API" and more "we all built the search/paging UI and stubbed the data source." Issa got the closest — he actually defined a `searchMedia` endpoint and built real 20-at-a-time paging logic — but his `SearchViewModel` still reads from the fake list and never calls that endpoint, so it isn't wired either. I left him a comment about that.

---

## Rubric

*You don't need to self-assess — this is here so you know what I'm looking at.*

| Section | Points | Full Credit | Half Credit | No Credit |
|:---|:---:|:---|:---|:---|
| **Reflection** | 10 | Specific, honest responses to "More Deeply" and "Still Confused" sections. Shows genuine thinking — not just "I learned X." | Responses are present but vague or generic ("I got better at Compose"). | Missing or one-word answers. |
| **Code Review** | 10 | Specific observation about the code with explanation of why it matters (or a substantive positive comment). Link to review present and verified. | A question or comment that shows you read the code, but lacks explanation. | "Looks good!" or equivalent. Missing link. Review not found on GitHub. |
| **Total** | **20** | | | |

**A note on the code review score:** I check that the review actually exists on GitHub before grading. The written summary here and the GitHub comment should match. If the review isn't there, the written summary can't earn credit.
