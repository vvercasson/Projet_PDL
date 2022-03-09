<script setup lang="ts">
import { defineProps } from 'vue';
import { api } from '@/http-api';

const props = defineProps<{ id: number }>()

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
      }
    };
  })
  .catch(e => {
    console.log(e.message);
  });
</script>

<template>
  <figure id="gallery"></figure>
  
  <div id="Filtres">

    <div id = "luminosite">
      <h3 id="light">Luminosité</h3>
      <button id="increaseLight">+</button>
      <button id="reduceLight">-</button>
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

<style></style>