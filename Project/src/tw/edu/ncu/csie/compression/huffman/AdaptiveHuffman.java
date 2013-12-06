/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.edu.ncu.csie.compression.huffman;

import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import tw.edu.ncu.csie.compression.huffman.HuffmanTreeBuilder.Node;
import idv.jiahan.io.BitBuffer;
import tw.edu.ncu.csie.compression.util.CompressionUtilities;

/**
 *
 * @author Jia-Han Su
 */
public class AdaptiveHuffman implements DataCompressor {

    @Override
    public Raster decode(final FileHeader fileHeader, FileChannel fileChannel, ColorModel colorModel, final JProgressBar progressBar) throws IOException {
        // final int frequencies[] = new int[MUM_ELEMENTS];

        final BitBuffer bitsBuffer = new BitBuffer();
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(16384);
        int i, j, k;
        byte bit;
        final Node root;
        short value;
        Node node;
        final WritableRaster raster;
        final HashMap<Short, Node> huffmanTable = new HashMap<Short, Node>();
        final HashMap<Long, Set<Node>> blocks = new HashMap<Long, Set<Node>>();
        final int imageSize = fileHeader.getImageWidth() * fileHeader.getImageHeight();
        final AtomicInteger counter = new AtomicInteger(0);
        final long fileSize = fileChannel.size();
        long filePosition = fileChannel.position();
        final Runnable updater = new Runnable() {

            @Override
            public final void run() {
                if (progressBar != null) {
                    progressBar.setValue(counter.intValue());
                }
            }
        };


        if (progressBar != null) {
            progressBar.setMaximum(imageSize);
            progressBar.setMinimum(0);
        }
        //final DataBufferByte dataBuffer;
        byteBuffer.order(ByteOrder.nativeOrder());



        raster = colorModel.createCompatibleWritableRaster(fileHeader.getImageWidth(), fileHeader.getImageHeight());

        root = HuffmanTreeBuilder.getInitialNativeTree(huffmanTable, blocks);

        // dataBuffer = (DataBufferByte)raster.getDataBuffer();
        // Node.printTree(root,System.out);// System.out.println(root);
        byteBuffer.clear();
        byteBuffer.position(byteBuffer.capacity());


        for (i = 0; i < fileHeader.getImageHeight(); ++i) {
            for (j = 0; j < fileHeader.getImageWidth(); ++j) {
                node = root;
                do {
                    if (byteBuffer.remaining() <= 0) {
                        byteBuffer.clear();
                        fileChannel.read(byteBuffer);
                        filePosition += byteBuffer.position();
                        byteBuffer.limit(byteBuffer.position());
                        byteBuffer.rewind();
                    }
                    if (bitsBuffer.getSize() <= 0) {
                        bitsBuffer.loadBits(byteBuffer);
                    }

                    if (i == 0 && j == 0) {
                        node = huffmanTable.get(Node.NYT);
                    } else {
                        bit = bitsBuffer.removeFirst();
                        node = node.getChild(bit);
                    }


                    if (node.isLeaf()) {
                        if (node.getSymbol() == Node.NYT) {
                            value = 0;
                            for (k = 0; k < 8; ++k) {
                                if (byteBuffer.remaining() <= 0) {
                                    byteBuffer.clear();
                                    fileChannel.read(byteBuffer);
                                    byteBuffer.limit(byteBuffer.position());
                                    byteBuffer.rewind();
                                }
                                if (bitsBuffer.getSize() <= 0) {
                                    bitsBuffer.loadBits(byteBuffer);
                                }


                                bit = bitsBuffer.removeFirst();
                                value <<= 1;
                                value += bit;
                            }
                        } else {
                            value = node.getSymbol();
                        }
                        //System.out.println(root.getSymbol());
                        //dataBuffer.setElem(i*fileHeader.getImageWidth()+j, root.getSymbol());
                        raster.setSample(j, i, 0, value);
                        HuffmanTreeBuilder.adjustTree(root, huffmanTable, blocks, (short) value);
                        //dataBuffer.setElem(i, root.getSymbol());
                        break;
                    }
                } while (filePosition < fileSize || byteBuffer.remaining() > 0 || bitsBuffer.getSize()>0);

                counter.incrementAndGet();
            }
            SwingUtilities.invokeLater(updater);
        }

        //raster = Raster.createPackedRaster(dataBuffer, fileHeader.getImageWidth(), fileHeader.getImageHeight(), 8, new Point(0, 0));


        fileChannel.close();



        return raster;
    }
    /*
    public final static void main(final String args[]) throws IOException {
        final AdaptiveHuffman staticHuffman = new AdaptiveHuffman();
        final FileCacheSeekableStream stream = new FileCacheSeekableStream(new FileInputStream("test/data/baboon.pgm"));
        final RenderedOp renderOP = PNMDescriptor.create(stream, new RenderingHints(null));
        final FileHeader fileHeader = new FileHeader();
        final FileChannel fileChannel;

        final FileOutputStream fileOutputStream = new FileOutputStream("test.huf");



        staticHuffman.encode(renderOP.getData(), fileOutputStream.getChannel(), null);

        final FileInputStream fileInputStream = new FileInputStream("test.huf");
        //System.out.println(huffmanTable);
        //System.out.println(renderOP.getHeight());
        // System.out.println(Arrays.toString(frequencies));
        fileChannel = fileInputStream.getChannel();
        fileHeader.read(fileChannel);
        final Raster raster = staticHuffman.decode(fileHeader, fileChannel, GRAY_PNM_COLOR_MODEL, null);

        final ImageEncoder imageEncoder = ImageCodec.createImageEncoder("PNM", new BufferedOutputStream(new FileOutputStream("test.pgm")),
                IMAGE_ENCODE_PARAM);


        imageEncoder.encode(raster, GRAY_PNM_COLOR_MODEL);

    }*/

