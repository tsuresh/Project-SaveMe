<?php
//SMS Receivers and Senders
include_once '../lib/sms/SmsReceiver.php';
include_once '../lib/sms/SmsSender.php';

//CASS API
include_once '../lib/cass/DirectDebitSender.php';
include_once '../lib/cass/KLogger.php';

$caslg = new KLogger ("cass_debug.log" , KLogger::DEBUG );

ini_set('error_log', 'sms-app-error.log');

try {
    $receiver = new SmsReceiver(); // Create the Receiver object
    $content = $receiver->getMessage(); // get the message content
    $address = $receiver->getAddress(); // get the sender's address
    $requestId = $receiver->getRequestID(); // get the request ID
    $applicationId = $receiver->getApplicationId(); // get application ID
    $encoding = $receiver->getEncoding(); // get the encoding value
    $version = $receiver->getVersion(); // get the version
	
	list($func,$data) = explode(" ",$content,2);
	
	$applicationId = "APP_000001";
    $password = "password";
	
	include("connection.php");
	
	$json_file = file_get_contents('http://localhost/newsaveme/SMSClient/sms/lbsApp.php?number='.$address);
	$jfo = json_decode($json_file);
		
	$latitude = $jfo->latitude;
	$longitude = $jfo->longitude;
	$timeStamp = $jfo->timeStamp;
	$finalloc = $latitude.",".$longitude;
		
	$curl = "http://api.openweathermap.org/data/2.5/weather?lat=".$latitude."&lon=".$longitude."&appid=19c7a3e263bc286699800fb20e098c67";
	$cdata = @file_get_contents($curl);
	$jsoncdata = json_decode($cdata,true);
	$thecity = $jsoncdata["name"];
	
	if($func == "road"){
		
		$message = mysqli_real_escape_string($conn,$data);	
	
		mysqli_query($conn,"INSERT INTO `reports` (`id`, `reporter`, `saveme`, `nic`, `name`, `location`, `proceed`, `datetime`, `type`, `disaster`, `howhelp`, `city`) VALUES (NULL, '$address', '', '', '$message', '$finalloc', '0', now(), 'road', '', '', '$thecity');");
		
		sendsms("Road accident successfully reported. We'll get back to you as soon as possible.",array($address));
		
	} elseif($func == "natural"){
		
		$message = mysqli_real_escape_string($conn,$data);	
	
		mysqli_query($conn,"INSERT INTO `reports` (`id`, `reporter`, `saveme`, `nic`, `name`, `location`, `proceed`, `datetime`, `type`, `disaster`, `howhelp`, `city`) VALUES (NULL, '$address', '', '', '', '$finalloc', '0', now(), 'natural', '', '$message', '$thecity');");
		
		sendsms("We received your request. And we'll get back to you as soon as possible.",array($address));
		
	} elseif($func == "fire"){
		
		$message = mysqli_real_escape_string($conn,$data);	
	
		mysqli_query($conn,"INSERT INTO `reports` (`id`, `reporter`, `saveme`, `nic`, `name`, `location`, `proceed`, `datetime`, `type`, `disaster`, `howhelp`, `city`) VALUES (NULL, '$address', '', '', '', '$finalloc', '0', now(), 'fire', '', '', '$thecity');");
		
		sendsms("Thank you for reporting. And we'll get back to you as soon as possible.",array($address));
		
	} else if($func == "thief"){
		
		$message = mysqli_real_escape_string($conn,$data);	
	
		mysqli_query($conn,"INSERT INTO `reports` (`id`, `reporter`, `saveme`, `nic`, `name`, `location`, `proceed`, `datetime`, `type`, `disaster`, `howhelp`, `city`) VALUES (NULL, '$address', '', '', '', '$finalloc', '0', now(), 'thief', '', '', '$thecity');");
		
		sendsms("Thank you for reporting. And we'll get back to you as soon as possible.",array($address));
		
	} elseif($func == "donate"){
		
		$amount = (int)$data;
		
		try {
			$sender = new DirectDebitSender("http://localhost:7000/caas/direct/debit");
			$externalTrxId = rand(4657654654,546465462323);
			$jsonResponse = $sender->cass($applicationId, $password, $externalTrxId, $address, "MobileAccount", "123456", "LKR", $amount);
			
			$responseArray = json_decode($jsonResponse, true);
			
			$timeStamp = $responseArray['timeStamp'];
			$statusDetail = $responseArray['statusDetail'];
			$externalTrxId = $responseArray['externalTrxId'];
			$internalTrxId = $responseArray['internalTrxId'];
			
			if($statusDetail == "Success"){
				
				//mysqli_query($conn,"INSERT INTO `saveme`.`donations` (`id`, `externalTrxId`, `internalTrxId`, `mobile`, `amount`, `datetime`) VALUES (NULL, '$externalTrxId', '$internalTrxId', '$address', '$amount', 'now()');");
				
				sendsms("You have successfully donated ".$amount." Rs. for people who are victims of floods. Thank You for your donation!",array($address));
				
				//Insert to donations database
				
			} else {
				
				sendsms("We were unable to receive your donation. You don't have enough credit on your mobile or your bill has not been paid!",array($address));
				
			}
			

		} catch (CassException $ex) {
			error_log("CASS direct-debit ERROR: {$ex->getStatusCode()} | {$ex->getStatusMessage()}");
		}
		
	} else {
		sendsms("Invalid Message!",array($address));
	}
	
    logFile("[ content=$content, address=$address, requestId=$requestId, applicationId=$applicationId, encoding=$encoding, version=$version ]");
   
} catch (SmsException $ex) {
    //throws when failed sending or receiving the sms
    error_log("ERROR: {$ex->getStatusCode()} | {$ex->getStatusMessage()}");
}

function sendsms($responseMsg,$destinationAddresses){
	// Create the sender object server url
    $sender = new SmsSender("http://localhost:7000/sms/send");
    //sending a one message
 	$applicationId = "APP_000001";
 	$encoding = "0";
 	$version =  "1.0";
    $password = "password";
    $sourceAddress = "77000";
    $deliveryStatusRequest = "1";
    $charging_amount = ":15.75";
    $binary_header = "";
    $res = $sender->sms($responseMsg, $destinationAddresses, $password, $applicationId, $sourceAddress, $deliveryStatusRequest, $charging_amount, $encoding, $version, $binary_header);
}

?>