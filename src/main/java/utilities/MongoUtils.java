package utilities;

import com.models.Todo;
import com.mongodb.*;

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

public class MongoUtils {

    private static final String LOCALHOST = "localhost";
    private static final short PORT_NUMBER = 27017;

    public static DB getDatabase( String databaseName )
                                         throws UnknownHostException {

        MongoClient mc = new MongoClient( LOCALHOST, PORT_NUMBER );
        return mc.getDB( databaseName );
    }

    public static DBCollection getCollection( String collectionName, DB db ) {

        DBCollection collection = db.getCollection( collectionName );
        return collection;
    }

    public static Set getAllCollections( DB db ) {

        return db.getCollectionNames();
    }

    public static void createTodo( Todo todo, DBCollection collection ) {

        BasicDBObject doc = new BasicDBObject("title", todo.getTitle() )
                                .append( "body", todo.getBody() )
                                .append( "done", todo.isDone() );
        collection.insert( doc );
    }

    public static DBObject getFirstDocument( DBCollection collection ) {

        return collection.findOne();
    }

    public static long countAll( DBCollection collection ) {

        return collection.getCount();
    }

    public static Set<DBObject> findAllDocuments( DBCollection collection ) {

        Set<DBObject> resultSet = new HashSet<DBObject>();
        DBCursor cursor = collection.find();
        try {
            while( cursor.hasNext() ) {
                resultSet.add( cursor.next() );
            }
        } finally {
            cursor.close();
        }
        return resultSet;

    }

    public static Set<DBObject> queryDocuments( DBCollection collection,
                                                String key,
                                                String value ) {

        Set<DBObject> resultSet = new HashSet<DBObject>();
        BasicDBObject queryItem = new BasicDBObject( key, value );
        DBCursor cursor = collection.find( queryItem );
        try {
            while( cursor.hasNext() ) {
                resultSet.add( cursor.next() );
            }
        } finally {
            cursor.close();
        }
        return resultSet;
    }

    public static boolean removeDocument( DBCollection collection,
                                          String key, String value ) {
        BasicDBObject queryItem = new BasicDBObject( key, value );
        return collection.findAndRemove( queryItem ) != null;
    }

    public static boolean saveDocument( DBCollection collection,
                                        DBObject dbObject ) {
        return collection.save( dbObject ) != null;
    }

    public static boolean updateDocument( DBCollection collection,
                                          DBObject queryObject,
                                          DBObject updateObject) {
        return collection.findAndModify( queryObject,
                                         updateObject ) != null;
    }

//    public static DBObject querySingleDocument( DBCollection collection,
//                                                String key, String value ) {
//        BasicDBObject queryItem = new BasicDBObject( key, value );
//        DBCursor cursor = collection.find( queryItem );
//        try {
//            while( cursor.hasNext() ) {
//
//            }
//        }
//    }
}
