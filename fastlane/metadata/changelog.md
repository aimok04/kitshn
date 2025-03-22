Hey ✌️
This week's release just contains some small new features, minor improvements and a new language:

1. 🇨🇿 **Czech** has been added on **[Weblate](https://hosted.weblate.org/projects/kitshn)** (by
   @hernikplays).
2. Implemented **human readable date labels** for iOS (already supported on Android).
3. Now **defaulting** to **recipe's servings value** when adding to **meal plan** (#147) (by
   @Monforton).
4. Now **defaulting** to **recipe's servings value** when selecting a recipe in **meal plan
   creation/edit dialog**.
5. Fixed error when trying to **add recipe ingredients** to **shopping list** (HTTP 204 non-zero
   Content-Length).
3. Checked **compatibility** with Tandoor **version 1.5.33**.

## Highlights

- feat(*Main/utils): implemented human readable date labels for iOS and jvm (unified)
- feat(commonMain/recipes): default servings when adding to meal plan from details view (#147)
- feat(commonMain/mealplan): default servings when selecting recipe in meal plan creation/edit
  dialog
- fix(commonMain/api): issue where server sends non-zero Content-Length HTTP 204 response
- l10n(Ukrainian): translated using Weblate
- l10n(German): translated using Weblate
- l10n(Czech): added translation using Weblate
- l10n(Czech): translated using Weblate
- chore(commonMain/api): marked v1.5.33 as compatible