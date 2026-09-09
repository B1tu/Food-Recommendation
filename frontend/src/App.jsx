import { useState, useEffect, useRef } from 'react'
import AIIcon from './AIIcon.png'
import iconLS from './iconLandingScreen.png'
import './App.css'

// ===== TRANSLATIONS =====
const I18N = {
  vie: {
    logoTitle: 'FoodAI',
    logoTagline: 'Người bạn ẩm thực của bạn',
    navNewChat: 'Cuộc trò chuyện mới',
    navRecipes: 'Công thức',
    navHistory: 'Lịch sử',
    navProfile: 'Hồ sơ & Sở thích',
    profileModalTitle: 'Hồ sơ & Sở thích',
    profileAge: 'Tuổi',
    profilePreferences: 'Sở thích ăn uống (vd: phở, món Việt, cay...)',
    profileDietary: 'Chế độ ăn (vd: ít calo, nhiều protein, ít béo)',
    profileBudget: 'Ngân sách mỗi bữa (VND)',
    profileSave: 'Lưu',
    profileCancel: 'Hủy',
    profileSaved: 'Đã lưu hồ sơ.',
    profileSaveFailed: 'Lưu hồ sơ thất bại.',
    recentTitle: 'Cuộc trò chuyện gần đây',
    recentEmpty: 'Chưa có cuộc trò chuyện nào',
    profileBadge: 'Gói miễn phí',
    planBadge: '⭐ Gói miễn phí',
    welcomeTitle: 'Chào bạn!',
    welcomeSub: 'Tôi là FoodAI – trợ lý ẩm thực thông minh của bạn',
    quickFood: 'Gợi ý món ăn hôm nay',
    quickRestaurant: 'Tìm nhà hàng gần đây',
    quickCalories: 'Tính calo',
    quickMenu: 'Tạo thực đơn',
    placeholder: 'Hãy hỏi tôi về món ăn, nhà hàng hoặc bất cứ điều gì liên quan đến ẩm thực...',
    inputVoice: 'Giọng nói',
    landingTitle: 'Khám phá ẩm thực cùng AI',
    landingSub: 'Từ món ăn yêu thích đến nhà hàng phù hợp - tất cả chỉ trong một cuộc trò chuyện.',
    landingCta: 'Bắt đầu ngay →',
    landingHasAccount: 'Đã có tài khoản? Đăng nhập',
    landingIconFood: 'Gợi ý món ăn',
    landingIconCalories: 'Tính calo & dinh dưỡng',
    landingIconMenu: 'Tạo thực đơn',
    landingIconRestaurant: 'Tìm nhà hàng gần đây',
    guestName: 'Khách',
    trialRemaining: (n) => `Dùng thử — còn ${n} câu hỏi`,
    trialLockedTitle: 'Bạn đã dùng hết 3 câu hỏi dùng thử',
    trialLockedSub: 'Đăng ký tài khoản miễn phí để tiếp tục trò chuyện không giới hạn.',
    authRegisterTab: 'Đăng ký',
    authLoginTab: 'Đăng nhập',
    authNameLabel: 'Tên của bạn',
    authEmailLabel: 'Email',
    authPasswordLabel: 'Mật khẩu',
    authConfirmPasswordLabel: 'Xác nhận mật khẩu',
    authSubmitRegister: 'Tạo tài khoản',
    authSubmitLogin: 'Đăng nhập',
    authShowPassword: 'Hiện mật khẩu',
    authHidePassword: 'Ẩn mật khẩu',
    authSocialLabel: 'Hoặc đăng nhập với',
    authFillAll: 'Vui lòng nhập đầy đủ thông tin.',
    authPasswordMismatch: 'Mật khẩu xác nhận không khớp.',
    authPasswordWeak: 'Mật khẩu chưa đủ mạnh, phải bao gồm tối thiểu 8 ký tự, chữ hoa, chữ thường, số và ký tự đặc biệt.',
    authConnectionError: 'Lỗi kết nối. Vui lòng thử lại.',
    authLogout: 'Đăng xuất',
    startTrialError: 'Không thể bắt đầu dùng thử, vui lòng thử lại.',
    pricingTitle: 'Chọn gói phù hợp với bạn',
    pricingCurrentPlan: 'Gói hiện tại',
    pricingChoose: 'Chọn gói này',
    settingsTitle: 'Cài đặt',
    nearbyResultsIntro: 'Dưới đây là danh sách món và nhà hàng gần bạn nhất:',
    voiceListeningTitle: 'Đang lắng nghe...',
    voiceListeningSub: 'Hãy nói cho tôi biết bạn muốn ăn gì nhé!',
  },
  eng: {
    logoTitle: 'FoodAI',
    logoTagline: 'Your Food Companion',
    navNewChat: 'New chat',
    navRecipes: 'Recipes',
    navHistory: 'History',
    navProfile: 'Profile & Preferences',
    profileModalTitle: 'Profile & Preferences',
    profileAge: 'Age',
    profilePreferences: 'Food preferences (e.g. pho, Vietnamese, spicy...)',
    profileDietary: 'Dietary preferences (e.g. low calorie, high protein, low fat)',
    profileBudget: 'Budget per meal (VND)',
    profileSave: 'Save',
    profileCancel: 'Cancel',
    profileSaved: 'Profile saved.',
    profileSaveFailed: 'Failed to save profile.',
    recentTitle: 'Recent chats',
    recentEmpty: 'No conversations yet',
    profileBadge: 'Free Plan',
    planBadge: '⭐ Free plan',
    welcomeTitle: 'Hello!',
    welcomeSub: 'I am FoodAI – your smart culinary assistant',
    quickFood: 'Suggest today\'s dish',
    quickRestaurant: 'Find nearby restaurants',
    quickCalories: 'Calculate calories',
    quickMenu: 'Create meal plan',
    placeholder: 'Ask me about food, restaurants, or anything culinary...',
    inputVoice: 'Voice',
    landingTitle: 'Discover culinary with AI',
    landingSub: 'From favorite dishes to suitable restaurants — all in one conversation.',
    landingCta: 'Get started →',
    landingHasAccount: 'Already have an account? Log in',
    landingIconFood: 'Dish suggestions',
    landingIconCalories: 'Calorie & nutrition info',
    landingIconMenu: 'Create a meal plan',
    landingIconRestaurant: 'Find nearby restaurants',
    guestName: 'Guest',
    trialRemaining: (n) => `Trial — ${n} question${n === 1 ? '' : 's'} left`,
    trialLockedTitle: 'You have used all 3 trial questions',
    trialLockedSub: 'Create a free account to keep chatting without limits.',
    authRegisterTab: 'Register',
    authLoginTab: 'Login',
    authNameLabel: 'Your name',
    authEmailLabel: 'Email',
    authPasswordLabel: 'Password',
    authConfirmPasswordLabel: 'Confirm password',
    authSubmitRegister: 'Create account',
    authSubmitLogin: 'Login',
    authShowPassword: 'Show password',
    authHidePassword: 'Hide password',
    authSocialLabel: 'Or continue with',
    authFillAll: 'Please fill in all fields.',
    authPasswordMismatch: 'Passwords do not match.',
    authPasswordWeak: 'Password is not strong enough — it must have at least 8 characters, an uppercase letter, a lowercase letter, a number, and a special character.',
    authConnectionError: 'Connection error. Please try again.',
    authLogout: 'Log out',
    startTrialError: 'Could not start the trial, please try again.',
    pricingTitle: 'Choose the plan that fits you',
    pricingCurrentPlan: 'Current plan',
    pricingChoose: 'Choose this plan',
    settingsTitle: 'Settings',
    nearbyResultsIntro: 'Here are the dishes and restaurants nearest to you:',
    voiceListeningTitle: 'Listening...',
    voiceListeningSub: 'Tell me what you would like to eat!',
  }
}

