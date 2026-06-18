# Week 04 Reflection

**Name:** Kenan Port
**Date:** 06-11-2026

---

## Commits This Week

**Link:** https://github.com/Zabzar22/media-tracker-android/commits/week-04

---

## Code Review

**Reviewed:** Issa Ali
**Link to my review:** https://github.com/Issa-Ismail-Ali/media-tracker-android/pull/4#issuecomment-4686752985

### What I Looked At

I looked at Issa's week-04 register PR — mainly where he put the sign-up logic and what he validates before letting registration go through.

### What I Noticed

Issa kept the auth logic in the `AuthViewModel`, but `onRegisterClick()` only checks that email and password aren't blank — there's no `password == confirmPassword` check, so someone could sign up with mismatched passwords and still get through.

### Comments I Left

Suggested he add a `password == confirmPassword` check before allowing registration, since right now it only checks that email and password aren't blank.

---

## One Thing I Understood More Deeply

The Compose state loop finally clicked for me. It's like a text field holds nothing on its own; you keep the value in `var x by remember { mutableStateOf("") }` and `onValueChange` writes each keystroke back, which triggers a recompose.

---

## One Thing I'm Still Confused About

Where should `UserRepository` get created — inside the ViewModel, or passed into its constructor? And if it's passed in, how does `viewModel()` build it?

---

## Anything Else

They helped me catch up from last week, but it seems like some of the people don't know github well enough to format things correctly. That's also why I've reviewed Issa both weeks — my other pod mate's repo has been on the wrong branch without working pull requests, so his changes haven't really been reviewable on time. I did still leave comments on his commits, just not as my main review.

---

## Rubric

*You don't need to self-assess — this is here so you know what I'm looking at.*

| Section | Points | Full Credit | Half Credit | No Credit |
|:---|:---:|:---|:---|:---|
| **Reflection** | 10 | Specific, honest responses to "More Deeply" and "Still Confused" sections. Shows genuine thinking — not just "I learned X." | Responses are present but vague or generic ("I got better at Compose"). | Missing or one-word answers. |
| **Code Review** | 10 | Specific observation about the code with explanation of why it matters (or a substantive positive comment). Link to review present and verified. | A question or comment that shows you read the code, but lacks explanation. | "Looks good!" or equivalent. Missing link. Review not found on GitHub. |
| **Total** | **20** | | | |

**A note on the code review score:** I check that the review actually exists on GitHub before grading. The written summary here and the GitHub comment should match. If the review isn't there, the written summary can't earn credit.