    @Override
    public double encode(final Raster data, final FileChannel fileChannel, final JProgressBar progressBar) throws IOException {
        final FileHeader fileHeader = new FileHeader();
        final BitBuffer bitsBuffer = new BitBuffer();
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(16384);
        final HashMap<Short, Node> huffmanTable = new HashMap<Short, Node>();
        int i, j, k;
        int value;
        byte bit;
        ArrayList<Byte> bitsList;
        final HashMap<Long, Set<Node>> blocks = new HashMap<Long, Set<Node>>();
        double bitsCount = 0;

        Node root = HuffmanTreeBuilder.getInitialNativeTree(huffmanTable, blocks);
        Node node;
        final int imageSize = data.getWidth() * data.getHeight();
         final AtomicInteger counter = new AtomicInteger(0);
        final Runnable updater = new Runnable() {

            @Override
            public final void run() {
                if (progressBar != null) {
                    progressBar.setValue(counter.intValue());
                }
            }
        };

        if (progressBar != null) {
            progressBar.setMaximum(imageSize);
            progressBar.setMinimum(0);
        }
        /*
        HuffmanTreeBuilder.adjustTree(root, huffmanTable, blocks, (short)'a');

        Node.printTree(root, System.out);

        HuffmanTreeBuilder.adjustTree(root, huffmanTable, blocks, (short)'a');

        Node.printTree(root, System.out);

        HuffmanTreeBuilder.adjustTree(root, huffmanTable, blocks, (short)'r');

        Node.printTree(root, System.out);

        HuffmanTreeBuilder.adjustTree(root, huffmanTable, blocks, (short)'d');

        Node.printTree(root, System.out);

        HuffmanTreeBuilder.adjustTree(root, huffmanTable, blocks, (short)'v');

        Node.printTree(root, System.out);*/

        byteBuffer.order(ByteOrder.nativeOrder());
        byteBuffer.clear();

        fileHeader.setVersion(FileHeader.VERSION);
        fileHeader.setMode(FileHeader.ADAPTIVE_MODE);
        fileHeader.setImageWidth(data.getWidth());
        fileHeader.setImageHeight(data.getHeight());

        fileHeader.write(fileChannel);

        for (i = 0; i < data.getHeight(); ++i) {
            for (j = 0; j < data.getWidth(); ++j) {
                value = data.getSample(j, i, 0);
                node = huffmanTable.get((short) value);

                // System.out.println(value);
                if (node == null) {
                    node = huffmanTable.get(Node.NYT);
                    //if(node!=null){
                    bitsList = node.getBitsList();
                    bitsCount += bitsList.size() + 8;
                    for (k = 0; k < bitsList.size(); ++k) {
                        bit = bitsList.get(k);
                        CompressionUtilities.writeBit(bit, bitsBuffer, byteBuffer, fileChannel, false);

                    }
                    //  }
                    CompressionUtilities.writeByte((short) value, bitsBuffer, byteBuffer, fileChannel, false);
                } else {
                    /*
                    if (value == 100 && node.getWeight() >= 1) {
                    Node.printTree(root, System.out);
                    }*/
                    bitsList = node.getBitsList();
                    bitsCount += bitsList.size();
                    for (k = 0; k < bitsList.size(); ++k) {
                        bit = bitsList.get(k);
                        CompressionUtilities.writeBit(bit, bitsBuffer, byteBuffer, fileChannel, false);
                        //System.out.println(bitsBuffer);
                    }
                }

                //adjust tree....
                HuffmanTreeBuilder.adjustTree(root, huffmanTable, blocks, (short) value);
                counter.incrementAndGet();
            }
            SwingUtilities.invokeLater(updater);
        }
        // System.out.println(bitsBuffer);
        // System.out.println(fileChannel.position());
        //Node.printTree(root, System.out);
        CompressionUtilities.flush(bitsBuffer, byteBuffer, fileChannel,true);
       
        //System.out.println(fileChannel.position());
        fileChannel.close();

        return bitsCount / imageSize;
    }
}
