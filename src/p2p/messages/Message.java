package p2p.messages;

import p2p.ByteManipulation;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Message {

    public final static int CHOKE = 0;
    public final static int UNCHOKE = 1;
    public final static int INTERESTED = 2;
    public final static int NOTINTERESTED = 3;
    public final static int HAVE = 4;
    public final static int BITFIELD = 5;
    public final static int REQUEST = 6;
    public final static int PIECE = 7;
    public final static int STOP = 8;

    private int messageType;
    private int length;
    protected byte[] payload;

    public Message() {
        messageType = 0;
        length = 0;
        payload = null;
    }

    public Message(int type) {
        messageType = type;
    }

    public static int getCHOKE() {
        return CHOKE;
    }

    public Message(int type, byte[] payLoad) {
        messageType = type;
        this.payload = payLoad;

    }

    public byte[] getPayload() {
        return payload;
    }

    //Message sent to neighbor's download by the given rules(length-type-variable payload)
    public void send(Socket uploadSocket) throws IOException{


        OutputStream out = uploadSocket.getOutputStream();
		/*An actual message consists of 4-byte message length field, 1-byte message type field, and a message
			payload with variable size.*/
        if(payload == null){
            length = 4;       				// since length = 4 bytes to denote message's length
        }else{
            length = payload.length + 4;
        }

        out.write(ByteManipulation.int2bytes(length));			//length is converted to byte array to send
        out.write(ByteManipulation.int2bytes(messageType));				//type is converted to byte array to send
        if(payload != null){
            out.write(payload);      // payload could be null for some messages
        }
        out.flush();

    }

    //Message received from neighbor's uploadSocket, and Message's length, type and payload is set
    public void receive(Socket uploadSocket) throws IOException{
        InputStream in = uploadSocket.getInputStream();
        byte[] bytearrayLength = new byte[4];
        int receivedBytes;				// counts the number of bytes read into bytearray
        int allReceivedBytes = 0;
        while(allReceivedBytes < 4){
			/*Reads up to len bytes of data from the input stream into an array of bytes.
			 *An attempt is made to read as many as len bytes, but a smaller number may be read.
			 *The number of bytes actually read is returned as an integer.*/
            receivedBytes = in.read(bytearrayLength, allReceivedBytes, 4 - allReceivedBytes);
            allReceivedBytes += receivedBytes;
        }
        length = ByteManipulation.bytes2int(bytearrayLength);

        byte[] bytearrayType = new byte[4];
        allReceivedBytes = 0;
        while(allReceivedBytes < 4){
            receivedBytes = in.read(bytearrayType, allReceivedBytes, 4 - allReceivedBytes);
            allReceivedBytes += receivedBytes;
        }
        messageType = ByteManipulation.bytes2int(bytearrayType);

        if(length > 4){				// only if there is a incoming payload, can the length be > 4
            payload = new byte[length - 4];
        }else{
            payload = null;
        }

        allReceivedBytes = 0;
        while(allReceivedBytes < (length-4)){    // only if there is a incoming payload, can the length be > 4
            receivedBytes = in.read(payload, allReceivedBytes, (length-4)-allReceivedBytes);
            allReceivedBytes += receivedBytes;
        }
    }

    public static int getUNCHOKE() {
        return UNCHOKE;
    }

    public static int getINTERESTED() {
        return INTERESTED;
    }

    public static int getNOTINTERESTED() {
        return NOTINTERESTED;
    }

    public static int getHAVE() {
        return HAVE;
    }

    public static int getBITFIELD() {
        return BITFIELD;
    }

    public static int getREQUEST() {
        return REQUEST;
    }

    public static int getPIECE() {
        return PIECE;
    }

    public static int getSTOP() {
        return STOP;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }
}
