package com.mingxiu.apptest.ui.login;


import com.mingxiu.apptest.Contact;
import com.mingxiu.apptest.base.util.SpUtil;

/**
 * Created by baixiaokang on 16/4/29.
 */
public class LoginPresenter extends LoginContract.Presenter {

    @Override
    public void login(String name, String pass) {
        mRxManager.add(mModel.login(name, pass).subscribe(user -> {
                    SpUtil.setUser(user);
                    mRxManager.post(Contact.EVENT_LOGIN, user);
                    mView.loginSuccess();
                }, e -> mView.showMsg("登录失败!")
        ));
    }

    @Override
    public void sign(String name, String pass) {
        mRxManager.add(mModel.sign(name, pass)
                .subscribe(res -> mView.signSuccess(),
                        e -> mView.showMsg("注册失败!")));
    }

}