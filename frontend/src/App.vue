<script setup lang="ts">
</script>

<template>
  <div id="app">
    <nav id='menu'>
      <input type='checkbox' id='responsive-menu' onclick='updatemenu()'><label></label>
      <ul>
        <li>
          <router-link to="/">Home</router-link>
          </li>
        <li>
          <router-link to="/gallery">Galerie</router-link>
        </li>
        <li>
          <router-link to="/upload">Upload</router-link>
        </li>
      </ul>
    </nav>
    <div>
      <h1 id="titre">PDL - Groupe M1A</h1>
      <router-view />
    </div>
  </div>
</template>

<style>

#titre{
	font-family: Georgia, serif;
	font-size: 35px;
	letter-spacing: 2px;
	word-spacing: 2px;
	color: #851680;
	font-weight: 400;
	text-decoration: none;
	font-style: normal;
	font-variant: normal;
	text-transform: none;
}

#app {
  text-align: center;
  font-family: Arial, Helvetica, sans-serif;
  color: #2c3e50;
}

#menu {
	background: #7549CC;
	color: #FFF;
	height: 45px;
	padding-left: 18px;
	border-radius: 10px;
	border: 2px solid #D83BFF;
}
#menu ul, #menu li {
	margin: 0 auto;
	padding: 0;
	list-style: none
}
#menu ul {
	width: 100%;
}
#menu li {
	float: left;
	display: inline;
	position: relative;
}
#menu a {
	display: block;
	line-height: 41px;
	padding: 0 14px;
	text-decoration: none;
	color: #FFFFFF;
	font-size: 16px;
	text-transform: uppercase;
}
#menu a.dropdown-arrow:after {
	content: "\25BE";
	margin-left: 5px;
}
#menu li a:hover {
	color: #CC49A9;
	background: #F2F2F2;
}
#menu input {
	display: none;
	margin: 0;
	padding: 0;
	height: 45px;
	width: 100%;
	opacity: 0;
	cursor: pointer
}
#menu label {
	display: none;
	line-height: 41px;
	text-align: center;
	position: absolute;
	left: 35px
}
#menu label:before {
	font-size: 1.6em;
	content: "\2261"; 
	margin-left: 20px;
}
#menu ul.sub-menus{
	height: auto;
	overflow: hidden;
	width: 170px;
	background: #444444;
	position: absolute;
	z-index: 99;
	display: none;
}
#menu ul.sub-menus li {
	display: block;
	width: 100%;
}
#menu ul.sub-menus a {
	color: #FFFFFF;
	font-size: 16px;
}
#menu li:hover ul.sub-menus {
	display: block
}
#menu ul.sub-menus a:hover{
	background: #F2F2F2;
	color: #444444;
}
@media screen and (max-width: 800px){
	#menu {position:relative}
	#menu ul {background:#111;position:absolute;top:100%;right:0;left:0;z-index:3;height:auto;display:none}
	#menu ul.sub-menus {width:100%;position:static;}
	#menu ul.sub-menus a {padding-left:30px;}
	#menu li {display:block;float:none;width:auto;}
	#menu input, #menu label {position:absolute;top:0;left:0;display:block}
	#menu input {z-index:4}
	#menu input:checked + label {color:white}
	#menu input:checked + label:before {content:"\00d7"}
	#menu input:checked ~ ul {display:block}
}
</style>
