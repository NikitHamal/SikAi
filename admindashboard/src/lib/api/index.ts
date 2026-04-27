const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8787';

let _token: string | null = null;

if (typeof localStorage !== 'undefined') {
	_token = localStorage.getItem('sikai_token');
}

export function getToken(): string | null {
	return _token;
}

export function setToken(t: string | null) {
	_token = t;
	if (typeof localStorage !== 'undefined') {
		if (t) localStorage.setItem('sikai_token', t);
		else localStorage.removeItem('sikai_token');
	}
}

export function isLoggedIn(): boolean {
	return !!_token;
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
	const headers: Record<string, string> = {
		'Content-Type': 'application/json',
		...(options.headers as Record<string, string> || {}),
	};
	if (_token) headers['Authorization'] = `Bearer ${_token}`;

	const res = await fetch(`${API_BASE}${path}`, { ...options, headers });
	if (res.status === 401) {
		setToken(null);
		throw new Error('Unauthorized');
	}
	if (!res.ok) {
		const body = await res.json().catch(() => ({ error: res.statusText }));
		throw new Error(body.error || body.message || `HTTP ${res.status}`);
	}
	return res.json();
}

// ── Auth ──

export async function login(password: string) {
	const res = await fetch(`${API_BASE}/v1/admin/login`, {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify({ password }),
	});
	if (!res.ok) {
		const body = await res.json().catch(() => ({ error: res.statusText }));
		throw new Error(body.error || 'Invalid credentials');
	}
	const data = await res.json();
	setToken(data.token);
	return data;
}

// ── Stats ──

export interface Stats {
	totalManifest: number;
	totalQuestions: number;
	totalSubjects: number;
	totalEvents: number;
	questionsByClass: Record<number, number>;
	manifestByType: Record<string, number>;
}

export function getStats(): Promise<Stats> {
	return request('/v1/admin/stats');
}

// ── Manifest ──

export interface ManifestItem {
	id: string;
	title: string;
	type: string;
	classLevel: number;
	subject: string;
	year: number | null;
	fileUrl: string | null;
	sizeBytes: number;
	checksumSha256: string | null;
	version: number;
	updatedAt: number;
	language: string;
	tags: string[];
}

export interface ManifestListResponse {
	items: ManifestItem[];
	total: number;
	limit: number;
	offset: number;
}

export function getManifest(limit = 50, offset = 0): Promise<ManifestListResponse> {
	return request(`/v1/admin/manifest?limit=${limit}&offset=${offset}`);
}

export function upsertManifest(items: ManifestItem[]): Promise<{ ok: boolean; upserted: number }> {
	return request('/v1/admin/manifest', {
		method: 'POST',
		body: JSON.stringify({ items }),
	});
}

export function updateManifest(id: string, data: Partial<ManifestItem>): Promise<{ ok: boolean }> {
	return request(`/v1/admin/manifest/${encodeURIComponent(id)}`, {
		method: 'PUT',
		body: JSON.stringify(data),
	});
}

export function deleteManifest(id: string): Promise<{ ok: boolean }> {
	return request(`/v1/admin/manifest/${encodeURIComponent(id)}`, { method: 'DELETE' });
}

// ── Questions ──

export interface Question {
	id: string;
	classLevel: number;
	subject: string;
	topic: string;
	prompt: string;
	options: string[];
	correctIndex: number;
	explanation: string | null;
	source: string;
	language: string;
	difficulty: string;
}

export interface QuestionListResponse {
	items: Question[];
	total: number;
	limit: number;
	offset: number;
}

export function getQuestions(classLevel?: number, subject?: string, limit = 50, offset = 0): Promise<QuestionListResponse> {
	const params = new URLSearchParams();
	if (classLevel) params.set('classLevel', String(classLevel));
	if (subject) params.set('subject', subject);
	params.set('limit', String(limit));
	params.set('offset', String(offset));
	return request(`/v1/admin/questions?${params}`);
}

export function createQuestion(q: Question): Promise<{ ok: boolean; id: string }> {
	return request('/v1/admin/questions', {
		method: 'POST',
		body: JSON.stringify(q),
	});
}

export function bulkImportQuestions(questions: Question[]): Promise<{ ok: boolean; upserted: number }> {
	return request('/v1/admin/questions/bulk', {
		method: 'POST',
		body: JSON.stringify({ questions }),
	});
}

export function updateQuestion(id: string, data: Partial<Question>): Promise<{ ok: boolean }> {
	return request(`/v1/admin/questions/${encodeURIComponent(id)}`, {
		method: 'PUT',
		body: JSON.stringify(data),
	});
}

export function deleteQuestion(id: string): Promise<{ ok: boolean }> {
	return request(`/v1/admin/questions/${encodeURIComponent(id)}`, { method: 'DELETE' });
}

// ── Subjects ──

export interface Subject {
	id: string;
	displayName: string;
	classLevel: number;
	icon: string | null;
	sortOrder: number;
}

export function getSubjects(): Promise<{ items: Subject[] }> {
	return request('/v1/admin/subjects');
}

export function upsertSubjects(items: Subject[]): Promise<{ ok: boolean; upserted: number }> {
	return request('/v1/admin/subjects', {
		method: 'POST',
		body: JSON.stringify({ items }),
	});
}

export function deleteSubject(id: string): Promise<{ ok: boolean }> {
	return request(`/v1/admin/subjects/${encodeURIComponent(id)}`, { method: 'DELETE' });
}

// ── Analytics ──

export interface AnalyticsData {
	totalEvents: number;
	byType: Record<string, number>;
	byClassLevel: Record<number, number>;
	recent: Array<{
		deviceId: string;
		eventType: string;
		classLevel?: number;
		subject?: string;
		metadata?: string;
		id: number;
		createdAt: number;
	}>;
	period: string;
}

export function getAnalytics(days = 30): Promise<AnalyticsData> {
	return request(`/v1/admin/analytics?days=${days}`);
}

// ── Config ──

export function getConfig(): Promise<{ items: Array<{ key: string; value: string; updated_at: number }> }> {
	return request('/v1/admin/config');
}

export function updateConfig(key: string, value: string): Promise<{ ok: boolean }> {
	return request(`/v1/admin/config/${encodeURIComponent(key)}`, {
		method: 'PUT',
		body: JSON.stringify({ value }),
	});
}