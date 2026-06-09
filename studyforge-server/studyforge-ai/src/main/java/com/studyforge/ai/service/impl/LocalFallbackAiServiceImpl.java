package com.studyforge.ai.service.impl;

import com.studyforge.ai.service.AiService;

public class LocalFallbackAiServiceImpl implements AiService {
    @Override
    public String generateSummary(String content, String language) {
        String normalized = trimContent(content, 220);
        if (normalized.isBlank()) {
            return isEnglish(language)
                    ? "AI service is unavailable, and there is no article content to summarize."
                    : "AI 服务暂时不可用，且没有可摘要的文章内容。";
        }
        if (!isEnglish(language)) {
            return """
                    AI 服务暂时不可用。文章开头内容如下：

                    %s
                    """.formatted(normalized);
        }
        return """
                AI service is unavailable. The article content begins with:

                %s
                """.formatted(normalized);
    }

    @Override
    public String generateTags(String content, String language) {
        String source = content == null ? "" : content.toLowerCase();
        if (source.contains("markdown")) {
            return "Markdown,Writing,Knowledge";
        }
        if (source.contains("vue") || source.contains("spring") || source.contains("mybatis")) {
            return "Technology,Architecture,StudyForge";
        }
        return "Learning,Notes,Review";
    }

    @Override
    public String recommendCategory(String content, String language) {
        String source = content == null ? "" : content.toLowerCase();
        if (source.contains("finance") || source.contains("cashflow") || source.contains("budget") || source.contains("现金流")) {
            return "FINANCE";
        }
        if (source.contains("career") || source.contains("interview") || source.contains("面试")) {
            return "CAREER";
        }
        if (source.contains("review") || source.contains("复习") || source.contains("效率")) {
            return "PRODUCTIVITY";
        }
        return "TECHNOLOGY";
    }

    @Override
    public String translateText(String text, String sourceLang, String targetLang) {
        return text == null ? "" : text;
    }

    @Override
    public String moderateContent(String content, String language) {
        return "LOW_RISK: no local rule matched high-risk content.";
    }

    @Override
    public String answerQuestion(String postContent, String question, String answerLanguage) {
        if (!isEnglish(answerLanguage)) {
            return "AI 服务暂时不可用。请先阅读文章内容，或在管理端检查模型配置后再试。";
        }
        return """
                AI service is unavailable. Please review the article directly and try again after checking the model configuration in the admin console.
                """.trim();
    }

    @Override
    public String generateQuiz(String postContent, String language) {
        String source = trimContent(postContent, 160);
        if (!isEnglish(language)) {
            return """
                    AI 服务暂时不可用。你可以先手动创建一张复习卡片：

                    - 问题：这篇文章解决了什么问题？
                    - 简短答案：%s
                    - 关键词：文章、复习、StudyForge
                    """.formatted(source.isBlank() ? "阅读文章后，用自己的话写下答案。" : source);
        }
        return """
                AI service is unavailable. You can create a review card manually:

                - Question: What problem does this article solve?
                - Short answer: %s
                - Keywords: article, review, StudyForge
                """.formatted(source.isBlank() ? "Read the article and write the answer in your own words." : source);
    }

    @Override
    public String formatMarkdown(String content, String language) {
        String source = content == null ? "" : content.trim();
        if (source.isBlank()) {
            return "";
        }
        if (isEnglish(language)) {
            return """
                    ## Core Notes

                    %s

                    ## Add Next

                    - Key concepts
                    - Concrete steps
                    - Links or code references
                    """.formatted(source);
        }
        return """
                ## 核心内容

                %s

                ## 可以继续补充

                - 关键概念
                - 具体步骤
                - 参考链接或代码
                """.formatted(source);
    }

    @Override
    public GeneratedCover generateCover(String title, String summary, String content, String language) {
        return null;
    }

    private String trimContent(String content, int maxLength) {
        if (content == null || content.isBlank()) {
            return "";
        }
        String normalized = content.trim().replaceAll("\\s+", " ");
        return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength) + "...";
    }

    private boolean isEnglish(String language) {
        return language != null && language.toLowerCase(java.util.Locale.ROOT).startsWith("en");
    }
}
