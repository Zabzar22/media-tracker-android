# Week 04 Reflection

**Name:** Kenan Port
**Date:** 06-11-2026

---

## Commits This Week

<!-- Paste a link to your commits for this week. The easiest way: go to your repo on GitHub,
     click "commits", and copy the URL after filtering by your name or branch. -->

**Link:** https://github.com/Zabzar22/media-tracker-android/commits/week-04

<!-- TODO: if you open a PR for week-04, swap this for the PR commits URL (e.g. .../pull/N/commits) -->

---

## Code Review

<!-- Every week you leave a review on a pod mate's pull request. Fill in both parts below.
     Part 1 is the link — I will verify the review exists on GitHub.
     Part 2 is your written assessment — what you actually looked at and what you found. -->

**Reviewed:** Issa Ali
**Link to my review:** https://github.com/Issa-Ismail-Ali/media-tracker-android/pull/4#issuecomment-4686752985

### What I Looked At

<!-- Walk through the code you reviewed. What was the PR trying to do? Which files or
     functions did you focus on? -->

I looked at Issa's week-04 register PR — mainly where he put the sign-up logic and what he validates before letting registration go through.

### What I Noticed

<!-- Be specific. Name the thing you noticed and explain why it matters. -->

Issa kept the auth logic in the `AuthViewModel`, but `onRegisterClick()` only checks that email and password aren't blank — there's no `password == confirmPassword` check, so someone could sign up with mismatched passwords and still get through.

### Comments I Left

<!-- Briefly summarize the comments you left on the PR. -->

Suggested he add a `password == confirmPassword` check before allowing registration, since right now it only checks that email and password aren't blank.

---

## One Thing I Understood More Deeply

<!-- Be specific. Don't write "I learned about ViewModels." Write what specifically clicked —
     what was confusing before, what made it make sense, and how you'd explain it to someone else.
     There are no wrong answers here. -->

The Compose state loop finally clicked for me, its like a text field holds nothing on its own; you keep the value in `var x by remember { mutableStateOf("") }` and `onValueChange` writes each keystroke back, which triggers a recompose.

---

## One Thing I'm Still Confused About

<!-- Be honest. This is the most useful part of the reflection for me — it tells me where to
     spend more time in class. You will not lose points for being confused. -->

`UserRepository` where should it get created inside the ViewModel, or passed into its constructor and if so, how does `viewModel()` build it?. 

---

## Anything Else *(optional)*

<!-- Did you help a pod mate work through something? Did you discover something cool or frustrating?
     Did something from a previous week finally click? This is a good place to put it. -->

They helped me catch up from last week, but it seems like some of the people don't know github well enough to format things correctly.

---

## Rubric

*You don't need to self-assess — this is here so you know what I'm looking at.*

| Section | Points | Full Credit | Half Credit | No Credit |
|:---|:---:|:---|:---|:---|
| **Reflection** | 10 | Specific, honest responses to "More Deeply" and "Still Confused" sections. Shows genuine thinking — not just "I learned X." | Responses are present but vague or generic ("I got better at Compose"). | Missing or one-word answers. |
| **Code Review** | 10 | Specific observation about the code with explanation of why it matters (or a substantive positive comment). Link to review present and verified. | A question or comment that shows you read the code, but lacks explanation. | "Looks good!" or equivalent. Missing link. Review not found on GitHub. |
| **Total** | **20** | | | |

**A note on the code review score:** I check that the review actually exists on GitHub before grading. The written summary here and the GitHub comment should match. If the review isn't there, the written summary can't earn credit.
