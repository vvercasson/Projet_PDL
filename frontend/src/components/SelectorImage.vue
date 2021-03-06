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

function updateTextInput(val) {     
  if(val.target.id == "rangeLight")
    document.getElementById('rangeValue').value=val.target.value;
  else if (val.target.id == "rangeBruit")
    document.getElementById('rangeBruitValue').value=val.target.value;          
} 

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

  function showImageFilter(data){ // fonction qui affiche l'image filtré
    target.value = data;
    const reader = new window.FileReader(); // créer un reader de la page pour récuperer les données
    reader.readAsDataURL(data);
    reader.onload = () => {
      const galleryElt = document.getElementById("imageFiltre"); // recupération du div de l'image filtré
      if (galleryElt !== null) {                                 // creation de la balise img
        const imgElt = document.createElement("img");
        imgElt.setAttribute("id","filtre");
        galleryElt.appendChild(imgElt);
        if (imgElt !== null && reader.result as string) {
          imgElt.setAttribute("src", (reader.result as string));  // on met le result du Blob dans la balise img
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

  function typeImage(type){ // fonction de typage d'une image img ou png
    switch(type){
          case "image/jpeg" || "image/jpg":
            return "jpg";
          case "image/png":
            return "png";
    }
  }

  function saveImg(event){ // fonction de sauvegarde des images (filtrées ou non)
    var id;
    var type =imageList.value[props.id].type; // recupère le type de l'image selectionné
    type = typeImage(type);
    if(event.target.id == "btSave"){ // creation d'un Blob pour sauvegarder l'image non filtré
      api.getImage(props.id)
      .then((data: Blob) =>{
        var url = window.URL.createObjectURL(data);
        var enregister = document.createElement('a');
        enregister.href = url;

        enregister.setAttribute('download','image.'+type); // telecharge l'image
        document.body.appendChild(enregister);

        enregister.click(); 
    });
    }
    else{ // creation d'un Blob pour sauvegarder l'image filtré
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
    var affiche = false; // variable d'affichage pour les selecteurs
    document.getElementById("btSaveFiltre").style.visibility = 'hidden';
    
    document.getElementById(id).disabled = true;
    setTimeout(function(){document.getElementById(id).disabled = false;},500); // bouton de disable pour éviter de spamer le chargement des images


    const imageShow = document.getElementById("imageFiltre");
    if (imageShow !== null) {
      var varP = document.getElementById("filtre");
    }
    if(varP != null){
      varP.parentNode?.removeChild(varP);
    }

    // générations des éléments des filtres

    if(event.target.id == "submitLight"){
      const algo = "luminosite";
      const first = document.getElementById("rangeLight");
      if (first.value < -255 || isNaN(first.value) || first.value > 255 ){ // vérification de la validité des valeurs
        alert("veuillez rentrer une valeur entre -255 et 255 !");
      }

      else{
        api.getImageFilterOneParameters(props.id,algo,first.value) // voir dans http-api.ts pour la definition de la méthode
        .then((data : Blob) => {
          showImageFilter(data); // application de la fonction pour l'image courante
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

      if (selectValue == "Filtre Bruit Gaussien"){
        const algo = "bruit";
        var first = document.getElementById("rangeBruit").value;
        api.getImageFilterOneParameters(props.id,algo,first)
        .then((data : Blob) => {
          showImageFilter(data);
        });
        affiche = true;
      }

      if (selectValue == "Filtre Miroir"){
        const algo= "miroir";
        api.getImageFilterOnlyAlgo(props.id,algo)
        .then((data : Blob) => {
          showImageFilter(data);
        });
        affiche = true;
      }
    }


    if (affiche == true){ // affiche le bouton que quand un filtre est utilisé
      var divHide = document.getElementById("btSaveFiltre")?.style.visibility;
      if (divHide == 'hidden') {
        document.getElementById("btSaveFiltre").style.visibility = 'visible';
      }
    }
  }


  function handleSelect(event) { // fonction pour gérer la lisibilité des div 
    selectValue = (event.target.value);
    

    // Filtre retourner image
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


    // Filtre bruit gaussien
    if (selectValue == "Filtre Bruit Gaussien"){
      const divVisible = document.getElementById("responseExtensionBruit")?.style.visibility;
        if (divVisible == 'hidden') {
          document.getElementById("responseExtensionBruit").style.visibility = 'visible';
        }
    }
    else{
      const divVisible = document.getElementById("responseExtensionBruit")?.style.visibility;
        if (divVisible == 'visible') {
          document.getElementById("responseExtensionBruit").style.visibility = 'hidden';
        }
    }


    // Filtre moyen
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


    // on remet tout les div en hidden pour éviter de les superposer
    document.getElementById("luminosite").style.visibility = 'hidden';
    document.getElementById("reponse").style.visibility = 'hidden';
    document.getElementById("blur").style.visibility = 'hidden';
    document.getElementById("contour").style.visibility = 'hidden';
    document.getElementById("reponseExtension").style.visibility = 'hidden';
    document.getElementById("responseExtensionBruit").style.visibility = 'hidden';
    document.getElementById("extension").style.visibility = 'hidden';


    var menu = event.target.id; // recupération de l'id des menu

    // maj de la lisibilité à visible pour la selection

    // Menu de la luminosité
    if (menu == "menuLumi"){
      var divHide = document.getElementById("luminosite")?.style.visibility;
      if (divHide == 'hidden') {
        document.getElementById("luminosite").style.visibility = 'visible';
      }
    }

    // Menu du filtre flou
    if (menu == "menuBlur"){
      var selected = document.getElementById("defaultMenuBlur");
      var divHide = document.getElementById("blur")?.style.visibility;
      if (divHide == 'hidden') {
        document.getElementById("blur").style.visibility = 'visible';

        if(document.getElementById("blur")?.childNodes[1].value == "Moyen"){
          const divVisible = document.getElementById("reponse")?.style.visibility;
          if (divVisible == 'hidden') {
            document.getElementById("reponse").style.visibility = 'visible';
          }
        }
      }
    }

    // Menu du filtre contour
    if (menu == "menuContour"){
      var divHide = document.getElementById("contour")?.style.visibility;
      if (divHide == 'hidden') {
        document.getElementById("contour").style.visibility = 'visible';
      }
    }

    // Menu des extensions
    if (menu == "menuExtensions"){
      var divHide = document.getElementById("extension")?.style.visibility;
      if (divHide == 'hidden') {
        document.getElementById("extension").style.visibility = 'visible';
      }
      if(document.getElementById("extension")?.childNodes[1].value == "Filtre Bruit Gaussien"){
          const divVisible = document.getElementById("responseExtensionBruit")?.style.visibility;
          if (divVisible == 'hidden') {
            document.getElementById("responseExtensionBruit").style.visibility = 'visible';
          }
      }
      else if(document.getElementById("extension")?.childNodes[1].value == "Retourner Image"){
          const divVisible = document.getElementById("reponseExtension")?.style.visibility;
          if (divVisible == 'hidden') {
            document.getElementById("reponseExtension").style.visibility = 'visible';
          }
      }
    }
  }

</script>

<template>

  <div id="menuDemo">
    <h3 id="titleMenu">Choisissez un filtre :</h3>
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
        <br>
        <div id="rangeDiv">
          <input id = "rangeLight" type="range" min="-255" max="255" step="1" @change="updateTextInput($event)">
          <input id="rangeValue" type="text" value="" readonly>
        </div>
        <button id="submitLight" @click="submitFilter($event)">appliquer</button>
      </div>

      <div id = "blur" style="visibility: hidden;">
        <h3 id="titleBlur">Filtre flou</h3>
        <select id="selectBlur" @change="handleSelect($event)">
          <option id="defaultMenuBlur">Choisir un filtre</option>
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
          <option>Filtre Bruit Gaussien</option>
          <option>Filtre Miroir</option>
        </select>
        <br>
        <div id="reponseExtension" style="visibility: hidden;">
          <select id="textretourner">
            <option>horizontal</option>
            <option>vertical</option>
            <option>les deux</option>
          </select>
        </div>
        <div id="responseExtensionBruit" style="visibility: hidden;">
          <input id = "rangeBruit" type="range" min="0" max="255" step="1" @change="updateTextInput($event)">
          <input id="rangeBruitValue" type="text" value="" readonly>
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

  #rangeValue{
    width: 30px;
  }

  #rangeBruitValue{
    width: 30px;
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

  #cssmenu {
    width:auto;
    display:block;
    text-align:center;
    font-family:Oswald;
    line-height:1.2;
  }

  #titleMenu{
    font-family:Oswald;
    font-size:20px;
  }

  #cssmenu ul {
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

  #cssmenu li {
    display:inline-block;
    position:relative;    
    font-size:0; 
    margin:0;
    padding:0;
  }

  #cssmenu >ul>li>span, #cssmenu >ul>li>a {   
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

  #cssmenu li:hover > span, #cssmenu li:hover > a {  
    color:#333333;
    background-color:#F3F3F3;
  }

</style>