package mandrik.security.notepad.client;

import java.security.KeyPair;
import java.util.Arrays;
import java.util.Scanner;

public class NotepadMenu implements INotepadMessages {

    public void showMenu() {
        System.out.println("*************************************************");
        System.out.println("1. " + GENERATE_SESSION_KEY);
        System.out.println("2. " + GENERATE_RSA_KEY);
        System.out.println("3. " + GET_SESSION_KEY);
        System.out.println("4. " + DOWNLOAD_FILE);
        System.out.println("5. " + DECODE_AND_SHOW_FILE);
        System.out.println("6. " + EXIT);
        System.out.println("*************************************************\n");
    }

    public int inCommand() {
        System.out.print("command > ");
        Scanner in = new Scanner(System.in);
        return in.nextInt();
    }

    public void executeCommand(NotepadClientCommands clientCommands) {
        int numCommand = inCommand();
        System.out.print("result: ");
        switch(numCommand) {
            case 1:
                clientCommands.requestToGenerateSessionKey();
                break;
            case 2:
                KeyPair keyPair = clientCommands.generateRSAKeyPair();
                clientCommands.sendPublicRSAKey(keyPair.getPublic());
                break;
            case 3:
                try {
                    byte[] encodedSessionKey = clientCommands.getEncodedSessionKey();
                    System.out.println(Arrays.toString(encodedSessionKey));
                    byte[] sessionKey = clientCommands.decryptSessionKey(encodedSessionKey);
                    System.out.println(Arrays.toString(sessionKey));
                } catch (NotepadServerException e) {
                    System.out.println("error");
                }
                break;
            case 4:
                System.out.print("file name > ");
                Scanner in = new Scanner(System.in);
                clientCommands.downloadFile(in.nextLine());
                break;
            case 5:
                clientCommands.decryptFile();
                break;
            case 6:
                System.out.println("success");
                System.exit(0);
        }
        System.out.println();
    }
}
