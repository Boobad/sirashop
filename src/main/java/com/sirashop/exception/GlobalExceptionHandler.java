package com.sirashop.exception;

import com.sirashop.dto.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Attrape les erreurs de validation / arguments null ou invalides
     * Ex: `companyId` ou `shopId` est null
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("❌ [ERREUR VALIDATION SIRA-SHOP] : {}", ex.getMessage());
        
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Donnée manquante ou invalide",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Attrape les erreurs métier personnalisées (ex: "Boutique non trouvée", "Stock insuffisant", etc.)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDto> handleRuntimeException(RuntimeException ex) {
        log.error("⚠️ [RÈGLE MÉTIER NON RESPECTÉE] : {}", ex.getMessage());

        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Erreur applicative",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Attrape toutes les autres erreurs imprévues
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGeneralException(Exception ex) {
        log.error("💥 [ERREUR INATTENDUE SIRA-SHOP] : ", ex);

        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erreur interne du serveur",
                ex.getMessage() != null ? ex.getMessage() : "Une erreur inattendue s'est produite",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