const QUICK_ACTIONS = [
  { id: 'food', icon: '🍴' },
  { id: 'restaurant', icon: '📍' },
  { id: 'calories', icon: '🔥' },
  { id: 'menu', icon: '📋' },
]

const FEATURES = [
  { icon: '⭐', vie: 'Khuyến nghị AI', eng: 'AI Recommendation' },
  { icon: '🎤', vie: 'Nhận diện giọng nói', eng: 'Voice recognition' },
  { icon: '📍', vie: 'Tìm nhà hàng gần đây', eng: 'Find nearby restaurants' },
  { icon: '📅', vie: 'Lưu lịch sử ẩm thực', eng: 'Save food history' },
  { icon: '🧮', vie: 'Tính calo', eng: 'Calculate calories' },
]

const QUICK_ACTION_MAP = {
  vie: {
    food: 'Cho tôi gợi ý món ăn hôm nay.',
    restaurant: 'Tìm nhà hàng ngon gần đây.',
    calories: 'Tính toán calo cho bữa ăn của tôi.',
    menu: 'Tạo thực đơn theo nhu cầu của tôi.',
  },
  eng: {
    food: 'Suggest me a dish for today.',
    restaurant: 'Find a nice restaurant near me.',
    calories: 'Calculate the calories for my meal.',
    menu: 'Create a meal plan based on my needs.',
  }
}

const PLANS = [
  {
    id: 'free',
    price: '0đ',
    vieName: 'Miễn phí', engName: 'Free',
    vieFeatures: ['Trò chuyện với AI', 'Gợi ý món ăn & nhà hàng cơ bản', 'Lưu lịch sử trò chuyện'],
    engFeatures: ['Chat with AI', 'Basic dish & restaurant suggestions', 'Save chat history'],
  },
  {
    id: 'basic',
    price: '19.000đ/tháng',
    vieName: 'Cơ bản', engName: 'Basic',
    vieFeatures: ['Mọi tính năng gói Miễn phí', 'Trò chuyện không giới hạn', 'Phản hồi AI nhanh hơn'],
    engFeatures: ['Everything in Free', 'Unlimited chat', 'Faster AI responses'],
  },
  {
    id: 'advanced',
    price: '49.000đ/tháng',
    vieName: 'Nâng cao', engName: 'Advanced',
    vieFeatures: ['Mọi tính năng gói Cơ bản', 'Tính calo & dinh dưỡng chi tiết', 'Tạo thực đơn theo tuần', 'Gợi ý cá nhân hóa theo Hồ sơ & Sở thích'],
    engFeatures: ['Everything in Basic', 'Detailed calorie & nutrition tracking', 'Weekly meal planning', 'Personalized suggestions based on your Profile'],
  },
  {
    id: 'premium',
    price: '99.000đ/tháng',
    vieName: 'Cao cấp', engName: 'Premium',
    vieFeatures: ['Mọi tính năng gói Nâng cao', 'Công thức nấu ăn chi tiết từng bước', 'Ưu đãi độc quyền tại nhà hàng đối tác', 'Hỗ trợ ưu tiên 24/7'],
    engFeatures: ['Everything in Advanced', 'Step-by-step recipes', 'Exclusive partner restaurant deals', '24/7 priority support'],
  },
]

const SETTINGS_CATEGORIES = [
  { id: 'account', icon: '👤', vie: 'Tài khoản', eng: 'Account' },
  { id: 'appearance', icon: '🎨', vie: 'Giao diện', eng: 'Appearance' },
  { id: 'notifications', icon: '🔔', vie: 'Thông báo', eng: 'Notifications' },
  { id: 'language', icon: '🌐', vie: 'Ngôn ngữ', eng: 'Language' },
  { id: 'privacy', icon: '🔒', vie: 'Quyền riêng tư & Dữ liệu', eng: 'Privacy & Data' },
  { id: 'billing', icon: '💳', vie: 'Gói cước', eng: 'Billing' },
]

// Đọc user đã đăng nhập từ lần trước (nếu có) để tự đăng nhập lại khi mở app.
function loadPersistedUser() {
  try {
    const saved = localStorage.getItem('foodai_user')
    return saved ? JSON.parse(saved) : null
  } catch {
    return null
  }
}

