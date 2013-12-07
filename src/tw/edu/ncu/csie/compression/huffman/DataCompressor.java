/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tw.edu.ncu.csie.compression.huffman;

import com.sun.media.jai.codec.PNMEncodeParam;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.io.IOException;
import java.nio.channels.FileChannel;
import javax.swing.JProgressBar;

/**
 *
 * @author Jia-Han Su
 */
public interface DataCompressor {

   
     public final static ColorModel GRAY_PNM_COLOR_MODEL =
             new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), new int[]{8}, false, true,
                Transparency.OPAQUE,
                DataBuffer.TYPE_BYTE);

    PNMEncodeParam IMAGE_ENCODE_PARAM = new PNMEncodeParam();
    int MUM_ELEMENTS = 256;

    Raster decode(final FileHeader fileHeader,final FileChannel fileChannel, final ColorModel colorModel, final JProgressBar progressBar) throws IOException;

    public double encode(final Raster data, final FileChannel fileChannel, final JProgressBar progressBar) throws IOException;

}
