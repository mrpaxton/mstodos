package utilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.models.Todo;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TodoDBOUtils {

    public static JsonObject asJson( Todo todo ) {
        return Json.createObjectBuilder()
                .add( "title", todo.getTitle() )
                .add( "body", todo.getBody() )
                .add( "done", todo.isDone() )
                .build();
    }

    public static boolean todoExist( JsonObject inputJsonObject,
                                     Set<DBObject> allDbObjectsSet) {
        DBObject dbObject =
                ( DBObject ) JSON.parse(inputJsonObject.toString());

        Map objMap = dbObject.toMap();
        //generate a POJO object
        Todo inputTodo = new Todo( objMap.get("title").toString(),
                objMap.get("body").toString(),
                Boolean.parseBoolean(objMap.get("done")
                        .toString()) );

        List<Todo> allTodos = new ArrayList<Todo>();
        //only save if not existing
        for( DBObject  obj : allDbObjectsSet ) {
            //DBObject returns a Map
            objMap = obj.toMap();
            //generate a POJO object
            Todo todoObject = new Todo( objMap.get("title").toString(),
                    objMap.get("body").toString(),
                    Boolean.parseBoolean(objMap.get("done")
                            .toString()) );
            allTodos.add( todoObject );
        }

        for( Todo todo : allTodos ) {
            if ( inputTodo.equals( todo ) ) {
                return true;
            }
        }
        return false;
    }

    public static DBObject queryDocumentByTitle( DBCollection collection,
                                           String title ) {
        //current database object
        Set<DBObject> todosSet =
                MongoUtils.queryDocuments( collection, "title", title);
        DBObject currentObject = ( DBObject ) todosSet.toArray()[0];
        return currentObject;
    }

    public static Todo createTodoFromDBObject( DBObject currentObject ) {
        //create a POJO using the info from the current object
        Todo currentTodo = new Todo( (String) currentObject.get("title"),
                (String) currentObject.get("body"),
                (boolean) currentObject.get("done") );
        return currentTodo;
    }

    public static Todo updateTodoFromJson( JsonObject inputJsonObject,
                                     Todo currentTodo ) {
        //grab info from input JSON data and update values
        DBObject inputObject =
                ( DBObject ) JSON.parse( inputJsonObject.toString() );
        if ( inputObject.get( "title" ) != null ) {
            currentTodo.setTitle( (String) inputObject.get("title") );
        } else if ( inputObject.get("body") != null ) {
            currentTodo.setBody( (String) inputObject.get("body") );
        } else if ( inputObject.get("done") != null ) {
            currentTodo.setDone( (boolean) inputObject.get("done") );
        }
        return currentTodo;
    }

    public static JsonArrayBuilder makeJsonArrayBuilder(
            Set<DBObject> objectSet ) {

        JsonArrayBuilder jab = Json.createArrayBuilder();
        for( DBObject  obj : objectSet ) {
            //convert DBObject to a Map
            Map objMap = obj.toMap();
            //generate a POJO object
            Todo todoObject = new Todo( objMap.get("title").toString(),
                    objMap.get("body").toString(),
                    Boolean.parseBoolean(objMap.get("done")
                            .toString()) );
            jab.add( asJson( todoObject ));
        }
        return jab;
    }

    public static DBObject changeTodoToDBObject( Todo todo ) {
        return ( DBObject ) JSON.parse( TodoDBOUtils.asJson( todo )
                                                    .toString() );
    }

    public static String makeJsonStringFromTodo( Todo todo ) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson( todo );
    }

    public static String makeJsonStringFromTodoList( List<Todo> todos ) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson( todos );
    }

    public static boolean validInDocuments( DBCollection collection,
                                            String title ) {
        boolean validInDocs = false;
        Set<DBObject> allObjects = MongoUtils.findAllDocuments( collection );
        for( DBObject dbo : allObjects ) {
            if ( dbo.get("title").toString().equalsIgnoreCase( title ) ) {
                validInDocs = true;
                break;
            }
        }
        return validInDocs;
    }
}
