import { redirect } from '@sveltejs/kit';
import type { LayoutLoad } from './$types';
import { isLoggedIn } from '$lib/api';
import { browser } from '$app/environment';

export const ssr = false;
export const prerender = true;

export const load: LayoutLoad = async ({ url }) => {
	if (browser) {
		const publicPaths = ['/login', '/'];
		if (!publicPaths.includes(url.pathname) && !isLoggedIn()) {
			redirect(307, '/login');
		}
	}
};