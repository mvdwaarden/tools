package graph.ext.persist.neo4j;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import data.ConfigurationUtil;
import persist.Connection;

public class Neo4JEmbeddedConnection implements Connection {
	GraphDatabaseService graphDB;
	private static String STORE_BASE_DIR = "neo4j.store.dir";
	private static String DEFAULT_STORE = "default";
	private String store = DEFAULT_STORE;

	public Neo4JEmbeddedConnection() {

	}

	public Neo4JEmbeddedConnection(String store) {
		this.store = store;
	}

	/* (non-Javadoc)
	 * @see graph.ext.persist.neo4j.Neo4JConnection#connect()
	 */
	@Override
	public boolean connect() {
		boolean result = false;
		if (null == graphDB) {
			class Locals {
				public GraphDatabaseBuilder builder;
				public GraphDatabaseBuilder.DatabaseCreator creator;
				public String storeProperty;
			}
			final Locals _local = new Locals();
			_local.creator = new GraphDatabaseBuilder.DatabaseCreator() {
				@Override
				public GraphDatabaseService newDatabase(Map<String, String> settings) {
					GraphDatabaseFactory factory = new GraphDatabaseFactory();
					GraphDatabaseService result = factory
							.newEmbeddedDatabase(new File(ConfigurationUtil.getInstance().getSetting(_local.storeProperty)));

					return result;
				}
			};

			if (null != store && !store.isEmpty())
				_local.storeProperty = STORE_BASE_DIR + "." + store;
			else
				_local.storeProperty = STORE_BASE_DIR;

			_local.builder = new GraphDatabaseBuilder(_local.creator);
			// _local.builder.setConfig(GraphDatabaseSettings.cache_type,
			// "soft");
			// _local.builder.setConfig(GraphDatabaseSettings.store_dir,
			// _local.storeProperty);

			graphDB = _local.builder.newGraphDatabase();
			result = true;
		}
		return result;

	}
	
	public String getStore() {
		return store;
	}

	/* (non-Javadoc)
	 * @see graph.ext.persist.neo4j.Neo4JConnection#disconnect()
	 */
	@Override
	public void disconnect() {
		if (null != graphDB)
			graphDB.shutdown();

	}

	/* (non-Javadoc)
	 * @see graph.ext.persist.neo4j.Neo4JConnection#close()
	 */
	@Override
	public void close() throws IOException {
		disconnect();
	}

	public GraphDatabaseService getDB() {
		return graphDB;
	}
}
