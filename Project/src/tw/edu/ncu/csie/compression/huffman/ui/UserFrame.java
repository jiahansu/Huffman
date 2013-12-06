/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * UserFrame.java
 *
 * Created on 2009/10/30, 下午 04:20:09
 */
package tw.edu.ncu.csie.compression.huffman.ui;

import java.util.prefs.BackingStoreException;
import tw.edu.ncu.csie.compression.huffman.*;
import com.sun.media.jai.codec.FileCacheSeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageEncoder;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.PNMDescriptor;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import tw.edu.ncu.csie.compression.huffman.handler.MessageHandler;

/**
 *
 * @author Jia-Han Su
 */
public class UserFrame extends javax.swing.JFrame {

    protected final static Logger LOG = Logger.getLogger(UserFrame.class.getName());
    private final static Preferences fileFolderPreference = Preferences.userRoot().node("/tw/edu/ncu/csie/compression/huffman/UserFrame");
    public final static String INPUT_FILE_FOLDER = "INPUT_FILE_FOLDER";
    public final static String OUTPUT_FILE_FOLDER = "OUTPUT_FILE_FOLDER";
    private final FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("PNM image or huffman compression file", "huf", "pgm", "pnm", " pbm");
    private final ImageDialog imageDialog = new ImageDialog(this);
    private final Processor processor = new Processor();

    private static enum OPERATION {

        COMPRESSION, DECOMPRESSION, INVALID_FILE
    };
    private OPERATION operation;

    /** Creates new form UserFrame */
    public UserFrame() {
        initComponents();
        this.modeButtonGroup.add(this.staticToggleButton);
        this.modeButtonGroup.add(this.adaptiveToggleButton);
    }

    private final String changeSuffix(final String name, final String suffix) {
        final int lastIndex = name.lastIndexOf(".");
        final StringBuilder sb = new StringBuilder();

        if (lastIndex > 0) {
            sb.append(name.substring(0, lastIndex));

        } else {
            sb.append(name);
        }
        sb.append(".");
        sb.append(suffix);

        return sb.toString();
    }

    private final void setOperation(OPERATION operation, final String filePath) {
        this.operation = operation;

        switch (this.operation) {
            case COMPRESSION:
                this.staticToggleButton.setEnabled(true);
                this.adaptiveToggleButton.setEnabled(true);
                this.startButton.setEnabled(true);
                this.startButton.setText(java.util.ResourceBundle.getBundle("tw/edu/ncu/csie/compression/huffman/message").getString("COMPRESSION"));
                this.outputFileTextField.setText(this.changeSuffix(filePath, "huf"));
                break;

            case DECOMPRESSION:
                this.staticToggleButton.setEnabled(false);
                this.adaptiveToggleButton.setEnabled(false);
                this.startButton.setEnabled(true);
                this.startButton.setText(java.util.ResourceBundle.getBundle("tw/edu/ncu/csie/compression/huffman/message").getString("DECOMPRESSION"));
                this.outputFileTextField.setText(this.changeSuffix(filePath, "pgm"));
                break;

            case INVALID_FILE:
                this.staticToggleButton.setEnabled(false);
                this.adaptiveToggleButton.setEnabled(false);
                this.startButton.setEnabled(false);
                LOG.log(Level.WARNING, java.util.ResourceBundle.getBundle("tw/edu/ncu/csie/compression/huffman/message").getString("INVALID_FILE_FORMAT"), (Throwable) null);
                break;
        }
    }

