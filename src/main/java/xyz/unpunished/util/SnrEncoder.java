package xyz.unpunished.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import xyz.unpunished.model.HexAction;
import xyz.unpunished.model.HexActionType;

@AllArgsConstructor
public class SnrEncoder {
    
    private final ByteOrder order = ByteOrder.LITTLE_ENDIAN;
    
    private String pathToWavSnr;
    
    public boolean isSnr(){
        return FilenameUtils.getExtension(pathToWavSnr).toLowerCase().equals("snr");
    }
    
    public String encodeSnr() throws InterruptedException, IOException {
        ProcessBuilder builder = new ProcessBuilder("snrtool.exe",
                        pathToWavSnr);
        Process pr = builder.start();
        pr.waitFor();
        pr.destroy();
        pathToWavSnr += ".snr";
        return pathToWavSnr;
    }
     
    public void loopSnr() throws IOException{
         if(isLooped())
             return;
         File snr = new File(pathToWavSnr);
         File temp = new File(pathToWavSnr + ".temp");
         InputStream is = new BufferedInputStream(new FileInputStream(snr));
         OutputStream os = new BufferedOutputStream(new FileOutputStream(pathToWavSnr + ".temp"));
         int size = (int) snr.length();
         HexAction[] actions = new HexAction[]{
             new HexAction(HexActionType.READ_AND_WRITE, HexEngine.toByteArray(4, order)),
             new HexAction(HexActionType.WRITE_AND_SKIP, new byte[]{32}),
             new HexAction(HexActionType.READ_AND_WRITE, HexEngine.toByteArray(3, order)),
             new HexAction(HexActionType.WRITE, new byte[] {0,0,0,0}),
             new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(size, order))
         };
         HexEngine engine = new HexEngine(is, os, order, actions);
         engine.execute();
         Files.move(temp.toPath(), snr.toPath(), StandardCopyOption.REPLACE_EXISTING);
     }
     
    private boolean isLooped() throws IOException {
        InputStream snrIn = new BufferedInputStream(new FileInputStream(pathToWavSnr));
        byte[] buf = new byte[5];
        snrIn.read(buf);
        snrIn.close();
        return buf[4] == 32;
    }
    
}
