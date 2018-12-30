package misc;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.imgscalr.Scalr;

/*
 * Created on Dec 28, 2018
 * By Kenneth Evans, Jr.
 */

/**
 * MakeThumbnails makes thumbnails for images in a directory.
 * 
 * @author Kenneth Evans, Jr.
 */
public class MakeThumbnails
{
    public static final String LS = System.getProperty("line.separator");
    private static final boolean DO_NOT_OVERWRITE = true;
    private static final String DEFAULT_RESCALE_EXT = ".jpg";
    private static final String DEFAULT_RESCALE_SUFFIX = "_M";
    private static final String RESCALE_SUFFIX = DEFAULT_RESCALE_SUFFIX
        + DEFAULT_RESCALE_EXT;
    private static final String[] IMAGE_FILE_EXTENSIONS = new String[] {"jpg",
        "jpeg", "png", "gif"};
    /** Directory from which the script is being run. */
    private static final String DEFAULT_PARENT_DIR = "C:/Scratch/AAA/Image Gallery Test/Test Site/gallery";
    /** Directory where the images are. */
    private static final String DEFAULT_DIR = DEFAULT_PARENT_DIR + "/images";
    private static final int RESCALE_SIZE = 200;

    private static void makeThumbnails() {
        File dir = new File(DEFAULT_DIR);
        File[] files = dir.listFiles();
        BufferedImage bi, biScaled;
        String fileNameNoExt, fileNameOut;
        File fileOut;
        String imageType = "jpg";
        boolean skip = true;
        for(File file : files) {
            skip = true;
            if(file.isDirectory()) continue;
            // Don't reprocess thumbnails
            if(file.getName().endsWith(RESCALE_SUFFIX)) continue;
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
                if(fileOut.exists()) {
                    if(DO_NOT_OVERWRITE) {
                        System.out.println("   Already exists, not converted");
                        continue;
                    }
                    int res = JOptionPane.showConfirmDialog(null,
                        "File exists:" + LS + fileOut.getPath() + LS
                            + "OK to overwrite?",
                        "File Exists", JOptionPane.OK_CANCEL_OPTION);
                    if(res != JOptionPane.OK_OPTION) continue;
                }
                ImageIO.write(biScaled, imageType, fileOut);
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

    public static void main(String[] args) {
        try {
            // Set window decorations
            JFrame.setDefaultLookAndFeelDecorated(true);
            // Set the native look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Throwable t) {
            t.printStackTrace();
        }

        makeThumbnails();
        System.out.println();
        System.out.println("All done");
    }

}
