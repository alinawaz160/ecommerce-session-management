import { useState, useEffect } from 'react'
import client from '../api/client.js'
import { useAuth } from '../context/AuthContext.jsx'

export default function ProductsPage() {
  const { refreshCartCount } = useAuth()
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const [toast, setToast] = useState('')
  const [adding, setAdding] = useState({})

  useEffect(() => {
    client.get('/api/products')
      .then(res => setProducts(res.data))
      .finally(() => setLoading(false))
  }, [])

  function showToast(msg) {
    setToast(msg)
    setTimeout(() => setToast(''), 2500)
  }

  async function addToCart(product) {
    setAdding(prev => ({ ...prev, [product.productId]: true }))
    try {
      await client.post('/api/cart/items', {
        id: product.id,
        name: product.name,
        price: product.price,
        quantity: 1,
        imageUrl: product.imageUrl,
        description : product.description
      })
      await refreshCartCount()
      showToast(`${product.name} added to cart!`)
    } catch {
      showToast('Failed to add item')
    } finally {
      setAdding(prev => ({ ...prev, [product.productId]: false }))
    }
  }

  if (loading) return <div className="loading">Loading products...</div>

  return (
    <div>
      <h1 className="page-title">Products</h1>
      <div className="products-grid">
        {products.map(product => (
          <div key={product.id} className="product-card">
            <img
              className="product-img"
              src={product.imageUrl}
              alt={product.name}
              onError={e => { e.target.src = 'https://placehold.co/600x400?text=No+Image' }}
            />
            <div className="product-info">
              <div className="product-name">{product.name}</div>
              <div className="product-price">${Number(product.price).toFixed(2)}</div>
              <div className="product-stock">{product.stock} in stock</div>
              <button
                className="btn btn-primary"
                style={{ width: '100%' }}
                onClick={() => addToCart(product)}
                disabled={adding[product.id]}
              >
                {adding[product.id] ? 'Adding...' : 'Add to Cart'}
              </button>
            </div>
            <div className='product-description'>
              {product.description}
            </div>
          </div>
        ))}
      </div>
      {toast && <div className="toast">{toast}</div>}
    </div>
  )
}
