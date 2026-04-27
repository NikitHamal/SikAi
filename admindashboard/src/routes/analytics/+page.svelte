<script lang="ts">
	import { getAnalytics, type AnalyticsData } from '$lib/api';

	let data = $state<AnalyticsData | null>(null);
	let loading = $state(true);
	let error = $state('');
	let days = $state(30);

	async function load() {
		loading = true;
		error = '';
		try {
			data = await getAnalytics(days);
		} catch (e: any) {
			error = e.message;
		} finally {
			loading = false;
		}
	}

	load();

	function formatTime(ms: number) {
		return new Date(ms).toLocaleDateString('en-US', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
	}
</script>

<div class="page-header">
	<h2>Analytics</h2>
	<p>User activity and engagement metrics</p>
</div>

<div style="display:flex; gap:8px; margin-bottom:20px;">
	{#each [7, 30, 90] as d}
		<button class="btn btn-sm" class:btn-primary={days === d} class:btn-secondary={days !== d} onclick={() => { days = d; load(); }}>
			{d}d
		</button>
	{/each}
</div>

{#if loading}
	<p style="color:var(--text-tertiary)">Loading...</p>
{:else if error}
	<div class="error-msg">{error}</div>
{:else if data}
	<div class="stats-grid animate-in">
		<div class="stat-card">
			<div class="label">Total Events ({data.period})</div>
			<div class="value accent">{data.totalEvents.toLocaleString()}</div>
		</div>
		{#each Object.entries(data.byType) as [type, count]}
			<div class="stat-card">
				<div class="label">{type.replace(/_/g, ' ')}</div>
				<div class="value green">{count.toLocaleString()}</div>
			</div>
		{/each}
	</div>

	<div style="display:grid; grid-template-columns:1fr 2fr; gap:16px; margin-top:24px;">
		<div class="card">
			<h3 style="font-size:14px; font-weight:600; margin-bottom:16px;">By Class Level</h3>
			{#each Object.entries(data.byClassLevel).sort(([a],[b]) => Number(a) - Number(b)) as [cls, count]}
				<div style="display:flex; justify-content:space-between; padding:8px 0; border-bottom:1px solid var(--border-subtle);">
					<span style="color:var(--text-secondary)">Class {cls}</span>
					<span style="font-weight:600">{count.toLocaleString()}</span>
				</div>
			{/each}
			{#if !Object.keys(data.byClassLevel).length}
				<div style="color:var(--text-tertiary); font-size:13px; padding:16px 0; text-align:center;">No data yet</div>
			{/if}
		</div>

		<div class="card">
			<h3 style="font-size:14px; font-weight:600; margin-bottom:16px;">Recent Events</h3>
			<div class="table-wrap">
				<table>
					<thead>
						<tr>
							<th>Event</th>
							<th>Class</th>
							<th>Subject</th>
							<th>Device</th>
							<th>Time</th>
						</tr>
					</thead>
					<tbody>
						{#each data.recent.slice(0, 50) as event}
							<tr>
								<td><span class="badge badge-accent">{event.eventType.replace(/_/g, ' ')}</span></td>
								<td>{event.classLevel ?? '—'}</td>
								<td>{event.subject ?? '—'}</td>
								<td style="font-family:monospace; font-size:11px; color:var(--text-tertiary)">{event.deviceId.slice(0, 8)}...</td>
								<td style="font-size:12px; color:var(--text-tertiary)">{formatTime(event.createdAt)}</td>
							</tr>
						{/each}
					</tbody>
				</table>
			</div>
		</div>
	</div>
{/if}