
import helper.Utilities;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.impl.path.Dijkstra;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.traversal.Evaluators;
import static org.neo4j.helpers.collection.IteratorUtil.firstOrNull;
import org.neo4j.kernel.Traversal;
import static org.neo4j.server.rest.transactional.ResultDataContent.graph;
import org.junit.Assert;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.RelationshipExpander;

public class tester {

    public String greeting;

    // START SNIPPET: vars
    private static final String DB_PATH = "C:/Users/anu/Documents/Neo4j/test2.graphdb";
    private static GraphDatabaseService graphDb;
    private static Index<Node> nodeIndex;
    private static Index<Node> referenceIndex;
    private static int TOTAL_NUMBER_OF_NODES = 4039;
    // END SNIPPET: vars

    // START SNIPPET: createReltype
    private static enum RelTypes implements RelationshipType {

        USER, FB_FRIENDS
    }

    // END SNIPPET: createReltype
//   *************** FUNCTIONS ****************
    public Path shortest_path(Node node1, Node node2) {
        double ret = 0;
//        maxDepth - the max Path.length() returned paths are allowed to have----> 10
        PathFinder<Path> finder = GraphAlgoFactory.shortestPath(Traversal.expanderForTypes(RelTypes.FB_FRIENDS, Direction.BOTH), 10);
        Path foundPath = finder.findSinglePath(node1, node2);
//        Iterable<Path> paths = finder.findAllPaths( node1,node2 );
        return foundPath;
    }

    public void MyDijkstra(Node node1, Node node2) {
     PathFinder<WeightedPath> dijkstraPathFinder;
     RelationshipExpander expander;
     CostEvaluator<Double> costEvaluator;
        expander = Traversal.expanderForTypes(
                RelTypes.FB_FRIENDS, Direction.BOTH );
        costEvaluator = new CostEvaluator<Double>() {
            public Double getCost(Relationship relationship, Direction direction) {
                Assert.assertEquals(Direction.BOTH, direction);
                return 1.0;
            }
        };
        dijkstraPathFinder = GraphAlgoFactory.dijkstra( expander, costEvaluator );
    }

    public int commonNeighbors(Node node1, Node node2) {
        int ret = 0;
        Transaction tx = graphDb.beginTx();
        nodeIndex = graphDb.index().forNodes("nodes");
        List<Node> node1friends = new ArrayList<>();
        List<Node> common = new ArrayList<>();
        for (final Path position : Traversal.description()
                .evaluator(Evaluators.fromDepth(1))
                .evaluator(Evaluators.toDepth(1))
                .evaluator(Evaluators.excludeStartPosition()).traverse(node1)) {
            node1friends.add(position.endNode());
        }
        System.out.println("Neighboors of node 2");
        for (final Path position : Traversal.description()
                .evaluator(Evaluators.fromDepth(1))
                .evaluator(Evaluators.toDepth(1))
                .evaluator(Evaluators.excludeStartPosition()).traverse(node2)) {
            for (Node n : node1friends) {
                if (n.equals(position.endNode())) {
                    common.add(position.endNode());
                }
            }
        }
        return common.size();
    }
//    measures the probability that both x and y have a feature f for a randomly selected feature f that either x or y has

    public double jaccard_coef() {
        double ret = 0;

        return ret;
    }

    // Jaccard index
//MATCH (u1:User), (u2:User) WHERE u1 <> u2
//MATCH (u1)-[:FOLLOWS]->(mutual)<-[:FOLLOWS]-(u2) WITH u1, u2, count(mutual) as intersect
//MATCH (u1)-[:FOLLOWS]->(u1_f) WITH u1, u2, intersect, collect(DISTINCT u1_f) AS coll1
//MATCH (u2)-[:FOLLOWS]->(u2_f) WITH u1, u2, collect(DISTINCT u2_f) AS coll2, coll1, intersect
//WITH u1, u2, intersect, coll1, coll2, length(coll1 + filter(x IN coll2 WHERE NOT x IN coll1)) as union
//CREATE (u1)<-[:SIMILAR_TO {coef: (1.0*intersect/union)}]-(u2)
//    *********** END FUNCTIONS ******************
    
     public Node getNode( final String id )
    {
        return firstOrNull(nodeIndex.get("id", id).iterator());
    }
        /**
     * Find a node or create a new node if it doesn't exist.
     * 
     * @param nodeName
     * @return
     */
    private Node findOrCreateNode( final String nodeName )
    {
        Node node = getNode( nodeName );
        if ( node == null )
        {
            node = graphDb.createNode();
            node.setProperty( "id", nodeName );
            nodeIndex.add( node, "id", nodeName );
        }
        return node;
    } 
    private static void shutdown() {
        System.out.println();
        System.out.println("Shutting down database ...");
        // START SNIPPET: shutdownServer
        graphDb.shutdown();
        // END SNIPPET: shutdownServer
    }

