package nl.umcg.suresnp.pipeline.io.summarystatisticreader;

public class MissingVariantException extends Exception {

    public MissingVariantException() {
    }

    public MissingVariantException(String message) {
        super(message);
    }

    public MissingVariantException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingVariantException(Throwable cause) {
        super(cause);
    }

    public MissingVariantException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
