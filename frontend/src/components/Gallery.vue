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
    button.setAttribute("onclick","supprImg(image.id)");
    //button.addEventListener("click",supprImg(image.id));

    var div = document.getElementById("carousselImg");
    var newDiv = document.createElement("div");
    newDiv.setAttribute("id",image.id);

    newDiv.appendChild(img);
    newDiv.appendChild(document.createElement("br"))
    newDiv.appendChild(button);
    div.appendChild(newDiv);
    
    //button.click();
  }

</script>

<template>
  <h3>Gallery</h3>
  <div id="gallery">
    <div id = "carousselImg">
      <img :id="image.name" :key="image.id" v-for="image in imageList" :src="showImageGalery(image)" @click="clickImg(image.id)">
    </div>
  </div>
</template>

<style>
  img{
    width: 300px;
    height: 300px;
    margin-top: 10px;
    margin-left: 10px;
  }
  #gallery{
    display: grid;
    grid-template-columns: repeat(2, 1fr) 8fr repeat(2, 1fr);
    grid-template-rows: repeat(2, 1fr) 8fr repeat(2, 1fr);
    grid-column-gap: 0px;
    grid-row-gap: 0px;
  } 

  #carousselImg{
    grid-area: 1 / 3 / 4 / 4;
    border: 5px solid #851680;
    border-radius: 32px 32px 32px 32px;
  }

</style>
