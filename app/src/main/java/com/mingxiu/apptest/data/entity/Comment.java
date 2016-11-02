package com.mingxiu.apptest.data.entity;


import com.mingxiu.apptest.base.BaseEntity;
import com.mingxiu.apptest.data.Pointer;

/**
 * Created by baixiaokang on 16/5/4.
 */
public class Comment extends BaseEntity.BaseBean {

    public Pointer article;
    public String content;
    public Pointer creater;

    public Comment(Pointer article, String content, Pointer creater) {
        this.article = article;
        this.content = content;
        this.creater = creater;
    }
}
