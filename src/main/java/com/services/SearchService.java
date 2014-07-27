package com.services;

import com.configurations.JestConfiguration;
import com.models.Todo;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Bulk;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.IndicesExists;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.MongoUtils;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;

public class SearchService {

    final static Logger logger =
                 LoggerFactory.getLogger(SearchService.class);

    private JestClient jestClient;

    public SearchService() {

        jestClient = ( new JestConfiguration() ).jestClient();
    }

    public void indexSampleTodos() {

        try {

            IndicesExists indicesExists =
                          new IndicesExists.Builder("todos").build();
            JestResult result = jestClient.execute(indicesExists);

            if ( !result.isSucceeded() ) {
                // Create todos index
                CreateIndex createIndex =
                            new CreateIndex.Builder("todos").build();
                jestClient.execute(createIndex);
            }

            //retrieve data from the MongoDB
            DB todosDatabase = null;
            try {
                todosDatabase = MongoUtils.getDatabase("todosdb");
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            DBCollection todos =
                MongoUtils.getCollection( "todosdb", todosDatabase );
            Set<DBObject> dbObjectSet =
                MongoUtils.findAllDocuments( todos );

            //index the todos
            Index index;
            Bulk bulk;
            Todo todoObject;
            Map objMap;
            for( DBObject  obj : dbObjectSet )  {
                //DBObject returns a Map
                objMap = obj.toMap();
                //generate a POJO object
                todoObject =
                     new Todo( objMap.get( "title" ).toString(),
                               objMap.get( "body" ).toString(),
                               Boolean.parseBoolean(
                                   objMap.get("done").toString() ) );

                index = new Index.Builder(todoObject)
                                 .index("todos")
                                 .type("todo")
                                 .build();
                bulk  = new Bulk.Builder().addAction( index ).build();
                jestClient.execute(bulk);
            }
        } catch (IOException e) {
            logger.error("Indexing error", e);
        } catch (Exception e) {
            logger.error("Indexing error", e);
        }
    }

    public List<Todo> searchTodos(String param) {

        try {
            SearchSourceBuilder searchSourceBuilder =
                                            new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.queryString(param));

            Search search = new Search
                    .Builder(searchSourceBuilder.toString())
                    .addIndex("todos")
                    .addType("todo")
                    .build();

            JestResult result = jestClient.execute(search);

            List<Todo> resultList =
                       result.getSourceAsObjectList( Todo.class );

            //use data structures Set/List to make unique results
            Set<Todo> resultSet = new HashSet<Todo>( resultList );
            resultList = new ArrayList<Todo>( resultSet );
            return resultList;

        } catch (IOException e) {
            logger.error("Search error", e);
        } catch (Exception e) {
            logger.error("Search error", e);
        }
        return null;
    }
}

