/**
 * En este archivo va el código JavaScript específico de la página 'parada'
 */

/* Código baado en la documentación de jQueryMobile */

// Carga el código de una parada concreta, basado en
// la URL pasada. Lo inyecta en una página embebida, y hace
// dicha página la página actual.
function showParada( urlObj, options )
{
	var numeroParada = urlObj.hash.replace( /.*n=/, "" ),

		// Las páginas que usamos para mostrar nuestro contenido están ya
		// En el DOM. El id de la página en la que vamos a escribir contenido
		// está especificado en la almohadilla antes del '?'.
		pageSelector = urlObj.hash.replace( /\?.*$/, "" );

	// Obtiene el objeto que representa la parada en
	// que estamos interesados. Lanzamos una petición AJAX.
	$.ajax({
		url:"./api/paradas/"+numeroParada,
		type:"GET",
		dataType: "json",
		success: function(json){
			//Una vez cargada la parada...
			var $page = $(pageSelector),
				// Obtiene la cabecera de la página
				$header = $page.children( ":jqmData(role=header)" ),
				// Obtiene el área del contenido de la página
				//$content = $page.children( ":jqmData(role=content)" );

				// Obtiene la lista donde van las líneas
				$lista = $("#parada-lista-lineas");

				// Busca el elemento h1 en el header e inyecta en él el
				// número de la parada.
				$header.find("h1").html("Parada "+json.numero);

			// Inyecta las líneas en el listview
			//Vacía la lista
			$lista.empty();
			//Pone el 'header' de la lista
			$lista.append('<li data-role="list-divider" role="heading">Tiempos de llegada</li>');
			//Y al lío
			$.each(json.lineas,function(){
				//Por defecto muestra un mensaje de cargando
				$lista.append(' <li class="llegada" id="llegada-'+this.nombre+'" data-theme="c">'+this.nombre+': Cargando...</li>')
			});

			// Lanza un chorro de peticiones AJAX para obtener el tiempo de cada línea
			obtenerTiempos(json.lineas, json.numero);

			// Inject the category items markup into the content element.
//			$content.html("<p>Éste es el contenido de la parada número "+json.numero+", "+json.nombre+"</p1>");

			// Pages are lazily enhanced. We call page() on the page
			// element to make sure it is always enhanced before we
			// attempt to enhance the listview markup we just injected.
			// Subsequent calls to page() are ignored since a page/widget
			// can only be enhanced once.
			$page.page();

			// We don't want the data-url of the page we just modified
			// to be the url that shows up in the browser's location field,
			// so set the dataUrl option to the URL for the category
			// we just loaded.
			options.dataUrl = urlObj.href;

			// Now call changePage() and tell it to switch to
			// the page we just modified.
			$.mobile.changePage( $page, options );

		},
		error: function(XMLHttpRequest, textStatus, errorThrown){
			alert("erró: " + errorThrown);
		}
	});	
}

function obtenerTiempos(lineas, parada){
	// Lanza una petición por cada línea cargada
	$.each(lineas,function(){
		// Pide el tiempo de llegada para esta parada y esta línea
		$.ajax({
			url:"./api/tiempo/"+parada+"/"+this.nombre,
			type:"GET",
			dataType: "json",
			success: function(json){
				// Comprueba que esté bien
				if(!json.error){
					// Coloca el tiempo en su lugar correspondiente 
					$("#llegada-"+json.linea).html(json.linea+": "+json.bus1.tiempo+" minutos | "+json.bus2.tiempo+" minutos");
				}else{
					$("#llegada-"+json.linea).html(json.linea+": "+json.error);
				}
			},
			error: function(XMLHttpRequest, textStatus, errorThrown){
				alert("erró: " + errorThrown);
			}
		});	
	});//each
}