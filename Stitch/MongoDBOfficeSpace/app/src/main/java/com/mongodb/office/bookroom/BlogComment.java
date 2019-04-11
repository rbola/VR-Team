package com.mongodb.office.bookroom;

import org.bson.Document;
import org.bson.types.ObjectId;

public class BlogComment {
    private final ObjectId _id;
    private final String _text;


    public BlogComment(final Document document) {
        _id = document.getObjectId("_id");
        _text = document.getString("comment");

    }

    public ObjectId getId() {
        return _id;
    }

    public String getComment() {
        return _text;
    }

}