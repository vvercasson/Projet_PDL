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

var selectValue = "";

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

  function showImageFilter(data){
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
    }
  }
  function supprImg(){
    api.deleteImage(props.id); // on applique la fonction qui efface l'image
    router.push({ name: 'home'}).then(() => {
    window.location.reload();
    }); // redirection vers la Vue Home
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

  function typeImage(type){
    switch(type){
          case "image/jpeg" || "image/jpg":
            return "jpg";
          case "image/png":
            return "png";
    }
  }

  function saveImg(event){
    var id;
    var type =imageList.value[props.id].type;
    type = typeImage(type);
    if(event.target.id == "btSave"){
      api.getImage(props.id)
      .then((data: Blob) =>{
        var url = window.URL.createObjectURL(data);
        var enregister = document.createElement('a');
        enregister.href = url;

        enregister.setAttribute('download','image.'+type); 
        document.body.appendChild(enregister);

        enregister.click(); 
    });
    }
    else{
      var enregister = document.createElement('a');
      var url = window.URL.createObjectURL(target.value);
      enregister.href = url;
      enregister.setAttribute('download','image.'+type); 
      document.body.appendChild(enregister);

      enregister.click();
    }
  }
  function submitFilter(event){
    const id = event.target.id;
    var affiche = false;
    document.getElementById("btSaveFiltre").style.visibility = 'hidden';
    
    document.getElementById(id).disabled = true;
    setTimeout(function(){document.getElementById(id).disabled = false;},500);


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
      const first = document.getElementById("textLight");
      if (first.value <= 0 || isNaN(first.value) || first.value > 255 ){
        alert("veuillez rentrer une valeur entre 0 et 255 !");
      }

      else{
        api.getImageFilterOneParameters(props.id,algo,first.value)
        .then((data : Blob) => {
          showImageFilter(data);
        });
        affiche = true;
      }
    }

    if(event.target.id == "submitContour"){
      const algo = "contour";
      api.getImageFilterOnlyAlgo(props.id,algo)
      .then((data : Blob) => {
        showImageFilter(data);
      });
      affiche = true;
    }

    if(event.target.id == "blurButton"){
      const algo = "flou";
      if (selectValue == "Moyen"){
        const first = "moyen";
        const size = document.getElementById("blurLevel").value; 
        if (size <= 0){
          alert("veuillez rentrer une valeur supérieur à 0 !");
        }
        else{
          api.getImageFilterAllParameters(props.id,algo,first,size)
          .then((data : Blob) => {
            showImageFilter(data);
          }); 
          affiche = true;
        }
      }
      if(selectValue == "Gaussien"){
        const first = "gaussien";
        api.getImageFilterOneParameters(props.id,algo,first)
        .then((data : Blob) => {
          showImageFilter(data);
        });
        affiche = true;
      }
    }
    if (event.target.id == "extensionButton"){
      if(selectValue == "Filtre Negatif"){
        const algo = "negatif";
        api.getImageFilterOnlyAlgo(props.id,algo)
        .then((data : Blob) => {
          showImageFilter(data);
        });
        affiche = true;
      }

      if (selectValue == "Retourner Image"){
        const algo = "rotate";
        var first = document.getElementById("textretourner").value;
        switch(first){
          case "horizontal":
            first = "h";
            break;
          case "vertical":
            first = "v";
            break;
          case "les deux":
            first = "b";
            break;
        }
        api.getImageFilterOneParameters(props.id,algo,first)
        .then((data : Blob) => {
          showImageFilter(data);
        });
        affiche = true;

      }
    }

    if (affiche == true){
      var divHide = document.getElementById("btSaveFiltre")?.style.visibility;
      if (divHide == 'hidden') {
        document.getElementById("btSaveFiltre").style.visibility = 'visible';
      }
    }
  }


  function handleSelect(event) {
    selectValue = (event.target.value);
    
    if (selectValue == "Retourner Image"){
      const divVisible = document.getElementById("reponseExtension")?.style.visibility;
        if (divVisible == 'hidden') {
          document.getElementById("reponseExtension").style.visibility = 'visible';
        }
    }
    else{
      const divVisible = document.getElementById("reponseExtension")?.style.visibility;
        if (divVisible == 'visible') {
          document.getElementById("reponseExtension").style.visibility = 'hidden';
        }
    }

    if(selectValue == "Moyen"){
      const divVisible = document.getElementById("reponse")?.style.visibility;
        if (divVisible == 'hidden') {
          document.getElementById("reponse").style.visibility = 'visible';
        }
    }
    else{
      const divVisible = document.getElementById("reponse")?.style.visibility;
        if (divVisible == 'visible') {
          document.getElementById("reponse").style.visibility = 'hidden';
        }
    }

  }

  function showFilters(event){
    document.getElementById("luminosite").style.visibility = 'hidden';
    document.getElementById("blur").style.visibility = 'hidden';
    document.getElementById("contour").style.visibility = 'hidden';
    document.getElementById("extension").style.visibility = 'hidden';


    var menu = event.target.id;
    if (menu == "menuLumi"){
      var divHide = document.getElementById("luminosite")?.style.visibility;
      if (divHide == 'hidden') {
        document.getElementById("luminosite").style.visibility = 'visible';
      }
    }
    if (menu == "menuBlur"){
      var divHide = document.getElementById("blur")?.style.visibility;
      if (divHide == 'hidden') {
        document.getElementById("blur").style.visibility = 'visible';
      }
    }
    if (menu == "menuContour"){
      var divHide = document.getElementById("contour")?.style.visibility;
      if (divHide == 'hidden') {
        document.getElementById("contour").style.visibility = 'visible';
      }
    }
    if (menu == "menuExtensions"){
      var divHide = document.getElementById("extension")?.style.visibility;
      if (divHide == 'hidden') {
        document.getElementById("extension").style.visibility = 'visible';
      }
    }
  }

