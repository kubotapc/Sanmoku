import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.Container;
import java.awt.BorderLayout;

public class UserConsole extends JFrame {
    JTextArea textArea;
    Server server;
    public UserConsole(int id, String name , Server server) {
        this.server = server;
        String idLetter;
        if ( id < 0 ) {
            idLetter = "err!";
        } else {
            idLetter = String.valueOf((char)('A'+id));
        }
        setTitle("("+idLetter+") "+name);
        setBounds(1100,400,300,300);
        // setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        textArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea);

        Container contentPane = getContentPane();
        contentPane.add(scrollPane,BorderLayout.CENTER);
    }
    void addText(Object text) {
        textArea.append(text.toString());
        textArea.setCaretPosition(textArea.getText().length());
    }
    void addTextln(Object text) {
        addText(text.toString()+"\n");
    }
    void clear() {
        textArea.setText("");
    }
    
}
