<?php
require_once './Slim/Slim.php';
require_once '../php/DAO.php';
require_once '../php/BDConexion.php';
require_once '../php/model/Parada.php';
require_once '../php/model/Linea.php';
require_once '../php/model/Llegada.php';

// 2. Initialize
$app = new Slim();


// 3. Define routes
// Con PHP<5.3 hay que crear la función aparte y pasar su nombre al método, no se pueden crear funciones anónimas
function hello($name){
	echo "Hola, ".$name;
}
$app->get('/hello/:name','hello');


function paradas(){
	$dao = new DAO();
	echo json_encode($dao->getTodasParadas());
}
$app->get('/paradas//','paradas');


function paradas_numero($numero){
	$dao = new DAO();
	echo json_encode($dao->getParada($numero));
}
$app->get('/paradas/:numero','paradas_numero');


function paradas_buscar_query($query){
	$dao = new DAO();
	echo '{"resultados":'.json_encode($dao->buscarParadas($query))."}";
}
$app->get('/paradas/buscar/:query','paradas_buscar_query');


/**
 * --- Hace la petición de tiempo al servidor de Tussam ---
 * El método original es SOAP, pero lo hago mediante Http y parseando el resultado a mano.
 * 
 * TODO: Control de errores!! Madre del amó hermozo... -.-'
 */
function tiempo_parada_linea($parada, $linea){
	// Construye la petición según mandaría SOAP
	$soap_str = "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><GetPasoParada xmlns=\"http://tempuri.org/\"><linea>$linea</linea><parada>$parada</parada><status>1</status></GetPasoParada></soap:Body></soap:Envelope>";
	$ch = curl_init("http://www.infobustussam.com:9001/services/dinamica.asmx");
	curl_setopt ($ch, CURLOPT_POST, 1);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1 );
	curl_setopt($ch, CURLOPT_POSTFIELDS, $soap_str); 
	curl_setopt($ch, CURLOPT_HTTPHEADER, array('Content-Type: text/xml'));

	// Y la ejecuta. Fire!
	$res = curl_exec ($ch);
	curl_close ($ch);


	// Transforma la respuesta para evitar problemas de incompatibilidad
	$res = str_replace("soap:", "", $res);
	// Create the XML
	$envelope = simplexml_load_string($res);

	// Obtiene el 'PasoParada', que contiene las llegadas de los 2 buses
	$PasoParada = $envelope->Body->GetPasoParadaResponse->GetPasoParadaResult->PasoParada;

	if(is_object($PasoParada)){ // OK
		// Crea el primer bus, tiempo y distancia
		// Casting para que devuelva el texto y no un objeto SimpleXMLElement
		$e1 = $PasoParada->e1;
		$bus1 = new Bus((string)$e1->minutos,(string) $e1->metros);

		// Lo mismo con el segundo
		$e2 = $PasoParada->e2;
		$bus2 = new Bus((string)$e2->minutos, (string)$e2->metros);

		// Constuye el objeto llegada, fasi fasi
		$llegada = new Llegada((string)$PasoParada->linea, (string)$PasoParada->parada, $bus1, $bus2);

		// Listo, muestra la llegada
		echo json_encode($llegada);
	}else{ // ERROR
		echo '{"error":"Error desconocido", "linea":"'.$linea.'", "parada":"'.$parada.'"}';
	}
}
$app->get('/tiempo/:parada/:linea','tiempo_parada_linea');

// 4. Run!
$app->run();
?>