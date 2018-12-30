package misc;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import javax.swing.JFrame;
import javax.swing.UIManager;

/*
 * Created on Dec 30, 2018
 * By Kenneth Evans, Jr.
 */

public class CopyPhotoTagsToThumbnails
{
    public static final String LS = System.getProperty("line.separator");
    private static final String DEFAULT_RESCALE_EXT = ".jpg";
    private static final String DEFAULT_RESCALE_SUFFIX = "_M";
    /** Directory from which the script is being run. */
    private static final String RESCALE_SUFFIX = DEFAULT_RESCALE_SUFFIX
        + DEFAULT_RESCALE_EXT;
    private static final String[] IMAGE_FILE_EXTENSIONS = new String[] {"jpg",
        "jpeg", "png", "gif"};
    private static final String DEFAULT_PARENT_DIR = "C:/Users/evans/Documents/Web Pages/kenevans.net/Digital Art";
    // private static final String DEFAULT_PARENT_DIR = "C:/Scratch/AAA/Image
    // Gallery Test/Test Site/gallery";
    /** Directory where the images are. */
    private static final String DEFAULT_DIR = DEFAULT_PARENT_DIR + "/images";
    private static final String PHOTO_TAGS_CMD = "C:/bin/EXIFTool/CopyPhotoTagsNoPause.bat";
    private static int nErrors = 0;

    private static void copyPhotoTags() {
        File dir = new File(DEFAULT_DIR);
        File[] files = dir.listFiles();
        String fileNameNoExt, fileNameOut;
        File fileOut;
        boolean skip = true;
        for(File file : files) {
            skip = true;
            if(file.isDirectory()) continue;
            // Don't process thumbnails
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
            System.out.println(LS + "Processing " + file.getPath());
            try {
                fileNameNoExt = file.getName();
                int pos = fileNameNoExt.lastIndexOf(".");
                if(pos > 0 && pos < (fileNameNoExt.length() - 1)) {
                    // '.' is not the first or last character
                    fileNameNoExt = fileNameNoExt.substring(0, pos);
                } else {
                    System.out.println("Bad filename: " + fileNameNoExt);
                    nErrors++;
                    continue;
                }
                fileNameOut = fileNameNoExt + RESCALE_SUFFIX;
                fileOut = new File(DEFAULT_DIR, fileNameOut);
                if(!fileOut.exists()) {
                    System.out.println("Not found: " + fileOut.getPath());
                    continue;
                }
                String cmd = PHOTO_TAGS_CMD + " \"" + file.getPath() + "\" \""
                    + fileOut.getPath() + "\"";
                try {
                    execCmd(cmd);
                } catch(Exception ex) {
                    ex.printStackTrace();
                    return;
                }
                System.out.println("Processed " + fileOut.getPath());
                // DEBUG
                // return;
            } catch(Exception ex) {
                ex.printStackTrace();
                return;
            }
        }
    }

    public static void execCmd(String cmd) throws java.io.IOException {
        Process proc = Runtime.getRuntime().exec(cmd);
        BufferedReader stdInput = new BufferedReader(
            new InputStreamReader(proc.getInputStream()));
        BufferedReader stdError = new BufferedReader(
            new InputStreamReader(proc.getErrorStream()));
        System.out.println("Stdout:\n");
        String s = null;
        while((s = stdInput.readLine()) != null) {
            System.out.println(s);
            if(s.endsWith("Aborting")) nErrors++;
        }
        // read any errors from the attempted command
        System.out.println("Stderr:");
        while((s = stdError.readLine()) != null) {
            System.out.println(s);
        }
    }

    public static void main(String[] args) {
        System.out.println("Processing: " + DEFAULT_DIR);
        try {
            // Set window decorations
            JFrame.setDefaultLookAndFeelDecorated(true);
            // Set the native look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Throwable t) {
            t.printStackTrace();
        }

        copyPhotoTags();
        System.out.println();
        System.out.println("nErrors=" + nErrors);
        System.out.println("All done");
    }

}
