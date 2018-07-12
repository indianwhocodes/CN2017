package p2p;

public class ByteManipulation {
    public static byte[] int2bytes(int a){
        byte[] bytearray = new byte[4];
        bytearray[0] = (byte)((a & 0xff000000) >> 24);
        bytearray[1] = (byte)((a & 0xff0000) >> 16);
        bytearray[2] = (byte)((a & 0xff00) >> 8);
        bytearray[3] = (byte)((a & 0xff));
        return bytearray;
    }

    public static int bytes2int(byte[] bytearray) {
        int a = 0;
        for(int i = 0 ; i < 4; i++){
            a = (a << 8) | (bytearray[i] & 0xFF);
        }
        return a;
    }
}
