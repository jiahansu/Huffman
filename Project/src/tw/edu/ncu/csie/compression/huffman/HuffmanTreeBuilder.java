package tw.edu.ncu.csie.compression.huffman;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/*
 * Huffman Algorithm for the DEI's Programming Contest, 2004
 * (c) Paulo Marques, 2004.
 * pmarques@dei.uc.pt
 *
 * Note: this program only process text characters:
 *       ('a'-'z' / 'A'-'Z'). Everything else is ignored.
 */
public class HuffmanTreeBuilder {

    public final static Node buildHuffmanTree(final int frequency[]) {
        // Build up the initial trees
        TreeSet<Node> trees = new TreeSet<Node>();  // List containing all trees -- ORDERED!
        for (short i = 0; i < frequency.length; i++) {
            if (frequency[i] > 0) {
                Node n = new Node(i, frequency[i]);
                trees.add(n);
            }
        }

        // Huffman algoritm
        while (trees.size() > 1) {
            Node tree1 = (Node) trees.first();
            trees.remove(tree1);
            Node tree2 = (Node) trees.first();
            trees.remove(tree2);

            Node merged = new Node(tree1, tree2);
            trees.add(merged);
        }

        // Print the resulting tree
         /*
        if (trees.size() > 0) {
        Node theTree = (Node) trees.first();
        Node.printTree(theTree);
        } else {
        System.out.println("The file didn't contain useful characters.");
        }*/
        trees.first().updateBitsList();
        return trees.first();
    }

    public final static Node adjustTree(final Node root, final Map<Short, Node> huffmanTable, final Map<Long, Set<Node>> blocks, final short symbol) {
        Node symbolNode = huffmanTable.get(symbol);
        Node NYT = huffmanTable.get(Node.NYT);
        Node nnyt = NYT;

        if (symbolNode == null) {
            nnyt = new Node(Node.NYT, 0);
            Node rightNode = new Node(symbol, 1);
            NYT.setLeftChild(nnyt);
            NYT.setRightChild(rightNode);
            NYT.symbol = Node.NON_LEAF_SYMBOL;
            NYT.updateBitsList();
            //rightNode.updateBitsList();
            rightNode.updateWeight(blocks);
            //NYT.updateBitsList();


            huffmanTable.put(symbol, rightNode);
            huffmanTable.put(Node.NYT, nnyt);

        } else {
            symbolNode.increaseWeight(blocks);
        }

        return root;
    }

    public final static Node getInitialNativeTree(final Map<Short, Node> huffmanTable, final Map<Long, Set<Node>> blocks) {
        final Node root = new Node(Node.NYT, 0);


        root.getHuffmanTable(huffmanTable);
        //root.updateBitsList();

        return root;
    }

    public static class Node implements Comparable {

        public final static short NYT = 256;
        public final static short NON_LEAF_SYMBOL = -2;
        public final static short ROOT = -1;
        public final static byte LEFT_BIT = 0;
        public final static byte RIGHT_BIT = 1;
        private long weight;
        private short symbol = NON_LEAF_SYMBOL;
        private Node leftChild;
        private Node rightChild;
        private Node leftSibling;
        private Node rightSibling;
        private Node parent;
        private final ArrayList<Byte> bitsList = new ArrayList<Byte>();

        public Node(short content, long value) {
            this.symbol = content;
            this.weight = value;
        }

        public Node(Node left, Node right) {
            // Assumes that the leftChild three is always the one that is lowest
            if (left != null && right != null) {
                this.symbol = (left.symbol < right.symbol) ? left.symbol : right.symbol;
                this.weight = left.weight + right.weight;
            } else {
                this.weight = 0;
            }
            this.setLeftChild(left);
            this.setRightChild(right);
        }

        public boolean isGrandparent(final Node node){
            boolean b = true;

            final ArrayList<Byte> b1 = node.getBitsList();
            final ArrayList<Byte> b2 = this.getBitsList();
            byte bb1, bb2;

            if(node.getBitsList().size()<this.getBitsList().size()){
                 for (int i = 0; i < b1.size(); ++i) {
                    bb1 = b1.get(i);

                    bb2 = b2.get(i);

                    if(bb1!=bb2){
                        b = false;
                        break;
                    }
                }
            }else{
                b = false;
            }

            return b;
        }

