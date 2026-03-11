import { Link, useSearchParams } from 'react-router-dom'

export default function OrderConfirmationPage() {
  const [searchParams] = useSearchParams()
  const orderNumber = searchParams.get('order')

  return (
    <div className="order-confirmation">
      <div className="order-icon">✅</div>
      <h1>Order Placed!</h1>
      <p style={{ color: '#666', marginBottom: '0.5rem' }}>Thank you for your purchase.</p>
      {orderNumber && (
        <>
          <p style={{ color: '#555', fontSize: '0.9rem' }}>Your order number:</p>
          <div className="order-number">{orderNumber}</div>
        </>
      )}
      <p style={{ color: '#888', fontSize: '0.85rem', margin: '1rem 0' }}>
        A confirmation email has been sent (check server logs for simulated email).
      </p>
      <Link to="/products" className="btn btn-primary" style={{ display: 'inline-block', marginTop: '0.5rem' }}>
        Continue Shopping
      </Link>
    </div>
  )
}
