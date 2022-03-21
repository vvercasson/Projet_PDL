<script setup lang="ts">
import { defineProps, ref } from 'vue';
import router from "@/router";
import { api } from '@/http-api';
import { ImageType } from '@/image';
import axios from 'axios';
import { report } from 'process';

const props = defineProps<{ id: number}>();

const imageList = ref<ImageType[]>([]);

api.getImageList() // fonction qui récupère la liste des images
  .then((data) => {
    imageList.value = data;
  })
  .catch(e => {
    console.log(e.message);
  });

api.getImage(props.id) // fonction qui recupere l'image avec l'id
  .then((data: Blob) => {
    const reader = new window.FileReader();
    reader.readAsDataURL(data); // transforme le Blob sous forme d'URL
    reader.onload = () => {
      const galleryElt = document.getElementById("image"); // récupération du div image
      if (galleryElt !== null) {
        const imgElt = document.createElement("img"); // creation d'une balise img
        galleryElt.appendChild(imgElt); // ajout de l'image au div image
        if (imgElt !== null && reader.result as string) {
          imgElt.setAttribute("src", (reader.result as string)); // on ajoute l'image en tant que src de la balise
        }
      }
    };
  })
  .catch(e => {
    console.log(e.message);
  });

  function supprImg(){
    api.deleteImage(props.id); // on applique la fonction qui efface l'image
    router.push({ name: 'home'}) // redirection vers la Vue Home
  }

  function showDescription(image){
    if(image.id == props.id){

      // Mise en place de la description contenant les méta données de l'image

      // création des noms pour les balises p avec l'affichage des méta données de l'image
      const textSize = document.createTextNode("Taille : "+image.size);
      const textName = document.createTextNode("Nom : "+image.name);
      const textType = document.createTextNode("Type : "+image.type);

      // création des balises texte en html
      const pSize = document.createElement("p").appendChild(textSize);
      const pName = document.createElement("p").appendChild(textName);
      const pType = document.createElement("p").appendChild(textType);

      const desc = document.getElementById("description");

      // ajout des balises dans le div description
      desc?.appendChild(pName);
      desc?.appendChild(document.createElement("br"));
      desc?.appendChild(pType);
      desc?.appendChild(document.createElement("br"));
      desc?.appendChild(pSize);
    }
  }

  function saveImg(){
    api.getImage(props.id) // utilisation de la fonction de récupération d'image
    .then((data: Blob) =>{
      var url = window.URL.createObjectURL(data); // on créer un url avec le Blob
      var enregister = document.createElement('a'); // on créer la balise a en html
      enregister.href = url;
      var type = "";
      switch(data.type){
        case "image/jpeg" || "image/jpg":
          type = "jpg";
          break;
        case "image/png":
          type = "png";
          break;
      }

      enregister.setAttribute('download','image.'+type); // quand l'image est enregistrée elle est sous forme image.jpg ou png
      document.body.appendChild(enregister);

      enregister.click(); // quand l'évenement de clique est effectué, l'enregistrement en local se fait
    });
  }
</script>

<template>
  <figure id="image"></figure>

  <div id="description">
    <div v-for="image in imageList" :key="image.id" >{{showDescription(image)}}</div>
  </div>
  <br>
  <div id="Boutons">
    <div id = "supprimer">
      <button id="btDelete" @click="supprImg">supprimer</button>
    </div>
    <div id = "telecharger">
      <button id="btSave" @click="saveImg">enregister</button>
    </div>
  </div>
  <div id="Filtres">
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
  #Filtres {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    grid-template-rows: repeat(5, 1fr);
    grid-column-gap: 0px;
    grid-row-gap: 0px;
  }

  #luminosite {
    grid-area: 1 / 1 / 2 / 2;
  }
</style>