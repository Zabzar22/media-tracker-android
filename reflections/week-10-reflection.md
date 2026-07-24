# Week 10 Reflection

**Name:** Kenan Port
**Date:** 07-23-2026

---

## Commits This Week

**Link:**

---

## Code Review

**Reviewed:**
**Link to my review:**

### What I Looked At

### What I Noticed

### Comments I Left

---

## One Thing I Understood More Deeply

The list on the screen is a copy, not the real thing, and I did not really feel that until it bit me. My `removeItem()` from a couple weeks back just did `_libraryItems.value.filter { it.mediaId != mediaId }`, and honestly it looked finished. You tap remove, the row disappears, no errors, no crash. It was only after I swapped My Library over to `GET /library` this week that I found out it never worked at all. Now every tab tap fires a fresh request, so I removed something, switched to Finished, came back to Want To, and there it was again. Nothing had happened on the server. I had crossed the name off my own printout and thought I had changed the record.

What made it click was realizing that part 1 did not break anything, it just gave the bug a way to show itself. Before this week nothing ever re-fetched, so the app never had a chance to disagree with the server, and a local edit could sit there looking correct forever. So the fix was not really about `DELETE`, it was about accepting that my screen does not own that data. It draws whatever came back from the last `GET`, and if I want something to actually change I have to tell the server and then ask again.

The professor said something in class about transactional stuff needing to make the user wait so the data set stays up to date, and that is the same idea from the other side. If you do not wait, you are showing the user something you have not confirmed yet. Both of my buttons work that way now; `toggleFavorite()` sets `isUpdatingFavorite = true`, disables the button, waits, and only then flips the heart. If the call throws, the state never changes and you can try again. It feels slow, but it cannot lie, and I think I would rather be slow than wrong at this point in the project.

---

## One Thing I'm Still Confused About

After a call succeeds, I do not know how you are supposed to decide between reloading the whole list and just patching the one item you changed by hand. I went with reload for both remove and status change, so `removeItem()` calls `removeFromLibrary()` and then immediately calls `loadLibrary()` again. I picked it because it means I never have to think about which item goes in which tab, or what happens when you move something to Finished while you are looking at Want To. The server decides, I just draw it. But it is obviously more requests than I need, and there is a spinner flash after every single tap, which looks bad.

The alternative is patching the local list myself, which is what the optimistic pattern in the handout does. That is faster and smoother, but now I am maintaining a second version of the truth and hoping it agrees with the first one. And the handout's rollback example even says it is fine if a rolled back item lands at the end of the list instead of where it was, which tells me the copies are allowed to drift a little, and I do not have a feel for how much drift is acceptable before you have a real bug. I am guessing the answer is something like "reload when it is cheap, patch when the user would notice the wait," but that is me guessing, not something I actually understand yet.

---

## Anything Else

Being straight about where this actually is, because the last two weeks I had to admit the API was not really connected. This week it is. My Library pulls from `GET /library` with the tab wired to the `status` query, so the server does the filtering instead of me filtering a list I already had. I got to delete `items.filter { it.status == selectedStatus }` from the screen, which was a weirdly satisfying way to find out the step was working. Cover art is how I confirmed it, actually; those library cards already had an `if (coverUrl != null)` check sitting in them doing nothing, and the moment I swapped to real data they filled in with actual book and movie covers. That was better proof than counting rows.

A few honest notes on scope. "+ Want To" turned out to be already done from week 8, so requirement 1 was free. I did add `DELETE /favorites/{mediaId}`, which the handout did not ask for, because the Save button had no way to undo itself and there is no favorites screen to remove things from; it seemed worse to ship a button you can only press once. I also put a favorites list on the profile screen. That is a stretch goal from the part 2 handout and it is not in the wireframes anywhere, but saving something had literally no visible result other than the button changing, which felt broken even though it was not. The profile itself is still mock data, so it is a real list sitting inside a fake profile, which is a little odd but at least the part that matters is real.

Two things I am deliberately not done with. I have not done optimistic updates yet, so everything still waits on the server; I wanted remove and status change working honestly before making anything feel fast. And I have not written the MockK rollback test, because there is no rollback to test until the optimistic part exists. Both of those are next.

One thing I noticed that is not from this week and did not fix: search result cards always show the placeholder icon instead of real cover art. `SearchComponents.kt` never checks `coverUrl` at all, even though the server sends one, so it is just a leftover from when that card was built against mock data. Library and Media Detail both check it, so it is only Search. Also the stars are missing on search results, but that one is correct behavior; the catalog genuinely has no ratings yet because `average_rating` is calculated from reviews by a trigger, and nobody has posted any, so the card hides the star instead of printing 0.0 on everything. Took me a minute to convince myself that one was not a bug.

---

## Rubric

*You don't need to self-assess — this is here so you know what I'm looking at.*

| Section | Points | Full Credit | Half Credit | No Credit |
|:---|:---:|:---|:---|:---|
| **Reflection** | 10 | Specific, honest responses to "More Deeply" and "Still Confused" sections. Shows genuine thinking — not just "I learned X." | Responses are present but vague or generic ("I got better at Compose"). | Missing or one-word answers. |
| **Code Review** | 10 | Specific observation about the code with explanation of why it matters (or a substantive positive comment). Link to review present and verified. | A question or comment that shows you read the code, but lacks explanation. | "Looks good!" or equivalent. Missing link. Review not found on GitHub. |
| **Total** | **20** | | | |

**A note on the code review score:** I check that the review actually exists on GitHub before grading. The written summary here and the GitHub comment should match. If the review isn't there, the written summary can't earn credit.
