package com.mysocial;

import static com.mysocial.util.Constants.DEFAULT_WORKER_POOL_SIZE;
import static com.mysocial.util.Constants.HTTP_PORT;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.ErrorHandler;

import com.mysocial.verticles.MySocialVerticle;
public class Server {
	
	private VertxOptions options;
	private Vertx vertx;
	private Router router;
	private HttpServer server;

	public Server() {
		options = new VertxOptions().setWorkerPoolSize(DEFAULT_WORKER_POOL_SIZE);
		vertx = Vertx.vertx(options);
		server = vertx.createHttpServer();
		router = Router.router(vertx);
		
		router.route().handler(CookieHandler.create());
	    router.route().handler(BodyHandler.create());
		router.route().failureHandler(ErrorHandler.create());
	}
	
	public VertxOptions getOptions() {
		return options;
	}

	public Vertx getVertx() {
		return vertx;
	}

	public Router getRouter() {
		return router;
	}

	public HttpServer getServer() {
		return server;
	}
	
	public void deployVerticles() throws Exception {
		
		MySocialVerticle cpv = new MySocialVerticle();
		cpv.deploy(getVertx(), getRouter());
		server.requestHandler(this.getRouter()::accept).listen(HTTP_PORT);
	}

	public static void main(String[] args) throws Exception {
		
		Server s = new Server();
		s.deployVerticles();
	}
}
