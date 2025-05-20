<script setup>
const props = defineProps(["images", "imagesMobile", "imagesTablet"])

const imgM = [ ...(props.imagesMobile || []), ...(props.images || []) ]
const imgT = [ ...(props.imagesTablet || []), ...(props.images || []) ]
</script>

<template>
    <div class="screenshots-row">
        <template v-for="image of imgM">
            <a :href="`/images/screenshots/light_phone_${ image }.png`">
                <img :src="`/images/screenshots/light_phone_${ image }.png`" />
            </a>
        </template>

        <template v-for="image of imgM">
            <a :href="`/images/screenshots/dark_phone_${ image }.png`">
                <img :src="`/images/screenshots/dark_phone_${ image }.png`" />
            </a>
        </template>
    </div>

    <div class="screenshots-row">
        <template v-for="image of imgT">
            <a :href="`/images/screenshots/light_tablet_${ image }.png`">
                <img alt="" :src="`/images/screenshots/light_tablet_${ image }.png`" />
            </a>
        </template>

        <template v-for="image of imgT">
            <a :href="`/images/screenshots/dark_tablet_${ image }.png`">
                <img alt="" :src="`/images/screenshots/dark_tablet_${ image }.png`" />
            </a>
        </template>
    </div>
</template>

<style>
.screenshots-row {
    display: flex;
    flex-direction: row;
    flex-wrap: wrap;
    
    gap: 24px;
    margin-top: 16px;
}

.screenshots-row > a > img {
    max-height: 500px;
}
</style>