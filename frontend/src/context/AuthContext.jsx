import { createContext, useContext, useState, useEffect, useCallback } from 'react'
import client from '../api/client.js'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('user')
    return stored ? JSON.parse(stored) : null
  })
  const [cartCount, setCartCount] = useState(0)

  const isLoggedIn = !!user

  const refreshCartCount = useCallback(async () => {
    try {
      const res = await client.get('/api/cart')
      setCartCount(res.data.totalItems ?? 0)
    } catch {
      setCartCount(0)
    }
  }, [])

  useEffect(() => {
    refreshCartCount()
  }, [user, refreshCartCount])

  async function login(credentials) {
    const res = await client.post('/api/users/login', credentials)
    const { accessToken, refreshToken, user: userData } = res.data
    localStorage.setItem('accessToken', accessToken)
    localStorage.setItem('refreshToken', refreshToken)
    localStorage.setItem('user', JSON.stringify(userData))
    setUser(userData)
    return userData
  }

  async function logout() {
    const refreshToken = localStorage.getItem('refreshToken')
    try {
      if (refreshToken) {
        await client.post('/api/users/logout', { refreshToken })
      }
    } finally {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('user')
      setUser(null)
      setCartCount(0)
    }
  }

  return (
    <AuthContext.Provider value={{ user, isLoggedIn, cartCount, setCartCount, refreshCartCount, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
