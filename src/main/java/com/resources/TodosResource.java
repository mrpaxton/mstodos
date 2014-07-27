package com.resources;

import com.models.Todo;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.services.SearchService;
import utilities.MongoUtils;
import utilities.TodoDBOUtils;

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Set;

@Path("/api")
public class TodosResource {

    private DBCollection todos;
    private Set<DBObject> allDbObjectsSet;
    private SearchService searchService;

    //initialize the database collection and the Set of db objects
    public TodosResource() {
        //retrieve data from the MongoDB database
        DB todosDatabase = null;
        try {
            todosDatabase = MongoUtils.getDatabase("todosdb");
            todos = MongoUtils.getCollection("todosdb", todosDatabase);
            allDbObjectsSet = MongoUtils.findAllDocuments( todos );
        } catch (UnknownHostException e) {
            System.err.println("Unknown host exception occurred.");
        } catch (Exception e ) {
            System.err.println("Cannot establish connection with database");
        }
    }

    @GET
    @Path("/todos/{title}")
    @Produces( MediaType.APPLICATION_JSON )
    public Response todo(@PathParam("title") String title) {

        if( title.isEmpty() ) {
            return Response.serverError()
                           .entity("Title cannot be blank")
                           .build();
        }

        try {
            DBObject inputObject =
                    TodoDBOUtils.queryDocumentByTitle( todos, title );
            Todo currentTodo =
                    TodoDBOUtils.createTodoFromDBObject( inputObject );
            String jsonString =
                    TodoDBOUtils.makeJsonStringFromTodo( currentTodo );
            return Response.ok( jsonString, MediaType.APPLICATION_JSON )
                           .build();
        } catch( Exception e ) {
            return Response.status(404)
                           .entity(title + ": object not found")
                           .build();
        }
    }

    @GET
    @Path("/todos")
    @Produces( MediaType.APPLICATION_JSON )
    public JsonArray todos() {

        JsonArrayBuilder jab =
                TodoDBOUtils.makeJsonArrayBuilder( allDbObjectsSet );
        return jab.build();
    }

    @DELETE
    @Path("/todos/{title}")
    public Response removeTodo(@PathParam("title") String title) {

        //remove only the first occurrence of the DBObject
        if ( MongoUtils.removeDocument(todos, "title", title) ) {
            return Response.ok()
                           .entity(title + " : todo removed successfully")
                           .build();
        } else {
            return Response.notModified( title + " : cannot remove todo.")
                           .build();
        }
    }

    //save: stores an object into the MongoDB database
    //      insert an entry if the identifier not exist
    //      else throw exception
    @POST
    @Path("/todos")
    @Consumes( MediaType.APPLICATION_JSON )
    public Response saveTodo( JsonObject inputJsonObject )
                                            throws Exception {

        DBObject dbObject =
                ( DBObject ) JSON.parse( inputJsonObject.toString() );
        if( TodoDBOUtils.todoExist(inputJsonObject, allDbObjectsSet) ) {
            return Response.notModified( dbObject.get("title") +
                                         ": already exist.\n").build();
        }

        if ( MongoUtils.saveDocument( todos, dbObject ) ) {
            return Response.status(201).entity(dbObject.get("title") +
                    " : saved successfully.\n").build();
        } else {
            return Response.notModified( dbObject.get("title") +
                                       " : cannot be saved.\n").build();
        }
    }

    //update : update the object using identifier
    //         if the identifier is missing, throw exception
    //         if repeating objects, update the most recent one
    @PUT
    @Path("todos/{title}")
    @Consumes( "application/json" )
    public Response updateTodo( @PathParam("title") String title,
                               JsonObject inputJsonObject ) {
        //identifier is missing
        if( ! TodoDBOUtils.validInDocuments( todos, title ) ) {
            return Response.notModified(title + " : not valid in database")
                           .build();
        }
        try {
            DBObject currentObject =
                    TodoDBOUtils.queryDocumentByTitle(todos, title);
            Todo currentTodo =
                    TodoDBOUtils.createTodoFromDBObject(currentObject);
            currentTodo =
                    TodoDBOUtils.updateTodoFromJson(inputJsonObject,
                                                    currentTodo);
            DBObject updateObject =
                    TodoDBOUtils.changeTodoToDBObject( currentTodo );
            if ( MongoUtils.updateDocument( todos,
                    currentObject, updateObject ) ) {
                return Response.ok( title + ": has been updated\n")
                               .build();
            } else {
                return Response.notModified( title +
                                     " : cannot be updated in database.\n")
                               .build();
            }
        } catch ( Exception e ) {
            return Response.notModified( title + " : cannot be updated\n")
                           .build();
        }
    }

    @GET
    @Path("todos/search/{keyword}")
    public Response elasticSearch( @PathParam("keyword") String keyword ) {

        searchService = new SearchService();
        searchService.indexSampleTodos();
        List<Todo> todos = searchService.searchTodos( keyword );

        if( todos.size() == 0 ) {
            return Response.noContent().build();
        }
        String searchResult =
                TodoDBOUtils.makeJsonStringFromTodoList( todos );
        return Response.ok( searchResult, MediaType.APPLICATION_JSON )
                       .build();
    }

    //to change the done operation
    @PUT
    @Path("todos/{title}-{done}")
    public Response markTodoAsDone( @PathParam("title") String title,
                         @PathParam("done") String strDone ) {
        boolean done;
        if( strDone.equalsIgnoreCase( "true" ) ||
            strDone.equalsIgnoreCase( "false") ) {
            done = Boolean.parseBoolean( strDone.toLowerCase() );
        } else {
            return Response.status(400)
                    .entity("api/{title}-{done} : " +
                            "{done} must be true or false\n")
                    .build();
        }

        try {
            DBObject currentObject =
                    TodoDBOUtils.queryDocumentByTitle(todos, title);
            Todo currentTodo =
                    TodoDBOUtils.createTodoFromDBObject(currentObject);
            currentTodo.setDone( done );
            DBObject updateObject =
                    TodoDBOUtils.changeTodoToDBObject( currentTodo );
            MongoUtils.updateDocument( todos,
                                       currentObject, updateObject );

            return Response.ok( title + " todo's done set to : " +
                                done + "\n" )
                           .build();

        } catch( Exception e ) {
            return Response.status(304)
                    .entity(title + " todo's done cannot be modified\n")
                    .build();
        }
    }
}
