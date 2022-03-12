<script setup lang="ts">
import { ref } from 'vue';
import router from "@/router";
import { api } from '@/http-api';
import { ImageType } from '@/image'

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
  var imageid = event.target.value;
  //router.push({ name: 'image', params: { id: selectedId.value } })
  const imageShow = document.getElementById("image");
  if (imageShow !== null) {
    var varP = document.getElementById("divImage");
    if(varP != null){
      varP.parentNode?.removeChild(varP);
    }

    const imgElt = document.createElement("img");
    var imageUrl = "http://localhost:4000/images/"+imageid;

    imgElt.setAttribute("src",imageUrl); 
    imgElt.setAttribute("id","divImage");
    //var text = document.createTextNode("l'image");
    //imgElt.appendChild(text);
    imageShow.appendChild(imgElt);

    // show filter div
    var divHide = document.getElementById("Filtres")?.style.visibility;
    if (divHide == 'hidden') {
                document.getElementById("Filtres").style.visibility = 'visible';
            }
  }
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

  <div id="image"></div>

  <div style="visibility: hidden;" id="Filtres">
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
  #divImage{
    width: 500px;
    height: 500px;
  }
</style>
