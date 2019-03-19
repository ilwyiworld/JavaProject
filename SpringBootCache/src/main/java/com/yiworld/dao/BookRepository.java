package com.yiworld.dao;

import com.yiworld.entity.Book;

public interface BookRepository {
    Book getByIsbn(String isbn);
}
