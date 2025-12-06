---
description: The kitshn project produces recurring costs. You can help cover these costs by donating.
---

<script setup>
import badge from "./components/badge.vue"
import { ref, onMounted } from 'vue'

const funding = ref([])

onMounted(async () => {
  const fundingRes = await fetch('https://funding-api.kitshn.app/v2/state/')
  funding.value = await fundingRes.json()
})

const currentYear = new Date().getFullYear()
</script>

# 💵 Funding

<template v-if="funding.events?.[0] !== undefined && currentYear !== funding.events[0].year">

::: tip Funding secured until {{ funding.events?.[0]?.year }}
The costs for **{{ currentYear }}** are already covered! :partying_face:<br>
kitshn is currently collecting money for **{{ funding.events?.[0]?.year }}**.
:::

</template>

The kitshn project produces recurring costs. You can help cover these costs by donating. This page publishes the costs and income sources for the sake of transparency.

### Sources of income
1. [Ko-Fi](https://ko-fi.com/aimok04)
2. [GitHub Sponsors](https://github.com/sponsors/aimok04)
3. Apple App Store subscriptions

### Costs

<table>
    <thead>
        <tr>
            <th>Source</th>
            <th>Label</th>
            <th>Price</th>
        </tr>
    </thead>
    <tbody>
        <tr v-for="cost in funding.costs">
            <td>{{ cost.source }}</td>
            <td>{{ cost.label }}</td>
            <td>{{ cost.amount }} {{ cost.currency }}<br><i>{{ cost.type }}</i></td>
        </tr>
    </tbody>
</table>

### Current goal

This goal gets updated automatically and collects all sources of income.
There might be some inaccuracy due to transaction and platform fees.

<table>
    <thead>
        <tr>
            <th>Goal</th>
            <th>State</th>
            <th>Total</th>
            <th>Target</th>
            <th>Last updated</th>
        </tr>
    </thead>
    <tbody v-if="funding?.events?.[0] !== undefined">
        <tr>
            <td>Cover <a href="#costs">costs</a> for <b>{{ funding.events[0].year }}</b></td>
            <td>{{ funding.events[0].percentage * 100 }} %</td>
            <td>{{ funding.events[0].total }} EUR</td>
            <td>{{ funding.events[0].goal }} EUR</td>
            <td>{{ new Date(funding.events[0].lastUpdate).toLocaleString() }}</td>
        </tr>
        <tr>
            <td colspan="5">
                <progress style="width: 100%" max="100" :value="funding.events[0].percentage * 100">
                    {{ funding.events[0].percentage * 100 }} % of {{ funding.events[0].goal }} EUR
                </progress>
            </td>
        </tr>
    </tbody>
</table>