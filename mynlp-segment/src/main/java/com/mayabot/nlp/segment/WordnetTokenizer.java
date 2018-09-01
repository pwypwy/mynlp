/*
 * Copyright 2018 mayabot.com authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mayabot.nlp.segment;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.segment.common.VertexHelper;
import com.mayabot.nlp.segment.wordnet.BestPathComputer;
import com.mayabot.nlp.segment.wordnet.Wordnet;
import com.mayabot.nlp.segment.wordnet.Wordpath;

import java.util.List;

/**
 * 一个基于词图的流水线 要求里面所有的组件都是无状态的，线程安全的类
 *
 * @author jimichan
 */
public class WordnetTokenizer implements MynlpTokenizer {

    public static WordnetTokenizerBuilder builder() {
        return new WordnetTokenizerBuilder();
    }

    private static InternalLogger logger = InternalLoggerFactory.getInstance(WordnetTokenizer.class);

    /**
     * 当wordnet创建后，调用这些处理器来填充里面的节点
     */
    private WordnetInitializer initer;

    /**
     * 处理器网络
     */
    private WordpathProcessor[] pipeline;

    private BestPathComputer bestPathComputer;

    private WordTermCollector collector;

    private VertexHelper vertexHelper;

    WordnetTokenizer(WordnetInitializer initer,
                     WordpathProcessor[] pipeline,
                     BestPathComputer bestPathComputer,
                     WordTermCollector termCollector,
                     VertexHelper vertexHelper) {
        this.initer = initer;
        this.pipeline = pipeline;
        this.bestPathComputer = bestPathComputer;
        this.collector = termCollector;
        this.vertexHelper = vertexHelper;

        Preconditions.checkNotNull(bestPathComputer);
        Preconditions.checkNotNull(this.initer);
        Preconditions.checkNotNull(pipeline);
        Preconditions.checkArgument(pipeline.length != 0);
    }

    @Override
    public void token(char[] text, List<WordTerm> target) {

        if (!target.isEmpty()) {
            target.clear();
        }

        // 处理为空的特殊情况
        if (text.length == 0) {
            return;
        }

        //构建一个空的Wordnet对象
        final Wordnet wordnet = new Wordnet(text);
        wordnet.getBeginRow().put(vertexHelper.newBegin());
        wordnet.getEndRow().put(vertexHelper.newEnd());

        initer.init(wordnet);


        //选择一个路径出来(第一次不严谨的分词结果)
        Wordpath wordPath = bestPathComputer.select(wordnet);

        // call WordpathProcessor
        for (WordpathProcessor processor : pipeline) {
            if (processor.isEnabled()) {
                wordPath = processor.process(wordPath);
            }
        }

        collector.collect(wordnet, wordPath, target);
    }


    public List<WordpathProcessor> getPipeline() {
        return ImmutableList.copyOf(pipeline);
    }

    public WordTermCollector getCollector() {
        return collector;
    }

}
