package com.favorites.service;

import com.favorites.domain.Feedback;

public interface FeedbackService {

    public void saveFeeddback(Feedback feedback,Long userId);
}
