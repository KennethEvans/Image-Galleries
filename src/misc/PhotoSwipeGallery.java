package misc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.imgscalr.Scalr;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/*
 * Created on Jan 2, 2019
 * By Kenneth Evans, Jr.
 */

public class PhotoSwipeGallery extends JFrame
    implements MouseListener, MouseMotionListener
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
    private static final int RESCALE_SIZE = 200;
    private static double THUMBNAIL_RESIZE_FACTOR = .4;

    private static boolean USE_WEB_SITE = true;

    /** Directory from which the script is being run. */
    private static final String DEFAULT_PARENT_DIR = USE_WEB_SITE
        ? "C:/Users/evans/Documents/Web Pages/kenevans.net/DigitalArt"
        : "C:/Scratch/AAA/Image Gallery Test/Test Site/gallery";

    /** Directory where the images are. */
    private static final String DEFAULT_DIR = DEFAULT_PARENT_DIR + "/images";

    private static final int WIDTH = 400;
    private static final int HEIGHT = 200;
    private static final int NCOLS = 4;
    private static final String TITLE = "PhotoSwipe Gallery";
    private static List<Thumbnail> thumbnails = new ArrayList<Thumbnail>();
    private static String currentDir = DEFAULT_PARENT_DIR;
    public static final SimpleDateFormat fileFormatter = new SimpleDateFormat(
        "yyyy-MM-dd-hh-mm");

    private boolean mouseMoving = false;
    private boolean mouseDown = false;

    private JPanel mainPanel;
    private JMenuBar menuBar;
    private JScrollPane scrollPane;

    private Item[] savedItems;

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

        scrollPane = new JScrollPane(mainPanel);
        scrollPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        // Scrollbars don't work with NORTH
        // Gets centered as size increases with CENTER (not that bad)
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

        // Reset Order
        menuItem = new JMenuItem();
        menuItem.setText("Reset Order");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                reorder();
            }
        });
        menu.add(menuItem);

        // Write JSON
        menuItem = new JMenuItem();
        menuItem.setText("Write JSON File...");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                writeJson();
            }
        });
        menu.add(menuItem);

        // Set items from JSON
        menuItem = new JMenuItem();
        menuItem.setText("Reset Items from JSON File...");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                resetFromJson();
            }
        });
        menu.add(menuItem);

        JSeparator separator = new JSeparator();
        menu.add(separator);

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

    @Override
    public void mouseClicked(MouseEvent ev) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mousePressed(MouseEvent ev) {
        // System.out.println("mousePressed: mouseDown=" + mouseDown
        // + " mouseMoving=" + mouseMoving);
        Component component = ev.getComponent();
        if(component instanceof DataButton) {
            mouseDown = true;
        } else {
            mouseDown = false;
            mouseMoving = false;
        }
    }

    @Override
    public void mouseReleased(MouseEvent ev) {
        // System.out.println("mouseReleased: mouseDown=" + mouseDown
        // + " mouseMoving=" + mouseMoving);
        // The ev.getComponent() is the originating component
        Component component = ev.getComponent();
        if(mouseMoving && component instanceof DataButton) {
            DataButton button = (DataButton)component;
            // Find the component where released
            Component upComponent = null;
            Component[] components = mainPanel.getComponents();
            Point point = new Point(SwingUtilities.convertPoint(component,
                ev.getPoint(), mainPanel));
            for(Component component1 : components) {
                if(component1.getBounds().contains(point)) {
                    upComponent = component1;
                    break;
                }
            }
            if(upComponent != null && upComponent instanceof DataButton) {
                DataButton upButton = (DataButton)upComponent;
                reconfigure(button.index, upButton.index);
            }
            mouseDown = false;
            mouseMoving = false;
        }

    }

    @Override
    public void mouseEntered(MouseEvent ev) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent ev) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseDragged(MouseEvent ev) {
        // System.out.println("mouseDragged: mouseDown=" + mouseDown
        // + " mouseMoving=" + mouseMoving);
        if(mouseDown) mouseMoving = true;
    }

    @Override
    public void mouseMoved(MouseEvent ev) {
        // System.out.println(
        // "mouseMoved: mouseDown=" + mouseDown + " mouseMoving=" +
        // mouseMoving);
        // if(mouseDown) mouseMoving = true;
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

    private static String getJsonForItems() {
        List<Item> items = new ArrayList<Item>();
        for(Thumbnail thumbnail : thumbnails) {
            items.add(thumbnail.item);
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(items);
    }

    private static Item[] getItemsFromJson(String json) {
        Gson gson = new Gson();
        Item[] items = gson.fromJson(json, Item[].class);
        return items;
    }

    /**
     * Reorders the thumbnails from the saved items.
     */
    private void reorder() {
        reorder(savedItems);
    }

    /**
     * Reorders the thumbnails from the given items.
     * 
     * @param items
     */
    private void reorder(Item[] items) {
        if(items == null || items.length == 0) {
            Utils.warnMsg("There are no items for reordering");
            return;
        }
        int indexI = 0;
        int indexT = 0;
        try {
            // Reorder
            for(Item item : items) {
                indexT = 0;
                if(item == null) {
                    Utils.errMsg("null item at indexI=" + indexI + " of "
                        + items.length + " items" + LS
                        + "(Check there is no comma after the last JSON element)");
                    continue;
                }
                // Loop over thumbnails looking for a match of title to name;
                for(Thumbnail thumbnail : thumbnails) {
                    if(item.title == null) {
                        Utils.errMsg("item.title = null for index=" + indexT);
                        indexT++;
                        continue;
                    }
                    if(thumbnail.name == null) {
                        Utils.errMsg(
                            "thumbnail.name = null for index=" + indexT);
                        indexT++;
                        continue;
                    }
                    if(item.title.equals(thumbnail.name)) {
                        // Move it to the end
                        // (Should leave unmatched ones at the beginning)
                        Collections.rotate(
                            thumbnails.subList(indexT, thumbnails.size()), -1);
                    }
                    indexT++;
                }
                indexI++;
            }
            // Reset the indices
            int i = 0;
            for(Thumbnail thumbnail : thumbnails) {
                thumbnail.index = i++;
            }
        } catch(Exception ex) {
            Utils.excMsg("Error processing reorder at indexI=" + indexI
                + " indexT=" + indexT, ex);
            return;
        }
        try {
            // System.out.println("After resetting the indices:");
            // for(Thumbnail thumbnail : thumbnails) {
            // System.out
            // .println(" " + thumbnail.name + " (" + thumbnail.index + ")");
            // }
            loadThumbnails();
        } catch(Exception ex) {
            Utils.excMsg("Error loading thumbnails", ex);
            return;
        }
    }

    /**
     * Reorders the thumbnails based on the items in the given File.
     * 
     * @param jsonFile
     */
    private void reorderThumbnails(File jsonFile) {
        BufferedReader in = null;
        Item[] items = null;
        try {
            in = new BufferedReader(new FileReader(jsonFile));
            Gson gson = new Gson();
            items = gson.fromJson(in, Item[].class);
        } catch(FileNotFoundException ex) {
            Utils.excMsg("Could not parse " + jsonFile.getPath(), ex);
            return;
        } finally {
            try {
                if(in != null) in.close();
            } catch(Exception ex) {
                // Do nothing
            }
        }
        reorder(items);
    }

    /**
     * Prompts for a file of items to use in reordering the thumbnails. Calls
     * reorder(File) to do the reordering if not cancelled.
     */
    private void resetFromJson() {
        // Prompt for file
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON",
            "json");
        chooser.setFileFilter(filter);
        chooser.setDialogTitle("Open JSON");
        if(currentDir != null) {
            File file = new File(currentDir);
            if(file != null && file.exists()) {
                chooser.setCurrentDirectory(file);
            }
        }
        int result = chooser.showOpenDialog(this);
        if(result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            // Save the selected path for next time
            currentDir = chooser.getSelectedFile().getParentFile().getPath();
            reorderThumbnails(file);
        }
    }

    private void writeJson() {
        System.out.println();
        System.out.println("JSON items for PhotoSwipe:");
        String json = getJsonForItems();
        System.out.println("var items =" + LS + json);
        // Prompt to save
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON",
            "json");
        chooser.setFileFilter(filter);
        chooser.setDialogTitle("Save JSON");
        if(currentDir != null) {
            File file = new File(currentDir);
            if(file != null && file.exists()) {
                chooser.setCurrentDirectory(file);
            }
        }
        chooser.setSelectedFile(
            new File("Items-" + fileFormatter.format(new Date()) + ".json"));
        int result = chooser.showSaveDialog(this);
        if(result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            // Save the selected path for next time
            currentDir = chooser.getSelectedFile().getParentFile().getPath();
            PrintWriter out = null;
            try {
                out = new PrintWriter(new FileWriter(file));
                out.write(json);
                out.close();
                System.out.println();
                System.out.println("Wrote " + file.getPath());
            } catch(Exception ex) {
                Utils.excMsg("Error writing " + file.getPath(), ex);
            } finally {
                if(out != null) out.close();
            }
        }
    }

    /**
     * Reads the DEFAULT_DIR directory and makes thumbnails. If there is no
     * thumbnail file yet, it makes those.<br>
     * <br>
     * 
     * Image types may be any of the IMAGE_FILE_EXTENSIONS. Thumbnails are .jpg.
     */
    private void makeThumbnails() {
        thumbnails.clear();
        List<Item> itemsList = new ArrayList<Item>();
        File dir = new File(DEFAULT_DIR);
        if(!dir.exists()) {
            Utils.errMsg("Does not exist " + dir.getPath());
            return;
        }
        File[] files = dir.listFiles();
        BufferedImage bi, biScaled;
        String fileNameNoExt, fileNameOut;
        File fileOut;
        int index = 0;
        String imageType = "jpg";
        boolean skip = true;
        Thumbnail thumbnail;
        Item item;
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
                        thumbnail = new Thumbnail(index, fileOut,
                            fileNameNoExt);
                        item = new Item();
                        item.src = new File(DEFAULT_PARENT_DIR).toURI()
                            .relativize(file.toURI()).getPath();
                        item.h = bi.getHeight();
                        item.w = bi.getWidth();
                        item.title = fileNameNoExt;
                        thumbnail.item = item;
                        itemsList.add(item);
                        thumbnails.add(thumbnail);
                        continue;
                    }
                    int res = JOptionPane.showConfirmDialog(null,
                        "File exists:" + LS + fileOut.getPath() + LS
                            + "OK to overwrite?",
                        "File Exists", JOptionPane.OK_CANCEL_OPTION);
                    if(res != JOptionPane.OK_OPTION) continue;
                }
                ImageIO.write(biScaled, imageType, fileOut);
                thumbnail = new Thumbnail(index, fileOut, fileNameNoExt);
                item = new Item();
                item.src = new File(DEFAULT_PARENT_DIR).toURI()
                    .relativize(file.toURI()).getPath();
                item.h = bi.getHeight();
                item.w = bi.getWidth();
                item.title = fileNameNoExt;
                thumbnail.item = item;
                itemsList.add(item);
                thumbnails.add(thumbnail);
                System.out.println("   Converted to " + fileOut.getPath());
                System.out.println(
                    "     " + bi.getWidth() + "x" + bi.getHeight() + "->"
                        + biScaled.getWidth() + "x" + biScaled.getHeight());
            } catch(IOException ex) {
                Utils.excMsg("Error making thumbnails", ex);
                return;
            }
        }
        savedItems = new Item[itemsList.size()];
        savedItems = itemsList.toArray(savedItems);
    }

    private void reconfigure(int index1, int index2) {
        // Utils.infoMsg("Dragged from " + index1 + " to " + index2);
        // System.out.println("Before:");
        // for(Thumbnail thumbnail : thumbnails) {
        // System.out.println(" " + thumbnail.name);
        // }
        if(index1 <= index2) {
            Collections.rotate(thumbnails.subList(index1, index2 + 1), -1);
        } else {
            Collections.rotate(thumbnails.subList(index2, index1 + 1), 1);
        }
        // System.out.println("After:");
        // for(Thumbnail thumbnail : thumbnails) {
        // System.out
        // .println(" " + thumbnail.name + " (" + thumbnail.index + ")");
        // }
        // Reset the indices
        int i = 0;
        for(Thumbnail thumbnail : thumbnails) {
            thumbnail.index = i++;
        }
        // System.out.println("After resetting the indices:");
        // for(Thumbnail thumbnail : thumbnails) {
        // System.out
        // .println(" " + thumbnail.name + " (" + thumbnail.index + ")");
        // }
        loadThumbnails();
    }

    private void loadThumbnails() {
        // Clear the JPanel
        mainPanel.removeAll();

        GridBagConstraints gbcDefault = new GridBagConstraints();
        gbcDefault.insets = new Insets(2, 2, 2, 2);
        gbcDefault.weightx = 100;
        gbcDefault.anchor = GridBagConstraints.WEST;
        gbcDefault.fill = GridBagConstraints.HORIZONTAL;
        GridBagConstraints gbc = null;

        // Add the current buttons
        DataButton button;
        int row, col;
        int i = 0;
        for(Thumbnail thumbnail : thumbnails) {
            row = thumbnail.index / NCOLS;
            col = thumbnail.index % NCOLS;
            button = new DataButton(thumbnail.icon, i);
            button.setMargin(new Insets(0, 0, 0, 0));
            button.addMouseListener(this);
            button.addMouseMotionListener(this);
            gbc = (GridBagConstraints)gbcDefault.clone();
            gbc = (GridBagConstraints)gbcDefault.clone();
            gbc.gridx = col;
            gbc.gridy = row;
            mainPanel.add(button, gbc);
            i++;
        }
        // Seems to minimum necessary
        mainPanel.revalidate();
        mainPanel.repaint();
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

    private static class Item
    {
        String src;
        int w;
        int h;
        String title;
    }

    private class DataButton extends JButton
    {
        private static final long serialVersionUID = 1L;
        private int index;

        DataButton(Icon icon, int index) {
            super(icon);
            this.index = index;
        }
    }

    private static class Thumbnail
    {
        private int index;
        private String name;
        private Icon icon;
        private Item item;

        private static final int TEXT_HEIGHT = 10;

        public Thumbnail(int index, File file, String name) {
            this.index = index;
            this.name = name;
            String ext = Utils.getExtension(file);
            try {
                BufferedImage bi = ImageIO.read(file);
                int w = (int)Math
                    .round(THUMBNAIL_RESIZE_FACTOR * bi.getWidth());
                int h = (int)Math
                    .round(THUMBNAIL_RESIZE_FACTOR * bi.getHeight());
                // Rescale it;
                BufferedImage biScaled = resizeImage(bi, w, h);
                // WHITE or BLACK works
                drawTextOnImage(biScaled, name, Color.WHITE);
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

        public static void drawTextOnImage(BufferedImage bi, String text,
            Color color) {

            Graphics2D g = bi.createGraphics();
            g.setColor(color);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setFont(new Font("default", Font.BOLD, 10));

            int totalWidth = bi.getWidth();
            int y = TEXT_HEIGHT / 2;
            String[] words = text.split("\\s");
            for(int i = 0; i < words.length; i++) {
                int textWidth = g.getFontMetrics().stringWidth(words[i]);
                int x = (totalWidth - textWidth) / 2;
                g.drawString(words[i], x, y += TEXT_HEIGHT);
            }
            g.dispose();
        }
    }
}
