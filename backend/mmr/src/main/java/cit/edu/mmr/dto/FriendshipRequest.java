package cit.edu.mmr.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FriendshipRequest {
    private long userId;
    private long friendId;
    private String status;

}