    private final void setFile(final File file) {
        FileCacheSeekableStream stream = null;

        this.inputFileTextField.setText(file.getAbsolutePath());

        try {
            stream = new FileCacheSeekableStream(new FileInputStream(file));
            final RenderedOp renderOP = PNMDescriptor.create(stream, new RenderingHints(null));

            if (renderOP.getColorModel().getColorSpace().getType() == ColorSpace.TYPE_GRAY) {
                final BufferedImage image = renderOP.getAsBufferedImage();

                this.setOperation(OPERATION.COMPRESSION, file.getAbsolutePath());
                imageDialog.setImage(image);
                imageDialog.setSize(image.getWidth(), image.getHeight());
                imageDialog.setVisible(true);
            } else {
                this.setOperation(OPERATION.INVALID_FILE, file.getAbsolutePath());
            }
        } catch (Throwable ex) {

            FileInputStream fileInputStream = null;
            try {
                final FileHeader fileHeader = new FileHeader();

                fileInputStream = new FileInputStream(file);

                fileHeader.read(fileInputStream.getChannel());

                if (fileHeader.isValidHuffmanFile()) {
                    this.setOperation(OPERATION.DECOMPRESSION, file.getAbsolutePath());
                } else {
                    this.setOperation(OPERATION.INVALID_FILE, file.getAbsolutePath());
                }

            } catch (Exception ex1) {
                this.setOperation(OPERATION.INVALID_FILE, file.getAbsolutePath());
            } finally {
                try {
                    fileInputStream.close();
                } catch (IOException ex1) {
                    LOG.log(Level.WARNING, null, ex1);
                }
            }
        } finally {
            try {
                stream.close();
            } catch (IOException ex) {
                LOG.log(Level.WARNING, null, ex);
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        modeButtonGroup = new javax.swing.ButtonGroup();
        inputFileTextField = new javax.swing.JTextField();
        inputFileButton = new javax.swing.JButton();
        outputFileTextField = new javax.swing.JTextField();
        outputFileButton = new javax.swing.JButton();
        staticToggleButton = new javax.swing.JToggleButton();
        adaptiveToggleButton = new javax.swing.JToggleButton();
        startButton = new javax.swing.JButton();
        progressBar = new javax.swing.JProgressBar();
        scrollPane = new javax.swing.JScrollPane();
        messageTextArea = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("tw/edu/ncu/csie/compression/huffman/message"); // NOI18N
        setTitle(bundle.getString("HUFFMAN_PROGRAM")); // NOI18N

        inputFileTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputFileTextFieldActionPerformed(evt);
            }
        });

