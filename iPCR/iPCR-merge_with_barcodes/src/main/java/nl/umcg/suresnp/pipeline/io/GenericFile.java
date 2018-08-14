package nl.umcg.suresnp.pipeline.io;

import org.apache.commons.io.FilenameUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by olivier on 07/12/2017.
 */
public class GenericFile {

    protected String path;

    public GenericFile() {
    }

    public GenericFile(String path) {
        this.path = path;
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
        return(FilenameUtils.getBaseName(this.path));
    }

    public String getExtention() {
        return(FilenameUtils.getExtension(this.path));
    }

    public boolean isGzipped() {
        return(getExtention().endsWith("gz"));
        }
}
