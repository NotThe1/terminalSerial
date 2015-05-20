package jsscCode;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

/**
 *
 * @author scream3r
 */
public class NimbusGui {

    public static final Font DEFAULT_FONT = new Font("Tahoma", 0, 11);

    public static final Font INFO_PANEL_FONT = new Font("Tahoma", 1, 18);
    public static final Color INFO_PANEL_FONT_COLOR = new Color(47, 76, 102);
    public static final Color INFO_PANEL_TOP_COLOR = new Color(183, 203, 216);
    public static final Color INFO_PANEL_BOTTOM_COLOR = new Color(156, 183, 204);
    public static final CompoundBorder INFO_PANEL_BORDER = BorderFactory.createCompoundBorder(
                                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(244, 248, 255)),
                                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(141, 156, 166)));

    public static final Color WORK_PANEL_BG_COLOR = new Color(199, 202, 208);
    public static final Color DATA_PANEL_BG_COLOR = new Color(231,233,237);
    public static final CompoundBorder WORK_AND_DATA_PANEL_BORDER = BorderFactory.createCompoundBorder(
                                        BorderFactory.createMatteBorder(1, 1, 0, 0, new Color(244, 248, 255)),
                                        BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(141, 156, 166)));

    public static final CompoundBorder DIALOG_PANEL_BORDER = BorderFactory.createCompoundBorder(
                                        BorderFactory.createMatteBorder(1, 1, 0, 0, new Color(244, 248, 255)),
                                        BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(141, 156, 166)));

    public static final Font SECTION_LABEL_FONT = new Font("Tahoma", 1, 17);
    public static final Color SECTION_LABEL_FONT_COLOR = new Color(47, 76, 102);

    public static final Color DATA_LABEL_BORDER_COLOR = new Color(156, 183, 204);

    static Border borderStatusOn = javax.swing.BorderFactory.createLineBorder(new Color(96,168,74));
    static Border borderStatusOff = javax.swing.BorderFactory.createLineBorder(new Color(234,90,94));

    static Color colorStatusOnBG = new Color(218,247,189);
    static Color colorStatusOffBG = new Color(255,180,180);

    static Color colorDialogBG = new Color(218,247,189, 25);
}
