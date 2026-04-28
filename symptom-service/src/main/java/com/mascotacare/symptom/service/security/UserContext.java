package com.mascotacare.symptom.service.security;

public final class UserContext {
    private static final ThreadLocal<User> CURRENT = new ThreadLocal<>();
    private UserContext() {}

    public static void set(User u) { CURRENT.set(u); }
    public static User get() { return CURRENT.get(); }
    public static void clear() { CURRENT.remove(); }
    public static boolean isAdmin() {
        User u = CURRENT.get();
        return u != null && "ADMIN".equals(u.role());
    }

    public record User(String id, String email, String role, String name) {}
}
