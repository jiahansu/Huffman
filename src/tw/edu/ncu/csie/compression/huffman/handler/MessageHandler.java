/*
 * OutputHandler.java
 *
 * Created on 2007年10月22日, 下午 7:12
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package tw.edu.ncu.csie.compression.huffman.handler;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
/**
 *
 * @author 蘇家漢
 */
public class MessageHandler extends Handler{
    //private long lines = 0l;

    private JTextArea io = null;
    //private long lines = 0l;
    
    public MessageHandler(final JTextArea textArea){
        this.io = textArea;
    }

    @Override
    public void publish(LogRecord record) {
        final StringBuilder sb = new StringBuilder();
        final Throwable t = record.getThrown();
        //final InputOutput io = IOProvider.getDefault().getIO(messageSource.getMessage("LOG",null,userLocale), false);
        final Object localIO = io;
        synchronized(localIO){

            
            sb.append(record.getMessage());
            if(t!=null){
                sb.append(":"+"\n"+t.getLocalizedMessage());
            }
            
            if(record.getLevel().intValue()<Level.INFO.intValue()){

                io.append(sb.toString()+"\n");
            } else if(record.getLevel().intValue()< Level.WARNING.intValue()){
                io.append(sb.toString()+"\n");
                /*
                SwingUtilities.invokeLater(new Runnable(){
                    @Override
                    public final void run(){
                        JOptionPane.showMessageDialog(null,sb,messageSource.getMessage("INFO",null,userLocale),JOptionPane.INFORMATION_MESSAGE);
                    }
                });*/
            }else if(record.getLevel().intValue()<Level.SEVERE.intValue()) {
                io.append(sb.toString()+"\n");

                SwingUtilities.invokeLater(new Runnable(){
                    @Override
                    public final void run(){
                        JOptionPane.showMessageDialog(io,sb,java.util.ResourceBundle.getBundle("tw/edu/ncu/csie/compression/huffman/message").getString("WARNING"),JOptionPane.WARNING_MESSAGE);
                    }
                });
            }else{
                io.append(sb.toString()+"\n");
            }
           this.io.notify();
            //this.io.closeInputOutput();
        }
        
    }
     @Override
    public void flush() {
        final Object localIO = io;
        synchronized(localIO){
            this.io.notify();
        }
    }
     @Override
    public void close() throws SecurityException {
        final Object localIO = io;
        synchronized(localIO){
            this.io.notify();
        }
    }
  
    
}
