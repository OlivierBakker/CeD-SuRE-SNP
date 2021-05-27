package nl.umcg.mafsamplesubset;

import org.apache.commons.io.FilenameUtils;

/**
 * Created by olivier on 07/12/2017.
 */
public class GenericFile {

    private String path;

    public GenericFile() {
    }

    public GenericFile(String path) {
        this.path = path;
    }

    public String getPath() {
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
}
