# Week 05 Reflection

**Name:** Kenan Port
**Date:** 06-18-2026

---

## Commits This Week

**Link:** https://github.com/Zabzar22/media-tracker-android/commits/week-05

---

## Code Review

**Reviewed:** Samba Kamara
**Link to my review:** https://github.com/fascineh1/media-tracker-android/pull/1

### What I Looked At

I reviewed Samba's week-05 PR (#1), "Add authentication models and repository structure." It adds the auth request/response models — `CreateUserRequest`, `TokenRequest`, `TokenResponse`, `AuthResponse` — plus starter files for `MediaTrackerApi` and `UserRepository`. I focused on the model classes, since those are what get serialized to and from the API this week.

### What I Noticed

The model data classes aren't marked `@Serializable`. Since these are meant to be sent and received as JSON through Retrofit's kotlinx-serialization converter, they'll fail at runtime — the converter can't build a serializer for a plain class — unless each one is annotated `@Serializable` and the serialization Gradle plugin is applied. This is the same thing that would have bitten us, so it stood out. I also noticed `data/repository/UserRepository.kt` declares `package edu.metrostate.ics342.mediatracker.data.remote` even though it lives in the `data/repository` folder, so the package doesn't match its directory — looks like a copy-paste from `MediaTrackerApi.kt`.

### Comments I Left

Suggested adding `@Serializable` to each of the auth model classes (and confirming the kotlinx-serialization plugin is in the build file) before wiring them to Retrofit, since otherwise the converter throws when it tries to (de)serialize them. Also flagged the package/folder mismatch in `UserRepository.kt` so the declared package matches its `data/repository` directory.

---

## One Thing I Understood More Deeply

The auth flow clicked once I was the one wiring login after registration was already done. The `clientId`/`clientSecret` identify the *app* to the API (they live in `BuildConfig`, loaded from `local.properties` so they stay out of git) and are sent on every auth call — that is completely separate from the *user's* email and password. The other piece that clicked: `POST /users` creates the account but gives you back **no token** — just the profile. You don't actually get an access token until a second, separate call to `POST /tokens`. So "register" and "log in" are two different requests, and only the second one gives you the Bearer token you'd need for everything else.

Writing that login call myself is also where I understood why the response type matters. Register uses `Response<Unit>` and only checks the status code — it doesn't care about the body. But login *needs* the token out of the body, so I couldn't reuse that; I had to make it `Response<TokenResponse>` and actually read the response. "Did it succeed?" and "give me the data back" turned out to be two different things.

---

## One Thing I'm Still Confused About

How the token actually gets "handed off" and used after login. Right now we store the `accessToken` in an in-memory `TokenStore`, but nothing reads it yet. I'm not clear on how it gets attached to future authenticated requests (I think an OkHttp interceptor adds an `Authorization: Bearer` header automatically?), or where the token should be saved so it survives the app closing — I saw DataStore mentioned but I don't understand how that fits in yet.

---

## Anything Else

To be straight about what was mine this week: a lot of the registration code closely followed the structure we were given for reference — the `UserRepository` interface + `DefaultUserRepository`, the dedicated `RegisterViewModel`, the `data/network` layout — so I matched that rather than inventing my own. The part I actually wrote was extending that pattern to login/token, since the reference stopped at registration: the `login()` repository function and its `LoginResult` states, the real `onLoginClick()` in `AuthViewModel`, and a small `TokenStore` to hold the returned token (there wasn't a reference for where the token should go, so that was my call). It was a good exercise in applying a pattern to a new endpoint instead of copying it.

Also, the thing I wrote as "still confused" last week — where `UserRepository` should get created and how `viewModel()` builds it — finally made sense. Making `UserRepository` an interface with a `DefaultUserRepository` implementation, and giving the ViewModel a constructor like `AuthViewModel(private val userRepository: UserRepository = DefaultUserRepository())`, is the answer: the default value lets `viewModel()` still build it with no arguments, while the interface means a fake repository could be passed in for testing. That connected the dots from last week.

---

## Rubric

*You don't need to self-assess — this is here so you know what I'm looking at.*

| Section | Points | Full Credit | Half Credit | No Credit |
|:---|:---:|:---|:---|:---|
| **Reflection** | 10 | Specific, honest responses to "More Deeply" and "Still Confused" sections. Shows genuine thinking — not just "I learned X." | Responses are present but vague or generic ("I got better at Compose"). | Missing or one-word answers. |
| **Code Review** | 10 | Specific observation about the code with explanation of why it matters (or a substantive positive comment). Link to review present and verified. | A question or comment that shows you read the code, but lacks explanation. | "Looks good!" or equivalent. Missing link. Review not found on GitHub. |
| **Total** | **20** | | | |

**A note on the code review score:** I check that the review actually exists on GitHub before grading. The written summary here and the GitHub comment should match. If the review isn't there, the written summary can't earn credit.
