package util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class UriSplit {

    private static final Pattern regex = Pattern.compile("[\\?]");
    private static final Pattern regex2 = Pattern.compile("[&]");
    private static final Pattern regex3 = Pattern.compile("[=]");

    private List<String[]> params = new ArrayList<>();
    private final String uri;

    public UriSplit(String componentUrl) {
        String[] split = regex.split(componentUrl);
        uri = split[0];

        if (split.length > 1) {
            String[] split1 = regex2.split(split[1]);
            for (String query : split1) {
                String[] split2 = regex3.split(query);
                params.add(split2);
            }
        }
    }

    public List<String[]> getParams() {
        return params;
    }

    public String getUri() {
        return uri;
    }
}
