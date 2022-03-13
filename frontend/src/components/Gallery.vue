<script setup lang="ts">
import { ref } from 'vue'
import { api } from '@/http-api';
import { ImageType } from '@/image'
import router from "@/router";
import Image from './Image.vue';
import func from '../../vue-temp/vue-editor-bridge';

const imageList = ref<ImageType[]>([]);

api.getImageList()
  .then((data) => {
    imageList.value = data;
  })
  .catch(e => {
    console.log(e.message);
  });

  function showImageGalery(event){ 
    console.log(event);
    //router.push({ name: 'image', params: {id: event.target.id}});
  }
</script>

<template>
  <div>
    <h3>Gallery</h3>
    <div id="carousel">
      <div id="imageCarousel">
      <Image v-for="image in imageList" :id="image.id" :key="image.id" @click="showImageGalery($event)"/>
    </div>
    </div>
  </div>
</template>

<style scoped>
  #carousel {width: 80%; max-width: 1000px;}
  #carousel #imageCarousel {
    position: relative;
    width: 500%;
    margin: 0;
    padding: 0;
    text-align: left;
  }

  Image{
    width: 20%; 
    height: auto; 
    float: left;
  }
</style>
