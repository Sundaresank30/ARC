# ARC Manufacturing Frontend

A modern, high-performance web interface for the **ARC Manufacturing System**, built with React 19, TypeScript, Vite, and Tailwind CSS. The application provides role-tailored dashboards and operational modules for **Managers** and **Operators**.

---

## 🛠️ Technology Stack

- **Framework**: [React 19](https://react.dev/) + [TypeScript](https://www.typescriptlang.org/)
- **Build Tool**: [Vite 6](https://vitejs.dev/)
- **Styling**: [Tailwind CSS](https://tailwindcss.com/) + Custom Design System
- **State Management**: [Zustand 5](https://github.com/pmndrs/zustand) (with persistent authentication storage)
- **Data Fetching**: [TanStack Query v5 (React Query)](https://tanstack.com/query) + [Axios](https://axios-http.com/)
- **Data Tables & Charts**: [AG Grid React](https://www.ag-grid.com/react-data-grid/) + [Recharts](https://recharts.org/)
- **Icons & UI**: [Lucide React](https://lucide.dev/)
- **Form Validation**: React Hook Form + Zod

---

## 👥 Role-Based Modules

Access control and navigation dynamically adapt based on the logged-in user role:

### 👔 Manager Role
Designed for line supervision, monitoring batch progress, and resolving production exceptions.
- **Dashboard**: Real-time KPI summary (completed count, failure rates, total batches), exception tracking table for unresolved carry-forward jobs and leakage test failures.
- **Data Preparation**: Queueing jobs, batch generation, and preparing manufacturing work orders.
- **Settings**: System configurations and account preferences.

### 👷 Operator Role
Focused on shop-floor operations and machine execution.
- **Data Embossing**: Real-time embossing machine queue, active job status (`IN_MACHINE`, `PRINTING`, `COMPLETED`), and manual controls.
- **Leakage Testing**: Quality testing interface tracking pressure test values, failure attempts, and scrap/re-test actions.
- **Machine**: Machine chamber monitoring, active job inspection, status transitions (`waiting` → `completed`), and system resets.
- **Settings**: Local workstation settings.

---

## 📁 Project Structure

```text
arc-frontend/
├── public/                 # Static assets and illustrations
├── src/
│   ├── api/                # Axios API service calls (auth, embossing, machine, leakage)
│   ├── components/         # Modular React UI components
│   │   ├── Dashboard/      # Main layout, sidebar, status cards, exception tables
│   │   ├── DataEmbossing/  # Embossing queue & execution view
│   │   ├── DataPreparation/# Job preparation table & batch forms
│   │   ├── LeakageTesting/ # Leakage testing UI & failure logs
│   │   ├── Machine/        # Machine chamber detail view & status manager
│   │   └── login/          # Role selection & login portal
│   ├── config/             # API configuration and endpoints
│   ├── hooks/              # Custom React hooks (React Query wrappers)
│   ├── store/              # Zustand global auth store (`authStore.ts`)
│   ├── types.ts            # TypeScript interfaces & domain types
│   ├── utils/              # Helper utilities & navigation mappers
│   ├── App.tsx             # Root application component
│   └── main.tsx            # Application entry point
├── index.html              # Entry HTML document
├── package.json            # Project dependencies & scripts
├── tailwind.config.js      # Tailwind CSS configuration
├── tsconfig.json           # TypeScript configuration
└── vite.config.ts          # Vite bundler configuration
```

---

## 🚀 Getting Started

### Prerequisites

Ensure you have [Node.js](https://nodejs.org/) (v18+ recommended) and `npm` installed.

### Installation

1. Navigate to the `arc-frontend` directory:
   ```bash
   cd arc-frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

### Development Server

Start the local development server with Hot Module Replacement (HMR):

```bash
npm run dev
```

The application will be accessible at `http://localhost:3000/`.

### Production Build

To build the application for production deployment:

```bash
npm run build
```

To preview the built production bundle locally:

```bash
npm run preview
```

---

## 🔗 Backend API Connectivity

- The frontend communicates with the Spring Boot backend running on `http://localhost:8080`.
- Authentication uses JWT tokens stored in `localStorage`.
- Automatic fallback mechanisms are included to allow offline development mode if the backend service is offline.
