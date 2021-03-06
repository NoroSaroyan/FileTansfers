package noro.geekbrains;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;

public class Mapper {
    public static String objectToString(DbFiles dbFiles) {
        try {
            ObjectMapper om = new ObjectMapper();
            return om.writeValueAsString(dbFiles);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String objectToString(List<DbFiles> dbFiles) {
        try {
            ObjectMapper om = new ObjectMapper();
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            om.writeValue(os, dbFiles);
            return os.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static List<DbFiles> stringToList(String str) {
        try {
            ObjectMapper om = new ObjectMapper();
            return om.readValue(str, new TypeReference<List<DbFiles>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public static DbFiles stringToObject(String str) {
        try {
            ObjectMapper om = new ObjectMapper();
            return om.readValue(str, DbFiles.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}