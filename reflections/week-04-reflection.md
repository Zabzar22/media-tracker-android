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

**Reviewed:** Issa Ali & Samba Kamara
**Link to my review:** <!-- PASTE THE REAL LINK after you leave the review — this is verified on GitHub -->

### What I Looked At

<!-- Walk through the code you reviewed. What was the PR trying to do? Which files or
     functions did you focus on? -->

I reviewed their registration work for this week, focusing on `RegisterScreen` and (if they have one) `RegisterViewModel` — specifically how they wired the text fields to state and where they put the sign-up and password-matching logic, since our pod handled that part a few different ways.

### What I Noticed

<!-- Be specific. Name the thing you noticed and explain why it matters. -->

Their `RegisterViewModel` takes `UserRepository` as a constructor parameter, but the screen builds it with `viewModel()` and nothing ever passes the repository in. That will crash at runtime, because `viewModel()` has no way to supply that argument — it needs either a no-arg constructor that creates `UserRepository` internally, or a `ViewModelProvider.Factory` to inject it.

### Comments I Left

<!-- Briefly summarize the comments you left on the PR. -->

I left a suggestion pointing out the constructor/repository issue and why it would crash, and proposed instantiating `UserRepository` inside the ViewModel as the simplest fix for now.

---

## One Thing I Understood More Deeply

<!-- Be specific. Don't write "I learned about ViewModels." Write what specifically clicked —
     what was confusing before, what made it make sense, and how you'd explain it to someone else.
     There are no wrong answers here. -->

The Compose state model finally clicked. A text field doesn't store anything on its own — you keep the value in `var displayName by remember { mutableStateOf("") }`, and the field's `onValueChange` writes every keystroke back into that variable, which triggers recomposition and redraws the field with the new text. Before, I didn't understand why a field needed *both* a `value` and an `onValueChange`; now I see it's a loop (state drives the UI, the UI updates the state), and that `remember` is what stops the text from resetting to empty on every redraw.

---

## One Thing I'm Still Confused About

<!-- Be honest. This is the most useful part of the reflection for me — it tells me where to
     spend more time in class. You will not lose points for being confused. -->

Where `UserRepository` is supposed to get created. If registration uses a ViewModel, do we instantiate the repository inside the ViewModel, or pass it into the constructor? And if it's a constructor parameter, how does `viewModel()` build the ViewModel when it's never handed a repository? Related to that, I'm not sure whether the confirm-password check belongs in the screen or the ViewModel — putting logic in the view feels wrong, but I don't yet know what the clean alternative is supposed to look like.

---

## Anything Else *(optional)*

<!-- Did you help a pod mate work through something? Did you discover something cool or frustrating?
     Did something from a previous week finally click? This is a good place to put it. -->

The class question board hit a lot of the same walls our pod did this week — especially whether confirm-password (which is only a client-side check and never sent to the server) should be modeled the same as the fields that actually go into the create-user request. It would help to walk through the intended registration architecture in class.

---

## Rubric

*You don't need to self-assess — this is here so you know what I'm looking at.*

| Section | Points | Full Credit | Half Credit | No Credit |
|:---|:---:|:---|:---|:---|
| **Reflection** | 10 | Specific, honest responses to "More Deeply" and "Still Confused" sections. Shows genuine thinking — not just "I learned X." | Responses are present but vague or generic ("I got better at Compose"). | Missing or one-word answers. |
| **Code Review** | 10 | Specific observation about the code with explanation of why it matters (or a substantive positive comment). Link to review present and verified. | A question or comment that shows you read the code, but lacks explanation. | "Looks good!" or equivalent. Missing link. Review not found on GitHub. |
| **Total** | **20** | | | |

**A note on the code review score:** I check that the review actually exists on GitHub before grading. The written summary here and the GitHub comment should match. If the review isn't there, the written summary can't earn credit.
