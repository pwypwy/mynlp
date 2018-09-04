package com.mayabot.nlp.segment;

import com.google.common.base.Joiner;
import com.mayabot.nlp.segment.analyzer.StandardMynlpAnalyzer;
import com.mayabot.nlp.segment.xprocessor.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

public class HowFast {
    public static void main(String[] args) throws Exception {
        File file = new File("data/红楼梦.txt");


        List<String> lines = Files.readAllLines(file.toPath()).stream().filter(it -> !it.isEmpty()).collect(Collectors.toList());


        MynlpTokenizer tokenizer = MynlpSegments.builder()
                .config(CommonPatternProcessor.class, p -> {
                    p.setEnableEmail(false);
                }).config(IndexSubwordProcessor.class, p -> {
                    p.setEnabled(false);
                })
                .config(OptimizeProcessor.class, p -> {
                    p.setEnabled(false);
                }).config(MergeNumberAndLetterPreProcessor.class, p -> {
                    p.setEnabled(true);
                }).config(MergeNumberQuantifierPreProcessor.class, p -> {
                    p.setEnabled(true);
                }).config(PartOfSpeechTaggingComputerProcessor.class, p -> {
                    p.setEnabled(true);
                })
                .build();

        int charNum = lines.stream().mapToInt(it -> it.length()).sum();


        tokenizer.tokenToTermList(lines.get(0));


        MynlpAnalyzer analyzer = new StandardMynlpAnalyzer(tokenizer);
        analyzer.stream(new StringReader(lines.get(0))).count();

        String text = Joiner.on("\n").join(lines);

        try (BufferedReader reader = new BufferedReader(new StringReader(text))) {
            analyzer.stream(reader).limit(10).forEach(x -> {
            });
        }


        {
            long t1 = System.currentTimeMillis();
            try (BufferedReader reader = new BufferedReader(new StringReader(text))) {
                analyzer.parse(reader).forEach(x -> {
                });
            }

            long t2 = System.currentTimeMillis();

            double time = (t2 - t1);
            System.out.println("红楼梦 分词 使用 " + (int) time + " ms");

            System.out.println("速度 " + (int) ((charNum / time) * 1000) + "字/秒");
        }


        {
            long t1 = System.currentTimeMillis();

            lines.forEach(line -> {
                tokenizer.tokenToTermList(line);
            });

            long t2 = System.currentTimeMillis();

            double time = (t2 - t1);
            System.out.println("红楼梦 分词 使用 " + (int) time + " ms");

            System.out.println("速度 " + (int) ((charNum / time) * 1000) + "字/秒");
        }


//        System.out.println("--------Ansj-----------");
//        NlpAnalysis.parse(lines.get(0));
//
//        {
//            long t1 = System.currentTimeMillis();
//
//            for (int j = 0; j < 3; j++) {
//                for (int i = 0; i < lines.size(); i++) {
//                    NlpAnalysis.parse(lines.get(i));
//                }
//            }
//
//            long t2 = System.currentTimeMillis();
//
//            double time = (t2 - t1) / 3.0;
//            System.out.println("红楼梦 分词 使用 " + (int) time + " ms");
//            System.out.println("速度 " + (int) ((charNum / time) * 1000) + "字/秒");
//        }
    }
}