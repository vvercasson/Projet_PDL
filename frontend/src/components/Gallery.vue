<script setup lang="ts">
import { createElementBlock, ref } from 'vue'
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
    //router.push({ name: 'selectorImage', params: {id: image}});
    var url = 'images/'+image.id
    return url;
  }

  function clickImg(id){
    router.push({ name: 'selectorImage', params: {id: id}});
  }

  function insertAfter(newNode, existingNode) {
    existingNode.parentNode.insertBefore(newNode, existingNode.nextSibling);
  }

  function supprImg(id){
    api.deleteImage(id);
    router.push({ name: 'home'})
  }

  function btSuppr(image){
    var img = document.getElementById(image.name);
    var button = document.createElement("button");
    button.append("supprimer");
    button.setAttribute("id","bt"+image.name);
    //button.addEventListener("click",supprImg(image.id));

    var div = document.getElementById("carousselImg");
    var newDiv = document.createElement("div");
    newDiv.setAttribute("id","divGallery");

    newDiv.appendChild(img);
    newDiv.appendChild(document.createElement("br"))
    newDiv.appendChild(button);
    div.appendChild(newDiv);
    
  }

</script>

<template>
  <h3>Gallery</h3>
  <div>
    <div id = "carousselImg">
      <img :id="image.name" :key="image.id" v-for="image in imageList" :src="showImageGalery(image)" @click="clickImg(image.id)" @load="btSuppr(image)">
    </div>
  </div>
</template>

<style>
  img{
    width: 300px;
    height: 300px;
  }
  #carousselImg{
    display: flex;
  } 

  #carousselImg div{
    margin: auto;
  }

</style>
