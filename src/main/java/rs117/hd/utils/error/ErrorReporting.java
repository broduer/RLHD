package rs117.hd.utils.error;

import lombok.SneakyThrows;

import java.util.List;

public class ErrorReporting {

    String message;
    String exception;
    List<String> possibleFix;

    @SneakyThrows
    public ErrorReporting(Throwable exception) {
        this.exception = exception.toString();
        ErrorScreen.open(exception.toString());
        throw exception;
    }

    @SneakyThrows
    public ErrorReporting(Exception exception) {
        this.exception = exception.toString();
        ErrorScreen.open(exception.toString());
        throw exception;
    }

}
