<script lang="ts">
	import '../lib/app.css';
	import { isLoggedIn, setToken } from '$lib/api';
	import { goto } from '$app/navigation';
	import { page } from '$app/state';

	let { children } = $props();

	const navItems = [
		{ section: 'Overview', items: [
			{ href: '/dashboard', label: 'Dashboard', icon: '■' },
			{ href: '/analytics', label: 'Analytics', icon: '◈' },
		] },
		{ section: 'Content', items: [
			{ href: '/content', label: 'Manifest', icon: '▤' },
			{ href: '/questions', label: 'Questions', icon: '?' },
			{ href: '/subjects', label: 'Subjects', icon: '◒' },
		] },
		{ section: 'System', items: [
			{ href: '/config', label: 'Config', icon: '⚙' },
		] },
	];

	function handleLogout() {
		setToken(null);
		goto('/login');
	}
</script>

{#if !isLoggedIn()}
	{@render children()}
{:else}
	<div class="app-layout">
		<aside class="sidebar">
			<div class="sidebar-brand">
				<h1>Sik<span>Ai</span></h1>
				<small>Admin Dashboard</small>
			</div>
			<nav class="sidebar-nav">
				{#each navItems as section}
					<div class="nav-section">
						<div class="nav-section-title">{section.section}</div>
						{#each section.items as item}
							<a href={item.href} class="nav-link" class:active={page.url.pathname === item.href}>
								<span class="icon">{item.icon}</span>
								{item.label}
							</a>
						{/each}
					</div>
				{/each}
			</nav>
			<div class="sidebar-footer">
				<button class="btn btn-secondary" style="width:100%" onclick={handleLogout}>
					Logout
				</button>
			</div>
		</aside>
		<main class="main-content">
			{@render children()}
		</main>
	</div>
{/if}