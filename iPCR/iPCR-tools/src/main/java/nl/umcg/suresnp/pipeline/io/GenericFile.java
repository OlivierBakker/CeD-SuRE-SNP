package nl.umcg.suresnp.pipeline.io;

import org.apache.commons.compress.compressors.FileNameUtil;
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
        return (FilenameUtils.getBaseName(this.path));
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

    public boolean isGzipped() {
        return (getExtention().endsWith("gz"));
    }

    public Charset getCharset() {
        return charset;
    }

    public InputStream getAsInputStream() throws IOException {
        if (isGzipped()) {
            return new BufferedInputStream(new GZIPInputStream(new FileInputStream(new File(path))));
        } else {
            return new BufferedInputStream(new FileInputStream(new File(path)));

        }
    }

    public BufferedReader getAsBufferedReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getAsInputStream()));
    }

    public OutputStream getAsOutputStream() throws IOException {
        if (isGzipped()) {
            return new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(new File(path))));
        } else {
            return new BufferedOutputStream(new FileOutputStream(new File(path)));
        }
    }

    public BufferedWriter getAsBufferedWriter() throws IOException {
        return new BufferedWriter(new OutputStreamWriter(getAsOutputStream(), charset));
    }


}
