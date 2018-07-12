package p2p.file_handling;

import java.io.IOException;
import java.io.RandomAccessFile;

import p2p.config_files.CommonConfigReader;
import p2p.messages.Piece;

import java.io.FileNotFoundException;
import java.io.File;


public class FileManager{

	private RandomAccessFile raccFile;
	private CommonConfigReader commonConfig;

	public FileManager(int id, CommonConfigReader commonConfig)throws FileNotFoundException{
		String path = "peer_" + id + "/";
		File directory = new File(path);
		this.commonConfig = commonConfig;

		if(!directory.exists()){
			directory.mkdirs();
		}
		raccFile = new RandomAccessFile(path + commonConfig.getFileName(), "rw");
	}

	public synchronized void writeChunk(Piece chunk) throws IOException{
		int startPt = chunk.getMissingPieceIndex() * commonConfig.getPieceSize();
		raccFile.seek(startPt);
		byte[] chunkByteArr = chunk.getPayload();

		for(int a = 0; a < chunkByteArr.length; a++){
			raccFile.writeByte(chunkByteArr[a]);
		}
	}

	public synchronized Piece readChunk(int index) throws IOException{
		byte[] chunkByteArr;
		int size = 0;
		int startPt = index * commonConfig.getPieceSize();

		if(index != commonConfig.getNumPieces() - 1){
			size = commonConfig.getPieceSize();
		}
		else{
			size = (commonConfig.getFileSize()) - (index * commonConfig.getPieceSize());
		}
		System.out.println("Read piece "+index+" size :"+size);
		chunkByteArr = new byte[size];
		raccFile.seek(startPt);
		for(int a = 0; a < size; a++){
			chunkByteArr[a] = raccFile.readByte();
		}
		Piece chunk = new Piece(index, chunkByteArr);
		return chunk;
	}


}
