package nl.umcg.suresnp.pipeline.io;

import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.samtools.util.BlockCompressedOutputStream;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by olivier on 07/12/2017.
 */
public class GenericFile {

    protected String path;
    private Charset charset;

    public GenericFile() {
    }

    public GenericFile(String path) {
        this.path = path.trim();
        this.charset = StandardCharsets.UTF_8;
    }

    public GenericFile(String path, Charset charset) {
        this.path = path.trim();
        this.charset = charset;
    }

    public Path getPath() {
        return Paths.get(path);
    }

    public String getPathAsString() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getBaseName() {
        String cur = FilenameUtils.getBaseName(this.path);
        int idx = cur.indexOf(".");
        if (idx > 0) {
            return (cur.substring(0, idx));
        } else {
            return cur;
        }
    }

    public String getFullExtension() {
        String cur = this.getPath().getFileName().toString();
        int idx = cur.indexOf(".");
        if (idx > 0) {
            return (cur.substring(idx));
        } else {
            return "";
        }
    }

    public String getFileName() {
        return FilenameUtils.getName(this.path);
    }

    public String getFolder() {
        return FilenameUtils.getPath(this.path);
    }

    public String getExtention() {
        return (FilenameUtils.getExtension(this.path));
    }

    public boolean isGZipped() {
        return (getExtention().endsWith("gz"));
    }

    public boolean isBgZipped() {
        return (getExtention().endsWith("bgz"));
    }

    public boolean isTabixIndexed() {
        if (isBgZipped()) {
            File curFile = new File(path + ".tbi");
            if (curFile.exists()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean isBigBed() {
        return getExtention().endsWith("bb");
    }

    public GenericFile getTabixFile() throws FileNotFoundException {

        if (isTabixIndexed()) {
            return new GenericFile(path + ".tbi");
        } else {
            throw new FileNotFoundException("File either not tabixed or bgZipped: " + path);
        }

    }

    public Charset getCharset() {
        return charset;
    }

    public InputStream getAsInputStream() throws IOException {
        if (isBgZipped()) {
            return new BlockCompressedInputStream(new File(path));
        } else if (isGZipped()) {
            return new BufferedInputStream(new GZIPInputStream(new FileInputStream(new File(path))));
        } else {
            return new BufferedInputStream(new FileInputStream(new File(path)));
        }
    }

    public BufferedReader getAsBufferedReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getAsInputStream()));
    }

    public OutputStream getAsOutputStream() throws IOException {

        if (isBgZipped()) {
            return new BlockCompressedOutputStream(path);
        } else if (isGZipped()) {
            return new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(new File(path))));
        } else {
            return new BufferedOutputStream(new FileOutputStream(new File(path)));
        }
    }

    public FileOutputStream getAsFileOutputStream() throws IOException {
        return new FileOutputStream(new File(path));
    }

    public BufferedWriter getAsBufferedWriter() throws IOException {
        return new BufferedWriter(new OutputStreamWriter(getAsOutputStream(), charset));
    }

    public static String[] trimAllExtensionsFromFilenameArray(String[] inputArray) {
        String[] outputArray = new String[inputArray.length];

        // Clean filenames, trim all.
        int i = 0;
        for (String curFile : inputArray) {
            String tmp = new GenericFile(curFile).getBaseName();
            int idx = tmp.indexOf('.');
            if (idx > 0) {
                outputArray[i] = tmp.substring(0, idx);
            } else {
                outputArray[i] = tmp;
            }
            i++;
        }

        return outputArray;
    }

}