        public void increaseWeight(final Map<Long, Set<Node>> blocks) {
            Node node = Node.findMax(weight, blocks);

            if (node != null && this != node&& !this.isGrandparent(node)) {
                node.swap(this);
                node.parent.updateWeight(blocks);//update node's weight will cause the changing again!!
                //node.updateWeight(blocks);
            }
            Node.updateBlocks(this, blocks, weight, weight + 1);
            ++this.weight;
            this.parent.updateWeight(blocks);//update self will cause the swaping with asscentor node
        }

        private final static Node findMax(final long weight, final Map<Long, Set<Node>> blocks) {
            final Set<Node> set = blocks.get(weight);
            //long temp;
            //long maxWeight = Long.MIN_VALUE;
            Node maxNode = null;
            ArrayList<Byte> maxBitsList = null;
            boolean comp;

            if (set != null) {
                for (Node node : set) {
                    //temp = node.getWeight();
                    if (maxNode == null) {
                        comp = true;
                    } else {
                        comp = Node.compare(maxBitsList, node.getBitsList()) < 0;
                    }

                    if (comp) {
                        maxBitsList = node.getBitsList();
                        maxNode = node;
                    }
                }
            }
            return maxNode;
        }

        private final static int compare(final ArrayList<Byte> b1, final ArrayList<Byte> b2) {
            byte bb1;
            byte bb2;
            int comp = 1;

            if (b1.size() > b2.size()) {
                comp = -1;
            } else if (b1.size() == b2.size()) {
                for (int i = 0; i < b1.size(); ++i) {
                    bb1 = b1.get(i);

                    bb2 = b2.get(i);

                    if (bb1 > bb2) {
                        comp = 1;
                        break;
                    } else if (bb1 < bb2) {
                        comp = -1;
                        break;
                    }
                }
            }

            return comp;
        }

        public synchronized void swap(final Node node) {
            Node pa;

            if (node.parent == this.parent) {
                pa = this.parent.leftChild;
                this.parent.leftChild = this.parent.rightChild;
                this.parent.rightChild = pa;
            } else {
                if (this.parent.leftChild == this) {
                    pa = node.parent;

                    this.parent.setLeftChild(node);

                    if (pa.leftChild == node) {
                        pa.setLeftChild(this);
                    } else {
                        pa.setRightChild(this);
                    }

                } else {
                    pa = node.parent;

                    this.parent.setRightChild(node);

                    if (pa.leftChild == node) {
                        pa.setLeftChild(this);
                    } else {
                        pa.setRightChild(this);
                    }
                }
            }
            this.updateBitsList();
            node.updateBitsList();
        }

        @Override
        public String toString() {
            return "Symbol: " + this.symbol + ", weight: " + this.weight + ", " + this.bitsList.toString();
        }

        public final void updateWeight(final Map<Long, Set<Node>> blocks) {
            long localWeight = 0;

            Node node;

            if (this.isLeaf()) {

                localWeight = this.weight;

            } else {
                if (this.leftChild != null) {
                    localWeight = this.leftChild.weight;
                }
                if (this.rightChild != null) {
                    localWeight += this.rightChild.weight;
                }
            }
            if (this.parent != null) {
                //maintaining silbing property.
                if (localWeight > 1) {
                    node = Node.findMax(weight, blocks);
                    if (node != null && this != node && !this.isGrandparent(node)) {
                        node.swap(this);

                        node.parent.updateWeight(blocks);//update node's weight will cause the changing again!!
                        //node.updateWeight(blocks);
                    }
                }

                Node.updateBlocks(this, blocks, weight, localWeight);

                this.weight = localWeight;

                this.parent.updateWeight(blocks);
            } else {
                this.weight = localWeight;

                //Node.updateBlocks(this, blocks, localWeight, weight);
            }
        }

        private final static void updateBlocks(final Node node, final Map<Long, Set<Node>> blocks, final long oldWeight, final long newWeight) {
            Set<Node> set;
            set = blocks.get(oldWeight);

            if (set != null) {
                set.remove(node);
                if (set.size() <= 0) {
                    blocks.remove(oldWeight);
                }
            }

            set = blocks.get(newWeight);

            if (set == null) {
                set = new HashSet<Node>();
                blocks.put(newWeight, set);
            }
            set.add(node);
        }

        public boolean isLeaf() {
            return (getLeftChild() == null) && (getRightChild() == null);
        }

