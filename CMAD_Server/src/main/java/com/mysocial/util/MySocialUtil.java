package com.mysocial.util;

import java.security.MessageDigest;

import io.vertx.core.AsyncResult;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.RoutingContext;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mysocial.beans.User;
import com.mysocial.db.UserPersistence;

import static com.mysocial.util.Constants.*;

public class MySocialUtil {
	
	private static MongoClient mongoClient = null;
	private static ThreadLocal<MongoDatabase> mongoDb = new ThreadLocal<MongoDatabase>();
	
	/**
	 * Method to retrieve a mongo database client from the thread local storage
	 * @return
	 */
	public static MongoDatabase getMongoDB(){
		if(mongoDb.get()==null){
			MongoClientURI connectionString = new MongoClientURI(MONGODB_URL);
			mongoClient = new MongoClient(connectionString);	
			MongoDatabase mongoDatabase = mongoClient.getDatabase(DB_NAME);
			mongoDb.set(mongoDatabase);
			return mongoDatabase;
		}
		return mongoDb.get();
	}
	
	public static void handleFailure(AsyncResult<Object> resultHandler, @SuppressWarnings("rawtypes") Class clazz) {
		Throwable t = resultHandler.cause();
		System.err.println("Failed in " + clazz.getName() + ", result = " + resultHandler.result() + ", message = " + t.getMessage() + ", stacktrace = "); 
		t.printStackTrace() ;
	}
	
	public static synchronized String encrypt(String plaintext) throws Exception
	{
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(plaintext.getBytes());
		byte[] digest = md.digest();
		StringBuffer sb = new StringBuffer();
		for (byte b : digest) {
			sb.append(String.format("%02x", b & 0xff));
		}
		return sb.toString();
	}
	
	public static boolean comparePassword(String inputPassword, User u) throws Exception
	{
		return u.getPassword().equals(encrypt(inputPassword));
	}

	public static void handleException (Exception ex)
	{
		System.err.println(ex.getMessage());
		ex.printStackTrace();
	}
	
	public static User getSignedInUser (RoutingContext routingContext)
	{
		String signedInUserId = null;
		Cookie authCookie = routingContext.getCookie(COOKIE_HEADER);
		if (authCookie != null) {
			signedInUserId = authCookie.getValue();
		}

		User u = null;
		if (signedInUserId != null) {
			u = UserPersistence.getUserById(signedInUserId);
			if (u != null) {
				System.out.println("User data for " + signedInUserId + " fetched successfully");
			} else {
				System.err.println("Failed to fetch data for signed in user");
			}
		}
		return u;
	}
	
	protected void finalize() {
		mongoClient.close();
	}
	

}