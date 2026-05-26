import { AuthLayout } from '../app/layouts/AuthLayout';
import { RegisterForm } from '../features/auth/RegisterForm';

export function RegisterPage() {
  return (
    <AuthLayout
      subtitle="Создайте аккаунт для персонального пространства обучения."
      title="Регистрация"
    >
      <RegisterForm />
    </AuthLayout>
  );
}
