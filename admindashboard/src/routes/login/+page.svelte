<script lang="ts">
	import { login, setToken, isLoggedIn } from '$lib/api';
	import { goto } from '$app/navigation';

	let password = $state('');
	let error = $state('');
	let loading = $state(false);

	if (isLoggedIn()) {
		goto('/dashboard');
	}

	async function handleSubmit(e: Event) {
		e.preventDefault();
		error = '';
		loading = true;
		try {
			await login(password);
			goto('/dashboard');
		} catch (err: any) {
			error = err.message || 'Login failed';
		} finally {
			loading = false;
		}
	}
</script>

<div class="login-page">
	<div class="login-card animate-in">
		<h1>Sik<span style="color:var(--accent)">Ai</span></h1>
		<p>Admin Dashboard</p>

		{#if error}
			<div class="error-msg">{error}</div>
		{/if}

		<form onsubmit={handleSubmit}>
			<div class="form-group">
				<label for="password">Admin Password</label>
				<input
					id="password"
					type="password"
					class="form-input"
					placeholder="Enter admin password"
					bind:value={password}
					autocomplete="current-password"
					required
				/>
			</div>
			<button class="btn btn-primary" type="submit" disabled={loading || !password}>
				{loading ? 'Signing in...' : 'Sign In'}
			</button>
		</form>
	</div>
</div>