package xyz.unpunished.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.nio.file.Paths;
import javafx.scene.control.Alert;
import lombok.Builder;
import org.apache.commons.io.FilenameUtils;
import xyz.unpunished.model.HexAction;
import xyz.unpunished.model.HexActionType;

@Builder
public class TmxEncoder {
    
    private final ByteOrder order = ByteOrder.LITTLE_ENDIAN;
    
    private String wavSnr, gin1, gin2, gin3;
    private String exportPath, exportName;
    private float minRPM, maxRPM;
    private boolean doExportPath, doExportName, encodeSnr, loopSnr;
    private boolean ai, writeMinRPM;
    
    public boolean encodeTmx() {
        File[] files = ai 
            ? new File[]{new File(gin1),
                new File(wavSnr),
                new File(gin3)}
            : new File[]{new File(wavSnr),
                new File(gin3),
                new File(gin2),
                new File(gin1)
            };
        File outFile;
        if(doExportPath)
            if(doExportName)
                outFile = Paths.get(Paths.get(exportPath).toString(), exportName).toFile();
            else
               outFile = new File(exportPath); 
        else
            if(doExportName)
                outFile = Paths.get(files[0].getParent(), exportName).toFile();
                
            else
                outFile = new File(FilenameUtils.removeExtension(wavSnr) + ".tmx");
        SnrEncoder encoder = new SnrEncoder(wavSnr);
        try{
            boolean wasSnr = encoder.isSnr();
            if(!wasSnr || encodeSnr){
                wavSnr = encoder.encodeSnr();
                if(!wasSnr || loopSnr)
                    encoder.loopSnr();
            }
        }
        catch(IOException | InterruptedException ex){
            ex.printStackTrace();
            AlertWorker.showAlert(
                    Alert.AlertType.ERROR,
                    "tmxtool",
                    "Error",
                    "File error: failed to encode/loop SNR");
            return false;
        }
        files[ai ? 1 : 0] = new File(wavSnr);
        int[] offsets = new int[files.length - 1];
        int offset = (int) files[0].length();
        for(int i = 0; i < offsets.length; i ++){
            offsets[i] = offset;
            offset += (int) files[i + 1].length();
        }
        OutputStream os;
        try{
            os = new BufferedOutputStream(new FileOutputStream(outFile));
        }
        catch(IOException ex){
            ex.printStackTrace();
            AlertWorker.showAlert(
                    Alert.AlertType.ERROR,
                    "tmxtool",
                    "Error",
                    "File error: failed to create an output file");
            return false;
        }
        InputStream base;
        byte[] maxRPMBytes = HexEngine.toByteArray(maxRPM, order);
        HexEngine engine;
        HexAction[] actions;
        if (ai){
            base = getClass().getResourceAsStream("/static/base/" + "ai.tmx");
            if(writeMinRPM){
                byte[] minRPMBytes = HexEngine.toByteArray(minRPM, order);
                byte[] idleRPMBytes = HexEngine.toByteArray(minRPM - (float) 200.0, order);
                actions = new HexAction[]{
                    new HexAction(HexActionType.READ_AND_WRITE, HexEngine.toByteArray(452, order)),
                    new HexAction(HexActionType.WRITE_AND_SKIP, minRPMBytes),
                    new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(484, order)),
                    new HexAction(HexActionType.WRITE_AND_SKIP, maxRPMBytes),
                    new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(544, order)),
                    new HexAction(HexActionType.WRITE_AND_SKIP, idleRPMBytes),
                    new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(576, order)),
                    new HexAction(HexActionType.WRITE_AND_SKIP, minRPMBytes),
                    new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(640, order)),
                    new HexAction(HexActionType.WRITE_AND_SKIP, minRPMBytes),
                    new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(672, order)),
                    new HexAction(HexActionType.WRITE_AND_SKIP, maxRPMBytes),
                    new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(688, order)),
                    new HexAction(HexActionType.WRITE_AND_SKIP, idleRPMBytes),
                    new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(692, order)),
                    new HexAction(HexActionType.WRITE_AND_SKIP, maxRPMBytes),
                    new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(936, order)),
                    new HexAction(HexActionType.WRITE_AND_SKIP, HexEngine.toByteArray(offsets[0], order)),
                    new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(972, order)),
                    new HexAction(HexActionType.WRITE_AND_SKIP, HexEngine.toByteArray(offsets[1], order)),
                    new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(1000, order)),
                    new HexAction(HexActionType.NEW_INPUT, HexEngine.toByteArray(files[0].getAbsolutePath())),
                    new HexAction(HexActionType.READ_AND_WRITE, HexEngine.toByteArray((int) files[0].length(), order)),
                    new HexAction(HexActionType.NEW_INPUT, HexEngine.toByteArray(files[1].getAbsolutePath())),
                    new HexAction(HexActionType.READ_AND_WRITE, HexEngine.toByteArray((int) files[1].length(), order)),
                    new HexAction(HexActionType.NEW_INPUT, HexEngine.toByteArray(files[2].getAbsolutePath())),
                    new HexAction(HexActionType.READ_AND_WRITE, HexEngine.toByteArray((int) files[2].length(), order)),
                };
            }
            else{
                actions = new HexAction[]{
                    new HexAction(HexActionType.READ_AND_WRITE, HexEngine.toByteArray(484, order)),
                    new HexAction(HexActionType.WRITE_AND_SKIP, maxRPMBytes),
                    new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(672, order)),
                    new HexAction(HexActionType.WRITE_AND_SKIP, maxRPMBytes),
                    new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(692, order)),
                    new HexAction(HexActionType.WRITE_AND_SKIP, maxRPMBytes),
                    new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(936, order)),
                    new HexAction(HexActionType.WRITE_AND_SKIP, HexEngine.toByteArray(offsets[0], order)),
                    new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(972, order)),
                    new HexAction(HexActionType.WRITE_AND_SKIP, HexEngine.toByteArray(offsets[1], order)),
                    new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(1000, order)),
                    new HexAction(HexActionType.NEW_INPUT, HexEngine.toByteArray(files[0].getAbsolutePath())),
                    new HexAction(HexActionType.READ_AND_WRITE, HexEngine.toByteArray((int) files[0].length(), order)),
                    new HexAction(HexActionType.NEW_INPUT, HexEngine.toByteArray(files[1].getAbsolutePath())),
                    new HexAction(HexActionType.READ_AND_WRITE, HexEngine.toByteArray((int) files[1].length(), order)),
                    new HexAction(HexActionType.NEW_INPUT, HexEngine.toByteArray(files[2].getAbsolutePath())),
                    new HexAction(HexActionType.READ_AND_WRITE, HexEngine.toByteArray((int) files[2].length(), order)),
                };
            }
        }
        else{
            base = getClass().getResourceAsStream("/static/base/" + "player.tmx");
            if(writeMinRPM){
                byte[] minRPMBytes = HexEngine.toByteArray(minRPM, order);
                byte[] idleRPMBytes = HexEngine.toByteArray(minRPM - (float) 200.0, order);
                actions = new HexAction[]{
                    new HexAction(HexActionType.READ_AND_WRITE, HexEngine.toByteArray(420, order)),
                    new HexAction(HexActionType.WRITE_AND_SKIP, minRPMBytes),
                    new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(452, order)),
                    new HexAction(HexActionType.WRITE_AND_SKIP, maxRPMBytes),
                    new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(512, order)),
                    new HexAction(HexActionType.WRITE_AND_SKIP, idleRPMBytes),
                    new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(544, order)),
                    new HexAction(HexActionType.WRITE_AND_SKIP, minRPMBytes),
                    new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(608, order)),
                    new HexAction(HexActionType.WRITE_AND_SKIP, minRPMBytes),
                    new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(640, order)),
                    new HexAction(HexActionType.WRITE_AND_SKIP, maxRPMBytes),
                    new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(708, order)),
                    new HexAction(HexActionType.WRITE_AND_SKIP, minRPMBytes),
                    new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(740, order)),
                    new HexAction(HexActionType.WRITE_AND_SKIP, maxRPMBytes),
                    new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(756, order)),
                    new HexAction(HexActionType.WRITE_AND_SKIP, idleRPMBytes),
                    new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(760, order)),
                    new HexAction(HexActionType.WRITE_AND_SKIP, maxRPMBytes),
                    new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(924, order)),
                    new HexAction(HexActionType.WRITE_AND_SKIP, HexEngine.toByteArray(offsets[0], order)),
                    new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(960, order)),
                    new HexAction(HexActionType.WRITE_AND_SKIP, HexEngine.toByteArray(offsets[1], order)),
                    new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(996, order)),
                    new HexAction(HexActionType.WRITE_AND_SKIP, HexEngine.toByteArray(offsets[2], order)),
                    new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(1024, order)),
                    new HexAction(HexActionType.NEW_INPUT, HexEngine.toByteArray(files[0].getAbsolutePath())),
                    new HexAction(HexActionType.READ_AND_WRITE, HexEngine.toByteArray((int) files[0].length(), order)),
                    new HexAction(HexActionType.NEW_INPUT, HexEngine.toByteArray(files[1].getAbsolutePath())),
                    new HexAction(HexActionType.READ_AND_WRITE, HexEngine.toByteArray((int) files[1].length(), order)),
                    new HexAction(HexActionType.NEW_INPUT, HexEngine.toByteArray(files[2].getAbsolutePath())),
                    new HexAction(HexActionType.READ_AND_WRITE, HexEngine.toByteArray((int) files[2].length(), order)),
                    new HexAction(HexActionType.NEW_INPUT, HexEngine.toByteArray(files[3].getAbsolutePath())),
                    new HexAction(HexActionType.READ_AND_WRITE, HexEngine.toByteArray((int) files[3].length(), order)),
                };
            }
            else{
                actions = new HexAction[]{
                    new HexAction(HexActionType.READ_AND_WRITE, HexEngine.toByteArray(452, order)),
                    new HexAction(HexActionType.WRITE_AND_SKIP, maxRPMBytes),
                    new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(640, order)),
                    new HexAction(HexActionType.WRITE_AND_SKIP, maxRPMBytes),
                    new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(740, order)),
                    new HexAction(HexActionType.WRITE_AND_SKIP, maxRPMBytes),
                    new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(760, order)),
                    new HexAction(HexActionType.WRITE_AND_SKIP, maxRPMBytes),
                    new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(924, order)),
                    new HexAction(HexActionType.WRITE_AND_SKIP, HexEngine.toByteArray(offsets[0], order)),
                    new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(960, order)),
                    new HexAction(HexActionType.WRITE_AND_SKIP, HexEngine.toByteArray(offsets[1], order)),
                    new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(996, order)),
                    new HexAction(HexActionType.WRITE_AND_SKIP, HexEngine.toByteArray(offsets[2], order)),
                    new HexAction(HexActionType.READ_TO_AND_WRITE, HexEngine.toByteArray(1024, order)),
                    new HexAction(HexActionType.NEW_INPUT, HexEngine.toByteArray(files[0].getAbsolutePath())),
                    new HexAction(HexActionType.READ_AND_WRITE, HexEngine.toByteArray((int) files[0].length(), order)),
                    new HexAction(HexActionType.NEW_INPUT, HexEngine.toByteArray(files[1].getAbsolutePath())),
                    new HexAction(HexActionType.READ_AND_WRITE, HexEngine.toByteArray((int) files[1].length(), order)),
                    new HexAction(HexActionType.NEW_INPUT, HexEngine.toByteArray(files[2].getAbsolutePath())),
                    new HexAction(HexActionType.READ_AND_WRITE, HexEngine.toByteArray((int) files[2].length(), order)),
                    new HexAction(HexActionType.NEW_INPUT, HexEngine.toByteArray(files[3].getAbsolutePath())),
                    new HexAction(HexActionType.READ_AND_WRITE, HexEngine.toByteArray((int) files[3].length(), order)),
                };
            }
        }
        engine = new HexEngine(base, os, order, actions);
        try{
            engine.execute();
        }
        catch(IOException ex){
            AlertWorker.showAlert(Alert.AlertType.ERROR,
                    "tmxtool",
                    exportPath, 
                    "File error: HexEngine failed to encode TMX");
        }
        return true;   
    }

    
}