        public Node getChild(final byte bit) {
            Node node;

            if (bit == LEFT_BIT) {
                node = this.getLeftChild();
            } else {
                node = this.getRightChild();
            }

            return node;
        }
        @Override
        public int compareTo(Object arg) {
            Node other = (Node) arg;

            // Content weight has priority and then the lowest letter
            if (this.getWeight() == other.getWeight()) {
                return this.getSymbol() - other.getSymbol();
            } else {
                return (int) (this.getWeight() - other.getWeight());
            }
        }

        public static void printTree(Node tree, final PrintStream printStream) {
            tree.printNode("", printStream);
        }

        ////////////////
        private void printNode(String path, final PrintStream printStream) {
            if (this.isLeaf()) {
                printStream.println(this);
            }

            if (getLeftChild() != null) {
                getLeftChild().printNode(path + LEFT_BIT, printStream);
            }
            if (getRightChild() != null) {
                getRightChild().printNode(path + RIGHT_BIT, printStream);
            }
        }

        public synchronized void updateBitsList() {
            if (this.parent != null) {
                if (parent.leftChild == this) {
                    this.setBitsList(parent.bitsList, LEFT_BIT);
                } else {
                    this.setBitsList(parent.bitsList, RIGHT_BIT);
                }

            }
            if (getLeftChild() != null) {
                this.leftChild.updateBitsList();
            }
            if (getRightChild() != null) {
                this.rightChild.updateBitsList();
            }
        }

        public Map<Short, Node> getHuffmanTable(final Map<Short, Node> map) {
            //final ArrayList<Byte> currentBitsList = new ArrayList<Byte>(symbolBitsList);

            if ((getLeftChild() == null) && (getRightChild() == null)) {
                map.put(getSymbol(), this);
                //System.out.println(symbol + " " + path);
            } else {

                if (getLeftChild() != null) {
                    getLeftChild().getHuffmanTable(map);

                }
                if (getRightChild() != null) {
                    getRightChild().getHuffmanTable(map);
                    //rightChild.printNode(path + '1');
                }
            }

            return map;
        }

        /**
         * @return the symbol
         */
        public short getSymbol() {
            return symbol;
        }

        /**
         * @return the leftSibling
         */
        public Node getLeftSibling() {
            return leftSibling;
        }

        /**
         * @param leftSibling the leftSibling to set
         */
        public void setLeftSibling(Node leftSibling) {
            this.leftSibling = leftSibling;

        }

        /**
         * @return the rightSibling
         */
        public Node getRightSibling() {
            return rightSibling;
        }

        /**
         * @param rightSibling the rightSibling to set
         */
        public void setRightSibling(Node rightSibling) {
            this.rightSibling = rightSibling;
        }

        /**
         * @return the weight
         */
        public long getWeight() {
            return weight;
        }

        /**
         * @return the NYT
         */
        public boolean isNYT() {
            return this.symbol == Node.NYT;
        }

        /**
         * @return the leftChild
         */
        public Node getLeftChild() {
            return leftChild;
        }

        public void setBitsList(final List<Byte> bitsList, final byte bit) {
            this.getBitsList().clear();
            this.getBitsList().addAll(bitsList);
            this.getBitsList().add(bit);
        }

        /**
         * @param leftChild the leftChild to set
         */
        public void setLeftChild(Node leftChild) {
            if (leftChild != null) {
                this.leftChild = leftChild;
                //this.leftChild.setBitsList(getBitsList(), LEFT_BIT);
                this.leftChild.parent = this;
                this.updateSibling();
            }

        }

        private final void updateSibling() {
            if (this.rightChild != null && this.leftChild != null) {
                this.leftChild.setRightSibling(this.rightChild);
                this.rightChild.setLeftSibling(this.leftChild);
            }
        }

        /**
         * @return the rightChild
         */
        public Node getRightChild() {
            return rightChild;
        }

        /**
         * @param rightChild the rightChild to set
         */
        public void setRightChild(Node rightChild) {

            if (rightChild != null) {
                this.rightChild = rightChild;
                //this.rightChild.setBitsList(getBitsList(), RIGHT_BIT);
                this.rightChild.parent = this;
                this.updateSibling();
            }
        }

        /**
         * @return the bitsList
         */
        public ArrayList<Byte> getBitsList() {
            return bitsList;
        }
    }
}
