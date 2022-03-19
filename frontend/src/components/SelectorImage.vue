<script setup lang="ts">
import { defineProps, ref } from 'vue';
import router from "@/router";
import { api } from '@/http-api';
import { ImageType } from '@/image';

const props = defineProps<{ id: number}>();

const imageList = ref<ImageType[]>([]);

api.getImageList()
  .then((data) => {
    imageList.value = data;
  })
  .catch(e => {
    console.log(e.message);
  });

api.getImage(props.id)
  .then((data: Blob) => {
    const reader = new window.FileReader();
    reader.readAsDataURL(data);
    reader.onload = () => {
      const galleryElt = document.getElementById("gallery");
      if (galleryElt !== null) {
        const imgElt = document.createElement("img");
        galleryElt.appendChild(imgElt);
        if (imgElt !== null && reader.result as string) {
          imgElt.setAttribute("src", (reader.result as string));
        }
        /*const divDescription = document.createElement("div");
        const h4 = document.createElement("h4");
        const titreH4 = document.createTextNode("Description");
        h4.appendChild(titreH4);
        divDescription.appendChild(h4);
        galleryElt.appendChild(divDescription);*/
      }
    };
  })
  .catch(e => {
    console.log(e.message);
  });

  function supprImg(){
    api.deleteImage(props.id);
    router.push({ name: 'home'})
  }

  function showDescription(image){
    if(image.id == props.id){
      const textSize = document.createTextNode("Taille : "+image.size);
      const textName = document.createTextNode("Nom : "+image.name);
      const textType = document.createTextNode("Type : "+image.type);

      const pSize = document.createElement("p").appendChild(textSize);
      const pName = document.createElement("p").appendChild(textName);
      const pType = document.createElement("p").appendChild(textType);

      const desc = document.getElementById("description");
      desc?.appendChild(pName);
      desc?.appendChild(document.createElement("br"));
      desc?.appendChild(pType);
      desc?.appendChild(document.createElement("br"));
      desc?.appendChild(pSize);
    }
  }
</script>

<template>
  <figure id="gallery"></figure>

  <div id="description">
    <div v-for="image in imageList" :key="image.id" >{{showDescription(image)}}</div>
  </div>
   <div id="Filtres">
    <div id = "supprimer">
        <button id="btDelete" @click="supprImg">supprimer</button>
    </div>
    <div id = "luminosite">
      <h3 id="light">Luminosité</h3>
      <button id="increaseLight">-</button>
      <button id="reduceLight">+</button>
    </div>

    <div id = "histogramme">
      <h3 id="titleHisto">Histogramme</h3>
      <button id="histButton">appliquer</button>
    </div>
    
    <div id = "couleur">
      <h3 id="titleColor">Filtre couleur</h3>
      <label for="inputColor">Couleur : </label> 
      <input type="color" id="inputColor" name="couleur">
      &nbsp;
      <button id="colorButton">appliquer</button>
    </div>

    <div id = "blur">
      <h3 id="titleBlur">Filtre flou</h3>
      <select>
        <option id="moyen">Moyen</option>
        <option id="gaussien">Gaussien</option>
      </select>
      <br>
      <label for="blurLevel">Niveau de Flou : </label> 
      <input type="number" id="blurLevel">
      &nbsp;
      <button id="blurButton">appliquer</button>
    </div>

    <div id="contour">
      <h3 id="titleContour"> Filtre Contour</h3>
      <button id="light">Filtre contour</button>
    </div>
  </div>
</template>

<style>
  img {
    width: 300px; 
    height: 300px; 
  }
</style>