/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.edu.ncu.csie.compression.util;

import idv.jiahan.io.BitBuffer;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 *
 * @author Jia-Han Su
 */
public class CompressionUtilities {

    public final static byte writeBit(final byte bit, final BitBuffer bitBuffer, final ByteBuffer byteBuffer, final FileChannel fileChannel, final boolean flush) throws IOException {
        byte writeCount = 0;

        if (bitBuffer.isFull()) {
            writeCount += CompressionUtilities.flush(bitBuffer, byteBuffer, fileChannel, flush);
        }

        bitBuffer.putBit(bit);

        if (bitBuffer.isFull() || flush) {
            writeCount += CompressionUtilities.flush(bitBuffer, byteBuffer, fileChannel, flush);
        }

        return writeCount;
    }

    public final static byte writeByte(final short bits, final BitBuffer bitBuffer, final ByteBuffer byteBuffer, final FileChannel fileChannel, final boolean flush) throws IOException {
        byte writeCount = 0;
        byte bit;

        for (int i = 7; i >= 0; --i) {
            bit = (byte) (bits >> i);
            bit &= BitBuffer.BIT_MASK;
            writeCount += CompressionUtilities.writeBit(bit, bitBuffer, byteBuffer, fileChannel, flush);
        }


        return writeCount;
    }

    public final static byte flush(final BitBuffer bitBuffer, final ByteBuffer byteBuffer, final FileChannel fileChannel, boolean flushToFile) throws IOException {

        byte writeCount = 0;

        writeCount = bitBuffer.getSize();
        //byteBuffer.clear();
        if (byteBuffer.remaining() < 8) {
            byteBuffer.limit(byteBuffer.capacity());
            byteBuffer.rewind();
            do {
                fileChannel.write(byteBuffer);
            } while (byteBuffer.remaining() > 0);
            byteBuffer.clear();
        }
        // System.out.println(bitBuffer);
        bitBuffer.flush(byteBuffer);
        //System.out.println(Integer.toBinaryString(byteBuffer.get(0)));
        //System.out.println(Integer.toBinaryString(byteBuffer.get(1)));
        //System.out.println(Integer.toBinaryString(byteBuffer.get(2)));
        //System.out.println(Integer.toBinaryString(byteBuffer.get(3)));
        //byteBuffer.putLong(bitBuffer.flush());
        // byteBuffer.limit(byteBuffer.position());
        // byteBuffer.rewind();
        if (flushToFile) {
            byteBuffer.limit(byteBuffer.position());
            byteBuffer.rewind();
            do {
                fileChannel.write(byteBuffer);
            } while (byteBuffer.remaining() > 0);
        }


        return writeCount;
    }
}
