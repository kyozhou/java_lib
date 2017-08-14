package com.mirahome.lib;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.InsertManyOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Created by zhoubin on 2017/4/27.
 */
public class FastMongoClient {

    private static HashMap<String, FastMongoClient> mongos = new HashMap<String, FastMongoClient>();

    private MongoClient mongoClient = null;
    private MongoDatabase mongoDatabase = null;


    public static FastMongoClient getInstance(String connectionString, String database) {
        String key = DigestUtils.md5Hex(connectionString + database);
        if(mongos.get(key) == null) {
            FastMongoClient fastMongoClient = new FastMongoClient(connectionString, database);
            FastMongoClient.mongos.put(key, fastMongoClient);
        }
        return mongos.get(key);
    }

    public FastMongoClient(String connectingString, String database) {
        this.mongoClient = new MongoClient(new MongoClientURI(connectingString));
        this.mongoDatabase = mongoClient.getDatabase(database);
    }

    public List<Document> query(String coll, Bson filter, Bson sort, int limit) {
        MongoCollection<Document> collection = this.mongoDatabase.getCollection(coll);

        FindIterable<Document> findIterable = collection.find(filter);
        if(sort != null) {
            findIterable = findIterable.sort(sort);
        }
        if(limit > 0) {
            findIterable = findIterable.limit(limit);
        }
        MongoCursor<Document> mongoCursor = findIterable.iterator();
        List<Document> list = new ArrayList<Document>();
        while (mongoCursor.hasNext()) {
            list.add(mongoCursor.next());
        }
        return list;
    }

    public List<Document> query(String coll, Bson filter, Bson sort) {
        return this.query(coll, filter, sort, -1);
    }


    public List<Document> query(String coll, Bson filter) {
        return this.query(coll, filter, null, -1);
    }

    public Document queryOne(String coll, Bson filter, Bson sort) {
        List<Document> result = this.query(coll, filter, sort, 1);
        if(result.size() > 0) {
            return result.get(0);
        } else {
            return new Document();
        }
    }

    public void insert(String coll, Document data) {
        MongoCollection<Document> collection = this.mongoDatabase.getCollection(coll);
        collection.insertOne(data);
    }

    public void insertMulti(String coll, List list) {
        MongoCollection<Document> collection = this.mongoDatabase.getCollection(coll);
        try {
            InsertManyOptions options = new InsertManyOptions();
            options.ordered(false);
            //options.bypassDocumentValidation(true);
            collection.insertMany(list, options);
        }catch (Exception e) {
            System.out.println("insertMulti error: " + e.getMessage());
        }
    }

    public boolean update(String coll, Bson filter, Bson newData) {
        MongoCollection<Document> collection = this.mongoDatabase.getCollection(coll);
        UpdateResult result = collection.updateOne(filter, newData);
        return result.getModifiedCount() > 0;
    }

    public boolean delete(String coll, Bson filter) {
        MongoCollection<Document> collection = this.mongoDatabase.getCollection(coll);
        DeleteResult result = collection.deleteOne(filter);
        return result.getDeletedCount() > 0;
    }
}
