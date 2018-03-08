package ru.track;



public class App2 {

    public static void main(String[] args) throws Exception{
        float b = 0.5e-10f, a = 1, c = 228;
        System.out.println( (a * b ) * c == a * ( b * c ) );
    }
}
