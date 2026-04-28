<script lang="ts">
	import { getManifest, upsertManifest, deleteManifest, bulkDeleteManifest, type ManifestItem } from '$lib/api';

	let items = $state<ManifestItem[]>([]);
	let total = $state(0);
	let loading = $state(true);
	let error = $state('');
	let successMsg = $state('');
	let showForm = $state(false);
	let showImport = $state(false);
	let editingItem = $state<ManifestItem | null>(null);
	let searchQuery = $state('');
	let selectedIds = $state<Set<string>>(new Set());

	let formId = $state('');
	let formTitle = $state('');
	let formType = $state('past_paper');
	let formClassLevel = $state(10);
	let formSubject = $state('Mathematics');
	let formYear = $state<number | ''>('');
	let formFileUrl = $state('');
	let formLanguage = $state('en');
	let formTags = $state('');

	let importJson = $state('');
	let importError = $state('');

	let showConfirmDelete = $state(false);
	let confirmDeleteCount = $state(0);

	const TYPES = ['past_paper', 'syllabus', 'textbook', 'notes', 'mcq_pack', 'model_question', 'solution'];
	const SUBJECTS = ['Mathematics', 'Science', 'English', 'Nepali', 'Social Studies', 'Physics', 'Chemistry', 'Biology', 'Computer', 'All'];
	const CLASS_LEVELS = [8, 10, 12];

	let filteredItems = $derived(() => {
		let result = items;
		if (searchQuery) {
			const q = searchQuery.toLowerCase();
			result = result.filter(i => i.title.toLowerCase().includes(q) || i.id.toLowerCase().includes(q) || i.subject.toLowerCase().includes(q) || i.type.toLowerCase().includes(q));
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
			const res = await getManifest(500, 0);
			items = res.items;
			total = res.total;
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
		editingItem = null;
		formId = '';
		formTitle = '';
		formType = 'past_paper';
		formClassLevel = 10;
		formSubject = 'Mathematics';
		formYear = '';
		formFileUrl = '';
		formLanguage = 'en';
		formTags = '';
		showForm = true;
	}

	function openEdit(item: ManifestItem) {
		editingItem = item;
		formId = item.id;
		formTitle = item.title;
		formType = item.type;
		formClassLevel = item.classLevel;
		formSubject = item.subject;
		formYear = item.year ?? '';
		formFileUrl = item.fileUrl ?? '';
		formLanguage = item.language;
		formTags = item.tags.join(', ');
		showForm = true;
	}

	async function handleSave() {
		error = '';
		try {
			const item: ManifestItem = {
				id: formId,
				title: formTitle,
				type: formType,
				classLevel: formClassLevel,
				subject: formSubject,
				year: formYear ? Number(formYear) : null,
				fileUrl: formFileUrl || null,
				sizeBytes: 0,
				checksumSha256: null,
				version: 1,
				updatedAt: Date.now(),
				language: formLanguage,
				tags: formTags.split(',').map(t => t.trim()).filter(Boolean),
			};
			await upsertManifest([item]);
			showForm = false;
			successMsg = editingItem ? 'Content updated' : 'Content created';
			setTimeout(() => successMsg = '', 3000);
			await load();
		} catch (e: any) {
			error = e.message;
		}
	}

	async function handleDelete(id: string) {
		if (!confirm(`Delete "${id}"?`)) return;
		try {
			await deleteManifest(id);
			successMsg = 'Content deleted';
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
			await bulkDeleteManifest([...selectedIds]);
			successMsg = `Deleted ${selectedIds.size} items`;
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
		a.download = `sikai-manifest-${new Date().toISOString().slice(0, 10)}.json`;
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
		a.download = `sikai-manifest-selected-${new Date().toISOString().slice(0, 10)}.json`;
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
			const itemsToImport: ManifestItem[] = Array.isArray(parsed) ? parsed : parsed.items ?? [parsed];
			if (!itemsToImport.length) { importError = 'No items found in JSON'; return; }
			const res = await upsertManifest(itemsToImport);
			showImport = false;
			successMsg = `Imported ${res.upserted} items`;
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
	<h2>Content Manifest</h2>
	<p>Manage downloadable content — past papers, syllabi, textbooks. Host files on GitHub Releases or any CDN and paste the URL here.</p>
</div>

<div style="display:flex; gap:12px; align-items:center; margin-bottom:20px; flex-wrap:wrap;">
	<input class="form-input" style="width:240px;padding:6px 12px;font-size:13px" placeholder="Search content..." bind:value={searchQuery} />
	<span style="color:var(--text-secondary); font-size:13px; margin-left:auto;">{total} items</span>
	<button class="btn btn-primary" onclick={openCreate}>+ Add Content</button>
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
				Are you sure you want to delete <strong style="color:var(--red)">{confirmDeleteCount}</strong> content items? This cannot be undone.
			</p>
			<div class="modal-actions">
				<button class="btn btn-secondary" onclick={() => showConfirmDelete = false}>Cancel</button>
				<button class="btn btn-danger" onclick={confirmBulkDelete}>Delete {confirmDeleteCount} Items</button>
			</div>
		</div>
	</div>
{/if}

{#if showForm}
	<div class="modal-overlay" onclick={() => showForm = false}>
		<div class="modal" onclick={(e) => e.stopPropagation()}>
			<h3>{editingItem ? 'Edit Content' : 'Add Content'}</h3>
			{#if error}<div class="error-msg">{error}</div>{/if}
			<div class="form-grid">
				<div class="form-group">
					<label for="cm-id">ID</label>
					<input id="cm-id" class="form-input" bind:value={formId} placeholder="see-2080-math" disabled={!!editingItem} />
				</div>
				<div class="form-group">
					<label for="cm-type">Type</label>
					<select id="cm-type" class="form-input" bind:value={formType}>
						{#each TYPES as t}<option value={t}>{t.replace(/_/g, ' ')}</option>{/each}
					</select>
				</div>
				<div class="form-group full">
					<label for="cm-title">Title</label>
					<input id="cm-title" class="form-input" bind:value={formTitle} placeholder="SEE 2080 Mathematics — Full Paper" />
				</div>
				<div class="form-group">
					<label for="cm-class">Class Level</label>
					<select id="cm-class" class="form-input" bind:value={formClassLevel}>
						{#each CLASS_LEVELS as c}<option value={c}>Class {c}</option>{/each}
					</select>
				</div>
				<div class="form-group">
					<label for="cm-subject">Subject</label>
					<select id="cm-subject" class="form-input" bind:value={formSubject}>
						{#each SUBJECTS as s}<option value={s}>{s}</option>{/each}
					</select>
				</div>
				<div class="form-group">
					<label for="cm-year">Year (optional)</label>
					<input id="cm-year" class="form-input" type="number" bind:value={formYear} placeholder="2080" />
				</div>
				<div class="form-group">
					<label for="cm-lang">Language</label>
					<select id="cm-lang" class="form-input" bind:value={formLanguage}>
						<option value="en">English</option>
						<option value="ne">Nepali</option>
					</select>
				</div>
				<div class="form-group full">
					<label for="cm-url">Download URL (GitHub Releases, CDN, etc.)</label>
					<input id="cm-url" class="form-input" bind:value={formFileUrl} placeholder="https://github.com/OWNER/REPO/releases/download/content/see-2080-math.pdf" />
				</div>
				<div class="form-group full">
					<label for="cm-tags">Tags (comma-separated)</label>
					<input id="cm-tags" class="form-input" bind:value={formTags} placeholder="see, 2080, mathematics" />
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
			<h3>Import Content (JSON)</h3>
			<p style="color:var(--text-secondary); font-size:13px; margin-bottom:12px;">
				Paste JSON or upload a file. Format: array of manifest items or &#123;"items": [...]&#125;.
			</p>
			{#if importError}<div class="error-msg">{importError}</div>{/if}
			<div class="form-group" style="margin-bottom:12px;">
				<label>Upload JSON File</label>
				<input type="file" accept=".json" class="form-input" onchange={handleFileImport} />
			</div>
			<div class="form-group">
				<label>Or paste JSON below</label>
				<textarea class="form-input" bind:value={importJson} rows="8" style="font-family:monospace;font-size:12px;" placeholder="Paste JSON array of manifest items here..."></textarea>
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
{:else if items.length === 0}
	<div class="empty-state">
		<div class="icon">▤</div>
		<h3>No content yet</h3>
		<p>Add past papers, syllabi, and textbooks</p>
	</div>
{:else}
	<div class="table-wrap animate-in">
		<table>
			<thead>
				<tr>
					<th style="width:40px;">
						<input type="checkbox" checked={allCurrentSelected()} onchange={toggleSelectAll} />
					</th>
					<th>ID</th>
					<th>Title</th>
					<th>Type</th>
					<th>Class</th>
					<th>Subject</th>
					<th>Year</th>
					<th>URL</th>
					<th>Actions</th>
				</tr>
			</thead>
			<tbody>
				{#each filteredItems() as item}
					<tr class:row-selected={selectedIds.has(item.id)}>
						<td>
							<input type="checkbox" checked={selectedIds.has(item.id)} onchange={() => toggleSelect(item.id)} />
						</td>
						<td style="font-family:monospace; font-size:12px; color:var(--accent)">{item.id}</td>
						<td style="color:var(--text)">{item.title}</td>
						<td><span class="badge badge-accent">{item.type.replace(/_/g, ' ')}</span></td>
						<td>{item.classLevel}</td>
						<td>{item.subject}</td>
						<td>{item.year ?? '—'}</td>
						<td style="max-width:180px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; font-size:12px">
							{item.fileUrl ? '✓ ' + item.fileUrl.split('/').pop() : '—'}
						</td>
						<td>
							<button class="btn btn-secondary btn-sm" onclick={() => openEdit(item)}>Edit</button>
							<button class="btn btn-danger btn-sm" onclick={() => handleDelete(item.id)}>Delete</button>
						</td>
					</tr>
				{/each}
			</tbody>
		</table>
	</div>
{/if}