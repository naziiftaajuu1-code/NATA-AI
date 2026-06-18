// api/chat.js - NATA AI Premium Serverless Backend Engine
export default async function handler(req, res) {
    // CORS Headers - Weebsaayitiin kee saphaphitti akka qunnamu godha
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'POST, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

    if (req.method === 'OPTIONS') {
        return res.status(200).end();
    }

    if (req.method !== 'POST') {
        return res.status(405).json({ error: 'Method Not Allowed. Only POST requests are accepted.' });
    }

    const { prompt, type, url } = req.body;

    // Configuration for Google Gemini API Key
    // API Key kee gara fulduraatti Vercel Settings -> Environment Variables irratti saagda
    const GEMINI_API_KEY = process.env.GEMINI_API_KEY || "AIzaSyDummyKey_PleaseChangeInVercel";
    const GEMINI_URL = `https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${GEMINI_API_KEY}`;

    // 1. ADVANCED WEBSITE SCANNER LOGIC ENGINE
    if (type === 'scan' && url) {
        try {
            const scanPrompt = `Act as an advanced cyber security and SEO expert. Analyze this website URL thoroughly: ${url}. Provide a premium security and optimization summary in Afaan Oromoo. List safe points, potential vulnerabilities, and performance score out of 100. Formulate your response professionally.`;
            
            const aiResponse = await fetch(GEMINI_URL, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ contents: [{ parts: [{ text: scanPrompt }] }] })
            });

            const data = await aiResponse.json();
            if (data.candidates && data.candidates[0]?.content?.parts[0]?.text) {
                return res.status(200).json({ text: data.candidates[0].content.parts[0].text });
            }
            return res.status(500).json({ error: 'Scanner engine failed to generate premium report.' });
        } catch (err) {
            return res.status(500).json({ error: 'Scanner backend server error: ' + err.message });
        }
    }

    // 2. STANDARD PREMIUM CHATBOT LOGIC ENGINE
    if (!prompt) {
        return res.status(400).json({ error: 'Prompt field cannot be empty.' });
    }

    try {
        const aiResponse = await fetch(GEMINI_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                contents: [{
                    parts: [{ text: prompt }]
                }]
            })
        });

        if (!aiResponse.ok) {
            return res.status(500).json({ error: `Gemini API Error: Status ${aiResponse.status}` });
        }

        const data = await aiResponse.json();

        if (data.candidates && data.candidates[0]?.content?.parts[0]?.text) {
            const replyText = data.candidates[0].content.parts[0].text;
            return res.status(200).json({ text: replyText });
        } else {
            return res.status(500).json({ error: 'AI core returned empty or unparseable data.' });
        }

    } catch (error) {
        console.error("Backend Core Error:", error);
        return res.status(500).json({ error: 'Internal Server Error: ' + error.message });
    }
            }
