> [!CAUTION]
> ‼️ Android will become a locked-down platform. Learn more: https://keepandroidopen.org/

<img width="1604" height="267" alt="kitshn (for Tandoor)" src="https://github.com/user-attachments/assets/746041d2-1a85-44b4-a03b-aef0c792cc87" />


### Hey ✌️

This release again includes a lot of work by @jonsch318 🙌

- Added support for mutual TLS (mTLS) (#414).
- Added setting to change request timeouts (#359) (#382).
- Added credential storage encryption (#361) (#360).
- Now showing created_by value for shopping list entries.
- Unified recipe importing and recipe creation into the plus button (#397).
- Fixed empty chip when amount=0 for shopping list entries.
- Fixed error 400 when trying to favorite a recipe (#357) (#400).
- Fixed weird behavior with meal plan servings field (#407) (#408).
- Fixed ShoppingItemDoubleClick and PropertiesShowFractionalValues settings being linked (#417) (#418).
- Fixed bug reporting on iOS. 
- Dependency version bumps.
- 🇨🇿 **Czech** has been **updated** on **[Weblate](https://hosted.weblate.org/projects/kitshn)** (by @hernikplays).
- 🇪🇸 **Spanish** has been **updated** on **[Weblate](https://hosted.weblate.org/projects/kitshn)** (by @cpsaul-dev). 
- 🇩🇪 **German** has been **updated** on **[Weblate](https://hosted.weblate.org/projects/kitshn)** (by @EwgB and @aimok04). 
- 🇨🇳 **Chinese** has been **updated** on **[Weblate](https://hosted.weblate.org/projects/kitshn)** (by @lycia324). 
- 🇵🇹 **Portuguese** has been **updated** on **[Weblate](https://hosted.weblate.org/projects/kitshn)** (by @SantosSi). 
- 🇷🇺 **Russian** has been **updated** on **[Weblate](https://hosted.weblate.org/projects/kitshn)** (by @Xapitonov).
- 🇸🇪 **Swedish** has been **updated** on **[Weblate](https://hosted.weblate.org/projects/kitshn)** (by @bittin, @olsson82 and @MrHaddock7).
- **Arabic** has been **updated** on **[Weblate](https://hosted.weblate.org/projects/kitshn)** (by @j-neko).

---

## Commits

- feat(ui/shopping): improve layout of and add created_by value to IndividualShoppingListEntryDetailCard.kt
- feat(ui): improve SettingsListItem look when selected = true
- feat: add mTLS support (#414)
- feat: add settings to change http client timeouts (resolves #359) (#382)
- feat(ui): unify recipe creation and import buttons
- fix: crash when logging out in KitshnViewModel.kt
- fix(ui/shopping): hide amount chip when amount = 0 in IndividualShoppingListEntryDetailCard.kt
- fix: derive entryByRecipeId fixing favorites button
- fix: add step and precision arguments to double fields
- fix: mealplan step amount buttons ceil & floor
- fix(ios/db): add ExperimentalForeignApi annotation to AppDatabase.closeAndDelete()
- fix: double click check setting being coupled with fractional values setting
- fix(ios/bugsnag): ensure that logs get send with bugsnag reports
- chore(libs): rename versions
- chore(libs): version bumps
- chore(commonMain/api): marked v2.0/1/2/3 as incompatible (#427)
- l10n(Czech): translated using Weblate
- l10n(Spanish): translated using Weblate
- l10n(German): translated using Weblate
- l10n(Chinese (Simplified Han script)): translated using Weblate
- l10n(Portuguese): translated using Weblate
- l10n(Portuguese): added translation using Weblate
- l10n(Breton): added translation using Weblate
- l10n(Arabic): translated using Weblate
- l10n(Russian): translated using Weblate
- l10n(Swedish): translated using Weblate
- l10n(Swedish): added translation using Weblate
- improve SecureCredentialStore.ios.kt
- encrypt credential storage with platform-specific secure storage