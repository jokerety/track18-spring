package ru.track;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;

public class App2 {

    public static final String URL = "http://guarded-mesa-31536.herokuapp.com/track";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_GITHUB = "github";
    public static final String FIELD_EMAIL = "email";

    public static void main(String[] args) throws Exception{
        HttpResponse <JsonNode> r = Unirest.post(URL)
                .field (FIELD_NAME,"Roma")
                .field (FIELD_GITHUB, "https://github.com/jokerety")
                .field (FIELD_EMAIL, "maslov.roman.phystech@mail.ru")
                .asJson();
        System.out.println( r.getBody().getObject().get("success"));
    }
}
