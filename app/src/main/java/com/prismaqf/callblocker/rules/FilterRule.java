package com.prismaqf.callblocker.rules;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * A class to represent a Filter to match a number against a set of pattern
 * @author ConteDiMonteCristo
 */
public class FilterRule implements IFilterRule, Cloneable, Parcelable, Serializable{

    private final static long serialVersionUID = 1L; //for serialization consistency
    private String name;
    private String description;
    private Map<String,Pattern> patterns;
    //characters not allowed in a patter: non digits and everything else apart from *
    private static final Pattern NOT_ALLOWED = Pattern.compile("[^\\d*]");
    private static final Pattern LEFT_TRIM = Pattern.compile("^\\*+");
    private static final Pattern RIGHT_TRIM = Pattern.compile("\\*+$");
    private static final Pattern SEPARATOR = Pattern.compile("\\*+");
    private static final StringBuilder sbuilder = new StringBuilder();

    /**
     * Constructor
     * @param name the name of the filter (e.g. blacklist)
     * @param description a description of the purpouse of this filter
     */
    public FilterRule(String name, String description) {
        this.name = name;
        this.description = description;
        patterns = new TreeMap<>();
    }

    private FilterRule(Parcel in) {
        name = in.readString();
        description = in.readString();
        patterns = new TreeMap<>();
        int size = in.readInt();
        for (int i=0; i<size; i++)
            addPattern(in.readString());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    //managing patterns (adding, getting removing

    /**
     * @return the set of keys associated with patterns
     */
    public synchronized Set<String> getPatternKeys() {
        return patterns.keySet();
    }

    /**
     * Accesse a Regex pattern by its key
     * @param key the string that maps to a regex expression
     * @return a compiled regex pattern
     */
    public synchronized Pattern getPattern(String key) {
        return patterns.get(key);
    }

    /**
     * Add a new regex and stores it as a compiled Pattern
     * @param key the string that maps to a regex expression
     */
    public synchronized void addPattern(String key) {
        if (patterns.containsKey(key)) return;
        key = filterUnwanted(key);
        patterns.put(key, makeRegex(key));
    }


    /**
     * Remove a pattern from the map
     * @param key the string that maps to a regex expressionif
     */
    public synchronized void removePattern(String key) {
        patterns.remove(key);
    }

    /**
     * Clear all patterns
     */
    public synchronized void clearPatterns() {
        patterns.clear();
    }

    /**
     * Create a regex Pattern based on the client defined pattern,
     * transforming the client pattern into a proper regex. The syntax
     * allowed is very simple: groups of numbers separated by '*'.
     * '*' at the beginning and end are removed
     * @param key the client key
     * @return a regex Pattern
     */
    static Pattern makeRegex(String key) {

        String[] tokens = SEPARATOR.split(key);
        if (tokens.length==0 || tokens[0].isEmpty()) return Pattern.compile("\\d*"); //always matching any digit
        sbuilder.setLength(0);
        sbuilder.append(tokens[0]);
        for (int i=1;i<tokens.length;i++) {
            sbuilder.append("\\d+");
            sbuilder.append(tokens[i]);
        }
        return Pattern.compile(sbuilder.toString());
    }

    static String filterUnwanted(String key) {
        //first clean up everything that is not a digit or a * sign
        if (NOT_ALLOWED.matcher(key).find())
            key = NOT_ALLOWED.matcher(key).replaceAll("");
        if (LEFT_TRIM.matcher(key).find())
            key = LEFT_TRIM.matcher(key).replaceFirst("");
        if (RIGHT_TRIM.matcher(key).find())
            key = RIGHT_TRIM.matcher(key).replaceFirst("");
        return key;
    }

    @Override
    public synchronized boolean Matches(String number) {
        if (number == null) return false;
        //purify the number
        number = filterUnwanted(number);
        //check first if the number is one of the patterns
        if (patterns.containsKey(number)) return true;
        for(Pattern p:patterns.values())
            if (p.matcher(number).find()) return true;
        return false;
    }

    //override some Object methods

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),"Rule %s [description = %s, total number of patterns = %d]",name,description,patterns.size());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof FilterRule)) return false;
        FilterRule other = (FilterRule)o;
        if (!name.equals(other.name) || !description.equals(other.description)) return false;
        //now compare the map keys (the values are derived from the keys so no need to compare)
        if (patterns.size()!=other.patterns.size()) return false;
        //if same size check that each key in this is contained in other
        for (String key: patterns.keySet())
            if (!other.patterns.containsKey(key)) return false;
        //if we get here then everything should be the same
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + description.hashCode();
        for (String key: patterns.keySet())
            result = prime * result + key.hashCode();
        return result;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        FilterRule other = (FilterRule) super.clone();
        other.patterns = new TreeMap<>();
        for (String key: patterns.keySet())
            other.addPattern(key);
        return other;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(description);
        dest.writeInt(patterns.size());
        for(String key:patterns.keySet())
            dest.writeString(key);
    }

    public static final Parcelable.Creator<FilterRule> CREATOR
            = new Parcelable.Creator<FilterRule>() {
        public FilterRule createFromParcel(Parcel in) {
            return new FilterRule(in);
        }

        public FilterRule[] newArray(int size) {
            return new FilterRule[size];
        }
    };
}
