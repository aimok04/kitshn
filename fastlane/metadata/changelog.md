Hey ✌️
This beta version of kitshn is *only* compatible with **[Tandoor v2](https://github.com/TandoorRecipes/recipes/releases/tag/2.0.0-beta-1)**.
Available here on **GitHub**, through **[Google Play testing](https://play.google.com/apps/testing/de.kitshn.android)** and through **[TestFlight](https://testflight.apple.com/join/zx1xzSMg)**.

Feel free to comment on #95 or create **a new issue** if you **encounter any issues**! :)

---

One release never comes alone 🐞
So here is the cleanup of the mess that v2.0.0-beta.11.1 caused:

1. Fixed **copy, cut and paste** not working on *iOS* and *iPadOS* (#273).
2. Fixed **crash** when using social media import *twice*.
3. Fixed social media **import** of **TikTok** videos not working (#276).
4. Fixed **session timeout** issue when using **social media import**.
5. Fixed **url** not being set as *sourceUrl* value of recipe when using **social media import**.
6. Added better **image fetching** for Instagram posts (previously included *play arrow* overlay for reels).

## Commits

- feat(commonMain/import): improved image fetching when importing TikTok with social media import
- feat(commonMain/import): improved image fetching for instagram in social media import
- fix(libs): downgraded agp for compatibility with F-Droid
- fix(commonMain/api): increased timeout values for multipart requests to prevent timeouts for long requests (e.g. /ai-import/)
- fix(commonMain/import): added social media import url as sourceUrl when creating recipe
- fix(gradle): problem with compiling for android
- fix(commonMain/import): don't fetch imageURL if blank (resolved #266)
- fix(commonMain/import): wait until content is loaded in social media import (resolved #276)
- fix(commonMain/import): added error handler to fetchAiRequestState in RecipeImportSocialMediaDialog.kt (resolved #275)
- fix(commonMain/import): improved detection of iOS platform in social media import
- fix(commonMain/import): added error when recipe description is no longer than three characters in RecipeImportSocialMediaDialog.kt
- fix(libs): downgrade adaptive and compose material3 to fix paste issue on iOS (resolved #273)
- fix(mobileMain/ui): compiling error in ChoosePhotoBottomSheet.mobile.kt
- chore(libs): version bumps and downgrade compose