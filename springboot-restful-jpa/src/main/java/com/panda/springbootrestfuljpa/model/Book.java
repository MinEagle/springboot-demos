package com.panda.springbootrestfuljpa.model;

import lombok.Data;

import javax.persistence.*;

/**
 * @author YIN
 */
@Data
@Entity(name = "t_book")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "book_name")
    private String name;
    private String author;
    //省略 getter/setter
}