# LedgerFlow Frontend

React frontend for the LedgerFlow invoice management system.

## 🚀 Getting Started

### Prerequisites

- Node.js 18+ and npm

### Installation

1. Install dependencies:
```bash
npm install
```

2. Start the development server:
```bash
npm run dev
```

The app will be available at `http://localhost:5173`

### Build for Production

```bash
npm run build
```

The built files will be in the `dist` directory.

## 📁 Project Structure

```
frontend/
├── src/
│   ├── components/          # Reusable UI components
│   │   ├── ProtectedRoute.jsx
│   │   ├── InvoiceUpload.jsx
│   │   └── InvoiceList.jsx
│   ├── pages/               # Page components
│   │   ├── Login.jsx
│   │   ├── Register.jsx
│   │   └── Dashboard.jsx
│   ├── services/            # API service layer
│   │   ├── api.js           # Axios configuration
│   │   ├── authService.js   # Authentication API calls
│   │   └── invoiceService.js # Invoice API calls
│   ├── store/               # Zustand state management
│   │   └── authStore.js     # Authentication store
│   ├── App.jsx              # Main app component with routing
│   ├── main.jsx             # Entry point
│   └── index.css           # Global styles
├── package.json
└── vite.config.js
```

## 🔧 Configuration

### Backend URL

The backend URL is configured in `src/services/api.js`. By default, it points to `http://localhost:8080/api`.

### CORS

Make sure the backend CORS configuration allows requests from `http://localhost:5173`.

## 🎨 Features

- **Authentication**: Login and registration with JWT tokens in httpOnly cookies
- **Dashboard**: View and manage invoices
- **Invoice Upload**: Upload PDF invoices for AI processing
- **Invoice List**: View all invoices with status and actions
- **Protected Routes**: Automatic redirect to login if not authenticated

## 📝 Notes

- Cookies are automatically sent with requests via `withCredentials: true` in Axios
- Authentication state is managed globally with Zustand
- All API calls go through the service layer for consistency

