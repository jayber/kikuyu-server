package util;

import domain.Page;
import domain.PageComponent;

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
        final List<PageComponent> pageComponents = page.getPageComponents();

        //todo: this should actually concatenate responses if there are > 1
        return mergeTemplates(bodyList, pageComponents).get(0);
    }

    private List<String> mergeTemplates(List<String> bodyList, List<PageComponent> pageComponents) {
        PageComponent pageComponent = pageComponents.get(0);
        String substitutedText = substituteVariableValues(bodyList.get(0), pageComponent);
        if (bodyList.size() > 1) {
            if (pageComponent.isTemplate()) {
                return doSlotReplace(substitutedText, mergeTemplates(bodyList.subList(1, bodyList.size()), pageComponents.subList(1, pageComponents.size())));
            } else {
                final List<String> subList = new ArrayList<>();
                subList.add(substitutedText);
                final List<String> filledComponents = mergeTemplates(bodyList.subList(1, bodyList.size()), pageComponents.subList(1, pageComponents.size()));
                subList.addAll(filledComponents);
                return subList;
            }
        }
        final ArrayList<String> rtnVal = new ArrayList<>();
        rtnVal.add(substitutedText);
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

    private String substituteVariableValues(String templateText, PageComponent pageComponent) {
        final Map<String, String> substitutionVariables = pageComponent.getSubstitutionVariables();

        final Matcher matcher = SUBSTITUTION_VARIABLE_PATTERN.matcher(templateText);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            final String group = matcher.group(1);
            final String value = substitutionVariables.get(group);
            if (value != null) {
                matcher.appendReplacement(sb, value);
            } else {
                matcher.appendReplacement(sb, "");
            }
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
}
