Hey ✌️
This is the third version of kitshn **compatible** with the API changes for *
*[Tandoor v2](https://github.com/TandoorRecipes/recipes/releases/tag/2.0.0-alpha-2)**. 🥳
New versions of kitshn will **no longer** support instances on **v1.5.***.

This beta is available here on **GitHub**, through **[Google Play testing](https://play.google.com/apps/testing/de.kitshn.android)** and through **[TestFlight](https://testflight.apple.com/join/zx1xzSMg)**.

Although I have tested the app with **Tandoor v2.0.0-alpha-2**, there could still be some minor API
changes that I might have missed, so **please feel free** to comment on #95 or create **a new issue** if you **encounter any issues**! :)

---

1. Added support for **AI import**, a **new** feature of **Tandoor v2
   ** ([learn more](https://github.com/TandoorRecipes/recipes/blob/feature/vue3/docs/system/configuration.md#ai-integration)) (
   #95).
2. Implemented button to **clear shopping list** from **all** or **done** entries (#95).
3. Added **recipe name** to **sharing text** for easier identification (#185).
4. Enabled users to **disable** the **shopping mode enlargement** (#181).
5. Checked **compatibility** with Tandoor **version 2.0.0-alpha-2**.

## Highlights

- feat(build.gradle): added new "mobileMain" source set to unify peekaboo for androidMain and
  iosMain
- feat(androidMain/ui): added disableAnimation parameter to AdaptiveFullscreenDialog
- feat(commonMain/shopping): added "enlarge shopping mode" setting (resolved #181)
- feat(commonMain/shopping): implemented clear shopping list dialog (#95)
- feat(commonMain/ui): replaced peekaboo with FileKit in PhotoPickerDialog
- feat(commonMain/import): implemented support for import using AI
- feat(mobileMain/ui): added photo taking dialog using peekaboo library
- feat(commonMain/recipes): added recipe name to sharing text (resolved #185)
- fix(build.gradle): issue with applying default hierarchy template
- fix(commonMain/shopping): don't purge checked items from cache
- fix(commonMain/shopping): issue with deleting shopping list entries (resolved #184)
- chore(commonMain/api): marked v2.0.0-alpha-2 as compatible
- l10n(Ukrainian): translated using Weblate
- l10n(German): translated using Weblate
- l10n(Czech): added translation using Weblate
- l10n(Portuguese (Brazil)): added translation using Weblate