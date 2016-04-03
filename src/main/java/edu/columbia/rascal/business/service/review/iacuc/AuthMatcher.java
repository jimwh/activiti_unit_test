package edu.columbia.rascal.business.service.review.iacuc;

import java.util.regex.Pattern;

/**
 *
 */
public enum AuthMatcher {

    foo;
    /**
     *
     */
    private final static String REG_PATTER = "^IACUC_(\\B[A-Z]+_\\B)+[A-Z]+$";
    /**
     *
     */
    private final static Pattern AUTH_PATTERN = Pattern.compile(REG_PATTER);
    /*
    private AuthMatcher() {
        //REG_PATTER = "^IACUC_(\\B[A-Z]+_\\B)+[A-Z]+$";
    }
    */

    /**
     *
     * @param authority
     * @return
     */
    public boolean matchIacucAuthority(final String authority) {
        return AUTH_PATTERN.matcher(authority).matches();
    }

}
