import { Navigate, Route, Routes } from 'react-router-dom';

import { AppLayout } from './layouts/AppLayout';
import { ProtectedRoute } from './routes/ProtectedRoute';
import { PublicRoute } from './routes/PublicRoute';
import { DashboardPage } from '../pages/DashboardPage';
import { LearningGoalDetailsPage } from '../pages/LearningGoalDetailsPage';
import { LearningGoalsPage } from '../pages/LearningGoalsPage';
import { LoginPage } from '../pages/LoginPage';
import { PlaceholderPage } from '../pages/PlaceholderPage';
import { RegisterPage } from '../pages/RegisterPage';
import { TasksPage } from '../pages/TasksPage';

export function AppRoutes() {
  return (
    <Routes>
      <Route element={<PublicRoute />}>
        <Route element={<LoginPage />} path="/login" />
        <Route element={<RegisterPage />} path="/register" />
      </Route>

      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route element={<Navigate replace to="/dashboard" />} index />
          <Route element={<DashboardPage />} path="/dashboard" />
          <Route element={<LearningGoalsPage />} path="/goals" />
          <Route element={<LearningGoalDetailsPage />} path="/goals/:goalId" />
          <Route element={<PlaceholderPage title="Notes" />} path="/notes" />
          <Route element={<TasksPage />} path="/tasks" />
        </Route>
      </Route>

      <Route element={<Navigate replace to="/dashboard" />} path="*" />
    </Routes>
  );
}
