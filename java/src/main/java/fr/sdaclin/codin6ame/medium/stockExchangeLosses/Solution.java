package fr.sdaclin.codin6ame.medium.stockExchangeLosses;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Scanner;

class Solution {

    public static void main(String args[]) {
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;

        process(inputStream, outputStream);
    }

    static void process(InputStream inputStream, OutputStream outputStream) {
        PrintStream out = new PrintStream(outputStream);
        Scanner in = new Scanner(inputStream);

        int n = in.nextInt();
        in.nextLine();
        String vs = in.nextLine();

        String[] valuesStr = vs.split(" ");
        int[] values = Arrays.stream(valuesStr).mapToInt(s -> Integer.parseInt(s, 10)).toArray();

        int maxDecote = 0;
        int currentMax = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i]< currentMax)
                continue;
            if (i>0 && values[i] < values[i-1])
                continue;
            currentMax = values[i];
            for (int j = i; j < values.length; j++) {
                int diff = values[j] - values[i];
                if (diff < maxDecote) {
                    maxDecote = diff;
                }
            }
        }
        out.println(maxDecote);
    }
}
