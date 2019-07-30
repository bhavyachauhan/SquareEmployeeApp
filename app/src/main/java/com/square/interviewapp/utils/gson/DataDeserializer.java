package com.square.interviewapp.utils.gson;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Field;
import java.lang.reflect.Type;


public class DataDeserializer<T> implements JsonDeserializer<T> {

    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        T object = new Gson().fromJson(json, typeOfT);
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getAnnotation(Required.class) != null) {
                field.setAccessible(true);
                try {
                    if (field.get(object) == null) {
                        throw new JsonParseException("Missing field in JSON: " + field.getName());
                    }
                } catch (IllegalAccessException e) {
                    throw new JsonParseException("Failed to parse field in JSON: " + field.getName());
                }
            }
        }
        return object;

    }
}
