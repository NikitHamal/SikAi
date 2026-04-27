<script lang="ts">
	import { getConfig, updateConfig, type Stats, getStats } from '$lib/api';

	let configItems = $state<Array<{ key: string; value: string; updated_at: number }>>([]);
	let loading = $state(true);
	let error = $state('');
	let success = $state('');
	let editKey = $state<string | null>(null);
	let editValue = $state('');

	async function load() {
		loading = true;
		error = '';
		try {
			const res = await getConfig();
			configItems = res.items;
		} catch (e: any) {
			error = e.message;
		} finally {
			loading = false;
		}
	}

	function startEdit(key: string, value: string) {
		editKey = key;
		editValue = value;
		success = '';
	}

	async function saveEdit() {
		if (!editKey) return;
		error = '';
		success = '';
		try {
			await updateConfig(editKey, editValue);
			success = `Updated ${editKey}`;
			editKey = null;
			await load();
		} catch (e: any) {
			error = e.message;
		}
	}

	load();
</script>

<div class="page-header">
	<h2>Configuration</h2>
	<p>Feature flags, app version, and announcements</p>
</div>

{#if error}
	<div class="error-msg">{error}</div>
{/if}

{#if success}
	<div style="background:var(--green-muted); color:var(--green); padding:10px 14px; border-radius:var(--radius-sm); font-size:13px; margin-bottom:16px;">
		{success}
	</div>
{/if}

{#if loading}
	<p style="color:var(--text-tertiary)">Loading...</p>
{:else}
	<div class="card animate-in">
		<div class="table-wrap">
			<table>
				<thead>
					<tr>
						<th>Key</th>
						<th>Value</th>
						<th>Last Updated</th>
						<th>Actions</th>
					</tr>
				</thead>
				<tbody>
					{#each configItems as item}
						<tr>
							<td style="font-family:monospace; font-size:12px; color:var(--accent)">{item.key}</td>
							<td>
								{#if editKey === item.key}
									<input class="form-input" style="width:100%;padding:4px 8px;font-size:13px" bind:value={editValue} onkeydown={(e) => e.key === 'Enter' && saveEdit()} />
								{:else}
									<span style="color:var(--text)">{item.value || '(empty)'}</span>
								{/if}
							</td>
							<td style="font-size:12px; color:var(--text-tertiary)">
								{new Date(item.updated_at).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}
							</td>
							<td>
								{#if editKey === item.key}
									<button class="btn btn-primary btn-sm" onclick={saveEdit}>Save</button>
									<button class="btn btn-secondary btn-sm" onclick={() => editKey = null}>Cancel</button>
								{:else}
									<button class="btn btn-secondary btn-sm" onclick={() => startEdit(item.key, item.value)}>Edit</button>
								{/if}
							</td>
						</tr>
					{/each}
				</tbody>
			</table>
		</div>
	</div>
{/if}