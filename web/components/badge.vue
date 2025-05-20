<script setup>
const props = defineProps(["src","link","label"])
</script>

<template>
    <a :href="props.link">
        <img :src="`/images/badges/${ src }.png`" :alt="`Get it on ${ label }`" style="height: 80px !important">
    </a>
</template>