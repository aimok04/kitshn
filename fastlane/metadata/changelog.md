Hey ✌️
This release contains the **following changes**:

1. Implemented **step editing** and **creation** with **markdown** support (thanks to https://github.com/MohamedRejeb/compose-rich-editor) (#177).
2. Added **recipe name** to **sharing text** for easier identification (#185).
3. Enabled users to **disable** the **shopping mode enlargement** (#181).
4. 🇵🇱 **Polish** has been added on **[Weblate](https://hosted.weblate.org/projects/kitshn)** (thanks to @dhunter49).

## Highlights

- feat(commonMain/recipes): implemented step editing and creation with markdown support (#177)
- feat(commonMain/shopping): added "enlarge shopping mode" setting (#181)
- feat(commonMain/recipes): added recipe name to sharing text (#185)
- feat(fastlane): changed deploy_github branch from main to v1
- feat(fastlane): added prepare_release and commit_release for easier and faster changelog and release creation
- feat(build.gradle): removed F-Droid checkupdates fix (fdroid/fdroiddata#3531 on gitlab.com)
- fix(commonMain/recipes): fetch steps before creating new step to avoid discarding changes in StepCreationAndEditDialog
- fix(commonMain/recipes): moved save/create button into TopAppBar in StepCreationAndEditDialog for usability
- fix(commonMain): removed imports of de.kitshn.JsonAsStringSerializer
- l10n(Polish): added translation using Weblate
- l10n(Polish): translated using Weblate
- l10n(Polish): fix short_description.txt being too long
- l10n(Russian): translated using Weblate
- l10n(German): translated using Weblate
- l10n(Ukrainian): translated using Weblate