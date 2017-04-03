package wtf.pants.stamp.util;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Pants
 */
public class ZipUtils {

    /**
     * Adds a directory to a .zip file
     *
     * @param zipOutputStream instance of ZipOutputStream
     * @param dirName         Directory name you want to add to the .zip file
     * @throws IOException
     */
    public static void addDirectoryToZip(ZipOutputStream zipOutputStream, String dirName) throws IOException {
        zipOutputStream.putNextEntry(new ZipEntry(dirName));
        zipOutputStream.closeEntry();
    }

    /**
     * Adds a file to a .zip file
     *
     * @param zipOutputStream instance of ZipOutputStream
     * @param path            Path to where you want to place the file inside the .zip
     * @param bytes           The bytes of the file you want to add to the .zip
     * @throws IOException
     */
    public static void addFileToZip(ZipOutputStream zipOutputStream, String path, byte[] bytes) throws IOException {
        zipOutputStream.putNextEntry(new ZipEntry(path));
        zipOutputStream.write(bytes, 0, bytes.length);
        zipOutputStream.closeEntry();
    }

}
