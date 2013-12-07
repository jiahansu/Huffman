/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.edu.ncu.csie.compression.huffman;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/**
 *
 * @author Jia-Han Su
 */
public class FileHeader {
     public final static byte VERSION = 0;

    public final static byte STATIC_MODE =0;
    public final static byte ADAPTIVE_MODE =1;
    public final static int VERSION_POSITION = 0;
    public final static int MODE_POSITION = VERSION_POSITION + 1;
    public final static int IMAGE_WIDTH_POSITION = MODE_POSITION + 2;
    public final static int IMAGE_HEIGHT_POSITION = IMAGE_WIDTH_POSITION + 4;
    public final static int FREQUENCIES_TABLE_POSITION = IMAGE_HEIGHT_POSITION + 4;
    private final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(FREQUENCIES_TABLE_POSITION + 256 * 4);


    public boolean isValidHuffmanFile(){
        boolean b = true;

        b &=this.getVersion()==VERSION ;
         b &=this.getMode()==STATIC_MODE || this.getMode()==ADAPTIVE_MODE ;
         //System.out.println(this.getImageWidth());
         b &=this.getImageWidth()>0 ;
         b &=this.getImageHeight()>0 ;
         
        return b;
    }

    public FileHeader() {
        byteBuffer.order(ByteOrder.nativeOrder());
    }

    public byte getVersion() {
        return this.byteBuffer.get(VERSION_POSITION);
    }

    public byte getMode() {
        return this.byteBuffer.get(MODE_POSITION);
    }

    public int getImageWidth() {
        return this.byteBuffer.getInt(IMAGE_WIDTH_POSITION);
    }

    public int getImageHeight() {
        return this.byteBuffer.getInt(IMAGE_HEIGHT_POSITION);
    }

    public int[] getFrequencies(final int[] frequencies) {
        for (int i = 0; i < frequencies.length; ++i) {
            frequencies[i] = this.byteBuffer.getInt(FREQUENCIES_TABLE_POSITION + (i*4) );
        }

        return frequencies;
    }

    public void setVersion(final byte version) {
        this.byteBuffer.put(VERSION_POSITION, version);
    }

    public void setMode(final byte version) {
        this.byteBuffer.put(MODE_POSITION, version);
    }

    public void setImageWidth(final int version) {
        this.byteBuffer.putInt(IMAGE_WIDTH_POSITION, version);
    }

    public void setImageHeight(final int version) {
        this.byteBuffer.putInt(IMAGE_HEIGHT_POSITION, version);
    }

    public void setFrequencies(final int[] frequencies) {
        for (int i = 0; i < frequencies.length; ++i) {
            this.byteBuffer.putInt(FREQUENCIES_TABLE_POSITION + (i*4), frequencies[i]);
        }
    }

    public void read(final FileChannel fc) throws IOException{
        this.byteBuffer.clear();
         this.byteBuffer.limit(IMAGE_HEIGHT_POSITION+4);
         fc.read(byteBuffer);
         if(this.getMode()==FileHeader.STATIC_MODE){
           this.byteBuffer.limit(this.byteBuffer.capacity());
           fc.read(byteBuffer);
        }
       // System.out.println(fc.position());
    }

  public void write(final FileChannel fc) throws IOException{
        this.byteBuffer.clear();

        if(this.getMode()==FileHeader.ADAPTIVE_MODE){
            this.byteBuffer.limit(IMAGE_HEIGHT_POSITION+4);
        }
        do{
            fc.write(byteBuffer);
           
        }while(this.byteBuffer.remaining()>0);
    }
}
