Hey ✌️
This beta version of kitshn is *only* compatible with **[Tandoor v2](https://github.com/TandoorRecipes/recipes/releases/tag/2.0.0-beta-1)**.
Available here on **GitHub**, through **[Google Play testing](https://play.google.com/apps/testing/de.kitshn.android)** and through **[TestFlight](https://testflight.apple.com/join/zx1xzSMg)**.

Feel free to comment on #95 or create **a new issue** if you **encounter any issues**! :)

---

It's been some time. Here are the changes that have accumulated:

1. Added **» Take new photo «** option to every **image upload** field.
2. Added support for **timer ranges** (e.g. *2 to 5 minutes*) (#169).
3. Added **AI provider** selector to **AI** and **social media** import.
4. Fixed **AI import** options being disabled due to **API changes**.
5. Fixed **recipes list** not updating when selecting different **recipe book** (#256).
6. Fixed issue where **ingredient amount template** wasn't scaling (#264).
7. Changed **sorting order** of meal plan entries to *time* value (#268). 
8. Checked **compatibility** with Tandoor **version 2.0.3** to **version 2.2.7**. 
9. 🇺🇦 **Ukrainian** has been **updated** on **[Weblate](https://hosted.weblate.org/projects/kitshn)** (by Максим Горпиніч).**
10. 🇨🇿 **Czech** has been **updated** on **[Weblate](https://hosted.weblate.org/projects/kitshn)** (by @hernikplays).**
11. 🟡🔴 **Catalan** has been **updated** on **[Weblate](https://hosted.weblate.org/projects/kitshn)** (by @rubenixnagios).**
12. 🇳🇱 **Dutch** has been **updated** on **[Weblate](https://hosted.weblate.org/projects/kitshn)** (by @jstrvr).**

## Commits

- feat(README.md): added kitshn beta invite links to installation section
- feat(commonMain/import): added AI provider selector and adapted to AI related API changes
- feat(commonMain/ui): removed peekaboo and implemented ChoosePhotoBottomSheet for every photo upload situation
- feat(commonMain/recipes): implemented timer ranges regex and bottom sheet (resolved #169)
- feat(web/funding): update to funding api v2
- feat(README.md): add kitshn.app screenshots page link to impressions
- feat(README.md): improve impressions layout
- feat(README.md): update impressions to kitshn v2
- feat(web/screenshots): update screenshots to kitshn v2
- feat(fastlane): added Android take_screenshots lane and take_android_screenshots actions
- feat(commonMain & androidTest): added automatic screenshot creation for Android
- feat(README.md): update features
- feat(README.md): removed roadmap
- fix(images): added testflight badge
- fix(commonMain/strings.xml): added search_providers string
- fix(commonMain/import): replaced search_categories with search_providers string in SelectAIProviderDialog.kt
- fix(commonMain/import): added SelectAIProviderDialog to RecipeImportAIDialog.kt
- fix(fastlane): increased sleep in take_android_screenshots.rb to ensure system ui demo mode being enabled
- fix(fastlane): changed Android take_screenshots lane to copy screenshots to web
- fix(fastlane): added missing screenshots
- fix(commonMain/books): also show placeholder icon when all recipes have no images
- fix(commonMain/mealplan): changed sorting order of mealplans to meal type time value (resolved #268)
- fix(commonMain/books): wrapped listAllEntries with TandoorRequestState to prevent exception
- fix(commonMain/books): filter recipes were not cleared/ updated when selecting different book (resolved #256)
- fix(commonMain/recipes): issue where ingredient amount template wasn't scaling (resolved #264)
- chore(fastlane): ran android take_screenshots
- chore(fastlane): ran android take_screenshots
- chore(libs): version bumps
- chore(fastlane): ran android take_screenshots
- chore(composeApp/composeResources): update aboutlibraries.json
- chore(commonMain/api): marked v2.0.3 - v2.2.7 as compatible
- l10n(German): translated using Weblate
- l10n(Ukrainian): translated using Weblate
- l10n(German): translated using Weblate
- l10n(Czech): translated using Weblate
- l10n(Catalan): translated using Weblate
- l10n(Czech): translated using Weblate
- l10n(Dutch): translated using Weblate