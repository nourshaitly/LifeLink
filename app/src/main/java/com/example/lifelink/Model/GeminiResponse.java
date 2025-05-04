package com.example.lifelink.Model;

public class GeminiResponse {
    public Candidate[] candidates;

    public static class Candidate {
        public Content content;
    }

    public static class Content {
        public Part[] parts;
    }

    public static class Part {
        public String text;
    }
}