package util;

import domain.ComponentUrl;
import domain.Page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResponseComposerImpl implements ResponseComposer {
    private static final Pattern SLOT_PATTERN = Pattern.compile("<div[^<>]*?location\\s*?>.*?</\\s*?div>");
    private static final Pattern SUBSTITUTION_VARIABLE_PATTERN = Pattern.compile("#\\{(.*?)\\}");

    @Override
    public String composeBody(Page page, String... bodies) {
        final List<String> bodyList = Arrays.asList(bodies);
        final List<ComponentUrl> componentUrls = page.getComponentUrls();

        return getFilledComponents(bodyList, componentUrls).get(0);
    }

    private List<String> getFilledComponents(List<String> bodyList, List<ComponentUrl> componentUrls) {
        ComponentUrl componentUrl = componentUrls.get(0);
        if (bodyList.size() > 1) {
            if (componentUrl.isTemplate()) {
                String templateText = bodyList.get(0);
                templateText = substituteVariables(templateText, componentUrl);
                return doSlotReplace(templateText, getFilledComponents(bodyList.subList(1, bodyList.size()), componentUrls.subList(1, componentUrls.size())));
            } else {
                final List<String> subList = new ArrayList<>();
                subList.add(substituteVariables(bodyList.get(0), componentUrl));
                final List<String> filledComponents = getFilledComponents(bodyList.subList(1, bodyList.size()), componentUrls.subList(1, componentUrls.size()));
                subList.addAll(filledComponents);
                return subList;
            }
        }
        final ArrayList<String> rtnVal = new ArrayList<>();
        rtnVal.add(substituteVariables(bodyList.get(0), componentUrl));
        return rtnVal;
    }

    private List<String> doSlotReplace(String templateText, List<String> bodies) {
        final Matcher matcher = SLOT_PATTERN.matcher(templateText);

        StringBuffer sb = new StringBuffer();
        int i = 0;
        while (matcher.find()) {
            String body = bodies.get(i++);
            matcher.appendReplacement(sb, body);
        }
        matcher.appendTail(sb);

        final List<String> returnBodies = new ArrayList<>();
        returnBodies.add(sb.toString());
        if (i < bodies.size()) {
            returnBodies.addAll(bodies.subList(i, bodies.size()));
        }
        return returnBodies;
    }

    private String substituteVariables(String templateText, ComponentUrl componentUrl) {
        final Map<String, String> substitutionVariables = componentUrl.getSubstitutionVariables();

        final Matcher matcher = SUBSTITUTION_VARIABLE_PATTERN.matcher(templateText);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            final String group = matcher.group(1);
            final String value = substitutionVariables.get(group);
            matcher.appendReplacement(sb, value);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
}
