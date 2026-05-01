> [!CAUTION]
> ‼️ Android will become a locked-down platform. Learn more: https://keepandroidopen.org/

<img width="1604" height="267" alt="kitshn (for Tandoor)" src="https://github.com/user-attachments/assets/746041d2-1a85-44b4-a03b-aef0c792cc87" />


### Hey ✌️

This release includes a lot of work by @jonsch318 🙌

- Added dedicated **recipe selection** dialog (#375) (thanks to @jonsch318).
  - *Improves recipe selection for meal plan entries for example.*
- Added **meal type selection** bottom sheet (#386) (thanks to @jonsch318).
- Added meal type **editing** and **creation** (#386) (thanks to @jonsch318).
- Added **placeholder** to **step editor** (#387) (thanks to @jonsch318).
- Added **double click** to **check items** in shopping list (#381) (thanks to @jonsch318).
- Added more **packaging formats** including `.deb` `.rpm` `.appimage` and `.msi` (#392) (#334) (
  thanks to @jonsch318).
- Added *experimental support* for Android 7 (#304).
  - *Please feel free to give feedback in #304.*
- Fixed **flickering** when loading home page (#377) (thanks to @jonsch318).
- Fixed unable to enter `< 1` **servings values** in **meal plan** dialog (#394).
- *Multiple small tweaks and design improvements.*

---

## Commits

- feat(ui): replaced ListItem with SegmentedListItem + small tweaks in MealTypePickerField bottom
  sheet
- feat: add delete to meal-type dialog and select created meal-type in picker
- feat: add time, color fields, meal-type creation dialog
- feat: add new MealTypePickerField
- feat(ui): adapt SettingsListItem.kt to new SegmentedListItem and adapt usages
- feat(ui/ingredient): adapt IngredientItem.kt to new SegmentedListItem
- feat: move desktopApp & fix & extended packaging
- feat: move composeApp to shared
- feat: first working legacy version
- feat: implemented setting to double-click shopping items to complete
- feat: change version setting appearance for compatibilities
- feat: use semver versioning for compatibility computation
- feat(ui/RecipeSearchDialog): replaced Dialog with AdaptiveFullscreenDialog
- feat(ui/recipeSearchField): add UnfoldMore icon as trailingIcon
- feat(ui/recipeSearch): always show loading indicator when loading content
- feat: consolidate recipe search and selection
- feat(build.gradle): lower minSdk to 24 in androidApp and shared (#304)
- fix(strings.xml): replace action_add_meal_type with action_create_meal_type
- fix(ui): replace "Add new meal type" with "New meal type" for consistency
- fix(ui): change border size and padding of LazyVerticalGrid
- fix(ui): add showColorPickerDialog = false after hiding bottom sheet in ColorPickerField
- fix(ui): replace border color in ColorPickerField bottom sheet
- fix(ui): remove title from ColorPickerField bottom sheet for consistency
- fix(ui): use sheetState.hide() when closing bottom sheet
- fix(ui): use clickable overlay for TimeField for consistency
- fix(ui): use clickable overlay for ColorPickerField for consistency
- fix(ui): use clickable overlay for MealTypePickerField for consistency
- fix: remove old meal-type search input
- fix: requests and create selection and default mealtype
- fix: add meal type sheet divider only if there are meal-types
- fix(ui): use clickable overlay for DateField for consistency
- fix(androidApp): bump compileSdk to 37
- fix(ui/mealplan): enable entering values < 1 in servings field in
  MealPlanCreationAndEditDialog.kt (resolved #394)
- fix(iosApp): move bugsnag api token to Config.xcconfig
- fix(iosMain/Utils): adapt to new compose version
- fix(fastlane/metadata): remove faulty metadata
- fix(shared/build.gradle.kts): disable iosX64 target
- fix(ui/input): code clean up and logic fix in RecipeSearchField.kt
- fix(ui/mealplan): remove accidental rounding of servings value in MealPlanDetailsCard.kt (#394)
- fix: desktopApp debugging and log verbosity
- fix: gradle version print & cache invalidation
- fix fastlane version management
- fix: split value strings
- fix: ship progruard rules
- fix: cleanup build files
- fix: complete migration to agp 9
- fix: use kmp library
- fixup: remove libs comment
- fix: move to jvm 21
- fix: add placeholder to step editor
- fix: reduce grid rerenders in home sections
- fix(ui/SettingsListItem): don't apply .copy(alpha = 0.8f) when containerColor is Unspecified
- fix: clear up comment on versioning
- fix: remove unnecessary imageLoaders
- fix(ui/AdaptiveFullscreenDialog.kt): apply full height when bottom bar isn't shown
- fix: home search double request
- fix: FAP recipe view overlaying
- chore(libs): bump compose versions
- chore: update deps except adp
- chore: add french translation to double click check
- chore(ui/RecipeSearchField): code improvements
- git(.gitignore): add /androidApp/release