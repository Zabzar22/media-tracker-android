# Week 03 Reflection

**Name:** Kenan Port
**Date:** 06-11-2026 <!-- caught up late; was away for a camping trip the week this was due -->

---

## Commits This Week

<!-- Paste a link to your commits for this week. The easiest way: go to your repo on GitHub,
     click "commits", and copy the URL after filtering by your name or branch. -->

**Link:** https://github.com/Zabzar22/media-tracker-android/commits/week-03

<!-- TODO: confirm there is a branch named exactly week-03 with the "Caught up for week-03" commit
     in its own PR — otherwise the naming/submission points are lost. Swap this for the PR commits URL
     if you open one. -->

---

## Code Review

<!-- Every week you leave a review on a pod mate's pull request. Fill in both parts below.
     Part 1 is the link — I will verify the review exists on GitHub.
     Part 2 is your written assessment — what you actually looked at and what you found. -->

**Reviewed:** Issa Ali & Samba Kamara
**Link to my review:** <!-- PASTE THE REAL LINK after you leave the review — this is verified on GitHub -->

### What I Looked At

<!-- Walk through the code you reviewed. What was the PR trying to do? Which files or
     functions did you focus on? -->

I looked at their week-03 PR and compared their approach to mirroring the login flow into a register method against mine, since I built my version on my own while catching up rather than in class.

### What I Noticed

<!-- Be specific. Name the thing you noticed and explain why it matters. A good, specific angle:
     do their login and register screens duplicate the same field/state/button structure, or did
     they pull the shared parts into one place? Duplication works now but means every future fix
     has to be made twice. -->

### Comments I Left

<!-- Briefly summarize the comments you left on the PR. -->

---

## One Thing I Understood More Deeply

<!-- Be specific. Don't write "I learned about ViewModels." Write what specifically clicked —
     what was confusing before, what made it make sense, and how you'd explain it to someone else.
     There are no wrong answers here. -->

Because I missed class for a camping trip, I caught up by tracing the existing login flow line by line and mirroring it into a register method myself. Doing it without the lecture forced me to actually read the login code instead of following along, and that's when I understood that the register screen is really the *same shape* as login — the same field/state/button pattern, just with more fields and a different repository call. Rebuilding it from the login as a template is what made the structure stick.

---

## One Thing I'm Still Confused About

<!-- Be honest. This is the most useful part of the reflection for me — it tells me where to
     spend more time in class. You will not lose points for being confused. -->

Catching up without access to the recorded lecture was the hard part — I could see *what* changed in the code from the diffs, but not the *reasoning* the class talked through. I'm still not sure whether the way I mirrored login into register matches the approach the rest of the pod took, which I only started to find out later when comparing PRs.

---

## Anything Else *(optional)*

<!-- Did you help a pod mate work through something? Did you discover something cool or frustrating?
     Did something from a previous week finally click? This is a good place to put it. -->

If a student has to miss a class, is there any way to see what was covered so the catch-up is about understanding the reasoning and not just guessing from the code diff? That was my main friction this week.

---

## Rubric

*You don't need to self-assess — this is here so you know what I'm looking at.*

| Section | Points | Full Credit | Half Credit | No Credit |
|:---|:---:|:---|:---|:---|
| **Reflection** | 10 | Specific, honest responses to "More Deeply" and "Still Confused" sections. Shows genuine thinking — not just "I learned X." | Responses are present but vague or generic ("I got better at Compose"). | Missing or one-word answers. |
| **Code Review** | 10 | Specific observation about the code with explanation of why it matters (or a substantive positive comment). Link to review present and verified. | A question or comment that shows you read the code, but lacks explanation. | "Looks good!" or equivalent. Missing link. Review not found on GitHub. |
| **Total** | **20** | | | |

**A note on the code review score:** I check that the review actually exists on GitHub before grading. The written summary here and the GitHub comment should match. If the review isn't there, the written summary can't earn credit.
