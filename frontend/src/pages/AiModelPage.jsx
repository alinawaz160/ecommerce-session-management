import { useState, useEffect, useCallback } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import client from '../api/client.js'
import { useAuth } from '../context/AuthContext.jsx'

export default function AiModelPage() {
    const navigate = useNavigate();
    const [sharedPrompt, setSharedPrompt] = useState('');
    const [responses, setResponses] = useState({
        ollama: '',
        anthropic: '',
        openai: ''
    });
    const [loading, setLoading] = useState(false)
    const [responseOrder, setResponseOrder] = useState([]);
    const models = [
        // { id: 'openai', name: 'OpenAI (GPT-4o)', color: '#2ECC71' },
        // { id: 'chatClient', name: 'Anthropic (Claude)', color: '#9B59B6' },
        { id: 'ollama', name: 'Ollama (Gemma 2)', color: '#E67E22' }
    ];
    const fetchModelResponse = useCallback(async (model, prompt) => {
    setLoading(true)
    try {
      const encodedPrompt = encodeURIComponent(prompt);
      const res = await client.get(`/api/${model}/${encodedPrompt}`)
      return res.data;
    } finally {
      setLoading(false)
    }
  }, [])

  const handlePromptChange = useCallback((value) => {
    setSharedPrompt(value);
  }, []);

  const handleSubmit = useCallback(async () => {
  if (!sharedPrompt.trim()) return;

  setLoading(true);
  setResponseOrder([]);

  const newResponses = {};
  const newOrder = [];

  for (const model of models) {
    const resp = await fetchModelResponse(model.id, sharedPrompt);
    console.log(resp);

    newResponses[model.id] = resp;
    newOrder.push(model.id);
  }

  setResponses(newResponses);
  setResponseOrder(newOrder);
  setLoading(false);
}, [sharedPrompt, fetchModelResponse, models]);

    return (
    <div className="app-container">
      <h1>Exploring Different LLM Models</h1>
      
      <div className="shared-prompt-container">
        <div className="shared-prompt-area">
          <textarea
            value={sharedPrompt}
            onChange={(e) => setSharedPrompt(e.target.value)}
            disabled={loading}
            />

            <button 
            onClick={handleSubmit}
            disabled={loading || !sharedPrompt.trim()}
            >
            {loading ? 'Sending...' : 'Submit'}
            </button>
        </div>
      </div>
      
      {responseOrder.length > 0 && (
        <div className="response-order">
          <h3>Response Order:</h3>
          <ol>
            {responseOrder.map((modelId, index) => {
              const model = models.find(m => m.id === modelId);
              return (
                <li key={modelId} style={{ color: model.color }}>
                  {model.name} {index === 0 ? '(fastest)' : ''}
                </li>
              );
            })}
          </ol>
        </div>
      )}
      
      <div className="model-grid">
        {models.map(model => (
          <div 
            key={model.id} 
            className="model-box"
            style={{ 
              borderColor: model.color,
              // Highlight the fastest model
              boxShadow: responseOrder[0] === model.id ? `0 0 15px ${model.color}` : 'none'
            }}
          >
            <h2 style={{ color: model.color }}>
              {model.name}
              {responseOrder.includes(model.id) && (
                <span className="response-badge">
                  {responseOrder.indexOf(model.id) + 1}
                </span>
              )}
            </h2>
            
            <div className="response-area">
              <h3>Response:</h3>
              <div className="response-content">
                {responses[model.id] ? (
                  <div className="response-text">{responses[model.id]}</div>
                ) : (
                  <div className="placeholder-text">Response will appear here</div>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}