// Gom các dòng AIConversation (mỗi dòng = 1 lượt hỏi-đáp) thành các
// "phiên chat" theo sessionId. Dòng cũ chưa có sessionId (tạo trước khi
// có tính năng này) được coi là phiên chat riêng của chính nó, để không
// mất lịch sử cũ.
function groupConversationsIntoSessions(rows, lang) {
  const bySession = new Map()

  for (const row of rows) {
    const key = row.sessionId || `legacy-${row.conversationId}`
    if (!bySession.has(key)) bySession.set(key, [])
    bySession.get(key).push(row)
  }

  const sessions = []
  for (const [key, rows] of bySession) {
    const sorted = [...rows].sort(
      (a, b) => new Date(a.createdAt) - new Date(b.createdAt)
    )
    const first = sorted[0]
    const last = sorted[sorted.length - 1]

    const label =
      (first.userMessage || '').slice(0, 40) ||
      (lang === 'vie' ? 'Cuộc trò chuyện' : 'Conversation')

    const messages = sorted.flatMap(r => ([
      { role: 'user', content: r.userMessage },
      { role: 'ai', content: r.aiResponse },
    ]))

    sessions.push({ sessionId: key, label, lastTime: last.createdAt, messages })
  }

  sessions.sort((a, b) => new Date(b.lastTime) - new Date(a.lastTime))
  return sessions
}

function formatRecentTime(iso, lang) {
  if (!iso) return ''
  const date = new Date(iso)
  const now = new Date()
  const yesterday = new Date(now)
  yesterday.setDate(now.getDate() - 1)

  if (date.toDateString() === now.toDateString()) {
    return lang === 'vie' ? 'Hôm nay' : 'Today'
  }
  if (date.toDateString() === yesterday.toDateString()) {
    return lang === 'vie' ? 'Hôm qua' : 'Yesterday'
  }
  return date.toLocaleDateString(lang === 'vie' ? 'vi-VN' : 'en-US')
}

