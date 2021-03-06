package com.mysocial.util;

import java.security.MessageDigest;

import org.bson.Document;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import io.vertx.core.AsyncResult;
import io.vertx.ext.web.RoutingContext;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mysocial.Server;
import com.mysocial.beans.User;
import com.mysocial.db.UserPersistence;

import static com.mysocial.util.Constants.*;

public class MySocialUtil {
	
	private static MongoClient mongoClient = null;
	private static ThreadLocal<MongoDatabase> mongoDb = new ThreadLocal<MongoDatabase>();
	private static ThreadLocal<Datastore> mongoTL = new ThreadLocal<Datastore>();
	
	/**
	 * Method to retrieve a mongo database client from the thread local storage
	 * @return
	 */
	public static MongoDatabase getMongoDB(){
		if(mongoDb.get()==null){
			MongoClientURI connectionString = new MongoClientURI(Server.MONGODB_URL);
			mongoClient = new MongoClient(connectionString);	
			MongoDatabase mongoDatabase = mongoClient.getDatabase(DB_NAME);
			mongoDb.set(mongoDatabase);
			return mongoDatabase;
		}
		return mongoDb.get();
	}
	
	public static Datastore getMongoDataStore(){
		Morphia morphia = new Morphia();
		morphia.mapPackage(DB_PACKAGE_NAME);
		Datastore datastore = morphia.createDatastore(mongoClient, DB_NAME);
		if(mongoTL.get()==null){
			datastore.ensureIndexes();
			mongoTL.set(datastore);
		}
		return mongoTL.get();
	}

	public static MongoCollection<Document> getCollectionForDB (String dbName)
	{
		MongoDatabase db = getMongoDB();
		MongoIterable<String> collections = db.listCollectionNames();
		boolean collectionExists = false;
		for (String collection : collections) {
			if (collection.equals(dbName)) {
				collectionExists = true;
				break;
			}
		}
		
		if (!collectionExists) {
			db.createCollection(dbName);
		}
		
		return db.getCollection(dbName);
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
		String signedInUserId = routingContext.session().get(SESSION_USER_KEY);
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