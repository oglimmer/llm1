package de.oglimmer.llm1.llm;

import dev.langchain4j.service.TokenStream;

interface Assistant {

    TokenStream chat(String userMessage);
}
