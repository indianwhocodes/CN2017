package p2p.messages;

import java.nio.ByteBuffer;

public class Request extends Message {

	public Request(int pieceIndex) {
		super(Message.REQUEST, ByteBuffer.allocate(4).putInt(pieceIndex).array());
	}

	public int getPieceIndex() {
		return ByteBuffer.wrap(payload).getInt();
	}
}
