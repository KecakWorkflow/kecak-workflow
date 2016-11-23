package org.joget.commons.util;

import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URLDecoder;

import javax.imageio.ImageIO;

import org.springframework.web.multipart.MultipartFile;

/**
 * Utility methods used by the system to manager temporary files
 * 
 */
public class FileManager {
	
    public final static Integer THUMBNAIL_SIZE = 60; 
    public final static String THUMBNAIL_EXT = ".thumb.jpg"; 
    
    /**
     * Gets directory path to temporary files folder
     * @return 
     */
    public static String getBaseDirectory() {
    	String basePath = SetupManager.getBaseDirectory();
        String dataFileBasePath = SetupManager.getSettingValue("dataFileBasePath");
        if (dataFileBasePath != null && dataFileBasePath.length() > 0) {
        	basePath = dataFileBasePath;
        }
        return basePath + File.separator + "app_tempfile" + File.separator;
    }
    
    /**
     * Stores files post to the HTTP request to temporary files folder
     * @param file
     * @return the relative path of the stored temporary file, NULL if failure.
     */
    @SuppressWarnings("finally")
	public static String storeFile(MultipartFile file) {
        if (file != null && !file.getOriginalFilename().isEmpty()) {
            String id = UuidGenerator.getInstance().getUuid();
            FileOutputStream out = null;
            String path =  id + File.separator;
            String filename = path;
            try {
                filename += URLDecoder.decode(file.getOriginalFilename(), "UTF-8");
                File uploadFile = new File(getBaseDirectory(), filename);
                if (!uploadFile.isDirectory()) {
                    //create directories if not exist
                    new File(getBaseDirectory(), path).mkdirs();

                    // write file
                    out = new FileOutputStream(uploadFile);
                    out.write(file.getBytes());
                }
            } catch (Exception ex) {
                LogUtil.error(FileManager.class.getName(), ex, "");
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (Exception ex) {
                    }
                }
                return filename;
            }
        }
        return null;
    }
    
    @SuppressWarnings("finally")
	public static String storeFile(MultipartFile file, String basePath) {
        if (file != null && !file.getOriginalFilename().isEmpty()) {
        	basePath = basePath + File.separator + "app_tempfile" + File.separator;
        	
            String id = UuidGenerator.getInstance().getUuid(); 
            FileOutputStream out = null;
            String path =  id + File.separator;
            String filename = path;
            try {
                filename += URLDecoder.decode(file.getOriginalFilename(), "UTF-8");
                File uploadFile = new File(basePath, filename);
                if (!uploadFile.isDirectory()) {
                    //create directories if not exist
                    new File(basePath, path).mkdirs();

                    // write file
                    out = new FileOutputStream(uploadFile);
                    out.write(file.getBytes());
                }
            } catch (Exception ex) {
                LogUtil.error(FileManager.class.getName(), ex, "");
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (Exception ex) {
                    }
                }
                return filename;
            }
        }
        return null;
    }
    
    /**
     * Gets the temporary file from temporary files folder by relative path
     * @param path
     * @return 
     */
    public static File getFileByPath(String path) {
        if (path != null) {
            try {
                File file = new File(getBaseDirectory(), URLDecoder.decode(path, "UTF-8"));
                if (file.exists() && !file.isDirectory()) {
                    return file;
                }
            } catch (Exception e) {}
        }
        return null;
    }
    
    /**
     * Deletes the temporary file from temporary files folder by relative path
     * @param path 
     */
    public static void deleteFileByPath(String path) {
        File file = getFileByPath(path);
        File directory = file.getParentFile();
        
        if (file != null && file.exists()) {
            file.delete();
        }

        if (directory != null && directory.exists()) {
            directory.delete();
        }
    }
    
    /**
     * Deletes a file
     * @param file 
     */
    public static void deleteFile(File file) {
        if (!file.exists()) {
            return;
        }
        
        if (file.isDirectory()){
            for (File f : file.listFiles()){
                deleteFile(f);
            }
        }
        file.delete();
    }
    
    /**
     * Generates a thumbnail of a image file in temporary files folder by relative path
     * @param path
     * @param thumbWidth
     * @param thumbHeight 
     */
    public static void createThumbnail(String path, Integer thumbWidth, Integer thumbHeight) {
        if (thumbWidth == null) {
            thumbWidth = THUMBNAIL_SIZE;
        }
        if (thumbHeight == null) {
            thumbHeight = THUMBNAIL_SIZE;
        }
        
        BufferedOutputStream out = null;

        try{
            File imageFile = new File(getBaseDirectory(), URLDecoder.decode(path, "UTF-8"));
            Image image = Toolkit.getDefaultToolkit().getImage(imageFile.getAbsolutePath());
            MediaTracker mediaTracker = new MediaTracker(new Container());
            mediaTracker.addImage(image, 0);
            mediaTracker.waitForID(0);

            double thumbRatio = (double) thumbWidth / (double) thumbHeight;
            int imageWidth = image.getWidth(null);
            int imageHeight = image.getHeight(null);
            double imageRatio = (double) imageWidth / (double) imageHeight;
            if (thumbRatio < imageRatio) {
                thumbHeight = (int) (thumbWidth / imageRatio);
            } else {
                thumbWidth = (int) (thumbHeight * imageRatio);
            }

            BufferedImage thumbImage = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics2D = thumbImage.createGraphics();
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);
            
            out = new BufferedOutputStream(new FileOutputStream(imageFile.getAbsolutePath() + THUMBNAIL_EXT));
            ImageIO.write(thumbImage, "jpeg", out);

            out.flush();
        } catch (Exception ex) {
            LogUtil.error(FileManager.class.getName(), ex, "");
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception ex) {
                LogUtil.error(FileManager.class.getName(), ex, "");
            }
        }
    }
    
}
