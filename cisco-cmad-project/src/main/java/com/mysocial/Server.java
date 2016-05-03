package com.mysocial;

import static com.mysocial.util.Constants.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.ErrorHandler;
import com.mysocial.verticles.MySocialVerticle;


public class Server {
	
	private static String MONGODB_HOST = "";
	private static int MONGODB_PORT = 0;
	private static int HTTP_PORT = 0;
	
	public static String MONGODB_URL = "";
	
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
		
		loadProperties();
		Server s = new Server();
		s.deployVerticles();
	}
	
	private static void loadProperties()
	{
		Properties prop = new Properties();
		InputStream input = null;

		try {
			input = new FileInputStream(CONFIG_PROPS_FILE);
			prop.load(input);

			MONGODB_PORT = Integer.parseInt(prop.getProperty(PROP_KEY_MONGODB_PORT));
			HTTP_PORT = Integer.parseInt(prop.getProperty(PROP_KEY_HTTP_PORT));
			MONGODB_HOST = prop.getProperty(PROP_KEY_MONGODB_HOST);
			
			MONGODB_URL = MONGODB_URL_PREFIX + MONGODB_HOST + ":" + Integer.toString(MONGODB_PORT);

		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

	}
}
