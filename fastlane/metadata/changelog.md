> [!CAUTION]
> ‼️ Android will become a locked-down platform. Learn more: https://keepandroidopen.org/

<img width="1604" height="267" alt="kitshn (for Tandoor)" src="https://github.com/user-attachments/assets/746041d2-1a85-44b4-a03b-aef0c792cc87" />


### Hey ✌️

This release again includes a lot of work by @jonsch318 🙌

- Fix dynamic home screen not loading correctly (#422) (#424).
- Allow recipe dialogs to be maximized on large screen devices (#364) (#401).
- Improved timer detection in recipe steps text (#306) (#409).
- Offline mode / app architecture improvements (#396).

---

## Commits

- feat: unify tandoor client api
- feat: implement food & supermarket category repo in app
- feat: refactor food & supermarket flow
- feat: add pull to refresh to shopping
- feat: implement unit repository in app
- feat: add unit repo
- feat: implement shopping repo in app
- feat: add shopping repo & entity
- feat: add room dependency
- feat: add koin dependency injection & tandoor session
- feat: allow RecipeLinkDialog to maximize
- fix(workflows): aarch64 flatpak distribution #332
- fix: harden dynamic homepage builder against bad requests
- fix: correct time in range steper
- fix: improve time formatting in timer range sheet
- fix: improve timer detection
- fix: unique constraints on insert and delete shopping list items on reconcile
- fix: wire in clearAllTable on android which is sealed
- fix: and improve debug clear & reset process
- fix: refactor db or repo implementation
- fix: show skeleton in shopping list view
- fix: shopping double click and selection timeout
- fix(ui/dialog): remove spacing between back and fullscreen buttons in RecipeLinkDialog.kt
- OnboardingSignInBrowser: wipe localStorage and sessionStorage on first load