</script>

<template>

  <div id="menuDemo">
    <div id="cssmenu">
      <ul>
          <li><a id="menuLumi" @click="showFilters($event)">Luminosité</a></li>
          <li><a id="menuBlur" @click="showFilters($event)">Filtre Flou</a></li>
          <li><a id="menuContour" @click="showFilters($event)">Contour</a></li>
          <li><a id="menuExtensions" @click="showFilters($event)">Extensions</a></li>
      </ul>
    </div>
  </div>

<br>

<div class="parent">
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
      <br>
    </div>
    <div id="imageFiltré">
      <figure id="imageFiltre"></figure>
      <div id="BoutonsFiltre">
        <div id = "telechargerFiltre">
          <button id="btSaveFiltre" @click="saveImg($event)" style="visibility: hidden;">enregister</button>
        </div>
      </div>
    </div>
</div>
  <div id="Filtres">
      <div id = "luminosite" style="visibility: hidden;">
        <h3 id="light">Luminosité</h3>
        <input type="text" id="textLight" ref="text" placeholder="intensité">
        <br>
        <button id="submitLight" @click="submitFilter($event)">appliquer</button>
      </div>

      <div id = "blur" style="visibility: hidden;">
        <h3 id="titleBlur">Filtre flou</h3>
        <select id="selectBlur" @change="handleSelect($event)">
          <option>Choisir un filtre</option>
          <option id="moyen">Moyen</option>
          <option id="gaussien">Gaussien</option>
        </select>
        <br>
        <div id="reponse" style="visibility: hidden;">
          <label for="blurLevel">Niveau de Flou : </label> 
          <input  type="number" min = "0" max="255" id="blurLevel">
        </div> 
        &nbsp;
        <button id="blurButton" @click="submitFilter($event)">appliquer</button>
      </div>

      <div id="contour" style="visibility: hidden;">
        <h3 id="titleContour"> Filtre Contour</h3>
        <button id="submitContour" @click="submitFilter($event)">Filtre contour</button>
      </div>

      <div id = "extension" style="visibility: hidden;">
        <h3 id="titleExtension">Extensions</h3>
        <select id="selectExtension" @change="handleSelect($event)">
          <option>Choisir un filtre</option>
          <option>Filtre Negatif</option>
          <option>Retourner Image</option>
        </select>
        <br>
        <div id="reponseExtension" style="visibility: hidden;">
          <select id="textretourner">
            <option>horizontal</option>
            <option>vertical</option>
            <option>les deux</option>
          </select>
        </div> 
        <br>
        <button id="extensionButton" @click="submitFilter($event)">appliquer</button>
      </div>

      <div id = "histogramme" style="visibility: hidden;">
        <h3 id="titleHisto">Histogramme</h3>
        <button id="histButton">appliquer</button>
      </div>
      
      <div id = "couleur" style="visibility: hidden;">
        <h3 id="titleColor">Filtre couleur</h3>
        <label for="inputColor">Couleur : </label> 
        <input type="color" id="inputColor" name="couleur">
        &nbsp;
        <button id="colorButton">appliquer</button>
      </div>
  </div>
</template>

<style>

  .parent{
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    grid-template-rows: 1fr;
    grid-column-gap: 0px;
    grid-row-gap: 0px;
    grid-column-gap: 16px;
  }

  #images {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    grid-template-rows: 1fr;
    grid-column-gap: 10px;
    grid-row-gap: 0px;
  }

  #imagedeBase {
    border-style: solid;
    border-color: #851680;
    grid-area: 1 / 1 / 2 / 2;
  }

  #imageFiltré{
    border-style: solid;
    border-color: #851680;
    grid-area: 1 / 2 / 2 / 3;
    display: inline-block;
  }

  #Filtres{
    display: grid;
    grid-template-columns: 1fr;
    grid-template-rows: 1fr;
    grid-column-gap: 0px;
    grid-row-gap: 0px;

  }

  #luminosite{
    grid-area: 1 / 1 / 2 / 2;
  }
  #blur{
    grid-area: 1 / 1 / 2 / 2;
  }

  #contour{
    grid-area: 1 / 1 / 2 / 2;
  }

  #extension{
    grid-area: 1 / 1 / 2 / 2;
  }

  #Boutons{
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    grid-template-rows: 1fr;
    grid-row-gap: 0px;
  }

  img {
    width: 300px; 
    height: 300px; 
  }

  #cssmenu
{
    width:auto;
    display:block;
    text-align:center;
    font-family:Oswald;
    line-height:1.2;
}
#cssmenu ul
{
    width:auto;
    display:block;
    font-size:0;
    text-align:center;
    color:#851680;
    background-color:white;
    border: transparent;
    margin:0; 
    padding:0;
    list-style:none;
    position:relative;
    z-index:999999990;
    border-radius: 3px;
} 

#cssmenu li
{
    display:inline-block;
    position:relative;    
    font-size:0; 
    margin:0;
    padding:0;
}

#cssmenu >ul>li>span, #cssmenu >ul>li>a 
{   
    font-size:22px;
    color:inherit;
    text-decoration:none;
    padding:14px 20px; 
    font-weight:400;
    text-transform:uppercase;
    letter-spacing:2px;   
    display:block;   
    position:relative;
    transition:all 0.3s;
}
#cssmenu li:hover > span, #cssmenu li:hover > a
{  
    color:#333333;
    background-color:#F3F3F3;
}

</style>