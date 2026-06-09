import { http, unwrap } from '@/api/http';
import type { HealthStatus } from '@/types/api';

export function getHealth() {
  return unwrap<HealthStatus>(http.get('/health'));
}
