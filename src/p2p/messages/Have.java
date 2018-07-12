package p2p.messages;

import java.nio.ByteBuffer;

public class Have extends Message {

	public Have(int index) {
		super(Message.HAVE, ByteBuffer.allocate(4).putInt(index).array());
	}
	
	public int getIndex(){
		return ByteBuffer.wrap(payload).getInt();
	}
}
