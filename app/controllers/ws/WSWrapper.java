package controllers.ws;


import play.libs.WS;

public interface WSWrapper {

    WS.WSRequestHolder url(String address);
}
