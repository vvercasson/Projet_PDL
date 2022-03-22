<script setup lang="ts">
import { ref } from 'vue';
import router from "@/router";
import { api } from '@/http-api';
import { ImageType } from '@/image'
import func from '../../vue-temp/vue-editor-bridge';

const selectedId = ref(-1);
const imageList = ref<ImageType[]>([]);
getImageList();


function getImageList() {
  api.getImageList().then((data) => {
    imageList.value = data;
  }).catch(e => {
    console.log(e.message);
  });
}

function showImage(event)  {
  router.push({ name: "selectorImage",params: {id: event.target.value}});
}


</script>

<template>
  <div>
    <h3>Choose an image</h3>
    <div>
      <select v-model="selectedId" @change="showImage($event)">
        <option v-for="image in imageList" :value="image.id" :key="image.id">{{ image.name }}</option>
      </select>
    </div>
  </div>
</template>

<style>
</style>
