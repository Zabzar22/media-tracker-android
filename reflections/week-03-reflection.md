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

I looked at Issa's and Samba's week-03 PRs — both built the register screen and sign-up logic, so I focused on where they put the registration code and how the fields connect to creating the account.

### What I Noticed

<!-- Be specific. Name the thing you noticed and explain why it matters. -->

Our pod split two ways — Issa put registration in the shared `AuthViewModel`, Samba made a separate `RegisterViewModel`. Samba's `onSignUpClicked()` hardcodes `username`/`email`/`password` as `""` so accounts get made with blank info, and Issa's only checks email/password aren't blank with no password == confirmPassword match.

### Comments I Left

<!-- Briefly summarize the comments you left on the PR. -->

Told Samba to capture `username`/`email`/`password` like he does `displayName` instead of passing empty strings. Told Issa to add a `password == confirmPassword` check before marking it successful.

---

## One Thing I Understood More Deeply

<!-- Be specific. Don't write "I learned about ViewModels." Write what specifically clicked —
     what was confusing before, what made it make sense, and how you'd explain it to someone else.
     There are no wrong answers here. -->

Since I missed class, I caught up by reading the login code and mirroring it into register — that's when it clicked that register is basically the same screen as login with more fields and a different repository call.

---

## One Thing I'm Still Confused About

<!-- Be honest. This is the most useful part of the reflection for me — it tells me where to
     spend more time in class. You will not lose points for being confused. -->

Catching up from just the code diffs was rough — I could see what changed but not the reasoning behind it, so I'm not sure my approach matches what the pod did in class.

---

## Anything Else *(optional)*

<!-- Did you help a pod mate work through something? Did you discover something cool or frustrating?
     Did something from a previous week finally click? This is a good place to put it. -->

If someone misses a class, is there any way to see what was covered so catching up is about understanding it, not guessing from the diff?

---

## Rubric

*You don't need to self-assess — this is here so you know what I'm looking at.*

| Section | Points | Full Credit | Half Credit | No Credit |
|:---|:---:|:---|:---|:---|
| **Reflection** | 10 | Specific, honest responses to "More Deeply" and "Still Confused" sections. Shows genuine thinking — not just "I learned X." | Responses are present but vague or generic ("I got better at Compose"). | Missing or one-word answers. |
| **Code Review** | 10 | Specific observation about the code with explanation of why it matters (or a substantive positive comment). Link to review present and verified. | A question or comment that shows you read the code, but lacks explanation. | "Looks good!" or equivalent. Missing link. Review not found on GitHub. |
| **Total** | **20** | | | |

**A note on the code review score:** I check that the review actually exists on GitHub before grading. The written summary here and the GitHub comment should match. If the review isn't there, the written summary can't earn credit.
