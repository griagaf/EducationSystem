import { AuthLayout } from '../app/layouts/AuthLayout';
import { LoginForm } from '../features/auth/LoginForm';

export function LoginPage() {
  return (
    <AuthLayout
      subtitle="Введите email и пароль, чтобы открыть рабочую область."
      title="Вход"
    >
      <LoginForm />
    </AuthLayout>
  );
}
