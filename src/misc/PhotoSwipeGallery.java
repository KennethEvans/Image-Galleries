package misc;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.imgscalr.Scalr;

/*
 * Created on Jan 2, 2019
 * By Kenneth Evans, Jr.
 */

public class PhotoSwipeGallery extends JFrame
{
    private static final long serialVersionUID = 1L;
    public static final String LS = System.getProperty("line.separator");
    private static final boolean DO_NOT_OVERWRITE = true;
    private static final String DEFAULT_RESCALE_EXT = ".jpg";
    private static final String DEFAULT_RESCALE_SUFFIX = "_M";
    private static final String RESCALE_SUFFIX = DEFAULT_RESCALE_SUFFIX
        + DEFAULT_RESCALE_EXT;
    private static final String[] IMAGE_FILE_EXTENSIONS = new String[] {"jpg",
        "jpeg", "png", "gif"};
    /** Directory from which the script is being run. */
    private static final String DEFAULT_PARENT_DIR = "C:/Users/evans/Documents/Web Pages/kenevans.net/Digital Art";
    /*
     * private static final String DEFAULT_PARENT_DIR =
     * "C:/Scratch/AAA/Image Gallery Test/Test Site/gallery";
     */
    /** Directory where the images are. */
    private static final String DEFAULT_DIR = DEFAULT_PARENT_DIR + "/images";
    private static final int RESCALE_SIZE = 200;
    private static double THUMBNAIL_RESIZE_FACTOR = .4;

    private static final int WIDTH = 400;
    private static final int HEIGHT = 200;
    private static final int NCOLS = 4;
    private static final String TITLE = "PhotoSwipe Gallery";
    private static List<Thumbnail> thumbnails = new ArrayList<Thumbnail>();

    private JPanel mainPanel;
    private JMenuBar menuBar;
    private JScrollPane scrollPane;

    /**
     * PhotoSwipeGallery constructor.
     */
    public PhotoSwipeGallery() {
        // loadUserPreferences();
        uiInit();
    }

    /**
     * Initializes the user interface.
     */
    void uiInit() {
        // JFrame.add() and JFrame.getContentPane().add() both
        // do the same thing - JFrame.add() is overridden to call
        // JFrame.getContentPane().add(). As of 1.5.

        this.setLayout(new BorderLayout());

        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        JTextField textField = new JTextField("Calculating...");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        mainPanel.add(textField);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.add(scrollPane, BorderLayout.CENTER);

        // mainPanel.setBackground(Color.RED);
        // this.setBackground(Color.BLUE);
        // scrollPane.setBackground(Color.GREEN);
    }

