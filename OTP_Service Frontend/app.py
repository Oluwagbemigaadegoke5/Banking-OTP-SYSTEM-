import uvicorn
from fastapi import FastAPI
from pydantic import BaseModel
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI(title="Smart Transaction Risk Engine")

# Allow Spring Boot and Web Browsers to communicate with Python
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class TransactionContext(BaseModel):
    amount: float
    ip_risk_score: float
    consecutive_failures: int

@app.post("/api/v1/risk/evaluate")
def evaluate_risk(context: TransactionContext):
    # Core Machine Learning / Heuristic Scoring Pipeline
    risk_probability = 0.1
    
    # Anomaly checks mirroring behavioral detection trends
    if context.amount > 500000: 
        risk_probability += 0.4  # Excessive amount anomaly
    if context.ip_risk_score > 0.7: 
        risk_probability += 0.3  # Suspicious connection footprint 
    if context.consecutive_failures >= 3: 
        risk_probability += 0.2  # Brute-force local signature
        
    risk_probability = min(risk_probability, 1.0)
    
    # Define system response directives
    recommendation = "APPROVE"
    if risk_probability > 0.75:
        recommendation = "BLOCK"
    elif risk_probability > 0.4:
        recommendation = "CHALLENGE"

    return {
        "riskScore": round(risk_probability, 4),
        "recommendation": recommendation
    }

if __name__ == "__main__":
    uvicorn.run(app, host="127.0.0.1", port=8000)