/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 * <p>
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * <p>
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.remote.rest.example;

import java.util.Locale;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Variant;
import javax.xml.bind.annotation.XmlRootElement;

/**
* @author Carlos Sierra Andrés
*/
@Path("/test2")
public class ExampleResource2 {

	@GET
	public MyData sayHello() {
		return new MyData("Hello", 3);
	}

	// curl -v -X GET http://localhost:9090/o/rest/api/example/test2/ -H 'Accept: application/json'
	@GET
	@Produces({"application/json"}) // ({"application/xml"})
	public MyData sayHello2() {
		return new MyData("Hello", 3);
	}

	// curl -v -X PUT http://localhost:9090/o/rest/api/example/test2/ -H 'Accept: application/json'
	@PUT
	@Produces({"application/json"}) // ({"application/xml"})
	public MyData sayHello3() {
		return new MyData("Hello", 3);
	}

	// curl  -X PUT http://localhost:9090/o/rest/api/example/test2/sayHello/ -H 'Accept: text/plain' -H 'Accept-Language: es_ES' -H 'Content-Type: application/json' --data-ascii '{"a": "Ambrin", "b": 3}'
	@PUT
	@Path("/sayHello")
	@Produces({"text/plain"}) // ({"application/xml"})
	 public MyData sayHello(MyData myData, @Context Request request) {
	
	    Variant.VariantListBuilder variantListBuilder =
	        Variant.VariantListBuilder.newInstance();
	
	    Variant variant = request.selectVariant(
	        variantListBuilder.languages(
	            Locale.ENGLISH, new Locale("es_ES")).add().build());
	
	    if (variant == null) {
	        return new MyData(myData.getA(), myData.getB() + 1);
	    }
	
	    if (variant.getLanguage().getCountry().equals("en")) {
	        new MyData(myData.getA(), myData.getB() + 2);
	    }
	
	    return new MyData("español!! ", myData.getB() + 1);
	}


	 // que la clase es serializable con jaxb. Tiene que ser un bean con getter y setter y constructor vacío.
	// al ser una inner class tiene que ser stática para que la pueda instanciar jaxb
	@XmlRootElement
	public static class MyData {
		
		public String getA() {
			return a;
		}

		public void setA(String a) {
			this.a = a;
		}

		public long getB() {
			return b;
		}

		public void setB(long b) {
			this.b = b;
		}

		String a;
		long b;
		
		public MyData() {
		}
		
		public MyData(String a, long b) {
			super();
			this.a = a;
			this.b = b;
		}
	}
}