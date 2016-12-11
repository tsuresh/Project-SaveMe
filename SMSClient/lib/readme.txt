# mChoice SDP API PHP Library
This is a simple guide on how to use mChoice SDP API PHP Library.


## PHP Library Components
### SMS
   - SmsSnder.php               - To send SMS MT(Mobile Terminated) messages.
   - SmsReceiver.php            - To receive SMS MO(Mobile Originated) messages.
   - SmsDeliveryReport.php      - To receive SMS DR(Delivery Reports) messages.

### USSD
    - MtUssdSender.php          - To send USSD MT messages.
    - MoUssdReceiver.php        - To receive USSD MO messages.

### CAAS
    - DirectDebitSender.php     - To send Direct Debit charging requests.
    - QueryBalanceSender.php    - To check balance
    - KLogger.php               - A simple php logging library to log information.

### LBS
    - LbsClient.php             - To send requests and receive responses from SDP/SDP simulator
    - LbsRequest.php            - To handle lbs requests.
    - LbsResponse.php           - To handle lbs responses.
    - KLogger.php               - A simple php logging library to log information.

## Using Libraries
- To use  mChoice SDP API PHP library just import the proper library file(sms, ussd or cass) in top of your own PHP source file.
    eg:- for ussd applications
    <?php
    include_once 'ussd/MoUssdReceiver.php';
    include_once 'ussd/MtUssdSender.php';
    ?>


## Testing Your Application
Once you finished writing your application you can test that using mChoice SDP Simulator. Simulator will start on port
7000. You can use the URSs listed below as request sending URLs. For more information on SDP Simulator please refer to
the Readme file provided with it.


SMS
: http://localhost:7000/sms/send/
USSD
: http://localhost:7000/ussd/send/
CAAS
: http://localhost:7000/caas/direct/debit (Send direct debit)
: http://localhost:7000/caas/balance/query (Send query balance)
LBS
: http://127.0.0.1:7000/lbs/locate

Note:To use these libraries,cURL library should have installed with PHP.