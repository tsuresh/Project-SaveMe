<?php

session_start();

if(!empty($_SESSION['login'])){
	
	$_SESSION['login'] = "";
	session_destroy();
	
	header("Location: index.php");
	exit();
	
} else {
	
	header("Location: index.php");
	exit();
	
}

?>