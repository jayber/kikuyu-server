package util;

public class ResponseComposerImpl implements ResponseComposer {
    @Override
    public String composeBody(String templateBody, String componentBody) {
        String finalText = templateBody.replaceAll("<div location>.*</div>", componentBody);
        return finalText;
    }
}