// Mật khẩu mạnh: tối thiểu 8 ký tự, có chữ hoa, chữ thường, số, ký tự đặc biệt.
function isPasswordStrong(password) {
  return (
    password.length >= 8 &&
    /[a-z]/.test(password) &&
    /[A-Z]/.test(password) &&
    /[0-9]/.test(password) &&
    /[!@#$%^&*(),.?":{}|<>_\-+=[\]/\\;'`~]/.test(password)
  )
}

function App() {
  const [messages, setMessages] = useState([])
  const [input, setInput] = useState('')
  const [chatLoading, setChatLoading] = useState(false)
  const [isListening, setIsListening] = useState(false)
  const recognitionRef = useRef(null)
  const [activeChat, setActiveChat] = useState('new')
  const [showUserPanel, setShowUserPanel] = useState(false)
  const [lang, setLang] = useState('vie')
  const [sidebarExpanded, setSidebarExpanded] = useState(true)
  const chatAreaRef = useRef(null)
  // Local dev: dùng localhost mặc định. Khi deploy, đặt biến môi trường
  // VITE_API_URL trên Render (Static Site) trỏ tới URL backend đã deploy,
  // vd https://ten-backend-cua-ban.onrender.com/api
  const API = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

  // ===== Đăng nhập / Dùng thử =====
  // currentUser: tài khoản thật đã đăng ký/đăng nhập, nhớ qua localStorage.
  // guestUserId + trialCount: phiên dùng thử (chưa đăng ký), chỉ tồn tại
  // trong sessionStorage (mất khi đóng tab) để không phải tạo guest mới
  // mỗi lần refresh giữa chừng dùng thử.
  const [currentUser, setCurrentUser] = useState(loadPersistedUser)
  const [guestUserId, setGuestUserId] = useState(() => {
    const saved = sessionStorage.getItem('foodai_guest_id')
    return saved ? Number(saved) : null
  })
  const [trialCount, setTrialCount] = useState(() => {
    const saved = sessionStorage.getItem('foodai_trial_count')
    return saved ? Number(saved) : 0
  })
  const [isAuthenticated, setIsAuthenticated] = useState(
    () => !!loadPersistedUser() || !!sessionStorage.getItem('foodai_guest_id')
  )
  const isGuest = !currentUser && !!guestUserId
  const effectiveUserId = currentUser ? currentUser.userId : guestUserId
  const TRIAL_LIMIT = 3

  const [showAuthModal, setShowAuthModal] = useState(null) // null | 'login' | 'register'
  const [authForm, setAuthForm] = useState({ name: '', email: '', password: '', confirmPassword: '' })
  const [authError, setAuthError] = useState('')
  const [authSubmitting, setAuthSubmitting] = useState(false)
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)

  // sessionId hiện tại — mọi tin nhắn gửi đi trong phiên này đều mang
  // cùng 1 sessionId để backend/frontend gom lại thành 1 cuộc hội thoại.
  const [sessionId, setSessionId] = useState(() => crypto.randomUUID())
  const [sessions, setSessions] = useState([])
  const [toast, setToast] = useState(null)

  // Hiện thông báo nhỏ, tự ẩn sau 2.5s
  const showToast = (message) => {
    setToast(message)
    setTimeout(() => setToast(null), 2500)
  }
  const showComingSoon = (label) => {
    showToast(`${label} — ${lang === 'vie' ? 'sắp ra mắt' : 'coming soon'}`)
  }

  // ===== Hồ sơ & Sở thích (dùng bởi RecommendationEngine ở backend) =====
  const [showProfileModal, setShowProfileModal] = useState(false)
  const [showPricingModal, setShowPricingModal] = useState(false)
  const [showSettingsModal, setShowSettingsModal] = useState(false)
  const [profileLoading, setProfileLoading] = useState(false)
  const [profileSaving, setProfileSaving] = useState(false)
  const [profileId, setProfileId] = useState(null)
  const [profileForm, setProfileForm] = useState({
    age: '', preferences: '', dietaryPreferences: '', budget: ''
  })

  const openProfileModal = async () => {
    if (!effectiveUserId) return
    setShowProfileModal(true)
    setProfileLoading(true)
    try {
      const res = await fetch(`${API}/profiles/user/${effectiveUserId}`)
      if (!res.ok) throw new Error(`HTTP ${res.status}`)
      const data = await res.json()
      if (data) {
        setProfileId(data.profileId)
        setProfileForm({
          age: data.age ?? '',
          preferences: data.preferences ?? '',
          dietaryPreferences: data.dietaryPreferences ?? '',
          budget: data.budget ?? '',
        })
      } else {
        setProfileId(null)
        setProfileForm({ age: '', preferences: '', dietaryPreferences: '', budget: '' })
      }
    } catch (err) {
      console.error('Không tải được hồ sơ:', err)
    } finally {
      setProfileLoading(false)
    }
  }

  const saveProfile = async () => {
    setProfileSaving(true)
    const payload = {
      userId: effectiveUserId,
      age: profileForm.age === '' ? null : Number(profileForm.age),
      preferences: profileForm.preferences || null,
      dietaryPreferences: profileForm.dietaryPreferences || null,
      budget: profileForm.budget === '' ? null : Number(profileForm.budget),
    }
    try {
      const res = profileId
        ? await fetch(`${API}/profiles/${profileId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload),
          })
        : await fetch(`${API}/profiles`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload),
          })
      if (!res.ok) throw new Error(`HTTP ${res.status}`)
      const saved = await res.json()
      setProfileId(saved.profileId)
      setShowProfileModal(false)
      showToast(t.profileSaved)
    } catch {
      showToast(t.profileSaveFailed)
    } finally {
      setProfileSaving(false)
    }
  }

  // ===== Load lịch sử chat từ backend =====
  const loadSessions = async () => {
    if (!effectiveUserId) return
    try {
      const res = await fetch(`${API}/ai-conversations/user/${effectiveUserId}`)
      if (!res.ok) throw new Error(`HTTP ${res.status}`)
      const data = await res.json()
      setSessions(groupConversationsIntoSessions(data, lang))
    } catch (err) {
      console.error('Không tải được lịch sử chat:', err)
    }
  }

  useEffect(() => {
    if (isAuthenticated && effectiveUserId) loadSessions()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isAuthenticated, effectiveUserId])

  // ===== Vị trí người dùng (để gợi ý nhà hàng gần đây) =====
  // Xin quyền định vị 1 lần khi vào app — nếu bị từ chối/không hỗ trợ thì
  // im lặng bỏ qua, AI sẽ tự báo "chưa có vị trí" khi người dùng hỏi.
  const [userLocation, setUserLocation] = useState(null)

  useEffect(() => {
    if (!isAuthenticated || !navigator.geolocation) return
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setUserLocation({ latitude: pos.coords.latitude, longitude: pos.coords.longitude })
      },
      () => {
        // Người dùng từ chối hoặc lỗi định vị — không làm gì cả.
      },
      { timeout: 8000 }
    )
  }, [isAuthenticated])

  // ===== Speech Recognition =====
  useEffect(() => {
    const SR = window.SpeechRecognition || window.webkitSpeechRecognition
    if (!SR) return
    const recognition = new SR()
    recognition.continuous = false
    recognition.interimResults = false
    recognition.lang = lang === 'vie' ? 'vi-VN' : 'en-US'
    recognition.onstart = () => setIsListening(true)
    recognition.onend = () => setIsListening(false)
    recognition.onresult = (e) => {
      const transcript = e.results[0][0].transcript
      setInput(transcript)
      handleSendMessage(transcript)
    }
    recognition.onerror = () => setIsListening(false)
    recognitionRef.current = recognition
  }, [lang])

  // ===== Send Message =====
  // overrideMessage: dùng khi gọi ngay sau setInput() trong cùng lần render
  // (voice, quick action) — đọc thẳng giá trị mới thay vì đọc "input" từ
  // state, vì state chưa kịp cập nhật lúc hàm này chạy (stale closure).
  const handleSendMessage = async (overrideMessage) => {
    const messageToSend = typeof overrideMessage === 'string' ? overrideMessage : input
    if (!messageToSend.trim()) return
    if (isGuest && trialCount >= TRIAL_LIMIT) {
      setShowAuthModal('register')
      return
    }
    setMessages(prev => [...prev, { role: 'user', content: messageToSend }])
    const sent = messageToSend
    setInput('')
    setChatLoading(true)

    try {
      const res = await fetch(`${API}/ai-conversations`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          userMessage: sent,
          aiResponse: 'AI đang xử lý...',
          userId: effectiveUserId,
          sessionId,
          latitude: userLocation?.latitude ?? null,
          longitude: userLocation?.longitude ?? null,
        })
      })
      if (!res.ok) throw new Error(`HTTP ${res.status}`)
      const data = await res.json()
      setMessages(prev => [...prev, {
        role: 'ai',
        content: data.aiResponse || 'Xin lỗi, hệ thống chưa phản hồi.',
        nearbyRestaurants: data.nearbyRestaurants || [],
      }])
      setActiveChat(sessionId)
      loadSessions()
      if (isGuest) {
        const next = trialCount + 1
        setTrialCount(next)
        sessionStorage.setItem('foodai_trial_count', String(next))
      }
    } catch {
      setMessages(prev => [...prev, { role: 'ai', content: 'Lỗi kết nối. Vui lòng thử lại.' }])
    } finally {
      setChatLoading(false)
      // Auto-scroll xuống tin nhắn mới nhất
      setTimeout(() => {
        chatAreaRef.current?.scrollTo({ top: chatAreaRef.current.scrollHeight, behavior: 'smooth' })
      }, 50)
    }
  }

  // ===== Bắt đầu dùng thử (landing screen) =====
  const handleStartTrial = async () => {
    if (guestUserId) {
      // Đã có phiên khách từ trước (refresh giữa chừng) — dùng lại, không tạo mới.
      setIsAuthenticated(true)
      return
    }
    try {
      // Bảng USER (SQL Server) có ràng buộc NOT NULL trên email/password
      // (ddl-auto=none, không phải do entity Java) — guest không có tài
      // khoản thật nên cần giá trị placeholder duy nhất để thỏa ràng buộc.
      const guestToken = crypto.randomUUID()
      const res = await fetch(`${API}/users`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name: t.guestName,
          email: `guest-${guestToken}@trial.local`,
          password: guestToken,
        }),
      })
      if (!res.ok) throw new Error(`HTTP ${res.status}`)
      const data = await res.json()
      sessionStorage.setItem('foodai_guest_id', String(data.userId))
      sessionStorage.setItem('foodai_trial_count', '0')
      setGuestUserId(data.userId)
      setTrialCount(0)
      setIsAuthenticated(true)
    } catch (err) {
      console.error('Không tạo được phiên dùng thử:', err)
      showToast(t.startTrialError)
    }
  }

  // Hoàn tất đăng ký/đăng nhập: chuyển từ khách (nếu có) sang tài khoản thật.
  const completeLogin = (user) => {
    localStorage.setItem('foodai_user', JSON.stringify(user))
    sessionStorage.removeItem('foodai_guest_id')
    sessionStorage.removeItem('foodai_trial_count')
    setCurrentUser(user)
    setGuestUserId(null)
    setTrialCount(0)
    setIsAuthenticated(true)
    setShowAuthModal(null)
    setAuthError('')
    setAuthForm({ name: '', email: '', password: '', confirmPassword: '' })
    setSessionId(crypto.randomUUID())
    setMessages([])
    setActiveChat('new')
  }

  const handleRegisterSubmit = async () => {
    setAuthError('')
    if (!authForm.name.trim() || !authForm.email.trim() || !authForm.password) {
      setAuthError(t.authFillAll)
      return
    }
    if (!isPasswordStrong(authForm.password)) {
      setAuthError(t.authPasswordWeak)
      return
    }
    if (authForm.password !== authForm.confirmPassword) {
      setAuthError(t.authPasswordMismatch)
      return
    }
    setAuthSubmitting(true)
    try {
      const res = await fetch(`${API}/users`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name: authForm.name,
          email: authForm.email,
          password: authForm.password,
        }),
      })
      const data = await res.json()
      if (!res.ok) {
        setAuthError(data.message || t.authFillAll)
        return
      }
      completeLogin(data)
    } catch {
      setAuthError(t.authConnectionError)
    } finally {
      setAuthSubmitting(false)
    }
  }

  const handleLoginSubmit = async () => {
    setAuthError('')
    if (!authForm.email.trim() || !authForm.password) {
      setAuthError(t.authFillAll)
      return
    }
    setAuthSubmitting(true)
    try {
      const res = await fetch(`${API}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: authForm.email, password: authForm.password }),
      })
      const data = await res.json()
      if (!res.ok) {
        setAuthError(data.message || t.authConnectionError)
        return
      }
      completeLogin(data)
    } catch {
      setAuthError(t.authConnectionError)
    } finally {
      setAuthSubmitting(false)
    }
  }

  const handleLogout = () => {
    localStorage.removeItem('foodai_user')
    sessionStorage.removeItem('foodai_guest_id')
    sessionStorage.removeItem('foodai_trial_count')
    setCurrentUser(null)
    setGuestUserId(null)
    setTrialCount(0)
    setIsAuthenticated(false)
    setMessages([])
    setSessions([])
    setShowUserPanel(false)
  }

  // ===== Voice Toggle =====
  const toggleVoice = () => {
    const rec = recognitionRef.current
    if (!rec) return
    if (isListening) { rec.stop(); setIsListening(false) }
    else { try { rec.start(); setIsListening(true) } catch { setMessages(prev => [...prev, { role: 'ai', content: lang === 'vie' ? 'Không thể bật mic.' : 'Cannot activate microphone.' }]) } }
  }

  // ===== New Chat =====
  const handleNewChat = () => {
    setSessionId(crypto.randomUUID())
    setMessages([])
    setActiveChat('new')
  }

  // ===== Open a past chat session =====
  const handleOpenSession = (session) => {
    setSessionId(session.sessionId)
    setMessages(session.messages)
    setActiveChat(session.sessionId)
  }

  // ===== Quick Action Handler =====
  const handleQuickAction = (id) => {
    const map = QUICK_ACTION_MAP[lang] || QUICK_ACTION_MAP.vie
    const text = map[id] || ''
    setInput(text)
    handleSendMessage(text)
  }

  // ===== Toggles =====
  const toggleLang = (newLang) => {
    setLang(newLang)
  }

  // ===== Get translations =====
  const t = I18N[lang] || I18N.vie
  const recentChats = sessions

  return (
    <div className="app">
      {!isAuthenticated ? (
        <div className="landing-screen">
          <div className="landing-lang-toggle">
            <button
              className={`mode-btn ${lang === 'vie' ? 'active' : ''}`}
              onClick={() => toggleLang('vie')}
              title="Tiếng Việt"
            >
              VIE
            </button>
            <button
              className={`mode-btn ${lang === 'eng' ? 'active' : ''}`}
              onClick={() => toggleLang('eng')}
              title="English"
            >
              ENG
            </button>
          </div>
          <div className="landing-content">
            <div className="landing-logo">
              <span className="landing-logo-text">{t.logoTitle}</span>
              <span className="landing-logo-tagline">{t.logoTagline}</span>
            </div>

            <div className="landing-mascot">
              <img src={iconLS} alt="FoodAI" />
            </div>

            <h1 className="landing-title">{t.landingTitle}</h1>
            <p className="landing-sub">{t.landingSub}</p>

            <div className="landing-icons-row">
              <div className="landing-icon-card">
                <span>🥗</span>
                <span className="icon-tooltip">{t.landingIconFood}</span>
              </div>
              <div className="landing-icon-card">
                <span>🍲</span>
                <span className="icon-tooltip">{t.landingIconCalories}</span>
              </div>
              <div className="landing-icon-card">
                <span>🥤</span>
                <span className="icon-tooltip">{t.landingIconMenu}</span>
              </div>
              <div className="landing-icon-card">
                <span>🍣</span>
                <span className="icon-tooltip">{t.landingIconRestaurant}</span>
              </div>
            </div>

            <button className="landing-cta-btn" onClick={handleStartTrial}>
              {t.landingCta}
            </button>
            <button className="landing-login-link" onClick={() => setShowAuthModal('login')}>
              {t.landingHasAccount}
            </button>
          </div>
        </div>
      ) : (
        <>
          {/* ===== SIDEBAR ===== */}
          <aside className={sidebarExpanded ? 'sidebar expanded' : 'sidebar collapsed'}>
            <div className="sidebar-header">
              <img src={iconLS} alt="AI" className="logo-icon-img" />
              <div className="logo-text">
                <span className="logo-title">{t.logoTitle}</span>
                <span className="logo-tagline">{t.logoTagline}</span>
              </div>
            </div>
            <button className="sidebar-toggle-btn" onClick={() => setSidebarExpanded(!sidebarExpanded)} title={sidebarExpanded ? 'Thu hẹp sidebar' : 'Mở sidebar'}>
              {sidebarExpanded ? '✕' : '☰'}
            </button>

            <nav className={`sidebar-nav ${!sidebarExpanded ? 'collapsed' : ''}`}>
              <button
                className={`nav-item ${activeChat === 'new' ? 'active' : ''} ${!sidebarExpanded ? 'collapsed' : ''}`}
                onClick={handleNewChat}
              >
                <span className="nav-icon">💬</span>
                <span className="nav-label">{t.navNewChat}</span>
              </button>
              <button
                className={`nav-item ${!sidebarExpanded ? 'collapsed' : ''}`}
                onClick={openProfileModal}
              >
                <span className="nav-icon">👤</span>
                <span className="nav-label">{t.navProfile}</span>
              </button>
              <button
                className={`nav-item ${!sidebarExpanded ? 'collapsed' : ''}`}
                onClick={() => showComingSoon(t.navRecipes)}
              >
                <span className="nav-icon">📖</span>
                <span className="nav-label">{t.navRecipes}</span>
              </button>
              <button
                className={`nav-item ${!sidebarExpanded ? 'collapsed' : ''}`}
                onClick={() => showComingSoon(t.navHistory)}
              >
                <span className="nav-icon">🕐</span>
                <span className="nav-label">{t.navHistory}</span>
              </button>
            </nav>

            <div className={`sidebar-recent ${recentChats.length === 0 ? 'empty' : ''}`}>
              {recentChats.length === 0 ? (
                <div className="sidebar-section-title">{t.recentEmpty}</div>
              ) : (
                <>
                  <div className="sidebar-section-title">{t.recentTitle}</div>
                  {recentChats.map(chat => (
                    <button
                      key={chat.sessionId}
                      className={`recent-chat-item ${activeChat === chat.sessionId ? 'active' : ''}`}
                      onClick={() => handleOpenSession(chat)}
                    >
                      <span className="recent-icon">📄</span>
                      <div className="recent-info">
                        <span className="recent-label">{chat.label}</span>
                        <span className="recent-time">{formatRecentTime(chat.lastTime, lang)}</span>
                      </div>
                    </button>
                  ))}
                </>
              )}
            </div>

            <div className={`sidebar-profile ${!sidebarExpanded ? 'collapsed' : ''}`}>
              <div className="profile-avatar">
                {(currentUser?.name || t.guestName).charAt(0).toUpperCase()}
              </div>
              <div className="profile-info">
                <span className="profile-name">{currentUser?.name || t.guestName}</span>
                <span className="profile-badge">
                  {isGuest ? t.trialRemaining(Math.max(TRIAL_LIMIT - trialCount, 0)) : t.profileBadge}
                </span>
              </div>
              <div className="profile-menu-wrap">
                <button className="profile-menu-btn" onClick={() => setShowUserPanel(v => !v)}>⋮</button>
                {showUserPanel && (
                  <div className="profile-dropdown">
                    {isGuest && (
                      <button className="profile-dropdown-item" onClick={() => { setShowUserPanel(false); setShowAuthModal('register') }}>
                        {t.authRegisterTab}
                      </button>
                    )}
                    <button className="profile-dropdown-item" onClick={handleLogout}>
                      {t.authLogout}
                    </button>
                  </div>
                )}
              </div>
            </div>
          </aside>
        </>
      )}

      {/* ===== MAIN CONTENT ===== */}
      <main className="main-content">
        <div className="main-header">
          <div className="mode-toggle">
            <button
              className={`mode-btn ${lang === 'vie' ? 'active' : ''}`}
              onClick={() => toggleLang('vie')}
              title="Tiếng Việt"
            >
              VIE
            </button>
            <button
              className={`mode-btn ${lang === 'eng' ? 'active' : ''}`}
              onClick={() => toggleLang('eng')}
              title="English"
            >
              ENG
            </button>
          </div>
          <div className="header-right">
            <button className="plan-badge" onClick={() => setShowPricingModal(true)}>{t.planBadge}</button>
            <button className="icon-btn" onClick={() => setShowSettingsModal(true)} title="Settings">⚙️</button>
          </div>
        </div>

        <div className="chat-area" ref={chatAreaRef}>
          {messages.length === 0 && (
            <div className="welcome-screen">
              <div className="welcome-robot">
                <img src={iconLS} alt="FoodAI" className="welcome-robot-img" />
              </div>
              <h2 className="welcome-title">{t.welcomeTitle}</h2>
              <p className="welcome-sub">{t.welcomeSub}</p>

              <div className="quick-actions">
                {QUICK_ACTIONS.map(action => (
                  <button
                    key={action.id}
                    className="quick-action-pill"
                    onClick={() => handleQuickAction(action.id)}
                  >
                    <span className="qa-icon">{action.icon}</span>
                    <span className="qa-label">
                      {action.id === 'food' && t.quickFood}
                      {action.id === 'restaurant' && t.quickRestaurant}
                      {action.id === 'calories' && t.quickCalories}
                      {action.id === 'menu' && t.quickMenu}
                    </span>
                  </button>
                ))}
              </div>

              <div className="features-list">
                {FEATURES.map((f, idx) => (
                  <div key={idx} className="feature-item">
                    <span className="feature-icon">{f.icon}</span>
                    <span>{lang === 'vie' ? f.vie : f.eng}</span>
                  </div>
                ))}
              </div>
            </div>
          )}

          <div className="chat-messages-inner">
            {messages.map((msg, idx) => (
              <div key={idx} className={`message ${msg.role}`}>
                {msg.role === 'ai' && <div className="msg-avatar">🤖</div>}
                <div className={`msg-bubble ${msg.role}`}>
                  {msg.nearbyRestaurants && msg.nearbyRestaurants.length > 0 ? (
                    <div className="msg-content">{t.nearbyResultsIntro}</div>
                  ) : (
                    <div className="msg-content">{msg.content}</div>
                  )}
                  {msg.nearbyRestaurants && msg.nearbyRestaurants.length > 0 && (
                    <div className="restaurant-cards">
                      {msg.nearbyRestaurants.map((r, i) => (
                        <div key={i} className="restaurant-card">
                          <div className="restaurant-card-main">
                            <div className="restaurant-card-name">{r.name}</div>
                            <div className="restaurant-card-meta">
                              <span className="restaurant-card-rating">⭐ {r.rating != null ? r.rating.toFixed(1) : '—'}</span>
                              <span className="restaurant-card-distance">📍 {r.distanceKm != null ? `${r.distanceKm} km` : ''}</span>
                            </div>
                            <div className="restaurant-card-address">{r.address}</div>
                            {r.openingHours && (
                              <div className="restaurant-card-hours">🕐 {r.openingHours}</div>
                            )}
                          </div>
                          <button
                            className="restaurant-card-map-btn"
                            onClick={() => showComingSoon('Google Maps')}
                            title={lang === 'vie' ? 'Xem trên Google Maps' : 'View on Google Maps'}
                          >
                            🗺️
                          </button>
                        </div>
                      ))}
                    </div>
                  )}
                  <div className="msg-time">
                    {new Date().toLocaleTimeString(lang === 'vie' ? 'vi-VN' : 'en-US', {
                      hour: '2-digit',
                      minute: '2-digit'
                    })}
                  </div>
                </div>
              </div>
            ))}

            {chatLoading && (
              <div className="message ai">
                <div className="msg-avatar">🤖</div>
                <div className="msg-bubble">
                  <div className="typing-dots"><span></span><span></span><span></span></div>
                </div>
              </div>
            )}
          </div>
        </div>

        <div className="input-area">
          {isGuest && trialCount >= TRIAL_LIMIT ? (
            <div className="trial-locked">
              <p className="trial-locked-title">{t.trialLockedTitle}</p>
              <p className="trial-locked-sub">{t.trialLockedSub}</p>
              <div className="trial-locked-actions">
                <button className="modal-btn-cancel" onClick={() => setShowAuthModal('login')}>
                  {t.authLoginTab}
                </button>
                <button className="modal-btn-save" onClick={() => setShowAuthModal('register')}>
                  {t.authRegisterTab}
                </button>
              </div>
            </div>
          ) : (
            <div className="input-container">
              <textarea
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' && !e.shiftKey) {
                    e.preventDefault()
                    handleSendMessage()
                  }
                }}
                placeholder={t.placeholder}
                rows={1}
              />
              <button className="input-icon-btn" onClick={toggleVoice} title={t.inputVoice}>
                🎤
              </button>
              <button className="send-btn" onClick={handleSendMessage} disabled={!input.trim() || chatLoading}>
                ↑
              </button>
            </div>
          )}
        </div>
      </main>

      {showProfileModal && (
        <div className="modal-overlay" onClick={() => setShowProfileModal(false)}>
          <div className="modal-panel" onClick={(e) => e.stopPropagation()}>
            <h3 className="modal-title">{t.profileModalTitle}</h3>

            {profileLoading ? (
              <div className="modal-loading">...</div>
            ) : (
              <div className="modal-form">
                <label className="modal-field">
                  <span>{t.profileAge}</span>
                  <input
                    type="number"
                    min="0"
                    value={profileForm.age}
                    onChange={(e) => setProfileForm(f => ({ ...f, age: e.target.value }))}
                  />
                </label>
                <label className="modal-field">
                  <span>{t.profilePreferences}</span>
                  <input
                    type="text"
                    value={profileForm.preferences}
                    onChange={(e) => setProfileForm(f => ({ ...f, preferences: e.target.value }))}
                  />
                </label>
                <label className="modal-field">
                  <span>{t.profileDietary}</span>
                  <input
                    type="text"
                    value={profileForm.dietaryPreferences}
                    onChange={(e) => setProfileForm(f => ({ ...f, dietaryPreferences: e.target.value }))}
                  />
                </label>
                <label className="modal-field">
                  <span>{t.profileBudget}</span>
                  <input
                    type="number"
                    min="0"
                    value={profileForm.budget}
                    onChange={(e) => setProfileForm(f => ({ ...f, budget: e.target.value }))}
                  />
                </label>
              </div>
            )}

            <div className="modal-actions">
              <button className="modal-btn-cancel" onClick={() => setShowProfileModal(false)}>
                {t.profileCancel}
              </button>
              <button className="modal-btn-save" onClick={saveProfile} disabled={profileSaving || profileLoading}>
                {t.profileSave}
              </button>
            </div>
          </div>
        </div>
      )}

      {showAuthModal && (
        <div className="modal-overlay" onClick={() => setShowAuthModal(null)}>
          <div className="modal-panel" onClick={(e) => e.stopPropagation()}>
            <div className="auth-tabs">
              <button
                className={`auth-tab ${showAuthModal === 'register' ? 'active' : ''}`}
                onClick={() => { setShowAuthModal('register'); setAuthError('') }}
              >
                {t.authRegisterTab}
              </button>
              <button
                className={`auth-tab ${showAuthModal === 'login' ? 'active' : ''}`}
                onClick={() => { setShowAuthModal('login'); setAuthError('') }}
              >
                {t.authLoginTab}
              </button>
            </div>

            <div className="modal-form">
              {showAuthModal === 'register' && (
                <label className="modal-field">
                  <span>{t.authNameLabel}</span>
                  <input
                    type="text"
                    value={authForm.name}
                    onChange={(e) => setAuthForm(f => ({ ...f, name: e.target.value }))}
                  />
                </label>
              )}
              <label className="modal-field">
                <span>{t.authEmailLabel}</span>
                <input
                  type="email"
                  value={authForm.email}
                  onChange={(e) => setAuthForm(f => ({ ...f, email: e.target.value }))}
                />
              </label>
              <label className="modal-field">
                <span>{t.authPasswordLabel}</span>
                <div className="password-input-wrap">
                  <input
                    type={showPassword ? 'text' : 'password'}
                    value={authForm.password}
                    onChange={(e) => setAuthForm(f => ({ ...f, password: e.target.value }))}
                  />
                  <button
                    type="button"
                    className="password-toggle-btn"
                    onClick={() => setShowPassword(v => !v)}
                    tabIndex={-1}
                    aria-label={showPassword ? t.authHidePassword : t.authShowPassword}
                  >
                    {showPassword ? '🙈' : '👁️'}
                  </button>
                </div>
                {showAuthModal === 'register' && (
                  <span className="password-hint">{t.authPasswordWeak}</span>
                )}
              </label>
              {showAuthModal === 'register' && (
                <label className="modal-field">
                  <span>{t.authConfirmPasswordLabel}</span>
                  <div className="password-input-wrap">
                    <input
                      type={showConfirmPassword ? 'text' : 'password'}
                      value={authForm.confirmPassword}
                      onChange={(e) => setAuthForm(f => ({ ...f, confirmPassword: e.target.value }))}
                    />
                    <button
                      type="button"
                      className="password-toggle-btn"
                      onClick={() => setShowConfirmPassword(v => !v)}
                      tabIndex={-1}
                      aria-label={showConfirmPassword ? t.authHidePassword : t.authShowPassword}
                    >
                      {showConfirmPassword ? '🙈' : '👁️'}
                    </button>
                  </div>
                </label>
              )}
              {authError && <p className="auth-error">{authError}</p>}
            </div>

            <div className="modal-actions">
              <button className="modal-btn-cancel" onClick={() => setShowAuthModal(null)}>
                {t.profileCancel}
              </button>
              <button
                className="modal-btn-save"
                disabled={authSubmitting}
                onClick={showAuthModal === 'register' ? handleRegisterSubmit : handleLoginSubmit}
              >
                {showAuthModal === 'register' ? t.authSubmitRegister : t.authSubmitLogin}
              </button>
            </div>

            <div className="auth-social">
              <span className="auth-social-label">{t.authSocialLabel}</span>
              <div className="auth-social-icons">
                <button className="auth-social-btn google" onClick={() => showComingSoon('Google')}>G</button>
                <button className="auth-social-btn facebook" onClick={() => showComingSoon('Facebook')}>f</button>
                <button className="auth-social-btn twitter" onClick={() => showComingSoon('Twitter')}>𝕏</button>
                <button className="auth-social-btn github" onClick={() => showComingSoon('GitHub')}>gh</button>
              </div>
            </div>
          </div>
        </div>
      )}

      {showPricingModal && (
        <div className="modal-overlay" onClick={() => setShowPricingModal(false)}>
          <div className="modal-panel pricing-panel" onClick={(e) => e.stopPropagation()}>
            <h3 className="modal-title">{t.pricingTitle}</h3>
            <div className="pricing-grid">
              {PLANS.map(plan => (
                <div key={plan.id} className={`pricing-card ${plan.id === 'free' ? 'current' : ''}`}>
                  <div className="pricing-card-name">{lang === 'vie' ? plan.vieName : plan.engName}</div>
                  <div className="pricing-card-price">{plan.price}</div>
                  <ul className="pricing-card-features">
                    {(lang === 'vie' ? plan.vieFeatures : plan.engFeatures).map((f, i) => (
                      <li key={i}>{f}</li>
                    ))}
                  </ul>
                  {plan.id === 'free' ? (
                    <button className="pricing-card-btn current" disabled>{t.pricingCurrentPlan}</button>
                  ) : (
                    <button
                      className="pricing-card-btn"
                      onClick={() => showComingSoon(lang === 'vie' ? plan.vieName : plan.engName)}
                    >
                      {t.pricingChoose}
                    </button>
                  )}
                </div>
              ))}
            </div>
            <div className="modal-actions">
              <button className="modal-btn-cancel" onClick={() => setShowPricingModal(false)}>
                {t.profileCancel}
              </button>
            </div>
          </div>
        </div>
      )}

      {showSettingsModal && (
        <div className="modal-overlay" onClick={() => setShowSettingsModal(false)}>
          <div className="modal-panel settings-panel" onClick={(e) => e.stopPropagation()}>
            <h3 className="modal-title">{t.settingsTitle}</h3>
            <div className="settings-list">
              {SETTINGS_CATEGORIES.map(cat => (
                <button
                  key={cat.id}
                  className="settings-list-item"
                  onClick={() => showComingSoon(lang === 'vie' ? cat.vie : cat.eng)}
                >
                  <span className="settings-item-icon">{cat.icon}</span>
                  <span className="settings-item-label">{lang === 'vie' ? cat.vie : cat.eng}</span>
                  <span className="settings-item-arrow">›</span>
                </button>
              ))}
            </div>
            <div className="modal-actions">
              <button className="modal-btn-cancel" onClick={() => setShowSettingsModal(false)}>
                {t.profileCancel}
              </button>
            </div>
          </div>
        </div>
      )}

      {isListening && (
        <div className="voice-overlay">
          <div className="voice-overlay-header">
            <button className="voice-back-btn" onClick={toggleVoice}>←</button>
            <img src={iconLS} alt="FoodAI" className="voice-header-icon" />
            <span className="voice-header-title">{t.logoTitle}</span>
          </div>
          <div className="voice-overlay-center">
            <div className="voice-mic-rings">
              <div className="voice-ring ring-1"></div>
              <div className="voice-ring ring-2"></div>
              <div className="voice-ring ring-3"></div>
              <div className="voice-mic-circle">🎤</div>
            </div>
            <h2 className="voice-listening-title">{t.voiceListeningTitle}</h2>
            <p className="voice-listening-sub">{t.voiceListeningSub}</p>
            <div className="voice-waveform">
              {Array.from({ length: 24 }).map((_, i) => (
                <span
                  key={i}
                  className="voice-wave-bar"
                  style={{
                    animationDelay: `${(i % 8) * 0.09}s`,
                    animationDuration: `${0.7 + (i % 5) * 0.12}s`,
                  }}
                ></span>
              ))}
            </div>
            <button className="voice-cancel-btn" onClick={toggleVoice}>{t.profileCancel}</button>
          </div>
        </div>
      )}

      {toast && <div className="toast">{toast}</div>}
    </div>
  )
}

export default App