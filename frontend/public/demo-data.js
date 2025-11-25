/**
 * Demo Data Module
 * Provides high-quality sample responses for offline testing and demos
 */

const DEMO_DATA = {
    // Scenario 1: High-risk fake news - sensational language, no credible sources
    highRiskFake: {
        title: "BREAKING: SHOCKING Discovery That Changes EVERYTHING!!!",
        content: "BREAKING NEWS!!! Scientists have made an UNBELIEVABLE discovery that will SHOCK the world. This AMAZING finding is INCREDIBLE and will change your life FOREVER. You won't believe what happened next! Sources say this is the most CATASTROPHIC event in history. Everyone is TERRIFIED by these developments. The implications are HUGE and will affect everyone on the planet!!!",
        source: "https://clickbait-news.net/breaking",
        response: {
            articleId: "demo-1",
            title: "BREAKING: SHOCKING Discovery That Changes EVERYTHING!!!",
            source: "https://clickbait-news.net/breaking",
            credibilityScore: 0.89,
            classification: "LIKELY_FAKE",
            explanation: "This article exhibits multiple indicators of misinformation including excessive sensational language, lack of credible sourcing, and clickbait patterns.",
            featureScores: {
                content_analysis: 0.92,
                domain_credibility: 0.95,
                claims_verification: 0.85,
                cross_reference: 0.78
            },
            claimsCount: 2,
            keyReasons: [
                "Excessive use of sensational language (SHOCKING, UNBELIEVABLE, CATASTROPHIC)",
                "No attribution to credible sources or specific individuals",
                "Clickbait-style headline with emotional manipulation",
                "Domain has low credibility rating",
                "Lack of verifiable facts or evidence"
            ],
            robertaConfidence: 0.94,
            sentimentScore: -0.65,
            writingQuality: {
                clickbait_score: 9.2,
                caps_ratio: 0.28,
                exclamation_count: 8,
                avg_sentence_length: 15.2
            },
            suspiciousPhrases: [
                { phrase: "SHOCKING", reason: "Sensational capitalization" },
                { phrase: "You won't believe", reason: "Classic clickbait pattern" },
                { phrase: "CATASTROPHIC event in history", reason: "Hyperbolic claim" },
                { phrase: "Everyone is TERRIFIED", reason: "Appeal to fear" }
            ],
            riskLevel: { level: "HIGH", icon: "🚨" },
            extractedClaims: [
                { text: "Scientists made discovery", status: "UNVERIFIED", evidenceDensity: 0 },
                { text: "Will affect everyone on planet", status: "UNVERIFIED", evidenceDensity: 0 }
            ],
            attentionTokens: [
                { token: "BREAKING", weight: 0.95 },
                { token: "NEWS", weight: 0.72 },
                { token: "Scientists", weight: 0.45 },
                { token: "UNBELIEVABLE", weight: 0.92 },
                { token: "discovery", weight: 0.68 },
                { token: "SHOCK", weight: 0.88 },
                { token: "world", weight: 0.52 }
            ],
            topWords: [
                { word: "SHOCKING", importance: 0.95 },
                { word: "UNBELIEVABLE", importance: 0.92 },
                { word: "CATASTROPHIC", importance: 0.88 },
                { word: "TERRIFIED", importance: 0.85 }
            ],
            uncertainty: {
                entropy: 0.18,
                variance: 0.0012,
                stdDev: 0.035,
                confidenceInterval: [0.87, 0.91],
                mcSamples: 20
            }
        }
    },

    // Scenario 2: Low-risk credible news - well-sourced, balanced
    lowRiskCredible: {
        title: "Climate Change Report Released by Scientific Panel",
        content: "According to a new report published by the Intergovernmental Panel on Climate Change, global temperatures have risen by 1.1 degrees Celsius since pre-industrial times. Dr. Sarah Johnson, lead author of the study, said the findings are based on comprehensive data analysis spanning three decades. The report was peer-reviewed and published in Nature Climate journal. Multiple research institutions contributed to the study, including MIT, Stanford, and Oxford University. The panel recommends immediate action to reduce carbon emissions.",
        source: "https://reuters.com/environment/climate-report-2024",
        response: {
            articleId: "demo-2",
            title: "Climate Change Report Released by Scientific Panel",
            source: "https://reuters.com/environment/climate-report-2024",
            credibilityScore: 0.18,
            classification: "LIKELY_CREDIBLE",
            explanation: "This article demonstrates strong credibility indicators including attribution to reputable sources, peer-reviewed research citations, and balanced reporting without sensational language.",
            featureScores: {
                content_analysis: 0.15,
                domain_credibility: 0.08,
                claims_verification: 0.22,
                cross_reference: 0.25
            },
            claimsCount: 5,
            keyReasons: [
                "Well-sourced with attribution to specific individuals and institutions",
                "Published in peer-reviewed journal (Nature Climate)",
                "Domain is highly credible (reuters.com)",
                "Balanced language without hyperbole",
                "Multiple verifiable claims with evidence"
            ],
            robertaConfidence: 0.91,
            sentimentScore: 0.15,
            writingQuality: {
                clickbait_score: 1.2,
                caps_ratio: 0.02,
                exclamation_count: 0,
                avg_sentence_length: 24.5
            },
            suspiciousPhrases: [],
            riskLevel: { level: "SAFE", icon: "✅" },
            extractedClaims: [
                { text: "Global temperatures risen 1.1°C since pre-industrial times", status: "VERIFIED", evidenceDensity: 8 },
                { text: "Study based on three decades of data", status: "VERIFIED", evidenceDensity: 6 },
                { text: "Published in Nature Climate journal", status: "VERIFIED", evidenceDensity: 9 },
                { text: "MIT, Stanford, Oxford contributed", status: "VERIFIED", evidenceDensity: 7 },
                { text: "Panel recommends emission reduction", status: "SUPPORTED", evidenceDensity: 5 }
            ],
            attentionTokens: [
                { token: "report", weight: 0.35 },
                { token: "Intergovernmental", weight: 0.42 },
                { token: "Panel", weight: 0.38 },
                { token: "Climate", weight: 0.55 },
                { token: "Change", weight: 0.52 },
                { token: "temperatures", weight: 0.48 }
            ],
            topWords: [
                { word: "report", importance: 0.35 },
                { word: "study", importance: 0.32 },
                { word: "research", importance: 0.28 }
            ],
            uncertainty: {
                entropy: 0.12,
                variance: 0.0005,
                stdDev: 0.022,
                confidenceInterval: [0.16, 0.20],
                mcSamples: 20
            }
        }
    },

    // Scenario 3: Medium-risk mixed signals
    mediumRiskMixed: {
        title: "New Technology Breakthrough Announced",
        content: "A major technology company announced a breakthrough in quantum computing today. The CEO claims this will revolutionize the industry and create AMAZING new possibilities. Some experts are SHOCKED by the development, while others remain skeptical about the timeline. The company's stock price increased by 15% following the announcement. Industry analysts said this could be a game-changer, though verification of the claims is still pending.",
        source: "https://technews.example.com/quantum-breakthrough",
        response: {
            articleId: "demo-3",
            title: "New Technology Breakthrough Announced",
            source: "https://technews.example.com/quantum-breakthrough",
            credibilityScore: 0.52,
            classification: "MIXED_SIGNALS",
            explanation: "This article shows mixed credibility indicators - contains some verifiable information but also sensational elements and unverified claims.",
            featureScores: {
                content_analysis: 0.55,
                domain_credibility: 0.48,
                claims_verification: 0.58,
                cross_reference: 0.45
            },
            claimsCount: 4,
            keyReasons: [
                "Mix of factual reporting and sensational language",
                "Some attribution to experts but also vague claims",
                "Stock price movement is verifiable",
                "Contains words like 'AMAZING' and 'SHOCKED' in all caps",
                "Acknowledges skepticism, showing some balance"
            ],
            robertaConfidence: 0.68,
            sentimentScore: 0.35,
            writingQuality: {
                clickbait_score: 5.8,
                caps_ratio: 0.08,
                exclamation_count: 0,
                avg_sentence_length: 19.3
            },
            suspiciousPhrases: [
                { phrase: "AMAZING new possibilities", reason: "Sensational capitalization" },
                { phrase: "experts are SHOCKED", reason: "Emotional language" }
            ],
            riskLevel: { level: "MEDIUM", icon: "⚠️" },
            extractedClaims: [
                { text: "Breakthrough in quantum computing", status: "PENDING", evidenceDensity: 2 },
                { text: "Stock price increased 15%", status: "VERIFIED", evidenceDensity: 7 },
                { text: "Will revolutionize industry", status: "UNVERIFIED", evidenceDensity: 1 },
                { text: "Some experts skeptical", status: "SUPPORTED", evidenceDensity: 3 }
            ],
            attentionTokens: [
                { token: "breakthrough", weight: 0.72 },
                { token: "quantum", weight: 0.58 },
                { token: "AMAZING", weight: 0.85 },
                { token: "SHOCKED", weight: 0.82 },
                { token: "skeptical", weight: 0.45 }
            ],
            topWords: [
                { word: "AMAZING", importance: 0.75 },
                { word: "SHOCKED", importance: 0.68 },
                { word: "breakthrough", importance: 0.55 }
            ],
            uncertainty: {
                entropy: 0.42,
                variance: 0.0098,
                stdDev: 0.099,
                confidenceInterval: [0.44, 0.60],
                mcSamples: 20
            }
        }
    },

    // Scenario 4: Claims-heavy article
    claimsHeavy: {
        title: "Economic Report Shows GDP Growth and Employment Gains",
        content: "The Bureau of Labor Statistics reported that unemployment fell to 3.8% in March, down from 4.1% in February. According to Federal Reserve Chairman Jerome Powell, the economy added 215,000 jobs last month. The Commerce Department stated that GDP grew at 2.4% annually in Q1. Treasury Secretary Janet Yellen said inflation remained at 2.1%, near the Fed's target. Economist Dr. Michael Chen from Harvard noted that consumer spending increased 1.8% quarter-over-quarter.",
        source: "https://wsj.com/economy/gdp-report",
        response: {
            articleId: "demo-4",
            title: "Economic Report Shows GDP Growth and Employment Gains",
            source: "https://wsj.com/economy/gdp-report",
            credibilityScore: 0.12,
            classification: "LIKELY_CREDIBLE",
            explanation: "Highly credible article with multiple verifiable statistical claims attributed to official government sources and named experts.",
            featureScores: {
                content_analysis: 0.10,
                domain_credibility: 0.05,
                claims_verification: 0.15,
                cross_reference: 0.18
            },
            claimsCount: 7,
            keyReasons: [
                "Multiple specific, verifiable statistical claims",
                "Attribution to official government agencies (BLS, Commerce Dept)",
                "Named sources with credentials (Jerome Powell, Janet Yellen)",
                "Highly credible domain (wsj.com)",
                "No sensational language or emotional appeals"
            ],
            robertaConfidence: 0.95,
            sentimentScore: 0.08,
            writingQuality: {
                clickbait_score: 0.5,
                caps_ratio: 0.03,
                exclamation_count: 0,
                avg_sentence_length: 22.1
            },
            suspiciousPhrases: [],
            riskLevel: { level: "SAFE", icon: "✅" },
            extractedClaims: [
                { text: "Unemployment fell to 3.8%", status: "VERIFIED", evidenceDensity: 9 },
                { text: "Down from 4.1% in February", status: "VERIFIED", evidenceDensity: 9 },
                { text: "Economy added 215,000 jobs", status: "VERIFIED", evidenceDensity: 8 },
                { text: "GDP grew 2.4% annually in Q1", status: "VERIFIED", evidenceDensity: 9 },
                { text: "Inflation at 2.1%", status: "VERIFIED", evidenceDensity: 8 },
                { text: "Consumer spending up 1.8%", status: "VERIFIED", evidenceDensity: 7 },
                { text: "Statement by Jerome Powell", status: "VERIFIED", evidenceDensity: 8 }
            ],
            attentionTokens: [
                { token: "Bureau", weight: 0.38 },
                { token: "unemployment", weight: 0.52 },
                { token: "3.8%", weight: 0.45 },
                { token: "Powell", weight: 0.42 },
                { token: "GDP", weight: 0.55 }
            ],
            topWords: [
                { word: "reported", importance: 0.28 },
                { word: "stated", importance: 0.25 }
            ],
            uncertainty: {
                entropy: 0.08,
                variance: 0.0003,
                stdDev: 0.017,
                confidenceInterval: [0.10, 0.14],
                mcSamples: 20
            }
        }
    },

    // Scenario 5: Suspicious domain with moderate content
    suspiciousDomain: {
        title: "Health Officials Issue New Guidelines",
        content: "Health officials announced new dietary guidelines yesterday. The recommendations include eating more fruits and vegetables while reducing processed foods. Experts suggest limiting sugar intake to less than 10% of daily calories. The guidelines also emphasize regular physical activity and adequate sleep.",
        source: "https://health-rumors-blog.net/guidelines",
        response: {
            articleId: "demo-5",
            title: "Health Officials Issue New Guidelines",
            source: "https://health-rumors-blog.net/guidelines",
            credibilityScore: 0.64,
            classification: "SUSPICIOUS",
            explanation: "While the content appears reasonable, the source domain has low credibility and the article lacks specific attribution to named officials or organizations.",
            featureScores: {
                content_analysis: 0.35,
                domain_credibility: 0.88,
                claims_verification: 0.62,
                cross_reference: 0.58
            },
            claimsCount: 3,
            keyReasons: [
                "Source domain has very low credibility rating",
                "Vague attribution ('health officials' without names)",
                "Content itself is scientifically reasonable",
                "Lacks links to official guidelines or sources",
                "No peer-reviewed references"
            ],
            robertaConfidence: 0.72,
            sentimentScore: 0.12,
            writingQuality: {
                clickbait_score: 3.2,
                caps_ratio: 0.01,
                exclamation_count: 0,
                avg_sentence_length: 16.8
            },
            suspiciousPhrases: [
                { phrase: "Health officials", reason: "Vague attribution without names" }
            ],
            riskLevel: { level: "MEDIUM", icon: "⚠️" },
            extractedClaims: [
                { text: "Eat more fruits and vegetables", status: "GENERAL_GUIDANCE", evidenceDensity: 4 },
                { text: "Limit sugar to 10% of calories", status: "SUPPORTED", evidenceDensity: 5 },
                { text: "Emphasize physical activity", status: "GENERAL_GUIDANCE", evidenceDensity: 4 }
            ],
            attentionTokens: [
                { token: "Health", weight: 0.48 },
                { token: "officials", weight: 0.52 },
                { token: "guidelines", weight: 0.58 },
                { token: "dietary", weight: 0.42 }
            ],
            topWords: [
                { word: "guidelines", importance: 0.45 },
                { word: "health", importance: 0.38 }
            ],
            uncertainty: {
                entropy: 0.35,
                variance: 0.0062,
                stdDev: 0.079,
                confidenceInterval: [0.58, 0.70],
                mcSamples: 20
            }
        }
    }
};

// Export for use in main app
window.DEMO_DATA = DEMO_DATA;
