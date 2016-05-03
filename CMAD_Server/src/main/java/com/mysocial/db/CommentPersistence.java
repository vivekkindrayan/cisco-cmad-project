package com.mysocial.db;

import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mysocial.beans.Blog;
import com.mysocial.beans.Comment;


public class CommentPersistence {
	
	private static final String KEY_ID = "_id";
	private static final String KEY_USERID = "userId";
	private static final String KEY_FIRST = "userFirst";
	private static final String KEY_LAST = "userLast";
	private static final String KEY_CONTENT = "content";
	private static final String KEY_DATE = "date";
	public static final String KEY_BLOG_ID = "blogId";
	
	
	public static void saveComment (Comment c) throws Exception
	{
		Blog b = BlogPersistence.getBlogForId(c.getBlogId().toHexString());
		Comment[] existingComments = b.getComments();
		if (existingComments == null || existingComments.length == 0) {
			b.setComments(new Comment[] {c});
		} else {
			List<Comment> commentList = Arrays.asList(existingComments);
			commentList.add(c);
			b.setComments((Comment[]) commentList.toArray());
		}
		BlogPersistence.saveBlog(b);
	}
	
	static Document serialize(Comment c) throws Exception
	{
		return new Document()
			.append(KEY_USERID, c.getUserId())
			.append(KEY_FIRST, c.getUserFirst())
			.append(KEY_LAST, c.getUserLast())
			.append(KEY_CONTENT, c.getContent())
			.append(KEY_DATE, c.getDate())
			.append(KEY_BLOG_ID, c.getBlogId());
	}
	
	static BasicDBObject getBasicDBObjComment (Comment c)
	{
		return new BasicDBObject()
		.append(KEY_USERID, c.getUserId())
		.append(KEY_FIRST, c.getUserFirst())
		.append(KEY_LAST, c.getUserLast())
		.append(KEY_CONTENT, c.getContent())
		.append(KEY_DATE, c.getDate())
		.append(KEY_BLOG_ID, c.getBlogId());
	}
	
	static Comment deSerialize(Document document) throws Exception
	{
		ObjectId id = (ObjectId) document.get(KEY_ID);
		String userId = (String) document.get(KEY_USERID);
        String first = (String) document.get(KEY_FIRST);
        String last = (String) document.get(KEY_LAST);
        String content = (String) document.get(KEY_CONTENT);
        String date = (String) document.get(KEY_DATE);
        String blogId = (String) document.get(KEY_BLOG_ID);
        
        Comment c = new Comment();
        c.setId(id);
        c.setUserFirst(first);
        c.setUserLast(last);
        c.setContent(content);
        c.setDate(date);
        c.setBlogId(new ObjectId(blogId));
        c.setUserId(new ObjectId(userId));
        return c;
	}
	

}
