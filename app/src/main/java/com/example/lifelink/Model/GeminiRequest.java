package com.example.lifelink.Model;

public class GeminiRequest {
    public Content[] contents;

    public GeminiRequest(String text) {
        this.contents = new Content[]{new Content(text)};
    }

    public static class Content {
        public Part[] parts;

        public Content(String text) {
            this.parts = new Part[]{new Part(text)};
        }
    }

    public static class Part {
        public String text;

        public Part(String text) {
            this.text = text;
        }
    }
}


