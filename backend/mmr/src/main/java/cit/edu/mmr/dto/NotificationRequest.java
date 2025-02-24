package cit.edu.mmr.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class NotificationRequest {
    private String type;
    private String text;
    private long relatedItemId;
    private String itemType;

}