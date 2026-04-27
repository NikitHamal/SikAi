<script lang="ts">
	import { getStats, type Stats } from '$lib/api';

	let stats = $state<Stats | null>(null);
	let loading = $state(true);
	let error = $state('');

	async function loadStats() {
		loading = true;
		error = '';
		try {
			stats = await getStats();
		} catch (err: any) {
			error = err.message;
		} finally {
			loading = false;
		}
	}

	loadStats();
</script>

<div class="page-header">
	<h2>Dashboard</h2>
	<p>Overview of your SikAi content platform</p>
</div>

{#if loading}
	<p style="color: var(--text-tertiary)">Loading...</p>
{:else if error}
	<div class="error-msg">{error}</div>
{:else if stats}
	<div class="stats-grid animate-in">
		<div class="stat-card">
			<div class="label">Content Items</div>
			<div class="value accent">{stats.totalManifest}</div>
		</div>
		<div class="stat-card">
			<div class="label">Questions</div>
			<div class="value green">{stats.totalQuestions}</div>
		</div>
		<div class="stat-card">
			<div class="label">Subjects</div>
			<div class="value yellow">{stats.totalSubjects}</div>
		</div>
		<div class="stat-card">
			<div class="label">Analytics Events</div>
			<div class="value">{stats.totalEvents}</div>
		</div>
	</div>

	<div style="display:grid; grid-template-columns:1fr 1fr; gap:16px; margin-top:24px;">
		<div class="card">
			<h3 style="font-size:14px; font-weight:600; margin-bottom:16px;">Questions by Class</h3>
			{#each Object.entries(stats.questionsByClass || {}) as [cls, count]}
				<div style="display:flex; justify-content:space-between; padding:8px 0; border-bottom:1px solid var(--border-subtle);">
					<span style="color:var(--text-secondary)">Class {cls}</span>
					<span style="font-weight:600">{count}</span>
				</div>
			{/each}
			{#if !Object.keys(stats.questionsByClass || {}).length}
				<div style="color:var(--text-tertiary); font-size:13px; padding:16px 0; text-align:center;">No questions yet</div>
			{/if}
		</div>

		<div class="card">
			<h3 style="font-size:14px; font-weight:600; margin-bottom:16px;">Content by Type</h3>
			{#each Object.entries(stats.manifestByType || {}) as [type, count]}
				<div style="display:flex; justify-content:space-between; padding:8px 0; border-bottom:1px solid var(--border-subtle);">
					<span style="color:var(--text-secondary)">{type.replace(/_/g, ' ')}</span>
					<span style="font-weight:600">{count}</span>
				</div>
			{/each}
			{#if !Object.keys(stats.manifestByType || {}).length}
				<div style="color:var(--text-tertiary); font-size:13px; padding:16px 0; text-align:center;">No content yet</div>
			{/if}
		</div>
	</div>
{/if}