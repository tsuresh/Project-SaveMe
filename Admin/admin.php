<?php
   session_start();
   
   if (empty($_SESSION['login'])) {
       header("Location: index.php");
       exit();
   } else {
       $privilege = $_SESSION["login"];
   }
   
   include("connection.php");
                 
   if(!empty($_GET["remove"])){
	   mysqli_query($sql_connect,"UPDATE reports SET proceed='1' WHERE id='".$_GET["remove"]."'");
	   header("Location: admin.php");
   }
   
   ?>
<!doctype html>
<html>
   <head>
      <meta charset="utf-8">
      <title>SaveMe | Administration</title>
      <!--     Fonts and icons     -->
      <link rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons" />
      <link rel="stylesheet" type="text/css" href="https://fonts.googleapis.com/css?family=Roboto:300,400,500,700" />
      <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/latest/css/font-awesome.min.css" />
      <!-- CSS Files -->
      <link href="css/bootstrap.min.css" rel="stylesheet" />
      <link href="css/material-kit.css" rel="stylesheet"/>
   </head>
   <body>
      <nav class="navbar navbar-default">
         <div class="container-fluid">
         
            <div class="navbar-header">
               <p class="navbar-text">Welcome, <?php echo $privilege;?></p>
            </div>
            
            <div class="collapse navbar-collapse" id="example-navbar-info">
               <ul class="nav navbar-nav">
              
				<li<?php if(!empty($_GET["type"]) && $_GET["type"] == "road"){echo ' class="active"';}?>><a href="?type=road">Road Accidents</a></li>
        		<li<?php if(!empty($_GET["type"]) && $_GET["type"] == "natural"){echo ' class="active"';}?>><a href="?type=natural">Natural Disasters</a></li>
                <li<?php if(!empty($_GET["type"]) && $_GET["type"] == "thief"){echo ' class="active"';}?>><a href="?type=thief">Theif Alerts</a></li>
                <li<?php if(!empty($_GET["type"]) && $_GET["type"] == "fire"){echo ' class="active"';}?>><a href="?type=fire">Fire Alerts</a></li>
    		   </ul>
               <ul class="nav navbar-nav navbar-right">
                  <li>
                     <a href="logout.php">Logout</a>
                  </li>
               </ul>
            </div>
            
         </div>
      </nav>
      
      <div class="container">
         <h1>Hello, <?php echo $privilege ?>!</h1>
         <p>Active Reports.</p>
         <div class="panel panel-default">
            <!-- Default panel contents -->
            <div class="panel-heading">Active Reports</div>

            <?php
			
			if(empty($_GET["type"])){
				header("Location: admin.php?type=road");
			}
			
			if($_GET["type"] == "road"){
			?>
            <!-- Table -->
            <table class="table">
               <tr>
                  <th width="15%">Datetime</th>
                  <th width="15%">Victim Data</th>
                  <th width="25%">Location</th>
                  <th width="25%">Reporter/Message</th>
                  <th width="20%">Process</th>
               </tr>
               
               <?php
                  $query = mysqli_query($sql_connect,"SELECT* FROM reports WHERE proceed='0' AND type='road' ORDER BY datetime DESC");
                  while($data = mysqli_fetch_array($query)){
                  	
                  	if($data["saveme"]){
                  		$saveme_q = mysqli_query($sql_connect,"SELECT* FROM users WHERE savehash='".$data["saveme"]."'");
                  		$saveme_d = mysqli_fetch_array($saveme_q);
                  		$person_name = $saveme_d["fullname"];
                  	} else if($data["name"]){
                  		$person_name = $data["name"];
                  	} else {
                  		$person_name = "";
                  	}
                  ?>
                  
                  <?php
				  $location = explode(",",$data["location"]);
				  ?>
                  
               <tr>
                  <td><?php echo $data["datetime"];?></td>
                  <td><?php echo $person_name; if(!empty($data["phone"])){echo $data["phone"];} ?></td>
                  <td><?php echo getLocation($data["location"]); ?><br/><a target="_blank" href="http://maps.google.com/?q=<?php echo $data["location"] ?>">Click to View in map</a></td>
                  <td><?php if(!empty($data["message"])){echo $data["message"];} else {echo $data["reporter"];}?></td>
                  <td>
                     <a href="admin.php?remove=<?php echo $data["id"] ?>">Done Proceeding</a>
                  </td>
               </tr>
               <?php
                  }
                  ?>
            </table>
            <?php
			} else if($_GET["type"] == "natural"){
			?>
            <!-- Table -->
            <table class="table">
               <tr>
                  <th width="10%">Datetime</th>
                  <th width="10%">Reporter</th>
                  <th width="25%">Location</th>
                  <th width="15%">Disaster</th>
                  <th width="30%">How help?</th>
                  <th width="10%">Process</th>
               </tr>
               <?php
                  $query = mysqli_query($sql_connect,"SELECT* FROM reports WHERE proceed='0' AND type='natural' ORDER BY datetime DESC");
                  while($data = mysqli_fetch_array($query)){
                  ?>
               <tr>
                  <td><?php echo $data["datetime"];?></td>
                  <td><?php echo $data["reporter"];?></td>
                  <td><?php echo getLocation($data["location"]); ?><br/><a target="_blank" href="http://maps.google.com/?q=<?php echo $data["location"] ?>">Click to View in map</a></td>
                  <td><?php echo $data["disaster"];?></td>
                  <td><?php echo $data["howhelp"];?></td>
                  <td>
                     <a href="admin.php?remove=<?php echo $data["id"] ?>">Done Proceeding</a>
                  </td>
               </tr>
               <?php
                  }
                  ?>
            </table>
            <?php		  
			} else if($_GET["type"] == "thief"){
			?>
            <!-- Table -->
            <table class="table">
               <tr>
                  <th width="10%">Datetime</th>
                  <th width="10%">Reporter</th>
                  <th width="25%">Location</th>
                  <th width="10%">Process</th>
               </tr>
               <?php
                  $query = mysqli_query($sql_connect,"SELECT* FROM reports WHERE proceed='0' AND type='thief' ORDER BY datetime DESC");
                  while($data = mysqli_fetch_array($query)){
                  ?>
               <tr>
                  <td><?php echo $data["datetime"];?></td>
                  <td><?php echo $data["reporter"];?></td>
                  <td><?php echo getLocation($data["location"]); ?><br/><a target="_blank" href="http://maps.google.com/?q=<?php echo $data["location"] ?>">Click to View in map</a></td>
                  <td>
                     <a href="admin.php?remove=<?php echo $data["id"] ?>">Done Proceeding</a>
                  </td>
               </tr>
               <?php
                  }
                  ?>
            </table>
            <?php
			} else if($_GET["type"] == "fire"){
			?>
            <!-- Table -->
            <table class="table">
               <tr>
                  <th width="10%">Datetime</th>
                  <th width="10%">Reporter</th>
                  <th width="25%">Location</th>
                  <th width="10%">Process</th>
               </tr>
               <?php
                  $query = mysqli_query($sql_connect,"SELECT* FROM reports WHERE proceed='0' AND type='fire' ORDER BY datetime DESC");
                  while($data = mysqli_fetch_array($query)){
                  ?>
               <tr>
                  <td><?php echo $data["datetime"];?></td>
                  <td><?php echo $data["reporter"];?></td>
                  <td><?php echo getLocation($data["location"]); ?><br/><a target="_blank" href="http://maps.google.com/?q=<?php echo $data["location"] ?>">Click to View in map</a></td>
                  <td>
                     <a href="admin.php?remove=<?php echo $data["id"] ?>">Done Proceeding</a>
                  </td>
               </tr>
               <?php
                  }
                  ?>
            </table>
            <?php	
			}
			?>
         </div>
         <?php
            function getLocation($coords){
            	$url = "http://maps.googleapis.com/maps/api/geocode/json?latlng=".$coords."&sensor=false";
            	// Make the HTTP request
            	$data = @file_get_contents($url);
            	// Parse the json response
            	$jsondata = json_decode($data,true);
            		
            	$address = $jsondata["results"][0]["formatted_address"];
            	return $address;
            }
            ?>
      </div>
      <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
      <!-- Include all compiled plugins (below), or include individual files as needed -->
      <script src="js/bootstrap.min.js"></script>
      <script src="js/material.min.js"></script>
      <script src="js/material-kit.js" type="text/javascript"></script>
   </body>
</html>