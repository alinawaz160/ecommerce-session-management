import { useState, useEffect, useCallback } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import client from '../api/client.js'
import { useAuth } from '../context/AuthContext.jsx'

export default function CartPage() {
  const navigate = useNavigate()
  const { refreshCartCount, isLoggedIn } = useAuth()
  const [proceeding, setProceeding] = useState(false)
  const [proceedError, setProceedError] = useState('')
  const [cart, setCart] = useState(null)
  const [loading, setLoading] = useState(true)
  const [updating, setUpdating] = useState({})

  const fetchCart = useCallback(async () => {
    setLoading(true)
    try {
      const res = await client.get('/api/cart')
      setCart(res.data)
    } finally {
      setLoading(false)
    }
  }, [])

  // Fetch on mount and whenever auth state changes (e.g. logout)
  useEffect(() => { fetchCart() }, [fetchCart, isLoggedIn])

  async function updateQty(productId, qty) {
    const quantity = parseInt(qty, 10)
    if (isNaN(quantity) || quantity < 1) return
    setUpdating(prev => ({ ...prev, [productId]: true }))
    try {
      await client.put(`/api/cart/items/${productId}`, { quantity })
      await fetchCart()
      await refreshCartCount()
    } finally {
      setUpdating(prev => ({ ...prev, [productId]: false }))
    }
  }

  async function removeItem(productId) {
    setUpdating(prev => ({ ...prev, [productId]: true }))
    try {
      await client.delete(`/api/cart/items/${productId}`)
      await fetchCart()
      await refreshCartCount()
    } finally {
      setUpdating(prev => ({ ...prev, [productId]: false }))
    }
  }

  async function handleProceedToCheckout() {
    setProceedError('')
    setProceeding(true)
    try {
      await client.post('/api/cart/checkout')
      navigate('/checkout')
    } catch (err) {
      setProceedError(err.response?.data?.error || 'Failed to initiate checkout')
    } finally {
      setProceeding(false)
    }
  }

  if (loading) return <div className="loading">Loading cart...</div>

  const items = cart?.items ?? []

  if (items.length === 0) {
    return (
      <div className="cart-page">
        <h1>Your Cart</h1>
        <div className="empty-cart">
          <p>Your cart is empty.</p>
          <Link to="/products" className="btn btn-primary">Shop Now</Link>
        </div>
      </div>
    )
  }

  return (
    <div className="cart-page">
      <h1>Your Cart</h1>
      <table className="cart-table">
        <thead>
          <tr>
            <th>Product</th>
            <th>Price</th>
            <th>Qty</th>
            <th>Subtotal</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {items.map(item => (
            <tr key={item.productId}>
              <td>{item.productName}</td>
              <td>${Number(item.price).toFixed(2)}</td>
              <td>
                <input
                  className="qty-input"
                  type="number"
                  min="1"
                  max="99"
                  defaultValue={item.quantity}
                  disabled={updating[item.productId]}
                  onBlur={e => {
                    const val = parseInt(e.target.value, 10)
                    if (val !== item.quantity && val >= 1) updateQty(item.productId, val)
                  }}
                  onKeyDown={e => {
                    if (e.key === 'Enter') e.target.blur()
                  }}
                />
              </td>
              <td>${Number(item.subtotal).toFixed(2)}</td>
              <td>
                <button
                  className="btn btn-danger"
                  onClick={() => removeItem(item.productId)}
                  disabled={updating[item.productId]}
                >
                  Remove
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <div className="cart-total">
        Total: ${Number(cart.totalAmount).toFixed(2)}
      </div>
      {proceedError && <div className="error-msg">{proceedError}</div>}
      <div className="cart-actions">
        <Link to="/products" className="btn btn-secondary">Continue Shopping</Link>
        <button className="btn btn-primary" onClick={handleProceedToCheckout} disabled={proceeding}>
          {proceeding ? 'Preparing checkout...' : 'Proceed to Checkout'}
        </button>
      </div>
    </div>
  )
}
