package edu.columbia.rascal.business.service.review.iacuc;

import java.util.regex.Pattern;

/**
 *
 */
public final class IacucAuthMatcher {

    /**
     *
     */
    enum TaskKey {
        Approved,
        Distributed
    };
    /**
     *
     */
    public static final String APPROVE_KEY = "Approved";
    /**
     *
     */
    private static final String REG_PATTER = "^IACUC_(\\B[A-Z]+_\\B)+[A-Z]+$";
    /**
     *
     */
    private static final Pattern AUTH_PATTER = Pattern.compile(REG_PATTER);

    private IacucAuthMatcher() {}

    /**
     *
     * @param name
     * @return
     */
    public static boolean isApprovedKey(final String name) {
        return APPROVE_KEY.equals(name);
    }

    /**
     *
     * @param name
     * @return
     */
    public static boolean isDistributed(final String name) {
        return TaskKey.Distributed.equals(name);
    }

    /**
     *
     * @param authority
     * @return
     */
    public static boolean matchIacucAuthority(final String authority) {
        return AUTH_PATTER.matcher(authority).matches();
    }

}
