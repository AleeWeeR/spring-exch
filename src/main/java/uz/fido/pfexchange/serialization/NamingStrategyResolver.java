package uz.fido.pfexchange.serialization;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;

public interface NamingStrategyResolver {

    PropertyNamingStrategy resolve();

    String convertFieldName(String fieldName);
}
