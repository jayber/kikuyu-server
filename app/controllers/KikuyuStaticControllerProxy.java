package controllers;

import play.api.modules.spring.Spring;
import play.mvc.Controller;
import play.mvc.Result;

public class KikuyuStaticControllerProxy extends Controller {

    public static Result index() {
        return ok("this is it");
    }

    public static Result siphon(String path) {
        KikuyuController bean = (KikuyuController) Spring.getBeanOfType(KikuyuController.class);
        return bean.siphon(path);
    }

}
