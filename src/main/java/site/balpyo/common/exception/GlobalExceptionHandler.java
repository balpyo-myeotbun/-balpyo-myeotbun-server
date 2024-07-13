package site.balpyo.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.balpyo.common.ErrorLogRepository;
import site.balpyo.common.dto.CommonResponse;
import site.balpyo.common.entity.ErrorLogEntity;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final ErrorLogRepository errorLogRepository;

    public GlobalExceptionHandler(ErrorLogRepository errorLogRepository) {
        this.errorLogRepository = errorLogRepository;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // 유효성 검사 실패에 대한 모든 에러 메시지를 연결하여 하나의 문자열로 만듭니다.
        String errorMessage = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError) {
                        return ((FieldError) error).getField() + ": " + error.getDefaultMessage();
                    } else {
                        return error.getDefaultMessage();
                    }
                })
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest().body(new CommonResponse("9001", errorMessage,""));
    }

    // 모든 예외를 처리하는 핸들러
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse> handleAllExceptions(Exception ex) {

        // 스택 트레이스에서 예외 발생 위치 가져오기
        StackTraceElement[] stackTrace = ex.getStackTrace();
        String className = stackTrace[0].getClassName();
        String methodName = stackTrace[0].getMethodName();
        Integer lineNumber = stackTrace[0].getLineNumber();

        // 예외 메시지 가져오기
        String errorMessage = ex.getMessage();
        ErrorLogEntity errorLogEntity = ErrorLogEntity.builder()
                .errorText(ex.getMessage())
                .className(className)
                .methodName(methodName)
                .lineNumber(lineNumber)
                .build();


        errorLogRepository.save(errorLogEntity);


        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CommonResponse("9999", errorMessage, ""));
    }

}
