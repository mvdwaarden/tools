package data;

public interface BlockReaderCallback {
	public boolean readBlock(int blocknr, int blocksize, int currentReadbytes, byte[] currentBlock, int[] readbytes,
			byte[][] blockswindow);
}
