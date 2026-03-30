import { useState } from 'react'
import client from '../api/client.js'
import { useNavigate } from 'react-router-dom'

export default function AddProductPage() {
  const navigate = useNavigate()

  const [form, setForm] = useState({
    name: '',
    price: '',
    stock: '',
    image: '',
    description: '',
    prompt: ''
  })

  const [aiDescription, setAiDescription] = useState('')
  const [generating, setGenerating] = useState(false)
  const [loading, setLoading] = useState(false)
  const [toast, setToast] = useState('')

  function handleChange(e) {
    const { name, value } = e.target
    setForm(prev => ({ ...prev, [name]: value }))
  }

  function showToast(msg) {
    setToast(msg)
    setTimeout(() => setToast(''), 2500)
  }

  // ✅ Generate AI description
  async function generateDescription() {
    if (!form.prompt) {
      showToast('Enter product prompt first')
      return
    }

    setGenerating(true)

    try {
      const res = await client.post('/api/ollama/generate-description', {
        prompt: form.prompt
      })

      setAiDescription(res.data)

    } catch {
      showToast('AI generation failed')
    } finally {
      setGenerating(false)
    }
  }

  // ✅ Submit product
  async function handleSubmit(e) {
    e.preventDefault()
    setLoading(true)

    try {
      await client.post('/api/products', { // ✅ FIXED endpoint
        name: form.name,
        price: Number(form.price),
        stock: Number(form.stock),
        image: form.image,
        description: form.description
      })

      showToast('✅ Product created successfully')

      setTimeout(() => {
        navigate('/products')
      }, 1000)

    } catch {
      showToast('❌ Failed to create product')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="form-container">
      <h1 className="page-title">Add Product</h1>

      <form onSubmit={handleSubmit} className="form">
        <input
          name="name"
          placeholder="Product Name"
          value={form.name}
          onChange={handleChange}
          required
        />

        <input
          name="price"
          type="number"
          placeholder="Price"
          value={form.price}
          onChange={handleChange}
          required
        />

        <input
          name="stock"
          type="number"
          placeholder="Stock"
          value={form.stock}
          onChange={handleChange}
          required
        />

        <input
          name="image"
          placeholder="Image URL"
          value={form.image}
          onChange={handleChange}
        />

        {/* 🔥 Prompt Input */}
        <textarea
          name="prompt"
          placeholder="Short description (AI prompt)"
          value={form.prompt}
          onChange={handleChange}
        />

        {/* ✅ Generate Button */}
        <button
          type="button"
          className="btn"
          onClick={generateDescription}
          disabled={generating}
        >
          {generating ? 'Generating...' : '✨ Generate Description'}
        </button>

        {/* ✅ AI Preview */}
        {aiDescription && (
          <div className="ai-preview">
            <h3>AI Generated Description</h3>
            <p>{aiDescription}</p>

            <button
              type="button"
              className="btn btn-primary"
              onClick={() =>
                setForm(prev => ({
                  ...prev,
                  description: aiDescription
                }))
              }
            >
              Use this description
            </button>
          </div>
        )}

        {/* ✅ Final Description */}
        <textarea
          name="description"
          placeholder="Final Description"
          value={form.description}
          onChange={handleChange}
        />

        <button className="btn btn-primary" disabled={loading}>
          {loading ? 'Creating...' : 'Create Product'}
        </button>
      </form>

      {toast && <div className="toast">{toast}</div>}
    </div>
  )
}