package com.panda.springbootrestfuljpa.repository;

import com.panda.springbootrestfuljpa.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author YIN
 */
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * 根据作者查找书
     *
     * @param author 作者名称
     * @return List<Book>
     */
    List<Book> findBookByAuthorContaining(@Param("author") String author);
}

