package ru.track;


import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class App2 {

    public static void main(String[] args) {
        System.out.println(Arrays.toString(args));
        String str = StringUtils.capitalize(args[0]);
        System.out.println(str);
    }
}
