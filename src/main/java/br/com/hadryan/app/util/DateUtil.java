package br.com.hadryan.app.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe utilitária para manipulação de datas.
 *
 * @author Hadryan Silva
 * @since 21-03-2025
 */
public class DateUtil {

    private static final Logger LOGGER = Logger.getLogger(DateUtil.class.getName());

    // Padrão principal de formatação de data (ISO)
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    // Padrões alternativos para parsing
    private static final DateTimeFormatter[] ALTERNATIVE_FORMATTERS = {
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("MM-dd-yyyy")
    };

    /**
     * Formata uma data para exibição na interface
     *
     * @param date Data a ser formatada
     * @return String contendo a data formatada ou vazio se a data for nula
     */
    public static String format(LocalDate date) {
        if (date == null) {
            return "";
        }
        return DEFAULT_FORMATTER.format(date);
    }

    /**
     * Converte uma string para LocalDate, tentando vários formatos
     *
     * @param dateStr String contendo a data
     * @return LocalDate ou null se a conversão falhar
     */
    public static LocalDate parse(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        dateStr = dateStr.trim();

        // Tenta com o formato padrão
        try {
            return LocalDate.parse(dateStr, DEFAULT_FORMATTER);
        } catch (DateTimeParseException e) {
            // Ignora e tenta outros formatos
        }

        // Tenta com formatos alternativos
        for (DateTimeFormatter formatter : ALTERNATIVE_FORMATTERS) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
                // Continua tentando
            }
        }

        // Se chegou aqui, não conseguiu converter
        LOGGER.log(Level.WARNING, "Não foi possível converter a string de data: " + dateStr);
        return null;
    }

    /**
     * Valida se uma string contém uma data válida
     *
     * @param dateStr String a ser validada
     * @return true se for uma data válida, false caso contrário
     */
    public static boolean isValid(String dateStr) {
        return parse(dateStr) != null;
    }
}