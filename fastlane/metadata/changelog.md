Hey ✌️
This is the fourth version of kitshn **compatible** with the API changes for **[Tandoor v2](https://github.com/TandoorRecipes/recipes/releases/tag/2.0.0-alpha-2)**. 🥳
New versions of kitshn will **no longer** support instances on **v1.5.***.

This beta is available here on **GitHub**, through **[Google Play testing](https://play.google.com/apps/testing/de.kitshn.android)** and through **[TestFlight](https://testflight.apple.com/join/zx1xzSMg)**.

Although I have tested the app with **Tandoor v2.0.0-alpha-2**, there could still be some minor API
changes that I might have missed, so **please feel free** to comment on #95 or create **a new issue** if you **encounter any issues**! :)

---

1. Implemented **step editing** and **creation** with **markdown** support (thanks to https://github.com/MohamedRejeb/compose-rich-editor) (#177).
2. 🇵🇱 **Polish** has been added on **[Weblate](https://hosted.weblate.org/projects/kitshn)** (thanks to @dhunter49).

## Highlights

- feat(commonMain/recipes): implemented step editing and creation with markdown support (resolved #177)
- feat(build.gradle): removed F-Droid checkupdates fix (fdroid/fdroiddata#3531 on gitlab.com)
- feat(fastlane): added prepare_release and commit_release for easier and faster changelog and release creation
- fix(commonMain/recipes): fetch steps before creating new step to avoid discarding changes in StepCreationAndEditDialog
- fix(commonMain/recipes): moved save/create button into TopAppBar in StepCreationAndEditDialog for usability
- l10n(Russian): translated using Weblate
- l10n(German): translated using Weblate
- l10n(Ukrainian): translated using Weblate
- l10n(Polish): translated using Weblate
- l10n(Polish): added translation using Weblate