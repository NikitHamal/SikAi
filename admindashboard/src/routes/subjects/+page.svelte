<script lang="ts">
	import { getSubjects, upsertSubjects, deleteSubject, bulkDeleteSubjects, type Subject } from '$lib/api';

	let items = $state<Subject[]>([]);
	let loading = $state(true);
	let error = $state('');
	let successMsg = $state('');
	let showForm = $state(false);
	let showImport = $state(false);
	let searchQuery = $state('');
	let selectedIds = $state<Set<string>>(new Set());

	let formId = $state('');
	let formDisplayName = $state('');
	let formClassLevel = $state(10);
	let formIcon = $state('');
	let formSortOrder = $state(0);

	let importJson = $state('');
	let importError = $state('');

	let showConfirmDelete = $state(false);
	let confirmDeleteCount = $state(0);

	const CLASS_LEVELS = [8, 10, 12];

	let filteredItems = $derived(() => {
		let result = items;
		if (searchQuery) {
			const q = searchQuery.toLowerCase();
			result = result.filter(i => i.displayName.toLowerCase().includes(q) || i.id.toLowerCase().includes(q));
		}
		return result;
	});

	let allCurrentSelected = $derived(() => {
		const fi = filteredItems();
		return fi.length > 0 && fi.every(i => selectedIds.has(i.id));
	});

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

	function toggleSelectAll() {
		const fi = filteredItems();
		if (allCurrentSelected()) {
			const newSet = new Set(selectedIds);
			for (const i of fi) newSet.delete(i.id);
			selectedIds = newSet;
		} else {
			const newSet = new Set(selectedIds);
			for (const i of fi) newSet.add(i.id);
			selectedIds = newSet;
		}
	}

	function toggleSelect(id: string) {
		const newSet = new Set(selectedIds);
		if (newSet.has(id)) newSet.delete(id);
		else newSet.add(id);
		selectedIds = newSet;
	}

	function clearSelection() {
		selectedIds = new Set();
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
			successMsg = 'Subject saved';
			setTimeout(() => successMsg = '', 3000);
			await load();
		} catch (e: any) {
			error = e.message;
		}
	}

	async function handleDelete(id: string) {
		if (!confirm(`Delete subject "${id}"?`)) return;
		try {
			await deleteSubject(id);
			successMsg = 'Subject deleted';
			setTimeout(() => successMsg = '', 3000);
			await load();
		} catch (e: any) {
			error = e.message;
		}
	}

	function askBulkDelete() {
		confirmDeleteCount = selectedIds.size;
		showConfirmDelete = true;
	}

	async function confirmBulkDelete() {
		showConfirmDelete = false;
		try {
			await bulkDeleteSubjects([...selectedIds]);
			successMsg = `Deleted ${selectedIds.size} subjects`;
			selectedIds = new Set();
			setTimeout(() => successMsg = '', 3000);
			await load();
		} catch (e: any) {
			error = e.message;
		}
	}

	function exportJson() {
		const data = filteredItems().length && searchQuery ? filteredItems() : items;
		const json = JSON.stringify(data, null, 2);
		const blob = new Blob([json], { type: 'application/json' });
		const url = URL.createObjectURL(blob);
		const a = document.createElement('a');
		a.href = url;
		a.download = `sikai-subjects-${new Date().toISOString().slice(0, 10)}.json`;
		a.click();
		URL.revokeObjectURL(url);
	}

	function exportSelectedJson() {
		const data = items.filter(i => selectedIds.has(i.id));
		const json = JSON.stringify(data, null, 2);
		const blob = new Blob([json], { type: 'application/json' });
		const url = URL.createObjectURL(blob);
		const a = document.createElement('a');
		a.href = url;
		a.download = `sikai-subjects-selected-${new Date().toISOString().slice(0, 10)}.json`;
		a.click();
		URL.revokeObjectURL(url);
	}

	function openImport() {
		importJson = '';
		importError = '';
		showImport = true;
	}

	async function handleImport() {
		importError = '';
		try {
			const parsed = JSON.parse(importJson);
			const itemsToImport: Subject[] = Array.isArray(parsed) ? parsed : parsed.items ?? [parsed];
			if (!itemsToImport.length) { importError = 'No items found in JSON'; return; }
			const res = await upsertSubjects(itemsToImport);
			showImport = false;
			successMsg = `Imported ${res.upserted} subjects`;
			setTimeout(() => successMsg = '', 3000);
			await load();
		} catch (e: any) {
			importError = e.message || 'Invalid JSON format';
		}
	}

	function handleFileImport(e: Event) {
		const input = e.target as HTMLInputElement;
		const file = input.files?.[0];
		if (!file) return;
		const reader = new FileReader();
		reader.onload = () => { importJson = reader.result as string; };
		reader.readAsText(file);
	}

	load();
