package hubhds.bpo.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class RestExceptionHandler {

    //erros de duplicidade
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleSqlException(Exception exception) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("erro", "Conflito de Dados");
        body.put("mensagem", "Este registro já existe no sistema.");

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    //Erros de "não encontrado"
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRunTime(RuntimeException runtimeException) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("erro", "Erro na operação");
        body.put("mensagem", runtimeException.getMessage());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<Object> handleGeneric(Exception exception) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("erro","Erro interno");
        body.put("mensagem", "Ocorreu um erro inesperado do servidor.");

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
