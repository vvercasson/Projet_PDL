<script setup lang="ts">
import { ref } from 'vue';
import { api } from '@/http-api';

const target = ref<HTMLInputElement>();

function submitFile() {
  const textUpload = document.getElementById("textUpload");
  if(textUpload != null){
    textUpload.parentNode?.removeChild(textUpload);
  }

  if (target.value?.files[0].type == "application/pdf"){
    alert("Erreur de format, veuillez inserer une image png ou jpg");
  }
  else{
    if (target.value !== null && target.value !== undefined && target.value.files !== null) {
    const file = target.value.files[0];
    if (file === undefined)
      return;
    let formData = new FormData();
    formData.append("file", file);
    api.createImage(formData).then(() => {
      if (target.value !== undefined)
        target.value.value = '';
    }).catch(e => {
      console.log(e.message);
    });
    
    var text = document.createElement("h4");
    text.setAttribute("id","textUpload");
    text.textContent = "Image Upload !";
    var divUpload = document.getElementById("divUpload");
    divUpload?.appendChild(text);

    
    }
  }
}

function handleFileUpload(event: Event) {
  target.value = (event.target as HTMLInputElement);
}
</script>

<template>
  <div>
    <h3>Upload une image</h3>
    <div id ="divUpload">
      <input type="file" id="file" ref="file" @change="handleFileUpload" />
    </div>
    <div>
      <button @click="submitFile">Valider</button>
    </div>
  </div>
</template>

<style scoped>
</style>
