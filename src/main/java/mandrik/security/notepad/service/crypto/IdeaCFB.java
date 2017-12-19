package mandrik.security.notepad.service.crypto;


import java.util.Arrays;

public class IdeaCFB {

    private static final int R = 8;

    private IdeaCipher idea;
    private boolean encrypt;

    private int blockSize;
    private int partSize;
    private int rounds;
    private byte[] feedback;

    public IdeaCFB(boolean encrypt, String key) {
        idea = new IdeaCipher(key, true);
        this.encrypt = encrypt;
        blockSize = idea.getBlockSize();
        assert blockSize % R == 0 : "R must be divisor of blockSize";
        partSize = R;
        rounds = blockSize / R;
        feedback = IdeaUtils.makeKey(key, blockSize); // Get initial vector (IV) from user key
    }


    protected void crypt(byte[] data, int pos) {
        // Divide de block of data of size blockSize to partSize blocks
        byte[][] block = new byte[rounds][];
        for (int i = 0; i < rounds; i++) {
            block[i] = Arrays.copyOfRange(data, pos + partSize * i, pos + partSize * i + partSize);
        }
        // If decyphering -> save cryptogram (needed in xor operation)
        byte[][] crypt = new byte[0][];
        if (!this.encrypt) {
            crypt = new byte[rounds][];
            for (int i = 0; i < rounds; i++) {
                crypt[i] = block[i].clone();
            }
        }
        // Run CFB algorithm
        byte[] feedbackP1, feedbackP2;
        for (int i = 0; i < rounds; i++) {
            idea.crypt(feedback, 0);                                           // Encrypt feedback
            feedbackP1 = Arrays.copyOfRange(feedback, 0, partSize);         // Leftmost R-Bytes of feedback
            feedbackP2 = Arrays.copyOfRange(feedback, partSize, blockSize); // Rightmost (blockSize-R)-Bytes of feedback
            IdeaUtils.xor(block[i], 0, feedbackP1, partSize);              // XOR part of data and feecback
            if (this.encrypt) {
                feedback = IdeaUtils.concat2Bytes(feedbackP2, block[i]);   // Update feedback with the new cipherblock
            } else {
                feedback = IdeaUtils.concat2Bytes(feedbackP2, crypt[i]);   // Update feedback with the cipherblock saved
            }
        }
        // Merge results
        for (int i = 0; i < rounds; i++) {
            System.arraycopy(block[i], 0, data, pos + partSize * i, partSize);
        }
    }
}
