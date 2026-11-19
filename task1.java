import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.StringTokenizer;

public class task1 {

    public static void main(String[] args) throws IOException {
        FastReader reader = new FastReader();
        int n = reader.nextInt();
        int m = reader.nextInt();
        int s = reader.nextInt();

        Graph graph = new AdjacencyListGraph(n, false);
        for (int i = 0; i < m; i++) {
            int u = reader.nextInt();
            int v = reader.nextInt();
            graph.addEdge(u, v, 1);
        }

        int[] stackOrder = DFS.search(graph, s);
        int[] queueOrder = BFS.search(graph, s);

        System.out.println(formatOrder(stackOrder));
        System.out.println(formatOrder(queueOrder));
    }

    private static String formatOrder(int[] order) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < order.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(order[i]);
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

class AdjacencyListNode {

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

class AdjacencyListGraph implements Graph {

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

final class GraphChecks {

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

class Node {

    private int value;
    private Node next;

    public Node(int value, Node next) {
        this.value = value;
        this.next = next;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }
}

class IntStack {

    private Node top;

    boolean isEmpty() {
        return top == null;
    }

    void push(int val) {
        top = new Node(val, top);
    }

    int pop() {
        if (isEmpty()) {
            throw new IllegalStateException("stack is empty");
        }

        int result = top.getValue();
        top = top.getNext();
        return result;
    }

}

class IntQueue {

    private Node head;
    private Node tail;

    boolean isEmpty() {
        return head == null;
    }

    void enqueue(int val) {
        Node node = new Node(val, null);
        if (tail == null) {
            head = node;
        } else {
            tail.setNext(node);
        }
        tail = node;
    }

    int dequeue() {
        if (isEmpty()) {
            throw new IllegalStateException("queue is empty");
        }

        int result = head.getValue();
        head = head.getNext();
        if (head == null) {
            tail = null;
        }
        return result;
    }

}

class DFS {

    private DFS() { }

    public static int[] search(Graph graph, int start) {
        GraphChecks.requireVertex(graph, start);
        int n = graph.vertexCount();
        boolean[] visited = new boolean[n];
        int[] order = new int[n];
        int count = 0;

        IntStack stack = new IntStack();
        stack.push(start);

        while (!stack.isEmpty()) {
            int v = stack.pop();
            if (visited[v]) {
                continue;
            }
            visited[v] = true;
            order[count++] = v;
            graph.forEachOutgoing(
                    v,
                    (to, weight) -> {
                        if (!visited[to]) {
                            stack.push(to);
                        }
                    }
            );
        }

        return Arrays.copyOf(order, count);
    }

}

class BFS {

    private BFS() { }

    public static int[] search(Graph graph, int start) {
        GraphChecks.requireVertex(graph, start);
        int n = graph.vertexCount();
        boolean[] visited = new boolean[n];
        int[] order = new int[n];
        int count = 0;

        IntQueue queue = new IntQueue();
        queue.enqueue(start);
        visited[start] = true;

        while (!queue.isEmpty()) {
            int current = queue.dequeue();
            order[count++] = current;
            graph.forEachOutgoing(
                    current,
                    (to, weight) -> {
                        if (!visited[to]) {
                            visited[to] = true;
                            queue.enqueue(to);
                        }
                    }
            );
        }

        return Arrays.copyOf(order, count);
    }

}
