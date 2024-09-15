package br.com.papaprecoapi.model;

import java.util.Map;

import lombok.Data;

@Data
public class NotificationMessage {
    private String recipientToken;
    private String title;
    private String body;
    /*private String image;*/
    private Map<String, String> data;
}
