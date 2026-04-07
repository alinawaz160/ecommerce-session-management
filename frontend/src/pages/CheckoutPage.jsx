import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import client from '../api/client.js'
import { useAuth } from '../context/AuthContext.jsx'

export default function CheckoutPage() {
  const navigate = useNavigate()
  const { user, isLoggedIn, refreshCartCount } = useAuth()
  const [cart, setCart] = useState(null)
  const [form, setForm] = useState({ email: '', shippingAddress: '', paymentMethod: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    client.get('/api/cart')
      .then(res => setCart(res.data))
      .catch(() => setError('Failed to load cart'))
      .finally(() => setLoading(false))
  }, [])

  // Pre-fill email for logged-in users
  useEffect(() => {
    if (isLoggedIn && user?.email) {
      setForm(f => ({ ...f, email: user.email }))
    }
  }, [isLoggedIn, user])

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  async function handleSubmit(e) {
    e.preventDefault()
    if (!form.email.trim() || !form.shippingAddress.trim() || !form.paymentMethod) {
      setError('Please fill in all fields')
      return
    }
    setError('')
    setSubmitting(true)
    try {
      // Snapshot was already created when user clicked "Proceed to Checkout"
      // Just place the order with shipping/payment details
      const orderRes = await client.post('/api/cart/place-order', form)
      const { orderNumber } = orderRes.data

      await refreshCartCount()
      navigate(`/order-confirmation?order=${encodeURIComponent(orderNumber)}`)
    } catch (err) {
      setError(err.response?.data?.error || err.response?.data?.message || 'Checkout failed. Please try again.')
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) return <div className="loading">Loading...</div>

  const items = cart?.items ?? []

  if (items.length === 0) {
    return (
      <div style={{ textAlign: 'center', padding: '4rem' }}>
        <p>Your cart is empty. <a href="/products">Shop now</a></p>
      </div>
    )
  }

  return (
    <div>
      <h1 className="page-title">Checkout</h1>
      {error && <div className="error-msg">{error}</div>}
      <div className="checkout-layout">
        {/* Order Summary */}
        <div className="checkout-section">
          <h2>Order Summary</h2>
          <table className="summary-table">
            <tbody>
              {items.map(item => (
                <tr key={item.productId}>
                  <td>{item.productName} × {item.quantity}</td>
                  <td style={{ textAlign: 'right' }}>${Number(item.subtotal).toFixed(2)}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <div className="summary-total">
            Total: ${Number(cart.totalAmount).toFixed(2)}
          </div>
        </div>

        {/* Shipping & Payment Form */}
        <div className="checkout-section">
          <h2>Shipping & Payment</h2>
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>Email</label>
              <input
                type="email"
                name="email"
                value={form.email}
                onChange={handleChange}
                placeholder="Enter your email"
                readOnly={isLoggedIn}
                style={isLoggedIn ? { background: '#f5f5f5', cursor: 'not-allowed' } : {}}
                required
              />
            </div>
            <div className="form-group">
              <label>Shipping Address</label>
              <textarea
                name="shippingAddress"
                value={form.shippingAddress}
                onChange={handleChange}
                rows={4}
                placeholder="Enter full shipping address"
                required
              />
            </div>
            <div className="form-group">
              <label>Payment Method</label>
              <select name="paymentMethod" value={form.paymentMethod} onChange={handleChange} required>
                <option value="" disabled>Select payment method</option>
                <option value="CREDIT_CARD">Credit Card</option>
                <option value="DEBIT_CARD">Debit Card</option>
                <option value="PAYPAL">PayPal</option>
                <option value="BANK_TRANSFER">Bank Transfer</option>
              </select>
            </div>
            <button
              type="submit"
              className="btn btn-primary"
              style={{ width: '100%', padding: '0.75rem' }}
              disabled={submitting}
            >
              {submitting ? 'Placing Order...' : 'Place Order'}
            </button>
          </form>
        </div>
      </div>
    </div>
  )
}
