package com.mingxiu.apptest.ui.article;


import com.mingxiu.apptest.base.BaseModel;
import com.mingxiu.apptest.base.BasePresenter;
import com.mingxiu.apptest.base.BaseView;
import com.mingxiu.apptest.data.Pointer;
import com.mingxiu.apptest.data.entity.Image;
import com.mingxiu.apptest.data.entity._User;

import rx.Observable;


/**
 * Created by baixiaokang on 16/4/22.
 */
public interface ArticleContract {
    interface Model extends BaseModel {
        Observable createComment(String content, Pointer article, Pointer user);
    }


    interface View extends BaseView {
        void commentSuc();
        void commentFail();
        void showLoginAction();
    }

    abstract class Presenter extends BasePresenter<Model, View> {
        public abstract void createComment(String content, Image article, _User user);
        @Override
        public void onStart() {}
    }
}

