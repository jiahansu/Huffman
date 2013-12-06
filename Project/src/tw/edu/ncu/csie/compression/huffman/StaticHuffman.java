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
public class StaticHuffman implements DataCompressor {
    /*
    public final static void main(final String args[]) throws IOException {
        final StaticHuffman staticHuffman = new StaticHuffman();
        final FileCacheSeekableStream stream = new FileCacheSeekableStream(new FileInputStream("test/data/lena.pgm"));
        final RenderedOp renderOP = PNMDescriptor.create(stream, new RenderingHints(null));
        final FileHeader fileHeader = new FileHeader();
        final FileChannel fileChannel;


        final FileOutputStream fileOutputStream = new FileOutputStream("test.huf");



        staticHuffman.encode(renderOP.getData(), fileOutputStream.getChannel(),null);

        final FileInputStream fileInputStream = new FileInputStream("test.huf");
        //System.out.println(huffmanTable);
        //System.out.println(renderOP.getHeight());
        // System.out.println(Arrays.toString(frequencies));

        fileChannel = fileInputStream.getChannel();
        fileHeader.read(fileChannel);
        final Raster raster = staticHuffman.decode(fileHeader, fileChannel, GRAY_PNM_COLOR_MODEL,null);

        final ImageEncoder imageEncoder = ImageCodec.createImageEncoder("PNM", new BufferedOutputStream(new FileOutputStream("test.pgm")),
                IMAGE_ENCODE_PARAM);


        imageEncoder.encode(raster, GRAY_PNM_COLOR_MODEL);

    }*/

    @Override
    public Raster decode(final FileHeader fileHeader, final FileChannel fileChannel, final ColorModel colorModel, final JProgressBar progressBar) throws IOException {
        final int frequencies[] = new int[MUM_ELEMENTS];
        //final FileHeader fileHeader = new FileHeader();
        final BitBuffer bitsBuffer = new BitBuffer();
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(16384);
        int i, j;
        byte bit;
        final Node root;
        Node node;
        final WritableRaster raster;
         final int imageSize = fileHeader.getImageWidth()*fileHeader.getImageHeight();
       
        final long fileSize = fileChannel.size();
        long filePosition = fileChannel.position();
         final AtomicInteger counter = new AtomicInteger(0);
        final Runnable updater = new Runnable() {

            @Override
            public final void run() {
                if (progressBar != null) {
                    progressBar.setValue(counter.intValue());
                }
            }
        };


         if(progressBar!=null){
            progressBar.setMaximum(imageSize);
            progressBar.setMinimum(0);
         }
        //final DataBufferByte dataBuffer;
        byteBuffer.order(ByteOrder.nativeOrder());

        //  fileHeader.read(fileChannel);
        //int imageSize = fileHeader.getImageHeight();
        raster = colorModel.createCompatibleWritableRaster(fileHeader.getImageWidth(), fileHeader.getImageHeight());


        fileHeader.getFrequencies(frequencies);

        root = HuffmanTreeBuilder.buildHuffmanTree(frequencies);

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
                        filePosition+=byteBuffer.position();
                        byteBuffer.limit(byteBuffer.position());
                        byteBuffer.rewind();
                    }
                    if (bitsBuffer.getSize() <= 0) {
                        bitsBuffer.loadBits(byteBuffer);
                    }


                    bit = bitsBuffer.removeFirst();
                    node = node.getChild(bit);

                    if (node.isLeaf()) {
                        //System.out.println(node.getSymbol());
                        //dataBuffer.setElem(i*fileHeader.getImageWidth()+j, node.getSymbol());
                        raster.setSample(j, i, 0, node.getSymbol());
                        //dataBuffer.setElem(i, node.getSymbol());
                        break;
                    }
                } while (filePosition < fileSize || byteBuffer.remaining() > 0 || bitsBuffer.getSize() > 0);
            }
            SwingUtilities.invokeLater(updater);
        }

        //raster = Raster.createPackedRaster(dataBuffer, fileHeader.getImageWidth(), fileHeader.getImageHeight(), 8, new Point(0, 0));


        fileChannel.close();



        return raster;
    }

    @Override
    public double encode(final Raster data, final FileChannel fileChannel, final JProgressBar progressBar) throws IOException {
        final FileHeader fileHeader = new FileHeader();
        final BitBuffer bitsBuffer = new BitBuffer();
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(16384);
        final int frequencies[] = this.computeFrequencies(data);
        final HashMap<Short, Node> huffmanTable = new HashMap<Short, Node>();
        int i, j, k;
        int value;
        byte bit;
        ArrayList<Byte> bitsList;
        Node root = HuffmanTreeBuilder.buildHuffmanTree(frequencies);
        Node node;
        final int imageSize = data.getHeight()*data.getWidth();
        final AtomicInteger counter = new AtomicInteger(0);
        final Runnable updater = new Runnable() {

            @Override
            public final void run() {
                if (progressBar != null) {
                    progressBar.setValue(counter.intValue());
                }
            }
        };
       double bitsCount=0;

         if(progressBar!=null){
            progressBar.setMaximum(imageSize);
            progressBar.setMinimum(0);
         }
        //Node.printTree(root, System.out);
        root.getHuffmanTable(huffmanTable);

        byteBuffer.order(ByteOrder.nativeOrder());
        byteBuffer.clear();

        fileHeader.setVersion(FileHeader.VERSION);
        fileHeader.setMode(FileHeader.STATIC_MODE);
        fileHeader.setImageWidth(data.getWidth());
        fileHeader.setImageHeight(data.getHeight());
        fileHeader.setFrequencies(frequencies);

        fileHeader.write(fileChannel);

        for (i = 0; i < data.getHeight(); ++i) {
            for (j = 0; j < data.getWidth(); ++j) {
                value = data.getSample(j, i, 0);
                node = huffmanTable.get((short) value);
                //System.out.println(value);

                bitsList = node.getBitsList();
                bitsCount+=bitsList.size();
                for (k = 0; k < bitsList.size(); ++k) {
                    bit = bitsList.get(k);
                    CompressionUtilities.writeBit(bit, bitsBuffer, byteBuffer, fileChannel, false);
                    //System.out.println(bitsBuffer);
                }
            }
            SwingUtilities.invokeLater(updater);
        }
        // System.out.println(bitsBuffer);
        // System.out.println(fileChannel.position());
        CompressionUtilities.flush(bitsBuffer, byteBuffer, fileChannel,true);
        //System.out.println(fileChannel.position());
        fileChannel.close();

        return bitsCount/imageSize;
    }

    public int[] computeFrequencies(final Raster raster) {
        final int frequencies[] = new int[MUM_ELEMENTS];
        int value;
        int i, j;

        for (i = 0; i < frequencies.length; ++i) {
            frequencies[i] = 0;
        }

        for (i = 0; i < raster.getHeight(); ++i) {
            for (j = 0; j < raster.getWidth(); ++j) {
                value = raster.getSample(j, i, 0);
                //System.out.println(value);
                ++frequencies[value];
            }
        }

        //System.out.println(Arrays.toString(frequencies));
        return frequencies;
    }
}
