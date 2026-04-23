package com.ej.subscript.infrastructure.adapter.web;

import com.ej.subscript.domain.exception.BusinessException;
import com.ej.subscript.domain.exception.DomainException;
import com.ej.subscript.domain.exception.TechnicalException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
public class GlobalExceptionHandler implements WebExceptionHandler {

    private record ErrorResponse(String title, int status, String detail) {}

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        var response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorResponse body = switch (ex) {
            case BusinessException be ->  {
                response.setStatusCode(HttpStatus.valueOf(be.status()));
                yield new ErrorResponse(be.title(), be.status(), be.detail());
            }
            case TechnicalException te -> {
                response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                yield new ErrorResponse(te.title(), 500, "Error interno del servidor");
            }
            case DomainException ignored -> {
                response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                yield new ErrorResponse("Error de dominio", 500, "Error interno del servidor");
            }
            case IllegalArgumentException iae -> {
                response.setStatusCode(HttpStatus.BAD_REQUEST);
                yield new ErrorResponse("Datos inválidos", 400, iae.getMessage());
            }
            default -> {
                response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                yield new ErrorResponse("Error interno", 500, "Ocurrió un error inesperado");
            }
        };

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(body);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }
}
