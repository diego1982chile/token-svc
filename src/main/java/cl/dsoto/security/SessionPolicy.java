package cl.dsoto.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.time.Duration;

public final class SessionPolicy {

    public static final String LAST_ACTIVITY_AT_KEY = SessionPolicy.class.getName() + ".lastActivityAt";
    public static final Duration MAX_IDLE_TIME = Duration.ofMinutes(5);

    private SessionPolicy() {
    }

    public static void touch(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        touch(session);
    }

    public static void touch(HttpSession session) {
        if (session == null) {
            return;
        }
        session.setAttribute(LAST_ACTIVITY_AT_KEY, System.currentTimeMillis());
    }

    public static boolean isExpired(HttpSession session) {
        if (session == null) {
            return true;
        }
        return System.currentTimeMillis() - getLastActivityAt(session) > MAX_IDLE_TIME.toMillis();
    }

    private static long getLastActivityAt(HttpSession session) {
        Object lastActivityAt = session.getAttribute(LAST_ACTIVITY_AT_KEY);
        if (lastActivityAt instanceof Number number) {
            return number.longValue();
        }
        return session.getCreationTime();
    }
}
