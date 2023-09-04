package xyz.unpunished.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import xyz.unpunished.model.HexAction;
import static xyz.unpunished.model.HexActionType.WRITE_AND_SKIP;

public class HexEngine {
    
    private InputStream is;
    private OutputStream os;
    private ByteOrder order;
    private final HexAction[] actions;
    
    private int indexOn = 0;
    
    public HexEngine(InputStream is, OutputStream os, ByteOrder order,
                HexAction[] actions){
        this.is = is;
        this.os = os;
        this.order = order;
        this.actions = actions;
    }
    
    
    public void execute() throws IOException{
        for(HexAction action: actions){
            switch(action.getActionType()){
                case SKIP: {
                    skip(action.getValue(), false);
                    break;
                }
                case SKIP_TO: {
                    skip(action.getValue(), true);
                    break;
                }
                case WRITE: {
                    write(action.getValue());
                    break;
                }
                case WRITE_AND_SKIP: {
                    writeAndSkip(action.getValue());
                    break;
                }
                case READ_AND_WRITE: {
                    readAndWrite(action.getValue(), false);
                    break;
                }
                case READ_TO_AND_WRITE:{
                    readAndWrite(action.getValue(), true);
                    break;
                }
                case NEW_INPUT: {
                    switchInput(action.getValue());
                    break;
                }
                case NEW_OUTPUT: {
                    switchOutput(action.getValue());
                    break;
                }
                case CHANGE_BYTE_ORDER: {
                    changeByteOrder(action.getValue());
                    break;
                }
            }
        }
        is.close();
        os.flush();
        os.close();
    }
    
    private void skip(int howMuch) throws IOException{
        is.read(new byte[howMuch]);
        indexOn += howMuch;
    }
    
    private void skip(byte[] howMuch, boolean skipTo) throws IOException{
        int val = getInt32(howMuch) - (skipTo ? indexOn : 0);
        is.read(new byte[val]);
        indexOn += val;
    }
    
    private void write(byte[] value) throws IOException{
        os.write(value);
    }
    
    private void writeAndSkip(byte[] value) throws IOException{
        os.write(value);
        skip(value.length);
    }
    
    private void readAndWrite(byte[] howMuch, boolean readTo) throws IOException{
        int val = getInt32(howMuch) - (readTo ? indexOn : 0);
        byte[] buf = new byte[val];
        is.read(buf);
        os.write(buf);
        indexOn += val;
    }
    
    private void switchInput(byte[] newInput) throws IOException{
        is.close();
        is = new BufferedInputStream(new FileInputStream(new File(
                        getStringFromByteArray(newInput))));
        indexOn = 0;
    }
    
    private void switchOutput(byte[] newOutput) throws IOException{
        os.flush();
        os.close();
        os = new BufferedOutputStream(new FileOutputStream(new File(
                getStringFromByteArray(newOutput))));
    }
    
    private void changeByteOrder(byte[] order){
        this.order = order[0] == 0 ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
    }
    
    private float getFloat32(byte[] array){
        return ByteBuffer.wrap(array).order(order).getFloat(0);
    }
    
    private int getInt32(byte[] array){
        return ByteBuffer.wrap(array).order(order).getInt(0);
    }
    
    private String getStringFromByteArray(byte[] array){
        return new String(array, StandardCharsets.UTF_8);
    }
    
    public static byte[] toByteArray(int value, ByteOrder byteOrder){
        return ByteBuffer.allocate(4).order(byteOrder).putInt(value).array();
    }
    
    public static byte[] toByteArray(float value, ByteOrder byteOrder){
        return ByteBuffer.allocate(4).order(byteOrder).putFloat(value).array();
    }
    
    public static byte[] toByteArray(String value){
        return value.getBytes(StandardCharsets.UTF_8);
    }
    
    public static void skip(InputStream stream, int howMuch) throws IOException{
        stream.read(new byte[howMuch]);
    }
    
    public static int readInt32(byte[] val, ByteOrder byteOrder){
        return ByteBuffer.wrap(val).order(byteOrder).getInt(0);
    }
    
}
