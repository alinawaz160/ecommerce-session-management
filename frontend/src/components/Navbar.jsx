import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

export default function Navbar() {
  const { isLoggedIn, user, cartCount, logout, isAdmin} = useAuth()
  const navigate = useNavigate()

  async function handleLogout() {
    await logout()
    navigate('/login')
  }

  return (
    <nav className="navbar">
      <Link to="/products" className="navbar-brand">ShopEasy</Link>
      <ul className="navbar-links">
        <li><Link to="/products">Products</Link></li>
        <li className="cart-link">
          <Link to="/cart">
            Cart
            {cartCount > 0 && <span className="cart-badge">{cartCount}</span>}
          </Link>
        </li>
        {isLoggedIn ? (
          <>
           {isAdmin && <li><Link to="/add-products">Add Products</Link></li>}
            <li style={{ color: '#aaa', fontSize: '0.9rem' }}>Hi, {user.username}</li>
            <li>
              <button className="btn btn-outline" onClick={handleLogout}>Logout</button>
            </li>
          </>
        ) : (
          <>
            <li><Link to="/login">Login</Link></li>
            <li><Link to="/register">Register</Link></li>
          </>
        )}
      </ul>
    </nav>
  )
}
