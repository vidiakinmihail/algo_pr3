import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class task2 {

    public static void main(String[] args) throws IOException {
        FastReader reader = new FastReader();
        int n = reader.nextInt();
        int m = reader.nextInt();
        int s = reader.nextInt();

        Graph graph = new AdjacencyListGraph(n, true);
        for (int i = 0; i < m; i++) {
            int u = reader.nextInt();
            int v = reader.nextInt();
            long w = reader.nextLong();
            graph.addEdge(u, v, w);
        }

        try {
            DijkstraResult result = Dijkstra.search(graph, s);
            System.out.println(formatDistances(result));
        } catch (IllegalArgumentException e) {
            System.out.println("INVALID");
        }
    }

    private static String formatDistances(DijkstraResult result) {
        StringBuilder sb = new StringBuilder();
        for (int v = 0; v < result.vertexCount(); v++) {
            if (v > 0) {
                sb.append(' ');
            }
            if (result.isReachable(v)) {
                sb.append(result.distanceTo(v));
            } else {
                sb.append("INF");
            }
        }
        return sb.toString();
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

    static final class BinaryMinHeap<T> {
        private static final int DEFAULT_CAPACITY = 8;

        private final Comparator<? super T> comparator;
        private Object[] elements;
        private int size;

        public BinaryMinHeap(Comparator<? super T> comparator) {
            this(comparator, DEFAULT_CAPACITY);
        }

        public BinaryMinHeap(Comparator<? super T> comparator, int initialCapacity) {
            if (comparator == null) {
                throw new IllegalArgumentException("comparator must not be null");
            }
            if (initialCapacity < 0) {
                throw new IllegalArgumentException("initialCapacity must be non-negative");
            }
            this.comparator = comparator;
            this.elements = new Object[Math.max(1, initialCapacity)];
        }

        public boolean isEmpty() { return size == 0; }
        public int size() { return size; }

        public void add(T value) {
            if (value == null) {
                throw new IllegalArgumentException("heap does not accept null values");
            }
            ensureCapacity();
            elements[size] = value;
            siftUp(size++);
        }

        public T removeMin() {
            if (isEmpty()) {
                throw new NoSuchElementException("heap is empty");
            }

            T minimum = elementAt(0);
            size--;
            if (size > 0) {
                elements[0] = elements[size];
                elements[size] = null;
                siftDown(0);
            } else {
                elements[0] = null;
            }
            return minimum;
        }

        private void siftUp(int index) {
            while (index > 0) {
                int parent = (index - 1) / 2;
                if (comparator.compare(elementAt(index), elementAt(parent)) >= 0) return;
                swap(index, parent);
                index = parent;
            }
        }

        private void siftDown(int index) {
            while (true) {
                int left = index * 2 + 1;
                if (left >= size) return;
                int right = left + 1;
                int smallerChild = right < size
                        && comparator.compare(elementAt(right), elementAt(left)) < 0
                        ? right : left;
                if (comparator.compare(elementAt(index), elementAt(smallerChild)) <= 0) return;
                swap(index, smallerChild);
                index = smallerChild;
            }
        }

        private void swap(int first, int second) {
            Object value = elements[first];
            elements[first] = elements[second];
            elements[second] = value;
        }

        private void ensureCapacity() {
            if (size == elements.length) {
                int newCapacity = elements.length <= Integer.MAX_VALUE / 2
                        ? elements.length * 2 : Integer.MAX_VALUE;
                if (newCapacity == elements.length) {
                    throw new IllegalStateException("heap capacity limit reached");
                }
                Object[] expanded = new Object[newCapacity];
                System.arraycopy(elements, 0, expanded, 0, size);
                elements = expanded;
            }
        }

        @SuppressWarnings("unchecked")
        private T elementAt(int index) {
            return (T) elements[index];
        }
    }

    static final class DijkstraResult {
        public static final long UNREACHABLE = Long.MAX_VALUE;

        private final int source;
        private final long[] distances;
        private final int[] parents;

        DijkstraResult(int source, long[] distances, int[] parents) {
            this.source = source;
            this.distances = distances.clone();
            this.parents = parents.clone();
        }

        public int source() { return source; }
        public int vertexCount() { return distances.length; }
        public long distanceTo(int vertex) { checkVertex(vertex); return distances[vertex]; }
        public int parentOf(int vertex) { checkVertex(vertex); return parents[vertex]; }
        public boolean isReachable(int vertex) { return distanceTo(vertex) != UNREACHABLE; }
        public long[] distances() { return distances.clone(); }
        public int[] parents() { return parents.clone(); }

        private void checkVertex(int vertex) {
            if (vertex < 0 || vertex >= distances.length) {
                throw new IndexOutOfBoundsException("vertex out of range: " + vertex);
            }
        }
    }

    static final class Dijkstra {
        private Dijkstra() { }

        public static DijkstraResult search(Graph graph, int source) {
            GraphChecks.requireVertex(graph, source);
            validateNonNegativeWeights(graph);

            int n = graph.vertexCount();
            long[] distance = new long[n];
            int[] parent = new int[n];
            Arrays.fill(distance, DijkstraResult.UNREACHABLE);
            Arrays.fill(parent, -1);
            distance[source] = 0;

            BinaryMinHeap<Entry> queue = new BinaryMinHeap<>(
                    Comparator.comparingLong(Entry::distance).thenComparingInt(Entry::vertex));
            queue.add(new Entry(source, 0));
            while (!queue.isEmpty()) {
                Entry current = queue.removeMin();
                if (current.distance != distance[current.vertex]) continue;

                graph.forEachOutgoing(current.vertex, (to, weight) -> {
                    long candidate;
                    try {
                        candidate = Math.addExact(current.distance, weight);
                    } catch (ArithmeticException overflow) {
                        throw new ArithmeticException("path length exceeds long range");
                    }
                    if (candidate < distance[to]) {
                        distance[to] = candidate;
                        parent[to] = current.vertex;
                        queue.add(new Entry(to, candidate));
                    }
                });
            }
            return new DijkstraResult(source, distance, parent);
        }

        private static void validateNonNegativeWeights(Graph graph) {
            for (int vertex = 0; vertex < graph.vertexCount(); vertex++) {
                graph.forEachOutgoing(vertex, (to, weight) -> {
                    if (weight < 0) {
                        throw new IllegalArgumentException("Dijkstra does not support negative weights");
                    }
                });
            }
        }

        private record Entry(int vertex, long distance) { }
    }
}
