package xyz.unpunished.util;

import javafx.scene.control.Alert;
import lombok.Getter;

import java.io.*;
import java.util.Locale;
import lombok.Setter;

@Getter
@Setter
public class IniWorker {

    private final File defaultIni;
    private String wavSnrPath;
    private String gin1Path;
    private String gin2Path;
    private String gin3Path;
    private boolean customOutput;
    private String customOutputPath;
    private boolean blackBoxPattern;
    private int number;
    private int engExh;
    private int append;
    private int tmxType;
    private boolean encodeSnr;
    private boolean loopSnr;
    private boolean minRPM;
    private float minRPMVal;
    private float maxRPMVal;
    private String extractPath;
    private boolean firstLaunch;
    private Locale locale;
    
    public void initIni(){
        wavSnrPath = "";
        gin1Path = "";
        gin2Path = "";
        gin3Path = "";
        customOutput = false;
        customOutputPath = "";
        blackBoxPattern = false;
        number = 0;
        engExh = 0;
        append = 0;
        encodeSnr = false;
        loopSnr = false;
        tmxType = 0;
        minRPM = false;
        minRPMVal = (float) 0.0;
        maxRPMVal = (float) 0.0;
        extractPath = "";
        firstLaunch = true;
        locale = Locale.ENGLISH;
    }

    public IniWorker(String iniName){
        defaultIni = new File(iniName);
        initIni();
        if(defaultIni.exists()){
            readIniFile(defaultIni);
        }
        else {
            rewriteIni(defaultIni);
        }
    }

    private void readIniFile(File ini){
        try{
            BufferedReader reader = new BufferedReader((new FileReader(ini)));
            wavSnrPath = readLine(reader);
            gin1Path = readLine(reader);
            gin2Path = readLine(reader);
            gin3Path = readLine(reader);
            customOutput = Boolean.parseBoolean(readLine(reader));
            customOutputPath = readLine(reader);
            blackBoxPattern = Boolean.parseBoolean(readLine(reader));
            String line = readLine(reader);
            number = line.equals("") ? 0 : Integer.parseInt(line);
            number = number < 0 ? 0 : number;
            number %= 1000;
            line = readLine(reader);
            engExh = line.equals("") ? 0 : Integer.parseInt(line);
            engExh = engExh > 1 || engExh < 0 ? 0 : engExh;
            line = readLine(reader);
            append = line.equals("") ? 0 : Integer.parseInt(line);
            append = append > 2 || append < 0 ? 0 : append;
            line = readLine(reader);
            tmxType = line.equals("") ? 0 : Integer.parseInt(line);
            tmxType = tmxType > 1 || tmxType < 0 ? 0 : tmxType;
            encodeSnr = Boolean.parseBoolean(readLine(reader));
            loopSnr = Boolean.parseBoolean(readLine(reader));
            minRPM = Boolean.parseBoolean(readLine(reader));
            minRPMVal = Float.parseFloat(readLine(reader));
            maxRPMVal = Float.parseFloat(readLine(reader));
            extractPath = readLine(reader);
            firstLaunch = Boolean.parseBoolean(readLine(reader));
            locale = Locale.forLanguageTag(readLine(reader));
        }
        catch (IOException | NumberFormatException | NullPointerException | IndexOutOfBoundsException e){
            e.printStackTrace();
            initIni();
            AlertWorker.showAlert(Alert.AlertType.ERROR,
                    I18N.get("error"),
                    I18N.get("couldnt_read_ini"));
            rewriteIni(defaultIni);
        }
    }
    
    private String readLine(BufferedReader reader) throws IOException, IndexOutOfBoundsException {
        return reader.readLine().split("\\s+=\\s+", 2)[1];
    }

    public void rewriteIni(File ini){
        try{
            BufferedWriter bw = new BufferedWriter(new FileWriter(ini));
            bw.write("wavSnrPath = " + wavSnrPath); bw.newLine();
            bw.write("gin1Path = " + gin1Path); bw.newLine();
            bw.write("gin2Path = " + gin2Path); bw.newLine();
            bw.write("gin3Path = " + gin3Path); bw.newLine();
            bw.write("customOutput = " + customOutput); bw.newLine();
            bw.write("customOutputPath = " + customOutputPath); bw.newLine();
            bw.write("blackBoxPattern = " + blackBoxPattern); bw.newLine();
            bw.write("number = " + number); bw.newLine();
            bw.write("engExh = " + engExh); bw.newLine();
            bw.write("append = " + append); bw.newLine();
            bw.write("tmxType = " + tmxType); bw.newLine();
            bw.write("encodeSnr = " + encodeSnr); bw.newLine();
            bw.write("loopSnr = " + loopSnr); bw.newLine();
            bw.write("minRPM = " + minRPM); bw.newLine();
            bw.write("minRPMVal = " + String.format(Locale.ENGLISH, "%.2f", minRPMVal)); bw.newLine();
            bw.write("maxRPMVal = " + String.format(Locale.ENGLISH, "%.2f", maxRPMVal)); bw.newLine();
            bw.write("extractPath = " + extractPath); bw.newLine();
            bw.write("firstLaunch = " + firstLaunch); bw.newLine();
            bw.write("locale = " + locale.toLanguageTag());
            bw.flush();
            bw.close();
        }
        catch (IOException e){
            e.printStackTrace();
            AlertWorker.showAlert(Alert.AlertType.ERROR,
                    I18N.get("error"),
                    I18N.get("couldnt_write_ini"));
        }
    }


}
