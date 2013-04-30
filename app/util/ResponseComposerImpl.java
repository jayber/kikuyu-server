package util;

public class ResponseComposerImpl implements ResponseComposer {
    @Override
    public String composeBody(String... bodies) {
        String templateText = bodies[0];

        // starting at 1 is not a mistake!
        for (int i = 1; i < bodies.length; i++) {
            String body = bodies[i];
            templateText = templateText.replaceFirst("<div[^<>]*?location\\s*?>.*?</\\s*?div>", body);
        }

        return templateText;
    }
}
