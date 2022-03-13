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

  function showImageGalery(image){ 
    //router.push({ name: 'image', params: {id: event.target.id}});
    var url = 'images/'+image
    return url;
  }

  function clickImg(id){
    router.push({ name: 'image', params: {id: id}});
  }
</script>

<template>
  <h3>Gallery</h3>
  <div>
    <div id = "carousselImg">
      <img :id="image.name" :key="image.id" v-for="image in imageList" :src="showImageGalery(image.id)" @click="clickImg(image.id)">
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
