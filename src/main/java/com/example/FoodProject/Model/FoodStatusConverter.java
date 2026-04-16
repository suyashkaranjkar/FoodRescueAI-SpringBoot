package com.example.FoodProject.Model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class FoodStatusConverter implements AttributeConverter<FoodStatus, String> {

    @Override
    public String convertToDatabaseColumn(FoodStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public FoodStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        // support old and new values safely
        return FoodStatus.fromString(dbData);
    }
}
