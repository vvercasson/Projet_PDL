<script setup lang="ts">
import { ref } from 'vue'
import { api } from '@/http-api';
import { ImageType } from '@/image'
import Image from './Image.vue';

const imageList = ref<ImageType[]>([]);

api.getImageList()
  .then((data) => {
    imageList.value = data;
  })
  .catch(e => {
    console.log(e.message);
  });
</script>

<template>
  <div id="carousel">
    <h3>Gallery</h3>
    <div id="imageCarousel">
      <Image v-for="image in imageList" :id="image.id" />
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
