package rs117.hd.utils.error;

import org.pushingpixels.substance.internal.SubstanceSynapse;
import rs117.hd.utils.error.ui.MaterialTab;
import rs117.hd.utils.error.ui.MaterialTabGroup;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ErrorScreen extends JPanel {

    String error = "";

    /* The orange color used for the branding's accents */
    public static final Color BRAND_ORANGE = new Color(220, 138, 0);

    /* The orange color used for the branding's accents, with lowered opacity */
    public static final Color BRAND_ORANGE_TRANSPARENT = new Color(220, 138, 0, 120);

    public static final Color DARKER_GRAY_COLOR = new Color(30, 30, 30);
    public static final Color DARK_GRAY_COLOR = new Color(40, 40, 40);
    public static final Color MEDIUM_GRAY_COLOR = new Color(77, 77, 77);
    public static final Color LIGHT_GRAY_COLOR = new Color(165, 165, 165);

    public static final Color DARKER_GRAY_HOVER_COLOR = new Color(60, 60, 60);
    public static final Color DARK_GRAY_HOVER_COLOR = new Color(35, 35, 35);

    /* The color for the green progress bar (used in ge offers, farming tracker, etc)*/
    public static final Color PROGRESS_COMPLETE_COLOR = new Color(55, 240, 70);

    /* The color for the red progress bar (used in ge offers, farming tracker, etc)*/
    public static final Color PROGRESS_ERROR_COLOR = new Color(230, 30, 30);

    /* The color for the orange progress bar (used in ge offers, farming tracker, etc)*/
    public static final Color PROGRESS_INPROGRESS_COLOR = new Color(230, 150, 30);

    /* The color for the price indicator in the ge search results */
    public static final Color GRAND_EXCHANGE_PRICE = new Color(110, 225, 110);

    /* The color for the high alch indicator in the ge search results */
    public static final Color GRAND_EXCHANGE_ALCH = new Color(240, 207, 123);

    /* The color for the limit indicator in the ge search results */
    public static final Color GRAND_EXCHANGE_LIMIT = new Color(50, 160, 250);

    /* The background color of the scrollbar's track */
    public static final Color SCROLL_TRACK_COLOR = new Color(25, 25, 25);

    private final JPanel rightColumn = new JPanel();
    private final Font font = new Font(Font.DIALOG, Font.PLAIN, 12);
    private final JLabel title;


    //private final MaterialTabGroup tabGroup = new MaterialTabGroup(this);


    public static void open(String error) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("117 HD Crash");
            frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    frame.setVisible(false);
                }
            });

            frame.setBackground(new Color(40, 40, 40));
            frame.getContentPane().add(new ErrorScreen(error));
            frame.setSize(672, 386);
            frame.setBackground(new Color(40, 40, 40));

            frame.setVisible(true);
        });

    }

    public ErrorScreen(String error) {
        this.error = error;

        setLayout(new BorderLayout());

        setBackground(DARKER_GRAY_COLOR);
        putClientProperty(SubstanceSynapse.COLORIZATION_FACTOR, 1.0);

        JPanel leftPane = new JPanel();
        leftPane.setBackground(DARKER_GRAY_COLOR);
        leftPane.setLayout(new BorderLayout());

        title = new JLabel(error);
        title.setForeground(Color.WHITE);
        title.setFont(font.deriveFont(16.f));
        title.setBorder(new EmptyBorder(10, 10, 10, 10));
        leftPane.add(title, BorderLayout.NORTH);

        leftPane.setPreferredSize(new Dimension(400, 200));

        String possibleFixes = "dfdf";
        if(error.contains("OpenGL 4.3 is required but not available")) {
            possibleFixes = "Get Good";
        }

        JTextArea textArea = new JTextArea(possibleFixes);
        textArea.setFont(font);
        textArea.setBackground(DARKER_GRAY_COLOR);
        textArea.setForeground(Color.LIGHT_GRAY);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        textArea.setEditable(false);
        textArea.setOpaque(false);
        leftPane.add(textArea, BorderLayout.CENTER);


        leftPane.setPreferredSize(new Dimension(400, 200));
        MaterialTabGroup tabGroup = new MaterialTabGroup(leftPane);
        MaterialTab offersTab = new MaterialTab("General", tabGroup,textArea);
        MaterialTab searchTab = new MaterialTab("Stack Trace", tabGroup, createPane("General2"));

        tabGroup.setBorder(new EmptyBorder(5, 0, 0, 0));
        tabGroup.addTab(offersTab);
        tabGroup.addTab(searchTab);
        tabGroup.select(offersTab); // selects the default selected tab

        add(tabGroup, BorderLayout.CENTER);

        rightColumn.setLayout(new BoxLayout(rightColumn, BoxLayout.Y_AXIS));
        rightColumn.setBackground(DARK_GRAY_COLOR);
        rightColumn.setMaximumSize(new Dimension(200, Integer.MAX_VALUE));

        rightColumn.add(addButton("Send Report",() -> System.out.println("")), BorderLayout.SOUTH);
        rightColumn.add(addButton("Discord",() -> System.out.println("")), BorderLayout.SOUTH);
        rightColumn.add(addButton("Github Issues",() -> System.out.println("")), BorderLayout.SOUTH);
        rightColumn.add(addButton("Logs",() -> System.out.println("")), BorderLayout.SOUTH);

        add(rightColumn, BorderLayout.EAST);
    }

    public JButton addButton(String message, Runnable action)
    {
        JButton button = new JButton(message);
        button.addActionListener(e -> action.run());
        button.setFont(font);
        button.setBackground(DARK_GRAY_COLOR);
        button.setForeground(Color.LIGHT_GRAY);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, DARK_GRAY_COLOR.brighter()),
                new EmptyBorder(4, 4, 4, 4)
        ));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        button.setFocusPainted(false);
        button.addChangeListener(ev ->
        {
            if (button.getModel().isPressed())
            {
                button.setBackground(DARKER_GRAY_COLOR);
            }
            else if (button.getModel().isRollover())
            {
                button.setBackground(DARK_GRAY_HOVER_COLOR);
            }
            else
            {
                button.setBackground(DARK_GRAY_COLOR);
            }
        });


        return button;
    }

    JPanel createPane(final String s) {
        JPanel p = new JPanel();
        p.add(new JLabel(s));
        return p;
    }

}

