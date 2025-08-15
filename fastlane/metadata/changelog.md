Hey ✌️
This beta version of kitshn is *only* compatible with **[Tandoor v2](https://github.com/TandoorRecipes/recipes/releases/tag/2.0.0-beta-1)**.
Available here on **GitHub**, through **[Google Play testing](https://play.google.com/apps/testing/de.kitshn.android)** and through **[TestFlight](https://testflight.apple.com/join/zx1xzSMg)**.

Feel free to comment on #95 or create **a new issue** if you **encounter any issues**! :)

---

kitshn turns **one year** today 🥳🎂!
And to **celebrate** that, this beta release once again includes a *bunch* of **features**, **design** changes and **bug** fixes.

With the **release** of **Tandoor v2 stable**, the beta will also come to **an end** in the **following weeks**.

1. Added **social media** import using **AI** *(currently supporting **Instagram** and **TikTok**)* (#219)
2. Added support for creating **native timers** on **iOS** (#115).
3. Added **created by** and **created at** info to recipe details view.
4. Added **compact mode** for the home screen (#239).
5. Made multiple text areas **selectable** (#225).
6. Enabled **easier editing** of **recipes** inside **meal plan** dialog (#242).
7. Improved the **design** of multiple **components**.
8. Now **ignoring casing** when sorting **shopping list items** alphabetically (#237) (by @D3v01dZA).
9. Changed **floating toolbar** on **home screen** to be **horizontal** to resolve **scrolling issues** (#230) (by @Monforton).
10. Fixed issue where the **paste dialog** wasn't **showing** on **iOS** (#227).
11. Fixed **shopping list** not updating when adding **shopping list items** to **meal plan** entries with **empty** recipe field (#244).
12. Fixed issue where **floating toolbar** was not **expanding** on **shopping** page (#230) (by @Monforton).
13. Fixed **layout issues** when **step** has **long title** string (#223).
14. Checked **compatibility** with Tandoor **version 2.0.0**.
15. Checked **compatibility** with Tandoor **version 2.0.1**.
16. 🏴󠁥󠁳󠁣󠁴󠁿 **Catalan** has been **updated** on **[Weblate](https://hosted.weblate.org/projects/kitshn)** (by pedro miguel plasencia).
17. 🇳🇱 **Dutch** has been **updated** on **[Weblate](https://hosted.weblate.org/projects/kitshn)** (by @jstrvr).
18. 🇫🇷 **French** has been **updated** on **[Weblate](https://hosted.weblate.org/projects/kitshn)** (by @wachkyri).
19. 🇷🇺 **Russian** has been **updated** on **[Weblate](https://hosted.weblate.org/projects/kitshn)** (by @yurtpage).
20. 🇺🇦 **Ukrainian** has been **updated** on **[Weblate](https://hosted.weblate.org/projects/kitshn)** (by Максим Горпиніч).
21. 🇪🇸 **Spanish** has been **updated** on **[Weblate](https://hosted.weblate.org/projects/kitshn)** (by 1024mb).

## Commits

- feat(commonMain/import): implemented new social media import with support for TikTok and Instagram (resolved #219)
- feat(*Main): added support for creating timers on iOS (resolved #115)
- feat(commonMain/recipes): display created by and created at data in RecipeDetails.kt
- feat(commonMain/home): implemented compact home screen design (resolved #239)
- feat(commonMain/onboarding): added iOS local network permission info hint (resolved #240)
- feat(commonMain): added SelectionContainers to multiple Text components (resolved #225)
- feat(commonMain/components): improved HorizontalRecipeCardLink design
- feat(commonMain/mealplan): added floating toolbar layout to MealPlanDetailsDialog (resolved #242)
- feat(commonMain/components): added min height to RecipeStepMultimediaBox (resolved #233)
- fix(commonMain/utils): removed annoying pager state haptics
- fix(commonMain/onboarding): removed PasswordVisualTransformation to allow paste on iOS (resolved #227)
- fix(commonMain/shopping): serialization issue with entries associated to meal plans with empty recipe field (resolved #244)
- fix(commonMain/import): adapted social media import script to iOS
- fix(commonMain/import): issue with social media import script on Android
- fix(commonMain/ui): changed containerColor of HorizontalRecipeLinkCard in SelectRecipeDialog.kt
- fix(commonMain/recipes): search view not opening correctly when clicking keyword chip
- fix(commonMain/mealplan): text overlapping in MealPlanDetailsCard (resolved #245)
- fix(commonMain/home): set new parameter to true in extended list query in HomeTraditionalLayout
- fix(commonMain/shopping): apply floatingToolbarVerticalNestedScroll after initial load  (#235) (#230)
- fix(commonMain/home): changed floating toolbar to be horizontal (#234) (resolved #230)
- fix(commonMain/shopping): ignore casing when sorting shopping list items alphabetically (#236) (resolved #237)
- fix(commonMain/recipes): layout issue with long step title in RecipeStepCard.kt (resolved #223)
- chore(libs): updated library definitions
- chore(libs): version bumps
- chore(commonMain/api): marked v2.0.0/1 as compatible and removed alpha and beta versions
- l10n(Catalan): translated using Weblate
- l10n(Dutch): translated using Weblate
- l10n(French): translated using Weblate
- l10n(Russian): translated using Weblate
- l10n(Ukrainian): translated using Weblate
- l10n(Spanish): translated using Weblate