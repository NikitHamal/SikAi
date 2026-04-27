<script lang="ts">
	import { getSubjects, upsertSubjects, deleteSubject, type Subject } from '$lib/api';

	let items = $state<Subject[]>([]);
	let loading = $state(true);
	let error = $state('');
	let showForm = $state(false);

	let formId = $state('');
	let formDisplayName = $state('');
	let formClassLevel = $state(10);
	let formIcon = $state('');
	let formSortOrder = $state(0);

	const CLASS_LEVELS = [8, 10, 12];

	async function load() {
		loading = true;
		error = '';
		try {
			const res = await getSubjects();
			items = res.items;
		} catch (e: any) {
			error = e.message;
		} finally {
			loading = false;
		}
	}

	function openCreate() {
		formId = '';
		formDisplayName = '';
		formClassLevel = 10;
		formIcon = '';
		formSortOrder = 0;
		showForm = true;
	}

	async function handleSave() {
		error = '';
		try {
			const id = formId || `${formClassLevel}-${formDisplayName.toLowerCase().replace(/[^a-z]/g, '')}`;
			await upsertSubjects([{
				id,
				displayName: formDisplayName,
				classLevel: formClassLevel,
				icon: formIcon || null,
				sortOrder: formSortOrder,
			}]);
			showForm = false;
			await load();
		} catch (e: any) {
			error = e.message;
		}
	}

	async function handleDelete(id: string) {
		if (!confirm(`Delete subject "${id}"?`)) return;
		try {
			await deleteSubject(id);
			await load();
		} catch (e: any) {
			error = e.message;
		}
	}

	load();
</script>

<div class="page-header">
	<h2>Subjects</h2>
	<p>Manage subjects available for each class level</p>
</div>

<div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:20px;">
	<span style="color:var(--text-secondary); font-size:13px;">{items.length} subjects</span>
	<button class="btn btn-primary" onclick={openCreate}>+ Add Subject</button>
</div>

{#if error}
	<div class="error-msg">{error}</div>
{/if}

{#if showForm}
	<div class="modal-overlay" onclick={() => showForm = false}>
		<div class="modal" onclick={(e) => e.stopPropagation()}>
			<h3>Add Subject</h3>
			{#if error}<div class="error-msg">{error}</div>{/if}
			<div class="form-grid">
				<div class="form-group">
					<label>Display Name</label>
					<input class="form-input" bind:value={formDisplayName} placeholder="Mathematics" />
				</div>
				<div class="form-group">
					<label>Class Level</label>
					<select class="form-input" bind:value={formClassLevel}>
						{#each CLASS_LEVELS as c}<option value={c}>Class {c}</option>{/each}
					</select>
				</div>
				<div class="form-group">
					<label>Icon (emoji)</label>
					<input class="form-input" bind:value={formIcon} placeholder="📐" />
				</div>
				<div class="form-group">
					<label>Sort Order</label>
					<input class="form-input" type="number" bind:value={formSortOrder} />
				</div>
			</div>
			<div class="modal-actions">
				<button class="btn btn-secondary" onclick={() => showForm = false}>Cancel</button>
				<button class="btn btn-primary" onclick={handleSave}>Save</button>
			</div>
		</div>
	</div>
{/if}

{#if loading}
	<p style="color:var(--text-tertiary)">Loading...</p>
{:else}
	<div class="table-wrap animate-in">
		<table>
			<thead>
				<tr>
					<th>Icon</th>
					<th>ID</th>
					<th>Display Name</th>
					<th>Class Level</th>
					<th>Sort Order</th>
					<th>Actions</th>
				</tr>
			</thead>
			<tbody>
				{#each items as item}
					<tr>
						<td style="font-size:20px">{item.icon || '—'}</td>
						<td style="font-family:monospace; font-size:12px; color:var(--accent)">{item.id}</td>
						<td style="color:var(--text)">{item.displayName}</td>
						<td><span class="badge badge-accent">Class {item.classLevel}</span></td>
						<td>{item.sortOrder}</td>
						<td>
							<button class="btn btn-danger btn-sm" onclick={() => handleDelete(item.id)}>Delete</button>
						</td>
					</tr>
				{/each}
			</tbody>
		</table>
	</div>
{/if}