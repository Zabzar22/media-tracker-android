# Week 03 Reflection

**Name:** Kenan Port
**Date:** 06-11-2026

---

## Commits This Week

**Link:** https://github.com/Zabzar22/media-tracker-android/compare/week-02...week-03

---

## Code Review

**Reviewed:** Issa Ali
**Link to my review:** https://github.com/Issa-Ismail-Ali/media-tracker-android/pull/3#issuecomment-4686824335

### What I Looked At

I looked at Issa's week-03 PR, which added the register screen, the RegisterViewModel, and the API/repository setup. I focused on the sign-up logic and what it checks before creating the account.

### What I Noticed

His `onRegisterClick()` only checks that email and password aren't blank; the username, display name, and password confirmation aren't validated, so someone could register with missing info or mismatched passwords and still hit the success state.

### Comments I Left

Told Issa that `onRegisterClick()` only validates email and password right now, and that he'll eventually want to validate the username, display name, and password confirmation before setting the state to success.

---

## One Thing I Understood More Deeply

Since I missed class, I caught up by reading the login code and mirroring it into register, that's when it clicked that register is basically the same screen as login with more fields and a different repository call.

---

## One Thing I'm Still Confused About

Catching up from just the code diffs was rough; I could see what changed but not the reasoning behind it, so I'm not sure my approach matches what the pod did in class.

---

## Anything Else

If someone misses a class, is there any way to see what was covered so catching up is about understanding it, not guessing from the diff on github?

---

## Rubric

*You don't need to self-assess — this is here so you know what I'm looking at.*

| Section | Points | Full Credit | Half Credit | No Credit |
|:---|:---:|:---|:---|:---|
| **Reflection** | 10 | Specific, honest responses to "More Deeply" and "Still Confused" sections. Shows genuine thinking — not just "I learned X." | Responses are present but vague or generic ("I got better at Compose"). | Missing or one-word answers. |
| **Code Review** | 10 | Specific observation about the code with explanation of why it matters (or a substantive positive comment). Link to review present and verified. | A question or comment that shows you read the code, but lacks explanation. | "Looks good!" or equivalent. Missing link. Review not found on GitHub. |
| **Total** | **20** | | | |

**A note on the code review score:** I check that the review actually exists on GitHub before grading. The written summary here and the GitHub comment should match. If the review isn't there, the written summary can't earn credit.
