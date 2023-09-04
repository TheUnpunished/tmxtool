package xyz.unpunished.util;

import com.sun.javafx.application.PlatformImpl;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.nio.file.Paths;
import javafx.scene.control.Alert;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import xyz.unpunished.model.FileInfo;
import xyz.unpunished.model.FileType;

@AllArgsConstructor
public class TmxExtractor {
    
    private final String tmxPathString;
    
    private final int int32LE = 808464433;
    private final int int32BE = 825241648;
    
    public boolean extractTmx(){
        File tmx = new File(tmxPathString);
        try (InputStream is = new BufferedInputStream(
                new FileInputStream(tmx))) {
            ByteOrder order;
            byte[] buf = new byte[4];
            HexEngine.skip(is, 12);
            is.read(buf);
            int endian = HexEngine.readInt32(buf, ByteOrder.LITTLE_ENDIAN);
            switch (endian) {
                case int32LE:{
                    order = ByteOrder.LITTLE_ENDIAN;
                    break;
                }
                case int32BE:{
                    order = ByteOrder.BIG_ENDIAN;
                    break;
                }
                default:
                    PlatformImpl.runAndWait(() -> AlertWorker.showAlert(
                            Alert.AlertType.ERROR,
                            "tmxtool", 
                            "Error",
                            "File error: failed to detect endianness"));
                    return false;
            }
            HexEngine.skip(is, 16);
            is.read(buf);
            int fileCount = HexEngine.readInt32(buf, order);
            HexEngine.skip(is, 52);
            is.read(buf);
            int tableOffset = HexEngine.readInt32(buf, order);
            is.read(buf);
            int dataOffset = HexEngine.readInt32(buf, order);
            HexEngine.skip(is, tableOffset - 96);
            FileInfo[] fileInfo = new FileInfo[fileCount];
            int prevOffset = 0;
            for(int i = 0; i < fileCount; i ++){
                buf = new byte[4];
                is.read(buf);
                int val = HexEngine.readInt32(buf, order);
                FileType type;
                if(FileType.GIN.equals(val))
                    type = FileType.GIN;
                else
                    if (FileType.SNR.equals(val))
                        type = FileType.SNR;
                    else{
                        PlatformImpl.runAndWait(() -> AlertWorker.showAlert(
                            Alert.AlertType.ERROR,
                            "tmxtool", 
                            "Error",
                            "File error: failed to detect one of file types"));
                        return false;
                    }
                HexEngine.skip(is, 4);
                is.read(buf);
                val = HexEngine.readInt32(buf, order);
                if(i > 0){
                    fileInfo[i - 1].setSize(val - prevOffset);
                    prevOffset = val;
                }
                if(i + 1 < fileCount){
                    buf = new byte[24];
                    is.read(buf);
                }
                fileInfo[i] = new FileInfo(0, type);
            }
            fileInfo[fileCount - 1].setSize((int) tmx.length() - prevOffset);
            HexEngine.skip(is, dataOffset - (tableOffset + 36 * (fileCount - 1) + 12));
            for(int i = 0; i < fileCount; i ++){
                OutputStream os = new BufferedOutputStream(
                        new FileOutputStream(
                                Paths.get(FilenameUtils.removeExtension(
                                        tmxPathString) + " [" + i + "]" + "."
                                        + fileInfo[i].getType().toString().toLowerCase())
                                        .toFile()));
                buf = new byte[fileInfo[i].getSize()];
                is.read(buf);
                os.write(buf);
                os.flush();
                os.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            AlertWorker.showAlert(
                    Alert.AlertType.ERROR,
                    "tmxtool",
                    "Error",
                    "File error: Failed to read from or write to "
                    + "one of the files. Check for read/write permissions");
        }
        return true;
    }
}
