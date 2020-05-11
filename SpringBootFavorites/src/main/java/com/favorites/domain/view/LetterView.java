package com.favorites.domain.view;

import com.favorites.domain.enums.LetterType;

public interface LetterView {
    Long getId();
    Long getSendUserId();
    String getSendUserName();
    String getProfilePicture();
    String getContent();
    Long getCreateTime();
    Long getPid();
    String getType();
}