    /**
     * Initializes the menus.
     */
    private void initMenus() {
        JMenuItem menuItem;

        // Menu
        menuBar = new JMenuBar();

        // File
        JMenu menu = new JMenu();
        menu.setText("File");
        menuBar.add(menu);

        // // File Open
        // JMenuItem menuItem = new JMenuItem();
        // menuItem.setText("Open...");
        // menuItem.addActionListener(new ActionListener() {
        // public void actionPerformed(ActionEvent ae) {
        // open();
        // }
        // });
        // menu.add(menuItem);
        //
        // JSeparator separator = new JSeparator();
        // menu.add(separator);

        // File Exit
        menuItem = new JMenuItem();
        menuItem.setText("Exit");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                quit();
            }
        });
        menu.add(menuItem);

        // // Help
        // menu = new JMenu();
        // menu.setText("Help");
        // menuBar.add(menu);

        // menuItem = new JMenuItem();
        // menuItem.setText("Contents");
        // menuItem.addActionListener(new ActionListener() {
        // public void actionPerformed(ActionEvent ae) {
        // try {
        // java.awt.Desktop.getDesktop()
        // .browse(java.net.URI.create(HELP_URL));
        // } catch(IOException ex) {
        // Utils.excMsg("Cannot open help contents", ex);
        // }
        // }
        // });
        // menu.add(menuItem);

        // menuItem = new JMenuItem();
        // menuItem.setText("About");
        // menuItem.addActionListener(new ActionListener() {
        // public void actionPerformed(ActionEvent ae) {
        // JOptionPane.showMessageDialog(null,
        // new AboutBoxPanel(HELP_TITLE, AUTHOR, COMPANY, COPYRIGHT),
        // "About", JOptionPane.PLAIN_MESSAGE);
        // }
        // });
        // menu.add(menuItem);
    }

    /**
     * Quits the application
     */
    private void quit() {
        System.exit(0);
    }

    /**
     * Puts the panel in a JFrame and runs the JFrame.
     */
    public void run() {
        try {
            // Create and set up the window.
            // JFrame.setDefaultLookAndFeelDecorated(true);
            // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            // SwingUtilities.updateComponentTreeUI(this);
            this.setTitle(TITLE);
            this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            // frame.setLocationRelativeTo(null);

            // // Set the icon
            // ImageUtils.setIconImageFromResource(this,
            // "/resources/ICC Profile Viewer.256x256.png");

            // Has to be done here. The menus are not part of the JPanel.
            initMenus();
            this.setJMenuBar(menuBar);
            pack();

            // Display the window
            this.setBounds(20, 20, WIDTH, HEIGHT);
            this.setVisible(true);

            // Get the thumbnails
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    makeThumbnails();
                    loadThumbnails();
                    setCursor(
                        Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            });
        } catch(Throwable t) {
            Utils.excMsg("Error making thumbnails", t);
        }
    }

    private static void makeThumbnails() {
        thumbnails.clear();
        File dir = new File(DEFAULT_DIR);
        File[] files = dir.listFiles();
        BufferedImage bi, biScaled;
        String fileNameNoExt, fileNameOut;
        File fileOut;
        int index = 0;
        String imageType = "jpg";
        boolean skip = true;
        for(File file : files) {
            skip = true;
            if(file.isDirectory()) continue;
            // Don't reprocess thumbnails
            if(file.getName().endsWith(RESCALE_SUFFIX)) {
                index = thumbnails.size();
                continue;
            }
            for(String ext : IMAGE_FILE_EXTENSIONS) {
                if(file.getName().toLowerCase().endsWith(ext)) {
                    // imageType = ext;
                    skip = false;
                    continue;
                }
            }
            if(skip) continue;
            // Is an image file
            System.out.println("Processing " + file.getPath());
            try {
                bi = ImageIO.read(file);
                biScaled = Scalr.resize(bi, RESCALE_SIZE);
                fileNameNoExt = file.getName();
                int pos = fileNameNoExt.lastIndexOf(".");
                if(pos > 0 && pos < (fileNameNoExt.length() - 1)) {
                    // '.' is not the first or last character
                    fileNameNoExt = fileNameNoExt.substring(0, pos);
                }
                fileNameOut = fileNameNoExt + RESCALE_SUFFIX;
                fileOut = new File(DEFAULT_DIR, fileNameOut);
                index = thumbnails.size();
                if(fileOut.exists()) {
                    if(DO_NOT_OVERWRITE) {
                        System.out.println("   Already exists, not converted");
                        thumbnails.add(new Thumbnail(index, fileOut));
                        continue;
                    }
                    int res = JOptionPane.showConfirmDialog(null,
                        "File exists:" + LS + fileOut.getPath() + LS
                            + "OK to overwrite?",
                        "File Exists", JOptionPane.OK_CANCEL_OPTION);
                    if(res != JOptionPane.OK_OPTION) continue;
                }
                ImageIO.write(biScaled, imageType, fileOut);
                thumbnails.add(new Thumbnail(index, fileOut));
                System.out.println("   Converted to " + fileOut.getPath());
                System.out.println(
                    "     " + bi.getWidth() + "x" + bi.getHeight() + "->"
                        + biScaled.getWidth() + "x" + biScaled.getHeight());
            } catch(IOException ex) {
                ex.printStackTrace();
                return;
            }
        }
    }

    private void loadThumbnails() {
        // Clear the JPanel
        Component[] components = mainPanel.getComponents();
        for(Component component : components) {
            mainPanel.remove(component);
        }

        GridBagConstraints gbcDefault = new GridBagConstraints();
        gbcDefault.insets = new Insets(2, 2, 2, 2);
        gbcDefault.weightx = 100;
        gbcDefault.anchor = GridBagConstraints.WEST;
        gbcDefault.fill = GridBagConstraints.HORIZONTAL;
        GridBagConstraints gbc = null;

        // Add the current buttons
        JButton button;
        int row, col;
        for(Thumbnail thumbnail : thumbnails) {
            row = thumbnail.index / NCOLS;
            col = thumbnail.index % NCOLS;
            button = new JButton(thumbnail.icon);
            button.setMargin(new Insets(0, 0, 0, 0));
            gbc = (GridBagConstraints)gbcDefault.clone();
            gbc.gridx = col;
            gbc.gridy = row;
            mainPanel.add(button, gbc);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        final PhotoSwipeGallery app = new PhotoSwipeGallery();

        try {
            // Set window decorations
            JFrame.setDefaultLookAndFeelDecorated(true);

            // Set the native look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Throwable t) {
            Utils.excMsg("Error setting Look & Feel", t);
        }

        // Make the job run in the AWT thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if(app != null) {
                    app.run();
                }
            }
        });
    }

    private static class Thumbnail
    {
        private int index;
        private String name;
        private Icon icon;

        public Thumbnail(int index, File file) {
            this.index = index;
            String name = file.getName();
            String ext = Utils.getExtension(file);
            if(ext != null) {
                name = file.getName().substring(0,
                    name.length() - ext.length());
            }
            try {
                BufferedImage bi = ImageIO.read(file);
                int w = (int)Math
                    .round(THUMBNAIL_RESIZE_FACTOR * bi.getWidth());
                int h = (int)Math
                    .round(THUMBNAIL_RESIZE_FACTOR * bi.getHeight());
                // Rescale it;
                BufferedImage biScaled = resizeImage(bi, w, h);
                icon = new ImageIcon(biScaled);
            } catch(IOException ex) {
                Utils.excMsg("Failed to get thumbnail image for " + index, ex);
            }
        }

        /**
         * Resizes a BufferedImage.
         * 
         * @param bi The original image.
         * @param w The new width.
         * @param h The new height;
         * @return Resized image.
         */
        public static BufferedImage resizeImage(BufferedImage bi, int w,
            int h) {
            BufferedImage biScaled = new BufferedImage(w, h,
                BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = biScaled.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(bi, 0, 0, w, h, null);
            g2d.dispose();
            return biScaled;
        }
    }

}
