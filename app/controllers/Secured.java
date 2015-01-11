package controllers;

//  認証関連、詳しくは以下リンクで
//  https://www.playframework.com/documentation/ja/2.2.x/JavaGuide4

import play.*;
import play.mvc.*;
import play.mvc.Http.*;

import models.*;

public class Secured extends Security.Authenticator {

    @Override
    public String getUsername(Context ctx) {
        return ctx.session().get("email");
    }

    @Override
    public Result onUnauthorized(Context ctx) {
        return redirect(routes.Application.login());
    }
}