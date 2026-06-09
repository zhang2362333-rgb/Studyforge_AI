import { http, unwrap } from '@/api/http';
import type { IntegrationSetting } from '@/types/api';

export function getIntegrationSettings() {
  return unwrap<IntegrationSetting[]>(http.get('/admin/settings/integrations'));
}

export function saveIntegrationSettings(settings: IntegrationSetting[]) {
  return unwrap<void>(
    http.put(
      '/admin/settings/integrations',
      settings.map((setting) => ({
        settingKey: setting.settingKey,
        settingValue: setting.settingValue,
        secretFlag: setting.secretFlag
      }))
    )
  );
}
