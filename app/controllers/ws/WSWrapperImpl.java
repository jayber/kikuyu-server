package controllers.ws;


import play.libs.WS;

public class WSWrapperImpl implements WSWrapper {
    @Override
    public WS.WSRequestHolder url(String address) {
        return WS.url(address);
    }
}
