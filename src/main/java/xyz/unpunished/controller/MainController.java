package xyz.unpunished.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import javafx.collections.FXCollections;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import lombok.Getter;
import org.apache.commons.io.FilenameUtils;
import xyz.unpunished.util.AlertWorker;
import xyz.unpunished.util.IniWorker;
import xyz.unpunished.util.TmxEncoder;
import xyz.unpunished.util.TmxExtractor;

@Getter
public class MainController implements Initializable {

    private IniWorker iniWorker;
    private Thread fileThread;
    
    @FXML
    private TextField wavSnrFileField;
    @FXML
    private TextField gin1Field;
    @FXML
    private TextField gin2Field;
    @FXML
    private TextField gin3Field;
    @FXML
    private CheckBox minRPM;
    @FXML
    private TextField minRPMField;
    @FXML
    private TextField maxRPMField;
    @FXML
    private CheckBox customPath;
    @FXML
    private TextField customPathField;
    @FXML
    private CheckBox blackBoxPattern;
    @FXML
    private TextField carNumberField;
    @FXML
    private ComboBox<String> engExhBox;
    @FXML
    private ComboBox<String> playerAIBox;
    @FXML
    private ComboBox<String> tmxBaseBox;
    @FXML
    private TextField tmxExtractorField;
    @FXML
    private CheckBox encodeToEAL3;
    @FXML
    private CheckBox loopSNR;
    @FXML
    private HBox customPathBox;
    @FXML
    private HBox blackBoxPatternBox;
    @FXML
    private HBox gin2Box;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        iniWorker = new IniWorker("tmxtool.ini");
        String[] baseNames = new String[]{
                "player",
                "ai"
        };
        String[] appends = new String[]{
                ".tmx",
                "-player.tmx",
                "-ai.tmx"
        };
        String[] typeNames = new String[]{
                "engine",
                "exhaust"
        };
        engExhBox.setItems(FXCollections.observableArrayList(typeNames));
        playerAIBox.setItems(FXCollections.observableArrayList(appends));
        tmxBaseBox.setItems(FXCollections.observableArrayList(baseNames));
        readIniWorkerValues();
        customPath.selectedProperty().addListener((ov, t, t1) -> {
            customPathBox.setDisable(t);
            iniWorker.setCustomOutput(t1);
        });
        blackBoxPattern.selectedProperty().addListener((ov, t, t1) -> {
            blackBoxPatternBox.setDisable(t);
            iniWorker.setBlackBoxPattern(t1);
        });
        engExhBox.getSelectionModel().selectedIndexProperty().addListener((ov, t, t1) -> {
            iniWorker.setEngExh(t1.intValue());
        });
        playerAIBox.getSelectionModel().selectedIndexProperty().addListener((ov, t, t1) -> {
            iniWorker.setAppend(t1.intValue());
        });
        tmxBaseBox.getSelectionModel().selectedIndexProperty().addListener((ov, t, t1) -> {
            gin2Box.setDisable(!t1.equals(0));
            iniWorker.setTmxType(t1.intValue());
        });
        encodeToEAL3.selectedProperty().addListener((ov, t, t1) -> {
            loopSNR.setDisable(t);
            iniWorker.setEncodeSnr(t1);
        });
        loopSNR.selectedProperty().addListener((ov, t, t1) -> {
            iniWorker.setLoopSnr(t1);
        });
        minRPM.selectedProperty().addListener((ov, t, t1) -> {
            minRPMField.setDisable(t);
            iniWorker.setMinRPM(t1);
        });
        customPathField.textProperty().addListener((ov, t, t1) -> {
            iniWorker.setCustomOutputPath(t1);
        });
        wavSnrFileField.textProperty().addListener((ov, t, t1) -> {
            iniWorker.setWavSnrPath(t1);
        });
        gin1Field.textProperty().addListener((ov, t, t1) -> {
            iniWorker.setGin1Path(t1);
        });
        gin2Field.textProperty().addListener((ov, t, t1) -> {
            iniWorker.setGin2Path(t1);
        });
        gin3Field.textProperty().addListener((ov, t, t1) -> {
            iniWorker.setGin3Path(t1);
        });
        minRPM.selectedProperty().addListener((ov, t, t1) -> {
            iniWorker.setMinRPM(t1);
        });
        tmxExtractorField.textProperty().addListener((ov, t, t1) -> {
            iniWorker.setExtractPath(t1);
        });
        minRPMField.textProperty().addListener((ov, oldValue, newValue) -> {
            try{
                float x = Float.parseFloat(newValue);
            }
            catch(NumberFormatException ex){
                if(!newValue.equals(""))
                    minRPMField.setText(oldValue);
                return;
            }
            String[] newValueSplit = newValue.split("\\.", 2);
            for(int i = 0; i < newValueSplit.length; i ++)
                newValueSplit[i] = newValueSplit[i].matches("\\d*") 
                    ? newValueSplit[i]
                    : newValueSplit[i].replaceAll("[^\\d]", "");
            if(newValueSplit.length > 1)
                minRPMField.setText(newValueSplit[0] + "." 
                        + (newValueSplit[1].length() > 2
                        ? newValueSplit[1].substring(0, 2)
                        : newValueSplit[1]));
            else
                if(!newValueSplit[0].equals(newValue))
                    minRPMField.setText(newValueSplit[0]);
            iniWorker.setMinRPMVal(Float.parseFloat(minRPMField.getText()));
        });
        minRPMField.focusedProperty().addListener((ov, t, t1) -> {
            if(!t1){
                String[] split = minRPMField.getText().split("\\.", 2);
                if(split.length > 1 && split[1].equals(""))
                    minRPMField.setText(minRPMField.getText() + "00");
            }
        });
        maxRPMField.textProperty().addListener((ov, oldValue, newValue) -> {
            try{
                float x = Float.parseFloat(newValue);
            }
            catch(NumberFormatException ex){
                if(!newValue.equals(""))
                    maxRPMField.setText(oldValue);
                return;
            }
            String[] newValueSplit = newValue.split("\\.", 2);
            for(int i = 0; i < newValueSplit.length; i ++)
                newValueSplit[i] = newValueSplit[i].matches("\\d*") 
                    ? newValueSplit[i]
                    : newValueSplit[i].replaceAll("[^\\d]", "");
            if(newValueSplit.length > 1)
                maxRPMField.setText(newValueSplit[0] + "." 
                        + (newValueSplit[1].length() > 2
                        ? newValueSplit[1].substring(0, 2)
                        : newValueSplit[1]));
            else
                if(!newValueSplit[0].equals(newValue))
                    maxRPMField.setText(newValueSplit[0]);
            iniWorker.setMaxRPMVal(Float.parseFloat(maxRPMField.getText()));
        });
        maxRPMField.focusedProperty().addListener((ov, t, t1) -> {
            if(!t1){
                String[] split = maxRPMField.getText().split("\\.", 2);
                if(split.length > 1 && split[1].equals(""))
                    maxRPMField.setText(maxRPMField.getText() + "00");
            }
        });
        carNumberField.textProperty().addListener((ov, t, t1) -> {
            if(!t1.matches("\\d*"))
                carNumberField.setText(t1.replaceAll("[^\\d]", ""));
            if(t1.length() > 3)
                carNumberField.setText(t1.substring(1, 4));
            if(t1.length() < 3)
                carNumberField.setText("0" + t1);
            iniWorker.setNumber(Integer.parseInt(carNumberField.getText()));
        });
        fileThread = new Thread(() -> {
            try {
                while (true){
                    Thread.sleep(10000);
                    iniWorker.rewriteIni(iniWorker.getDefaultIni());
                }
            } catch (InterruptedException ex) {
                
            }
        });
        fileThread.start();
    }
    
    private void readIniWorkerValues(){
        wavSnrFileField.setText(iniWorker.getWavSnrPath());
        gin1Field.setText(iniWorker.getGin1Path());
        gin2Field.setText(iniWorker.getGin2Path());
        gin3Field.setText(iniWorker.getGin3Path());
        customPath.setSelected(iniWorker.isCustomOutput());
        customPathField.setText(iniWorker.getCustomOutputPath());
        blackBoxPattern.setSelected(iniWorker.isBlackBoxPattern());
        carNumberField.setText(String.format("%03d", iniWorker.getNumber()));
        engExhBox.getSelectionModel().select(iniWorker.getEngExh());
        playerAIBox.getSelectionModel().select(iniWorker.getAppend());
        tmxBaseBox.getSelectionModel().select(iniWorker.getTmxType());
        minRPM.setSelected(iniWorker.isMinRPM());
        minRPMField.setText(String.format("%.2f", iniWorker.getMinRPMVal()));
        maxRPMField.setText(String.format("%.2f", iniWorker.getMaxRPMVal()));
        encodeToEAL3.setSelected(iniWorker.isEncodeSnr());
        loopSNR.setSelected(iniWorker.isLoopSnr());
        tmxExtractorField.setText(iniWorker.getExtractPath());
        customPathBox.setDisable(!iniWorker.isCustomOutput());
        blackBoxPatternBox.setDisable(!iniWorker.isBlackBoxPattern());
        gin2Box.setDisable(iniWorker.getTmxType() != 0);
        loopSNR.setDisable(!iniWorker.isEncodeSnr());
        minRPMField.setDisable(!iniWorker.isMinRPM());
    }

    private String browseFile(String initialDirectory, String[] fileTypes, String[] fileTypeNames,
            boolean save){
        File f = new File(initialDirectory);
        FileChooser fc = new FileChooser();
        for(int i = 0; i < fileTypes.length; i ++){
            FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(
                fileTypeNames[i],
                fileTypes[i]);
            fc.getExtensionFilters().add(filter);
        }
        if(fileTypes.length > 1){
            FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(
                    "All Supported Audio Files",
                    fileTypes
            );
            fc.getExtensionFilters().add(filter);
        }
        if(f.exists()){
            Path p = Paths.get(f.getAbsolutePath());
            fc.setInitialDirectory(new File(p.getParent().toString()));
        }
        else{
            fc.setInitialDirectory(new File(System.getProperty("user.dir")));
        }
        try{
            if(save)
                f = fc.showSaveDialog((Stage) gin1Field.getScene().getWindow());
            else
                f = fc.showOpenDialog((Stage) gin1Field.getScene().getWindow());
            if((f.exists() && f.isFile()) || save)
                return f.getAbsolutePath();
            else
                return initialDirectory;
        }
        catch (Exception e){
            return initialDirectory;
        }
    }

    @FXML
    private void generateTmx(){
        if (checkInputs()){
            TmxEncoder encoder = TmxEncoder.builder()
                .wavSnr(wavSnrFileField.getText())
                .gin1(gin1Field.getText())
                .gin2(gin2Field.getText())
                .gin3(gin3Field.getText())
                .exportName(createExportName())
                .exportPath(customPathField.getText())
                .doExportName(blackBoxPattern.isSelected())
                .doExportPath(customPath.isSelected())
                .minRPM(minRPM.isSelected() ? Float.parseFloat(minRPMField.getText()): (float) 0.0)
                .writeMinRPM(minRPM.isSelected())
                .maxRPM(Float.parseFloat(maxRPMField.getText()))
                .ai(tmxBaseBox.getSelectionModel().getSelectedIndex() == 1)
                .encodeSnr(encodeToEAL3.isSelected())
                .loopSnr(loopSNR.isSelected())
                .build();
            if(encoder.encodeTmx()){
                AlertWorker.showAlert(
                        Alert.AlertType.INFORMATION,
                        "tmxtool",
                        "Information",
                        "TMX has been encoded");
            }
        }
    }
    
    private String createExportName(){
        return "car_" + carNumberField.getText() 
                + "-"
                + engExhBox.getSelectionModel().getSelectedItem()
                + playerAIBox.getSelectionModel().getSelectedItem();
    }
    
    @FXML
    private boolean checkInputs(){
        // main outputs should not be empty
        try{
            if(wavSnrFileField.getText().equals("") 
                || gin1Field.getText().equals("")
                || (gin2Field.getText().equals("") && !gin2Box.isDisable())
                || gin3Field.getText().equals(""))
                throw new Exception();
            if(wavSnrFileField.getText() == null 
                || gin1Field.getText() == null
                || (gin2Field.getText() == null && !gin2Box.isDisable())
                || gin3Field.getText() == null)
                throw new Exception();
        }
        catch(Exception ex){
            AlertWorker.showAlert(
                    Alert.AlertType.ERROR,
                    "tmxtool",
                    "Error",
                    "All main inputs should be filled");
            return false;
        }
        // minRPM < maxRPM
        try{
            float maxRPMVal = Float.parseFloat(maxRPMField.getText());
            float minRPMVal = minRPM.isSelected() 
                    ? Float.parseFloat(minRPMField.getText())
                    : (float) 0.0;
            if(maxRPMVal < minRPMVal){
                throw new NumberFormatException();
            }
            if(minRPMVal < 200 && minRPM.isSelected())
                throw new NumberFormatException();
        }
        catch (NumberFormatException ex){
            AlertWorker.showAlert(
                    Alert.AlertType.ERROR,
                    "tmxtool",
                    "Error",
                    "RPM Value error, maxRPM should be bigger than "
                    + "minRPM and minRPM should be bigger than 200");
            return false;
        }
        if(customPath.isSelected())
            if(customPathField.getText().equals("") || customPathField.getText() == null){
                AlertWorker.showAlert(
                    Alert.AlertType.ERROR,
                    "tmxtool",
                    "Error",
                    "Please select custom outputh path or unselect "
                            + "the checkbox");
                return false;
            }
        if(blackBoxPattern.isSelected())
            if(carNumberField.getText().equals("") || carNumberField.getText().equals("")){
                AlertWorker.showAlert(
                    Alert.AlertType.ERROR,
                    "tmxtool",
                    "Error",
                    "Please select and fill all the Black Box file pattern fields "
                            + "or unselect the checkbox");
                return false;
            }
        return true; 
    }
    
    @FXML
    private void browseWavSnr(){
        wavSnrFileField.setText(browseFile(wavSnrFileField.getText(), new String[]{"*.wav", "*snr"},
                new String[]{"Waveform Audio File",
                            "SNR Audio File"}, false));
    }

    @FXML
    private void browseGin1(){
        gin1Field.setText(browseFile(gin1Field.getText(), new String[]{"*.gin"}, new String[]{"Ginsu Audio File"}, false));
    }

    @FXML
    private void browseGin2(){
        gin2Field.setText(browseFile(gin2Field.getText(), new String[]{"*.gin"}, new String[]{"Ginsu Audio File"}, false));
    }

    @FXML
    private void browseGin3(){
        gin3Field.setText(browseFile(gin3Field.getText(), new String[]{"*.gin"}, new String[]{"Ginsu Audio File"}, false));
    }
    
    @FXML
    private void browseCustomPath(){
        customPathField.setText(browseFile(customPathField.getText(), new String[]{"*.tmx"}, new String[]{"TMX Audio File"}, true));
    }
    
    @FXML
    private void browseTmxExtract(){
        tmxExtractorField.setText(browseFile(tmxExtractorField.getText(), new String[]{"*.tmx"}, new String[]{"TMX Audio File"}, false));
    }
    
    private boolean checkExtractField(){
        if(tmxExtractorField.getText() == null || tmxExtractorField.getText().equals("")){
            AlertWorker.showAlert(
                    Alert.AlertType.ERROR,
                    "tmxtool",
                    "Error",
                    "No file to extract is selected");
            return false;
        }
        return true;
    }
    
    @FXML
    private void extractTmx(){
        if(checkExtractField()){
            TmxExtractor extractor = new TmxExtractor(tmxExtractorField.getText());
            if(extractor.extractTmx())
                AlertWorker.showAlert(
                    Alert.AlertType.INFORMATION,
                    "tmxtool",
                    "Success",
                    "TMX file extracted");
        }
    }
    
    @FXML
    private void launchGinTool(){
        if (iniWorker.getJavaPath() != null 
                && FilenameUtils.getName(iniWorker.getJavaPath())
                        .toLowerCase().equals("java.exe")){
            ProcessBuilder builder = new ProcessBuilder(iniWorker.getJavaPath(),
                "-jar",
                Paths.get(System.getProperty("user.dir"), "gintool.jar").toString());
            builder = builder.inheritIO();
            builder = builder.directory(new File(System.getProperty("user.dir")));
            try{
                builder.start();
            }
            catch(IOException ex){
                ex.printStackTrace();
                AlertWorker.showAlert(
                        Alert.AlertType.ERROR,
                        "tmxtool",
                        "Error",
                        "File error: gintool.jar or "
                        + "java.exe doesn't exist or process failed to start");
                iniWorker.setJavaPath("");
            }
        }
        else{
            AlertWorker.showAlert(
                        Alert.AlertType.INFORMATION,
                        "tmxtool",
                        "Information",
                        "Please select bin/java.exe inside of your JDK 11 "
                        + "and ensure gintool.jar is in the same folder as tmxtool "
                        + "to make sure it works");
            iniWorker.setJavaPath(browseFile(System.getProperty("user.dir"),
                    new String[]{"java.exe"}, new String[]{"Java Exectuable"}, false));
        }
    }

}
