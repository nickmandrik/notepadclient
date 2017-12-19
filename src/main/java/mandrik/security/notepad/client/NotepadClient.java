package mandrik.security.notepad.client;

public class NotepadClient implements INotepadUrls {

    public static void main(String[] args) {
        NotepadMenu menu = new NotepadMenu();
        NotepadClientCommands clientCommands = new NotepadClientCommands();
        while(true) {
            menu.showMenu();
            menu.executeCommand(clientCommands);
        }
    }
}
