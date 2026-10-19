package com.browser.ai;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NativeAiEngine {

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "aren't",
            "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "can",
            "can't", "cannot", "could", "couldn't", "did", "didn't", "do", "does", "doesn't", "doing", "don't",
            "down", "during", "each", "few", "for", "from", "further", "had", "hadn't", "has", "hasn't", "have",
            "haven't", "having", "he", "he'd", "he'll", "he's", "her", "here", "here's", "hers", "herself", "him",
            "himself", "his", "how", "how's", "i", "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "isn't",
            "it", "it's", "its", "itself", "let's", "me", "more", "most", "mustn't", "my", "myself", "no", "nor",
            "not", "of", "off", "on", "once", "only", "or", "other", "ought", "our", "ours", "ourselves", "out",
            "over", "own", "same", "shan't", "she", "she'd", "she'll", "she's", "should", "shouldn't", "so", "some",
            "such", "than", "that", "that's", "the", "their", "theirs", "them", "themselves", "then", "there",
            "there's", "these", "they", "they'd", "they'll", "they're", "they've", "this", "those", "through", "to",
            "too", "under", "until", "up", "very", "was", "wasn't", "we", "we'd", "we'll", "we're", "we've", "were",
            "weren't", "what", "what's", "when", "when's", "where", "where's", "which", "while", "who", "who's",
            "whom", "why", "why's", "with", "won't", "would", "wouldn't", "you", "you'd", "you'll", "you're", "you've",
            "your", "yours", "yourself", "yourselves"
    ));

    public static class AiExecutionResult {
        public final String response;
        public final org.json.JSONObject action;

        public AiExecutionResult(String response, org.json.JSONObject action) {
            this.response = response;
            this.action = action;
        }
    }

    public AiExecutionResult thinkAndExecute(String userPrompt, String pageTitle, String pageUrl, String rawPageText) {
        if (userPrompt == null || userPrompt.isBlank()) {
            return new AiExecutionResult("Please enter a question or request!", null);
        }

        String prompt = userPrompt.trim();
        String lower = prompt.toLowerCase();

        // 1. Chrome Execution
        if (lower.contains("chrome")) {
            String target = "https://www.google.com";
            String query = "Google Chrome";
            if (lower.contains("youtube") || lower.contains("play") || lower.contains("music") || lower.contains("song")) {
                query = "popular top music";
                Pattern p = Pattern.compile("(?:play|search)\\s+(.+?)(?:\\s+on|\\s+in|\\s+there|$)", Pattern.CASE_INSENSITIVE);
                Matcher m = p.matcher(prompt);
                if (m.find()) {
                    String extracted = m.group(1).trim();
                    if (!extracted.equalsIgnoreCase("music") && !extracted.equalsIgnoreCase("youtube") && !extracted.equalsIgnoreCase("on there youtube")) {
                        query = extracted;
                    }
                }
                target = "https://www.youtube.com/results?search_query=" + java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8);
            }
            org.json.JSONObject act = new org.json.JSONObject();
            act.put("type", "OPEN_CHROME");
            act.put("query", query);
            act.put("target", target);
            return new AiExecutionResult("🚀 **Task Executing With Full Permission**:\n\nLaunching **Google Chrome** to open **" + target + "** right now!\n\n*(Chrome window will open automatically.)*", act);
        }

        // 2. In-Browser YouTube Play
        if ((lower.contains("youtube") && lower.contains("play")) || (lower.startsWith("play ") && (lower.contains("music") || lower.contains("song") || lower.contains("lofi") || lower.contains("beats"))) || lower.startsWith("play ")) {
            String query = "relaxing music";
            Pattern p = Pattern.compile("play\\s+(.+?)(?:\\s+on\\s+youtube|\\s+on\\s+yt|$)", Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(prompt);
            if (m.find()) {
                String extracted = m.group(1).trim();
                if (!extracted.equalsIgnoreCase("music") && !extracted.equalsIgnoreCase("a song") && !extracted.equalsIgnoreCase("youtube")) {
                    query = extracted;
                }
            } else if (lower.contains("lofi")) {
                query = "lofi hip hop radio beats to relax";
            }
            org.json.JSONObject act = new org.json.JSONObject();
            act.put("type", "PLAY_YOUTUBE");
            act.put("query", query);
            act.put("target", "player.html#q=" + java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8));
            return new AiExecutionResult("🎵 **Task Executing With Full Permission**:\n\nOpening **Autonomous YouTube Player** and starting playback for **'" + query + "'** for you right now!\n\n*(Video playback starting automatically.)*", act);
        }

        // 3. Open Sites
        if (lower.startsWith("open ") || lower.startsWith("go to ") || lower.startsWith("launch ")) {
            String site = lower.replaceFirst("^(open|go to|launch)\\s+", "").trim();
            String targetUrl = null;
            if (site.contains("youtube")) targetUrl = "player.html#q=trending+music";
            else if (site.contains("github")) targetUrl = "https://www.github.com";
            else if (site.contains("google")) targetUrl = "https://www.google.com";
            else if (site.contains("wikipedia")) targetUrl = "https://www.wikipedia.org";
            else if (site.contains("reddit")) targetUrl = "https://www.reddit.com";
            else if (site.contains("chatgpt")) targetUrl = "https://chatgpt.com";
            else if (site.contains("stackoverflow")) targetUrl = "https://stackoverflow.com";
            else if (site.contains("twitter") || site.equals("x")) targetUrl = "https://x.com";
            else if (site.contains(".") && !site.contains(" ")) targetUrl = site.startsWith("http") ? site : "https://" + site;

            if (targetUrl != null) {
                org.json.JSONObject act = new org.json.JSONObject();
                act.put("type", "NAVIGATE");
                act.put("target", targetUrl);
                return new AiExecutionResult("🚀 **Task Executing With Full Permission**:\n\nNavigating directly to **" + targetUrl + "**!", act);
            }
        }

        // 3. Search
        if (lower.startsWith("search ") || lower.startsWith("look up ")) {
            String q = lower.replaceFirst("^(search\\s+(for\\s+|google\\s+for\\s+)?|look\\s+up\\s+)", "").trim();
            org.json.JSONObject act = new org.json.JSONObject();
            act.put("type", "SEARCH");
            act.put("query", q);
            return new AiExecutionResult("🔍 **Task Executing With Full Permission**:\n\nSearching Google for **\"" + q + "\"**!", act);
        }

        // 4. Tab actions
        if (lower.contains("new tab")) {
            org.json.JSONObject act = new org.json.JSONObject();
            act.put("type", "NEW_TAB");
            return new AiExecutionResult("➕ **Task Executing With Full Permission**:\n\nOpening a fresh new tab.", act);
        }
        if (lower.contains("close tab") || lower.contains("close this tab")) {
            org.json.JSONObject act = new org.json.JSONObject();
            act.put("type", "CLOSE_TAB");
            return new AiExecutionResult("✕ **Task Executing With Full Permission**:\n\nClosing active tab.", act);
        }
        if (lower.contains("bookmark")) {
            org.json.JSONObject act = new org.json.JSONObject();
            act.put("type", "BOOKMARK");
            return new AiExecutionResult("⭐ **Task Executing With Full Permission**:\n\nBookmarking active webpage.", act);
        }
        if (lower.contains("zoom in")) {
            org.json.JSONObject act = new org.json.JSONObject();
            act.put("type", "ZOOM_IN");
            return new AiExecutionResult("🔎 **Task Executing With Full Permission**:\n\nZooming in (+10%).", act);
        }
        if (lower.contains("zoom out")) {
            org.json.JSONObject act = new org.json.JSONObject();
            act.put("type", "ZOOM_OUT");
            return new AiExecutionResult("🔎 **Task Executing With Full Permission**:\n\nZooming out (-10%).", act);
        }
        if (lower.contains("reload") || lower.contains("refresh")) {
            org.json.JSONObject act = new org.json.JSONObject();
            act.put("type", "RELOAD");
            return new AiExecutionResult("🔄 **Task Executing With Full Permission**:\n\nReloading active page.", act);
        }

        // Fallback to standard reasoning
        String standard = thinkAndRespond(userPrompt, pageTitle, pageUrl, rawPageText);
        return new AiExecutionResult(standard, null);
    }

    public String thinkAndRespond(String userPrompt, String pageTitle, String pageUrl, String rawPageText) {
        if (userPrompt == null || userPrompt.isBlank()) {
            return "Please enter a question or request!";
        }

        String prompt = userPrompt.trim();
        String lower = prompt.toLowerCase();
        boolean hasPageContent = rawPageText != null && rawPageText.trim().length() > 50;

        // 1. Math & Calculation requests
        String mathResult = tryEvaluateMath(prompt);
        if (mathResult != null) {
            return mathResult;
        }

        // 2. Unit Conversions
        String conversionResult = tryUnitConversion(prompt);
        if (conversionResult != null) {
            return conversionResult;
        }

        // 3. Page Summarization
        if (isSummarizeIntent(lower)) {
            if (hasPageContent) {
                return generatePageSummary(pageTitle, pageUrl, rawPageText);
            } else {
                return "📄 **Page Summary**\n\nThe current tab is the **New Tab / Start Page**. Open any website (like Wikipedia, GitHub, or documentation) and click **Summarize Page** to get an instant analysis of that site!";
            }
        }

        // 4. Page Key Concepts
        if (isConceptsIntent(lower)) {
            if (hasPageContent) {
                return extractKeyConcepts(pageTitle, rawPageText);
            } else {
                return "💡 **Key Concepts**\n\nNo active webpage is currently loaded. Navigate to any article or website, then ask for key concepts!";
            }
        }

        // 5. Quick TL;DR
        if (lower.contains("tldr") || lower.contains("tl;dr") || lower.contains("quick summary")) {
            if (hasPageContent) {
                return generateQuickTLDR(pageTitle, rawPageText);
            }
        }

        // 6. Questions specifically targeting the current webpage
        if (hasPageContent && isPageQuestionIntent(lower, prompt)) {
            String pageAnswer = answerFromPageContent(prompt, pageTitle, rawPageText);
            if (pageAnswer != null) {
                return pageAnswer;
            }
        }

        // 7. Programming & Code Generation
        if (isCodingIntent(lower)) {
            return handleCodingRequest(prompt, lower);
        }

        // 8. General Knowledge, Explanations, Science & Tech
        String knowledgeAnswer = answerGeneralKnowledge(lower, prompt);
        if (knowledgeAnswer != null) {
            return knowledgeAnswer;
        }

        // 9. If on an active webpage and prompt didn't match general knowledge, search page context first!
        if (hasPageContent) {
            String pageAttempt = answerFromPageContent(prompt, pageTitle, rawPageText);
            if (pageAttempt != null) {
                return pageAttempt;
            }
        }

        // 10. Autonomous reasoning fallback
        return generateGeneralReasoningResponse(prompt, pageTitle);
    }

    private boolean isSummarizeIntent(String lower) {
        return lower.contains("summarize") || lower.contains("summary") || lower.contains("overview")
                || lower.contains("give me a summary") || lower.equals("summarize this page");
    }

    private boolean isConceptsIntent(String lower) {
        return lower.contains("key concept") || lower.contains("concepts") || lower.contains("key takeaways")
                || lower.contains("important points") || lower.contains("main points");
    }

    private boolean isPageQuestionIntent(String lower, String prompt) {
        return lower.contains("this page") || lower.contains("this site") || lower.contains("the article")
                || lower.contains("according to") || lower.contains("mentioned") || lower.contains("does it say");
    }

    private boolean isCodingIntent(String lower) {
        return lower.contains("write code") || lower.contains("write a function") || lower.contains("create a class")
                || lower.contains("java code") || lower.contains("python code") || lower.contains("javascript")
                || lower.contains("how to code") || lower.contains("algorithm") || lower.contains("regex")
                || lower.contains("html code") || lower.contains("css style") || lower.contains("sql query");
    }

    // --- Page Intelligence & Summarization Engine ---

    private String generatePageSummary(String title, String url, String text) {
        List<String> sentences = extractCleanSentences(text);
        if (sentences.isEmpty()) {
            return "Unable to extract readable text from this webpage.";
        }

        Map<String, Integer> wordFrequencies = computeWordFrequencies(sentences);
        List<ScoredSentence> scored = scoreSentences(sentences, wordFrequencies, title);

        // Pick top sentences for executive summary
        int count = Math.min(scored.size(), 4);
        List<ScoredSentence> topSummary = new ArrayList<>(scored.subList(0, count));
        // Re-order by original chronological position for smooth narrative reading
        topSummary.sort(Comparator.comparingInt(s -> s.originalIndex));

        // Pick key takeaways (distinct points)
        List<String> keyTakeaways = new ArrayList<>();
        for (int i = count; i < Math.min(scored.size(), count + 5); i++) {
            String clean = scored.get(i).text.trim();
            if (clean.length() > 40 && clean.length() < 200 && !keyTakeaways.contains(clean)) {
                keyTakeaways.add(clean);
                if (keyTakeaways.size() >= 3) break;
            }
        }

        int wordCount = text.split("\\s+").length;
        int readingTimeMin = Math.max(1, wordCount / 220);

        StringBuilder sb = new StringBuilder();
        sb.append("🧠 **Autonomous AI Summary**\n\n");
        sb.append("### 📄 ").append(title != null && !title.isBlank() ? title : "Webpage Analysis").append("\n");
        sb.append("📊 **Quick Stats:** ~").append(wordCount).append(" words • ~").append(readingTimeMin).append(" min read\n\n");

        sb.append("#### 📋 Executive Overview:\n");
        for (ScoredSentence s : topSummary) {
            sb.append(s.text.trim()).append(" ");
        }
        sb.append("\n\n");

        if (!keyTakeaways.isEmpty()) {
            sb.append("#### ⚡ Core Key Takeaways:\n");
            for (String kp : keyTakeaways) {
                sb.append("• ").append(kp).append("\n");
            }
            sb.append("\n");
        }

        sb.append("💡 *Tip: Ask me specific questions about anything on this page!*");
        return sb.toString();
    }

    private String extractKeyConcepts(String title, String text) {
        List<String> sentences = extractCleanSentences(text);
        Map<String, Integer> wordFreq = computeWordFrequencies(sentences);

        // Extract high-value keyword entities
        List<Map.Entry<String, Integer>> topKeywords = new ArrayList<>(wordFreq.entrySet());
        topKeywords.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        StringBuilder sb = new StringBuilder();
        sb.append("💡 **Key Concepts & Themes: ").append(title != null ? title : "Current Page").append("**\n\n");

        sb.append("Here are the central topics identified from the content:\n\n");

        int found = 0;
        for (Map.Entry<String, Integer> entry : topKeywords) {
            String term = entry.getKey();
            if (term.length() > 3 && !STOP_WORDS.contains(term.toLowerCase())) {
                // Find a defining sentence containing this term
                for (String s : sentences) {
                    if (s.toLowerCase().contains(term.toLowerCase()) && s.length() > 45 && s.length() < 220) {
                        String capTerm = Character.toUpperCase(term.charAt(0)) + term.substring(1);
                        sb.append("• **").append(capTerm).append("** (mentions: ").append(entry.getValue()).append(")\n");
                        sb.append("  *\"").append(s.trim()).append("\"*\n\n");
                        found++;
                        break;
                    }
                }
                if (found >= 5) break;
            }
        }

        if (found == 0) {
            sb.append("The page focuses on general narrative text. You can ask me any direct question to drill down into specifics.");
        }

        return sb.toString();
    }

    private String generateQuickTLDR(String title, String text) {
        List<String> sentences = extractCleanSentences(text);
        if (sentences.isEmpty()) return "Not enough text on the page for a TL;DR.";

        Map<String, Integer> wordFrequencies = computeWordFrequencies(sentences);
        List<ScoredSentence> scored = scoreSentences(sentences, wordFrequencies, title);

        StringBuilder sb = new StringBuilder();
        sb.append("⚡ **TL;DR (Too Long; Didn't Read)**\n\n");
        int count = Math.min(scored.size(), 2);
        for (int i = 0; i < count; i++) {
            sb.append(scored.get(i).text.trim()).append(" ");
        }
        return sb.toString();
    }

    // --- In-Page Contextual Question Answering (Local RAG) ---

    private String answerFromPageContent(String query, String title, String text) {
        List<String> sentences = extractCleanSentences(text);
        if (sentences.isEmpty()) return null;

        Set<String> queryWords = extractKeywords(query);
        if (queryWords.isEmpty()) return null;

        List<ScoredSentence> matching = new ArrayList<>();
        for (int i = 0; i < sentences.size(); i++) {
            String s = sentences.get(i);
            String lowerS = s.toLowerCase();
            double score = 0;
            int matches = 0;

            for (String kw : queryWords) {
                if (lowerS.contains(kw)) {
                    matches++;
                    score += 10.0;
                    // Exact word boundary bonus
                    if (lowerS.matches(".*\\b" + Pattern.quote(kw) + "\\b.*")) {
                        score += 5.0;
                    }
                }
            }

            if (matches > 0 && s.length() > 30) {
                matching.add(new ScoredSentence(s, score, i));
            }
        }

        if (matching.isEmpty()) return null;

        matching.sort((a, b) -> Double.compare(b.score, a.score));

        StringBuilder sb = new StringBuilder();
        sb.append("🔍 **Found on Page (").append(title != null ? title : "Current Page").append(")**\n\n");
        sb.append("Based on the webpage content:\n\n");

        int limit = Math.min(matching.size(), 3);
        for (int i = 0; i < limit; i++) {
            sb.append("• ").append(matching.get(i).text.trim()).append("\n\n");
        }

        sb.append("💡 *Need more context or clarification? Just ask!*");
        return sb.toString();
    }

    // --- Coding Assistant Engine ---

    private String handleCodingRequest(String prompt, String lower) {
        StringBuilder sb = new StringBuilder();
        sb.append("💻 **Code Assistant (Autonomous Engine)**\n\n");

        if (lower.contains("singleton")) {
            sb.append("Here is a thread-safe Bill Pugh Singleton implementation in Java:\n\n");
            sb.append("```java\n");
            sb.append("public class DatabaseConnection {\n");
            sb.append("    private DatabaseConnection() {\n");
            sb.append("        // Private constructor prevents external instantiation\n");
            sb.append("    }\n\n");
            sb.append("    private static class Holder {\n");
            sb.append("        private static final DatabaseConnection INSTANCE = new DatabaseConnection();\n");
            sb.append("    }\n\n");
            sb.append("    public static DatabaseConnection getInstance() {\n");
            sb.append("        return Holder.INSTANCE;\n");
            sb.append("    }\n");
            sb.append("}\n");
            sb.append("```\n\n");
            sb.append("**Key Advantages:** Lazy-loaded, zero synchronization overhead, 100% thread-safe by classloader mechanics.");
            return sb.toString();
        }

        if (lower.contains("binary search")) {
            sb.append("Here is an efficient Binary Search implementation:\n\n");
            sb.append("```java\n");
            sb.append("public static int binarySearch(int[] array, int target) {\n");
            sb.append("    int low = 0;\n");
            sb.append("    int high = array.length - 1;\n\n");
            sb.append("    while (low <= high) {\n");
            sb.append("        int mid = low + (high - low) / 2;\n");
            sb.append("        if (array[mid] == target) return mid;\n");
            sb.append("        if (array[mid] < target) low = mid + 1;\n");
            sb.append("        else high = mid - 1;\n");
            sb.append("    }\n");
            sb.append("    return -1; // Not found\n");
            sb.append("}\n");
            sb.append("```\n\n");
            sb.append("**Complexity:** Time: `O(log n)`, Space: `O(1)`.");
            return sb.toString();
        }

        if (lower.contains("fetch") || lower.contains("http request") || lower.contains("javascript api")) {
            sb.append("Here is an asynchronous API fetch in modern JavaScript with error handling:\n\n");
            sb.append("```javascript\n");
            sb.append("async function fetchUserData(userId) {\n");
            sb.append("    try {\n");
            sb.append("        const response = await fetch(`https://api.example.com/users/${userId}`);\n");
            sb.append("        if (!response.ok) {\n");
            sb.append("            throw new Error(`HTTP Error ${response.status}`);\n");
            sb.append("        }\n");
            sb.append("        const data = await response.json();\n");
            sb.append("        return data;\n");
            sb.append("    } catch (error) {\n");
            sb.append("        console.error('Fetch failed:', error.message);\n");
            sb.append("        throw error;\n");
            sb.append("    }\n");
            sb.append("}\n");
            sb.append("```\n");
            return sb.toString();
        }

        // Generic code generator response
        sb.append("Here is an architecture template addressing your request:\n\n");
        sb.append("```java\n");
        sb.append("// Solution Architecture for: ").append(prompt).append("\n");
        sb.append("public class Solution {\n");
        sb.append("    public static void execute() {\n");
        sb.append("        System.out.println(\"Executing task: ").append(prompt.replace("\"", "\\\"")).append("\");\n");
        sb.append("        // Core logic implementation\n");
        sb.append("    }\n");
        sb.append("}\n");
        sb.append("```\n\n");
        sb.append("Specify any language (Java, Python, JS, C++, SQL) or algorithmic constraints if you want a tailored implementation!");
        return sb.toString();
    }

    // --- Math & Unit Evaluator Engine ---

    private String tryEvaluateMath(String prompt) {
        String clean = prompt.toLowerCase().replace("calculate", "").replace("what is", "").replace("calc", "").trim();
        clean = clean.replace("?", "").replace("=", "").trim();

        // Check for basic arithmetic expression: numbers and +, -, *, /, ^, %
        if (clean.matches("^[0-9\\.\\s\\+\\-\\*/\\^\\(\\)]+$") && clean.matches(".*[0-9].*") && clean.matches(".*[\\+\\-\\*/\\^].*")) {
            try {
                double result = evalSimpleMath(clean);
                return "🔢 **Calculation Result**\n\n`" + clean + "` = **" + formatNumber(result) + "**";
            } catch (Exception ignored) {}
        }

        // Percentage check: e.g. "15% of 850"
        Matcher pctMatcher = Pattern.compile("([0-9\\.]+)\\s*%\\s*of\\s*([0-9\\.]+)").matcher(clean);
        if (pctMatcher.find()) {
            double p = Double.parseDouble(pctMatcher.group(1));
            double v = Double.parseDouble(pctMatcher.group(2));
            double ans = (p / 100.0) * v;
            return "🔢 **Percentage Result**\n\n" + p + "% of " + v + " = **" + formatNumber(ans) + "**";
        }

        // Sqrt check: e.g. "sqrt(144)" or "square root of 144"
        Matcher sqrtMatcher = Pattern.compile("(?:sqrt|square root of)\\s*\\(?([0-9\\.]+)\\)?").matcher(clean);
        if (sqrtMatcher.find()) {
            double val = Double.parseDouble(sqrtMatcher.group(1));
            double ans = Math.sqrt(val);
            return "🔢 **Square Root Result**\n\n√" + val + " = **" + formatNumber(ans) + "**";
        }

        return null;
    }

    private double evalSimpleMath(String expr) {
        String sanitized = expr.replaceAll("\\s+", "");
        // Simple 2-operand or sequential calculator
        if (sanitized.contains("+")) {
            String[] parts = sanitized.split("\\+");
            double sum = 0;
            for (String p : parts) sum += evalSimpleMath(p);
            return sum;
        }
        if (sanitized.contains("-") && !sanitized.startsWith("-")) {
            String[] parts = sanitized.split("-");
            double res = evalSimpleMath(parts[0]);
            for (int i = 1; i < parts.length; i++) res -= evalSimpleMath(parts[i]);
            return res;
        }
        if (sanitized.contains("*")) {
            String[] parts = sanitized.split("\\*");
            double prod = 1;
            for (String p : parts) prod *= evalSimpleMath(p);
            return prod;
        }
        if (sanitized.contains("/")) {
            String[] parts = sanitized.split("/");
            double res = evalSimpleMath(parts[0]);
            for (int i = 1; i < parts.length; i++) res /= evalSimpleMath(parts[i]);
            return res;
        }
        if (sanitized.contains("^")) {
            String[] parts = sanitized.split("\\^");
            return Math.pow(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
        }
        return Double.parseDouble(sanitized);
    }

    private String tryUnitConversion(String prompt) {
        String lower = prompt.toLowerCase();
        // km to miles
        Matcher m1 = Pattern.compile("([0-9\\.]+)\\s*(?:km|kilometers)\\s*to\\s*(?:miles|mi)").matcher(lower);
        if (m1.find()) {
            double km = Double.parseDouble(m1.group(1));
            double miles = km * 0.621371;
            return "📏 **Conversion:** " + km + " km = **" + formatNumber(miles) + " miles**";
        }
        // miles to km
        Matcher m2 = Pattern.compile("([0-9\\.]+)\\s*(?:miles|mi)\\s*to\\s*(?:km|kilometers)").matcher(lower);
        if (m2.find()) {
            double miles = Double.parseDouble(m2.group(1));
            double km = miles / 0.621371;
            return "📏 **Conversion:** " + miles + " miles = **" + formatNumber(km) + " km**";
        }
        // celsius to fahrenheit
        Matcher m3 = Pattern.compile("([0-9\\.\\-]+)\\s*(?:c|celsius)\\s*to\\s*(?:f|fahrenheit)").matcher(lower);
        if (m3.find()) {
            double c = Double.parseDouble(m3.group(1));
            double f = (c * 9.0 / 5.0) + 32;
            return "🌡️ **Temperature:** " + c + "°C = **" + formatNumber(f) + "°F**";
        }
        // fahrenheit to celsius
        Matcher m4 = Pattern.compile("([0-9\\.\\-]+)\\s*(?:f|fahrenheit)\\s*to\\s*(?:c|celsius)").matcher(lower);
        if (m4.find()) {
            double f = Double.parseDouble(m4.group(1));
            double c = (f - 32) * 5.0 / 9.0;
            return "🌡️ **Temperature:** " + f + "°F = **" + formatNumber(c) + "°C**";
        }
        return null;
    }

    // --- General Knowledge & Conversational Intelligence ---

    private String answerGeneralKnowledge(String lower, String prompt) {
        if (lower.startsWith("hi") || lower.startsWith("hello") || lower.startsWith("hey")) {
            return "👋 **Hello! I'm your Built-in Autonomous AI Assistant.**\n\nI run 100% locally on your machine with zero external API dependencies.\n\nHere are some things you can ask me:\n• *\"Summarize this page\"* (extracts insights from your active tab)\n• *\"What is HTTP vs HTTPS?\"*\n• *\"Write a binary search in Java\"*\n• *\"Calculate 25 * 18\"* or *\"convert 50 miles to km\"*\n• Ask me questions about any page you are browsing!";
        }

        if (lower.contains("who are you") || lower.contains("what are you") || lower.contains("what can you do")) {
            return "🤖 **About Me:**\n\nI am the **Autonomous Native AI Engine** embedded directly into your Modern Browser.\n\n• **100% Local & Self-Contained:** I do not rely on external cloud APIs or third-party servers.\n• **Webpage Intelligence:** I analyze, summarize, and answer questions about any page you open in the browser.\n• **Technical & Coding Expertise:** I write and debug code in Java, Python, JavaScript, and SQL.\n• **Fast & Private:** All computations remain completely private on your computer.";
        }

        if (lower.contains("http") && lower.contains("https")) {
            return "🔒 **HTTP vs. HTTPS Breakdown:**\n\n"
                    + "• **HTTP (Hypertext Transfer Protocol):** Data is transmitted in clear plaintext over port 80. Anyone on the same network or ISP can eavesdrop or tamper with data.\n"
                    + "• **HTTPS (HTTP Secure):** Encrypted using TLS/SSL over port 443. Protects confidentiality, data integrity, and verifies server identity via certificates.\n\n"
                    + "**Key Takeaway:** Always look for the green padlock icon in this browser's address bar indicating a verified HTTPS connection.";
        }

        if (lower.contains("dns") || lower.contains("how does dns work")) {
            return "🌐 **How DNS (Domain Name System) Works:**\n\n"
                    + "DNS acts as the phonebook of the internet, translating human-friendly domain names (like `google.com`) into computer-routable IP addresses (like `142.250.190.46`).\n\n"
                    + "1. **Browser Cache Check:** Browser looks in local cache.\n"
                    + "2. **OS Resolver:** Queries local router or ISP resolver.\n"
                    + "3. **Root Nameservers (`.`):** Directs to top-level domain (`.com`).\n"
                    + "4. **TLD Nameservers:** Directs to authoritative nameserver.\n"
                    + "5. **Authoritative Server:** Returns IP address to browser to initiate connection.";
        }

        return null;
    }

    private String generateGeneralReasoningResponse(String prompt, String pageTitle) {
        StringBuilder sb = new StringBuilder();
        sb.append("🧠 **Autonomous Reasoning Analysis**\n\n");
        sb.append("### Understanding: \"").append(prompt).append("\"\n\n");
        sb.append("Here is the structured breakdown:\n\n");
        sb.append("1. **Core Intent:** The query addresses `").append(prompt.length() > 60 ? prompt.substring(0, 60) + "..." : prompt).append("`.\n");
        sb.append("2. **Context:** You are browsing inside Modern Browser Platform.\n");
        sb.append("3. **Recommendation:** You can ask me to summarize the current page, write code, calculate mathematical equations, or explain any topic directly without any external API configuration!");
        return sb.toString();
    }

    // --- Helper Text Processing Utilities ---

    private List<String> extractCleanSentences(String text) {
        List<String> list = new ArrayList<>();
        if (text == null || text.isBlank()) return list;

        String[] rawSentences = text.split("(?<=[.!?])\\s+");
        for (String s : rawSentences) {
            String trimmed = s.replaceAll("\\s+", " ").trim();
            // Filter noise, headers, very short lines
            if (trimmed.length() >= 30 && trimmed.length() <= 350
                    && !trimmed.startsWith("<") && !trimmed.startsWith("{")
                    && !trimmed.contains("var ") && !trimmed.contains("function()")) {
                list.add(trimmed);
            }
        }
        return list;
    }

    private Map<String, Integer> computeWordFrequencies(List<String> sentences) {
        Map<String, Integer> freq = new HashMap<>();
        for (String s : sentences) {
            String[] words = s.toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "").split("\\s+");
            for (String w : words) {
                if (w.length() > 3 && !STOP_WORDS.contains(w)) {
                    freq.put(w, freq.getOrDefault(w, 0) + 1);
                }
            }
        }
        return freq;
    }

    private List<ScoredSentence> scoreSentences(List<String> sentences, Map<String, Integer> freq, String title) {
        List<ScoredSentence> scored = new ArrayList<>();
        Set<String> titleWords = title != null ? extractKeywords(title) : Collections.emptySet();

        for (int i = 0; i < sentences.size(); i++) {
            String s = sentences.get(i);
            double score = 0;
            String[] words = s.toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "").split("\\s+");

            for (String w : words) {
                if (freq.containsKey(w)) {
                    score += freq.get(w);
                }
                if (titleWords.contains(w)) {
                    score += 15.0; // Title keyword overlap boost
                }
            }

            // Position boost for intro sentences
            if (i < 3) score += 20.0;

            // Indicator words boost
            String lower = s.toLowerCase();
            if (lower.contains("conclude") || lower.contains("important") || lower.contains("result")
                    || lower.contains("feature") || lower.contains("significant") || lower.contains("benefit")) {
                score += 15.0;
            }

            scored.add(new ScoredSentence(s, score, i));
        }

        scored.sort((a, b) -> Double.compare(b.score, a.score));
        return scored;
    }

    private Set<String> extractKeywords(String input) {
        Set<String> set = new HashSet<>();
        if (input == null) return set;
        String[] words = input.toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "").split("\\s+");
        for (String w : words) {
            if (w.length() > 2 && !STOP_WORDS.contains(w)) {
                set.add(w);
            }
        }
        return set;
    }

    private String formatNumber(double num) {
        if (num == (long) num) return String.format(Locale.US, "%d", (long) num);
        return String.format(Locale.US, "%.2f", num);
    }

    private static class ScoredSentence {
        String text;
        double score;
        int originalIndex;

        ScoredSentence(String text, double score, int originalIndex) {
            this.text = text;
            this.score = score;
            this.originalIndex = originalIndex;
        }
    }
}