</script>

<div class="page-header">
	<h2>Subjects</h2>
	<p>Manage subjects available for each class level</p>
</div>

<div style="display:flex; gap:12px; align-items:center; margin-bottom:20px; flex-wrap:wrap;">
	<input class="form-input" style="width:240px;padding:6px 12px;font-size:13px" placeholder="Search subjects..." bind:value={searchQuery} />
	<span style="color:var(--text-secondary); font-size:13px; margin-left:auto;">{items.length} subjects</span>
	<button class="btn btn-primary" onclick={openCreate}>+ Add Subject</button>
</div>

{#if error}
	<div class="error-msg">{error}</div>
{/if}

{#if successMsg}
	<div class="toast-container"><div class="toast toast-success">{successMsg}</div></div>
{/if}

{#if selectedIds.size > 0}
	<div class="bulk-action-bar animate-in">
		<span>{selectedIds.size} selected</span>
		<button class="btn btn-secondary btn-sm" onclick={clearSelection}>Clear</button>
		<button class="btn btn-secondary btn-sm" onclick={exportSelectedJson}>Export Selected</button>
		<button class="btn btn-danger btn-sm" onclick={askBulkDelete}>Delete Selected</button>
	</div>
{/if}

<div style="display:flex; gap:8px; margin-bottom:16px;">
	<button class="btn btn-secondary btn-sm" onclick={exportJson}>Export JSON</button>
	<button class="btn btn-secondary btn-sm" onclick={openImport}>Import JSON</button>
</div>

{#if showConfirmDelete}
	<div class="modal-overlay" onclick={() => showConfirmDelete = false}>
		<div class="modal" onclick={(e) => e.stopPropagation()}>
			<h3>Confirm Bulk Delete</h3>
			<p style="color:var(--text-secondary); font-size:14px; margin-bottom:8px;">
				Are you sure you want to delete <strong style="color:var(--red)">{confirmDeleteCount}</strong> subjects? This cannot be undone.
			</p>
			<div class="modal-actions">
				<button class="btn btn-secondary" onclick={() => showConfirmDelete = false}>Cancel</button>
				<button class="btn btn-danger" onclick={confirmBulkDelete}>Delete {confirmDeleteCount} Subjects</button>
			</div>
		</div>
	</div>
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

{#if showImport}
	<div class="modal-overlay" onclick={() => showImport = false}>
		<div class="modal" onclick={(e) => e.stopPropagation()}>
			<h3>Import Subjects (JSON)</h3>
			<p style="color:var(--text-secondary); font-size:13px; margin-bottom:12px;">
				Paste JSON or upload a file. Format: array of subject objects or &#123;"items": [...]&#125;.
			</p>
			{#if importError}<div class="error-msg">{importError}</div>{/if}
			<div class="form-group" style="margin-bottom:12px;">
				<label>Upload JSON File</label>
				<input type="file" accept=".json" class="form-input" onchange={handleFileImport} />
			</div>
			<div class="form-group">
				<label>Or paste JSON below</label>
				<textarea class="form-input" bind:value={importJson} rows="8" style="font-family:monospace;font-size:12px;" placeholder="Paste JSON array of subjects here..."></textarea>
			</div>
			<div class="modal-actions">
				<button class="btn btn-secondary" onclick={() => showImport = false}>Cancel</button>
				<button class="btn btn-primary" onclick={handleImport} disabled={!importJson.trim()}>Import</button>
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
					<th style="width:40px;">
						<input type="checkbox" checked={allCurrentSelected()} onchange={toggleSelectAll} />
					</th>
					<th>Icon</th>
					<th>ID</th>
					<th>Display Name</th>
					<th>Class Level</th>
					<th>Sort Order</th>
					<th>Actions</th>
				</tr>
			</thead>
			<tbody>
				{#each filteredItems() as item}
					<tr class:row-selected={selectedIds.has(item.id)}>
						<td>
							<input type="checkbox" checked={selectedIds.has(item.id)} onchange={() => toggleSelect(item.id)} />
						</td>
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