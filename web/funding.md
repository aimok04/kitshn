---
description: The kitshn project produces recurring costs. You can help cover these costs by donating.
---

<script setup>
import badge from "./components/badge.vue"
import { ref, onMounted } from 'vue'

const funding = ref([])

onMounted(async () => {
  const fundingRes = await fetch('https://funding-api.kitshn.app/v1/state/')
  funding.value = await fundingRes.json()
})
</script>

# Funding

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
            <th>State</th>
            <th>Total</th>
            <th>Goal</th>
            <th>Last updated</th>
        </tr>
    </thead>
    <tbody v-if="funding?.events?.[0] !== undefined">
        <tr>
            <td>{{ funding.events[0].percentage * 100 }} %</td>
            <td>{{ funding.events[0].total }} EUR</td>
            <td>{{ funding.events[0].goal }} EUR</td>
            <td>{{ new Date(funding.events[0].lastUpdate).toLocaleString() }}</td>
        </tr>
        <tr>
            <td colspan="4">
                <progress style="width: 100%" max="100" :value="funding.events[0].percentage * 100">
                    {{ funding.events[0].percentage * 100 }} % of {{ funding.events[0].goal }} EUR
                </progress>
            </td>
        </tr>
    </tbody>
</table>