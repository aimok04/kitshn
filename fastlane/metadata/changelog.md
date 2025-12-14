Hey ✌️
This beta version of kitshn is *only* compatible with **[Tandoor v2](https://github.com/TandoorRecipes/recipes/releases/tag/2.0.0-beta-1)**.
Available here on **GitHub**, through **[Google Play testing](https://play.google.com/apps/testing/de.kitshn.android)** and through **[TestFlight](https://testflight.apple.com/join/zx1xzSMg)**.

Feel free to comment on #95 or create **a new issue** if you **encounter any issues**! :)

---

This release mostly includes *big fixes*, especially for *social media* and *AI import*.

Thank you for **500 stars** on GitHub! 🥳

1. Increased *timeout values* for multipart requests (e.g. AI import) (#301).
2. Fixed issue where **AI / social media import** fails, when there is no *servings* value given (#295). 
3. Fixed issue where **post content** couldn't be fetched from **instagram** posts (#302).
4. Fixed issue where **recipe books** wouldn't show some recipes when exceeding *50 recipes* (#297).

## Commits

- fix(commonMain/api): increased timeout values for multipart requests to 60s (#301)
- fix(commonMain/import): improved fetching post content for instagram posts (resolved #302)
- fix(commonMain/books): issue when recipe book has more than 50 books (resolved #297)
- fix(commonMain/import): allow interaction with webview in RecipeImportSocialMediaDialog.kt (resolved #278)
- fix(commonMain/import): improved animation in RecipeImportSocialMediaDialog.kt (#278)
- fix(commonMain/utils): disable "day_after_tomorrow" and "day_before_yesterday" labels in english
- fix(commonMain/import): replace servings value with 1 if null (#295)
- chore(libs): version bumps