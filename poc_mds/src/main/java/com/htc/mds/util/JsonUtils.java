package com.htc.mds.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class JsonUtils {

    static Logger logger = LoggerFactory.getLogger(JsonUtils.class);
    private static final ObjectMapper _mapper = new ObjectMapper();
    public static String toString(Object obj) {

        try {

            String s = _mapper.writeValueAsString(obj);
            return s;
        } catch (JsonProcessingException ex) {
            logger.error(ex.getMessage(),ex);
        }
        return null;
    }

    public static <T> T toObject(String jsonData, Class<T> beanType) {

        try {
            T t = _mapper.readValue(jsonData, beanType);
            return t;
        } catch (Exception ex) {
            logger.error(ex.getMessage(),ex);
        }
        return null;
    }

    public static <T> List<T> jsonToList(String jsonData, Class<T> beanType) {
        JavaType javaType = _mapper.getTypeFactory().constructParametricType(List.class, beanType);
        try {
            List<T> list = _mapper.readValue(jsonData, javaType);
            return list;
        } catch (Exception ex) {
            logger.error(ex.getMessage(),ex);
        }
        return null;
    }

}
