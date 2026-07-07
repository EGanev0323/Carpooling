package bg.tu_sofia.carpooling.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class BusinessException extends RuntimeException {

    private final int statusCode;

    public BusinessException(String message) {
        super(message);
        this.statusCode = HttpStatus.UNPROCESSABLE_ENTITY.value();
    }

    public BusinessException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
