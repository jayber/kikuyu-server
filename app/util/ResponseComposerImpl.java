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
    private static final Pattern SUBSTITUTION_VARIABLE_PATTERN = Pattern.compile("@\\{(.*?)\\}");

    @Override
    public String composeBody(Page page, String... bodies) {
        final List<String> bodyList = Arrays.asList(bodies);
        final List<PageComponent> pageComponents = page.getPageComponents();

        List<String> resultsOfMerge = mergeTemplates(bodyList, pageComponents);
        return concatenateStrings(resultsOfMerge);
    }

    private String concatenateStrings(List<String> values) {
        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            sb.append(value);
        }
        return sb.toString();
    }

    /**
     * This can probably be made more efficient with tail recursion (or just iteration)
     *
     * @param bodyList       = the Strings containing the response bodies making up templates and components
     * @param pageComponents = the PageComponents that contain the values for substitution into vars
     * @return the bodies with all the slots filled with component text and variables substituted
     */
    private List<String> mergeTemplates(List<String> bodyList, List<PageComponent> pageComponents) {
        PageComponent pageComponent = pageComponents.get(0);
        String substitutedText = substituteVariableValues(bodyList.get(0), pageComponent);
        List<String> mergedComponents;
        //recursion termination condition
        if (bodyList.size() == 1) {
            mergedComponents = singleEntryList(substitutedText);
        } else {
            final List<String> remainingBodies = bodyList.subList(1, bodyList.size());
            final List<PageComponent> remainingPageComponents = pageComponents.subList(1, pageComponents.size());
            //recursion
            final List<String> nextComponentsMerged = mergeTemplates(remainingBodies, remainingPageComponents);

            if (pageComponent.isTemplate()) {
                mergedComponents = mergeTemplateWithUnusedComponentBodies(substitutedText, nextComponentsMerged);
            } else {
                mergedComponents = singleEntryList(substitutedText);
                mergedComponents.addAll(nextComponentsMerged);
            }
        }
        return mergedComponents;
    }

    private ArrayList<String> singleEntryList(String substitutedText) {
        ArrayList<String> rtnVal;
        rtnVal = new ArrayList<String>();
        rtnVal.add(substitutedText);
        return rtnVal;
    }

    private List<String> mergeTemplateWithUnusedComponentBodies(String templateText, List<String> bodies) {
        StringBuffer mergedResult = new StringBuffer();
        int unusedBodyIndex = searchAndReplaceSlotsWithComponentBodies(templateText, bodies, mergedResult);
        String mergedText = mergedResult.toString();
        final List<String> returnBodies = new ArrayList<String>();
        returnBodies.add(mergedText);
        appendUnusedComponentBodies(bodies, unusedBodyIndex, returnBodies);
        return returnBodies;
    }

    private void appendUnusedComponentBodies(List<String> bodies, int unusedBodyIndex, List<String> returnBodies) {
        if (unusedBodyIndex < bodies.size()) {
            returnBodies.addAll(bodies.subList(unusedBodyIndex, bodies.size()));
        }
    }

    private int searchAndReplaceSlotsWithComponentBodies(String templateText, List<String> bodies, StringBuffer sb) {
        final Matcher matcher = SLOT_PATTERN.matcher(templateText);
        int i = 0;
        while (matcher.find()) {
            String body = bodies.get(i++);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(body));
        }
        matcher.appendTail(sb);
        return i;
    }

    private String substituteVariableValues(String templateText, PageComponent pageComponent) {
        final Map<String, String> substitutionVariables = pageComponent.getSubstitutionVariables();
        final Matcher matcher = SUBSTITUTION_VARIABLE_PATTERN.matcher(templateText);
        final StringBuffer sb = new StringBuffer();
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