        inputFileButton.setText(bundle.getString("OPEN_INPUT_FILE")); // NOI18N
        inputFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputFileButtonActionPerformed(evt);
            }
        });

        outputFileButton.setText(bundle.getString("OPEN_OUTPUT_FILE")); // NOI18N
        outputFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputFileButtonActionPerformed(evt);
            }
        });

        staticToggleButton.setText("Static");
        staticToggleButton.setEnabled(false);

        adaptiveToggleButton.setSelected(true);
        adaptiveToggleButton.setText("Adaptive");
        adaptiveToggleButton.setEnabled(false);

        startButton.setText(bundle.getString("COMPRESSION")); // NOI18N
        startButton.setEnabled(false);
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        messageTextArea.setColumns(20);
        messageTextArea.setRows(5);
        scrollPane.setViewportView(messageTextArea);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(inputFileTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(inputFileButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(staticToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 147, Short.MAX_VALUE)
                                .addComponent(adaptiveToggleButton))
                            .addComponent(outputFileTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(startButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(outputFileButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
                    .addComponent(scrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 390, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(inputFileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(inputFileButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(outputFileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(outputFileButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(staticToggleButton)
                    .addComponent(startButton)
                    .addComponent(adaptiveToggleButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(scrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 173, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void inputFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputFileButtonActionPerformed
        try {
            // TODO add your handling code here:
            final File currentFolder = new File(fileFolderPreference.get(INPUT_FILE_FOLDER, System.getProperty("user.home")));
            final JFileChooser chooser = new JFileChooser(currentFolder);
            chooser.setMultiSelectionEnabled(false);
            chooser.setFileFilter(fileFilter);
            int returnVal = chooser.showOpenDialog(this);
            switch (returnVal) {
                case JFileChooser.APPROVE_OPTION:
                    this.setFile(chooser.getSelectedFile());
                    fileFolderPreference.put(INPUT_FILE_FOLDER, chooser.getCurrentDirectory().getAbsolutePath());
                    fileFolderPreference.flush();
                    break;
            }
        } catch (BackingStoreException ex) {
            LOG.log(Level.WARNING, null, ex);
        }
    }//GEN-LAST:event_inputFileButtonActionPerformed

    private void outputFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputFileButtonActionPerformed
        // TODO add your handling code here:
        try {
            // TODO add your handling code here:
            final File currentFolder = new File(fileFolderPreference.get(OUTPUT_FILE_FOLDER, System.getProperty("user.home")));
            final JFileChooser chooser = new JFileChooser(currentFolder);
            chooser.setMultiSelectionEnabled(false);
            chooser.setFileFilter(fileFilter);
            int returnVal = chooser.showOpenDialog(this);
            switch (returnVal) {
                case JFileChooser.APPROVE_OPTION:

                    this.outputFileTextField.setText(chooser.getSelectedFile().getAbsolutePath());
                    fileFolderPreference.put(OUTPUT_FILE_FOLDER, chooser.getCurrentDirectory().getAbsolutePath());
                    fileFolderPreference.flush();
                    break;
            }
        } catch (BackingStoreException ex) {
            LOG.log(Level.WARNING, null, ex);
        }
    }//GEN-LAST:event_outputFileButtonActionPerformed

    private void inputFileTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputFileTextFieldActionPerformed
        // TODO add your handling code here:
        this.setFile(new File(this.inputFileTextField.getText()));
    }//GEN-LAST:event_inputFileTextFieldActionPerformed

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
        // TODO add your handling code here:
        startButton.setEnabled(false);

        final Thread t = new Thread(processor,"Compression Processor");

        t.setPriority(Thread.MAX_PRIORITY);

        t.start();
    }//GEN-LAST:event_startButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {


        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException e) {
            LOG.log(Level.FINE, null, e);
        } catch (Exception e) {
            LOG.log(Level.WARNING, null, e);
        }

        final UserFrame userFrame = new UserFrame();
        Logger.getLogger("tw.edu.ncu.csie.compression.huffman").addHandler(new MessageHandler(userFrame.messageTextArea));

        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                userFrame.setSize(640, 480);
                userFrame.setVisible(true);
                userFrame.toFront();
            }
        });
    }

    private class Processor implements Runnable {

        @Override
        public final void run() {


            FileCacheSeekableStream stream = null;
            try {
                DataCompressor dataCompressor = null;
                double bitsCount = 0;
                double time = System.nanoTime();

                switch (operation) {
                    case COMPRESSION:
                        stream = new FileCacheSeekableStream(new FileInputStream(inputFileTextField.getText()));
                        final RenderedOp renderOP = PNMDescriptor.create(stream, new RenderingHints(null));
                        final FileOutputStream fileOutputStream = new FileOutputStream(outputFileTextField.getText());
                        if (staticToggleButton.isSelected()) {
                            dataCompressor = new StaticHuffman();
                        } else {
                            dataCompressor = new AdaptiveHuffman();
                        }
                        bitsCount = dataCompressor.encode(renderOP.getData(), fileOutputStream.getChannel(), progressBar);
                        break;
                    case DECOMPRESSION:
                        final FileHeader fileHeader = new FileHeader();
                        final FileChannel fileChannel;
                        final Raster raster;
                        final ImageEncoder imageEncoder;
                        final FileInputStream fileInputStream = new FileInputStream(inputFileTextField.getText());
                        final OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFileTextField.getText()));
                        //System.out.println(huffmanTable);
                        //System.out.println(renderOP.getHeight());
                        // System.out.println(Arrays.toString(frequencies));
                        fileChannel = fileInputStream.getChannel();
                        fileHeader.read(fileChannel);
                        if (fileHeader.isValidHuffmanFile()) {
                            switch (fileHeader.getMode()) {
                                case FileHeader.STATIC_MODE:
                                    dataCompressor = new StaticHuffman();
                                    break;
                                case FileHeader.ADAPTIVE_MODE:
                                    dataCompressor = new AdaptiveHuffman();
                                    break;
                                default:
                                    setOperation(OPERATION.INVALID_FILE, inputFileTextField.getText());
                                    break;
                            }
                        } else {
                            setOperation(OPERATION.INVALID_FILE, inputFileTextField.getText());
                        }
                        raster = dataCompressor.decode(fileHeader, fileChannel, DataCompressor.GRAY_PNM_COLOR_MODEL, progressBar);
                        imageEncoder = ImageCodec.createImageEncoder("PNM", outputStream, DataCompressor.IMAGE_ENCODE_PARAM);
                        imageEncoder.encode(raster, DataCompressor.GRAY_PNM_COLOR_MODEL);
                        outputStream.close();
                        break;
                    case INVALID_FILE:
                        break;
                }

                time = System.nanoTime() - time;
                time /= TimeUnit.SECONDS.toNanos(1);
                LOG.log(Level.INFO, java.util.ResourceBundle.getBundle("tw/edu/ncu/csie/compression/huffman/message").getString("SPEND_TIME") + time,
                        (Throwable) null);

                if (bitsCount > 0) {

                    LOG.log (Level.INFO,java.util.ResourceBundle.getBundle("tw/edu/ncu/csie/compression/huffman/message").getString("AVERAGE_BITS_COUNT") + bitsCount ,
                            (Throwable) null);
                }
            } catch (IOException ex) {
                LOG.log(Level.WARNING, null, ex);
            } finally {
                try {
                    if (stream != null) {
                        stream.close();
                    }


                } catch (IOException ex) {
                    LOG.log(Level.WARNING, null, ex);
                }
            }
            startButton.setEnabled(true);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton adaptiveToggleButton;
    private javax.swing.JButton inputFileButton;
    private javax.swing.JTextField inputFileTextField;
    private javax.swing.JTextArea messageTextArea;
    private javax.swing.ButtonGroup modeButtonGroup;
    private javax.swing.JButton outputFileButton;
    private javax.swing.JTextField outputFileTextField;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JButton startButton;
    private javax.swing.JToggleButton staticToggleButton;
    // End of variables declaration//GEN-END:variables
}
