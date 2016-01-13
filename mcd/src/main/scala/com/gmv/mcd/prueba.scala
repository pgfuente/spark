package com.gmv.mcd

import java.util.Date
import com.gmv.mcd.util.ParseUtils

/**
 * @author pagf
 */
object prueba extends App {

  //val excluded = List("2")
  // val lista:List[Option[String]] = List(Option[String]("1"),Option[String]("2"),Option[String]("3"),None)

  //lista.foreach { println }

  // val filtrado = lista.filter { x => !(x == None || excluded.contains(x.get)) }

  // filtrado.foreach { println }

  val line1 = "21 jul 2015 12:56:22,366 INFO  [services] [4fdc16f3-c50c-46c8-a671-9c8a0a0c46a8]: Executing operation subscribe from service SubscriptionManagementService with parameters '[transId = '1fac5feb-2f97-11e5-b407-eddb3861c8d4', client = '34603634279', product = 'IBUNDLE4', channel = 'SINCANAL', freeFee = 'false', newClient = 'false', system = 'YETI']'"
  val line2 = "21 jul 2015 12:56:22,386 INFO  [services] [4fdc16f3-c50c-46c8-a671-9c8a0a0c46a8]: Executing operation find from service ClientManagementService with parameters '34603634279'21 jul 2015 12:56:22,389 INFO  [services] [4fdc16f3-c50c-46c8-a671-9c8a0a0c46a8]: Finished execution of operation find from service ClientManagementService 21 jul 2015 12:56:22,390 INFO  [services] [4fdc16f3-c50c-46c8-a671-9c8a0a0c46a8]: Executing operation create from service ClientManagementService with parameters '[msisdn = '34603634279', state = 'INACTIVE', category = 'PREPAGO']'21 jul 2015 12:56:22,392 INFO  [services] [4fdc16f3-c50c-46c8-a671-9c8a0a0c46a8]: Finished execution of operation create from service ClientManagementService returned value [msisdn = '34603634279', state = 'ACTIVE', category = 'PREPAGO']21 jul 2015 12:56:22,394 INFO  [services] [4fdc16f3-c50c-46c8-a671-9c8a0a0c46a8]: Executing operation update from service ClientManagementService with parameters '[msisdn = '34603634279', state = 'ACTIVE', category = 'PREPAGO']'21 jul 2015 12:56:22,396 INFO  [services] [4fdc16f3-c50c-46c8-a671-9c8a0a0c46a8]: Executing operation changesClientCategory from service ClientManagementService with parameters '[msisdn = '34603634279', state = 'ACTIVE', category = 'PREPAGO']'21 jul 2015 12:56:22,401 INFO  [services] [4fdc16f3-c50c-46c8-a671-9c8a0a0c46a8]: Finished execution of operation changesClientCategory from service ClientManagementService returned value false21 jul 2015 12:56:22,407 INFO  [services] [4fdc16f3-c50c-46c8-a671-9c8a0a0c46a8]: Finished execution of operation update from service ClientManagementService 21 jul 2015 12:56:22,418 INFO  [services] [4fdc16f3-c50c-46c8-a671-9c8a0a0c46a8]: Executing operation chargeInitialFee from service ChargeManagementService with parameters '[client = '34603634279', product = 'IBUNDLE4', transId = '1fac5feb-2f97-11e5-b407-eddb3861c8d4']'21 jul 2015 12:56:22,418 INFO  [services] [4fdc16f3-c50c-46c8-a671-9c8a0a0c46a8]: Initial fee amount for Product=[name = 'IBUNDLE4'] Client=[msisdn = '34603634279', state = 'ACTIVE', category = 'PREPAGO'] Segment=[name = 'DEFAULT'] Channel=[name = 'SINCANAL'] is '8.264400'21 jul 2015 12:56:22,419 INFO  [services] [4fdc16f3-c50c-46c8-a671-9c8a0a0c46a8]: Import to charge to CORAL is '8.264400'21 jul 2015 12:56:22,419 DEBUG [CoralXAResource] [4fdc16f3-c50c-46c8-a671-9c8a0a0c46a8]: Beginning transaction...21 jul 2015 12:56:22,420 DEBUG [ManagedConnectionImpl] [4fdc16f3-c50c-46c8-a671-9c8a0a0c46a8]: context.getAccountTypes= 1,2,321 jul 2015 12:56:22,420 INFO  [Driver] [4fdc16f3-c50c-46c8-a671-9c8a0a0c46a8]: CORALService request content: (service get_credit)(RETRIEVE:MSISDN=603634279,dedicated_account_info;)21 jul 2015 12:56:22,420 INFO  [FipaClientUpdated] [4fdc16f3-c50c-46c8-a671-9c8a0a0c46a8]: Sending FIPA to host: coral12.prod.airtel.es:200021 jul 2015 12:56:22,579 INFO  [Driver] [4fdc16f3-c50c-46c8-a671-9c8a0a0c46a8]: CORAL response content: CODE=0,BALANCE=8.2645    ,DEDICATED_ACCOUNT_VALUE_3=0.0000,DEDICATED_ESPIRY_DATE_3=2037-01-01;21 jul 2015 12:56:22,580 DEBUG [ManagedConnectionImpl] [4fdc16f3-c50c-46c8-a671-9c8a0a0c46a8]: currentAmount= 8.2645 + 0.0000 = 8.264521 jul 2015 12:56:22,580 INFO  [services] [4fdc16f3-c50c-46c8-a671-9c8a0a0c46a8]: Finished execution of operation chargeInitialFee from service ChargeManagementService returned value 8.26440021 jul 2015 12:56:22,596 INFO  [services] [4fdc16f3-c50c-46c8-a671-9c8a0a0c46a8]: Finished execution of operation subscribe from service SubscriptionManagementService returned value [client = '34603634279', product = 'IBUNDLE4', transId = '1fac5feb-2f97-11e5-b407-eddb3861c8d4']21 jul 2015 12:56:22,605 DEBUG [ManagedConnectionImpl] [4fdc16f3-c50c-46c8-a671-9c8a0a0c46a8]: Charged amount: 8.26440021 jul 2015 12:56:22,605 INFO  [Driver] [4fdc16f3-c50c-46c8-a671-9c8a0a0c46a8]: CORALService request content: (service sdp-access)(UPDATE: MSISDN=603634279,AMOUNT=8.264400,SUBTRACT,EURO;)21 jul 2015 12:56:22,606 INFO  [FipaClientUpdated] [4fdc16f3-c50c-46c8-a671-9c8a0a0c46a8]: Sending FIPA to host: coral12.prod.airtel.es:200021 jul 2015 12:56:22,739 INFO  [Driver] [4fdc16f3-c50c-46c8-a671-9c8a0a0c46a8]: CORAL response content: CODE=0;"

  val lines = Iterable(Iterable(line1,line2))

  println(ParseUtils.vectorize(lines, List("")))

}