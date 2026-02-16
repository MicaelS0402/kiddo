package com.kiddo.utils;

public class Hash {
    public static String hashPassword(String password) {
        String str = password + "kiddo_salt_2024";
        int hash = 0;
        for (int i = 0; i < str.length(); i++) {
            int c = str.charAt(i);
            hash = ((hash << 5) - hash) + c;
            hash = hash & hash;
        }
        return Integer.toString(Math.abs(hash), 36);
    }
}
