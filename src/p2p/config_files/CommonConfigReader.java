package p2p.config_files;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

// class to read the properties of common.cfg

public class CommonConfigReader {
	private final int noOfPreferredNeighbors;
	private final int unchokingInterval;
	private final int optimisticUnchokingInterval;
	private final String fileName;
	private final int filesize;
	private final int piecesize;
	private final int numPieces;
	
	//getter and setter method.
	public int getNumPieces() {
		return numPieces;
	}
	public int getNoOfPreferredNeighbors() {
		return noOfPreferredNeighbors;
	}
	public int getUnchokingInterval() {
		return unchokingInterval;
	}
	public int getOptimisticUnchokingInterval() {
		return optimisticUnchokingInterval;
	}
	public String getFileName() {
		return fileName;
	}
	public int getFileSize() {
		return filesize;
	}
	public int getPieceSize() {
		return piecesize;
	}
	
	
	//setting variable values from cfg file
	//passing fileName as parameter.
	public CommonConfigReader(String commonCfg) throws FileNotFoundException{
		Scanner scan = new Scanner(new FileReader(commonCfg));

		this.noOfPreferredNeighbors = Integer.parseInt(scan.nextLine().split(" ")[1]);
		this.unchokingInterval = Integer.parseInt(scan.nextLine().split(" ")[1]);
		this.optimisticUnchokingInterval = Integer.parseInt(scan.nextLine().split(" ")[1]);
		this.fileName = scan.nextLine().split(" ")[1];
		this.filesize = Integer.parseInt(scan.nextLine().split(" ")[1]);
		this.piecesize = Integer.parseInt(scan.nextLine().split(" ")[1]);
		
		if(this.filesize % this.piecesize == 0) {
			this.numPieces = (this.filesize/this.piecesize);
		} else {
			this.numPieces = (this.filesize/this.piecesize)+1;
		}
		scan.close();
	}
	
}
