import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Consumer;

public class task3 {

    public static void main(String[] args) throws IOException {
        FastReader reader = new FastReader();
        int n = reader.nextInt();
        int m = reader.nextInt();

        EdgeGraph graph = new EdgeListGraph(n, false);
        for (int i = 0; i < m; i++) {
            int u = reader.nextInt();
            int v = reader.nextInt();
            long w = reader.nextLong();
            graph.addEdge(u, v, w);
        }

        MinimumSpanningForest forest = Kruskal.minimumSpanningForest(graph);
        System.out.println("cost=" + forest.totalWeight() + " edges=" + forest.edges().size());
    }

    private static final class FastReader {
        private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        private StringTokenizer tokenizer;

        int nextInt() throws IOException {
            return Integer.parseInt(nextToken());
        }

        long nextLong() throws IOException {
            return Long.parseLong(nextToken());
        }

        private String nextToken() throws IOException {
            while (tokenizer == null || !tokenizer.hasMoreTokens()) {
                String line = reader.readLine();
                if (line == null) {
                    throw new IllegalStateException("Unexpected end of input");
                }
                tokenizer = new StringTokenizer(line);
            }
            return tokenizer.nextToken();
        }
    }

    interface Graph {

        int vertexCount();
        boolean isDirected();
        void addEdge(int from, int to, long weight);
        boolean containsEdge(int from, int to);
        void forEachOutgoing(int from, EdgeConsumer consumer);

    }

    @FunctionalInterface
    interface EdgeConsumer {

        void accept(int to, long weight);

    }

    interface EdgeGraph extends Graph {

        void forEachEdge(Consumer<WeightedEdge> consumer);

    }

    record WeightedEdge(int from, int to, long weight) { }

    static final class EdgeListGraph implements EdgeGraph {
        private final int vertexCount;
        private final boolean directed;
        private final List<WeightedEdge> edges = new ArrayList<>();

        public EdgeListGraph(int vertexCount, boolean directed) {
            if (vertexCount < 0) {
                throw new IllegalArgumentException("vertexCount must be non-negative");
            }
            this.vertexCount = vertexCount;
            this.directed = directed;
        }

        @Override
        public int vertexCount() { return vertexCount; }

        @Override
        public boolean isDirected() { return directed; }

        @Override
        public void addEdge(int from, int to, long weight) {
            checkVertex(from);
            checkVertex(to);
            edges.add(new WeightedEdge(from, to, weight));
        }

        @Override
        public boolean containsEdge(int from, int to) {
            checkVertex(from);
            checkVertex(to);
            for (WeightedEdge edge : edges) {
                if (edge.from() == from && edge.to() == to) return true;
                if (!directed && edge.from() == to && edge.to() == from) return true;
            }
            return false;
        }

        @Override
        public void forEachOutgoing(int from, EdgeConsumer consumer) {
            checkVertex(from);
            if (consumer == null) {
                throw new IllegalArgumentException("consumer must not be null");
            }
            for (WeightedEdge edge : edges) {
                if (edge.from() == from) {
                    consumer.accept(edge.to(), edge.weight());
                } else if (!directed && edge.to() == from) {
                    consumer.accept(edge.from(), edge.weight());
                }
            }
        }

        @Override
        public void forEachEdge(Consumer<WeightedEdge> consumer) {
            if (consumer == null) {
                throw new IllegalArgumentException("consumer must not be null");
            }
            edges.forEach(consumer);
        }

        private void checkVertex(int vertex) {
            if (vertex < 0 || vertex >= vertexCount) {
                throw new IndexOutOfBoundsException("vertex out of range: " + vertex);
            }
        }
    }

    static final class DisjointSetUnion {
        private final int[] parent;
        private final byte[] rank;
        private int components;

        public DisjointSetUnion(int size) {
            if (size < 0) throw new IllegalArgumentException("size must be non-negative");
            parent = new int[size];
            rank = new byte[size];
            components = size;
            for (int i = 0; i < size; i++) parent[i] = i;
        }

        public int find(int value) {
            check(value);
            if (parent[value] != value) parent[value] = find(parent[value]);
            return parent[value];
        }

        public boolean union(int first, int second) {
            int rootFirst = find(first);
            int rootSecond = find(second);
            if (rootFirst == rootSecond) return false;
            if (rank[rootFirst] < rank[rootSecond]) {
                parent[rootFirst] = rootSecond;
            } else if (rank[rootFirst] > rank[rootSecond]) {
                parent[rootSecond] = rootFirst;
            } else {
                parent[rootSecond] = rootFirst;
                rank[rootFirst]++;
            }
            components--;
            return true;
        }

        public boolean connected(int first, int second) { return find(first) == find(second); }
        public int componentCount() { return components; }

        private void check(int value) {
            if (value < 0 || value >= parent.length) {
                throw new IndexOutOfBoundsException("value out of range: " + value);
            }
        }
    }

    record MinimumSpanningForest(List<WeightedEdge> edges, long totalWeight, int componentCount) {
        MinimumSpanningForest {
            edges = List.copyOf(edges);
        }

        public boolean isSpanningTree() {
            return componentCount == 1;
        }
    }

    static final class Kruskal {
        private Kruskal() { }

        public static MinimumSpanningForest minimumSpanningForest(EdgeGraph edgeGraph) {
            if (edgeGraph == null) {
                throw new IllegalArgumentException("edgeGraph must not be null");
            }
            if (edgeGraph.isDirected()) {
                throw new IllegalArgumentException("Kruskal requires an undirected graph");
            }

            List<WeightedEdge> sortedEdges = new ArrayList<>();
            edgeGraph.forEachEdge(sortedEdges::add);
            sortedEdges.sort(Comparator.comparingLong(WeightedEdge::weight)
                    .thenComparingInt(WeightedEdge::from)
                    .thenComparingInt(WeightedEdge::to));

            DisjointSetUnion dsu = new DisjointSetUnion(edgeGraph.vertexCount());
            List<WeightedEdge> chosen = new ArrayList<>();
            long totalWeight = 0;
            for (WeightedEdge edge : sortedEdges) {
                if (dsu.union(edge.from(), edge.to())) {
                    chosen.add(edge);
                    totalWeight = Math.addExact(totalWeight, edge.weight());
                }
            }
            return new MinimumSpanningForest(chosen, totalWeight, dsu.componentCount());
        }
    }
}
