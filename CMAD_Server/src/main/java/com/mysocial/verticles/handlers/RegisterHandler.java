package com.mysocial.verticles.handlers;

import static com.mysocial.util.Constants.*;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysocial.beans.User;
import com.mysocial.db.UserPersistence;
import com.mysocial.util.MySocialUtil;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.RoutingContext;

public class RegisterHandler implements Handler<RoutingContext> {
	
	Vertx vertx;
	
	public RegisterHandler(Vertx vertx) {
		this.vertx = vertx;
	}
	
	public void handle(RoutingContext routingContext) {
		
		HttpServerResponse response = routingContext.response();
		response.putHeader(RESPONSE_HEADER_CONTENT_TYPE, RESPONSE_HEADER_JSON);
		
		try {
			String jsonStr = routingContext.getBodyAsString();
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			User u = mapper.readValue(jsonStr, User.class);
			System.out.println("Got user with name " + u.getUserName());

			vertx.executeBlocking(future -> {
				System.out.println("Check if user already exists");
				Boolean registered = false;
				if (UserPersistence.getUserByUsername(u.getUserName()) == null) {
					try {
						UserPersistence.saveUser(u);
					} catch (Exception e) {
						System.err.println("Failed to save user - " + e.getMessage());
						e.printStackTrace();
					}
					System.out.println("User does not exist, registered successfully");
					registered = true;
				} else {
					System.err.println("User already exists, registration failed");
				}
				future.complete(registered);
			}, resultHandler -> {
				if (resultHandler.succeeded()) {
					Boolean registered = (Boolean) resultHandler.result();
					if (registered) {
						response.setStatusCode(HttpResponseStatus.OK.code());
						routingContext.removeCookie(COOKIE_HEADER);
						routingContext.addCookie(Cookie.cookie(COOKIE_HEADER, u.getId().toHexString()));
					} else {
						response.setStatusCode(HttpResponseStatus.BAD_REQUEST.code());
						routingContext.removeCookie(COOKIE_HEADER);
					}
					response.end();
				} else {
					response.setStatusCode(HttpResponseStatus.BAD_REQUEST.code());
					routingContext.removeCookie(COOKIE_HEADER);
					response.end(resultHandler.cause().getMessage());
					MySocialUtil.handleFailure(resultHandler, this.getClass());
				}
			});
		
		
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			response.setStatusCode(HttpResponseStatus.BAD_REQUEST.code());
			routingContext.removeCookie(COOKIE_HEADER);
			response.end();
		}
	}

}
