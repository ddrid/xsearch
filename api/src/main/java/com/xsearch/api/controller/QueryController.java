package com.xsearch.api.controller;

import com.hankcs.hanlp.dictionary.stopword.CoreStopWordDictionary;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.IndexTokenizer;
import com.xsearch.query.lib.QueryReply;
import com.xsearch.query.lib.QueryRequest;
import com.xsearch.query.lib.QueryServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class QueryController {
    @Value("${nodeNum}")
    int nodeNum;

    @GetMapping("/query")
    public void query(@RequestParam("query") String query,
                      @RequestParam("from") int from,
                      @RequestParam("size") int size) {
        List<Term> segment = CoreStopWordDictionary.apply(IndexTokenizer.segment(query));
        List<String> termList = new ArrayList<>();
        for (Term term : segment) {
            termList.add(term.word);
        }
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 1001 ).usePlaintext().build();
        QueryServiceGrpc.QueryServiceBlockingStub stub = QueryServiceGrpc.newBlockingStub(channel);
        QueryRequest req = QueryRequest.newBuilder()
                .setResultSize(from + size).addAllTerm(termList).build();
        QueryReply reply = stub.query(req);

    }
}
