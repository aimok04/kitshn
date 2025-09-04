<script setup>
const props = defineProps(["images", "deviceType"])
</script>

<template>
    <div class="screenshots-row">
        <template v-for="image of props.images">
            <a :href="`/images/screenshots/${ props.deviceType }Screenshots/LIGHT_${ image }.png`">
                <img :src="`/images/screenshots/${ props.deviceType }Screenshots/LIGHT_${ image }.png`" />
            </a>
        </template>

        <template v-for="image of props.images">
            <a :href="`/images/screenshots/${ props.deviceType }Screenshots/DARK_${ image }.png`">
                <img :src="`/images/screenshots/${ props.deviceType }Screenshots/DARK_${ image }.png`" />
            </a>
        </template>
    </div>
</template>

<style>
.screenshots-row {
    display: flex;
    flex-direction: row;
    flex-wrap: wrap;
    
    gap: 4px;
    margin-top: 24px;
}

.screenshots-row > a > img {
    max-height: 500px;
}
</style>