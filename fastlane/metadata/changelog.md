Hey ✌️
This week's release just contains bug fixes and minor improvements:

1. Improved **size ratio** on pages with **list detail layouts**.
2. Improved **scrolling** in **shopping** and **shopping mode** (#140).
3. Improved **error dialog** and **error handling**.
4. Added **placeholder icon** to empty **recipe books**.
5. Fixed annoying **error dialog** when having **empty recipe books** (#138).
6. Fixed **» servings «** label being in **German** when language is set to **English** (#134).
7. Fixed **crash** when selecting a **supermarket** without **assigned categories**.
8. Fixed **keyboard** disappearing when **creating** new **shopping list entry** on iOS (#141).
9. 🇹🇷 **Turkish** has been brought **up to date** (by @mikropsoft) (#137).

## Highlights

- feat(commonMain/ui): improved size ratio in KitshnListDetailPaneScaffold
- feat(commonMain/shopping): replaced ElevatedAssistChip with custom implementation in
  ShoppingListEntryListItem
- feat(commonMain/shopping): improved amountChips value strings in ShoppingListEntryListItem
- feat(commonMain/recipeBooks): show placeholder icon when book is empty to avoid endless loading
  animation
- feat(commonMain/requests): improved error handling and dialog
- feat(fastlane): added automatic release deployment with fastlane
- fix(commonMain/recipeBooks): ensure filter != null when fetching filter entries for thumbnail (
  resolved #138)
- fix(commonMain/strings.xml): removed common_plural_wo_count and fixed translation issue (resolved
  #134)
- fix(commonMain/shopping): unified row height in ShoppingListEntryListItem to fix scrolling issue (
  resolved #140)
- fix(commonMain/shopping): disabled auto focus of food input field in
  ShoppingListEntryCreationDialog (#141)
- fix(commonMain/shopping): crash when selecting supermarket with zero categories
- fix(commonMain/ui): blank screen when closing detail pane when expanded in
  KitshnListDetailPaneScaffold
- fix(commonMain/components): set link typography color to primary in
  MarkdownRichTextWithTimerDetection
- fix(commonMain/shopping): improved shopping mode layout
- l10n(Ukrainian): translated using Weblate
- l10n(Turkish): update translation
- chore(libs): version bumps
- chore(gradle): update gradle/agp