    // START SNIPPET: shutdownHook
    private static void registerShutdownHook() {
        // Registers a shutdown hook for the Neo4j and index service instances
        // so that it shuts down nicely when the VM exits (even if you
        // "Ctrl-C" the running example before it's completed)
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdown();
            }
        });
    }
    // END SNIPPET: shutdownHook

    static Relationship getRelationshipBetween(Node n1, Node n2) { // RelationshipType type, Direction direction
        for (Relationship rel : n1.getRelationships()) { // n1.getRelationships(type,direction)
            if (rel.getOtherNode(n1).equals(n2)) {
                return rel;
            }
        }
        return null;
    }

    private static Node createAndIndexUser(String username) {

        Node node = graphDb.createNode();
        node.setProperty("id", username);
        nodeIndex.add(node, "id", username);
        System.out.println("User " + username + " created successfully.");
        return node;
    }

    private static void addNodes(String featfileLocation, String featnamesfileLocation, String edgesfileLocation) {
        Transaction tx = graphDb.beginTx();
        HashMap<String, String> feature_names = new HashMap<>();
        try {
            BufferedReader feat = new BufferedReader(new FileReader(featfileLocation));
            BufferedReader featnames = new BufferedReader(new FileReader(featnamesfileLocation));
            BufferedReader edges = new BufferedReader(new FileReader(edgesfileLocation));
            String line = "";

            // Create users sub reference node
//            Node usersReferenceNode = graphDb.createNode();
//            usersReferenceNode.setProperty("reference", "users");
//            referenceIndex.add(usersReferenceNode, "reference", "users");
//get featnames in hashmap in order to search them more easily
            while ((line = featnames.readLine()) != null) {
                line = line.replaceAll(";", " ");
                String[] features = line.split(" ");
                String property = "";
                String value = features[features.length - 3];
                for (int i = 1; i < features.length - 3; i++) {
                    if (i < features.length - 4) {
                        property += features[i] + "_";
                    } else {
                        property += features[i];
                    }
                }
                for (int i = features.length - 2; i < features.length; i++) {
                    value += "_" + features[i];
                }
                feature_names.put(features[0], property + ":" + value);
            }
            int helpInt = 0;
            while ((line = edges.readLine()) != null) {
                helpInt++;
                boolean relexists = false;
                if (helpInt == 754) {
                    System.out.print("here it gets stuck but why?!");
                }
                String[] edge = line.split(" ");
//                System.out.println(line);
                Node userA = firstOrNull(nodeIndex.get("id", edge[0]).iterator());
                if (userA == null) {
                    userA = graphDb.createNode();
                    userA.setProperty("id", edge[0]);
                    nodeIndex.add(userA, "id", edge[0]);
                    tx.success();
                }

                // Node userA = hits.getSingle();
//                IndexHits<Node> hits2 = nodeIndex.get("id", edge[1]);
//                Node userB = hits2.getSingle();
                Node userB = firstOrNull(nodeIndex.get("id", edge[1]).iterator());
                if (userB == null) {
                    userB = graphDb.createNode();
                    userB.setProperty("id", edge[1]);
                    nodeIndex.add(userB, "id", edge[1]);
                    tx.success();
                }
                //  userA.createRelationshipTo(userB, RelTypes.FB_FRIENDS);

                if (!Utilities.isEmpty(userA.getRelationships(RelTypes.FB_FRIENDS))) {
                    for (Relationship neighbor : userA.getRelationships(RelTypes.FB_FRIENDS)) {
                        if (neighbor.getOtherNode(userA).equals(userB)) {
                            relexists = true;
                        }//has multiple relationships 
                    }
                    if (!relexists) {
                        userA.createRelationshipTo(userB, RelTypes.FB_FRIENDS);
                        //   System.out.println("1 Added relatioship between " + userA + "and " + userB);
                    }
                } else {
                    userA.createRelationshipTo(userB, RelTypes.FB_FRIENDS);
                    //   System.out.println("2 Added relatioship between " + userA + "and " + userB);
                    tx.success();
                }

            }
            while ((line = feat.readLine()) != null) {
                String[] features = line.split(" ");

                for (int f = 1; f < features.length; f++) {
                    if (features[f].equals("1")) {
                        String[] keypair;
                        IndexHits<Node> hits = nodeIndex.get("id", features[0]);
                        Node puser = hits.getSingle();
                        if (feature_names.containsKey(Integer.toString(f))) {
                            keypair = feature_names.get(Integer.toString(f)).split(":");
                            puser.setProperty(keypair[0], keypair[1]);
                            tx.success();
                        }
                    }

                }
            }

        } catch (IOException ex) {
            Logger.getLogger(tester.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception exc) {
            System.out.println("==> " + exc.getMessage());
        } finally {
            tx.close();
        }
    }

    public static void main(final String[] args) {

        // START SNIPPET: startDb- with auto-indexing
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);
//                newEmbeddedDatabaseBuilder(DB_PATH).
//                setConfig(GraphDatabaseSettings.node_keys_indexable, "id").
//                setConfig(GraphDatabaseSettings.relationship_keys_indexable, "KNOWS, FB_FRIENDS").
//                setConfig(GraphDatabaseSettings.node_auto_indexing, "true").
//                setConfig(GraphDatabaseSettings.relationship_auto_indexing, "true").
//                newGraphDatabase();
//        registerShutdownHook();
        Transaction tx = graphDb.beginTx();
        nodeIndex = graphDb.index().forNodes("nodes");

        for (int id = 1; id < TOTAL_NUMBER_OF_NODES + 1; id++) {
            Node node = graphDb.createNode();
            node.setProperty("id", Integer.toString(id));
            nodeIndex.add(node, "id", Integer.toString(id));
//            System.out.println("User " + id + " created successfully."+node.toString());     
            tx.success();
        }

        tx.close();
//IndexHits<Node> allNodes = nodeIndex.query( "id", "*" );
//        for (Iterator<Node> it = allNodes.iterator(); it.hasNext();) {
//            Node t = it.next();
//            System.out.println(t.getId()+"_"+t.getProperty("id").toString());
//        }
        // END SNIPPET: startDb
        // START SNIPPET: addUsers
//create all user nodes must be performed once
        String[] featList = {"C:/Users/anu/Desktop/INFL-papers/facebook/0.feat", "C:/Users/anu/Desktop/INFL-papers/facebook/107.feat", "C:/Users/anu/Desktop/INFL-papers/facebook/698.feat", "C:/Users/anu/Desktop/INFL-papers/facebook/348.feat",
            "C:/Users/anu/Desktop/INFL-papers/facebook/414.feat", "C:/Users/anu/Desktop/INFL-papers/facebook/686.feat",
            "C:/Users/anu/Desktop/INFL-papers/facebook/1684.feat", "C:/Users/anu/Desktop/INFL-papers/facebook/1912.feat", "C:/Users/anu/Desktop/INFL-papers/facebook/3437.feat",
            "C:/Users/anu/Desktop/INFL-papers/facebook/3980.feat"};
        String[] featnamesList = {"C:/Users/anu/Desktop/INFL-papers/facebook/0.featnames", "C:/Users/anu/Desktop/INFL-papers/facebook/107.featnames", "C:/Users/anu/Desktop/INFL-papers/facebook/348.featnames",
            "C:/Users/anu/Desktop/INFL-papers/facebook/414.featnames", "C:/Users/anu/Desktop/INFL-papers/facebook/686.featnames",
            "C:/Users/anu/Desktop/INFL-papers/facebook/1684.featnames", "C:/Users/anu/Desktop/INFL-papers/facebook/1912.featnames", "C:/Users/anu/Desktop/INFL-papers/facebook/3437.featnames",
            "C:/Users/anu/Desktop/INFL-papers/facebook/3980.featnames", "C:/Users/anu/Desktop/INFL-papers/facebook/698.featnames"};
        String[] edgesList = {"C:/Users/anu/Desktop/INFL-papers/facebook/0.edges", "C:/Users/anu/Desktop/INFL-papers/facebook/107.edges", "C:/Users/anu/Desktop/INFL-papers/facebook/348.edges",
            "C:/Users/anu/Desktop/INFL-papers/facebook/414.edges", "C:/Users/anu/Desktop/INFL-papers/facebook/686.edges",
            "C:/Users/anu/Desktop/INFL-papers/facebook/1684.edges", "C:/Users/anu/Desktop/INFL-papers/facebook/1912.edges", "C:/Users/anu/Desktop/INFL-papers/facebook/3437.edges",
            "C:/Users/anu/Desktop/INFL-papers/facebook/3980.edges", "C:/Users/anu/Desktop/INFL-papers/facebook/698.edges"};

        for (int i = 0; i < featList.length; i++) {
            addNodes(featList[i], featnamesList[i], edgesList[i]);
        }

        shutdown();
    }

}
