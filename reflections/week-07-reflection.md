# Week 07 Reflection

**Name:** Kenan Port
**Date:** 07-02-2026

---

## Commits This Week

**Link:** https://github.com/Zabzar22/media-tracker-android/commits/week-07

---

## Code Review

**Reviewed:** Issa Ali
**Link to my review:** https://github.com/Issa-Ismail-Ali/media-tracker-android/pull/7#issuecomment-4904249098

### What I Looked At

I looked at Issa's week-07 Media Detail PR, mostly `MediaDetailViewModel` and `MediaDetailScreen`. The task this week was Screen 07, the page that opens when you tap a title, so I focused on how he loads the item by its id and how he laid out the cover, title, creator, star rating, the two buttons, the About section, the three stat boxes, and the review cards.

### What I Noticed

Two things stood out to me. The first was in his `setMediaId`, where he does `firstOrNull { it.id == id } ?: mediaList.first()`. So if an id comes in that isn't in the list, it just falls through to the first item instead of showing nothing found. It doesn't matter now since all the ids line up, but I think once we're calling `GET /media/{id}` for real, a bad id would open the wrong title's page instead of an error, which seemed like it would be annoying to debug. I wasn't totally sure about it though, since our fake repository fallback might end up covering that case anyway. The second thing was his star row, which uses `averageRating.toInt()`. That chops off the decimal, so a 4.8 only lights up 4 stars. I caught it because I ran into the exact same thing on my own screen with a 3.4 book I inserted to test the mid-star case, and it was a pretty easy fix.

### Comments I Left

I started with something I actually liked, which was how he split the screen into smaller composables (`CoverPlaceholder`, `StatBox`, `ReviewCard`) instead of one giant block, and said it was honestly cleaner than mine. Then I brought up the `?: mediaList.first()` fallback and the `.toInt()` star thing as two things worth double checking, and was upfront that I wasn't sure the id one would even matter once the fake repository fallback is in. One other thing I flagged that wasn't about the code: his week-07 PR is currently the only pull request in his repo, and I wasn't sure if that was on purpose, so I mentioned it'd be worth fixing before the professor grades our week-06 reflections so he still gets full credit for that week.

---

## One Thing I Understood More Deeply

This is my first Android project, so a lot of this is still new to me, but the thing that clicked this week was using an actual state type for the screen instead of just a `Media?` that might be null. In my ViewModel I ended up making a `sealed interface` with three cases, Loading, Success, and NotFound, and building it that way is what made me realize that "there's no media" is really two different situations. Either the request is still going, or the server just doesn't have that id. If you only use `Media?` and check `if (media == null)`, which is what both of my pod mates did, the screen can't tell those two apart, so a loading spinner and a real "not found" end up in the same branch. Once I split it into three named states, the screen just checks which state it's in and shows one thing for each, and it also gave me an obvious place to put my fallback (try the server, and only fall back to the sample data if it comes back as a 404). Before this week I would have just used a nullable and an if-statement, so seeing why that quietly loses the difference between "not yet" and "not there" felt like it actually taught me something.

---

## One Thing I'm Still Confused About

I'm still not sure when you're supposed to make a whole new model versus just adding fields to one you already have. I put the detail-only fields (`description`, `pageCount`, `runtimeMinutes`, `seasonCount`, `episodeCount`) right onto the `Media` class I already had, because it felt like less to keep track of and the screen already used `Media`. But when I looked at the prof's branch, he made a separate `MediaDetail` model instead, and it has stuff mine doesn't, like `isbn` and `reviewCount`. I can tell his version matches the real API shape better, but I don't really understand how you're supposed to decide up front. Mine is simpler and lets search and the detail page share one type, but his keeps the short list version and the full detail version from mixing together, which sounds like it matters. I just don't have a feel yet for which choice comes back to bite you later.

---

## Anything Else

Being honest about where mine actually is: my ViewModel does call `GET /media/{id}` through `DefaultMediaRepository.getMedia()`, and it uses the fake repository as a backup only when the server returns a 404. That's a little further than both of my pod mates, who load straight from the fake list, and even further than the prof's week-07 ViewModel, which is still a `// TODO (Week 7)` stub. But I want to be upfront that in the emulator the backup is basically doing all the work right now, since I haven't actually tested it against a real server. So it's less "I connected the API" and more "I wired the call up and it falls back to mock data until the server is really there." That's what my commit message means when it says "working with MockData, next step is to correctly attach to API."

---

## Rubric

*You don't need to self-assess — this is here so you know what I'm looking at.*

| Section | Points | Full Credit | Half Credit | No Credit |
|:---|:---:|:---|:---|:---|
| **Reflection** | 10 | Specific, honest responses to "More Deeply" and "Still Confused" sections. Shows genuine thinking — not just "I learned X." | Responses are present but vague or generic ("I got better at Compose"). | Missing or one-word answers. |
| **Code Review** | 10 | Specific observation about the code with explanation of why it matters (or a substantive positive comment). Link to review present and verified. | A question or comment that shows you read the code, but lacks explanation. | "Looks good!" or equivalent. Missing link. Review not found on GitHub. |
| **Total** | **20** | | | |

**A note on the code review score:** I check that the review actually exists on GitHub before grading. The written summary here and the GitHub comment should match. If the review isn't there, the written summary can't earn credit.
