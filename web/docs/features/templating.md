# Templating <Badge style="margin-top: 14px" type="tip" text="v1.0.0-alpha.10" />

kitshn supports these three kinds of templating:

<div v-pre>

- `{{ ingredients[xyz] }}` (e.g. *300g Potatoes*)
- `{{ scale(xyz) }}` (Multiplies value *xyz* according to servings)
- `{# nice comment #}`

</div>

## Ingredient values <Badge style="margin-top: 14px" type="tip" text="v2.0.0-beta.10" />

kitshn also supports templating for these ingredient values:

<div v-pre>

- `{{ ingredients[xyz].amount }}` (e.g. *300*)
- `{{ ingredients[xyz].unit }}` (e.g. Grams)
- `{{ ingredients[xyz].food }}` (e.g. Potatoes)
- `{{ ingredients[xyz].note }}` (e.g. sliced)

</div>

Learn more about templating in Tandoor [on here](https://docs.tandoor.dev/features/templating/).