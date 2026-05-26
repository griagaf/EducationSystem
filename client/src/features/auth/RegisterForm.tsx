import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';

import { Alert } from '../../components/ui/Alert';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { getApiErrorMessage } from '../../services/apiClient';
import { useAuth } from '../../hooks/useAuth';

export function RegisterForm() {
  const navigate = useNavigate();
  const { register } = useAuth();
  const [displayName, setDisplayName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');
    setIsSubmitting(true);

    try {
      await register({ displayName, email, password });
      navigate('/dashboard', { replace: true });
    } catch (requestError) {
      setError(getApiErrorMessage(requestError));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form className="space-y-5" onSubmit={handleSubmit}>
      {error ? <Alert message={error} /> : null}
      <Input
        autoComplete="name"
        label="Display name"
        name="displayName"
        onChange={(event) => setDisplayName(event.target.value)}
        required
        value={displayName}
      />
      <Input
        autoComplete="email"
        label="Email"
        name="email"
        onChange={(event) => setEmail(event.target.value)}
        required
        type="email"
        value={email}
      />
      <Input
        autoComplete="new-password"
        label="Password"
        minLength={8}
        name="password"
        onChange={(event) => setPassword(event.target.value)}
        required
        type="password"
        value={password}
      />
      <Button className="w-full" disabled={isSubmitting} type="submit">
        {isSubmitting ? 'Creating account...' : 'Create account'}
      </Button>
      <p className="text-center text-sm text-slate-600">
        Уже есть аккаунт?{' '}
        <Link className="font-semibold text-sky-700 hover:text-sky-800" to="/login">
          Войти
        </Link>
      </p>
    </form>
  );
}
