<?php

session_start();

$errors = "";

if(!empty($_SESSION['login'])){
	header("Location: admin.php");
	exit();
}

if(isset($_POST["submit"])){
	
	$username = strip_tags($_POST["inputUsername"]);
	$password = $_POST["inputPassword"];
	
	if(!empty($username) && !empty($password)){
		
		include("connection.php");
		
		$username = mysqli_real_escape_string($sql_connect, $username);
		$password = md5($password);
	
		$query = mysqli_query($sql_connect,"SELECT * FROM admins WHERE username='$username' AND password='$password';");
		$rows = mysqli_num_rows($query);
		
		if($rows == 1){
			
			$_SESSION['login'] = $username;
			header("Location: admin.php");
			exit();
			
		} else {
			$errors = '<div class="alert alert-danger" role="alert">Login failed, Incorrect Username/Password.</div>';
		}
		
	} else {
		$errors = '<div class="alert alert-danger" role="alert">Please enter your username and password!</div>';
	}
	
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
   <body class="signup-page">
   
   <nav class="navbar navbar-transparent navbar-absolute">
    	<div class="container">
        	<!-- Brand and toggle get grouped for better mobile display -->
        	<div class="navbar-header">
        		<button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#navigation-example">
            		<span class="sr-only">Toggle navigation</span>
		            <span class="icon-bar"></span>
		            <span class="icon-bar"></span>
		            <span class="icon-bar"></span>
        		</button>
        	</div>
    	</div>
    </nav>
   
   	<div class="wrapper">
		<div class="header header-filter" style="background-image: url('img/background.png'); background-size: cover; background-position: top center;">
			<div class="container">
				<div class="row">
					<div class="col-md-4 col-md-offset-4 col-sm-6 col-sm-offset-3">
                    
						<div class="card card-signup">
                      
							<form class="form" method="post" action="#">
                            
                            	<div class="header header-primary text-center">
									<img src="img/logo.png" width="200px"/>
								</div>
                                
								<div class="content">

									<div class="input-group">
										<span class="input-group-addon">
											<i class="material-icons">email</i>
										</span>
										<input type="text" id="inputUsername" name="inputUsername" class="form-control" placeholder="Username" required autofocus>
									</div>

									<div class="input-group">
										<span class="input-group-addon">
											<i class="material-icons">lock_outline</i>
										</span>
										<input type="password" id="inputPassword" name="inputPassword" class="form-control" placeholder="Password" required>
									</div>

									<!-- If you want to add a checkbox to this form, uncomment this code

									<div class="checkbox">
										<label>
											<input type="checkbox" name="optionsCheckboxes" checked>
											Subscribe to newsletter
										</label>
									</div> -->
								</div>
                                <?php echo $errors; ?>
								<div class="footer text-center">
									<input name="submit" class="btn btn-primary btn-simple" type="submit" value="Sign In"/>
								</div>
							</form>
						</div>
					</div>
				</div>
			</div>
   
   
      
      	<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
      	<!-- Include all compiled plugins (below), or include individual files as needed -->
      	<script src="js/bootstrap.min.js"></script>
     
		<script src="js/material.min.js"></script>
    	<script src="js/material-kit.js" type="text/javascript"></script>
   </body>
</html>