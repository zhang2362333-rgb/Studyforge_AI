package com.studyforge.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyforge.ai.service.AiService;
import com.studyforge.system.service.IntegrationSettingService;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class SiliconFlowAiServiceImpl implements AiService {
    private static final String DEFAULT_BASE_URL = "https://api.siliconflow.cn/v1";
    private static final String DEFAULT_MODEL = "deepseek-ai/DeepSeek-V4-Flash";
    private static final String DEFAULT_IMAGE_BASE_URL = "https://api.hiyo.top/v1";
    private static final String DEFAULT_IMAGE_MODEL = "gpt-image-2";
    private static final String DEFAULT_IMAGE_SIZE = "1536x1024";
    private static final Duration AI_TIMEOUT = Duration.ofSeconds(200);

    private final IntegrationSettingService integrationSettingService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final LocalFallbackAiServiceImpl fallback = new LocalFallbackAiServiceImpl();

    public SiliconFlowAiServiceImpl(IntegrationSettingService integrationSettingService) {
        this.integrationSettingService = integrationSettingService;
        this.objectMapper = new ObjectMapper();
        HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(AI_TIMEOUT);
        proxySelector().ifPresent(builder::proxy);
        this.httpClient = builder.build();
    }

    @Override
    public String generateSummary(String content, String language) {
        String prompt = isEnglish(language)
                ? """
                Please summarize this learning post in English. Make the output feel like an AI summary in a real learning product:
                1. Start with 3 key points.
                2. Then add one concise takeaway worth saving.
                3. Do not mention "according to the document" or "as an AI".

                Website language: English

                Content:
                %s
                """.formatted(content)
                : """
                请用中文提炼这篇学习内容。输出要像真实学习产品里的 AI 摘要：
                1. 先给 3 条要点；
                2. 再给 1 句适合收藏的结论；
                3. 不要提“根据文档/作为 AI”。

                网站语言：中文

                内容：
                %s
                """.formatted(content);
        return complete(prompt, () -> fallback.generateSummary(content, language));
    }

    @Override
    public String generateTags(String content, String language) {
        String prompt = isEnglish(language)
                ? "Generate 4 to 6 learning tags for the content below. Output English tags separated by commas.\n" + content
                : "为下面内容生成 4 到 6 个学习标签，用中文输出，并用逗号分隔。\n" + content;
        return complete(prompt, () -> fallback.generateTags(content, language));
    }

    @Override
    public String recommendCategory(String content, String language) {
        String prompt = isEnglish(language)
                ? "Choose the best category from TECHNOLOGY, BUSINESS, PRODUCTIVITY, CAREER, FINANCE. Output only the category code.\n" + content
                : "从 TECHNOLOGY、BUSINESS、PRODUCTIVITY、CAREER、FINANCE 中选一个最适合的分类，只输出分类编码。\n" + content;
        return complete(prompt, () -> fallback.recommendCategory(content, language));
    }

    @Override
    public String translateText(String text, String sourceLang, String targetLang) {
        String prompt = isEnglish(targetLang)
                ? "Translate the following text from " + sourceLang + " to " + targetLang + ". Output only the translation.\n" + text
                : "把下面文本从 " + sourceLang + " 翻译到 " + targetLang + "，只输出译文。\n" + text;
        return complete(prompt, () -> fallback.translateText(text, sourceLang, targetLang));
    }

    @Override
    public String moderateContent(String content, String language) {
        String prompt = isEnglish(language)
                ? "Review whether the following content is suitable for a learning community. Output LOW_RISK, MEDIUM_RISK, or HIGH_RISK, followed by one short reason.\n" + content
                : "判断下面内容是否适合学习社区发布，输出 LOW_RISK、MEDIUM_RISK 或 HIGH_RISK，并给一句理由。\n" + content;
        return complete(prompt, () -> fallback.moderateContent(content, language));
    }

    @Override
    public String answerQuestion(String postContent, String question, String answerLanguage) {
        String prompt = isEnglish(answerLanguage)
                ? """
                You are StudyForge AI's learning assistant. Answer only from the article content.
                Answer in English.

                Article:
                %s

                Question:
                %s
                """.formatted(postContent, question)
                : """
                你是 StudyForge AI 的学习助手。请只依据文章内容回答问题。
                请用中文回答。

                文章：
                %s

                问题：
                %s
                """.formatted(postContent, question);
        return complete(prompt, () -> fallback.answerQuestion(postContent, question, answerLanguage));
    }

    @Override
    public String generateQuiz(String postContent, String language) {
        String prompt = isEnglish(language)
                ? """
                Turn this learning post into review cards. Output 4 cards. Each card must include:
                - Question
                - Short answer
                - Keywords for review

                Website language: English

                Content:
                %s
                """.formatted(postContent)
                : """
                请把这篇学习内容整理成复习卡片。输出 4 张卡片，每张包含：
                - 问题
                - 简短答案
                - 适合回顾的关键词

                网站语言：中文

                内容：
                %s
                """.formatted(postContent);
        return complete(prompt, () -> fallback.generateQuiz(postContent, language));
    }

    @Override
    public String formatMarkdown(String content, String language) {
        String prompt = isEnglish(language)
                ? """
                You are StudyForge AI's article formatting assistant. Reformat the user's plain text into Markdown suitable for a learning community post.

                Strict rules:
                - Output Markdown body only. Do not add explanations, prefaces, or code fences around the whole answer.
                - Preserve the user's meaning. Do not add facts or invent data.
                - Keep the source text's own language. If the source mixes Chinese and English, keep the mixed expression.
                - Add level-2 or level-3 headings, lists, quotes, tables, or code blocks when they naturally fit.
                - If the source already includes links, code, steps, or checklists, keep them and make them clearer.
                - Do not insert images that are not present in the source.
                - The output should be ready to paste into the Markdown editor.

                Website language: English

                Source:
                %s
                """.formatted(content)
                : """
                你是 StudyForge AI 的文章排版助手。请把用户的纯文字整理成适合学习社区发布的 Markdown。

                严格要求：
                - 只输出 Markdown 正文，不要输出解释、前言或代码围栏。
                - 保留用户原意，不新增事实、不编造数据。
                - 使用用户原文语言；如果原文混合中英，可以保留混合表达。
                - 根据内容自然加入二级/三级标题、列表、引用、表格或代码块。
                - 如果原文已经有链接、代码、步骤、清单，请保留并排得更清楚。
                - 不要插入不存在的图片。
                - 输出要适合直接写入 Markdown 编辑器。

                网站语言：中文

                原文：
                %s
                """.formatted(content);
        return complete(prompt, () -> fallback.formatMarkdown(content, language));
    }

    @Override
    public GeneratedCover generateCover(String title, String summary, String content, String language) {
        String source = """
                Title:
                %s

                Summary:
                %s

                Content:
                %s
                """.formatted(blankToEmpty(title), blankToEmpty(summary), truncate(blankToEmpty(content), 6000));
        String briefPrompt = isEnglish(language)
                ? """
                Read the article draft below and write a concise visual brief for a blog cover image.

                Rules:
                - Output one paragraph only, 60 to 100 English words.
                - Describe the core subject, visual metaphor, mood, color direction, and 2 to 4 concrete elements.
                - Do not mention the website, the author, or "this article".
                - Do not request visible text, logos, UI screenshots, or watermarks in the image.

                Draft:
                %s
                """.formatted(source)
                : """
                阅读下面的文章草稿，写一段适合生成博客封面的视觉简报。

                要求：
                - 只输出一段中文，60 到 100 字。
                - 说明核心主题、视觉隐喻、氛围、色彩方向和 2 到 4 个具体画面元素。
                - 不要提网站、作者或“这篇文章”。
                - 不要要求图片中出现文字、Logo、界面截图或水印。

                草稿：
                %s
                """.formatted(source);
        String brief = cleanText(complete(briefPrompt, () -> fallbackBrief(title, summary, content, language)), 700);
        String imagePrompt = coverPrompt(brief, language);
        String imageBase64 = generateImageBase64(imagePrompt);
        if (imageBase64 == null || imageBase64.isBlank()) {
            return null;
        }
        return new GeneratedCover(imageBase64, "image/png", brief, imagePrompt);
    }

    private String complete(String prompt, Fallback fallbackValue) {
        String apiKey = integrationSettingService.getValue("ai.api_key", "");
        if (apiKey.isBlank()) {
            return fallbackValue.get();
        }

        try {
            String baseUrl = trimSlash(integrationSettingService.getValue("ai.base_url", DEFAULT_BASE_URL), DEFAULT_BASE_URL);
            String model = integrationSettingService.getValue("ai.chat_model", DEFAULT_MODEL);
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(Map.of("role", "user", "content", prompt)),
                    "temperature", 0.3,
                    "stream", false
            );
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .timeout(AI_TIMEOUT)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return fallbackValue.get();
            }
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            return content.isMissingNode() || content.asText().isBlank() ? fallbackValue.get() : content.asText();
        } catch (Exception exception) {
            return fallbackValue.get();
        }
    }

    private String generateImageBase64(String prompt) {
        String apiKey = integrationSettingService.getValue("image.api_key", "");
        if (apiKey.isBlank()) {
            return null;
        }

        try {
            String baseUrl = trimSlash(integrationSettingService.getValue("image.base_url", DEFAULT_IMAGE_BASE_URL), DEFAULT_IMAGE_BASE_URL);
            String model = integrationSettingService.getValue("image.model", DEFAULT_IMAGE_MODEL);
            String size = integrationSettingService.getValue("image.size", DEFAULT_IMAGE_SIZE);
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "prompt", prompt,
                    "size", size,
                    "n", 1,
                    "response_format", "b64_json"
            );
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/images/generations"))
                    .timeout(AI_TIMEOUT)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return null;
            }
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode first = root.path("data").path(0);
            String b64 = first.path("b64_json").asText("");
            if (!b64.isBlank()) {
                return stripDataUrlPrefix(b64);
            }
            String url = first.path("url").asText("");
            return url.isBlank() ? null : fetchImageAsBase64(url);
        } catch (Exception exception) {
            return null;
        }
    }

    private String fetchImageAsBase64(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(AI_TIMEOUT)
                    .GET()
                    .build();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() < 200 || response.statusCode() >= 300 || response.body() == null || response.body().length == 0) {
                return null;
            }
            return Base64.getEncoder().encodeToString(response.body());
        } catch (Exception exception) {
            return null;
        }
    }

    private String coverPrompt(String brief, String language) {
        String content = isEnglish(language)
                ? "%s is the main idea of an article. Generate a blog-style cover image that matches this content.".formatted(brief)
                : "%s 是一篇文章的主要内容。请生成一张博客风格的文章封面，画面需要符合这个内容。".formatted(brief);
        return """
                %s

                Style requirements:
                - Landscape article cover, 3:2 ratio, clean modern editorial blog style.
                - Rich but restrained composition, suitable for a technology and learning community.
                - Use symbolic objects, workspace details, diagrams, notes, devices, light, and depth when relevant.
                - No readable text, no letters, no UI screenshots, no logo, no watermark.
                - High quality, polished, balanced negative space, clear focal point.
                """.formatted(content);
    }

    private String fallbackBrief(String title, String summary, String content, String language) {
        List<String> parts = new ArrayList<>();
        if (!isBlank(title)) {
            parts.add(title.trim());
        }
        if (!isBlank(summary)) {
            parts.add(summary.trim());
        }
        if (parts.isEmpty() && !isBlank(content)) {
            parts.add(truncate(content.trim().replaceAll("\\s+", " "), 260));
        }
        String text = parts.isEmpty() ? "学习笔记、知识整理、复习卡片和社区讨论" : String.join("。", parts);
        if (isEnglish(language)) {
            return "A thoughtful learning article about " + truncate(text, 320) + ", with a calm workspace, organized notes, subtle diagrams, and a focused editorial mood.";
        }
        return truncate(text, 320) + "。画面可以包含安静的学习桌面、结构化笔记、轻量图表和清晰的知识整理氛围。";
    }

    private String stripDataUrlPrefix(String value) {
        int comma = value.indexOf(',');
        return value.startsWith("data:") && comma >= 0 ? value.substring(comma + 1) : value;
    }

    private String cleanText(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim()
                .replaceAll("^```[a-zA-Z]*", "")
                .replaceAll("```$", "")
                .replaceAll("\\s+", " ")
                .trim();
        return truncate(normalized, maxLength);
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private String blankToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String trimSlash(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private java.util.Optional<ProxySelector> proxySelector() {
        String proxy = firstNotBlank(
                System.getProperty("https.proxy"),
                System.getProperty("https_proxy"),
                System.getenv("https_proxy"),
                System.getenv("HTTPS_PROXY"),
                System.getenv("http_proxy"),
                System.getenv("HTTP_PROXY")
        );
        if (proxy == null) {
            return java.util.Optional.empty();
        }

        try {
            URI uri = proxy.contains("://") ? URI.create(proxy) : URI.create("http://" + proxy);
            if (uri.getHost() == null) {
                return java.util.Optional.empty();
            }
            int port = uri.getPort();
            if (port <= 0) {
                port = "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
            }
            return java.util.Optional.of(ProxySelector.of(new InetSocketAddress(uri.getHost(), port)));
        } catch (IllegalArgumentException exception) {
            return java.util.Optional.empty();
        }
    }

    private String firstNotBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private boolean isEnglish(String language) {
        return language != null && language.toLowerCase(Locale.ROOT).startsWith("en");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @FunctionalInterface
    private interface Fallback {
        String get();
    }
}
