package jsscCode;

import javax.swing.JApplet;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author scream3r
 */
public class Main extends JApplet {

    private static JApplet applet;

    static JApplet getApplet() {
        return applet;
    }

    @Override
    public void init() {
        try {
           // UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    Form form = new Form();
                    add(form);
                    applet = Main.this;
                }
            });
        }
        catch (Exception e) {
            //Do nothing
        }
    }
}

