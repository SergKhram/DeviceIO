package io.github.sergkhram.utils.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.skyscreamer.jsonassert.comparator.DefaultComparator;
import org.skyscreamer.jsonassert.comparator.JSONCompareUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.*;

public class RegexpJsonComparator extends DefaultComparator {
    public RegexpJsonComparator(JSONCompareMode mode) {
        super(mode);
    }

    private static String toRegex(String value) {
        return value.replace(".", "\\.").replace("*", ".*");
    }

    @Override
    public void compareValues(String prefix, Object expectedValue, Object actualValue, JSONCompareResult result) throws JSONException {
        if (expectedValue instanceof String && expectedValue.toString().startsWith("regexp:")) {
            String regexp = expectedValue.toString().replaceFirst(toRegex("regexp:"), "").trim();
            if (JSONObject.NULL == actualValue || !actualValue.toString().matches(regexp)) {
                result.fail(prefix, expectedValue, actualValue);
            }
        } else if (expectedValue instanceof String && expectedValue.toString().startsWith("date:")) {
            String dateFormat = expectedValue.toString().replaceFirst(toRegex("date:"), "").trim();
            if (JSONObject.NULL == actualValue || !isThisDateValid(actualValue.toString(), dateFormat)) {
                result.fail(prefix, expectedValue, actualValue);
            }
        } else {
            super.compareValues(prefix, expectedValue, actualValue, result);
        }
    }

    @Override
    public void compareJSONArrayOfJsonObjects(String key, JSONArray expected, JSONArray actual, JSONCompareResult result) throws JSONException {
        String uniqueKey = findUniqueKey(expected);
        if (uniqueKey != null && isUsableAsUniqueKey(uniqueKey, actual)) {
            Map<Object, JSONObject> expectedValueMap = arrayOfJsonObjectToMap(expected, uniqueKey);
            Map<Object, JSONObject> actualValueMap = arrayOfJsonObjectToMap(actual, uniqueKey);
            Iterator iter = expectedValueMap.keySet().iterator();

            Object id;
            while (iter.hasNext()) {
                id = iter.next();
                if (!actualValueMap.containsKey(id)) {
                    result.missing(formatUniqueKey(key, uniqueKey, id), expectedValueMap.get(id));
                } else {
                    JSONObject expectedValue = expectedValueMap.get(id);
                    JSONObject actualValue = actualValueMap.get(id);
                    this.compareValues(formatUniqueKey(key, uniqueKey, id), expectedValue, actualValue, result);
                }
            }

            iter = actualValueMap.keySet().iterator();

            while (iter.hasNext()) {
                id = iter.next();
                if (!expectedValueMap.containsKey(id)) {
                    result.unexpected(formatUniqueKey(key, uniqueKey, id), actualValueMap.get(id));
                }
            }

        } else {
            this.recursivelyCompareJSONArray(key, expected, actual, result);
        }
    }

    private Boolean isThisDateValid(String dateToValidate, String dateFormat) {
        if (dateToValidate == null) {
            return false;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        sdf.setLenient(false);
        try {
            sdf.parse(dateToValidate);
        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    private String findUniqueKey(JSONArray expected) throws JSONException {
        JSONObject o = (JSONObject) expected.get(0);
        Iterator x = getKeys(o).iterator();

        String candidate = "";
        do {
            if (!x.hasNext()) {
                return null;
            }
            candidate = (String) x.next();
        } while (!isUsableAsUniqueKey(candidate, expected));
        return candidate;
    }

    Boolean isUsableAsUniqueKey(String candidate, JSONArray array) throws JSONException {
        HashSet seenValues = new HashSet<Object>();

        for (int i = 0; i < array.length(); i++){
            JSONObject item = (JSONObject) array.get(i);
            if (item == null) return false;
            if (!item.has(candidate)) {
                return false;
            }

            Object value = item.get(candidate);
            if (!isSimpleValue(value) || seenValues.contains(value) ||
                value.toString().startsWith("regexp:") ||
                value.toString().startsWith("date:")) {
                return false;
            }

            seenValues.add(value);
        }
        return true;
    }
}
