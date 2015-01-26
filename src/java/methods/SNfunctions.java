/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package methods;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipExpander;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.traversal.Evaluators;
import static org.neo4j.helpers.collection.IteratorUtil.firstOrNull;
import org.neo4j.kernel.Traversal;

public class SNfunctions {

    public String greeting;

    // START SNIPPET: vars
    private static final String DB_PATH = "C:/Users/anu/Documents/Neo4j/test2.graphdb";
    private static GraphDatabaseService graphDb;
    private static Index<Node> nodeIndex;
    private static Index<Node> referenceIndex;
    // END SNIPPET: vars

    // START SNIPPET: createReltype
    private static enum RelTypes implements RelationshipType {

        USER, FB_FRIENDS
    }

    // END SNIPPET: createReltype
//   *************** FUNCTIONS ****************
    public static Path shortest_path(Node node1, Node node2) {
        double ret = 0;
//        maxDepth - the max Path.length() returned paths are allowed to have----> 10
        PathFinder<Path> finder = GraphAlgoFactory.shortestPath(Traversal.expanderForTypes(RelTypes.FB_FRIENDS, Direction.BOTH), 10);
        Path foundPath = finder.findSinglePath(node1, node2);
//        Iterable<Path> paths = finder.findAllPaths( node1,node2 );
        return foundPath;
    }

    public static Path MyDijkstra(Node node1, Node node2) {
        PathFinder<WeightedPath> dijkstraPathFinder;
        RelationshipExpander expander;
        CostEvaluator<Double> costEvaluator;
        expander = Traversal.expanderForTypes(
                RelTypes.FB_FRIENDS, Direction.INCOMING,RelTypes.FB_FRIENDS, Direction.OUTGOING);
        costEvaluator = new CostEvaluator<Double>() {
            public Double getCost(Relationship relationship, Direction direction) {
               return 1.0;
            }
        };
        dijkstraPathFinder = GraphAlgoFactory.dijkstra(expander, costEvaluator);
        return dijkstraPathFinder.findSinglePath(node1, node2);
    }

    public static List<Node> commonNeighbors(Node node1, Node node2) {
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
            System.out.println("   nei  "+position.endNode().getId());
        }
        System.out.println("Neighboors of node 2");
        for (final Path position : Traversal.description()
                .evaluator(Evaluators.fromDepth(1))
                .evaluator(Evaluators.toDepth(1))
                .evaluator(Evaluators.excludeStartPosition()).traverse(node2)) {
            for (Node n : node1friends) {
                if (n.equals(position.endNode())) {
                    common.add(position.endNode());
                     System.out.print("   neiC  "+position.endNode().getId());
                }
            }
           
        }
        return common;
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
    public static Node getNode(final String id) {
        return firstOrNull(nodeIndex.get("id", id).iterator());
    }

    /**
     * Find a node or create a new node if it doesn't exist.
     *
     * @param nodeName
     * @return
     */
    private static Node findOrCreateNode(final String nodeName) {
        Node node = getNode(nodeName);
        if (node == null) {
            node = graphDb.createNode();
            node.setProperty("id", nodeName);
            nodeIndex.add(node, "id", nodeName);
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

    public double adamic() {
        double ret = 0;
        return ret;
    }

    public static void main(final String[] args) {

        // START SNIPPET: startDb- with auto-indexing
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);
        Transaction tx = graphDb.beginTx();
        nodeIndex = graphDb.index().forNodes("nodes");
        System.out.println("node 21 degree"+nodeIndex.get("id", "21").getSingle().getDegree());
        System.out.println("node 57 degree"+nodeIndex.get("id", "57").getSingle().getDegree());
        System.out.println("dijkstra");
        System.out.println(MyDijkstra(nodeIndex.get("id", "21").getSingle(), nodeIndex.get("id", "57").getSingle()).toString());
        System.out.println("shortest path");
        System.out.println(shortest_path(nodeIndex.get("id", "21").getSingle(), nodeIndex.get("id", "57").getSingle()).toString());
        List<Node> myList=commonNeighbors(nodeIndex.get("id", "21").getSingle(), nodeIndex.get("id", "57").getSingle());
        System.out.println("common neighbors count: "+ myList.size());
        System.out.println("common neighbors are: ");
        System.out.println(myList.toString());
        tx.close();
        shutdown();
    }
}
