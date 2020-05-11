package com.favorites.service;

import com.favorites.domain.Letter;
import com.favorites.domain.view.LetterSummary;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LetterService {

    public void sendLetter(Letter letter);

    public List<LetterSummary> findLetter(Long userId, Pageable pageable);
}
