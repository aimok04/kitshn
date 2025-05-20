---
title: Screenshots
description: Design impressions from the kitshn app

aside: false
outline: false
pageClass: page-screenshots
---

<script setup>
    import screenshots from "./components/screenshots.vue"
</script>

# Home / Recipe view

<screenshots 
    :imagesMobile="[ 'home', 'recipe' ]"
    :imagesTablet="[ 'home' ]" />

---

# Cooking mode

<screenshots 
    :images="[ 'cooking_mode' ]" />

---

# Search

<screenshots 
    :images="[ 'search', 'search_dialog' ]" />

---

# Meal plan

<screenshots 
    :images="[ 'meal_plan', 'meal_plan_new' ]" />

---

# Tools

<screenshots 
    :images="[ 'allocate_ingredients' ]" />

<style>
    .page-screenshots #VPContent {
        padding-right: 0px !important;
    }
</style>