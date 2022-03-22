<script setup lang="ts">
import { defineProps, ref } from 'vue';
import router from "@/router";
import { api } from '@/http-api';
import { ImageType } from '@/image';
import axios from 'axios';
import { report } from 'process';
import { request } from 'http';

const props = defineProps<{ id: number}>();

const imageList = ref<ImageType[]>([]);

const target = ref<Blob>();

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

  function saveImg(event){
    var id;
    console.log(event.target);
    if(event.target.id == "btSave"){
      api.getImage(props.id)
      .then((data: Blob) =>{
        var url = window.URL.createObjectURL(data);
        var enregister = document.createElement('a');
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

      enregister.setAttribute('download','image.'+type); 
      document.body.appendChild(enregister);

      enregister.click(); 
    });
    }
    else{
      var enregister = document.createElement('a');
      var url = window.URL.createObjectURL(target.value);
      enregister.href = url;
      enregister.setAttribute('download','image.jpg'); 
      document.body.appendChild(enregister);

      enregister.click();
    }
  }
  function submitFilter(event){
    const imageShow = document.getElementById("imageFiltre");
    if (imageShow !== null) {
      var varP = document.getElementById("filtre");
    }
    if(varP != null){
      varP.parentNode?.removeChild(varP);
    }
    console.log(event.target.id); // faire le if pour chaque filtre
    if(event.target.id == "submitLight"){
      const algo = "luminosite";
      const first = document.getElementById("textLight").value;
      api.getImageFilterOneParameters(props.id,algo,first)
    .then((data : Blob) => {
      target.value = data;
      const reader = new window.FileReader();
      reader.readAsDataURL(data);
      reader.onload = () => {
        const galleryElt = document.getElementById("imageFiltre");
        if (galleryElt !== null) {
          const imgElt = document.createElement("img");
          imgElt.setAttribute("id","filtre");
          galleryElt.appendChild(imgElt);
          if (imgElt !== null && reader.result as string) {
            imgElt.setAttribute("src", (reader.result as string)); 
          }
        }
      };
    });
    }

    if(event.target.id == "submitContour"){
      const algo = "contour";
      api.getImageFilterOnlyAlgo(props.id,algo)
      .then((data : Blob) => {
        target.value = data;
        const reader = new window.FileReader();
        reader.readAsDataURL(data);
        reader.onload = () => {
        const galleryElt = document.getElementById("imageFiltre");
        if (galleryElt !== null) {
          const imgElt = document.createElement("img");
          imgElt.setAttribute("id","filtre");
          galleryElt.appendChild(imgElt);
          if (imgElt !== null && reader.result as string) {
            imgElt.setAttribute("src", (reader.result as string)); 
          }
        }
      };
      });
    }


    var divHide = document.getElementById("imageFiltré")?.style.visibility;
      if (divHide == 'hidden') {
        document.getElementById("imageFiltré").style.visibility = 'visible';
      }
  }

</script>

<template>
<div>
  <div id="images">
    <div id="imagedeBase">
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
    </div>

  <div id="imageFiltré" style="visibility: hidden;">
    <figure id="imageFiltre"></figure>
    <div id="BoutonsFiltre">
      <div id = "telechargerFiltre">
        <button id="btSaveFiltre" @click="saveImg($event)">enregister</button>
      </div>
    </div>
  </div>
</div>

    <div id="Filtres">
      <div id = "luminosite">
        <h3 id="light">Luminosité</h3>
        <input type="text" id="textLight" ref="text" placeholder="intensité">
        <br>
        <button id="submitLight" @click="submitFilter($event)">appliquer</button>
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
        <button id="submitContour" @click="submitFilter($event)">Filtre contour</button>
      </div>
    </div>
</div>
</template>

<style>

  #images {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    grid-template-rows: 1fr;
    grid-column-gap: 10px;
    grid-row-gap: 0px;
  }

  #imagedeBase {
    grid-area: 1 / 1 / 2 / 2;
  }

  #imageFiltré{
    grid-area: 1 / 2 / 2 / 3;;
  }

  img {
    width: 300px; 
    height: 300px; 
  }
</style>