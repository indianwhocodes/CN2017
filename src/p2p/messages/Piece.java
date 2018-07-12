package p2p.messages;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Piece extends Message {

	private int missingPieceIndex;
	private byte[] payload;

	public Piece(int missingPieceIndex, byte[] payload) {
		super(Message.PIECE);
		this.missingPieceIndex = missingPieceIndex;
		this.payload = payload;
	}

	public byte[] getPayload() {
		return payload;
	}

	public int getMissingPieceIndex() {
		return missingPieceIndex;
	}
}
