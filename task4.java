import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.StringTokenizer;

public class task4 {

    public static void main(String[] args) throws IOException {
        FastReader reader = new FastReader();
        int n = reader.nextInt();
        int m = reader.nextInt();

        Graph graph = new AdjacencyListGraph(n, true);
        for (int i = 0; i < m; i++) {
            int dependency = reader.nextInt();
            int pkg = reader.nextInt();
            graph.addEdge(dependency, pkg, 1);
        }

        TopologicalSortResult result = TopologicalSort.sort(graph);
        if (!result.acyclic()) {
            System.out.println("CYCLE");
        } else {
            System.out.println(formatOrder(result.order()));
        }
    }

    private static String formatOrder(List<Integer> order) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < order.size(); i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(order.get(i));
        }
        return sb.toString();
    }

    private static final class FastReader {
        private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        private StringTokenizer tokenizer;

        int nextInt() throws IOException {
            while (tokenizer == null || !tokenizer.hasMoreTokens()) {
                String line = reader.readLine();
                if (line == null) {
                    throw new IllegalStateException("Unexpected end of input");
                }
                tokenizer = new StringTokenizer(line);
            }
            return Integer.parseInt(tokenizer.nextToken());
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

    static class AdjacencyListNode {

        private int to;
        private long weight;
        private AdjacencyListNode next;

        public AdjacencyListNode(int to, long weight, AdjacencyListNode next) {
            this.to = to;
            this.weight = weight;
            this.next = next;
        }

        public int getTo() {
            return to;
        }

        public void setTo(int to) {
            this.to = to;
        }

        public long getWeight() {
            return weight;
        }

        public void setWeight(long weight) {
            this.weight = weight;
        }

        public AdjacencyListNode getNext() {
            return next;
        }

        public void setNext(AdjacencyListNode next) {
            this.next = next;
        }
    }

    static class AdjacencyListGraph implements Graph {

        private final AdjacencyListNode[] heads;
        private final boolean directed;

        public AdjacencyListGraph(int vertexCount, boolean directed) {
            if (vertexCount < 0) {
                throw new IllegalArgumentException("vertexCount must be non-negative");
            }
            this.heads = new AdjacencyListNode[vertexCount];
            this.directed = directed;
        }

        @Override
        public int vertexCount() {
            return this.heads.length;
        }

        @Override
        public boolean isDirected() {
            return directed;
        }

        @Override
        public void addEdge(int from, int to, long weight) {
            checkVertex(from);
            checkVertex(to);
            this.heads[from] = new AdjacencyListNode(to, weight, this.heads[from]);
            if (!directed) {
                this.heads[to] = new AdjacencyListNode(from, weight, this.heads[to]);
            }
        }

        @Override
        public boolean containsEdge(int from, int to) {
            checkVertex(from);
            checkVertex(to);
            for (AdjacencyListNode node = heads[from]; node != null; node = node.getNext()) {
                if (node.getTo() == to) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void forEachOutgoing(int from, EdgeConsumer consumer) {
            checkVertex(from);
            if (consumer == null) {
                throw new IllegalArgumentException("consumer must not be null");
            }
            for (AdjacencyListNode node = heads[from];
                 node != null;
                 node = node.getNext()
            ) {
                consumer.accept(node.getTo(), node.getWeight());
            }
        }

        private void checkVertex(int vertex) {
            if (vertex < 0 || vertex >= heads.length) {
                throw new IndexOutOfBoundsException("vertex out of range: " + vertex);
            }
        }
    }

    static final class GraphChecks {

        private GraphChecks() { }

        static void requireGraph(Graph graph) {
            if (graph == null) {
                throw new IllegalArgumentException("graph must not be null");
            }
        }

        static void requireVertex(Graph graph, int vertex) {
            requireGraph(graph);
            if (vertex < 0 || vertex >= graph.vertexCount()) {
                throw new IndexOutOfBoundsException("vertex out of range: " + vertex);
            }
        }

    }

    record TopologicalSortResult(List<Integer> order, boolean acyclic) {
        TopologicalSortResult {
            order = List.copyOf(order);
        }
    }

    static final class TopologicalSort {
        private TopologicalSort() { }

        public static TopologicalSortResult sort(Graph graph) {
            GraphChecks.requireGraph(graph);
            if (!graph.isDirected()) {
                throw new IllegalArgumentException("topological sort requires a directed graph");
            }

            int n = graph.vertexCount();
            int[] indegree = new int[n];
            for (int vertex = 0; vertex < n; vertex++) {
                graph.forEachOutgoing(vertex, (to, weight) -> indegree[to]++);
            }

            PriorityQueue<Integer> ready = new PriorityQueue<>();
            for (int vertex = 0; vertex < n; vertex++) {
                if (indegree[vertex] == 0) ready.add(vertex);
            }

            List<Integer> order = new ArrayList<>(n);
            while (!ready.isEmpty()) {
                int vertex = ready.remove();
                order.add(vertex);
                graph.forEachOutgoing(vertex, (to, weight) -> {
                    if (--indegree[to] == 0) ready.add(to);
                });
            }
            return new TopologicalSortResult(order, order.size() == n);
        }
    }
}
