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

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

/**
* @author Carlos Sierra Andrés
*/
@Path("/")
public class ExampleResource {

	@GET
	public String sayHello() {
		return "Hello World";
	}

	
	// curl  -X PUT http://localhost:8080/o/rest/api/example/put --data-ascii "melonchino"

	@PUT
	@Path("/put")
	public String receive0(
			String input) {
			return "this is a put with " + input ;
			
	}

	// curl  -X PUT http://localhost:8080/o/rest/api/example/put/myparam\?order\=jose --data-ascii "melonchino"
	
	@PUT
	@Path("/put/{param}")
	public String receive1(
			String input,
			@PathParam("param") String param,
			@QueryParam("order") String order) {
		
			return "this is a put with " + input +
					" and param " + param + " and query param " + order;
	}

	
	@PUT
	@Path("/put/{param}")
	public SubResource receive2(
			String input,
			@PathParam("param") String param,
			@QueryParam("order") String order) {
		
			return new SubResource(param);
	}

	
	public class SubResource {

		public SubResource (String param) {
			_param = param;
		}

		@GET
		public String sayHello() {
			return "Hello World";
		}

		@PUT
		@Path("/put")
		public String receive0(
				String input) {

				return "this is a put with " + input ;
				
		}
	
		// curl  -X PUT http://localhost:8080/o/rest/api/example/put/myparam/myotherparam --data-ascii "melonchino"
		@PUT
		@Path("/{param}")
		public SubResource another(
				@PathParam("param") String param) {
				return new SubResource (param);
				
		}
	
		String _param;
	}

}