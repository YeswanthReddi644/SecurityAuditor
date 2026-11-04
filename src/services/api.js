const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '');
const TOKEN_KEY = 'securescan_jwt';

export const authStorage = {
  getToken: () => localStorage.getItem(TOKEN_KEY),
  setToken: (token) => localStorage.setItem(TOKEN_KEY, token),
  clear: () => localStorage.removeItem(TOKEN_KEY),
};

async function request(path, { method = 'GET', body, auth = true } = {}) {
  const headers = { Accept: 'application/json, text/plain, */*' };
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  const token = authStorage.getToken();
  if (auth && token) headers.Authorization = `Bearer ${token}`;

  let response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch {
    throw new Error(`Cannot reach the backend at ${API_BASE_URL}. Start the Spring Boot server and check the API URL.`);
  }

  const raw = await response.text();
  let data = raw;
  if (raw) {
    try { data = JSON.parse(raw); } catch { /* Spring may return plain text */ }
  }
  if (!response.ok) {
    if (response.status === 401 && auth) {
      authStorage.clear();
    }
    const message = typeof data === 'string' ? data : data?.message || data?.error || JSON.stringify(data);
    throw new Error(message || `Request failed (${response.status})`);
  }
  return data;
}

export const api = {
  async login(credentials) {
    const response = await request('/api/v1/auth/login', { method: 'POST', body: credentials, auth: false });
    const token = typeof response === 'string' ? response.trim().replace(/^"|"$/g, '') : response?.token;
    if (!token) throw new Error('Login succeeded but the backend did not return a JWT token.');
    authStorage.setToken(token);
    return token;
  },
  async signup(details) {
    return request('/api/v1/users', { method: 'POST', body: details, auth: false });
  },
  async getRepositories() {
    return request('/api/v1/repositories');
  },
  async addRepository(repository) {
    return request('/api/v1/repositories', { method: 'POST', body: repository });
  },
  async startScan(scanRequest) {
    return request('/api/v1/scans', { method: 'POST', body: scanRequest });
  },
  async getScan(scanId) {
    return request(`/api/v1/scans/${encodeURIComponent(scanId)}`);
  },
  async getScanHistory(repositoryId) {
    return request(`/api/v1/scans/history/${encodeURIComponent(repositoryId)}`);
  },
  async getFindings(scanId) {
    return request(`/api/v1/scans/${encodeURIComponent(scanId)}/findings`);
  },
};
