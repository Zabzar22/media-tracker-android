# Week 05 Reflection

**Name:** Kenan Port
**Date:** 06-18-2026

---

## Commits This Week

**Link:** https://github.com/Zabzar22/media-tracker-android/commits/week-05

---

## Code Review

**Reviewed:** Samba Kamara
**Link to my review:** https://github.com/fascineh1/media-tracker-android/pull/1#issuecomment-4782067757

### What I Looked At

I looked at Samba's week-05 PR (#1), mainly the auth model classes he added, since those are what get sent to the API as JSON.

### What I Noticed

His model classes aren't marked `@Serializable`, and without that Retrofit can't turn them into JSON, so the app would crash when it tries to send the request. I ran into the same thing in mine.

### Comments I Left

Suggested he add `@Serializable` to the model classes, and pointed him to the prof's branch since that's the fix we got at the end of class.

---

## One Thing I Understood More Deeply

Registration and login are actually two separate API calls. `POST /users` makes the account but doesn't give you a token — you only get one from a separate `POST /tokens` call. I'd assumed signing up would just log you in, so that surprised me.

---

## One Thing I'm Still Confused About

I don't really get how `@Serializable` actually works. I can add it to make things run, but I couldn't explain why that one annotation is the difference between the app working and crashing.

---

## Anything Else

Most of my registration code followed the reference structure we were given, so I mostly matched that. The part I tried to write myself was extending it to the login/token call, since the reference stopped at registration.

---

## Rubric

*You don't need to self-assess — this is here so you know what I'm looking at.*

| Section | Points | Full Credit | Half Credit | No Credit |
|:---|:---:|:---|:---|:---|
| **Reflection** | 10 | Specific, honest responses to "More Deeply" and "Still Confused" sections. Shows genuine thinking — not just "I learned X." | Responses are present but vague or generic ("I got better at Compose"). | Missing or one-word answers. |
| **Code Review** | 10 | Specific observation about the code with explanation of why it matters (or a substantive positive comment). Link to review present and verified. | A question or comment that shows you read the code, but lacks explanation. | "Looks good!" or equivalent. Missing link. Review not found on GitHub. |
| **Total** | **20** | | | |

**A note on the code review score:** I check that the review actually exists on GitHub before grading. The written summary here and the GitHub comment should match. If the review isn't there, the written summary can't earn credit.
