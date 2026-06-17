import os
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import google.generativeai as genai
from supabase import create_client, Client

app = FastAPI(title="NATA AI Backend Engine")

# 1. GOOGLE GEMINI API CONFIGURATION
# API Key kee as keessa kaayi ykn Environment Variable irraa fudhu
GEMINI_API_KEY = "YOUR_GEMINI_API_KEY_HERE"
genai.configure(api_key=GEMINI_API_KEY)

# 2. SUPABASE CLOUD DATABASE CONFIGURATION
SUPABASE_URL = "https://your-project-id.supabase.co"
SUPABASE_KEY = "your-anon-public-key-here"
supabase: Client = create_client(SUPABASE_URL, SUPABASE_KEY)

# Unka data Android irraa dhufu (Request Payload)
class ChatRequest(BaseModel):
    query: str
    model: str
    user_id: str

@app.post("/v1/api/chat")
async def nata_ai_endpoint(payload: ChatRequest):
    user_query = payload.query.strip()
    selected_model = payload.model
    uid = payload.user_id

    if not user_query:
        raise HTTPException(status_code=400, detail="Query cannot be empty")

    # Toftaa bifa gaaffii addaan baasuu (Text vs Image)
    # Gemini uumamaan afaan gaafatameen waan deebisuuf asirratti dhiphachuu hin barbaachisu
    if "uumi suuraa" in user_query.lower() or "fakkii" in user_query.lower():
        # --- KUTAA SUURAA UUMUU (IMAGE GENERATION PLACEHOLDER) ---
        # Asirratti API Dall-E ykn Stable Diffusion waamuu dandeessa
        generated_url = "https://supabase.co/storage/v1/object/public/images/nata_sample.png"
        
        # Herrega koinii dhimma suuraaf hir'isi
        db_status = deduct_user_coin(uid, amount=5) 
        
        return {
            "type": "image",
            "response": "NATA AI: I have generated the image based on your request!",
            "url": generated_url
        }
        
    else:
        # --- KUTAA TEXT ENGINE (GEMINI GENERATION) ---
        try:
            # Gemini 1.5 Flash baay'ee si'awaa fi lightweight dha
            model_engine = genai.GenerativeModel('gemini-1.5-flash')
            
            # AI-n akka gabaabaa fi qulqulluu deebisuuf qajeelfama itti makuu
            prompt = f"System Instruction: Respond accurately in the language of the prompt. Query: {user_query}"
            
            response = model_engine.generate_content(prompt)
            ai_response_text = response.text

            # Koinii maamilaa text-f hir'isi (Fkn: 1 Coin)
            deduct_user_coin(uid, amount=1)

            return {
                "type": "text",
                "response": ai_response_text
            }

        except Exception as e:
            return {
                "type": "text",
                "response": f"NATA AI Error: Tajaajila Gemini quunnamuun hin danda'amne. {str(e)}"
            }

def deduct_user_coin(user_id: str, amount: int) -> bool:
    """Koinii fayyadamichaa Supabase Database irratti hir'isuuf"""
    try:
        # 1. Koinii amma jiru dubbisi
        res = supabase.table("users").select("coin_balance").eq("id", user_id).execute()
        if res.data:
            current_balance = res.data[0]["coin_balance"]
            if current_balance >= amount:
                new_balance = current_balance - amount
                # 2. Koinii haaraa update godhi
                supabase.table("users").update({"coin_balance": new_balance}).eq("id", user_id).execute()
                return True
    except Exception:
        pass
    return False
