package p2p.messages;

import java.util.Arrays;

//class to handle bitfield operation
//Every peer has its own Bitfield to handle the pieces it has or doesn't have
public class Bitfield extends Message {
    
    private boolean[] bitfieldArray;
    private int totalPieces;
    private int numDownloadedPieces;
    boolean isFinishedDownloading;
    
    public Bitfield(int totalPieces) {
        super(Message.BITFIELD);
        this.totalPieces = totalPieces;
        System.out.println("Bitfield : num pieces = "+totalPieces);
        this.numDownloadedPieces = 0;
        this.isFinishedDownloading = false;
        bitfieldArray = new boolean[totalPieces];
        Arrays.fill(bitfieldArray, false);
    }
    
    public void setAllTrue() {
        for(int i=0;i<totalPieces;i++){
            bitfieldArray[i]=true;
        }
        numDownloadedPieces = totalPieces;
        isFinishedDownloading = true;
    }

    //converts the boolean bitField to byte array of 1s and 0s
    public synchronized byte[] toBytes(){
        int numOfBytes; //no of bytes needed to fit the boolean array

        if(totalPieces % 8 == 0){
            numOfBytes = totalPieces/8;
        }
        else{
            numOfBytes = totalPieces/8 + 1;  //calculating no of bytes
        }

        byte[] byteField = new byte[numOfBytes];

        //initialising byteField with all 0s
        for(int i = 0 ; i < numOfBytes; i++){
            byteField[i] = (byte)0;
        }

        for(int i = 0; i < totalPieces; i++){
            int byteIndex = i /8;  //index of byteField byte
            int bitIndex = i % 8;  //index of byteField bit
            if(bitfieldArray[i] == true){
                byteField[byteIndex] = (byte) (byteField[byteIndex] | (1<<bitIndex));  //setting 1 at bitindex
            }
            else{
                byteField[byteIndex] = (byte) (byteField[byteIndex] & ~(1<<bitIndex));  // setting 0 at bitindex
            }
        }

        return byteField;
    }

    //initializes the boolean array  using an input byte array
    public synchronized void setBoolBitfield(byte[] byteField){
        numDownloadedPieces = 0;					//num of pieces downloaded set to 0 initially
        for(int i = 0 ; i < totalPieces; i++){
            int byteIndex = i / 8;					//index of byte to be read from byte array
            int bitIndex = i % 8;					// index of bit to be read of that byte
            if((byteField[byteIndex] & (1 << bitIndex)) == 0){
                bitfieldArray[i] = false;
            }
            else{
                bitfieldArray[i] = true;
                numDownloadedPieces++;
            }
        }
        if(numDownloadedPieces == totalPieces){ // if all pieces are downloaded, finished is set to true;
            isFinishedDownloading = true;
        }
    }

    public synchronized int getMissingIndex(Bitfield b){
        int index = -1;   // if I have all the pieces that the other peer has, I send -1
        for(int  i = 0; i < totalPieces; i++){
            if(bitfieldArray[i] == false && b.bitfieldArray[i] == true){
                return i;
            }
        }
        return index;
    }

    public boolean isFinishedDownloading() {
        return isFinishedDownloading;
    }

    //turn a particular bit in the boolean array as true
    public synchronized void setSingleBit(int bitNum){
        if(bitfieldArray[bitNum] == false){
            bitfieldArray[bitNum] = true;
            numDownloadedPieces++;  //increase the number of pieces downloaded by 1
            if(numDownloadedPieces == totalPieces){  //if all pieces have been downloaded, set finished as true
                setFinishedDownloading(true);
            }
        }
    }

    public void setFinishedDownloading(boolean finishedDownloading) {
        isFinishedDownloading = finishedDownloading;
    }
}
