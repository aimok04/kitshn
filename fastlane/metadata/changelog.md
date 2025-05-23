## v2.0.0-beta.6.1

This release reverts dependency upgrades which caused visual issues.

---

Hey ✌️
This is the sixth version of kitshn **compatible** with the API changes for **[Tandoor v2](https://github.com/TandoorRecipes/recipes/releases/tag/2.0.0-alpha-2)**. 🥳
New versions of kitshn will **no longer** support instances on **v1.5.***.

This beta is available here on **GitHub**, through **[Google Play testing](https://play.google.com/apps/testing/de.kitshn.android)** and through **[TestFlight](https://testflight.apple.com/join/zx1xzSMg)**.

Although I have tested the app with **Tandoor v2.0.0-alpha-4**, there could still be some minor API
changes that I might have missed, so **please feel free** to comment on #95 or create **a new issue** if you **encounter any issues**! :)

---

1. Improved **search dialog** layout and actions (#203).
2. Fixed issue where **wrong meal plan entries** where shown **on home** (#200).
3. Fixed issue where some **recipe data** wasn't updated **correctly** (#171).
4. 🇪🇸 **Spanish** has been brought **up to date** on **[Weblate](https://hosted.weblate.org/projects/kitshn)** (by @1024mb).
5. 🇮🇹 **Italian** has been **updated** on **[Weblate](https://hosted.weblate.org/projects/kitshn)** (by @KindaArtsy).
6. 🇫🇷 **French** has been **updated** on **[Weblate](https://hosted.weblate.org/projects/kitshn)** (by @whayn).

## Highlights

- feat(androidMain/AndroidManifest.xml): added android:enableOnBackInvokedCallback="true" to AppActivity
- feat(commonMain/api): update recipeOverview map when retrieving recipe model from server (resolved #171)
- feat(commonMain/search): improved buttons and cursor position when reopening dialog (resolved #203)
- feat(web/vitepress): changed website description
- feat(web): merged kitshn.app into main repo
- feat(README.md): update alerts
- feat(README.md): added certificate fingerprints (resolved #202)
- fix(commonMain/search): crash on Android due to using rememberSaveable with TextFieldValue
- fix(commonMain/home): issue where wrong meal plan entries were shown in promotion section (resolved #200)
- chore(gradle): update gradle/agp
- chore(libs): version bumps
- chore(commonMain/api): marked v2.0.0-alpha-4 as compatible
- chore(web): update dependencies
- github(workflows): added "Deploy kitshn.app" workflow
- l10n(Ukrainian): translated using Weblate
- l10n(Spanish): translated using Weblate
- l10n(French): translated using Weblate
- l10n(Polish): translated using Weblate
- l10n(Dutch): translated using Weblate
- l10n(Catalan): translated using Weblate
- l10n(Catalan): added translation using Weblate
- l10n(Polish): translated using Weblate
- l10n(Polish): fix short_description.txt being too long